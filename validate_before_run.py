#!/usr/bin/env python3
"""
Pre-run validation: catch dynamic issues before starting the application.

Run after migration (or as part of build-application) to detect:
- Duplicate column mapping (Hibernate MappingException)
- Not a managed type (JpaRepository using key type instead of entity)
- ApplicationContext load failures
- Compilation errors

Usage:
  python3 validate_before_run.py [warranty_demo]
  python3 validate_before_run.py --quick   # compile only, no tests

Exit codes:
  0 = validation passed (compile + test OK)
  1 = validation failed
"""

import argparse
import re
import subprocess
import sys
from pathlib import Path

ROOT_DIR = Path(__file__).resolve().parent

# Known error patterns that indicate dynamic/runtime issues
KNOWN_ERROR_PATTERNS = [
    (r"Column ['\"](\w+)['\"] is duplicated", "Duplicate column mapping: use RESERVE1, RESERVE2, etc."),
    (r"Not a managed type: class (\S+)", "Repository uses key/embeddable type instead of entity"),
    (r"Failed to load ApplicationContext", "Spring context failed (often JPA/repository/entity issue)"),
    (r"BeanCreationException.*entityManagerFactory", "JPA/Hibernate configuration or entity mapping error"),
    (r"MappingException", "Hibernate entity mapping error"),
]


def run_validation(proj_path: Path, quick: bool = False):
    """Run mvn compile (and test if not quick). Return (success, output)."""
    if quick:
        proc = subprocess.run(
            ["mvn", "-f", "pom.xml", "-q", "compile"],
            cwd=str(proj_path),
            capture_output=True,
            text=True,
            timeout=120,
        )
    else:
        proc = subprocess.run(
            ["mvn", "-f", "pom.xml", "-q", "test"],
            cwd=str(proj_path),
            capture_output=True,
            text=True,
            timeout=300,
        )
    output = (proc.stdout or "") + (proc.stderr or "")
    return proc.returncode == 0, output


def detect_known_issues(output: str) -> list[tuple[str, str]]:
    """Return list of (pattern_match, hint) for known error patterns."""
    found = []
    for pattern, hint in KNOWN_ERROR_PATTERNS:
        m = re.search(pattern, output)
        if m:
            found.append((m.group(0)[:80], hint))
    return found


def main() -> int:
    parser = argparse.ArgumentParser(description="Validate warranty_demo before run")
    parser.add_argument("project", nargs="?", default="warranty_demo", help="Project directory")
    parser.add_argument("--quick", action="store_true", help="Compile only, skip tests")
    args = parser.parse_args()

    proj_path = Path(args.project)
    if not proj_path.is_absolute():
        proj_path = ROOT_DIR / proj_path
    if not proj_path.is_dir():
        print(f"Error: project directory not found: {proj_path}", file=sys.stderr)
        return 1

    mode = "compile" if args.quick else "compile + test"
    print(f"Validating {proj_path} ({mode})...", file=sys.stderr)
    success, output = run_validation(proj_path, quick=args.quick)

    if success:
        print("Validation passed.", file=sys.stderr)
        return 0

    issues = detect_known_issues(output)
    if issues:
        print("\nKnown issues detected:", file=sys.stderr)
        for match, hint in issues:
            print(f"  • {match}", file=sys.stderr)
            print(f"    → {hint}", file=sys.stderr)

    # Show last 40 lines of output
    lines = output.strip().splitlines()
    tail = lines[-40:] if len(lines) > 40 else lines
    print("\n--- Last output ---", file=sys.stderr)
    for line in tail:
        print(line, file=sys.stderr)
    return 1


if __name__ == "__main__":
    sys.exit(main())
