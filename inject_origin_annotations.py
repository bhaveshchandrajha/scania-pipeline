#!/usr/bin/env python3
"""
Inject inline @origin annotations into generated Java files.

Adds // @origin {program} L{start}-{end} ({kind}) comments before code blocks
that have RPG traceability, so developers can see origin directly in the IDE.

Usage:
  python3 inject_origin_annotations.py --java-dir path/to/java --program HS1210 --node n404
  python3 inject_origin_annotations.py --context context_index/HS1210_n404.json --java-dir path/to/java

Called automatically by migrate_to_pure_java.py when --add-inline-origin is set (default).
"""

import argparse
import json
import re
import sys
from pathlib import Path

MATCH_PRIORITY = {"Chain": 1, "Read": 2, "Write": 3, "Update": 4, "SetLL": 5, "If": 6, "Eval": 7, "ExSr": 8}


def _synthesize_node_rpg(n: dict) -> str:
    """Build a minimal RPG source line from node properties."""
    kind = n.get("kind", "")
    props = n.get("props", {})
    opcode = props.get("opcode", kind)
    target = props.get("target", "")
    if target:
        return f"{opcode} {target}"
    return opcode or kind


def compute_origin_blocks(
    java_root: Path,
    program_id: str,
    entry_node_id: str,
    root_dir: Path,
) -> list[dict]:
    """
    Compute origin blocks for each Java file under java_root.
    Returns list of {path, content, originBlocks, ...}.
    """
    import re as _re

    context_dir = root_dir / "context_index"
    node_index_path = context_dir / f"{program_id}_nodes.json"
    if not node_index_path.is_file():
        return []

    try:
        ni_data = json.loads(node_index_path.read_text(encoding="utf-8", errors="ignore"))
    except Exception:
        return []

    node_index = ni_data.get("nodes", {})
    root_node = node_index.get(entry_node_id, {})
    root_range = root_node.get("range", {})
    root_start = root_range.get("startLine", 0)
    root_end = root_range.get("endLine", 999999)

    sym_lookup: dict[str, list] = {}
    db_file_nodes: dict[str, list] = {}
    rpg_stmts_by_kind: dict[str, list] = {}

    for nid, n in node_index.items():
        kind = n.get("kind", "")
        props = n.get("props", {})
        opcode = props.get("opcode", "")
        target = props.get("target", "")
        rpg_src = n.get("rpgSource", "") or _synthesize_node_rpg(n)
        node_range = n.get("range") or {}
        sl = node_range.get("startLine", 0)

        if kind in ("Chain", "Read", "Write", "Update", "SetLL", "DclF") and target:
            db_file_nodes.setdefault(target.upper(), []).append({
                "nodeId": nid, "kind": kind, "opcode": opcode, "rpgSource": rpg_src, "line": sl,
            })

        for sym_key in n.get("sem", {}):
            parts = sym_key.split(".")
            short_name = parts[-1].upper() if parts else ""
            if short_name and len(short_name) > 1:
                sym_lookup.setdefault(short_name, []).append({
                    "nodeId": nid, "kind": kind, "opcode": opcode, "rpgSource": rpg_src,
                })

        if root_start <= sl <= root_end and kind in (
            "Chain", "Read", "Write", "Update", "SetLL",
            "Eval", "If", "DoW", "ExSr", "Select", "When",
        ):
            el = node_range.get("endLine", sl)
            rpg_stmts_by_kind.setdefault(kind, []).append({
                "nodeId": nid, "kind": kind, "opcode": opcode, "target": target,
                "rpgSource": rpg_src, "line": sl, "endLine": el,
            })

    for kind_list in rpg_stmts_by_kind.values():
        kind_list.sort(key=lambda x: x["line"])

    LAYER_ORDER = {"domain": 0, "repository": 1, "service": 2, "dto": 3, "web": 4, "config": 5}
    TABLE_RE = _re.compile(r'@Table\s*\(\s*name\s*=\s*"([^"]+)"')
    COLUMN_RE = _re.compile(r'@Column\s*\([^)]*name\s*=\s*"([^"]+)"')
    IDENT_RE = _re.compile(r'[A-Za-z_]\w*')
    FINDBY_RE = _re.compile(r'\.\s*find\w*By|\.findAll|\.findMax|\.count\w*By', _re.IGNORECASE)
    QUERY_RE = _re.compile(r'@Query\s*\(')
    REPO_METHOD_RE = _re.compile(
        r'\b(find\w*By|findAll\w*|findOne|findMax\w*|findFirst|count\w*By)\s*\(',
        _re.IGNORECASE,
    )
    SAVE_RE = _re.compile(r'\.\s*save\s*\(')
    SETTER_RE = _re.compile(r'\.\s*set[A-Z]\w*\s*\(')
    IF_RE = _re.compile(r'^\s*(?:if|else\s+if)\s*\(')
    FOR_RE = _re.compile(r'^\s*for\s*\(')
    THROW_RE = _re.compile(r'^\s*throw\s+')

    file_paths = []
    pkg = java_root / "com" / "scania" / "warranty"
    if pkg.is_dir():
        for sub in ("domain", "service", "repository", "dto", "web", "config"):
            sd = pkg / sub
            if sd.is_dir():
                for jf in sorted(sd.glob("*.java")):
                    file_paths.append(str(jf.relative_to(java_root)))
    if not file_paths:
        for jf in sorted(java_root.rglob("*.java")):
            file_paths.append(str(jf.relative_to(java_root)))

    files_out = []
    for fp in file_paths:
        full = java_root / fp
        if not full.is_file():
            continue
        try:
            content = full.read_text(encoding="utf-8", errors="replace")
        except Exception:
            continue
        lines = content.splitlines()
        parts = fp.replace("\\", "/").split("/")
        layer = "other"
        for p in parts:
            if p in LAYER_ORDER:
                layer = p
                break
        class_name = parts[-1].replace(".java", "")

        ord_counters: dict[str, int] = {}

        def _next_rpg(rpg_kind: str) -> dict | None:
            idx = ord_counters.get(rpg_kind, 0)
            stmts = rpg_stmts_by_kind.get(rpg_kind, [])
            if idx < len(stmts):
                ord_counters[rpg_kind] = idx + 1
                return stmts[idx]
            return None

        blocks = []
        current_block = None

        for i, line in enumerate(lines):
            stripped = line.strip()
            if not stripped or stripped.startswith("//") or stripped.startswith("*"):
                if current_block:
                    current_block["endLine"] = i
                continue
            if stripped.startswith("package ") or stripped.startswith("import "):
                continue

            origin_tag = None
            tm = TABLE_RE.search(line)
            if tm:
                rpg_file = tm.group(1).upper()
                ops = db_file_nodes.get(rpg_file, [])
                rpg_lines = [o.get("rpgSource", "") for o in ops if o.get("rpgSource")][:5]
                if not rpg_lines:
                    rpg_lines = [f"CHAIN {rpg_file}", f"READ {rpg_file}", f"WRITE {rpg_file}"]
                origin_tag = {
                    "kind": "DB_FILE", "label": rpg_file,
                    "detail": f"Entity mapped from RPG DB file {rpg_file}",
                    "rpgLines": rpg_lines,
                }

            if not origin_tag:
                cm = COLUMN_RE.search(line)
                if cm:
                    origin_tag = {
                        "kind": "FIELD", "label": cm.group(1).upper(),
                        "detail": f"RPG field {cm.group(1).upper()} (column in DB file)",
                    }

            if not origin_tag and layer in ("service", "repository"):
                rpg_node = None
                is_db_lookup = FINDBY_RE.search(line) or QUERY_RE.search(line) or REPO_METHOD_RE.search(line)
                if is_db_lookup:
                    rpg_node = _next_rpg("Chain") or _next_rpg("Read") or _next_rpg("SetLL")
                    if rpg_node:
                        origin_tag = {
                            "kind": "CHAIN", "label": rpg_node["opcode"] or "CHAIN",
                            "detail": rpg_node["rpgSource"],
                            "rpgLine": rpg_node.get("line"),
                            "rpgEndLine": rpg_node.get("endLine", rpg_node.get("line")),
                        }
                    else:
                        origin_tag = {"kind": "CHAIN", "label": "CHAIN/READ", "detail": "DB lookup — RPG CHAIN/READ"}
                elif SAVE_RE.search(line):
                    rpg_node = _next_rpg("Write") or _next_rpg("Update")
                    if rpg_node:
                        origin_tag = {
                            "kind": "WRITE", "label": rpg_node["opcode"] or "WRITE",
                            "detail": rpg_node["rpgSource"],
                            "rpgLine": rpg_node.get("line"),
                            "rpgEndLine": rpg_node.get("endLine", rpg_node.get("line")),
                        }
                    else:
                        origin_tag = {"kind": "WRITE", "label": "WRITE", "detail": "DB write — RPG WRITE/UPDATE"}
                elif SETTER_RE.search(line):
                    rpg_node = _next_rpg("Eval")
                    if rpg_node:
                        origin_tag = {
                            "kind": "EVAL", "label": "EVAL",
                            "detail": rpg_node["rpgSource"],
                            "rpgLine": rpg_node.get("line"),
                            "rpgEndLine": rpg_node.get("endLine", rpg_node.get("line")),
                        }
                    else:
                        origin_tag = {"kind": "EVAL", "label": "EVAL", "detail": "Field assignment — RPG EVAL"}
                elif IF_RE.search(line):
                    rpg_node = _next_rpg("If")
                    if rpg_node:
                        origin_tag = {
                            "kind": "IF", "label": "IF",
                            "detail": rpg_node["rpgSource"],
                            "rpgLine": rpg_node.get("line"),
                            "rpgEndLine": rpg_node.get("endLine", rpg_node.get("line")),
                        }
                    else:
                        origin_tag = {"kind": "IF", "label": "IF", "detail": "Condition — RPG IF"}
                elif FOR_RE.search(line):
                    rpg_node = _next_rpg("DoW")
                    if rpg_node:
                        origin_tag = {
                            "kind": "DOW", "label": "DOW",
                            "detail": rpg_node["rpgSource"],
                            "rpgLine": rpg_node.get("line"),
                            "rpgEndLine": rpg_node.get("endLine", rpg_node.get("line")),
                        }
                    else:
                        origin_tag = {"kind": "DOW", "label": "DOW", "detail": "Loop — RPG DOW"}
                elif THROW_RE.search(line):
                    rpg_node = _next_rpg("ExSr")
                    if rpg_node:
                        origin_tag = {
                            "kind": "EXSR", "label": "EXSR",
                            "detail": rpg_node["rpgSource"],
                            "rpgLine": rpg_node.get("line"),
                            "rpgEndLine": rpg_node.get("endLine", rpg_node.get("line")),
                        }
                    else:
                        origin_tag = {"kind": "EXSR", "label": "EXSR", "detail": "Error handling — RPG EXSR"}

            if not origin_tag and layer in ("service", "repository"):
                idents = set(w.upper() for w in IDENT_RE.findall(line) if len(w) > 2)
                best_match = None
                for ident in idents:
                    if ident in sym_lookup:
                        for h in sym_lookup[ident]:
                            if h["kind"] in ("Chain", "Read", "Write", "Update", "SetLL", "Eval", "If", "ExSr"):
                                if best_match is None or MATCH_PRIORITY.get(h["kind"], 99) < MATCH_PRIORITY.get(best_match["kind"], 99):
                                    best_match = h
                if best_match:
                    origin_tag = {
                        "kind": best_match["kind"].upper(),
                        "label": best_match["opcode"] or best_match["kind"],
                        "detail": best_match.get("rpgSource", ""),
                        "rpgLine": None,
                        "rpgEndLine": None,
                    }

            if origin_tag:
                tag_key = origin_tag["kind"] + ":" + origin_tag["label"]
                if current_block and current_block.get("_key") == tag_key and i - current_block["endLine"] <= 3:
                    current_block["endLine"] = i
                else:
                    if current_block:
                        blocks.append(current_block)
                    current_block = {
                        "startLine": i, "endLine": i,
                        "origin": origin_tag, "_key": tag_key,
                    }
            elif current_block and i - current_block["endLine"] <= 2:
                current_block["endLine"] = i
            else:
                if current_block:
                    blocks.append(current_block)
                    current_block = None

        if current_block:
            blocks.append(current_block)
        for b in blocks:
            b.pop("_key", None)

        files_out.append({
            "path": fp,
            "content": content,
            "lines": lines,
            "originBlocks": blocks,
        })

    return files_out


