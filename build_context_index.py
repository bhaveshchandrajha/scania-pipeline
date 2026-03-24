#!/usr/bin/env python3
"""
Build context_index from rpg-ast/1.0 AST files (Python replacement for Java IndexAll).

Integrates with the knowledge graph pipeline:
  - Reads global_context/call_graph_enriched.json for call/calledBy relationships
  - Reads global_context/db_registry.json for cross-program DB contract enrichment
  - Reads global_context/programs/*.program.json for shared variable detection
  - Reads dds-ast/1.0 display file ASTs for UI contract enrichment

Inputs:
  --astDir    Directory containing RPG *-ast.json files (e.g. JSON_ast/JSON_20260227)
  --rpgDir    RPG source root for snippet extraction (e.g. /Users/fkhan/Downloads/PoC_HS1210)
  --outputDir Root project directory (default: current directory)

Outputs:
  - context_index/<unitId>_<nodeId>.json   (one per migratable node)
  - context_index/manifest.json            (index of all context packages)

Each context package contains (backward-compatible + enriched):
  - astNode:        node metadata (id, kind, name, sem, outgoingEdges, range)
  - narrative:      Markdown business narrative from AST semantics
  - rpgSnippet:     RPG source lines for the node's range
  - dbContracts:    DB file schemas with resolved types (compatible with migrate_to_pure_java.py)
  - symbolMetadata: Referenced symbols with type and scope info
  - displayFiles:   Display file UI contracts from dds-ast/1.0
  - callGraph:      Calls/calledBy from knowledge graph (feature-level traceability)

USAGE:
  python3 build_context_index.py --astDir JSON_ast/JSON_20260227 --rpgDir /path/to/PoC_HS1210
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any, Dict, List, Optional, Set, Tuple

ROOT_DIR = Path(__file__).resolve().parent


# ---------------------------------------------------------------------------
# Type resolution (mirrors PksAstParser.resolveTypeInfo and build_db_registry._infer_sql_type)
# ---------------------------------------------------------------------------

def resolve_type_info(type_id: str | None, types: dict | None = None) -> Tuple[str | None, int | None, int | None]:
    """Resolve a typeId like t.char.65 or t.dec11_2 to (sqlType, length, scale)."""
    if not type_id:
        return None, None, None

    if types and type_id in types:
        t = types[type_id]
        category = t.get("category")
        if category == "numeric":
            precision = t.get("precision")
            scale = t.get("scale", 0)
            sql = f"DECIMAL({precision},{scale})" if precision is not None else "DECIMAL"
            return sql, precision, scale
        if category == "char":
            dot = type_id.rfind(".")
            length = _int_safe(type_id[dot + 1:]) if dot >= 0 else None
            sql = f"CHAR({length})" if length is not None else "CHAR"
            return sql, length, None

    if type_id.startswith("t.char."):
        length = _int_safe(type_id[len("t.char."):])
        sql = f"CHAR({length})" if length is not None else "CHAR"
        return sql, length, None

    if type_id.startswith("t.dec"):
        rest = type_id[len("t.dec"):]
        parts = rest.split("_")
        precision = _int_safe(parts[0])
        scale = _int_safe(parts[1]) if len(parts) > 1 else 0
        if precision is not None:
            return f"DECIMAL({precision},{scale})", precision, scale
        return "DECIMAL", None, None

    if type_id.startswith("t.date"):
        return "DATE", None, None
    if type_id.startswith("t.time"):
        return "TIME", None, None
    if type_id.startswith("t.timestamp"):
        return "TIMESTAMP", None, None

    return type_id, None, None


def _int_safe(s: str) -> int | None:
    try:
        return int(s)
    except (ValueError, TypeError):
        return None


# ---------------------------------------------------------------------------
# AST loading
# ---------------------------------------------------------------------------

def load_ast(path: Path) -> dict | None:
    try:
        raw = path.read_bytes()
    except Exception as e:
        print(f"[context-index] Cannot read {path}: {e}", file=sys.stderr)
        return None

    for enc in ("utf-8", "latin-1"):
        try:
            return json.loads(raw.decode(enc))
        except Exception:
            continue
    print(f"[context-index] Cannot decode {path}", file=sys.stderr)
    return None


# ---------------------------------------------------------------------------
# RPG source provider
# ---------------------------------------------------------------------------

class RpgSourceProvider:
    """Loads RPG source files and extracts line ranges."""

    def __init__(self, rpg_root: Path | None):
        self._root = rpg_root
        self._cache: Dict[str, List[str]] = {}

    def get_snippet(self, file_id: str | None, start_line: int, end_line: int) -> str | None:
        if not self._root or not file_id:
            return None
        lines = self._load_lines(file_id)
        if not lines:
            return None
        s = max(0, start_line - 1)
        e = min(len(lines), end_line)
        return "\n".join(lines[s:e])

    def _load_lines(self, file_id: str) -> List[str] | None:
        if file_id in self._cache:
            return self._cache[file_id]

        # file_id looks like "qsys:HSSRC/QRPGLESRC/HS1210"
        member = file_id.split("/")[-1] if "/" in file_id else file_id.replace("qsys:", "")
        for ext in (".SQLRPGLE", ".RPGLE", ".sqlrpgle", ".rpgle", ""):
            candidate = self._root / "HSSRC" / "QRPGLESRC" / (member + ext)
            if candidate.is_file():
                try:
                    text = candidate.read_text(encoding="utf-8", errors="replace")
                    self._cache[file_id] = text.splitlines()
                    return self._cache[file_id]
                except Exception:
                    pass
        # Broader search
        for pat in (f"**/{member}.SQLRPGLE", f"**/{member}.RPGLE"):
            matches = list(self._root.glob(pat))
            if matches:
                try:
                    text = matches[0].read_text(encoding="utf-8", errors="replace")
                    self._cache[file_id] = text.splitlines()
                    return self._cache[file_id]
                except Exception:
                    pass
        self._cache[file_id] = None
        return None


# ---------------------------------------------------------------------------
# Knowledge graph integration
# ---------------------------------------------------------------------------

def load_call_graph(root: Path) -> Dict[str, Dict]:
    """Load enriched call graph and build a per-program index.

    Returns: {programId: {
        "calls": [...],
        "callerToCallees": {nodeId: [calleeNodeId, ...]},
        "calleeToCallers": {nodeId: [callerNodeId, ...]},
        "tableAccess": {tableName: [{nodeId, opcode, line, callerNodeId}, ...]},
    }}
    """
    result = {}
    for name in ("call_graph_enriched.json", "call_graph.json"):
        path = root / "global_context" / name
        if path.exists():
            try:
                data = json.loads(path.read_text(encoding="utf-8", errors="ignore"))
                break
            except Exception:
                data = None
    else:
        return result

    if not data:
        return result

    for prog in data.get("programs", []):
        pid = prog.get("programId")
        if not pid:
            continue
        calls = prog.get("calls", [])
        caller_to_callees: Dict[str, List[str]] = {}
        callee_to_callers: Dict[str, List[str]] = {}
        for c in calls:
            caller_id = c.get("callerNodeId")
            callee_id = c.get("calleeNodeId")
            if caller_id and callee_id:
                caller_to_callees.setdefault(caller_id, []).append(callee_id)
                callee_to_callers.setdefault(callee_id, []).append(caller_id)
        table_access = prog.get("tableAccess") or {}
        result[pid] = {
            "calls": calls,
            "callerToCallees": caller_to_callees,
            "calleeToCallers": callee_to_callers,
            "tableAccess": table_access,
        }
    return result


def load_db_registry(root: Path) -> Dict[str, dict]:
    """Load db_registry.json keyed by file name for cross-program enrichment."""
    path = root / "global_context" / "db_registry.json"
    if not path.exists():
        return {}
    try:
        data = json.loads(path.read_text(encoding="utf-8", errors="ignore"))
    except Exception:
        return {}
    result = {}
    for f in data.get("files", []):
        name = f.get("name")
        if name:
            result[name] = f
    return result


def load_program_contexts(root: Path) -> Dict[str, dict]:
    """Load all program contexts for shared variable detection."""
    result = {}
    programs_dir = root / "global_context" / "programs"
    if not programs_dir.is_dir():
        return result
    for p in programs_dir.glob("*.program.json"):
        try:
            ctx = json.loads(p.read_text(encoding="utf-8", errors="ignore"))
            pid = ctx.get("programId", p.stem.replace(".program", ""))
            result[pid] = ctx
        except Exception:
            pass
    return result


# ---------------------------------------------------------------------------
# Display file (DDS AST) integration
# ---------------------------------------------------------------------------

def load_display_ast_contracts(ast_dir: Path, unit_id: str) -> List[dict]:
    """Load uiContracts from a matching *D-ast.json display file AST."""
    member = unit_id.split("/")[-1] if "/" in unit_id else unit_id
    dds_path = ast_dir / f"{member}D-ast.json"
    if not dds_path.is_file():
        return []
    ast = load_ast(dds_path)
    if not ast or ast.get("version") != "dds-ast/1.0":
        return []
    ui_contracts = ast.get("uiContracts", {})
    display_files = ui_contracts.get("displayFiles", [])
    return display_files


def get_dspf_refs_for_node(node: dict, edges: list) -> Set[str]:
    """Find sym.dspf.* references from node sem and AST edges."""
    refs = set()
    sem = node.get("sem")
    if isinstance(sem, dict):
        for key in sem:
            if isinstance(key, str) and key.startswith("sym.dspf."):
                refs.add(key)
    node_id = node.get("id")
    for edge in edges:
        if not isinstance(edge, dict):
            continue
        if edge.get("src") == node_id:
            dst = edge.get("dst", "")
            if isinstance(dst, str) and dst.startswith("sym.dspf."):
                refs.add(dst)
    return refs


# ---------------------------------------------------------------------------
# Narrative builder (Python equivalent of SemanticNarrativeBuilder.java)
# ---------------------------------------------------------------------------

def build_narrative(
    ast: dict,
    node: dict,
    db_contracts_by_sym: Dict[str, dict],
    symbol_table: dict,
    types: dict,
) -> str:
    """Build a Markdown narrative from the AST node's semantic block."""
    lines = []
    node_id = node.get("id", "?")
    kind = node.get("kind", "?")
    props = node.get("props") or {}
    name = props.get("name")

    lines.append(f"### Node {node_id}: {kind}" + (f" `{name}`" if name else ""))
    lines.append("")
    lines.append(f"This {kind.lower()} references symbols derived from its semantic block and symbol table.")
    lines.append("")

    sem = node.get("sem") or {}
    file_syms, var_syms, other_syms = [], [], []
    for sym_id in sem:
        if not isinstance(sym_id, str):
            continue
        if sym_id.startswith("sym.file."):
            file_syms.append(sym_id)
        elif sym_id.startswith("sym.var."):
            var_syms.append(sym_id)
        elif sym_id.startswith("sym.ds.") or sym_id.startswith("sym.ind."):
            other_syms.append(sym_id)

    if file_syms:
        lines.append("- **Files / DB Contracts**:")
        for sym_id in sorted(file_syms):
            sym = symbol_table.get(sym_id, {})
            label = sym.get("name") or sym_id
            contract = db_contracts_by_sym.get(sym_id)
            if contract and contract.get("columns"):
                col_summary = ", ".join(
                    f"{c.get('name')}" + (f" {c.get('type', '')}" if c.get("type") else "")
                    for c in contract["columns"][:20]
                )
                extra = f" +{len(contract['columns']) - 20} more" if len(contract["columns"]) > 20 else ""
                lines.append(f"  - {label} (columns: {col_summary}{extra})")
            else:
                lines.append(f"  - {label}")
        lines.append("")

    if var_syms:
        lines.append("- **Variables**:")
        for sym_id in sorted(var_syms):
            sym = symbol_table.get(sym_id, {})
            label = sym.get("name") or sym_id
            type_id = sym.get("typeId")
            suffix = f" (type: {type_id})" if type_id else ""
            lines.append(f"  - {label}{suffix}")
        lines.append("")

    if other_syms:
        lines.append("- **Other symbols**:")
        for sym_id in sorted(other_syms):
            sym = symbol_table.get(sym_id, {})
            label = sym.get("name") or sym_id
            lines.append(f"  - {label}")
        lines.append("")

    lines.append("- **Raw sem JSON**:")
    lines.append("```json")
    lines.append(json.dumps(sem, indent=2, ensure_ascii=False))
    lines.append("```")
    return "\n".join(lines)


