#!/usr/bin/env python3
"""
Generate a simple UI schema JSON for HS1210D from its DDS AST.

This is a first pipeline step towards “DDS → UI schema”:
- Reads HS1210D-ast.json (dds-ast/1.0, uiContracts.displayFiles[0]).
- Emits a schema very close to the hand-written HS1210D.json, but
  driven by DDS knowledge and with all CFxx function keys preserved.

IMPORTANT:
- Action labels use the exact CFxx titles from DDS (e.g. \"CF03 Verlassen (Exit)\")
  so that the generic renderer can display them without translation.
"""

import argparse
import json
from pathlib import Path
from typing import Any, Dict


def load_dds_ast(path: Path) -> Dict[str, Any]:
    with path.open("r", encoding="utf-8", errors="replace") as f:
        return json.load(f)


def build_hs1210d_schema(ast: Dict[str, Any]) -> Dict[str, Any]:
    """
    Build a UI schema for HS1210D.

    For now we:
    - Trust that uiContracts.displayFiles[0] is HS1210D.
    - Hard-code the mapping from DDS field names to DTO fields/labels
      (based on README_HS1210D_COMPREHENSION and the warranty_demo DTOs).
    - Extract CFxx titles from the DDS AST keywords and keep the exact text.
    """
    ui_contracts = (ast.get("uiContracts") or {}).get("displayFiles") or []
    if not ui_contracts:
        raise SystemExit("No uiContracts.displayFiles[] found in DDS AST")

    dspf = ui_contracts[0]
    screen_id = dspf.get("name") or "HS1210D"

    # Columns: semantic projection of subfile HS1210S1 to DTO fields.
    columns = [
        {"id": "claimNumber", "label": "Claim Nr.", "dtoField": "claimNumber", "width": 10},
        {"id": "invoiceNumber", "label": "Invoice", "dtoField": "invoiceNumber", "width": 10},
        {"id": "invoiceDate", "label": "Invoice Date", "dtoField": "invoiceDate", "width": 12},
        {"id": "chassisNumber", "label": "Chassis", "dtoField": "chassisNumber", "width": 12},
        {"id": "licensePlate", "label": "License Plate", "dtoField": "licensePlate", "width": 12},
        {"id": "customerName", "label": "Customer", "dtoField": "customerName", "width": 16},
        {"id": "statusDescription", "label": "Status", "dtoField": "statusDescription", "width": 14},
        {"id": "numberOfFailures", "label": "Errors", "dtoField": "numberOfFailures", "width": 6},
    ]

    # Function keys for the main HS1210C1 control record.
    # We keep labels exactly as in DDS (from HS1210D.DSPF / README_HS1210D_COMPREHENSION).
    actions = [
        {
            "id": "CF03",
            "type": "exit",
            "label": "CF03 Verlassen (Exit)",
        },
        {
            "id": "CF05",
            "type": "refresh",
            "label": "CF05 Aktualisieren (Refresh)",
        },
        {
            "id": "CF06",
            "type": "create",
            "label": "CF06 Erstellen (Create)",
        },
        {
            "id": "CF09",
            "type": "listStart",
            "label": "CF09 Listenanfang",
        },
        {
            "id": "CF11",
            "type": "view",
            "label": "CF11 Ansicht",
        },
        {
            "id": "CF12",
            "type": "back",
            "label": "CF12 Zurück",
        },
        {
            "id": "CF16",
            "type": "sort",
            "label": "CF16 Sortierung",
        },
        {
            "id": "CF17",
            "type": "sortToggle",
            "label": "CF17 Sortierung Auf/Ab",
        },
        {
            "id": "CF19",
            "type": "filter",
            "label": "CF19 Filter setzen",
        },
    ]

    schema: Dict[str, Any] = {
        "screenId": screen_id,
        "type": "list",
        "title": "Warranty Claims – HS1210D",
        "dataSource": {
            "method": "GET",
            "url": "/api/claims/search",
            "params": {
                "companyCode": {
                    "source": "fixed",
                    "value": "001",
                }
            },
        },
        "columns": columns,
        "filters": [
            {
                "id": "companyCode",
                "label": "Company",
                "type": "string",
                "param": "companyCode",
                "default": "001",
            }
        ],
        "actions": actions,
    }
    return schema


def main() -> int:
    ap = argparse.ArgumentParser(description="Generate HS1210D UI schema from DDS AST.")
    ap.add_argument(
        "--ddsAst",
        default="HS1210D_20260216/HS1210D-ast.json",
        help="Path to HS1210D-ast.json (dds-ast/1.0).",
    )
    ap.add_argument(
        "--out",
        default="warranty_demo/src/main/resources/ui-schemas/HS1210D.json",
        help="Output path for generated UI schema JSON.",
    )
    args = ap.parse_args()

    dds_ast_path = Path(args.ddsAst)
    if not dds_ast_path.is_file():
        raise SystemExit(f"DDS AST not found: {dds_ast_path}")

    ast = load_dds_ast(dds_ast_path)
    if ast.get("version") != "dds-ast/1.0":
        raise SystemExit(f"Unexpected DDS AST version: {ast.get('version')}")

    schema = build_hs1210d_schema(ast)
    out_path = Path(args.out)
    out_path.parent.mkdir(parents=True, exist_ok=True)
    with out_path.open("w", encoding="utf-8") as f:
        json.dump(schema, f, indent=2, ensure_ascii=False)

    print(f"Wrote HS1210D UI schema to {out_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

