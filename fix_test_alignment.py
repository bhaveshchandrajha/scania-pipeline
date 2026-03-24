#!/usr/bin/env python3
"""
Fix test/entity alignment: update test code to use correct entity getter names.

When entities use domain-style getters (getClaimNumber, getCompanyCode) but tests
use RPG-style names (getClaimNr, getPakz), tests fail to compile. This script
applies a mapping of common wrong->right getter names.

USAGE:
  python3 fix_test_alignment.py <project_dir>

Called automatically before build (resilient pipeline).
"""

import re
import sys
from pathlib import Path
from typing import Dict, List, Tuple

# Test code uses canonical names; Claim entity uses RPG-style. Map test -> entity.
# Direction: replace test getter with entity getter (entity has getClaimNr, getPakz, etc.)
GETTER_MAPPING: Dict[str, str] = {
    "getClaimNumber": "getClaimNr",
    "getCompanyCode": "getPakz",
    "getInvoiceNumber": "getRechNr",
    "getInvoiceDate": "getRechDatum",
    "getOrderNumber": "getAuftragsNr",
    "getChassisNumber": "getChassisNr",
    "getCustomerNumber": "getKdNr",
}
REPOSITORY_MAPPING: Dict[str, str] = {
    "findByCompanyAndClaimNumber": "findByPakzAndClaimNr",
}


def fix_file(content: str, path: Path) -> Tuple[bool, str]:
    """
    Apply getter/repository replacements in file content.
    Tests use canonical names; entities use RPG-style. Map test -> entity.
    Returns (changed, new_content).
    """
    changed = False
    for wrong, right in GETTER_MAPPING.items():
        pattern = r"\b(" + re.escape(wrong) + r")\s*\("
        if re.search(pattern, content):
            content = re.sub(pattern, right + "(", content)
            changed = True
    for wrong, right in REPOSITORY_MAPPING.items():
        pattern = r"\b(" + re.escape(wrong) + r")\s*\("
        if re.search(pattern, content):
            content = re.sub(pattern, right + "(", content)
            changed = True
    return changed, content


def run_fix(project_dir: Path) -> Tuple[int, List[str]]:
    """
    Scan test files and apply getter alignment fixes.
    Returns (count_fixed, messages).
    """
    project_dir = Path(project_dir).resolve()
    test_root = project_dir / "src" / "test" / "java"
    if not test_root.is_dir():
        return 0, []

    messages = []
    count = 0
    for path in test_root.rglob("*.java"):
        try:
            content = path.read_text(encoding="utf-8", errors="ignore")
            changed, new_content = fix_file(content, path)
            if changed:
                path.write_text(new_content, encoding="utf-8")
                count += 1
                messages.append(f"Aligned: {path.relative_to(project_dir)}")
        except Exception as e:
            messages.append(f"Error {path.name}: {e}")

    return count, messages


def main() -> None:
    if len(sys.argv) < 2:
        print("Usage: python3 fix_test_alignment.py <project_dir>", file=sys.stderr)
        sys.exit(1)
    project_dir = Path(sys.argv[1])
    if not project_dir.is_dir():
        print(f"Project directory not found: {project_dir}", file=sys.stderr)
        sys.exit(1)
    count, messages = run_fix(project_dir)
    for m in messages:
        print(m)
    if count > 0:
        print(f"\nTest alignment: {count} file(s) fixed.", file=sys.stderr)
    sys.exit(0)


if __name__ == "__main__":
    main()