# ---------------------------------------------------------------------------
# DB contract resolution
# ---------------------------------------------------------------------------

def resolve_db_contracts(
    node: dict,
    ast: dict,
    types: dict,
    db_registry: Dict[str, dict],
) -> Tuple[List[dict], Dict[str, dict]]:
    """Resolve DB contracts for a node's referenced sym.file.* symbols.

    Returns:
      - db_contracts: list of contract dicts (backward-compatible + enriched)
      - db_contracts_by_sym: dict keyed by symbolId for narrative building
    """
    native_files = ast.get("dbContracts", {}).get("nativeFiles", [])
    nf_by_sym = {nf.get("symbolId"): nf for nf in native_files if nf.get("symbolId")}

    sem = node.get("sem") or {}
    file_syms = [k for k in sem if isinstance(k, str) and k.startswith("sym.file.")]

    contracts = []
    contracts_by_sym = {}
    for sym_id in file_syms:
        nf = nf_by_sym.get(sym_id)
        if not nf:
            continue
        contract = _resolve_one_contract(nf, types, db_registry)
        contracts.append(contract)
        contracts_by_sym[sym_id] = contract

    return contracts, contracts_by_sym


def _resolve_one_contract(nf: dict, types: dict, db_registry: Dict[str, dict]) -> dict:
    """Resolve a single nativeFile entry to the backward-compatible + enriched format."""
    name = nf.get("name", "")
    symbol_id = nf.get("symbolId")
    library = nf.get("library")
    type_id = nf.get("typeId")
    record_format = nf.get("recordFormat")
    keyed = nf.get("keyed", False)

    key_names = {k.get("name") for k in (nf.get("keys") or []) if k.get("name")}

    columns = []
    for col in nf.get("columns") or []:
        col_name = col.get("name")
        col_type_id = col.get("typeId")
        nullable = col.get("nullable", False)
        is_key = col_name in key_names

        sql_type, length, scale = resolve_type_info(col_type_id, types)
        sql_type_short = sql_type
        if sql_type and "(" in sql_type:
            sql_type_short = sql_type[:sql_type.index("(")]

        columns.append({
            "name": col_name,
            "description": None,
            "type": sql_type_short,
            "length": length,
            "scale": scale,
            "key": is_key,
            "typeId": col_type_id,
            "sqlType": sql_type,
            "nullable": nullable,
        })

    keys_list = [{"name": k.get("name"), "ascending": k.get("ascending", True)}
                 for k in (nf.get("keys") or []) if k.get("name")]

    # Cross-program enrichment from db_registry
    source_units = []
    reg_entry = db_registry.get(name)
    if reg_entry:
        source_units = reg_entry.get("sourceUnits", [])

    return {
        "name": name,
        "symbolId": symbol_id,
        "library": library,
        "typeId": type_id,
        "recordFormat": record_format,
        "keyed": keyed,
        "keys": keys_list,
        "description": None,
        "columns": columns,
        "sourceUnits": source_units,
    }


