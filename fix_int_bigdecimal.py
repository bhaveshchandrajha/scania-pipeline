#!/usr/bin/env python3
"""
Fix int literal passed where BigDecimal is expected.

When test/factory code uses setXxx(0) but the entity setter expects BigDecimal,
the build fails. This script applies deterministic fixes without LLM.

USAGE:
  python3 fix_int_bigdecimal.py <project_dir>

Called automatically before build (resilient pipeline).
"""

import re
import sys
from pathlib import Path
from typing import Dict, List, Set, Tuple

# Setters that accept BigDecimal - built from common Invoice/Claim schema fields.
# Format: setter_name -> (replacement_for_0, replacement_for_1)
BIGDECIMAL_SETTERS: Dict[str, Tuple[str, str]] = {
    # Invoice AHK fields (precision columns)
    "setAhk820": ("BigDecimal.ZERO", "BigDecimal.ONE"),
    "setAhk830": ("BigDecimal.ZERO", "BigDecimal.ONE"),
    "setAhk840": ("BigDecimal.ZERO", "BigDecimal.ONE"),
    "setAhk845": ("BigDecimal.ZERO", "BigDecimal.ONE"),
    "setAhk850": ("BigDecimal.ZERO", "BigDecimal.ONE"),
    "setAhk920": ("BigDecimal.ZERO", "BigDecimal.ONE"),
    # Add more as discovered
}


def _discover_bigdecimal_setters(project_dir: Path) -> Set[str]:
    """Scan entity classes for setXxx(BigDecimal) and return setter names."""
    src_root = project_dir / "src" / "main" / "java"
    if not src_root.is_dir():
        return set()

    setters: Set[str] = set()
    for path in src_root.rglob("*.java"):
        try:
            content = path.read_text(encoding="utf-8", errors="ignore")
            # Match: public void setXxx(BigDecimal
            for m in re.finditer(r"public\s+void\s+(set[A-Za-z0-9]+)\s*\(\s*BigDecimal", content):
                setters.add(m.group(1))
        except Exception:
            continue
    return setters


def _replacer(setter: str, m: re.Match) -> str:
    literal = m.group(1)
    if literal == "0":
        return f"{setter}(BigDecimal.ZERO)"
    if literal == "1":
        return f"{setter}(BigDecimal.ONE)"
    return f"{setter}(BigDecimal.valueOf({literal}))"


def fix_file(content: str, path: Path, bigdecimal_setters: Set[str]) -> Tuple[bool, str]:
    """
    Replace setXxx(0) -> setXxx(BigDecimal.ZERO) when setter expects BigDecimal.
    Returns (changed, new_content).
    """
    changed = False
    for setter in bigdecimal_setters:
        pattern = rf"\b{re.escape(setter)}\s*\(\s*(\d+)\s*\)"
        new_content, n = re.subn(pattern, lambda m: _replacer(setter, m), content)
        if n > 0:
            content = new_content
            changed = True

    if changed and "import java.math.BigDecimal" not in content:
        if "package " in content:
            insert = content.find(";\n")
            if insert >= 0:
                content = content[: insert + 1] + "\nimport java.math.BigDecimal;" + content[insert + 1 :]
        else:
            content = "import java.math.BigDecimal;\n" + content

    return changed, content


def run_fix(project_dir: Path) -> Tuple[int, List[str]]:
    """
    Scan test and config files, fix int->BigDecimal mismatches.
    Returns (count_fixed, messages).
    """
    project_dir = Path(project_dir).resolve()

    # Discover setters from entities + use known set
    known = set(BIGDECIMAL_SETTERS)
    discovered = _discover_bigdecimal_setters(project_dir)
    bigdecimal_setters = known | discovered

    if not bigdecimal_setters:
        return 0, []

    messages = []
    count = 0

    for subdir in ["src/test/java", "src/main/java"]:
        root = project_dir / subdir
        if not root.is_dir():
            continue
        for path in root.rglob("*.java"):
            try:
                content = path.read_text(encoding="utf-8", errors="ignore")
                changed, new_content = fix_file(content, path, bigdecimal_setters)
                if changed:
                    path.write_text(new_content, encoding="utf-8")
                    count += 1
                    messages.append(f"Fixed int->BigDecimal: {path.relative_to(project_dir)}")
            except Exception as e:
                messages.append(f"Error {path.name}: {e}")

    return count, messages


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python3 fix_int_bigdecimal.py <project_dir>", file=sys.stderr)
        sys.exit(1)
    proj = Path(sys.argv[1])
    n, msgs = run_fix(proj)
    for m in msgs:
        print(m)
    sys.exit(0 if n >= 0 else 1)
