#!/usr/bin/env python3
"""
Build a merged DB contract registry from all available RPG AST files.

Inputs:
  - JSON_ast/**/-ast.json  (e.g. JSON_ast/JSON_20260211/HS1210-ast.json)

Output:
  - global_context/db_registry.json

The registry is keyed by (library, fileName) and records:
  - symbolId
  - name
  - library
  - typeId
  - columns (name, sqlType-ish, nullable, key)
  - sourceUnits: list of unit ids (programs) that reference this file

This is intentionally conservative and read-only: it does not change
any existing pipeline behaviour.
"""

from __future__ import annotations

import json
import sys
from dataclasses import dataclass, asdict
from pathlib import Path
from typing import Dict, List, Tuple, Any

ROOT_DIR = Path(__file__).resolve().parents[1]
DEFAULT_AST_ROOT = ROOT_DIR / "JSON_ast"
OUTPUT_PATH = ROOT_DIR / "global_context" / "db_registry.json"


@dataclass
class ColumnDef:
    name: str
    typeId: str | None
    sqlType: str | None
    length: int | None
    scale: int | None
    nullable: bool
    key: bool


@dataclass
class FileDef:
    symbolId: str | None
    name: str
    library: str | None
    typeId: str | None
    columns: List[ColumnDef]
    sourceUnits: List[str]


def _safe_get(node: Dict[str, Any], key: str, default=None):
    val = node.get(key, default)
    return val if val is not None else default


def _infer_sql_type(type_id: str | None) -> Tuple[str | None, int | None, int | None]:
    """
    Best-effort decode for simple RPG typeIds like t.char.65, t.dec11_2.
    This mirrors the logic in PksAstParser but in a lightweight way.
    """
    if not type_id:
        return None, None, None
    if type_id.startswith("t.char."):
        try:
            length = int(type_id.split(".")[-1])
        except ValueError:
            length = None
        sql = f"CHAR({length})" if length is not None else "CHAR"
        return sql, length, None
    if type_id.startswith("t.dec"):
        rest = type_id[len("t.dec") :]
        precision = None
        scale = 0
        if "_" in rest:
            p_str, s_str = rest.split("_", 1)
            try:
                precision = int(p_str)
            except ValueError:
                precision = None
            try:
                scale = int(s_str)
            except ValueError:
                scale = 0
        else:
            try:
                precision = int(rest)
            except ValueError:
                precision = None
        if precision is not None:
            return f"DECIMAL({precision},{scale})", precision, scale
        return "DECIMAL", None, None
    # Fallback: just echo
    return type_id, None, None


def collect_db_files(ast_root: Path | None = None) -> Dict[Tuple[str | None, str], FileDef]:
    files: Dict[Tuple[str | None, str], FileDef] = {}
    if ast_root is None:
        ast_root = DEFAULT_AST_ROOT

    if not ast_root.is_dir():
        print(f"[db-registry] AST root not found: {ast_root}", file=sys.stderr)
        return files

    # Filter out display file ASTs (*D-ast.json)
    ast_paths = sorted(p for p in ast_root.glob("**/*-ast.json") if not p.stem.endswith("D-ast"))
    if not ast_paths:
        print(f"[db-registry] No *-ast.json files under {AST_ROOT}", file=sys.stderr)
        return files

    print(f"[db-registry] Scanning {len(ast_paths)} AST files...", file=sys.stderr)

    for ast_path in ast_paths:
        try:
            raw = ast_path.read_bytes()
        except Exception as e:
            print(f"[db-registry] Skipping {ast_path} (read error): {e}", file=sys.stderr)
            continue

        # ASTs from IBM i can contain EBCDIC or non-UTF8 bytes; try UTF-8
        # first, then fall back to latin-1 as a best-effort.
        text: str
        for enc in ("utf-8", "latin-1"):
            try:
                text = raw.decode(enc)
                root = json.loads(text)
                break
            except Exception:
                root = None  # type: ignore[assignment]
        if root is None:
            print(f"[db-registry] Skipping {ast_path}: could not decode as UTF-8 or latin-1", file=sys.stderr)
            continue

        unit = root.get("unit") or {}
        unit_id = unit.get("id") or ""

        db_root = root.get("dbContracts", {}).get("nativeFiles") or []
        for file_node in db_root:
            symbol_id = _safe_get(file_node, "symbolId")
            name = _safe_get(file_node, "name")
            library = _safe_get(file_node, "library")
            type_id = _safe_get(file_node, "typeId")

            if not name:
                continue

            key = (library, name)
            cols: List[ColumnDef] = []
            key_names = {k.get("name") for k in (file_node.get("keys") or []) if k.get("name")}

            for col in file_node.get("columns") or []:
                col_name = _safe_get(col, "name")
                col_type_id = _safe_get(col, "typeId")
                nullable = bool(col.get("nullable", False))
                sql_type, length, scale = _infer_sql_type(col_type_id)
                cols.append(
                    ColumnDef(
                        name=col_name,
                        typeId=col_type_id,
                        sqlType=sql_type,
                        length=length,
                        scale=scale,
                        nullable=nullable,
                        key=(col_name in key_names),
                    )
                )

            if key in files:
                # Merge: extend sourceUnits, keep first structural definition
                existing = files[key]
                if unit_id and unit_id not in existing.sourceUnits:
                    existing.sourceUnits.append(unit_id)
            else:
                files[key] = FileDef(
                    symbolId=symbol_id,
                    name=name,
                    library=library,
                    typeId=type_id,
                    columns=cols,
                    sourceUnits=[unit_id] if unit_id else [],
                )

    return files


def main() -> None:
    import argparse
    parser = argparse.ArgumentParser(description="Build merged DB contract registry from RPG AST files.")
    parser.add_argument("--astDir", default=None, help="AST directory (default: JSON_ast/ under project root)")
    args = parser.parse_args()

    ast_root = Path(args.astDir) if args.astDir else DEFAULT_AST_ROOT
    if not ast_root.is_absolute():
        ast_root = ROOT_DIR / ast_root

    registry = collect_db_files(ast_root)
    data = {
        "root": str(ROOT_DIR),
        "astRoot": str(ast_root),
        "fileCount": len(registry),
        "files": [
            {
                **{
                    "symbolId": f.symbolId,
                    "name": f.name,
                    "library": f.library,
                    "typeId": f.typeId,
                    "sourceUnits": sorted(set(f.sourceUnits)),
                },
                "columns": [asdict(c) for c in f.columns],
            }
            for (_, _), f in sorted(registry.items(), key=lambda kv: (kv[0][0] or "", kv[0][1]))
        ],
    }

    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    with OUTPUT_PATH.open("w", encoding="utf-8") as out:
        json.dump(data, out, indent=2)

    print(f"[db-registry] Wrote {len(registry)} files to {OUTPUT_PATH}", file=sys.stderr)


if __name__ == "__main__":
    main()