# ---------------------------------------------------------------------------
# Symbol metadata extraction
# ---------------------------------------------------------------------------

def extract_symbol_metadata(
    node: dict,
    symbol_table: dict,
    program_ctx: dict | None,
) -> dict:
    """Extract relevant symbol metadata for the context package.

    Includes shared variable detection from the knowledge graph (program context).
    """
    sem = node.get("sem") or {}
    shared_symbols = set()
    if program_ctx:
        for sym_info in program_ctx.get("symbols", []):
            if sym_info.get("shared"):
                shared_symbols.add(sym_info.get("symbolId"))

    metadata = {}
    for sym_id in sem:
        if not isinstance(sym_id, str):
            continue
        if sym_id.startswith("sym.file.") or sym_id.startswith("sym.dspf."):
            continue
        sym = symbol_table.get(sym_id, {})
        entry = {
            "name": sym.get("name"),
            "kind": sym.get("kind"),
            "typeId": sym.get("typeId"),
            "scopeId": sym.get("scopeId"),
        }
        if sym_id in shared_symbols:
            entry["shared"] = True
        metadata[sym_id] = entry

    return metadata


# ---------------------------------------------------------------------------
# Tree traversal helpers
# ---------------------------------------------------------------------------

def _get_descendants(node_id: str, nodes_by_id: Dict[str, dict]) -> List[str]:
    """Get all descendant node IDs via BFS over children arrays."""
    result: List[str] = []
    queue = list(nodes_by_id.get(node_id, {}).get("children") or [])
    visited: set = set()
    while queue:
        cid = queue.pop(0)
        if cid in visited:
            continue
        visited.add(cid)
        result.append(cid)
        child_node = nodes_by_id.get(cid)
        if child_node:
            queue.extend(child_node.get("children") or [])
    return result


