#!/usr/bin/env python3
"""
Fix Java compilation errors in an existing application by sending the compiler
error log and current file contents to the LLM and applying the corrected code.

USAGE:
  export ANTHROPIC_API_KEY=sk-ant-...
  python3 fix_compile_errors.py <project_dir> <build_log_file> [--model claude-sonnet-4-5]

This is intended to be called automatically from the global-context UI server
after a failed Maven build.
"""

import json
import os
import re
import sys
from pathlib import Path
from typing import Dict, List, Tuple, Set


ROOT = Path(__file__).resolve().parent
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from fix_logic_gaps import parse_fixed_files  # reuse robust parser for LLM responses


def extract_error_blocks(build_log: str) -> str:
    """
    Extract the LAST (most recent) compilation error block from a Maven build log.
    When the log contains multiple build attempts, only the most recent errors matter.
    """
    lines = build_log.splitlines()
    blocks: List[List[str]] = []
    current: List[str] = []
    for line in lines:
        if "COMPILATION ERROR" in line:
            if current:
                blocks.append(current)
            current = [line]
            continue
        if current:
            current.append(line)
    if current:
        blocks.append(current)
    # Use the last block (most recent build's errors)
    collected = blocks[-1] if blocks else lines
    if not collected:
        collected = lines
    text = "\n".join(collected)
    return text[-25000:]  # Keep full recent errors


def extract_error_files(build_log: str, project_dir: Path) -> List[Path]:
    """
    From the Maven build log, extract absolute or relative .java file paths
    referenced in error messages and resolve them under project_dir.
    """
    files: List[Path] = []
    seen: Set[Path] = set()
    # Absolute path: /path/to/File.java or C:\path\File.java
    abs_pattern = re.compile(r"(?:\[\s*ERROR\s*\]\s*)?(?P<path>[/\\][^\s:]+\.java|[A-Za-z]:[^\s:]+\.java)")
    # Relative to module dir: src/main/java/.../File.java
    rel_pattern = re.compile(r"(?:\[\s*ERROR\s*\]\s*)?(?P<path>src/main/java[^\s:]*\.java)")
    for line in build_log.splitlines():
        for pattern in (abs_pattern, rel_pattern):
            m = pattern.search(line)
            if not m:
                continue
            raw_path = m.group("path").strip().replace("\\", "/")
            p = Path(raw_path)
            if not p.is_absolute():
                p = project_dir / p
            try:
                p_resolved = p.resolve()
                p_resolved.relative_to(project_dir.resolve())
            except Exception:
                continue
            if p_resolved in seen:
                continue
            seen.add(p_resolved)
            files.append(p_resolved)
            break
    return files


def extract_related_type_files(build_log: str, project_dir: Path) -> List[Path]:
    """
    From error lines like:
      location: variable claimRepository of type com.scania.warranty.repository.ClaimRepository
      location: class com.scania.warranty.service.ClaimCreationService
    infer related types and resolve their source files under src/main/java.
    """
    type_pattern = re.compile(
        r"location:\s+(?:variable|class)\s+[A-Za-z0-9_]+\s+of\s+type\s+([A-Za-z0-9_.]+)"
    )
    class_pattern = re.compile(
        r"location:\s+class\s+([A-Za-z0-9_.]+)"
    )
    fqns: Set[str] = set()
    for line in build_log.splitlines():
        m1 = type_pattern.search(line)
        if m1:
            fqns.add(m1.group(1).strip())
        m2 = class_pattern.search(line)
        if m2:
            fqns.add(m2.group(1).strip())

    src_root = project_dir / "src" / "main" / "java"
    paths: List[Path] = []
    seen_paths: Set[Path] = set()
    for fqn in fqns:
        rel = Path(*fqn.split("."))  # com.scania.warranty.X -> com/scania/warranty/X
        candidate = src_root / f"{rel}.java"
        if candidate.exists():
            p_resolved = candidate.resolve()
            if p_resolved not in seen_paths:
                seen_paths.add(p_resolved)
                paths.append(p_resolved)
    return paths


def build_fix_prompt(error_snippet: str, files_with_content: List[Tuple[str, str]]) -> str:
    """
    Build the prompt that asks the LLM to fix compilation errors only.
    """
    files_section = "\n\n".join(
        f"### === {rel} ===\n```java\n{content}\n```"
        for rel, content in files_with_content
    )
    return f"""You are fixing Java compilation errors. Your output is written directly to files and recompiled. You MUST fix EVERY error so the build succeeds.

## CRITICAL: Fix ALL errors in one pass

The build will fail again if you leave ANY error unfixed. Fix every single error listed below.

## Compiler errors (fix every one)

```
{error_snippet}
```

## Files to correct

{files_section}

## Mandatory rules

1. **cannot find symbol (setter)**: Use the EXACT setter name from the entity. JavaBean: field `reland` → `setReland`, NOT `setReLand`. Field `motornr` → `setMotornr`, NOT `setMotorNr`. Field `fahrgnr` → `setFahrgnr`, NOT `setFahrgNr`. Match the entity's field name exactly (camelCase).

2. **incompatible types (Integer vs String)**: Use the setter that matches the argument type. If you have `parseDate()` returning Integer, use `setRepDatum(Integer)` or `setZulDatum(Integer)`, NOT `setRepairDate(String)`. Check the entity for both overloads.

3. **Entity files are included**: When fixing DataInitializer or a service that uses Invoice/Claim, the entity file is in the list above. Read it to see the exact setter names (e.g. setReland, setReplz, setRetele, setRgsnetto, setWktid, setFv, setFb, setKampagnenr, setSpoorder, setKenav, setKenpe, setKlrberech, setKlrbetrag).

4. **Minimal edits**: Change ONLY what is broken. Do not refactor. Preserve all other code.

5. **No new text**: Output ONLY file markers and code blocks. No explanations, no summaries. Text after ``` corrupts the file.

## Response format

### === <exact path from above>.java ===
```java
<full corrected content>
```

Return ALL files that had errors. Use the exact same path strings as in the files section above. Do NOT add any text after the last ```."""


