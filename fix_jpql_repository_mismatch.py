#!/usr/bin/env python3
"""
Fix JPQL @Query attribute mismatches in Spring Data repositories.

Migration/generation can produce JPQL that references non-existent entity
attributes (e.g. epaKey000 instead of epa000), causing UnknownPathException
at Spring context startup. This script applies deterministic replacements
so the pipeline prevents production startup failures.

Known mappings (wrong -> correct):
  ExtendedPartAgreement: epaKey000->epa000, epaKey040->epa040, epaKey050->epa050,
                         epaKey060->epa060, epaVariant->epaType

USAGE:
  python3 fix_jpql_repository_mismatch.py <project_dir>

Called automatically before build (resilient pipeline).
"""

import re
import sys
from pathlib import Path
from typing import List, Tuple

# (wrong_attribute, correct_attribute) - order matters for overlapping patterns
JPQL_REPLACEMENTS: List[Tuple[str, str]] = [
    # ExtendedPartAgreement: schema uses epa000/epa040/... not epaKey000/epaKey040/...
    ("e.epaKey000", "e.epa000"),
    ("e.epaKey040", "e.epa040"),
    ("e.epaKey050", "e.epa050"),
    ("e.epaKey060", "e.epa060"),
    ("e.epaVariant", "e.epaType"),
]


def fix_file(content: str) -> Tuple[bool, str]:
    """
    Apply JPQL attribute replacements in @Query strings.
    Returns (changed, new_content).
    """
    changed = False
    for wrong, correct in JPQL_REPLACEMENTS:
        if wrong in content:
            content = content.replace(wrong, correct)
            changed = True
    return changed, content


def run_fix(project_dir: Path) -> Tuple[int, List[str]]:
    """
    Scan repository Java files for @Query and fix attribute mismatches.
    Returns (count_fixed, messages).
    """
    project_dir = Path(project_dir).resolve()
    src_root = project_dir / "src" / "main" / "java"
    if not src_root.is_dir():
        return 0, []

    messages: List[str] = []
    count = 0

    for path in src_root.rglob("*Repository.java"):
        try:
            content = path.read_text(encoding="utf-8", errors="ignore")
            if "@Query" not in content:
                continue
            changed, new_content = fix_file(content)
            if changed:
                path.write_text(new_content, encoding="utf-8")
                count += 1
                messages.append(f"Fixed JPQL attributes: {path.relative_to(project_dir)}")
        except Exception as e:
            messages.append(f"Error {path.name}: {e}")

    return count, messages


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python3 fix_jpql_repository_mismatch.py <project_dir>", file=sys.stderr)
        sys.exit(1)
    proj = Path(sys.argv[1])
    n, msgs = run_fix(proj)
    for m in msgs:
        print(m)
    sys.exit(0 if n >= 0 else 1)