# ---------------------------------------------------------------------------
# Context package builder (one per node)
# ---------------------------------------------------------------------------

def build_context_package(
    node: dict,
    ast: dict,
    types: dict,
    symbol_table: dict,
    rpg_provider: RpgSourceProvider,
    db_registry: Dict[str, dict],
    call_graph_data: dict | None,
    program_ctx: dict | None,
    display_contracts: List[dict],
    all_edges: list,
    unit_id: str,
    program_id: str,
) -> dict:
    """Build one context_index JSON for a single AST node."""
    node_id = node.get("id")
    kind = node.get("kind")
    props = node.get("props") or {}
    name = props.get("name")
    sem = node.get("sem") or {}
    range_node = node.get("range") or {}

    # Resolve DB contracts
    db_contracts, db_by_sym = resolve_db_contracts(node, ast, types, db_registry)

    # Build narrative
    narrative = build_narrative(ast, node, db_by_sym, symbol_table, types)

    # Extract RPG snippet
    rpg_snippet = None
    file_id = range_node.get("fileId")
    start_line = range_node.get("startLine")
    end_line = range_node.get("endLine")
    if file_id and start_line and end_line:
        rpg_snippet = rpg_provider.get_snippet(file_id, start_line, end_line)

    # Build outgoing edges list (symbol IDs referenced by this node)
    outgoing_edges = sorted(k for k in sem if isinstance(k, str))

    # Convert range to backward-compatible format
    range_compat = {
        "sourceId": file_id,
        "startLine": start_line,
        "startColumn": range_node.get("startCol", 1),
        "endLine": end_line,
        "endColumn": range_node.get("endCol", 1),
    }

    # Symbol metadata with shared-variable detection
    symbol_metadata = extract_symbol_metadata(node, symbol_table, program_ctx)

    # Display files (from DDS AST)
    display_files = []
    dspf_refs = get_dspf_refs_for_node(node, all_edges)
    if dspf_refs and display_contracts:
        dspf_by_name = {}
        for dc in display_contracts:
            dc_name = dc.get("name")
            if dc_name:
                dspf_by_name[dc_name] = dc
                dspf_by_name[f"sym.dspf.{dc_name}"] = dc
        for ref in dspf_refs:
            dc = dspf_by_name.get(ref)
            ref_name = ref.replace("sym.dspf.", "")
            if dc:
                display_files.append({
                    "symbolId": ref,
                    "name": ref_name,
                    "fileId": None,
                    "path": None,
                    "uiContracts": dc,
                })
            else:
                display_files.append({
                    "symbolId": ref,
                    "name": ref_name,
                    "fileId": None,
                    "path": None,
                })

    # Call graph integration (full call graph for LLM context)
    call_graph_info: Dict[str, Any] = {}
    if call_graph_data:
        callees = call_graph_data.get("callerToCallees", {}).get(node_id, [])
        callers = call_graph_data.get("calleeToCallers", {}).get(node_id, [])
        call_graph_info["calls"] = callees
        call_graph_info["calledBy"] = callers
        # Subroutines/programs invoked by this node (from calls where callerNodeId == node_id)
        subroutines_invoked: List[str] = []
        calls_with_details: List[Dict[str, Any]] = []
        for c in call_graph_data.get("calls", []):
            if c.get("callerNodeId") != node_id:
                continue
            callee_name = c.get("calleeName")
            if callee_name and callee_name not in subroutines_invoked:
                subroutines_invoked.append(callee_name)
            calls_with_details.append({
                "calleeNodeId": c.get("calleeNodeId"),
                "calleeName": callee_name,
                "opcode": c.get("opcode"),
            })
        if subroutines_invoked:
            call_graph_info["subroutinesInvoked"] = subroutines_invoked
        if calls_with_details:
            call_graph_info["callsWithDetails"] = calls_with_details

    # Fine-grained statement nodes for traceability (descendants of this node)
    all_nodes = ast.get("nodes") or []
    nodes_by_id = {n["id"]: n for n in all_nodes if isinstance(n, dict) and "id" in n}
    descendant_ids = _get_descendants(node_id, nodes_by_id)
    statement_nodes = []
    line_to_node_map: Dict[str, List] = {}
    STATEMENT_KINDS = {
        "Eval", "If", "Else", "EndIf", "DoW", "EndDo", "Select", "When",
        "EndSelect", "Chain", "Read", "ReadE", "Reade", "Write", "Update", "Delete",
        "SetLL", "SetGT", "SetOn", "SetOff", "ExSr", "Return", "Monitor", "OnError", "EndMon",
        "Builtin", "DclF", "DclDS", "DclConst", "Assign", "Condition",
        "Procedure", "Subroutine", "Other", "Comment",
    }
    FILE_OP_KINDS = {"Chain", "Read", "ReadE", "Reade", "Write", "Update", "Delete", "SetLL", "SetGT"}
    for did in descendant_ids:
        dnode = nodes_by_id.get(did)
        if not dnode:
            continue
        dkind = dnode.get("kind")
        if dkind not in STATEMENT_KINDS:
            continue
        dr = dnode.get("range") or {}
        dprops = dnode.get("props") or {}
        dsem = dnode.get("sem") or {}
        summary = {
            "id": did,
            "kind": dkind,
            "opcode": dprops.get("opcode"),
        }
        if dr.get("startLine"):
            summary["startLine"] = dr["startLine"]
        if dr.get("endLine"):
            summary["endLine"] = dr["endLine"]
        target = dprops.get("target")
        if target:
            summary["target"] = target
        sym_refs = [k for k in dsem if isinstance(k, str) and k.startswith("sym.")]
        if sym_refs:
            summary["symbols"] = sym_refs[:8]
        statement_nodes.append(summary)
        # Line mapping
        sl = dr.get("startLine")
        el = dr.get("endLine")
        if sl:
            key = str(sl) if sl == el else f"{sl}-{el}"
            line_to_node_map.setdefault(key, []).append(did)

    # Build tableAccess from statement_nodes (file ops in this node's scope)
    table_access: Dict[str, List[Dict[str, Any]]] = {}
    for sn in statement_nodes:
        if sn.get("kind") not in FILE_OP_KINDS:
            continue
        target = sn.get("target")
        if not target or not isinstance(target, str):
            continue
        table_name = target.strip().upper()
        if not table_name:
            continue
        entry = {"nodeId": sn.get("id"), "opcode": (sn.get("opcode") or sn.get("kind", "")).upper()}
        if sn.get("startLine"):
            entry["line"] = sn["startLine"]
        table_access.setdefault(table_name, []).append(entry)
    if table_access:
        call_graph_info["tableAccess"] = table_access

    # Assemble the context package
    package = {
        "astNode": {
            "id": node_id,
            "kind": kind,
            "name": name,
            "sem": json.dumps(sem, ensure_ascii=False),
            "outgoingEdges": outgoing_edges,
            "range": range_compat,
        },
        "narrative": narrative,
        "rpgSnippet": rpg_snippet or "",
        "dbContracts": db_contracts,
        "symbolMetadata": symbol_metadata,
    }

    if display_files:
        package["displayFiles"] = display_files
    # Always include callGraph (calls, calledBy, tableAccess, subroutinesInvoked) for LLM context
    package["callGraph"] = call_graph_info
    if statement_nodes:
        package["statementNodes"] = statement_nodes
        package["lineToNodeMap"] = line_to_node_map

    return package


