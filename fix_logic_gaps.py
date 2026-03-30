#!/usr/bin/env python3
"""
Fix logic completeness gaps in an existing Pure Java app by sending gap details
and current file contents to the LLM and applying the corrected code.

Use after migration when validation reports < 100% logic (stub methods, empty loops).
Reads app_dir and context_file, runs the validator to get gap_details, then asks
the LLM to fix only those issues and overwrites the affected files.

USAGE:
  export ANTHROPIC_API_KEY=sk-ant-...
  python3 fix_logic_gaps.py <app_dir> <context_file> [--model claude-sonnet-4-5]

  Example:
  python3 fix_logic_gaps.py HS1210_n404_pure_java context_index/HS1210_n404.json
"""

import json
import os
import re
import sys
from pathlib import Path
from typing import Dict, List, Any, Optional, Tuple

# Add project root for imports
ROOT = Path(__file__).resolve().parent
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from validate_pure_java import PureJavaValidator


def run_validation(app_dir: Path, context_file: Path) -> Dict[str, Any]:
    """Run Pure Java validator and return full results (including logic_completeness)."""
    validator = PureJavaValidator(app_dir, context_file)
    results = validator.validate_all()
    return results


def get_files_to_fix(app_dir: Path, gap_details: List[Dict[str, Any]]) -> List[Tuple[str, str]]:
    """
    From gap_details, resolve each 'file' to a path under app_dir and return
    list of (relative_path, content). Only includes concrete .java files we can read.
    """
    seen = set()
    out: List[Tuple[str, Path]] = []
    for g in gap_details:
        f = g.get("file") or ""
        if not f.endswith(".java") or f in seen:
            continue
        # Resolve: can be "ClaimSearchService.java" or "service/ClaimSearchService.java"
        if "/" in f:
            candidate = app_dir / f
            if candidate.exists():
                out.append((f, candidate))
                seen.add(f)
                continue
        # Find by filename under app_dir
        for path in app_dir.rglob(f):
            if path.is_file():
                try:
                    rel = path.relative_to(app_dir)
                    rel_str = str(rel).replace("\\", "/")
                    if rel_str not in seen:
                        out.append((rel_str, path))
                        seen.add(rel_str)
                except ValueError:
                    pass
                break
    # Load content for each
    result: List[Tuple[str, str]] = []
    for rel_str, path in out:
        try:
            content = path.read_text(encoding="utf-8")
            result.append((rel_str, content))
        except Exception:
            continue
    return result


def build_fix_prompt(
    gap_details: List[Dict[str, Any]],
    action_required: str,
    files_with_content: List[Tuple[str, str]],
    context: Dict[str, Any],
) -> str:
    """Build the prompt that asks the LLM to fix only the listed logic gaps."""
    narrative = (context.get("narrative") or "")[:4000]
    rpg_snippet = (context.get("rpgSnippet") or "")[:6000]
    gaps_text = "\n".join(
        f"- **{g.get('file', '?')}** ({g.get('type', '?')}): {g.get('description', '')}\n  → {g.get('action_required', '')}"
        for g in gap_details
    )
    files_section = "\n\n".join(
        f"### === {rel} ===\n```java\n{content}\n```"
        for rel, content in files_with_content
    )
    return f"""You are fixing logic completeness issues in an existing Pure Java application. The validator reported the following gaps. Fix ONLY these issues; do not change unrelated code.

## Gaps to fix (mandatory)

{action_required}

{gaps_text}

## Context (use to implement real logic)

**Narrative:**
{narrative}

**RPG snippet (for control flow / field mapping):**
```
{rpg_snippet}
```

## Current Java files to correct

Fix the issues in these files. Replace stub returns with real business logic (entity fields, repository calls, comparisons). Replace empty for/while loops with real loop bodies (map entities, save, copy fields). Keep the same package/class structure and imports.

{files_section}

## Your response

Return the FULL corrected Java file(s) in the same format. For each file use exactly:
### === <path> ===
```java
<full corrected content>
```

Use the same path strings as above (e.g. service/ClaimSearchService.java). Return every file that was listed above, with complete implementations (no stubs, no empty loops).

CRITICAL: Output ONLY the file markers and code blocks. Do NOT add any explanatory text, summaries, or markdown after the code blocks. Any text after a closing ``` will corrupt the file."""


