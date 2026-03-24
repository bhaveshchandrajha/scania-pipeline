#!/usr/bin/env python3
"""
Build fine-grained AST node index for RPG → Java traceability.

For each RPG AST file, creates a comprehensive node index containing:
  - All AST nodes (not just migratable ones) with full properties
  - Parent-child relationships (tree structure)
  - RPG source line(s) for each node
  - Line-to-node mapping for quick lookup

Inputs:
  --astDir    Directory containing RPG *-ast.json files
  --rpgDir    RPG source root for line extraction (optional)
  --outputDir Root project directory (default: current directory)

Outputs:
  context_index/<unitId>_nodes.json    (one per AST file)

USAGE:
  python3 build_node_index.py --astDir JSON_ast/JSON_20260227 --rpgDir /path/to/PoC_HS1210
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple

ROOT_DIR = Path(__file__).resolve().parent


def load_ast(path: Path) -> dict | None:
    try:
        raw = path.read_bytes()
    except Exception as e:
        print(f"[node-index] Cannot read {path}: {e}", file=sys.stderr)
        return None
    for enc in ("utf-8", "latin-1"):
        try:
            return json.loads(raw.decode(enc))
        except Exception:
            continue
    print(f"[node-index] Cannot decode {path}", file=sys.stderr)
    return None


class RpgSourceProvider:
    """Loads RPG source files and extracts line ranges."""

    def __init__(self, rpg_root: Path | None):
        self._root = rpg_root
        self._cache: Dict[str, List[str] | None] = {}

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


def build_parent_map(nodes: list) -> Dict[str, str]:
    """Build a child -> parent mapping from nodes' children arrays."""
    parent_map: Dict[str, str] = {}
    for node in nodes:
        nid = node.get("id")
        if not nid:
            continue
        for child_id in (node.get("children") or []):
            parent_map[child_id] = nid
    return parent_map


def get_descendants(node_id: str, nodes_by_id: Dict[str, dict]) -> List[str]:
    """Get all descendant node IDs (recursive BFS)."""
    result = []
    queue = list(nodes_by_id.get(node_id, {}).get("children") or [])
    visited = set()
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


def build_node_index_for_ast(
    ast: dict,
    rpg_provider: RpgSourceProvider | None,
) -> dict:
    """Build the complete node index for a single AST."""
    unit = ast.get("unit") or {}
    unit_id = unit.get("id") or ""
    member = unit.get("member")
    program_id = (
        member if isinstance(member, str) and member
        else (unit_id.split("/")[-1] if unit_id else "unknown")
    )

    nodes = ast.get("nodes") or []
    symbol_table = ast.get("symbolTable") or {}
    types = ast.get("types") or {}

    nodes_by_id: Dict[str, dict] = {}
    for n in nodes:
        if isinstance(n, dict) and "id" in n:
            nodes_by_id[n["id"]] = n

    parent_map = build_parent_map(nodes)

    # Build line-to-nodes mapping
    line_to_nodes: Dict[int, List[str]] = {}
    for node in nodes:
        r = node.get("range")
        if not r:
            continue
        start = r.get("startLine")
        end = r.get("endLine")
        nid = node.get("id")
        if start is not None and end is not None and nid:
            for line in range(start, end + 1):
                line_to_nodes.setdefault(line, []).append(nid)

    # Build node entries with RPG source
    node_entries: Dict[str, dict] = {}
    for node in nodes:
        nid = node.get("id")
        if not nid:
            continue

        r = node.get("range") or {}
        rpg_source = None
        if rpg_provider:
            file_id = r.get("fileId")
            start = r.get("startLine")
            end = r.get("endLine")
            if file_id and start and end:
                rpg_source = rpg_provider.get_snippet(file_id, start, end)

        entry: Dict[str, Any] = {
            "id": nid,
            "kind": node.get("kind"),
        }
        if r:
            entry["range"] = r
        props = node.get("props")
        if props:
            entry["props"] = props
        sem = node.get("sem")
        if sem:
            entry["sem"] = sem
        children = node.get("children")
        if children:
            entry["children"] = children
        pid = parent_map.get(nid)
        if pid:
            entry["parentId"] = pid
        if rpg_source:
            entry["rpgSource"] = rpg_source

        node_entries[nid] = entry

    # Compact line map
    compact_line_map: Dict[str, List[str]] = {}
    for line, nids in sorted(line_to_nodes.items()):
        compact_line_map[str(line)] = nids

    return {
        "unitId": unit_id,
        "programId": program_id,
        "nodeCount": len(node_entries),
        "nodes": node_entries,
        "lineToNodes": compact_line_map,
        "symbolTable": symbol_table,
        "types": types,
    }


def build_all_indexes(ast_dir: Path, rpg_root: Path | None, output_dir: Path) -> None:
    """Build node index files for all ASTs in the directory."""
    context_dir = output_dir / "context_index"
    context_dir.mkdir(parents=True, exist_ok=True)

    rpg_provider = RpgSourceProvider(rpg_root)

    ast_paths = sorted(
        p for p in ast_dir.glob("*-ast.json")
        if not p.stem.endswith("D-ast")
    )

    if not ast_paths:
        print(f"[node-index] No *-ast.json files in {ast_dir}", file=sys.stderr)
        return

    print(f"[node-index] Processing {len(ast_paths)} AST file(s)...", file=sys.stderr)

    for ast_path in ast_paths:
        ast = load_ast(ast_path)
        if not ast:
            continue

        index = build_node_index_for_ast(ast, rpg_provider)
        program_id = index["programId"]

        out_path = context_dir / f"{program_id}_nodes.json"
        with out_path.open("w", encoding="utf-8") as f:
            json.dump(index, f, indent=1, ensure_ascii=False)

        size_kb = out_path.stat().st_size / 1024
        print(
            f"[node-index] {program_id}: {index['nodeCount']} nodes, "
            f"{len(index['lineToNodes'])} source lines → "
            f"{out_path.name} ({size_kb:.0f} KB)",
            file=sys.stderr,
        )

    print(f"[node-index] Done.", file=sys.stderr)


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Build fine-grained AST node index for RPG→Java traceability."
    )
    parser.add_argument(
        "--astDir", required=True,
        help="Directory containing RPG *-ast.json files",
    )
    parser.add_argument(
        "--rpgDir", default=None,
        help="RPG source root for snippet extraction",
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
    build_all_indexes(ast_dir, rpg_root, output_dir)


if __name__ == "__main__":
    main()
