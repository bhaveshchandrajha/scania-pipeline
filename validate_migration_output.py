#!/usr/bin/env python3
"""
Validate migration output against call graph rules.

Enforces:
1. Entity consolidation: each physical table in tableAccess has exactly one @Entity
2. No duplicate entities for the same table
3. Subroutines invoked have corresponding service methods (best-effort)

USAGE:
  python3 validate_migration_output.py <context_json> <output_dir_or_project>
  python3 validate_migration_output.py context_index/HS1210_n404.json warranty_demo

Returns exit code 0 if validation passes, 1 if issues found.
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path
from typing import Any, Dict, List, Optional, Set, Tuple


def _load_context(context_path: Path) -> dict:
    with open(context_path, "r", encoding="utf-8") as f:
        return json.load(f)


def _extract_entities_from_java(files: Dict[str, str]) -> Dict[str, List[str]]:
    """
    Extract @Table(name="X") -> [entity_class_name, ...] from Java files.
    Returns table_name (upper) -> list of entity class names that map to it.
    """
    table_to_entities: Dict[str, List[str]] = {}
    for file_path, content in files.items():
        if "domain" not in file_path.lower() or not content:
            continue
        # Match @Entity and @Table(name="X") or @Table(name='X')
        table_match = re.search(
            r'@Table\s*\(\s*name\s*=\s*["\']([^"\']+)["\']',
            content,
            re.IGNORECASE,
        )
        if not table_match:
            continue
        table_name = table_match.group(1).strip().upper()
        class_match = re.search(r'public\s+(?:class|record)\s+(\w+)', content)
        entity_name = class_match.group(1) if class_match else file_path.split("/")[-1].replace(".java", "")
        table_to_entities.setdefault(table_name, []).append(entity_name)
    return table_to_entities


def _extract_service_methods(files: Dict[str, str]) -> Set[str]:
    """Extract public method names from service classes (best-effort)."""
    methods: Set[str] = set()
    for file_path, content in files.items():
        if "service" not in file_path.lower() or not content:
            continue
        # Match public method names (excluding constructors)
        for m in re.finditer(r'public\s+(?!class|record|enum|interface)(?:\w+(?:<[^>]+>)?\s+)+(\w+)\s*\(', content):
            methods.add(m.group(1))
    return methods


def _subroutine_to_method_hint(subroutine: str) -> str:
    """Convert SR_FILART -> srFilart or similar camelCase hint for fuzzy match."""
    parts = subroutine.replace("_", " ").split()
    if not parts:
        return subroutine.lower()
    return parts[0].lower() + "".join(p.capitalize() for p in parts[1:])


def validate_migration_output(
    context: dict,
    files: Dict[str, str],
    java_source_root: Optional[Path] = None,
) -> Tuple[bool, List[str], Dict[str, Any]]:
    """
    Validate generated Java against call graph rules.

    Args:
        context: Context package (with callGraph)
        files: Map of file_path -> content (can be empty if java_source_root provided)
        java_source_root: If provided, scan .java files under this path (project src/main/java or standalone output)

    Returns:
        (passed, issues, report)
    """
    issues: List[str] = []
    report: Dict[str, Any] = {
        "entityConsolidation": {"passed": True, "details": []},
        "subroutineCoverage": {"passed": True, "details": []},
        "tablesValidated": 0,
        "entitiesFound": 0,
        "duplicates": [],
        "missingEntities": [],
        "subroutinesMatched": [],
        "subroutinesUnmatched": [],
    }

    all_files = dict(files)
    if java_source_root and java_source_root.is_dir():
        for jf in java_source_root.rglob("*.java"):
            try:
                rel = str(jf.relative_to(java_source_root)).replace("\\", "/")
                if rel not in all_files:
                    all_files[rel] = jf.read_text(encoding="utf-8", errors="ignore")
            except Exception:
                pass

    call_graph = context.get("callGraph") or {}
    table_access = call_graph.get("tableAccess") or {}
    subroutines_invoked = call_graph.get("subroutinesInvoked") or []

    # 1) Entity consolidation: each table -> exactly one entity
    table_to_entities = _extract_entities_from_java(all_files)
    report["entitiesFound"] = sum(len(v) for v in table_to_entities.values())

    for table_name in table_access:
        report["tablesValidated"] += 1
        entities = table_to_entities.get(table_name, [])
        if len(entities) == 0:
            issues.append(f"Missing entity for table {table_name} (used in tableAccess)")
            report["missingEntities"].append(table_name)
            report["entityConsolidation"]["passed"] = False
        elif len(entities) > 1:
            issues.append(f"Duplicate entities for table {table_name}: {entities}. Use one canonical entity.")
            report["duplicates"].append({"table": table_name, "entities": entities})
            report["entityConsolidation"]["passed"] = False
        else:
            report["entityConsolidation"]["details"].append(f"{table_name} -> {entities[0]}")

    # 2) Subroutine coverage (best-effort): subroutinesInvoked should have service methods
    service_methods = _extract_service_methods(all_files)
    for sr in subroutines_invoked:
        hint = _subroutine_to_method_hint(sr)
        matched = any(
            hint in m.lower() or m.lower() in hint or sr.upper() in m.upper()
            for m in service_methods
        )
        if matched:
            report["subroutinesMatched"].append(sr)
        else:
            report["subroutinesUnmatched"].append(sr)
            # Only warn, don't fail - subroutines may be external or in other layers
            if len(service_methods) > 0:
                report["subroutineCoverage"]["passed"] = False
                issues.append(f"Subroutine {sr} may lack service method (hint: {hint})")

    passed = len([i for i in issues if "Missing entity" in i or "Duplicate entities" in i]) == 0
    return passed, issues, report


def main() -> None:
    import argparse
    parser = argparse.ArgumentParser(description="Validate migration output against call graph.")
    parser.add_argument("context_file", type=Path, help="Context JSON (e.g. context_index/HS1210_n404.json)")
    parser.add_argument(
        "output_path",
        type=Path,
        help="Output dir (standalone) or project root (integration mode, e.g. warranty_demo)",
    )
    parser.add_argument(
        "--strict",
        action="store_true",
        help="Fail on subroutine coverage warnings (default: only entity issues fail)",
    )
    args = parser.parse_args()

    context_path = args.context_file
    output_path = args.output_path
    if not context_path.exists():
        print(f"Context file not found: {context_path}", file=sys.stderr)
        sys.exit(2)

    context = _load_context(context_path)

    # Determine scan root: project (src/main/java) or standalone output dir
    scan_root: Optional[Path] = None
    if (output_path / "src/main/java").is_dir():
        scan_root = output_path / "src/main/java"
    elif output_path.is_dir():
        scan_root = output_path

    passed, issues, report = validate_migration_output(context, {}, scan_root)

    if args.strict and not report["subroutineCoverage"]["passed"]:
        passed = False

    print("=== Migration Validation Report ===")
    print(f"Entity consolidation: {'PASS' if report['entityConsolidation']['passed'] else 'FAIL'}")
    print(f"Subroutine coverage:  {'PASS' if report['subroutineCoverage']['passed'] else 'WARN'}")
    print(f"Tables validated:     {report['tablesValidated']}")
    print(f"Entities found:       {report['entitiesFound']}")
    if report["duplicates"]:
        print(f"Duplicates:           {report['duplicates']}")
    if report["missingEntities"]:
        print(f"Missing entities:     {report['missingEntities']}")
    if report["subroutinesUnmatched"]:
        print(f"Subroutines unmatched: {report['subroutinesUnmatched'][:10]}{'...' if len(report['subroutinesUnmatched']) > 10 else ''}")
    if issues:
        print("\nIssues:")
        for i in issues:
            print(f"  - {i}")

    sys.exit(0 if passed else 1)


if __name__ == "__main__":
    main()