def parse_fixed_files(llm_response: str) -> Dict[str, str]:
    """Parse LLM response into path -> content using the same markers as migrate_to_pure_java."""
    files = {}
    current_file = None
    current_content = []
    in_code_block = False
    lines = llm_response.split("\n")
    i = 0
    while i < len(lines):
        line = lines[i]
        marker_patterns = [
            r'###\s*===\s*(.+?\.java)\s*===',
            r'##\s*===\s*(.+?\.java)\s*===',
            r'//\s*===\s*(.+?\.java)\s*===',
        ]
        file_found = False
        for pattern in marker_patterns:
            m = re.search(pattern, line)
            if m:
                if current_file and current_content:
                    files[current_file] = "\n".join(current_content).strip()
                current_file = m.group(1).strip()
                current_content = []
                in_code_block = False
                file_found = True
                break
        if file_found:
            i += 1
            continue
        if line.strip().startswith("```"):
            if in_code_block and current_file and current_content:
                files[current_file] = "\n".join(current_content).strip()
            in_code_block = not in_code_block
            current_content = []  # Reset; next file's content starts after opening ```
            i += 1
            continue
        if in_code_block:
            current_content.append(line)
        i += 1
    if current_file and current_content:
        files[current_file] = "\n".join(current_content).strip()
    return files


def main() -> None:
    import argparse
    parser = argparse.ArgumentParser(description="Fix logic completeness gaps using LLM")
    parser.add_argument("app_dir", type=Path, help="Pure Java app directory (e.g. HS1210_n404_pure_java)")
    parser.add_argument("context_file", type=Path, help="Context JSON (e.g. context_index/HS1210_n404.json)")
    parser.add_argument("--model", default="claude-sonnet-4-5", help="Anthropic model")
    parser.add_argument("--max-tokens", type=int, default=32000)
    args = parser.parse_args()

    from anthropic_env import load_anthropic_from_env_files

    load_anthropic_from_env_files(Path(__file__).resolve().parent)

    api_key = os.environ.get("ANTHROPIC_API_KEY")
    if not api_key:
        print("ANTHROPIC_API_KEY env var is not set", file=sys.stderr)
        sys.exit(1)

    app_dir = args.app_dir.resolve()
    context_file = args.context_file.resolve()
    if not app_dir.exists():
        print(f"App dir not found: {app_dir}", file=sys.stderr)
        sys.exit(2)
    if not context_file.exists():
        print(f"Context file not found: {context_file}", file=sys.stderr)
        sys.exit(3)

    with open(context_file, "r", encoding="utf-8") as f:
        context = json.load(f)

    # Run validator to get gap details
    print("Running validation to collect gaps...", file=sys.stderr)
    results = run_validation(app_dir, context_file)
    lc = results.get("logic_completeness", {})
    if lc.get("skipped"):
        print("Logic completeness was skipped (no context). Nothing to fix.", file=sys.stderr)
        print(json.dumps({"success": True, "files_fixed": [], "message": "No context; nothing to fix."}))
        sys.exit(0)
    score = lc.get("score", 0)
    gap_details = lc.get("gap_details", [])
    action_required = lc.get("action_required", "")

    if score >= 100 or not gap_details:
        print("No logic gaps to fix (score already 100% or no gap details).", file=sys.stderr)
        print(json.dumps({"success": True, "files_fixed": [], "message": "No logic gaps to fix (score 100% or no gap details)."}))
        sys.exit(0)

    files_to_fix = get_files_to_fix(app_dir, gap_details)
    if not files_to_fix:
        print("No concrete Java files could be resolved from gap_details. Cannot fix.", file=sys.stderr)
        print(json.dumps({"success": False, "error": "No concrete Java files resolved from gap_details.", "files_fixed": []}))
        sys.exit(4)

    prompt = build_fix_prompt(gap_details, action_required, files_to_fix, context)
    print(f"Calling LLM to fix {len(files_to_fix)} file(s)...", file=sys.stderr)

    import anthropic
    client = anthropic.Anthropic(api_key=api_key)
    response = client.messages.create(
        model=args.model,
        max_tokens=args.max_tokens,
        temperature=0.2,
        messages=[{"role": "user", "content": prompt}],
        timeout=600.0,
    )
    text = ""
    for block in response.content:
        if getattr(block, "type", None) == "text":
            text += block.text

    fixed = parse_fixed_files(text)
    if not fixed:
        print("LLM did not return any parsed files. Check response format.", file=sys.stderr)
        print(json.dumps({"success": False, "error": "LLM did not return any parsed files.", "files_fixed": []}))
        sys.exit(5)

    written = []
    for rel_path, content in fixed.items():
        rel_path = rel_path.replace("\\", "/")
        target = app_dir / rel_path
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(content, encoding="utf-8")
        written.append(rel_path)
        print(f"Wrote {rel_path}", file=sys.stderr)

    # Output JSON for API consumer
    out = {
        "success": True,
        "files_fixed": written,
        "gap_count": len(gap_details),
        "message": f"Fixed {len(written)} file(s). Re-run validation to confirm 100%.",
    }
    print(json.dumps(out))


if __name__ == "__main__":
    main()
