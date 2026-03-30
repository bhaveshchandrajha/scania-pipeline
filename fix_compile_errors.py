#!/usr/bin/env python3
"""
Fix Java compilation errors in an existing application by sending the compiler
error log and current file contents to the LLM and applying the corrected code.

USAGE:
  export ANTHROPIC_API_KEY=sk-ant-...
  python3 fix_compile_errors.py <project_dir> <build_log_file> [--model claude-opus-4-6]

  Model: Defaults to Claude Opus if API key has access, else claude-sonnet-4-5.
  Use --model or ANTHROPIC_MODEL to override.

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
    # Relative to module dir: src/main/java/.../File.java or src/test/java/.../File.java
    rel_main = re.compile(r"(?:\[\s*ERROR\s*\]\s*)?(?P<path>src/main/java[^\s:]*\.java)")
    rel_test = re.compile(r"(?:\[\s*ERROR\s*\]\s*)?(?P<path>src/test/java[^\s:]*\.java)")
    for line in build_log.splitlines():
        for pattern in (abs_pattern, rel_main, rel_test):
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


def extract_types_from_error_text(error_snippet: str, project_dir: Path) -> List[Path]:
    """
    When errors mention constructor/record/type mismatches, include entity and DTO files
    so the LLM sees exact field types (e.g. Claim.getG71170() returns int, ClaimListItemDto expects int for statusCode).
    """
    # Match type names from "constructor X", "record X" in error messages
    seen: Set[str] = set()
    for pattern in [
        r"constructor\s+([A-Za-z][A-Za-z0-9_]*)",
        r"record\s+([A-Za-z][A-Za-z0-9_]*)",
        r"in\s+record\s+([A-Za-z][A-Za-z0-9_]*)",
    ]:
        for m in re.finditer(pattern, error_snippet, re.IGNORECASE):
            name = m.group(1).strip()
            if len(name) > 2 and name not in seen:
                seen.add(name)
    if not seen:
        return []

    src_root = project_dir / "src" / "main" / "java"
    paths: List[Path] = []
    for java_file in src_root.rglob("*.java"):
        try:
            content = java_file.read_text(encoding="utf-8", errors="ignore")
            # Match: public class X, public record X, public interface X
            for name in seen:
                if re.search(rf"public\s+(?:class|record|interface)\s+{name}\b", content):
                    p = java_file.resolve()
                    if p not in paths:
                        paths.append(p)
                    break
        except Exception:
            continue
    return paths


def extract_dtos_from_error_files(
    error_files: List[Path],
    error_snippet: str,
    project_dir: Path,
) -> List[Path]:
    """
    When errors are "incompatible types" or "bad type in conditional expression" at a call site,
    scan the error file(s) for constructor calls like new ClaimListItemDto(...) and include
    those record/DTO files so the LLM sees the exact parameter order.
    """
    # Only when error suggests constructor/argument mismatch
    if "incompatible types" not in error_snippet.lower() and "cannot be converted to" not in error_snippet.lower():
        return []

    seen: Set[str] = set()
    # Match: new ClaimListItemDto( or new SomeRecord(
    constructor_re = re.compile(r"\bnew\s+([A-Za-z][A-Za-z0-9_]*)\s*\(")
    for path in error_files:
        try:
            content = path.read_text(encoding="utf-8", errors="ignore")
            for m in constructor_re.finditer(content):
                name = m.group(1).strip()
                if len(name) > 2 and name not in seen:
                    seen.add(name)
        except Exception:
            continue

    if not seen:
        return []

    src_root = project_dir / "src" / "main" / "java"
    paths: List[Path] = []
    for java_file in src_root.rglob("*.java"):
        try:
            content = java_file.read_text(encoding="utf-8", errors="ignore")
            for name in seen:
                if re.search(rf"public\s+(?:class|record|interface)\s+{name}\b", content):
                    p = java_file.resolve()
                    if p not in paths:
                        paths.append(p)
                    break
        except Exception:
            continue
    return paths


def extract_entities_from_test_files(
    error_files: List[Path],
    error_snippet: str,
    project_dir: Path,
) -> List[Path]:
    """
    When test files have "int cannot be converted to java.math.BigDecimal",
    scan the test file for variable types (e.g. Invoice inv) and include entity files.
    """
    if "cannot be converted to" not in error_snippet.lower() or "BigDecimal" not in error_snippet:
        return []

    paths: List[Path] = []
    seen: Set[str] = set()
    src_main = project_dir / "src" / "main" / "java"

    for path in error_files:
        if "src/test" not in str(path).replace("\\", "/"):
            continue
        try:
            content = path.read_text(encoding="utf-8", errors="ignore")
            # Match: Invoice inv, Claim claim, SomeEntity foo
            for m in re.finditer(r"\b([A-Z][A-Za-z0-9]*)\s+([a-z][A-Za-z0-9]*)\s*[;=]", content):
                type_name = m.group(1)
                if type_name in ("String", "Integer", "Long", "List", "Optional", "Map", "Set"):
                    continue
                if type_name in seen:
                    continue
                seen.add(type_name)
                # Resolve entity path (assume same package base: com.scania.warranty.domain.X)
                for candidate_dir in [src_main / "com" / "scania" / "warranty" / "domain",
                                     src_main]:
                    candidate = candidate_dir / f"{type_name}.java"
                    if candidate.exists():
                        p = candidate.resolve()
                        if p not in paths:
                            paths.append(p)
                        break
        except Exception:
            continue
    return paths


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

3. **Repository methods expect (String, String)**: `findByCompanyAndClaimNr(String pkz, String claimNr)` — pass `claim.getG71050()` or `claimNumber` directly. Do NOT use `Integer.parseInt(claim.getG71050())` — that passes int where String is required.

4. **Record/constructor argument order and types**: When fixing ClaimListItemDto or similar records, match the EXACT constructor parameter order and types from the record definition. Example: if the record has `int statusCode, String statusText, String demandCode, int errorCount, String colorIndicator`, pass: (int) for statusCode, (String) for statusText, (String) for demandCode, (int) for errorCount (e.g. errors.size()), (String) for colorIndicator. ALWAYS read the record definition in the files above to get the exact order — do NOT guess.

5. **Entity files are included**: When fixing DataInitializer or a service that uses Invoice/Claim, the entity file is in the list above. Read it to see the exact setter names and getter return types.

6. **int cannot be converted to java.math.BigDecimal**: When the entity setter expects BigDecimal (e.g. setAhk820(BigDecimal)) but test/factory code passes an int literal (0), replace with BigDecimal.ZERO or BigDecimal.ONE. Example: `inv.setAhk820(0)` → `inv.setAhk820(BigDecimal.ZERO)`. Ensure `import java.math.BigDecimal;` is present.

7. **UnknownPathException / PathElementException / Could not resolve attribute**: When a repository @Query references attributes that do not exist on the entity (e.g. e.epaKey000 but entity has epa000), fix the JPQL to use the entity's actual field names. For ExtendedPartAgreement: epaKey000→epa000, epaKey040→epa040, epaKey050→epa050, epaKey060→epa060, epaVariant→epaType. For other entities, read the entity class to see the exact field names and align the @Query.

8. **Minimal edits**: Change ONLY what is broken. Do not refactor. Preserve all other code.

9. **No new text**: Output ONLY file markers and code blocks. No explanations, no summaries. Text after ``` corrupts the file.

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
    parser.add_argument("--model", default=None, help="Anthropic model. Default: Claude Opus if available, else claude-sonnet-4-5")
    parser.add_argument("--max-tokens", type=int, default=32000)
    parser.add_argument("--propose-only", action="store_true", help="Return suggested fixes as JSON without writing to disk (for HITL)")
    args = parser.parse_args()

    from anthropic_env import load_anthropic_from_env_files

    load_anthropic_from_env_files(Path(__file__).resolve().parent)

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

    # Include entity/DTO types from error text (e.g. ClaimListItemDto, Claim) for type-mismatch fixes
    entity_dto_files = extract_types_from_error_text(error_snippet, project_dir)
    # When "incompatible types" at call site, scan error file for new XDto(...) and include that DTO
    dto_from_error_files = extract_dtos_from_error_files(error_files, error_snippet, project_dir)
    # When test files have int->BigDecimal errors, include entity types (Invoice, etc.) from test vars
    entity_from_test = extract_entities_from_test_files(error_files, error_snippet, project_dir)
    all_files_set: Set[Path] = set(error_files) | set(related_type_files) | set(entity_dto_files) | set(dto_from_error_files) | set(entity_from_test)
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

    # Resolve model: explicit --model, else ANTHROPIC_MODEL env, else Opus if available, else Sonnet
    model = args.model or os.environ.get("ANTHROPIC_MODEL")
    if not model:
        try:
            from check_anthropic_models import check_models
            model_info = check_models()
            if model_info.get("opus_available") and model_info.get("opus_model"):
                model = model_info["opus_model"]
                print(f"Using Claude Opus ({model}) for compile error fixes", file=sys.stderr)
            else:
                model = "claude-sonnet-4-5"
                print(f"Opus not available; using {model}", file=sys.stderr)
        except Exception as e:
            model = "claude-sonnet-4-5"
            print(f"Model check failed ({e}); using {model}", file=sys.stderr)

    import anthropic

    client = anthropic.Anthropic(api_key=api_key)
    response = client.messages.create(
        model=model,
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

