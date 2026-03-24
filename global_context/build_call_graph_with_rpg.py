#!/usr/bin/env python3
"""
Enrich the AST-based call graph with RPG callee names (best-effort).

Inputs:
  - global_context/call_graph.json   (from build_call_graph.py)
  - global_context/programs/*.program.json
  - RPG source root (e.g. /Users/fkhan/Downloads/PoC_HS1210)

Output:
  - global_context/call_graph_enriched.json

For each call edge it tries to add:
  - calleeName: name after EXSR / CALLP on the RPG line
  - calleeNodeId: for EXSR, Subroutine or Procedure; for CALLP, Procedure
    (best-effort, same program only)

Also discovers procedure calls from free-format (procName()) and fixed-format
(Eval x = procName()) that are not in the AST as CallP nodes, and adds them
as synthetic call edges so BFS migration includes linked procedures.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from dataclasses import dataclass, asdict
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple

ROOT_DIR = Path(__file__).resolve().parents[1]
CALL_GRAPH_PATH = ROOT_DIR / "global_context" / "call_graph.json"
PROGRAMS_ROOT = ROOT_DIR / "global_context" / "programs"
OUTPUT_PATH = ROOT_DIR / "global_context" / "call_graph_enriched.json"


def _decode_text(path: Path) -> Optional[str]:
    try:
        raw = path.read_bytes()
    except Exception as e:
        print(f"[call-graph-rpg] Skipping {path} (read error): {e}", file=sys.stderr)
        return None
    for enc in ("utf-8", "latin-1"):
        try:
            return raw.decode(enc)
        except Exception:
            continue
    print(f"[call-graph-rpg] Skipping {path}: could not decode as UTF-8 or latin-1", file=sys.stderr)
    return None


def _load_json(path: Path) -> Optional[Dict[str, Any]]:
    text = _decode_text(path)
    if text is None:
        return None
    try:
        return json.loads(text)
    except Exception as e:
        print(f"[call-graph-rpg] Invalid JSON in {path}: {e}", file=sys.stderr)
        return None


@dataclass
class SubroutineDef:
    name: str
    nodeId: str
    line: int


@dataclass
class CallerRange:
    nodeId: str
    kind: str
    fileId: Optional[str]
    startLine: Optional[int]
    endLine: Optional[int]


def _build_procedure_index(program_ctx: Dict[str, Any]) -> Dict[str, str]:
    """
    Build name (upper) -> nodeId for Procedure nodes.
    Uses name or procedureName from the node.
    """
    index: Dict[str, str] = {}
    for node in program_ctx.get("nodes") or []:
        if not isinstance(node, dict):
            continue
        if node.get("kind") != "Procedure":
            continue
        node_id = node.get("id")
        name = node.get("name") or node.get("procedureName")
        if name and isinstance(name, str):
            index[name.strip().upper()] = node_id
    return index


def _build_caller_index(program_ctx: Dict[str, Any]) -> List[CallerRange]:
    """
    Build list of Procedure/Subroutine ranges for finding caller by line.
    Includes CompilationUnit as fallback for main-flow lines (e.g. before first subroutine).
    """
    callers: List[CallerRange] = []
    for node in program_ctx.get("nodes") or []:
        if not isinstance(node, dict):
            continue
        kind = node.get("kind")
        if kind not in ("Procedure", "Subroutine", "CompilationUnit"):
            continue
        node_id = node.get("id")
        rng = node.get("range") or {}
        callers.append(
            CallerRange(
                nodeId=node_id,
                kind=kind or "",
                fileId=rng.get("fileId"),
                startLine=rng.get("startLine"),
                endLine=rng.get("endLine"),
            )
        )
    return callers


def _find_caller_for_line(
    callers: List[CallerRange],
    file_id: Optional[str],
    line: Optional[int],
) -> Optional[str]:
    """Find the innermost Procedure/Subroutine that contains the given line."""
    if not file_id or line is None:
        return None
    best: Optional[CallerRange] = None
    for c in callers:
        if c.fileId != file_id:
            continue
        if c.startLine is None or c.endLine is None:
            continue
        if c.startLine <= line <= c.endLine:
            if best is None or (
                best.startLine is not None
                and c.startLine is not None
                and c.startLine > best.startLine
            ):
                best = c
    return best.nodeId if best else None


def _discover_procedure_calls_from_rpg(
    rpg_lines: List[str],
    procedure_index: Dict[str, str],
    caller_index: List[CallerRange],
    file_id: str,
    program_id: str,
) -> List[Dict[str, Any]]:
    """
    Scan RPG source for procedure calls not in the AST (free-format procName(),
    fixed-format Eval x = procName()). Returns synthetic call edges.
    """
    synthetic: List[Dict[str, Any]] = []
    seen: set[Tuple[int, str]] = set()
    # Free-format: procName() or procName ( )
    free_re = re.compile(
        r"\b([A-Za-z][A-Za-z0-9_]*)\s*\(\s*\)",
        re.IGNORECASE,
    )
    # Fixed-format: Eval x = procName()
    eval_re = re.compile(
        r"\bEval\s+[^=]+=\s*([A-Za-z][A-Za-z0-9_]*)\s*\(\s*\)",
        re.IGNORECASE,
    )
    for i, line in enumerate(rpg_lines):
        line_no = i + 1
        for m in free_re.finditer(line):
            name = m.group(1).strip().upper()
            if name in procedure_index and (line_no, name) not in seen:
                seen.add((line_no, name))
                caller = _find_caller_for_line(caller_index, file_id, line_no)
                if caller:
                    synthetic.append(
                        {
                            "callNodeId": f"syn_{program_id}_{line_no}_{name}",
                            "opcode": "CALLP",
                            "callerNodeId": caller,
                            "fileId": file_id,
                            "line": line_no,
                            "calleeName": name,
                            "calleeNodeId": procedure_index[name],
                            "synthetic": True,
                        }
                    )
        for m in eval_re.finditer(line):
            name = m.group(1).strip().upper()
            if name in procedure_index and (line_no, name) not in seen:
                seen.add((line_no, name))
                caller = _find_caller_for_line(caller_index, file_id, line_no)
                if caller:
                    synthetic.append(
                        {
                            "callNodeId": f"syn_{program_id}_{line_no}_{name}",
                            "opcode": "EVAL",
                            "callerNodeId": caller,
                            "fileId": file_id,
                            "line": line_no,
                            "calleeName": name,
                            "calleeNodeId": procedure_index[name],
                            "synthetic": True,
                        }
                    )
    return synthetic


def _program_id_from_unit(unit_id: str, ast_path: Path) -> str:
    if unit_id:
        tail = unit_id.split("/")[-1]
        if tail:
            return tail
    stem = ast_path.stem
    return stem.replace("-ast", "")


def _rpg_file_for_program(rpg_root: Path, program_id: str) -> Optional[Path]:
    """
    Best-effort mapping from programId (e.g. HS1210) to a local RPG source file.
    For this PoC, we know HS1210 lives under HSSRC/QRPGLESRC/HS1210.SQLRPGLE.
    """
    # Try the HS1210.SQLRPGLE path first
    candidate = rpg_root / "HSSRC" / "QRPGLESRC" / f"{program_id}.SQLRPGLE"
    if candidate.is_file():
        return candidate
    # Fallback: search under root for *<program_id>*.RPG* or *<program_id>*.SQLRPG*
    patterns = [
        f"**/{program_id}.RPG*",
        f"**/{program_id}.SQLRPG*",
    ]
    for pat in patterns:
        matches = list(rpg_root.glob(pat))
        if matches:
            return matches[0]
    return None


def _parse_callee_from_line(opcode: str, line: str) -> Optional[str]:
    """
    Extract the callee name from a fixed-format RPG line with EXSR or CALLP.
    This is intentionally simple and may need refinement for edge cases.
    """
    opcode = opcode.upper() if opcode else ""
    if opcode == "EXSR":
        m = re.search(r"\bEXSR\b\s+([A-Z0-9_#@$]+)", line, re.IGNORECASE)
        if m:
            return m.group(1).strip().upper()
    elif opcode in ("CALLP", "CALL"):
        # For CALLP/CALL the target can be a name or literal; we capture the identifier.
        m = re.search(r"\bCALLP?\b\s+('([^']+)'|([A-Z0-9_#@$]+))", line, re.IGNORECASE)
        if m:
            name = m.group(2) or m.group(3)
            if name:
                return name.strip().upper()
    return None


def _build_subroutine_index(program_ctx: Dict[str, Any], rpg_lines: List[str]) -> Dict[str, SubroutineDef]:
    """
    For each Subroutine node, look at its startLine and try to read the BEGSR label from the RPG source.
    Returns a map: subroutineName (upper) -> SubroutineDef(nodeId, line).
    """
    index: Dict[str, SubroutineDef] = {}
    for node in program_ctx.get("nodes") or []:
        if not isinstance(node, dict):
            continue
        if node.get("kind") != "Subroutine":
            continue
        node_id = node.get("id")
        rng = node.get("range") or {}
        line_no = rng.get("startLine")
        if not isinstance(line_no, int) or line_no <= 0 or line_no > len(rpg_lines):
            continue
        text = rpg_lines[line_no - 1]
        # Look for PATTERN: <LABEL> BEGSR
        m = re.search(r"\b([A-Z0-9_#@$]+)\s+BEGSR\b", text, re.IGNORECASE)
        if not m:
            continue
        name = m.group(1).strip().upper()
        index[name] = SubroutineDef(name=name, nodeId=node_id, line=line_no)
    return index


def enrich_call_graph(rpg_root: Path) -> Dict[str, Any]:
    graph = _load_json(CALL_GRAPH_PATH)
    if graph is None:
        raise SystemExit(f"Base call graph not found or invalid: {CALL_GRAPH_PATH}")

    # Load program contexts
    program_ctxs: Dict[str, Dict[str, Any]] = {}
    for ctx_path in PROGRAMS_ROOT.glob("*.program.json"):
        ctx = _load_json(ctx_path)
        if ctx is None:
            continue
        program_id = ctx.get("programId") or ctx_path.stem.replace(".program", "")
        program_ctxs[program_id] = ctx

    enriched_programs: List[Dict[str, Any]] = []

    for prog in graph.get("programs") or []:
        if not isinstance(prog, dict):
            continue
        program_id = prog.get("programId")
        unit_id = prog.get("unitId") or ""
        base_calls = prog.get("calls") or []

        ctx = program_ctxs.get(program_id)
        if ctx is None:
            enriched_programs.append(prog)
            continue

        rpg_file = _rpg_file_for_program(rpg_root, program_id)
        if rpg_file is None:
            print(f"[call-graph-rpg] No RPG source found for program {program_id}", file=sys.stderr)
            enriched_programs.append(prog)
            continue

        text = _decode_text(rpg_file)
        if text is None:
            enriched_programs.append(prog)
            continue
        # Split into physical lines; RPG line numbers in AST are 1-based.
        rpg_lines = text.splitlines()

        subr_index = _build_subroutine_index(ctx, rpg_lines)
        proc_index = _build_procedure_index(ctx)
        caller_index = _build_caller_index(ctx)

        new_calls: List[Dict[str, Any]] = []
        for call in base_calls:
            if not isinstance(call, dict):
                continue
            opcode = (call.get("opcode") or "").upper()
            file_id = call.get("fileId")
            line_no = call.get("line")

            callee_name: Optional[str] = None
            callee_node_id: Optional[str] = None

            # Only attempt to read from this program's fileId
            if isinstance(line_no, int) and 1 <= line_no <= len(rpg_lines):
                line_text = rpg_lines[line_no - 1]
                callee_name = _parse_callee_from_line(opcode, line_text)
                if callee_name:
                    if opcode == "EXSR":
                        sub_def = subr_index.get(callee_name)
                        if sub_def:
                            callee_node_id = sub_def.nodeId
                        elif callee_name in proc_index:
                            callee_node_id = proc_index[callee_name]
                    elif opcode in ("CALLP", "CALL"):
                        if callee_name in proc_index:
                            callee_node_id = proc_index[callee_name]

            call_enriched = dict(call)
            call_enriched["calleeName"] = callee_name
            call_enriched["calleeNodeId"] = callee_node_id
            new_calls.append(call_enriched)

        # Discover procedure calls from free-format (procName()) and Eval procName()
        prog_file_id = (
            (base_calls[0].get("fileId") if base_calls else None)
            or unit_id
            or f"qsys:HSSRC/QRPGLESRC/{program_id}"
        )
        synthetic = _discover_procedure_calls_from_rpg(
            rpg_lines,
            proc_index,
            caller_index,
            prog_file_id,
            program_id,
        )
        new_calls.extend(synthetic)

        enriched_programs.append(
            {
                **prog,
                "calls": new_calls,
            }
        )

    return {"programs": enriched_programs}


def main() -> None:
    parser = argparse.ArgumentParser(description="Enrich call_graph.json with RPG callee names.")
    parser.add_argument(
        "--rpgDir",
        required=True,
        help="Root directory of RPG sources (e.g. /Users/fkhan/Downloads/PoC_HS1210)",
    )
    args = parser.parse_args()

    rpg_root = Path(args.rpgDir)
    if not rpg_root.is_dir():
        raise SystemExit(f"RPG root not found: {rpg_root}")

    enriched = enrich_call_graph(rpg_root)
    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    with OUTPUT_PATH.open("w", encoding="utf-8") as f:
        json.dump(enriched, f, indent=2)
    print(f"[call-graph-rpg] Wrote enriched call graph to {OUTPUT_PATH}", file=sys.stderr)


if __name__ == "__main__":
    main()