# ---------------------------------------------------------------------------
# Node selection (which nodes get context packages)
# ---------------------------------------------------------------------------

MIGRATABLE_KINDS = {"Subroutine", "Procedure", "CompilationUnit"}


def select_migratable_nodes(nodes: list) -> List[dict]:
    """Select AST nodes that represent migratable code units."""
    return [n for n in nodes if isinstance(n, dict) and n.get("kind") in MIGRATABLE_KINDS]


# ---------------------------------------------------------------------------
# Manifest builder
# ---------------------------------------------------------------------------

def build_manifest_entry(
    node: dict,
    unit_id: str,
    program_id: str,
    db_contracts: List[dict],
    call_graph_data: dict | None,
) -> dict:
    """Build one manifest.json entry."""
    node_id = node.get("id")
    kind = node.get("kind")
    props = node.get("props") or {}
    name = props.get("name")
    range_node = node.get("range") or {}
    file_id = range_node.get("fileId")

    db_files_used = sorted(set(c.get("name", "") for c in db_contracts if c.get("name")))

    calls = []
    called_by = []
    if call_graph_data:
        callees = call_graph_data.get("callerToCallees", {}).get(node_id, [])
        callers = call_graph_data.get("calleeToCallers", {}).get(node_id, [])
        calls = sorted(set(callees))
        called_by = sorted(set(callers))

    return {
        "unitId": program_id,
        "nodeId": node_id,
        "kind": kind,
        "name": name,
        "sourceFileId": file_id,
        "dbFilesUsed": db_files_used,
        "calls": calls,
        "calledBy": called_by,
    }


