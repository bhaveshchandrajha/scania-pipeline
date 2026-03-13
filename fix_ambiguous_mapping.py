#!/usr/bin/env python3
"""
Fix Spring MVC ambiguous mapping errors (duplicate endpoint paths).

Two modes:
1. Proactive: Scans web controllers for duplicate @RequestMapping paths, fixes by
   changing the path of the duplicate (appends suffix like -from-request).
2. Reactive: Parses test/build output for "Ambiguous mapping" errors and fixes
   the conflicting controller.

USAGE:
  python3 fix_ambiguous_mapping.py <project_dir> [--test-output <path>]

Called automatically from the build pipeline before tests and after test failure.
"""

import re
import sys
from pathlib import Path
from typing import Dict, List, Optional, Tuple


def _extract_class_mappings(content: str, class_name: str, base_path: str) -> List[Tuple[str, str, int]]:
    """
    Extract (http_method, full_path, line_no) for each mapping in the class.
    base_path comes from @RequestMapping on the class.
    """
    mappings = []
    base = (base_path or "").strip()
    if base and not base.startswith("/"):
        base = "/" + base
    # Method-level @PostMapping("/create") or @PostMapping(value = "/create")
    for m in re.finditer(
        r"@(Post|Get|Put|Delete|Patch)Mapping\s*\((?:value\s*=\s*)?[\"']([^\"']*)[\"']\s*\)",
        content,
    ):
        method, path = m.group(1).upper(), m.group(2).strip()
        if not path.startswith("/"):
            path = "/" + path
        full = (base.rstrip("/") + path).replace("//", "/")
        mappings.append((method, full, content[: m.start()].count("\n") + 1))
    return mappings


def _extract_class_request_mapping(content: str) -> str:
    """Extract path from @RequestMapping on class."""
    m = re.search(r'@RequestMapping\s*\(\s*(?:value\s*=\s*)?["\']([^"\']*)["\']\s*\)', content)
    return (m.group(1) or "").strip() if m else ""


def _scan_controllers(proj_path: Path) -> Dict[str, List[Tuple[str, str, Path, int]]]:
    """
    Scan all controller files under src/main/java. Returns:
    path_key -> [(class_name, method, file_path, line_no), ...]
    path_key = "POST /api/claims/create" etc.
    """
    path_to_sources: Dict[str, List[Tuple[str, str, Path, int]]] = {}
    java_root = proj_path / "src" / "main" / "java"
    if not java_root.exists():
        return path_to_sources
    for p in java_root.rglob("*.java"):
        if "Controller" in p.name or "web" in str(p):
            _scan_file(p, path_to_sources)
    return path_to_sources


def _scan_file(p: Path, path_to_sources: Dict[str, List[Tuple[str, str, Path, int]]]) -> None:
    try:
        content = p.read_text(encoding="utf-8", errors="ignore")
    except Exception:
        return
    class_match = re.search(r"public\s+class\s+(\w+)", content)
    if not class_match:
        return
    class_name = class_match.group(1)
    base = _extract_class_request_mapping(content)
    for method, full_path, line_no in _extract_class_mappings(content, class_name, base):
        key = f"{method} {full_path}"
        if key not in path_to_sources:
            path_to_sources[key] = []
        path_to_sources[key].append((class_name, method, p, line_no))


def _parse_ambiguous_from_output(output: str) -> Optional[Tuple[str, str, str]]:
    """
    Parse "Ambiguous mapping. Cannot map 'X' ... to {POST [/api/claims/create]}:
    There is already 'Y' ... mapped."
    Returns (path, conflicting_controller, existing_controller) or None.
    """
    m = re.search(
        r"Cannot map ['\"]?(\w+)['\"]?.*?to\s*\{(\w+)\s*\[([^\]]+)\]\}.*?"
        r"There is already ['\"]?(\w+)['\"]?",
        output,
        re.DOTALL,
    )
    if m:
        conflicting, method, path, existing = m.group(1), m.group(2), m.group(3), m.group(4)
        return (f"{method} {path}", conflicting, existing)
    return None


def _fix_duplicate(
    file_path: Path,
    class_name: str,
    method: str,
    old_path: str,
    path_to_sources: Dict[str, List[Tuple[str, str, Path, int]]],
) -> bool:
    """
    Change the mapping in file_path for the given class/method from old_path
    to a new path with suffix (e.g. /create -> /create-from-request).
    """
    content = file_path.read_text(encoding="utf-8", errors="ignore")
    # old_path is like "/api/claims/create"; segment is "create"
    segment = Path(old_path).name
    new_segment = segment + "-from-request"
    # Match @PostMapping("/create") or @PostMapping(value = "/create") - path may have leading /
    pattern = rf'@(Post|Get|Put|Delete|Patch)Mapping\s*\(\s*(?:value\s*=\s*)?["\']/?{re.escape(segment)}["\']\s*\)'
    replacement = rf'@\1Mapping("/' + new_segment + '")'
    new_content, n = re.subn(pattern, replacement, content)
    if n > 0:
        file_path.write_text(new_content, encoding="utf-8")
        return True
    return False


def run_fix(proj_path: Path, test_output: Optional[str] = None) -> Tuple[int, str]:
    """
    Run the ambiguous mapping fixer.
    Returns (count of files fixed, message).
    """
    proj_path = Path(proj_path).resolve()
    path_to_sources = _scan_controllers(proj_path)

    # Build list of duplicates
    duplicates: List[Tuple[str, List[Tuple[str, str, Path, int]]]] = []
    for key, sources in path_to_sources.items():
        if len(sources) > 1:
            duplicates.append((key, sources))

    # If test output provided, also check for ambiguous mapping error
    if test_output:
        parsed = _parse_ambiguous_from_output(test_output)
        if parsed:
            path_key, conflicting, existing = parsed
            if path_key not in path_to_sources:
                # Re-scan with broader search
                for p in (proj_path / "src" / "main" / "java").rglob("*.java"):
                    if "Controller" in p.name:
                        _scan_file(p, path_to_sources)
            if path_key in path_to_sources and len(path_to_sources[path_key]) > 1:
                dup_key = path_key
                if not any(d[0] == dup_key for d in duplicates):
                    duplicates.append((dup_key, path_to_sources[dup_key]))

    fixed_count = 0
    for path_key, sources in duplicates:
        # Change the "second" controller (typically the one that caused the conflict)
        # Heuristic: prefer keeping the one with shorter name or "ClaimController" over "ClaimCreationController"
        sources_sorted = sorted(sources, key=lambda s: (s[0] != "ClaimController", s[0]))
        to_change = sources_sorted[1]  # Change the second one
        class_name, method, file_path, _ = to_change
        full_path = path_key.split(" ", 1)[1]
        if _fix_duplicate(file_path, class_name, method, full_path, path_to_sources):
            fixed_count += 1

    msg = f"Fixed {fixed_count} ambiguous mapping(s)." if fixed_count else "No ambiguous mappings found."
    return fixed_count, msg


def main():
    if len(sys.argv) < 2:
        print("Usage: fix_ambiguous_mapping.py <project_dir> [--test-output <path>]", file=sys.stderr)
        sys.exit(1)
    proj = Path(sys.argv[1])
    test_output = None
    if "--test-output" in sys.argv:
        idx = sys.argv.index("--test-output")
        if idx + 1 < len(sys.argv):
            test_output = Path(sys.argv[idx + 1]).read_text(encoding="utf-8", errors="ignore")
    count, msg = run_fix(proj, test_output)
    print(msg)
    sys.exit(0 if count >= 0 else 1)


if __name__ == "__main__":
    main()
