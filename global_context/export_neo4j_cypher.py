#!/usr/bin/env python3
"""
Export global_context artifacts as a Neo4j Cypher script.

Inputs:
  - global_context/db_registry.json
  - global_context/programs/*.program.json
  - global_context/call_graph_enriched.json  (if present; falls back to call_graph.json)

Output:
  - global_context/neo4j_export.cypher

Schema (initial, focused):

  (:Program {programId, unitId})
  (:DbFile  {name, library, symbolId})
  (:Column  {name, sqlType, length, scale})
  (:Node    {nodeId, kind, name, programId})

Relationships:

  (p:Program)-[:USES_DB]->(f:DbFile)
  (f:DbFile)-[:HAS_COLUMN]->(c:Column)
  (p:Program)-[:HAS_NODE]->(n:Node)
  (caller:Node)-[:CALLS]->(callee:Node)   -- when callerNodeId/calleeNodeId known
"""

from __future__ import annotations

import json
from pathlib import Path
from typing import Any, Dict, List, Set

ROOT_DIR = Path(__file__).resolve().parents[1]
GC_DIR = ROOT_DIR / "global_context"
DB_REGISTRY_PATH = GC_DIR / "db_registry.json"
PROGRAMS_DIR = GC_DIR / "programs"
CALL_GRAPH_ENRICHED_PATH = GC_DIR / "call_graph_enriched.json"
CALL_GRAPH_PATH = GC_DIR / "call_graph.json"
OUTPUT_PATH = GC_DIR / "neo4j_export.cypher"


def load_json(path: Path) -> Dict[str, Any] | None:
    if not path.exists():
        return None
    text = path.read_text(encoding="utf-8", errors="ignore")
    try:
        return json.loads(text)
    except Exception:
        return None


def export_db_registry(lines: List[str]) -> None:
    data = load_json(DB_REGISTRY_PATH)
    if not data:
        return
    for f in data.get("files", []):
        name = f.get("name")
        library = f.get("library")
        symbol_id = f.get("symbolId")
        # DbFile
        lines.append(
            "MERGE (f:DbFile {name: $name, library: $library}) "
            "ON CREATE SET f.symbolId = $symbol_id "
            "ON MATCH SET  f.symbolId = coalesce(f.symbolId, $symbol_id);"
        )
        params = {"name": name, "library": library, "symbol_id": symbol_id}
        lines.append("// params " + json.dumps(params))

        # Columns
        for col in f.get("columns", []):
            col_name = col.get("name")
            if not col_name:
                continue
            c_params = {
                "name": col_name,
                "sqlType": col.get("sqlType"),
                "length": col.get("length"),
                "scale": col.get("scale"),
                "key": bool(col.get("key", False)),
            }
            lines.append(
                "MERGE (c:Column {name: $name, sqlType: $sqlType, length: $length, scale: $scale}) "
                "ON CREATE SET c.key = $key "
                "ON MATCH SET  c.key = coalesce(c.key, $key);"
            )
            lines.append("// params " + json.dumps(c_params))
            # Relationship
            rel_params = {"name": name, "library": library, "col_name": col_name}
            lines.append(
                "MATCH (f:DbFile {name: $name, library: $library}), "
                "      (c:Column {name: $col_name}) "
                "MERGE (f)-[:HAS_COLUMN]->(c);"
            )
            lines.append("// params " + json.dumps(rel_params))


