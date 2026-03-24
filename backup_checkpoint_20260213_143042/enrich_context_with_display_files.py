#!/usr/bin/env python3
"""
Enriches context_index JSON files with display file (DSPF) information from the AST.

- Reads AST files to find sym.dspf.* symbols and their corresponding file id/path.
- For each context package, finds which display files are referenced by the node (sem or outgoingEdges).
- Optionally loads DDS source for each display file from --ddsDir (e.g. QDDSSRC/MEMBER).
- Adds a "displayFiles" array to each context package for UI-aware code generation.

Usage:
  python3 enrich_context_with_display_files.py --astDir JSON_ast/JSON_20260211 --contextDir context_index [--ddsDir path/to/dds]
"""

import argparse
import json
from pathlib import Path
from typing import List, Optional


def load_ast(ast_path: Path) -> dict:
    with open(ast_path, "r", encoding="utf-8", errors="replace") as f:
        return json.load(f)


def get_display_files_from_ast(ast: dict) -> List[dict]:
    """Extract display file metadata from AST: symbolTable (kind=dspf) + files[]."""
    symbol_table = ast.get("symbolTable") or {}
    files_list = ast.get("files") or []
    # Build map: member name -> file entry (id, path)
    file_by_name = {}
    for fe in files_list:
        fid = fe.get("id") or ""
        # id is like "qsys:HSSRC/QDDSSRC/HS1212D" -> member name is last segment
        if "/" in fid:
            name = fid.split("/")[-1]
            file_by_name[name] = {"fileId": fid, "path": fe.get("path") or ""}

    display_files = []
    for sym_id, sym in symbol_table.items():
        if sym.get("kind") != "dspf":
            continue
        name = sym.get("name") or sym_id.replace("sym.dspf.", "")
        file_info = file_by_name.get(name)
        if file_info:
            display_files.append({
                "symbolId": sym_id,
                "name": name,
                "fileId": file_info["fileId"],
                "path": file_info["path"],
            })
        else:
            display_files.append({
                "symbolId": sym_id,
                "name": name,
                "fileId": None,
                "path": None,
            })
    return display_files


def get_dspf_refs_for_node(context: dict) -> set[str]:
    """Return set of sym.dspf.* symbol IDs referenced by this context's node (sem or outgoingEdges)."""
    refs = set()
    ast_node = context.get("astNode") or {}
    # sem is often a stringified JSON map
    sem = ast_node.get("sem")
    if isinstance(sem, str):
        try:
            sem = json.loads(sem)
        except json.JSONDecodeError:
            sem = {}
    if isinstance(sem, dict):
        for key in sem:
            if key.startswith("sym.dspf."):
                refs.add(key)
    for edge in ast_node.get("outgoingEdges") or []:
        if isinstance(edge, str) and edge.startswith("sym.dspf."):
            refs.add(edge)
    return refs


def get_dspf_refs_from_ast_edges(ast: dict, node_id: str) -> set[str]:
    """Return set of sym.dspf.* symbol IDs that this node references via AST top-level edges.
    Edge format: { \"src\": \"n123\", \"dst\": \"sym.dspf.HS1210D\", \"kind\": \"refers_to\" } (or \"uses\", \"declares\")."""
    refs = set()
    for edge in ast.get("edges") or []:
        if not isinstance(edge, dict):
            continue
        if edge.get("src") != node_id:
            continue
        dst = edge.get("dst") or ""
        if dst.startswith("sym.dspf."):
            refs.add(dst)
    return refs


def resolve_dds_source(dds_dir: Path, file_id: str, name: str) -> Optional[str]:
    """Try to read DDS source for a display file. fileId is like qsys:HSSRC/QDDSSRC/HS1212D."""
    if not file_id or not dds_dir.is_dir():
        return None
    # Try QDDSSRC/NAME or NAME (flat)
    for sub in ["QDDSSRC", ""]:
        base = dds_dir / sub if sub else dds_dir
        for ext in [".mbr", ".txt", ".dspf", ""]:
            p = base / (name + ext)
            if p.is_file():
                try:
                    return p.read_text(encoding="utf-8", errors="replace")
                except Exception:
                    return None
    return None