def main() -> None:
    import argparse

    parser = argparse.ArgumentParser(description="Fix Java compilation errors using LLM")
    parser.add_argument("project_dir", type=Path, help="Project directory containing pom.xml (e.g. warranty_demo)")
    parser.add_argument("build_log_file", type=Path, help="Path to a text file with Maven build output")
    parser.add_argument("--model", default="claude-sonnet-4-5", help="Anthropic model")
    parser.add_argument("--max-tokens", type=int, default=32000)
    parser.add_argument("--propose-only", action="store_true", help="Return suggested fixes as JSON without writing to disk (for HITL)")
    args = parser.parse_args()

    api_key = os.environ.get("ANTHROPIC_API_KEY")
    if not api_key:
        print("ANTHROPIC_API_KEY env var is not set", file=sys.stderr)
        sys.exit(1)

    project_dir = args.project_dir.resolve()
    if not project_dir.exists():
        print(f"Project dir not found: {project_dir}", file=sys.stderr)
        sys.exit(2)

    if not args.build_log_file.exists():
        print(f"Build log file not found: {args.build_log_file}", file=sys.stderr)
        sys.exit(3)

    build_log = args.build_log_file.read_text(encoding="utf-8", errors="ignore")
    error_snippet = extract_error_blocks(build_log)
    error_files = extract_error_files(build_log, project_dir)
    related_type_files = extract_related_type_files(build_log, project_dir)

    all_files_set: Set[Path] = set(error_files) | set(related_type_files)
    all_files: List[Path] = list(all_files_set)

    if not all_files:
        print("No error-related Java or related type files found in build log.", file=sys.stderr)
        print(json.dumps({"success": False, "error": "No Java files found in build log.", "files_fixed": []}))
        sys.exit(4)

    # Collect file contents relative to project_dir
    files_with_content: List[Tuple[str, str]] = []
    for path in all_files:
        try:
            rel = path.resolve().relative_to(project_dir.resolve())
            rel_str = str(rel).replace("\\", "/")
            content = path.read_text(encoding="utf-8", errors="ignore")
            files_with_content.append((rel_str, content))
        except Exception:
            continue

    if not files_with_content:
        print("Could not read any Java file contents for errors.", file=sys.stderr)
        print(json.dumps({"success": False, "error": "Could not read Java file contents.", "files_fixed": []}))
        sys.exit(5)

    prompt = build_fix_prompt(error_snippet, files_with_content)
    print(f"Calling LLM to fix {len(files_with_content)} file(s) based on compilation errors...", file=sys.stderr)

    import anthropic

    client = anthropic.Anthropic(api_key=api_key)
    response = client.messages.create(
        model=args.model,
        max_tokens=args.max_tokens,
        temperature=0.0,
        messages=[{"role": "user", "content": prompt}],
        timeout=300.0,
    )
    text = ""
    for block in response.content:
        if getattr(block, "type", None) == "text":
            text += block.text

    fixed = parse_fixed_files(text)
    if not fixed:
        print("LLM did not return any parsed files. Check response format.", file=sys.stderr)
        print(json.dumps({"success": False, "error": "LLM did not return any parsed files.", "files_fixed": [], "suggestedFixes": None}))
        sys.exit(6)

    if args.propose_only:
        # HITL mode: return suggested fixes as JSON, do not write
        suggested = {k.replace("\\", "/"): v for k, v in fixed.items()}
        out = {
            "success": True,
            "proposeOnly": True,
            "suggestedFixes": suggested,
            "filesCount": len(suggested),
            "message": f"Proposed fixes for {len(suggested)} file(s). Apply via HITL to write.",
        }
        print(json.dumps(out))
        return

    written: List[str] = []
    for rel_path, content in fixed.items():
        rel_path = rel_path.replace("\\", "/")
        target = project_dir / rel_path
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(content, encoding="utf-8")
        written.append(rel_path)
        print(f"Wrote {rel_path}", file=sys.stderr)

    out = {
        "success": True,
        "files_fixed": written,
        "message": f"Fixed {len(written)} file(s) based on compilation errors. Re-run Maven build to confirm.",
    }
    print(json.dumps(out))


if __name__ == "__main__":
    main()