def export_programs_and_nodes(lines: List[str], program_ids: Set[str]) -> Dict[str, Set[str]]:
    """
    Export Program and Node nodes from program context files.
    Returns mapping programId -> set of nodeIds created.
    """
    program_nodes: Dict[str, Set[str]] = {}
    if not PROGRAMS_DIR.is_dir():
        return program_nodes

    for ctx_path in sorted(PROGRAMS_DIR.glob("*.program.json")):
        ctx = load_json(ctx_path)
        if not ctx:
            continue
        program_id = ctx.get("programId") or ctx_path.stem.replace(".program", "")
        unit_id = ctx.get("unitId")
        program_ids.add(program_id)
        # Program node
        params = {"programId": program_id, "unitId": unit_id}
        lines.append(
            "MERGE (p:Program {programId: $programId}) "
            "ON CREATE SET p.unitId = $unitId "
            "ON MATCH SET  p.unitId = coalesce(p.unitId, $unitId);"
        )
        lines.append("// params " + json.dumps(params))

        # Nodes
        created_nodes: Set[str] = set()
        for node in ctx.get("nodes", []):
            node_id = node.get("id")
            kind = node.get("kind")
            name = node.get("name") or node.get("procedureName")
            if not node_id:
                continue
            n_params = {
                "nodeId": node_id,
                "kind": kind,
                "name": name,
                "programId": program_id,
            }
            lines.append(
                "MERGE (n:Node {nodeId: $nodeId}) "
                "ON CREATE SET n.kind = $kind, n.name = $name, n.programId = $programId "
                "ON MATCH SET  n.kind = coalesce(n.kind, $kind), "
                "              n.name = coalesce(n.name, $name), "
                "              n.programId = coalesce(n.programId, $programId);"
            )
            lines.append("// params " + json.dumps(n_params))

            rel_params = {"programId": program_id, "nodeId": node_id}
            lines.append(
                "MATCH (p:Program {programId: $programId}), (n:Node {nodeId: $nodeId}) "
                "MERGE (p)-[:HAS_NODE]->(n);"
            )
            lines.append("// params " + json.dumps(rel_params))
            created_nodes.add(node_id)

        program_nodes[program_id] = created_nodes

    return program_nodes


def export_program_db_usage(lines: List[str]) -> None:
    """
    Very coarse: wire each Program to DbFiles used in that program via sourceUnits.
    """
    data = load_json(DB_REGISTRY_PATH)
    if not data:
        return
    for f in data.get("files", []):
        name = f.get("name")
        library = f.get("library")
        for unit in f.get("sourceUnits", []):
            # programId is the last segment of unitId (e.g. HS1210)
            program_id = unit.split("/")[-1] if "/" in unit else unit
            params = {"programId": program_id, "name": name, "library": library}
            lines.append(
                "MATCH (p:Program {programId: $programId}), (f:DbFile {name: $name, library: $library}) "
                "MERGE (p)-[:USES_DB]->(f);"
            )
            lines.append("// params " + json.dumps(params))


def export_call_graph(lines: List[str]) -> None:
    """
    Export CALLS edges based on call_graph_enriched (or call_graph fallback).
    Only edges with both callerNodeId and calleeNodeId are exported.
    """
    data = load_json(CALL_GRAPH_ENRICHED_PATH) or load_json(CALL_GRAPH_PATH)
    if not data:
        return
    for prog in data.get("programs", []):
        calls = prog.get("calls", [])
        for call in calls:
            caller_id = call.get("callerNodeId")
            callee_id = call.get("calleeNodeId")
            if not caller_id or not callee_id:
                continue
            params = {"callerId": caller_id, "calleeId": callee_id}
            lines.append(
                "MATCH (c:Node {nodeId: $callerId}), (t:Node {nodeId: $calleeId}) "
                "MERGE (c)-[:CALLS]->(t);"
            )
            lines.append("// params " + json.dumps(params))


def main() -> None:
    lines: List[str] = []
    lines.append("// Neo4j export generated from global_context artifacts")
    lines.append("// Load this file in the Neo4j browser or Cypher shell;")
    lines.append("// lines starting with '// params' show suggested parameter maps.")
    lines.append("")

    # 1) DbFile + Column + HAS_COLUMN
    export_db_registry(lines)

    # 2) Programs and Nodes
    program_ids: Set[str] = set()
    export_programs_and_nodes(lines, program_ids)

    # 3) Program -> DbFile usage
    export_program_db_usage(lines)

    # 4) CALLS relationships
    export_call_graph(lines)

    OUTPUT_PATH.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Wrote Neo4j Cypher export to {OUTPUT_PATH}")


if __name__ == "__main__":
    main()

