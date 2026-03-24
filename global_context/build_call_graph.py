#!/usr/bin/env python3
"""
Build a full call graph from RPG AST files.

Inputs:
  - JSON_ast/**/-ast.json           (e.g. JSON_ast/JSON_20260211/HS1210-ast.json)
  - global_context/programs/*.program.json (from build_program_context.py)

Output:
  - global_context/call_graph.json

For each program it records:
  - programId, unitId
  - calls: list of procedure/subroutine call edges (ExSr, CallP)
  - tableAccess: map of physical file/table name -> list of {nodeId, opcode, line, callerNodeId}
    for file operations: Chain, Read, ReadE, Reade, Write, Update, Delete, SetLL, SetGT
"""

from __future__ import annotations

import json
import sys
from dataclasses import dataclass, asdict
from pathlib import Path
from typing import Any, Dict, List, Optional

ROOT_DIR = Path(__file__).resolve().parents[1]
DEFAULT_AST_ROOT = ROOT_DIR / "JSON_ast"
PROGRAMS_ROOT = ROOT_DIR / "global_context" / "programs"
OUTPUT_PATH = ROOT_DIR / "global_context" / "call_graph.json"


def _decode_json(path: Path) -> Optional[Dict[str, Any]]:
    try:
        raw = path.read_bytes()
    except Exception as e:
        print(f"[call-graph] Skipping {path} (read error): {e}", file=sys.stderr)
        return None

    for enc in ("utf-8", "latin-1"):
        try:
            text = raw.decode(enc)
            return json.loads(text)
        except Exception:
            continue
    print(f"[call-graph] Skipping {path}: could not decode as UTF-8 or latin-1", file=sys.stderr)
    return None


@dataclass
class CallerNode:
    nodeId: str
    kind: str | None
    fileId: str | None
    startLine: int | None
    endLine: int | None


@dataclass
class CallEdge:
    callNodeId: str
    opcode: str | None
    callerNodeId: str | None
    fileId: str | None
    line: int | None


def _load_program_index() -> Dict[str, Dict[str, CallerNode]]:
    """
    Load program context files and build an index:
      programId -> { nodeId -> CallerNode }
    Only nodes with kind in {Procedure, Subroutine} are included as potential callers.
    """
    index: Dict[str, Dict[str, CallerNode]] = {}
    if not PROGRAMS_ROOT.is_dir():
        return index

    for ctx_path in PROGRAMS_ROOT.glob("*.program.json"):
        ctx = _decode_json(ctx_path)
        if ctx is None:
            continue
        program_id = ctx.get("programId") or ctx_path.stem.replace(".program", "")
        callers: Dict[str, CallerNode] = {}
        for node in ctx.get("nodes") or []:
            if not isinstance(node, dict):
                continue
            kind = node.get("kind")
            if kind not in ("Procedure", "Subroutine"):
                continue
            node_id = node.get("id")
            rng = node.get("range") or {}
            callers[node_id] = CallerNode(
                nodeId=node_id,
                kind=kind,
                fileId=rng.get("fileId"),
                startLine=rng.get("startLine"),
                endLine=rng.get("endLine"),
            )
        index[program_id] = callers
    return index


def _program_id_from_unit(unit_id: str, ast_path: Path) -> str:
    # Prefer member name if unitId has it as last segment; otherwise AST stem.
    if unit_id:
        tail = unit_id.split("/")[-1]
        if tail:
            return tail
    stem = ast_path.stem
    return stem.replace("-ast", "")


def _find_caller_for_line(
    callers: Dict[str, CallerNode],
    file_id: str | None,
    line: int | None,
) -> Optional[str]:
    if not file_id or line is None:
        return None
    best: Optional[CallerNode] = None
    for c in callers.values():
        if c.fileId != file_id:
            continue
        if c.startLine is None or c.endLine is None:
            continue
        if c.startLine <= line <= c.endLine:
            if best is None or (best.startLine is not None and c.startLine is not None and c.startLine > best.startLine):
                best = c
    return best.nodeId if best else None