# Regex to find LLM-generated @rpg-trace annotations (semantic traceability from LLM)
# Matches: " // @rpg-trace: n248" at end of line, or standalone "// @rpg-trace: n248"
RPG_TRACE_RE = re.compile(r'(?:^|\s)//\s*@rpg-trace:\s*(n\d+)\s*$', re.IGNORECASE)
# Regex to strip existing @origin lines (avoid duplicates on re-run)
ORIGIN_LINE_RE = re.compile(r'^\s*//\s*@origin\s+[\w]+\s+L\d+', re.IGNORECASE)


def _resolve_node_to_origin(node_index: dict, node_id: str, program_id: str) -> str | None:
    """Resolve AST node ID to @origin string. Returns None if node not found."""
    nodes = node_index.get("nodes", {})
    n = nodes.get(node_id)
    if not n:
        return None
    r = n.get("range") or {}
    sl = r.get("startLine")
    if sl is None:
        return None
    el = r.get("endLine", sl)
    kind = (n.get("props") or {}).get("opcode") or n.get("kind", "?")
    return f"// @origin {program_id} L{sl}-{el} ({kind})"


def inject_annotations(
    java_root: Path,
    program_id: str,
    entry_node_id: str,
    root_dir: Path,
) -> int:
    """
    Inject // @origin comments into Java files. Returns count of files modified.

    Uses two sources for traceability (in order of preference):
    1. LLM-generated @rpg-trace: nXXX — resolved via node index (semantic, accurate)
    2. Ordinal matching — structural fallback when @rpg-trace is absent
    """
    node_index_path = root_dir / "context_index" / f"{program_id}_nodes.json"
    node_index: dict = {}
    if node_index_path.is_file():
        try:
            node_index = json.loads(node_index_path.read_text(encoding="utf-8", errors="ignore"))
        except Exception:
            pass

    files_data = compute_origin_blocks(java_root, program_id, entry_node_id, root_dir)
    modified = 0

    for fd in files_data:
        blocks = fd.get("originBlocks", [])
        lines = fd["lines"]
        path = fd["path"]

        # 1) Prefer LLM @rpg-trace when present (semantic mapping)
        annotations: dict[int, str] = {}
        lines_to_strip: dict[int, str] = {}  # line_idx -> line with @rpg-trace removed
        lines_to_skip: set[int] = set()  # standalone @rpg-trace lines to remove
        for i, line in enumerate(lines):
            m = RPG_TRACE_RE.search(line)
            if m and node_index:
                node_id = m.group(1)
                ann = _resolve_node_to_origin(node_index, node_id, program_id)
                if ann:
                    code_before = line[: m.start()].strip()
                    if code_before:
                        annotations[i] = ann
                        lines_to_strip[i] = line[: m.start()].rstrip()
                    else:
                        # Standalone @rpg-trace: applies to next line
                        lines_to_skip.add(i)
                        if i + 1 < len(lines):
                            annotations[i + 1] = ann

        # 2) Fall back to ordinal matching for lines without @rpg-trace
        for b in blocks:
            start = b["startLine"]
            if start in annotations:
                continue  # Already have semantic annotation
            o = b.get("origin", {})
            rpg_line = o.get("rpgLine")
            if rpg_line is None:
                continue
            rpg_end = o.get("rpgEndLine", rpg_line)
            kind = o.get("kind", "?")
            annotations[start] = f"// @origin {program_id} L{rpg_line}-{rpg_end} ({kind})"

        if not annotations:
            continue

        new_lines = []
        for i, line in enumerate(lines):
            if i in lines_to_skip:
                continue  # Remove standalone @rpg-trace lines
            # Skip standalone @origin lines from previous runs (avoid duplicates)
            stripped = line.strip()
            if ORIGIN_LINE_RE.match(stripped) and ";" not in stripped and "{" not in stripped:
                continue
            if i in lines_to_strip:
                line = lines_to_strip[i]
            if i in annotations:
                indent = len(line) - len(line.lstrip()) if line.strip() else 0
                prefix = " " * indent if indent else ""
                new_lines.append(f"{prefix}{annotations[i]}")
            new_lines.append(line)

        new_content = "\n".join(new_lines)
        if new_content != fd["content"]:
            target = java_root / path
            target.write_text(new_content, encoding="utf-8")
            modified += 1
            print(f"// ✅ Injected @origin annotations into {path}", file=sys.stderr)

    return modified