# ---------------------------------------------------------------------------
# Main pipeline
# ---------------------------------------------------------------------------

def build_index(ast_dir: Path, rpg_root: Path | None, output_dir: Path) -> None:
    context_dir = output_dir / "context_index"
    context_dir.mkdir(parents=True, exist_ok=True)

    rpg_provider = RpgSourceProvider(rpg_root)

    # Load knowledge graph artifacts
    print("[context-index] Loading knowledge graph artifacts...", file=sys.stderr)
    call_graph = load_call_graph(output_dir)
    db_registry = load_db_registry(output_dir)
    program_contexts = load_program_contexts(output_dir)
    print(f"[context-index]   Call graph: {len(call_graph)} programs", file=sys.stderr)
    print(f"[context-index]   DB registry: {len(db_registry)} files", file=sys.stderr)
    print(f"[context-index]   Program contexts: {len(program_contexts)} programs", file=sys.stderr)

    # Discover RPG AST files (skip display file ASTs)
    ast_paths = sorted(p for p in ast_dir.glob("*-ast.json")
                       if not p.stem.endswith("D-ast"))

    if not ast_paths:
        print(f"[context-index] No *-ast.json files in {ast_dir}", file=sys.stderr)
        return

    print(f"[context-index] Processing {len(ast_paths)} AST file(s)...", file=sys.stderr)

    manifest_entries = []
    total_contexts = 0

    for ast_path in ast_paths:
        ast = load_ast(ast_path)
        if not ast:
            continue

        unit = ast.get("unit") or {}
        unit_id = unit.get("id") or ""
        member = unit.get("member")
        program_id = member if isinstance(member, str) and member else (
            unit_id.split("/")[-1] if unit_id else ast_path.stem.replace("-ast", "")
        )

        types = ast.get("types") or {}
        symbol_table = ast.get("symbolTable") or {}
        all_edges = ast.get("edges") or []
        nodes = ast.get("nodes") or []

        cg_data = call_graph.get(program_id)
        prog_ctx = program_contexts.get(program_id)

        # Load display file contracts
        display_contracts = load_display_ast_contracts(ast_dir, unit_id)
        if display_contracts:
            print(f"[context-index]   {program_id}: loaded {len(display_contracts)} display file(s)", file=sys.stderr)

        migratable = select_migratable_nodes(nodes)
        print(f"[context-index]   {program_id}: {len(migratable)} migratable nodes out of {len(nodes)} total", file=sys.stderr)

        for node in migratable:
            node_id = node.get("id")
            if not node_id:
                continue

            package = build_context_package(
                node=node,
                ast=ast,
                types=types,
                symbol_table=symbol_table,
                rpg_provider=rpg_provider,
                db_registry=db_registry,
                call_graph_data=cg_data,
                program_ctx=prog_ctx,
                display_contracts=display_contracts,
                all_edges=all_edges,
                unit_id=unit_id,
                program_id=program_id,
            )

            out_path = context_dir / f"{program_id}_{node_id}.json"
            with out_path.open("w", encoding="utf-8") as f:
                json.dump(package, f, indent=2, ensure_ascii=False)
            total_contexts += 1

            # Manifest entry (with call graph relationships populated)
            entry = build_manifest_entry(
                node=node,
                unit_id=unit_id,
                program_id=program_id,
                db_contracts=package.get("dbContracts", []),
                call_graph_data=cg_data,
            )
            manifest_entries.append(entry)

    manifest = {"entries": manifest_entries}
    manifest_path = context_dir / "manifest.json"
    with manifest_path.open("w", encoding="utf-8") as f:
        json.dump(manifest, f, indent=2, ensure_ascii=False)

    print(f"[context-index] Done. Wrote {total_contexts} context file(s) and manifest.json", file=sys.stderr)


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Build context_index from rpg-ast/1.0 files with knowledge graph integration."
    )
    parser.add_argument(
        "--astDir", required=True,
        help="Directory containing RPG *-ast.json files (e.g. JSON_ast/JSON_20260227)",
    )
    parser.add_argument(
        "--rpgDir", default=None,
        help="RPG source root for snippet extraction (e.g. /Users/fkhan/Downloads/PoC_HS1210)",
    )
    parser.add_argument(
        "--outputDir", default=str(ROOT_DIR),
        help="Root project directory (default: script directory)",
    )
    args = parser.parse_args()

    ast_dir = Path(args.astDir)
    if not ast_dir.is_absolute():
        ast_dir = ROOT_DIR / ast_dir
    if not ast_dir.is_dir():
        print(f"AST directory not found: {ast_dir}", file=sys.stderr)
        sys.exit(1)

    rpg_root = Path(args.rpgDir) if args.rpgDir else None
    if rpg_root and not rpg_root.is_dir():
        print(f"RPG root not found: {rpg_root}", file=sys.stderr)
        sys.exit(1)

    output_dir = Path(args.outputDir)

    build_index(ast_dir, rpg_root, output_dir)


if __name__ == "__main__":
    main()
