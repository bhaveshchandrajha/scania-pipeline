#!/usr/bin/env python3
"""
Fix runtime errors (e.g. Spring Boot startup failures) by applying known fixes.

Detects patterns like:
- "does not define an IdClass" -> run fix_idclass, rebuild

USAGE:
  python3 fix_runtime_errors.py <project_dir> [<runtime_log_file>]

If no log file is provided, returns without applying fixes.
Called by the build/run pipeline when application fails to start.
"""

import re
import subprocess
import sys
from pathlib import Path
from typing import Optional, Tuple


def detect_idclass_error(log_content: str) -> bool:
    """Check if log contains IdClass-related startup failure."""
    return "does not define an IdClass" in log_content


def detect_ambiguous_mapping(log_content: str) -> bool:
    """Check if log contains Spring MVC ambiguous mapping error."""
    return "Ambiguous mapping" in log_content


def apply_fixes(project_dir: Path, log_content: str) -> Tuple[bool, str]:
    """
    Apply fixes based on runtime log content.
    Returns (fixed, message).
    """
    project_dir = Path(project_dir).resolve()
    if not project_dir.is_dir():
        return False, f"Project directory not found: {project_dir}"

    if detect_idclass_error(log_content):
        try:
            from fix_idclass import run_fix
            count, messages = run_fix(project_dir)
            if count > 0:
                # Rebuild after fix
                proc = subprocess.run(
                    ["mvn", "-f", "pom.xml", "-T", "1C", "-q", "compile", "-DskipTests"],
                    cwd=str(project_dir),
                    capture_output=True,
                    text=True,
                    timeout=300,
                )
                if proc.returncode == 0:
                    return True, f"IdClass fixer applied ({count} entity/ies). Rebuilt. Retry Run Application."
                return True, f"IdClass fixer applied. Rebuild failed: {proc.stderr[:500]}"
            # fix_idclass found nothing to fix - maybe Id class exists but has wrong fields
            return False, "IdClass error detected but fix_idclass found no entities to fix. Check entity @Id fields match IdClass."
        except ImportError:
            return False, "fix_idclass not found. Cannot auto-fix IdClass errors."
        except Exception as e:
            return False, f"fix_idclass failed: {e}"

    if detect_ambiguous_mapping(log_content):
        try:
            from fix_ambiguous_mapping import run_fix
            count, _ = run_fix(project_dir, log_content)
            if count > 0:
                proc = subprocess.run(
                    ["mvn", "-f", "pom.xml", "-T", "1C", "-q", "compile", "-DskipTests"],
                    cwd=str(project_dir),
                    capture_output=True,
                    text=True,
                    timeout=300,
                )
                if proc.returncode == 0:
                    return True, f"Ambiguous mapping fixer applied ({count} endpoint/s). Rebuilt. Retry Run Application."
                return True, f"Ambiguous mapping fixer applied. Rebuild failed: {(proc.stderr or '')[:500]}"
            return False, "Ambiguous mapping detected but fix_ambiguous_mapping found no duplicates."
        except ImportError:
            return False, "fix_ambiguous_mapping not found. Cannot auto-fix ambiguous mappings."
        except Exception as e:
            return False, f"fix_ambiguous_mapping failed: {e}"

    return False, "No known runtime fix applicable."


def main() -> None:
    if len(sys.argv) < 2:
        print("Usage: python3 fix_runtime_errors.py <project_dir> [<runtime_log_file>]", file=sys.stderr)
        sys.exit(1)
    project_dir = Path(sys.argv[1])
    log_file = Path(sys.argv[2]) if len(sys.argv) > 2 else None

    if not log_file or not log_file.is_file():
        sys.exit(0)

    log_content = log_file.read_text(encoding="utf-8", errors="ignore")
    fixed, message = apply_fixes(project_dir, log_content)
    print(message)
    sys.exit(0 if fixed else 1)


if __name__ == "__main__":
    main()