def main() -> None:
    parser = argparse.ArgumentParser(description="Inject inline @origin annotations into generated Java files")
    parser.add_argument("--java-dir", required=True, help="Root directory containing Java files (e.g. src/main/java)")
    parser.add_argument("--program", help="RPG program ID (e.g. HS1210)")
    parser.add_argument("--node", help="Entry node ID (e.g. n404)")
    parser.add_argument("--context", help="Context file path (e.g. context_index/HS1210_n404.json) - overrides --program/--node")
    parser.add_argument("--root-dir", default=".", help="Project root (for context_index)")
    args = parser.parse_args()

    root_dir = Path(args.root_dir).resolve()
    java_root = Path(args.java_dir).resolve()

    if args.context:
        ctx_path = Path(args.context)
        if not ctx_path.is_absolute():
            ctx_path = root_dir / ctx_path
        stem = ctx_path.stem
        if "_" in stem:
            program_id, node_id = stem.split("_", 1)
        else:
            program_id = stem
            node_id = "unknown"
    elif args.program and args.node:
        program_id = args.program
        node_id = args.node
    else:
        print("Error: provide --context or both --program and --node", file=sys.stderr)
        sys.exit(1)

    if not java_root.is_dir():
        print(f"Error: Java directory not found: {java_root}", file=sys.stderr)
        sys.exit(1)

    n = inject_annotations(java_root, program_id, node_id, root_dir)
    if n > 0:
        print(f"\n// ✅ Injected @origin annotations into {n} file(s)", file=sys.stderr)
    else:
        print("// No annotations injected (no blocks with RPG line mapping, or node index not found)", file=sys.stderr)


if __name__ == "__main__":
    main()
