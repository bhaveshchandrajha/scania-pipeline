#!/usr/bin/env python3
"""
UI Schema Generator – Migration pipeline extension.

Generates ui-schemas/<ScreenId>.json from display file context (displayFiles)
and function key metadata. Aligns with the resilient pipeline: display → backend → DB → UI.

Uses:
- displayFiles from context (DDS AST uiContracts)
- Function keys extracted from narrative (CF03, CF05, CF06, etc.)
- Screen-specific column mappings (DDS field → DTO field)

Output: Declarative schema with action.url, action.method so a generic
Angular component can execute actions without hardcoded switches.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple

# ---------------------------------------------------------------------------
# Screen-specific configuration: DDS field → DTO field mapping
# ---------------------------------------------------------------------------

# HS1210D: Warranty claims list subfile (HS1210S1)
# DDS fields from HS1210D-ast.json recordFormat HS1210S1
HS1210D_COLUMN_MAP: Dict[str, Dict[str, Any]] = {
    "SUB000": {"dtoField": "companyCode", "label": "Company", "width": 6},
    "SUB010": {"dtoField": "invoiceNumber", "label": "Invoice", "width": 10},
    "SUB020": {"dtoField": "formattedInvoiceDate", "label": "Invoice Date", "width": 12},
    "SUB030": {"dtoField": "orderNumber", "label": "Order Nr.", "width": 10},
    "SUB050": {"dtoField": "claimNumber", "label": "Claim Nr.", "width": 10},
    "SUB060": {"dtoField": "chassisNumber", "label": "Chassis", "width": 12},
    "SUB140": {"dtoField": "customerNumber", "label": "Customer Nr.", "width": 10},
    "SUB150": {"dtoField": "customerName", "label": "Customer", "width": 16},
    "SUB170": {"dtoField": "statusDescription", "label": "Status", "width": 6},
    "SUBANZ": {"dtoField": "errorCount", "label": "Errors", "width": 6},
    "SUBDMC": {"dtoField": "demandCode", "label": "DMC", "width": 6},
}

# Default column order for HS1210D (visible columns)
HS1210D_COLUMN_ORDER = [
    "SUB050", "SUB010", "SUB020", "SUB060", "SUB140", "SUB150", "SUB170", "SUBANZ"
]

# Screen-specific data source and action bindings
SCREEN_CONFIG: Dict[str, Dict[str, Any]] = {
    "HS1210D": {
        "type": "list",
        "title": "Warranty Claims – HS1210D",
        "dataSource": {
            "method": "POST",
            "url": "/api/claims/search",
            "params": {
                "companyCode": {"source": "fixed", "value": "001"},
            },
        },
        "columnMap": HS1210D_COLUMN_MAP,
        "columnOrder": HS1210D_COLUMN_ORDER,
        "filters": [
            {"id": "companyCode", "label": "Company", "type": "string", "param": "companyCode", "default": "001"},
        ],
        # Declarative action bindings: type → url/method/navigate
        # Labels: German (English) for bilingual UI
        "actionBindings": {
            "CF03": {"type": "exit", "action": "historyBack", "label": "Verlassen (Exit)"},
            "CF04": {"type": "help", "action": "showOperatorGuidance", "label": "Bedienerführung (Operator Guidance)"},
            "CF05": {"type": "refresh", "reuseDataSource": True, "label": "Aktualisieren (Refresh)"},
            "CF06": {"type": "create", "navigate": "/claims/create", "label": "Erstellen (Create)"},
            "CF09": {"type": "listStart", "action": "scrollToTop", "label": "Listenanfang (List Start)"},
            "CF11": {"type": "view", "navigate": "/claims/{{companyCode}}/{{claimNumber}}", "label": "Ansicht (View)"},
            "CF12": {"type": "back", "action": "historyBack", "label": "Zurück (Back)"},
            "CF15": {
                "type": "apiCall",
                "label": "Freigabe beantragen (Request Release)",
                "url": "/api/claims/{{companyCode}}/{{claimNumber}}/request-release",
                "method": "POST",
                "requiresSelection": True,
            },
            "CF16": {"type": "sort", "action": "openSortDialog", "label": "Sortierung (Sort)"},
            "CF17": {"type": "sortToggle", "action": "toggleSortOrder", "label": "Sortierung Auf/Ab (Sort Asc/Desc)"},
            "CF19": {"type": "filter", "action": "openFilterDialog", "label": "Filter setzen (Set Filter)"},
            "CF20": {"type": "filter", "action": "selectAllOpenInvoices", "label": "Alle offenen Rechnungen auswählen (Select All Open Invoices)"},
        },
        "extraActionBindings": {
            "delete": {
                "type": "delete",
                "label": "Delete Claim",
                "url": "/api/claims/{{companyCode}}/{{claimNumber}}",
                "method": "DELETE",
                "requiresSelection": True,
            },
        },
    },
}

# Fallback action bindings for unknown screens (common RPG function keys)
DEFAULT_ACTION_BINDINGS: Dict[str, Dict[str, Any]] = {
    "CF03": {"type": "exit", "action": "historyBack"},
    "CF05": {"type": "refresh", "reuseDataSource": True},
    "CF12": {"type": "back", "action": "historyBack"},
}


def extract_function_keys_from_narrative(narrative: str) -> List[Tuple[str, str]]:
    """
    Extract function key definitions from narrative text.
    Matches patterns like: CF03(03 'Verlassen'), CA12(12 'Zurück'), CF06(06 'Erstellen')
    Returns list of (key_id, label) e.g. [("CF03", "Verlassen"), ("CF05", "Aktualisieren")].
    """
    # Match CFxx or CAxx followed by (nn 'Label')
    pattern = r"C[FA](\d{2})\((\d{2})\s+'([^']*)'\)"
    seen: set = set()
    result: List[Tuple[str, str]] = []
    for m in re.finditer(pattern, narrative):
        prefix = "CF" if "CF" in m.group(0)[:2] else "CA"
        num = m.group(1)
        key_id = f"{prefix}{num}"
        label = m.group(3).strip()
        if key_id not in seen:
            seen.add(key_id)
            result.append((key_id, label))
    return result


# Regex to match DDS function key patterns: CF03(03 'Verlassen'), CA12(12 'Zurück')
_FK_PATTERN = re.compile(r"C[FA](\d{2})\((\d{2})\s+'([^']*)'\)")


def _parse_function_key_string(s: str) -> Optional[Tuple[str, str]]:
    """Parse a single function key string like CF03(03 'Verlassen') -> ('CF03', 'Verlassen')."""
    if not isinstance(s, str):
        return None
    m = _FK_PATTERN.match(s.strip())
    if not m:
        return None
    prefix = "CF" if s.strip().startswith("CF") else "CA"
    key_id = f"{prefix}{m.group(1)}"
    label = m.group(3).strip()
    return (key_id, label)


def extract_function_keys_from_ui_contracts(ui_contracts: dict) -> List[Tuple[str, str]]:
    """
    Extract function keys from DDS AST uiContracts.
    DDS stores them in record format 'keywords' arrays (e.g. "CF03(03 'Verlassen')")
    or in a 'functionKeys' array. Searches both.
    """
    found: List[Tuple[str, str]] = []

    def search_keys(obj: Any, acc: List[Tuple[str, str]]) -> None:
        if isinstance(obj, dict):
            for k, v in obj.items():
                if k == "functionKeys" and isinstance(v, list):
                    for item in v:
                        parsed = _parse_function_key_string(item) if isinstance(item, str) else None
                        if parsed:
                            acc.append(parsed)
                elif k == "keywords" and isinstance(v, list):
                    for item in v:
                        parsed = _parse_function_key_string(item) if isinstance(item, str) else None
                        if parsed:
                            acc.append(parsed)
                else:
                    search_keys(v, acc)
        elif isinstance(obj, list):
            for x in obj:
                search_keys(x, acc)

    search_keys(ui_contracts, found)
    seen: set = set()
    deduped = []
    for k, lbl in found:
        if k not in seen:
            seen.add(k)
            deduped.append((k, lbl))
    return deduped


def build_columns_from_record_format(
    record_format: dict,
    screen_id: str,
) -> List[dict]:
    """
    Build schema columns from a DDS record format (subfile row).
    Uses screen-specific column map when available; otherwise uses field names.
    """
    config = SCREEN_CONFIG.get(screen_id, {})
    column_map = config.get("columnMap", {})
    column_order = config.get("columnOrder", [])

    fields = record_format.get("fields", [])
    keywords = record_format.get("keywords", []) or []
    # Only include SFL (subfile) record formats for list screens
    if "SFL" not in keywords and "SFLNXTCHG" not in keywords:
        # Use first record format with visible fields if no SFL
        pass

    columns = []
    for f in fields:
        name = f.get("name", "")
        if not name or name.startswith("IN-"):
            continue
        if column_map:
            if name not in column_map:
                continue
            cfg = column_map[name]
            columns.append({
                "id": cfg["dtoField"],
                "label": cfg.get("label", name),
                "dtoField": cfg["dtoField"],
                "width": cfg.get("width", 10),
            })
        else:
            columns.append({
                "id": name,
                "label": name,
                "dtoField": name,
                "width": 10,
            })

    if column_order and column_map:
        by_dto = {c["dtoField"]: c for c in columns}
        ordered = []
        for key in column_order:
            cfg = column_map.get(key, {})
            dto = cfg.get("dtoField")
            if dto and dto in by_dto:
                ordered.append(by_dto[dto])
        for c in columns:
            if c not in ordered:
                ordered.append(c)
        return ordered
    return columns


def build_actions(
    function_keys: List[Tuple[str, str]],
    screen_id: str,
) -> List[dict]:
    """
    Build declarative actions array from function keys and screen config.
    Each action has: id, type, label, and optionally url, method, navigate, action.
    """
    config = SCREEN_CONFIG.get(screen_id, {})
    bindings = config.get("actionBindings", DEFAULT_ACTION_BINDINGS)

    actions = []
    for key_id, label in function_keys:
        binding = bindings.get(key_id, {"type": "custom", "label": label})
        action: Dict[str, Any] = {
            "id": key_id,
            "type": binding.get("type", "custom"),
            "label": f"{key_id} {label}",
        }
        if "url" in binding:
            action["url"] = binding["url"]
        if "method" in binding:
            action["method"] = binding["method"]
        if "navigate" in binding:
            action["navigate"] = binding["navigate"]
        if "action" in binding:
            action["action"] = binding["action"]
        if "reuseDataSource" in binding:
            action["reuseDataSource"] = binding["reuseDataSource"]
        if "requiresSelection" in binding:
            action["requiresSelection"] = binding["requiresSelection"]
        if "label" in binding and binding["label"] != label:
            action["label"] = f"{key_id} {binding['label']}"
        actions.append(action)
    return actions


def generate_ui_schema(
    context: dict,
    screen_id: Optional[str] = None,
    unit_id: Optional[str] = None,
) -> Optional[dict]:
    """
    Generate a UI schema from migration context.

    Args:
        context: Migration context with displayFiles, narrative
        screen_id: Override screen ID (e.g. HS1210D for claims list)
        unit_id: Program unit ID (e.g. HS1210) – used to derive screen_id as unit_id + 'D' if not set

    Returns:
        Schema dict or None if no display files
    """
    display_files = context.get("displayFiles", [])
    narrative = context.get("narrative", "")

    if not display_files:
        return None

    # Resolve screen_id: explicit > unit_id + 'D' > first with uiContracts > first
    sid = screen_id
    if not sid and unit_id:
        sid = f"{unit_id}D"
    df = None
    if sid:
        for d in display_files:
            name = d.get("name") or (d.get("uiContracts") or d).get("name", "")
            if name == sid:
                df = d
                break
    if not df:
        for d in display_files:
            uc = d.get("uiContracts") or d
            if uc.get("recordFormats"):
                df = d
                sid = sid or d.get("name") or uc.get("name", "Unknown")
                break
    if not df:
        df = display_files[0]
        sid = sid or df.get("name") or (df.get("uiContracts") or df).get("name", "Unknown")

    ui_contracts = df.get("uiContracts") or df

    config = SCREEN_CONFIG.get(sid, {})
    schema: Dict[str, Any] = {
        "screenId": sid,
        "type": config.get("type", "list"),
        "title": config.get("title", f"{sid} – List"),
        "dataSource": config.get("dataSource", {
            "method": "POST",
            "url": f"/api/{sid.lower()}/search",
            "params": {},
        }),
        "columns": [],
        "actions": [],
    }

    # Columns from record formats
    record_formats = ui_contracts.get("recordFormats", [])
    for rf in record_formats:
        keywords = rf.get("keywords", []) or []
        if "SFL" in keywords or "SFLNXTCHG" in keywords:
            cols = build_columns_from_record_format(rf, sid)
            if cols:
                schema["columns"] = cols
                break
    if not schema["columns"] and record_formats:
        schema["columns"] = build_columns_from_record_format(record_formats[0], sid)

    # Function keys: prefer DDS AST, then narrative
    fk_ast = extract_function_keys_from_ui_contracts(ui_contracts)
    fk_narr = extract_function_keys_from_narrative(narrative)
    function_keys = fk_ast if fk_ast else fk_narr

    # If still empty, use standard list keys from config
    if not function_keys and sid in SCREEN_CONFIG:
        bindings = SCREEN_CONFIG[sid].get("actionBindings", {})
        default_labels = {
            "CF03": "Verlassen (Exit)", "CF05": "Aktualisieren (Refresh)", "CF06": "Erstellen (Create)",
            "CF09": "Listenanfang", "CF11": "Ansicht", "CF12": "Zurück", "CF16": "Sortierung",
            "CF17": "Sortierung Auf/Ab", "CF19": "Filter setzen",
        }
        function_keys = [(k, default_labels.get(k, k)) for k in bindings]

    schema["actions"] = build_actions(function_keys, sid)

    # Append extra actions (e.g. delete) not present in DDS function keys
    for extra_id, extra_binding in config.get("extraActionBindings", {}).items():
        action: Dict[str, Any] = {
            "id": extra_id,
            "type": extra_binding.get("type", "custom"),
            "label": extra_binding.get("label", extra_id),
        }
        if "url" in extra_binding:
            action["url"] = extra_binding["url"]
        if "method" in extra_binding:
            action["method"] = extra_binding["method"]
        if "requiresSelection" in extra_binding:
            action["requiresSelection"] = extra_binding["requiresSelection"]
        schema["actions"].append(action)

    if config.get("filters"):
        schema["filters"] = config["filters"]

    return schema


def write_ui_schema(schema: dict, output_dir: Path) -> Path:
    """Write schema JSON to ui-schemas/<screenId>.json."""
    screen_id = schema.get("screenId", "Unknown")
    ui_schemas_dir = output_dir / "src" / "main" / "resources" / "ui-schemas"
    ui_schemas_dir.mkdir(parents=True, exist_ok=True)
    out_path = ui_schemas_dir / f"{screen_id}.json"
    with out_path.open("w", encoding="utf-8") as f:
        json.dump(schema, f, indent=2, ensure_ascii=False)
    return out_path


def main() -> None:
    """CLI for standalone UI schema generation (without full migration)."""
    parser = argparse.ArgumentParser(
        description="Generate ui-schemas/<ScreenId>.json from display file context",
    )
    parser.add_argument("context_file", help="Path to context JSON (e.g. context_index/HS1210_n404.json)")
    parser.add_argument("--output-dir", "-o", default="warranty_demo", help="Project root for ui-schemas output")
    parser.add_argument("--screen-id", "-s", help="Override screen ID (e.g. HS1210D)")
    parser.add_argument("--unit-id", "-u", help="Program unit ID to derive screen (e.g. HS1210 -> HS1210D)")
    args = parser.parse_args()

    with open(args.context_file, encoding="utf-8") as f:
        context = json.load(f)
    schema = generate_ui_schema(context, screen_id=args.screen_id, unit_id=args.unit_id)
    if not schema:
        print("No display files in context.", file=sys.stderr)
        sys.exit(1)
    out_path = write_ui_schema(schema, Path(args.output_dir))
    print(f"Wrote {out_path}")


if __name__ == "__main__":
    main()