def build_call_graph(ast_root: Path | None = None) -> Dict[str, Any]:
    program_index = _load_program_index()
    if ast_root is None:
        ast_root = DEFAULT_AST_ROOT
    if not ast_root.is_dir():
        print(f"[call-graph] AST root not found: {ast_root}", file=sys.stderr)
        return {"programs": []}

    # Filter out display file ASTs (*D-ast.json)
    ast_paths = sorted(p for p in ast_root.glob("**/*-ast.json") if not p.stem.endswith("D-ast"))
    if not ast_paths:
        print(f"[call-graph] No *-ast.json files under {ast_root}", file=sys.stderr)
        return {"programs": []}

    FILE_OP_KINDS = {"Chain", "Read", "ReadE", "Reade", "Write", "Update", "Delete", "SetLL", "SetGT"}

    programs: List[Dict[str, Any]] = []

    print(f"[call-graph] Building call graph from {len(ast_paths)} AST files...", file=sys.stderr)

    for ast_path in ast_paths:
        root = _decode_json(ast_path)
        if root is None:
            continue
        unit = root.get("unit") or {}
        unit_id = unit.get("id") or ""
        program_id = _program_id_from_unit(unit_id, ast_path)
        callers = program_index.get(program_id) or {}

        calls: List[CallEdge] = []
        for node in root.get("nodes") or []:
            if not isinstance(node, dict):
                continue
            kind = node.get("kind")
            if kind not in ("ExSr", "CallP"):
                continue
            call_node_id = node.get("id")
            props = node.get("props") or {}
            opcode = props.get("opcode")
            rng = node.get("range") or {}
            file_id = rng.get("fileId")
            line = rng.get("startLine")
            caller_node_id = _find_caller_for_line(callers, file_id, line)
            calls.append(
                CallEdge(
                    callNodeId=call_node_id,
                    opcode=opcode,
                    callerNodeId=caller_node_id,
                    fileId=file_id,
                    line=line,
                )
            )

        # Extract file operations (Chain, Read, Write, etc.) for tableAccess
        table_access: Dict[str, List[Dict[str, Any]]] = {}
        for node in root.get("nodes") or []:
            if not isinstance(node, dict):
                continue
            kind = node.get("kind")
            if kind not in FILE_OP_KINDS:
                continue
            props = node.get("props") or {}
            target = props.get("target")
            if not target or not isinstance(target, str):
                continue
            table_name = target.strip().upper()
            if not table_name:
                continue
            rng = node.get("range") or {}
            file_id = rng.get("fileId")
            line = rng.get("startLine")
            caller_node_id = _find_caller_for_line(callers, file_id, line)
            entry = {
                "nodeId": node.get("id"),
                "opcode": (props.get("opcode") or kind).upper(),
                "line": line,
                "callerNodeId": caller_node_id,
            }
            table_access.setdefault(table_name, []).append(entry)

        programs.append(
            {
                "programId": program_id,
                "unitId": unit_id,
                "astPath": str(ast_path),
                "callCount": len(calls),
                "calls": [asdict(c) for c in calls],
                "tableAccess": table_access,
            }
        )

    return {"programs": programs}


def main() -> None:
    import argparse
    parser = argparse.ArgumentParser(description="Build call graph from RPG AST files.")
    parser.add_argument("--astDir", default=None, help="AST directory (default: JSON_ast/ under project root)")
    args = parser.parse_args()

    ast_root = Path(args.astDir) if args.astDir else DEFAULT_AST_ROOT
    if not ast_root.is_absolute():
        ast_root = ROOT_DIR / ast_root

    graph = build_call_graph(ast_root)
    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    with OUTPUT_PATH.open("w", encoding="utf-8") as f:
        json.dump(graph, f, indent=2)
    print(f"[call-graph] Wrote call graph to {OUTPUT_PATH}", file=sys.stderr)


if __name__ == "__main__":
    main()

