#!/usr/bin/env python3
"""
Compare AST efficiency: old (JSON_20260227) vs new PKS revised (JSON_20260311).
Reports duplicate column counts and build success to verify column redundancy fix.
"""
import json
import subprocess
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parent
DB_REGISTRY = ROOT / "global_context" / "db_registry.json"


def count_duplicate_columns(registry_path: Path) -> list[tuple[str, str, int]]:
    """Count duplicate column names per table. Returns (table, column, count)."""
    if not registry_path.exists():
        return []
    data = json.loads(registry_path.read_text(encoding="utf-8"))
    duplicates = []
    for f in data.get("files", []):
        table = f.get("name") or f.get("fileName") or "?"
        cols = f.get("columns") or []
        names = [str(c.get("name", "")).strip() for c in cols if c.get("name")]
        for name, count in Counter(names).items():
            if name and count > 1:
                duplicates.append((table, name, count))
    return duplicates


def run_build_registry(ast_dir: str) -> bool:
    """Run build_db_registry.py for given AST. Returns success."""
    proc = subprocess.run(
        [sys.executable, str(ROOT / "global_context" / "build_db_registry.py"), "--astDir", ast_dir],
        capture_output=True,
        text=True,
        timeout=120,
        cwd=str(ROOT),
    )
    return proc.returncode == 0


def main():
    old_ast = "JSON_ast/JSON_20260227"
    new_ast = "JSON_ast/JSON_20260311"

    if not (ROOT / new_ast).exists():
        print("Run: python setup_ast_20260311.py  (extract 20260311.zip first)")
        sys.exit(1)

    print("=" * 60)
    print("AST Efficiency Comparison: Column Redundancy")
    print("=" * 60)

    # Build registry with OLD AST
    print(f"\n1. Building DB registry with OLD AST ({old_ast})...")
    if run_build_registry(old_ast):
        old_dups = count_duplicate_columns(DB_REGISTRY)
        old_total = sum(d[2] for d in old_dups)
        print(f"   Duplicate columns: {old_total} across {len(old_dups)} table/column pairs")
        for t, c, n in old_dups[:10]:
            print(f"     - {t}.{c}: {n}x")
        if len(old_dups) > 10:
            print(f"     ... and {len(old_dups) - 10} more")
    else:
        print("   FAILED to build registry")
        old_dups = []
        old_total = -1

    # Build registry with NEW AST
    print(f"\n2. Building DB registry with NEW AST ({new_ast})...")
    if run_build_registry(new_ast):
        new_dups = count_duplicate_columns(DB_REGISTRY)
        new_total = sum(d[2] for d in new_dups)
        print(f"   Duplicate columns: {new_total} across {len(new_dups)} table/column pairs")
        for t, c, n in new_dups[:10]:
            print(f"     - {t}.{c}: {n}x")
        if len(new_dups) > 10:
            print(f"     ... and {len(new_dups) - 10} more")
    else:
        print("   FAILED to build registry")
        new_dups = []
        new_total = -1

    # Summary
    print("\n" + "=" * 60)
    print("SUMMARY")
    print("=" * 60)
    if old_total >= 0 and new_total >= 0:
        diff = old_total - new_total
        if diff > 0:
            print(f"  PKS revised AST reduced duplicate columns by {diff} ({old_total} -> {new_total})")
        elif diff < 0:
            print(f"  New AST has {abs(diff)} more duplicates ({old_total} -> {new_total})")
        else:
            print(f"  Same duplicate count: {old_total}")
    print("\nNext: Run full pipeline with new AST (JSON_20260311) via UI or:")
    print("  ./start_global_context_ui.sh")
    print("  Select AST: JSON_ast/JSON_20260311, then Build Global Context, Migrate, Build App")


if __name__ == "__main__":
    main()