def enrich_context_file(
    context_path: Path,
    ast_dir: Path,
    dds_dir: Optional[Path],
    unit_ast_cache: dict,
    attach_unit_dspf: bool = False,
) -> bool:
    """Enrich one context JSON with displayFiles. Returns True if modified."""
    # e.g. HS1212_n498.json -> unit_id=HS1212, node_id=n498
    stem = context_path.stem
    if "_" not in stem:
        return False
    unit_id, node_id = stem.split("_", 1)

    # Load AST for this unit
    if unit_id not in unit_ast_cache:
        ast_file = ast_dir / f"{unit_id}-ast.json"
        if not ast_file.is_file():
            return False
        unit_ast_cache[unit_id] = load_ast(ast_file)
    ast = unit_ast_cache[unit_id]
    all_dspf = get_display_files_from_ast(ast)
    if not all_dspf:
        return False

    with open(context_path, "r", encoding="utf-8") as f:
        context = json.load(f)
    # DSPF refs: from context (node.sem / node.outgoingEdges) and from AST top-level edges
    refs = get_dspf_refs_for_node(context) | get_dspf_refs_from_ast_edges(ast, node_id)
    # Optional: attach all unit-level DSPFs to every context (for UI building even when no per-node ref)
    if attach_unit_dspf and not refs:
        refs = {d["symbolId"] for d in all_dspf}
    if not refs:
        return False

    # Build displayFiles for referenced DSPFs only
    dspf_by_id = {d["symbolId"]: d for d in all_dspf}
    display_files = []
    for sym_id in refs:
        info = dspf_by_id.get(sym_id)
        if not info:
            info = {"symbolId": sym_id, "name": sym_id.replace("sym.dspf.", ""), "fileId": None, "path": None}
        entry = dict(info)
        if dds_dir and entry.get("name"):
            dds_source = resolve_dds_source(dds_dir, entry.get("fileId"), entry["name"])
            if dds_source:
                entry["ddsSource"] = dds_source
        display_files.append(entry)

    context["displayFiles"] = display_files
    with open(context_path, "w", encoding="utf-8") as f:
        json.dump(context, f, indent=2, ensure_ascii=False)
    return True


def main():
    ap = argparse.ArgumentParser(description="Enrich context packages with display file (DSPF) info from AST.")
    ap.add_argument("--astDir", required=True, help="Directory containing *-ast.json files (e.g. JSON_ast/JSON_20260211)")
    ap.add_argument("--contextDir", default="context_index", help="Directory containing context_index JSON files")
    ap.add_argument("--ddsDir", default=None, help="Optional: directory containing DDS source (e.g. QDDSSRC with .mbr/.txt)")
    ap.add_argument("--attachUnitDspf", action="store_true", help="If set, attach all unit-level DSPFs to every context (even when node has no ref)")
    args = ap.parse_args()

    ast_dir = Path(args.astDir)
    context_dir = Path(args.contextDir)
    dds_dir = Path(args.ddsDir) if args.ddsDir else None

    if not ast_dir.is_dir():
        print(f"AST dir not found: {ast_dir}")
        return 1
    if not context_dir.is_dir():
        print(f"Context dir not found: {context_dir}")
        return 1

    unit_ast_cache = {}
    updated = 0
    for path in sorted(context_dir.glob("*.json")):
        if path.name == "manifest.json":
            continue
        if enrich_context_file(path, ast_dir, dds_dir, unit_ast_cache, attach_unit_dspf=args.attachUnitDspf):
            updated += 1
            print(f"Enriched {path.name} with display files")
    print(f"Done. Enriched {updated} context file(s).")
    return 0


if __name__ == "__main__":
    exit(main())
