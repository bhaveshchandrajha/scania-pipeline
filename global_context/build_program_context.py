#!/usr/bin/env python3
"""
Build per-program context summaries from RPG AST files.

Inputs:
  - JSON_ast/**/-ast.json  (e.g. JSON_ast/JSON_20260211/HS1210-ast.json)

Outputs (one per program):
  - global_context/programs/<program_id>.program.json

Each program context includes:
  - unit metadata (id, library, member, sourceFile)
  - nodes: id, kind, name, basic range
  - symbols: name, kind, typeId, scopeId, declNodeId
    - referencedBy: node ids that refer_to this symbol
    - shared: true if referenced by > 1 node

This is the basis for program-level "global context" (shared variables,
files, etc.) without touching the existing migration pipeline.
"""

from __future__ import annotations

import json
import sys
from pathlib import Path
from typing import Any, Dict, List, Set

ROOT_DIR = Path(__file__).resolve().parents[1]
DEFAULT_AST_ROOT = ROOT_DIR / "JSON_ast"
PROGRAMS_ROOT = ROOT_DIR / "global_context" / "programs"


def _decode_json(ast_path: Path) -> Dict[str, Any] | None:
    try:
        raw = ast_path.read_bytes()
    except Exception as e:
        print(f"[program-context] Skipping {ast_path} (read error): {e}", file=sys.stderr)
        return None

    for enc in ("utf-8", "latin-1"):
        try:
            text = raw.decode(enc)
            return json.loads(text)
        except Exception:
            continue
    print(f"[program-context] Skipping {ast_path}: could not decode as UTF-8 or latin-1", file=sys.stderr)
    return None


def _safe_text(node: Dict[str, Any], key: str) -> str | None:
    val = node.get(key)
    return val if isinstance(val, str) else None


def build_program_context(ast_path: Path) -> Dict[str, Any] | None:
    root = _decode_json(ast_path)
    if root is None:
        return None

    unit = root.get("unit") or {}
    unit_id = unit.get("id") or ""
    library = unit.get("library")
    member = unit.get("member")
    source_file = unit.get("sourceFile")

    # Derive a program id: prefer member, else last segment of unit_id.
    if isinstance(member, str) and member:
        program_id = member
    else:
        program_id = unit_id.split("/")[-1] if unit_id else ast_path.stem.replace("-ast", "")

    # Collect nodes of interest (all nodes, but keep basic fields only).
    nodes_raw = root.get("nodes") or []
    nodes: List[Dict[str, Any]] = []
    for node in nodes_raw:
        if not isinstance(node, dict):
            continue
        nid = _safe_text(node, "id")
        kind = _safe_text(node, "kind")
        props = node.get("props") or {}
        name = _safe_text(props, "name") or _safe_text(node, "name")
        proc_name = _safe_text(props, "procedure_name")
        range_node = node.get("range") or {}
        range_summary = {
            "fileId": _safe_text(range_node, "fileId"),
            "startLine": range_node.get("startLine"),
            "endLine": range_node.get("endLine"),
        }
        nodes.append(
            {
                "id": nid,
                "kind": kind,
                "name": name,
                "procedureName": proc_name,
                "range": range_summary,
            }
        )

    # Invert refers_to edges: symbolId -> set(nodeId)
    symbol_to_nodes: Dict[str, Set[str]] = {}
    for edge in root.get("edges") or []:
        if not isinstance(edge, dict):
            continue
        if edge.get("kind") != "refers_to":
            continue
        src = edge.get("src")
        dst = edge.get("dst")
        if not isinstance(src, str) or not isinstance(dst, str):
            continue
        symbol_to_nodes.setdefault(dst, set()).add(src)

    # Symbols from symbolTable
    symbol_table = root.get("symbolTable") or {}
    symbols: List[Dict[str, Any]] = []
    for symbol_id, sym in symbol_table.items():
        if not isinstance(sym, dict):
            continue
        name = _safe_text(sym, "name")
        kind = _safe_text(sym, "kind")
        type_id = _safe_text(sym, "typeId")
        scope_id = _safe_text(sym, "scopeId")
        decl_node_id = _safe_text(sym, "declNodeId")
        refs = sorted(symbol_to_nodes.get(symbol_id, []))
        symbols.append(
            {
                "symbolId": symbol_id,
                "name": name,
                "kind": kind,
                "typeId": type_id,
                "scopeId": scope_id,
                "declNodeId": decl_node_id,
                "referencedBy": refs,
                "shared": len(refs) > 1,
            }
        )

    return {
        "unitId": unit_id,
        "programId": program_id,
        "library": library,
        "sourceFile": source_file,
        "astPath": str(ast_path),
        "nodes": nodes,
        "symbols": symbols,
    }


def main() -> None:
    import argparse
    parser = argparse.ArgumentParser(description="Build per-program context summaries from RPG AST files.")
    parser.add_argument("--astDir", default=None, help="AST directory (default: JSON_ast/ under project root)")
    args = parser.parse_args()

    ast_root = Path(args.astDir) if args.astDir else DEFAULT_AST_ROOT
    if not ast_root.is_absolute():
        ast_root = ROOT_DIR / ast_root

    if not ast_root.is_dir():
        print(f"[program-context] AST root not found: {ast_root}", file=sys.stderr)
        sys.exit(1)

    # Filter out display file ASTs (*D-ast.json)
    ast_paths = sorted(p for p in ast_root.glob("**/*-ast.json") if not p.stem.endswith("D-ast"))
    if not ast_paths:
        print(f"[program-context] No *-ast.json files under {ast_root}", file=sys.stderr)
        sys.exit(0)

    PROGRAMS_ROOT.mkdir(parents=True, exist_ok=True)
    print(f"[program-context] Building program contexts for {len(ast_paths)} AST files from {ast_root}...", file=sys.stderr)

    count = 0
    for ast_path in ast_paths:
        ctx = build_program_context(ast_path)
        if ctx is None:
            continue
        program_id = ctx.get("programId") or ast_path.stem.replace("-ast", "")
        out_path = PROGRAMS_ROOT / f"{program_id}.program.json"
        with out_path.open("w", encoding="utf-8") as f:
            json.dump(ctx, f, indent=2)
        count += 1
        print(f"[program-context] Wrote {out_path}", file=sys.stderr)

    print(f"[program-context] Done. {count} program context file(s) written.", file=sys.stderr)


if __name__ == "__main__":
    main()

