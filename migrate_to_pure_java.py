#!/usr/bin/env python3
"""
Track B: Direct Pure Java Migration Pipeline

Generates Pure Java application code with:
- Layered architecture (domain/service/repository/web)
- Domain-driven design (Claim, not HSG71LF2)
- Modern Java features (Records, Streams, Optional, Enums)
- Preserved database mappings (@Column(name="..."))

USAGE:
  export ANTHROPIC_API_KEY=sk-ant-...
  
  python3 migrate_to_pure_java.py context_index/HS1210_n404.json
  -> Generates Pure Java code in layered structure:
     - domain/Claim.java
     - service/ClaimSearchService.java
     - repository/ClaimRepository.java
     - dto/ClaimDto.java
     - web/ClaimController.java (optional)

  python3 migrate_to_pure_java.py context_index/HS1210_n404.json --stream
  -> Same; streams progress to stderr

  python3 migrate_to_pure_java.py context_index/HS1210_n404.json --rpg-file path/to/HS1210.sqlrpgle
  -> Loads full RPG source (lines from context range) so the LLM can generate complete logic (no empty loops or stub returns).

NOTE:
- The ANTHROPIC_API_KEY MUST be provided via environment variable.
- This generates Pure Java (Track B) - for RPG-native code use migrate_with_claude.py (Track A)
"""

import argparse
import json
import os
import re
import sys
import time
from pathlib import Path
from textwrap import dedent
from typing import Any, Dict, List, Optional, Set, Tuple

import anthropic


# Domain glossary: Map RPG file names to domain entity names
# This will be expanded based on your domain knowledge
DOMAIN_GLOSSARY = {
    # Claim-related files
    "HSG71LF2": "Claim",
    "HSG71PF": "Claim",
    "HSG73PF": "ClaimFailure",
    "HSG70F": "ClaimHeader",
    "HSG73LF": "ClaimFailure",
    # Add more mappings as you discover patterns
}

# Value object mappings: RPG concepts → Domain value objects
VALUE_OBJECT_MAPPINGS = {
    "SubfileFilter": "ClaimSearchCriteria",
    "SubfileContext": "ClaimListState",
    "SubfileResult": "ClaimSearchResult",
    "SubfileRecord": "ClaimListItemDto",
}

# Enum mappings: Magic values → Domain enums
ENUM_MAPPINGS = {
    # Status codes
    "99": "ClaimStatus.EXCLUDED",
    "20": "ClaimStatus.APPROVED",
    "11": "ClaimStatus.REJECTED",
    "0": "ClaimStatus.PENDING",
    "5": "ClaimStatus.MINIMUM",
    # Filter options
    '"J"': "FilterOption.OPEN_CLAIMS_ONLY",
    '"N"': "FilterOption.ALL_CLAIMS",
}


def clean_java_code(java_code: str) -> str:
    """Remove markdown code fences and other non-Java content from generated code."""
    code = java_code.strip()
    lines = code.split("\n")
    if lines and lines[0].strip().startswith("```"):
        lines = lines[1:]
    if lines and lines[-1].strip() == "```":
        lines = lines[:-1]
    code = "\n".join(lines).strip()
    if code.endswith("```"):
        code = code[:-3].strip()
    return code


def fix_unbalanced_braces(java_code: str) -> str:
    """Fix unbalanced braces by appending missing closing braces."""
    open_c = java_code.count("{")
    close_c = java_code.count("}")
    if open_c <= close_c:
        return java_code
    missing = open_c - close_c
    stripped = java_code.rstrip()
    return stripped + "\n" + ("}\n" * missing) + java_code[len(stripped):]


def check_code_truncation(java_code: str, max_tokens: int = 64000) -> bool:
    """Check if generated code might be truncated."""
    if not java_code:
        return False
    estimated_tokens = len(java_code) / 4
    last_non_whitespace = java_code.rstrip()[-50:].strip()
    if estimated_tokens > max_tokens * 0.9:
        if not last_non_whitespace.endswith('}') and not last_non_whitespace.endswith(';'):
            return True
    return False


def get_range_from_context(context: dict) -> Optional[tuple]:
    """
    Extract (start_line, end_line) 1-based from context.
    Checks astNode.range and raw.range.
    """
    ast_node = context.get("astNode", {})
    r = ast_node.get("range") or context.get("raw", {}).get("range")
    if not r:
        return None
    start = r.get("startLine") or r.get("startLinePP")
    end = r.get("endLine") or r.get("endLinePP")
    if start is not None and end is not None and 1 <= start <= end:
        return (int(start), int(end))
    return None


def load_rpg_source_from_file(rpg_file_path: Path, start_line: int, end_line: int) -> Optional[str]:
    """
    Load RPG source lines start_line..end_line (1-based) from a file.
    Returns None if file not found or read fails.
    """
    if not rpg_file_path.exists() or not rpg_file_path.is_file():
        return None
    try:
        with open(rpg_file_path, "r", encoding="utf-8", errors="replace") as f:
            lines = f.readlines()
        # 1-based range -> 0-based index
        start_idx = max(0, start_line - 1)
        end_idx = min(len(lines), end_line)
        if start_idx >= end_idx:
            return None
        selected = lines[start_idx:end_idx]
        return "".join(selected)
    except Exception:
        return None


def extract_embedded_sql(rpg_source: str) -> List[Dict[str, Any]]:
    """
    Extract RPG embedded SQL statements (EXEC SQL, DECLARE CURSOR, OPEN, FETCH, CLOSE)
    so they can be explicitly translated into repository @Query methods.

    Returns a list of dicts:
      - type: "declare_cursor" | "open" | "fetch" | "close" | "select" | "insert" | "update" | "delete"
      - cursor_name: str (for cursor-related statements)
      - sql: str (normalized SQL or statement text for prompt)
      - line_ref: int (1-based line number in source, if available)
    """
    if not rpg_source or not rpg_source.strip():
        return []
    entries: List[Dict[str, Any]] = []
    lines = rpg_source.split("\n")
    i = 0
    while i < len(lines):
        line = lines[i]
        # Normalize: strip fixed-format sequence/continuation (columns 1-7) only when columns 1-6 are sequence (blank/digit) and 7 is 'C'
        stripped = line.strip()
        if len(line) > 7 and line[6:7].upper() == "C" and (line[:6].strip() == "" or line[:6].rstrip().isdigit()):
            stripped = line[7:].strip()
        if not stripped.upper().startswith("EXEC SQL") and not stripped.upper().startswith("EXEC  SQL"):
            i += 1
            continue
        line_ref = i + 1  # 1-based line number for reference
        # Collect the full statement (may span lines until ;)
        stmt_lines = [stripped]
        j = i + 1
        while j < len(lines) and ";" not in " ".join(stmt_lines):
            next_line = lines[j]
            next_stripped = next_line.strip()
            if len(next_line) > 7 and next_line[6:7].upper() == "C" and (next_line[:6].strip() == "" or next_line[:6].rstrip().isdigit()):
                next_stripped = next_line[7:].strip()
            stmt_lines.append(next_stripped)
            j += 1
        full = " ".join(stmt_lines).strip()
        i = j
        # Remove EXEC SQL prefix and trailing ;
        sql_part = re.sub(r"^\s*EXEC\s+SQL\s+", "", full, flags=re.IGNORECASE).strip().rstrip(";").strip()

        # Classify and capture
        if re.match(r"DECLARE\s+\w+\s+CURSOR\s+FOR\s+", sql_part, re.IGNORECASE):
            m = re.search(r"DECLARE\s+(\w+)\s+CURSOR\s+FOR\s+(.+)", sql_part, re.IGNORECASE | re.DOTALL)
            if m:
                cursor_sql = m.group(2).strip()
                # Ensure SELECT is preserved (multiline join can lose it; recover from full)
                if not cursor_sql.upper().startswith("SELECT"):
                    for src in (sql_part, full):
                        idx = (src.upper().find("SELECT") if src else -1)
                        if idx >= 0:
                            cursor_sql = src[idx:]
                            if src is full:
                                cursor_sql = re.sub(r"^\s*EXEC\s+SQL\s+", "", cursor_sql, flags=re.IGNORECASE).strip().rstrip(";").strip()
                            break
                entries.append({
                    "type": "declare_cursor",
                    "cursor_name": m.group(1),
                    "sql": cursor_sql,
                    "line_ref": line_ref,
                })
        elif re.match(r"OPEN\s+\w+\s*;?", sql_part, re.IGNORECASE):
            m = re.search(r"OPEN\s+(\w+)", sql_part, re.IGNORECASE)
            if m:
                entries.append({
                    "type": "open",
                    "cursor_name": m.group(1),
                    "sql": sql_part,
                    "line_ref": line_ref,
                })
        elif re.match(r"FETCH\s+\w+\s+INTO\s+", sql_part, re.IGNORECASE):
            m = re.search(r"FETCH\s+(\w+)\s+INTO\s+(.+)", sql_part, re.IGNORECASE | re.DOTALL)
            if m:
                entries.append({
                    "type": "fetch",
                    "cursor_name": m.group(1),
                    "sql": "FETCH " + m.group(1) + " INTO " + m.group(2).strip(),
                    "line_ref": line_ref,
                })
        elif re.match(r"CLOSE\s+\w+\s*;?", sql_part, re.IGNORECASE):
            m = re.search(r"CLOSE\s+(\w+)", sql_part, re.IGNORECASE)
            if m:
                entries.append({
                    "type": "close",
                    "cursor_name": m.group(1),
                    "sql": sql_part,
                    "line_ref": line_ref,
                })
        elif re.match(r"SELECT\s+", sql_part, re.IGNORECASE):
            entries.append({"type": "select", "sql": sql_part, "line_ref": line_ref})
        elif re.match(r"INSERT\s+INTO\s+", sql_part, re.IGNORECASE):
            entries.append({"type": "insert", "sql": sql_part, "line_ref": line_ref})
        elif re.match(r"UPDATE\s+\w+\s+SET\s+", sql_part, re.IGNORECASE):
            entries.append({"type": "update", "sql": sql_part, "line_ref": line_ref})
        elif re.match(r"DELETE\s+FROM\s+", sql_part, re.IGNORECASE):
            entries.append({"type": "delete", "sql": sql_part, "line_ref": line_ref})
        else:
            # Any other EXEC SQL (e.g. SET, CALL)
            entries.append({"type": "other", "sql": sql_part, "line_ref": line_ref})
    return entries


def _format_embedded_sql_for_prompt(entries: List[Dict[str, Any]]) -> str:
    """Format extracted embedded SQL as a numbered checklist for the migration prompt."""
    if not entries:
        return ""
    lines_out = [
        "The following RPG embedded SQL statements were detected. You MUST translate every one into",
        "a corresponding repository method (Spring Data JPA @Query or method name). Do not skip any.",
        "",
    ]
    cursor_to_select: Dict[str, str] = {}
    for e in entries:
        if e.get("type") == "declare_cursor" and e.get("sql"):
            cursor_to_select[e.get("cursor_name", "")] = e.get("sql", "")
    for idx, e in enumerate(entries, 1):
        stype = e.get("type", "other")
        sql = e.get("sql", "")
        cname = e.get("cursor_name", "")
        if stype == "declare_cursor":
            lines_out.append(f"{idx}. DECLARE CURSOR ({cname}) → repository method returning List/Stream with @Query:")
            lines_out.append(f"   SQL: {sql[:500]}{'...' if len(sql) > 500 else ''}")
        elif stype == "fetch":
            select_sql = cursor_to_select.get(cname, "")
            if select_sql:
                lines_out.append(f"{idx}. FETCH {cname} (cursor) → same as cursor's SELECT above: {select_sql[:200]}...")
            else:
                lines_out.append(f"{idx}. FETCH {cname} INTO ... → repository method for this cursor's result set")
        elif stype in ("open", "close"):
            lines_out.append(f"{idx}. {stype.upper()} {cname} → no direct Java equivalent (use repository method that runs the SELECT)")
        elif stype == "select":
            lines_out.append(f"{idx}. Single-row SELECT → repository method with @Query returning Optional<Entity> or single value:")
            lines_out.append(f"   {sql[:400]}{'...' if len(sql) > 400 else ''}")
        elif stype in ("insert", "update", "delete"):
            lines_out.append(f"{idx}. {stype.upper()} → repository.save(entity) or @Modifying @Query for bulk:")
            lines_out.append(f"   {sql[:400]}{'...' if len(sql) > 400 else ''}")
        else:
            lines_out.append(f"{idx}. EXEC SQL ({stype}): {sql[:300]}{'...' if len(sql) > 300 else ''}")
        lines_out.append("")
    return "\n".join(lines_out)


def extract_domain_entities(db_contracts: List[Dict]) -> List[Dict]:
    """
    Extract domain entities from dbContracts.
    
    Returns list of entity info:
    {
        "domain_name": "Claim",  # Domain name (from glossary or inferred)
        "table_name": "HSG71LF2",  # Original table name
        "columns": [...],  # Column definitions
    }
    """
    entities = []
    for contract in db_contracts:
        table_name = (contract.get("fileName") or contract.get("name") or "Unknown").strip()
        # Map to domain name using glossary
        domain_name = DOMAIN_GLOSSARY.get(table_name)
        if not domain_name:
            # Infer domain name: remove prefixes, use meaningful part
            # HSG71LF2 -> Claim (if HSG71 is claim-related)
            # This is a simple heuristic - improve with domain knowledge
            if "CLAIM" in table_name.upper() or "HSG71" in table_name:
                domain_name = "Claim"
            elif "FAILURE" in table_name.upper() or "HSG73" in table_name:
                domain_name = "ClaimFailure"
            else:
                # Fallback: use table name as-is but camelCase
                domain_name = table_name.replace("_", "").replace("-", "")
        
        entities.append({
            "domain_name": domain_name,
            "table_name": table_name,
            "columns": contract.get("columns", []),
            "contract": contract,
        })
    return entities


def extract_value_objects(context: dict) -> List[Dict]:
    """
    Extract value objects from RPG variables/data structures.
    
    Returns list of value object info:
    {
        "rpg_name": "SubfileFilter",
        "domain_name": "ClaimSearchCriteria",
        "fields": [...],  # Inferred from symbol metadata
    }
    """
    value_objects = []
    symbol_metadata = context.get("symbolMetadata", {})
    
    # Look for common patterns in symbol metadata
    # This is a heuristic - improve with better analysis
    for sym_id, sym_info in symbol_metadata.items():
        if "Subfile" in sym_id or "Filter" in sym_id or "Context" in sym_id:
            rpg_name = sym_id.split(".")[-1] if "." in sym_id else sym_id
            domain_name = VALUE_OBJECT_MAPPINGS.get(rpg_name)
            if not domain_name:
                # Infer: SubfileFilter -> ClaimSearchCriteria
                if "Filter" in rpg_name:
                    domain_name = "ClaimSearchCriteria"
                elif "Context" in rpg_name:
                    domain_name = "ClaimListState"
                elif "Result" in rpg_name:
                    domain_name = "ClaimSearchResult"
                else:
                    domain_name = rpg_name.replace("Subfile", "Claim")
            
            value_objects.append({
                "rpg_name": rpg_name,
                "domain_name": domain_name,
                "symbol_id": sym_id,
                "info": sym_info,
            })
    
    return value_objects


def extract_enums(rpg_snippet: str, context: dict) -> List[Dict]:
    """
    Extract enums from magic strings/numbers in RPG code.
    
    Returns list of enum info:
    {
        "name": "ClaimStatus",
        "values": [{"code": 99, "name": "EXCLUDED"}, ...],
    }
    """
    enums = []
    
    # Look for status codes
    status_pattern = r'statusCode\s*[=<>!]+\s*(\d+)'
    status_codes = set(re.findall(status_pattern, rpg_snippet, re.IGNORECASE))
    
    if status_codes:
        enum_values = []
        for code in sorted(status_codes, key=int):
            enum_name = ENUM_MAPPINGS.get(code, f"STATUS_{code}")
            if "." in enum_name:
                enum_name = enum_name.split(".")[1]
            enum_values.append({"code": int(code), "name": enum_name})
        
        enums.append({
            "name": "ClaimStatus",
            "values": enum_values,
        })
    
    # Look for filter options (magic strings)
    filter_pattern = r'["\']([JN])["\']'
    filter_options = set(re.findall(filter_pattern, rpg_snippet))
    
    if filter_options:
        enum_values = []
        for opt in sorted(filter_options):
            enum_name = ENUM_MAPPINGS.get(f'"{opt}"', f"OPTION_{opt}")
            if "." in enum_name:
                enum_name = enum_name.split(".")[1]
            enum_values.append({"code": opt, "name": enum_name})
        
        enums.append({
            "name": "FilterOption",
            "values": enum_values,
        })
    
    return enums


def generate_architecture_guidance(entities: List[Dict], value_objects: List[Dict], enums: List[Dict]) -> str:
    """Generate architecture requirements section for prompt."""
    
    entity_names = [e["domain_name"] for e in entities]
    value_object_names = [vo["domain_name"] for vo in value_objects]
    enum_names = [e["name"] for e in enums]
    
    # Determine primary entity (first one, or most common)
    primary_entity = entity_names[0] if entity_names else "Entity"
    table_name = entities[0]["table_name"] if entities else "HSG71LF2"
    
    # Build code examples with escaped braces for f-string
    record_example = """public record ClaimDto(
           String claimNumber,
           LocalDate claimDate,
           ClaimStatus status
       ) {}"""
    
    stream_example = """List<ClaimDto> claims = claimRepository.findAll()
           .stream()
           .filter(c -> c.getStatus() == ClaimStatus.PENDING)
           .map(this::toDto)
           .collect(Collectors.toList());"""
    
    optional_example = """Optional<Claim> claim = claimRepository.findById(id);
       return claim.map(this::toDto)
           .orElseThrow(() -> new ClaimNotFoundException(id));"""
    
    di_example = """@Service
       public class ClaimSearchService {
           private final ClaimRepository claimRepository;
           
           @Autowired
           public ClaimSearchService(ClaimRepository claimRepository) {
               this.claimRepository = claimRepository;
           }
       }"""
    
    return dedent(f"""
    ## Target Architecture - Pure Java Application
    
    Generate Java code following this **layered architecture**:
    
    ### Package Structure
    ```
    com.scania.warranty/
    ├── domain/
    │   ├── {primary_entity}.java                    (Entity - @Entity, @Table)
    │   ├── {primary_entity}Status.java              (Enum - if status codes found)
    │   └── {primary_entity}SearchCriteria.java      (Value Object - if filters found)
    ├── repository/
    │   └── {primary_entity}Repository.java          (Spring Data JPA - extends JpaRepository)
    ├── service/
    │   └── {primary_entity}SearchService.java       (Business Logic - stateless)
    ├── dto/
    │   ├── {primary_entity}Dto.java                 (Response DTO - Java Record)
    │   └── {primary_entity}ListItemDto.java         (List Item DTO - Java Record)
    └── web/
        └── {primary_entity}Controller.java           (REST API - optional, @RestController)
    ```
    
    ### Domain-Driven Design Requirements
    
    1. **Entity Naming:**
       - Use domain names: **{primary_entity}** (not {table_name})
       - Keep table name in @Table: `@Table(name="{table_name}")`
       - Use camelCase for Java fields: `claimNumber` (not RECHNR)
       - Preserve DB names in @Column: `@Column(name="RECHNR")`
    
    2. **Value Objects:**
       - Extract value objects: {", ".join(value_object_names[:3]) if value_object_names else "ClaimSearchCriteria"}
       - Use Java Records: `public record ClaimSearchCriteria(...)`
       - Immutable by default
    
    3. **Enums:**
       - Replace magic numbers/strings with enums: {", ".join(enum_names) if enums else "ClaimStatus, FilterOption"}
       - Use enum values: `ClaimStatus.EXCLUDED` (not `99`)
       - Define enums in domain package
    
    ### Modern Java Requirements (Java 17+)
    
    1. **Use Java Records for DTOs:**
       ```java
       {record_example}
       ```
    
    2. **Use Streams for Data Processing:**
       ```java
       {stream_example}
       ```
    
    3. **Use Optional Properly:**
       ```java
       {optional_example}
       ```
    
    4. **Use Dependency Injection:**
       ```java
       {di_example}
       ```
    
    5. **Stateless Services:**
       - Services should be stateless (no instance variables for business state)
       - Pass state as parameters (request-scoped contexts if needed)
       - Use dependency injection for repositories
    
    ### Database Mapping (CRITICAL - PRESERVE)
    
    ⚠️ **MANDATORY:** Preserve all database mappings exactly:
    
    - Keep `@Table(name="{table_name}")` unchanged
    - Keep `@Column(name="EXACT_DB_NAME")` with exact DB column names
    - Use camelCase for Java field names, but preserve DB names in @Column
    - Example: `@Column(name="RECHNR") private String claimNumber;`
    
    ### Layer Responsibilities
    
    - **domain/**: Entities (@Entity), Value Objects (Records), Enums
    - **repository/**: Spring Data JPA interfaces (extends JpaRepository<Entity, ID>)
    - **service/**: Business logic (stateless, uses repositories)
    - **dto/**: Request/response objects (Java Records)
    - **web/**: REST controllers (HTTP handling, calls services)
    """)


def build_pure_java_prompt(
    context: dict,
    rpg_source_override: Optional[str] = None,
    rpg_range: Optional[tuple] = None,
) -> str:
    """
    Build enhanced prompt for Pure Java migration (Track B).
    
    Includes:
    - Target architecture requirements
    - Domain-driven design patterns
    - Modern Java features
    - Database mapping preservation
    - Mandatory complete logic (no stubs, no empty bodies)
    
    If rpg_source_override is provided (e.g. from --rpg-file), it is used as the
    full RPG source; otherwise context["rpgSnippet"] is used (and may be truncated).
    """
    ast_node = context.get("astNode", {})
    narrative = context.get("narrative", "")
    rpg_snippet = context.get("rpgSnippet", "")
    db_contracts = context.get("dbContracts", [])
    symbol_metadata = context.get("symbolMetadata", {})
    display_files = context.get("displayFiles", [])
    
    node_id = ast_node.get("id")
    kind = ast_node.get("kind")
    
    # Use full RPG source when provided (--rpg-file); otherwise use context snippet
    if rpg_source_override and rpg_source_override.strip():
        rpg_source = rpg_source_override.strip()
        rpg_source_label = "COMPLETE RPG source"
        if rpg_range:
            rpg_source_label += f" (lines {rpg_range[0]}-{rpg_range[1]})"
        rpg_source_label += " – use this to derive ALL control flow and business logic."
    else:
        rpg_source = rpg_snippet or "(no RPG snippet in context)"
        # Detect likely truncation (e.g. "... omitted" or very short for large range)
        range_tuple = rpg_range or get_range_from_context(context)
        snippet_truncated = (
            "... omitted" in rpg_source
            or (range_tuple and (range_tuple[1] - range_tuple[0] > 500) and len(rpg_source) < 3000)
        )
        if snippet_truncated or not rpg_snippet.strip():
            rpg_source_label = (
                "RPG snippet (TRUNCATED or missing). You MUST still generate COMPLETE logic: "
                "infer from the narrative and symbol names. For copy/map operations, generate full "
                "field mapping. For type/scope checks, use entity fields (e.g. hauptgruppe, claimArt, nebengruppe)."
            )
        else:
            rpg_source_label = "RPG source (primary source of control flow)."
    
    # Extract embedded SQL from RPG source so the LLM must translate each to a repository method
    embedded_sql_entries = extract_embedded_sql(rpg_source)
    embedded_sql_section = ""
    if embedded_sql_entries:
        embedded_sql_section = (
            "\n\n        ## ⚠️ EMBEDDED SQL TO TRANSLATE (MANDATORY)\n"
            "        The following RPG embedded SQL statements were extracted from the source. "
            "You MUST add a corresponding repository method for each (e.g. @Query or Spring Data method name). "
            "Cursors (DECLARE/OPEN/FETCH/CLOSE) map to one repository method that returns the cursor's result set (List or Stream).\n\n"
            "        --- EMBEDDED SQL CHECKLIST START ---\n"
            + _format_embedded_sql_for_prompt(embedded_sql_entries) + "\n"
            "        --- EMBEDDED SQL CHECKLIST END ---\n"
        )
    
    # Extract domain concepts (use effective RPG source for enum extraction)
    entities = extract_domain_entities(db_contracts)
    value_objects = extract_value_objects(context)
    enums = extract_enums(rpg_source, context)
    
    # Generate architecture guidance
    architecture_guidance = generate_architecture_guidance(entities, value_objects, enums)
    
    db_json = json.dumps(db_contracts, indent=2, ensure_ascii=False)
    symbols_json = json.dumps(symbol_metadata, indent=2, ensure_ascii=False)
    
    # Build column checklist
    column_checklist_lines = []
    total_columns = 0
    for c in db_contracts:
        name = (c.get("fileName") or c.get("name") or "?").strip()
        cols = c.get("columns") or []
        if cols:
            col_names = [str(col.get("name", "")).strip() for col in cols if col.get("name")]
            total_columns += len(col_names)
            column_checklist_lines.append(f"- {name}: " + ", ".join(col_names))
    column_checklist = "\n".join(column_checklist_lines) if column_checklist_lines else "(no columns in contracts)"
    
    # Display files section (DDS source and/or uiContracts from DDS AST for advanced UI context)
    display_files_section = ""
    if display_files:
        display_lines = []
        for df in display_files:
            name = df.get("name") or df.get("symbolId") or "?"
            file_id = df.get("fileId") or ""
            display_lines.append(f"- **{name}** (symbolId: {df.get('symbolId', '')}, fileId: {file_id})")
            if df.get("ddsSource"):
                display_lines.append("  DDS source (screen layout):")
                display_lines.append("  ```dds")
                display_lines.append(df.get("ddsSource", "")[:8000])
                if len(df.get("ddsSource", "")) > 8000:
                    display_lines.append("  ... (truncated)")
                display_lines.append("  ```")
            # Include structured uiContracts (recordFormats, fields) from DDS AST when present
            ui_contracts = df.get("uiContracts")
            if ui_contracts:
                display_lines.append("  UI contracts (record formats and fields from DDS AST – use for DTOs, form fields, validation):")
                display_lines.append("  ```json")
                uc_json = json.dumps(ui_contracts, indent=2, ensure_ascii=False)
                if len(uc_json) > 12000:
                    uc_json = uc_json[:12000] + "\n  ... (truncated)"
                display_lines.append(uc_json)
                display_lines.append("  ```")
        display_files_section = (
            "## Display files (DSPF) – for UI building\n"
            "The following display files are used by this unit. Use them to inform UI-related code "
            "(e.g. screen DTOs, form fields, record formats, or comments describing screen layout). "
            "When uiContracts (recordFormats/fields) are present, use them for precise field names and structure.\n\n"
            + "\n".join(display_lines)
            + "\n\n"
        )
    
    column_checklist_blurb = (
        f"⚠️ CRITICAL REQUIREMENT - 100% COLUMN MAPPING IS MANDATORY ⚠️\n\n"
        f"The following lists EVERY SINGLE COLUMN that MUST appear in your Java code. "
        f"Your Java code MUST include EVERY ONE as a field with @Column annotation. "
        f"Use @Column(name=\"EXACT_NAME\") with the exact contract column name if the Java field name differs. "
        f"DO NOT skip, omit, summarize, or abbreviate any columns.\n\n"
        f"TOTAL COLUMNS TO MAP: {total_columns}\n"
        f"YOUR CODE MUST HAVE EXACTLY {total_columns} @Column ANNOTATIONS.\n"
        f"This will be validated - incomplete mapping will fail validation."
    )
    
    return dedent(
        f"""
        You are an expert IBM i (AS/400) RPG and Java architect specializing in **Pure Java application architecture**.
        Your task is to migrate a single RPG subroutine/procedure to **Pure Java** following modern Java/Spring best practices,
        with **zero hallucinations** on data structures and **layered architecture**.
        
        {architecture_guidance}
        
        ## Unit to migrate
        - AST node id: {node_id}
        - Kind: {kind}
        
        ## Business / semantic narrative (primary source of intent)
        The following Markdown narrative describes what the RPG logic does
        and which files/variables it uses:
        
        --- NARRATIVE START ---
        {narrative}
        --- NARRATIVE END ---
        
        ## RPG source
        {rpg_source_label}
        
        --- RPG SOURCE START ---
        {rpg_source}
        --- RPG SOURCE END ---
        {embedded_sql_section}
        ## Database contracts (single source of truth for schema)
        The following JSON describes IBM i physical/logical files and columns
        used by this unit. All Java JPA entities, SQL, and field types MUST
        match these definitions exactly (names, lengths, precisions, scales).
        Do NOT invent new columns or change existing ones.
        
        --- DB CONTRACTS START (JSON) ---
        {db_json}
        --- DB CONTRACTS END ---
        
        ## ⚠️ MANDATORY COLUMN CHECKLIST - 100% MAPPING REQUIRED ⚠️
        {column_checklist_blurb}
        
        {column_checklist}
        
        **FINAL REMINDER**: Before finishing your code, count all @Column annotations. The count MUST equal {total_columns}. If it doesn't, add the missing @Column fields!
        
        {display_files_section}
        ## Symbol metadata
        Additional symbols (variables, data structures, k-lists, etc.) referenced
        by this unit:
        
        --- SYMBOL METADATA START (JSON) ---
        {symbols_json}
        --- SYMBOL METADATA END ---
        
        ## ⚠️ LOGIC COMPLETENESS – ENFORCED (validation will fail otherwise)
        Your generated code will be **automatically validated** for logic completeness. If any of the following are present, the build will **fail validation** and the code will be rejected:
        - **Empty loops**: Any `for (...)` or `while (...)` with an empty body `{{ }}` → FAIL. Every loop MUST contain real statements (e.g. map, repository.save, field copy).
        - **Stub-only methods**: Any method whose body is only `return true;`, `return false;`, or `return "X";` (or similar constant) → FAIL. Every method MUST implement real logic using entity fields, parameters, or repository results.
        - **Missing entities**: Every table in the context dbContracts MUST have a matching `@Entity` with `@Table(name="...")` in domain/.
        **Before you finish**, self-check: (1) No for/while has empty body. (2) No method returns only a constant. (3) Every context table has an entity. Generate **full implementation** so that logic completeness validation scores 100%.
        
        ## Requirements for your answer
        
        1. **Target**: Produce **Pure Java** code with **layered architecture**:
           - Generate **multiple files** organized by package (domain/service/repository/dto/web)
           - Use **domain names** (Claim, not HSG71LF2) for entities
           - Use **enums** (ClaimStatus, not magic numbers) for constants
           - Use **Java Records** for DTOs
           - Use **modern Java features** (Streams, Optional, dependency injection)
        
        2. **Architecture**:
           - **domain/**: Entities (@Entity), Value Objects (Records), Enums
           - **repository/**: Spring Data JPA interfaces (extends JpaRepository)
           - **service/**: Stateless business logic services
           - **dto/**: Request/response DTOs (Java Records)
           - **web/**: REST controllers (optional, @RestController)
        
        2a. **SQL Query Translation (CRITICAL)**:
           - **Translate ALL RPG embedded SQL statements** (EXEC SQL SELECT, FETCH, etc.) into repository methods
           - When the prompt includes an **"EMBEDDED SQL TO TRANSLATE"** checklist, every listed statement MUST have a corresponding repository method (@Query or Spring Data method). Do not skip any.
           - For each RPG SQL statement, create a corresponding repository method with @Query annotation
           - Use **JPQL** (Java Persistence Query Language) when possible: `@Query("SELECT c FROM Claim c WHERE c.companyCode = :code")`
           - Use **native SQL** (`nativeQuery = true`) only when JPQL cannot express the query (e.g., complex IBM i SQL functions, specific DB2 syntax)
           - Example RPG SQL: `EXEC SQL SELECT * FROM HSG71LF2 WHERE PAKZ = :pakz AND STATUSCODESDE <> 99`
             → Java: `@Query("SELECT c FROM Claim c WHERE c.companyCode = :pakz AND c.statusCodeSde <> 99") List<Claim> findActiveClaimsByCompanyCode(@Param("pakz") String pakz);`
           - **Map RPG cursors** (DECLARE CURSOR, OPEN, FETCH, CLOSE) to one repository method that runs the cursor's SELECT and returns List<Entity> or Stream<Entity>
           - **Preserve WHERE clauses**: Translate RPG WHERE conditions to JPQL WHERE clauses
           - **Preserve JOINs**: Translate RPG JOINs to JPQL JOINs (e.g., `FROM Claim c JOIN c.errors e`)
           - **Preserve ORDER BY**: Translate RPG ORDER BY to JPQL ORDER BY (or use Spring Data method naming: `findByXxxOrderByYyyAsc`)
           - **Preserve aggregate functions**: Translate COUNT, MAX, MIN, SUM, AVG to JPQL equivalents
           - If RPG uses complex SQL that cannot be expressed in JPQL, use native SQL: `@Query(value = "SELECT * FROM HSG71LF2 WHERE ...", nativeQuery = true)`
           - **Every RPG SQL SELECT/FETCH should have a corresponding repository method** - do not skip SQL statements
        
        3. **Data structures** (CRITICAL – 100% column mapping is MANDATORY):
           - Create **@Entity** classes in `domain/` package for every file in `dbContracts`
           - Use **domain names** for entity classes (Claim, not HSG71LF2)
           - Keep **@Table(name="HSG71LF2")** with original table name
           - Each entity MUST include **EVERY SINGLE COLUMN** from that contract's "columns" array
             as a field with @Column annotation
           - Use **camelCase** for Java field names, but preserve DB names in @Column(name="...")
           - Example: `@Column(name="RECHNR") private String claimNumber;`
        
        4. **Domain-driven design**:
           - Extract **value objects** from RPG data structures (ClaimSearchCriteria, not SubfileFilter)
           - Create **enums** for magic values (ClaimStatus.EXCLUDED, not 99)
           - Use **domain language** in method names (searchClaims, not buildClaimSubfile)
        
        5. **Modern Java**:
           - Use **Java Records** for DTOs: `public record ClaimDto(...)`
           - Use **Streams** for data processing: `claims.stream().filter(...).map(...).collect(...)`
           - Use **Optional** properly: `Optional.ofNullable(...).orElseThrow(...)`
           - Use **dependency injection**: Constructor injection with @Autowired
           - Services should be **stateless** (no instance variables for business state)
        
        6. **Output format**:
           - Generate **multiple Java files** (one per class/interface)
           - Start each file with package declaration: `package com.scania.warranty.domain;`
           - Include all necessary imports
           - Separate files clearly with comments: `// === domain/Claim.java ===`
           - Use clear file separators: `// ==========================================`
        
        7. **Syntax**: Your output must be valid, compilable Java. Every opening brace {{ must
           have a matching closing brace }}. Before finishing, verify: count of {{ equals count of }}.
        
        8. **Display files (when present)**: If the context includes a "Display files (DSPF)" section,
           use it to inform UI-related code: add comments or DTOs that reflect screen/form structure,
           or document which service methods correspond to which display operations (EXFMT/READ), so the
           target application can support UI building.
        
        9. **Complete logic (MANDATORY – no stubs or placeholders)**:
           - Do NOT generate empty loop bodies (e.g. `for (Item i : list) {{ }}`). Every for/while loop MUST contain real logic: map fields, create entities, call repository save, or other operations derived from the RPG or narrative.
           - Do NOT generate methods that only return a constant (e.g. `return true;` or `return "G";`). Every method MUST implement real logic: compare entity fields (e.g. hauptgruppe, claimArt, nebengruppe, steuerCode), derive value from parameters, or perform a real check.
           - Copy operations: When the RPG or narrative describes copying data (e.g. from invoice to claim, work positions to claim details, external services to claim), generate FULL Java: iterate source list, map each source entity's fields to the target entity or a new entity, and persist (e.g. claimErrorRepository.save(...)).
           - Type/scope checks: When filtering by claim type or scope, use the actual entity fields that correspond to the RPG symbols (e.g. hauptgruppe, nebengruppe, claimArt, steuerCode) and return the result of a real comparison, not a constant.
           - If the RPG source is truncated, infer from the narrative and dbContracts: e.g. "copy work positions to claim" means create claim detail/error records from each work position row; "determine scope" means return a field from the error/claim entity (e.g. hauptgruppe or a derived value), not a literal.
        
        **IMPORTANT FOR LARGE CONTEXTS**: If this context has many dbContracts ({len(db_contracts)} contracts, {total_columns} total columns), 
        you MUST generate COMPLETE entities for ALL contracts, even if the code is very long. Do NOT truncate or skip entities.
        Prioritize completeness: generate all {len(db_contracts)} entities with all {total_columns} columns, even if it means a very long output.
        
        **CRITICAL OUTPUT FORMAT**: Generate RAW Java code (NO markdown code blocks, NO ```java tags).
        Separate each file with clear markers. Format:
        
        // === domain/Claim.java ===
        package com.scania.warranty.domain;
        [full entity code - raw Java, no markdown]
        
        // ==========================================
        // === domain/ClaimStatus.java ===
        package com.scania.warranty.domain;
        [full enum code - raw Java, no markdown]
        
        // ==========================================
        // === repository/ClaimRepository.java ===
        package com.scania.warranty.repository;
        // IMPORTANT: Translate RPG SQL statements to @Query methods
        // Example: RPG "EXEC SQL SELECT * FROM HSG71LF2 WHERE PAKZ = :pakz AND STATUSCODESDE <> 99"
        // → @Query("SELECT c FROM Claim c WHERE c.companyCode = :pakz AND c.statusCodeSde <> 99")
        [full repository code with @Query annotations for all RPG SQL statements - raw Java, no markdown]
        
        // ==========================================
        // === service/ClaimSearchService.java ===
        package com.scania.warranty.service;
        [full service code - raw Java, no markdown]
        
        // ==========================================
        // === dto/ClaimDto.java ===
        package com.scania.warranty.dto;
        [full DTO code - raw Java, no markdown]
        
        IMPORTANT: Output RAW Java code only. Do NOT wrap in markdown code blocks (```java).
        Each file marker must be: // === path/to/File.java ===
        Generate ALL files needed for a complete Pure Java application.
        """
    ).strip()


def parse_multi_file_output(java_code: str, output_dir: Path) -> Dict[str, str]:
    """
    Parse multi-file Java output and return map of file_path -> content.
    
    Handles two formats:
    1. Markdown with code blocks:
       ### === domain/Claim.java ===
       ```java
       package com.scania.warranty.domain;
       [code]
       ```
    
    2. Raw Java with markers:
       // === domain/Claim.java ===
       package com.scania.warranty.domain;
       [code]
    """
    files = {}
    current_file = None
    current_content = []
    in_code_block = False
    
    lines = java_code.split("\n")
    i = 0
    
    while i < len(lines):
        line = lines[i]
        
        # Check for file marker FIRST (before code block handling)
        marker_patterns = [
            r'###\s*===\s*(.+?\.java)\s*===',  # Markdown: ### === domain/Claim.java ===
            r'##\s*===\s*(.+?\.java)\s*===',   # Markdown: ## === domain/Claim.java ===
            r'//\s*===\s*(.+?\.java)\s*===',   # Comment: // === domain/Claim.java ===
        ]
        
        file_found = False
        for pattern in marker_patterns:
            marker_match = re.search(pattern, line)
            if marker_match:
                # Save previous file if exists
                if current_file and current_content:
                    files[current_file] = "\n".join(current_content)
                
                current_file = marker_match.group(1)
                current_content = []
                in_code_block = False  # Reset code block state
                file_found = True
                break
        
        if file_found:
            i += 1
            continue
        
        # Check for markdown code block start/end
        if line.strip().startswith("```"):
            if in_code_block:
                # End of code block - don't save yet, wait for next file marker
                in_code_block = False
            else:
                # Start of code block
                in_code_block = True
            i += 1
            continue
        
        # If we're in a code block or have a current file, collect content
        if in_code_block or current_file:
            # Skip markdown headers and separators (but only if not in code block)
            if not in_code_block:
                if line.strip().startswith("#") or line.strip().startswith("---"):
                    i += 1
                    continue
                
                # Skip comment separators like "// =========================================="
                if re.match(r'^\s*//\s*=+\s*$', line):
                    i += 1
                    continue
            
            # Collect actual Java code
            current_content.append(line)
        
        i += 1
    
    # Save last file
    if current_file and current_content:
        files[current_file] = "\n".join(current_content)
    
    # If no file markers found, try to extract from markdown code blocks
    if not files and java_code.strip():
        # Try to find code blocks and extract Java
        code_block_pattern = r'```(?:java)?\n(.*?)```'
        matches = re.finditer(code_block_pattern, java_code, re.DOTALL)
        
        for match in matches:
            code_content = match.group(1).strip()
            if not code_content:
                continue
            
            # Try to infer file name from package/class
            package_match = re.search(r'package\s+([\w.]+);', code_content)
            class_match = re.search(r'public\s+(?:class|record|enum|interface)\s+(\w+)', code_content)
            
            if package_match and class_match:
                package = package_match.group(1)
                class_name = class_match.group(1)
                file_path = f"{package.replace('.', '/')}/{class_name}.java"
                files[file_path] = code_content
            elif class_match:
                # No package, use class name
                class_name = class_match.group(1)
                files[f"{class_name}.java"] = code_content
    
    # Clean up extracted content (remove leading/trailing empty lines)
    cleaned_files = {}
    for file_path, content in files.items():
        cleaned_content = content.strip()
        if cleaned_content:
            # Remove any remaining markdown artifacts
            # Remove lines that are just markdown formatting
            lines = cleaned_content.split("\n")
            filtered_lines = []
            for line in lines:
                # Skip markdown headers, separators, and code block markers
                stripped = line.strip()
                if stripped.startswith("```") or stripped.startswith("#") and "===" in stripped:
                    continue
                if re.match(r'^[-=]{3,}$', stripped):  # Markdown separators
                    continue
                filtered_lines.append(line)
            cleaned_content = "\n".join(filtered_lines).strip()
            if cleaned_content:
                cleaned_files[file_path] = cleaned_content
    
    # Debug: log what was found
    if cleaned_files:
        print(f"// Parser extracted {len(cleaned_files)} files:", file=sys.stderr)
        for file_path in sorted(cleaned_files.keys()):
            print(f"//   - {file_path}", file=sys.stderr)
    else:
        print(f"// ⚠️  Parser found 0 files. Output length: {len(java_code)} chars", file=sys.stderr)
        # Show first few lines for debugging
        preview_lines = java_code.split("\n")[:20]
        print(f"// Output preview (first 20 lines):", file=sys.stderr)
        for i, line in enumerate(preview_lines, 1):
            print(f"// {i:2d}: {line[:80]}", file=sys.stderr)
    
    return cleaned_files


def write_multi_file_output(files: Dict[str, str], base_output_dir: Path, unit_id: str, node_id: str):
    """Write multiple Java files to appropriate package directories."""
    output_dir = base_output_dir / f"{unit_id}_{node_id}_pure_java"
    output_dir.mkdir(parents=True, exist_ok=True)
    
    written_files = []
    for file_path, content in files.items():
        # Normalize path - handle both relative and absolute paths
        if "/" in file_path or "\\" in file_path:
            # Extract package path
            # Handle both forward and backslash separators
            normalized_path = file_path.replace("\\", "/")
            parts = normalized_path.split("/")
            file_name = parts[-1]
            package_parts = parts[:-1]
            
            # Create package directory structure
            file_dir = output_dir
            for part in package_parts:
                file_dir = file_dir / part
            file_dir.mkdir(parents=True, exist_ok=True)
            
            target_file = file_dir / file_name
        else:
            # Single file, put in root
            target_file = output_dir / file_path
        
        try:
            target_file.write_text(content, encoding="utf-8")
            written_files.append(str(target_file.relative_to(base_output_dir)))
            print(f"// ✅ Wrote {target_file.relative_to(base_output_dir)} ({len(content)} chars)", file=sys.stderr)
        except Exception as e:
            print(f"// ❌ Error writing {target_file}: {e}", file=sys.stderr)
            raise
    
    if written_files:
        print(f"\n// ✅ Successfully generated {len(written_files)} Java files", file=sys.stderr)
        print(f"// 📁 Output directory: {output_dir.absolute()}", file=sys.stderr)
        print(f"// 📦 Package structure:", file=sys.stderr)
        for file_path in sorted(written_files)[:10]:  # Show first 10 files
            print(f"//    {file_path}", file=sys.stderr)
        if len(written_files) > 10:
            print(f"//    ... and {len(written_files) - 10} more files", file=sys.stderr)
    else:
        print(f"// ⚠️  WARNING: No files were written!", file=sys.stderr)
    
    return written_files


def main():
    parser = argparse.ArgumentParser(
        description="Track B: Migrate RPG to Pure Java with layered architecture",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=dedent("""
        Examples:
          python3 migrate_to_pure_java.py context_index/HS1210_n404.json
          python3 migrate_to_pure_java.py context_index/HS1210_n404.json --stream
          python3 migrate_to_pure_java.py context_index/HS1210_n404.json --output-dir ./output
          python3 migrate_to_pure_java.py context_index/HS1210_n404.json --rpg-file PoC_HS1210/HS1210.sqlrpgle
        """),
    )
    parser.add_argument(
        "context_file",
        help="Path to a ContextPackage JSON file (e.g. context_index/HS1210_n404.json)",
    )
    parser.add_argument(
        "--output-dir",
        default=".",
        help="Output directory for generated Java files (default: current directory)",
    )
    parser.add_argument(
        "--model",
        default="claude-sonnet-4-5",
        help="Anthropic model name (default: claude-sonnet-4-5)",
    )
    parser.add_argument(
        "--max-tokens",
        type=int,
        default=64000,
        help="Max tokens for the response (default: 64000). Claude Sonnet 4.5 allows up to 64000.",
    )
    parser.add_argument(
        "--stream",
        action="store_true",
        help="Stream the response (shows progress)",
    )
    parser.add_argument(
        "--rpg-file",
        type=Path,
        default=None,
        metavar="PATH",
        help="Path to the RPG source file (e.g. HS1210.sqlrpgle). If provided, the full source for the node's range (startLine-endLine from context) is loaded and sent to the LLM so it can generate complete logic instead of stubs.",
    )
    args = parser.parse_args()

    api_key = os.environ.get("ANTHROPIC_API_KEY")
    if not api_key:
        raise SystemExit(
            "ANTHROPIC_API_KEY env var is not set.\n"
            "Please export your key:\n"
            "  export ANTHROPIC_API_KEY='sk-ant-...'\n"
            "Then retry this command."
        )
    
    if not api_key.startswith("sk-ant-"):
        print("⚠️  Warning: API key doesn't start with 'sk-ant-'. This might be invalid.", file=sys.stderr)

    with open(args.context_file, "r", encoding="utf-8") as f:
        context = json.load(f)

    # Extract unit/node IDs from context file name
    context_path = Path(args.context_file)
    stem = context_path.stem
    if "_" in stem:
        unit_id, node_id = stem.split("_", 1)
    else:
        unit_id = stem
        node_id = "unknown"

    # Log context size
    context_size_kb = len(json.dumps(context)) / 1024
    db_contracts = context.get("dbContracts", [])
    total_columns = sum(len(c.get("columns", [])) for c in db_contracts)
    print(f"// Context package size: {context_size_kb:.1f} KB", file=sys.stderr)
    print(f"// DB Contracts: {len(db_contracts)}, Total columns: {total_columns}", file=sys.stderr)
    
    # Optional: load full RPG source from file for complete logic generation
    rpg_source_override = None
    rpg_range = get_range_from_context(context)
    if args.rpg_file:
        rpg_path = Path(args.rpg_file)
        if not rpg_path.is_absolute():
            # Try relative to cwd first, then relative to context file's directory
            if not rpg_path.exists():
                rpg_path = context_path.parent / rpg_path
            if not rpg_path.exists():
                rpg_path = context_path.parent.parent / args.rpg_file
        if rpg_range and rpg_path.exists():
            rpg_source_override = load_rpg_source_from_file(rpg_path, rpg_range[0], rpg_range[1])
            if rpg_source_override:
                print(f"// Loaded full RPG source from {rpg_path} (lines {rpg_range[0]}-{rpg_range[1]}, {len(rpg_source_override)} chars)", file=sys.stderr)
            else:
                print(f"// ⚠️  Could not read RPG file {rpg_path}; using context snippet", file=sys.stderr)
        else:
            if not rpg_range:
                print(f"// ⚠️  No range in context; cannot load --rpg-file", file=sys.stderr)
            else:
                print(f"// ⚠️  RPG file not found: {args.rpg_file}; using context snippet", file=sys.stderr)
    
    # Embedded SQL extraction (for prompt checklist)
    rpg_source_for_sql = rpg_source_override or context.get("rpgSnippet", "") or ""
    embedded_sql_entries = extract_embedded_sql(rpg_source_for_sql)
    if embedded_sql_entries:
        print(f"// Extracted {len(embedded_sql_entries)} embedded SQL statement(s) for translation to repository methods", file=sys.stderr)
    
    print(f"// Generating Pure Java (Track B) with layered architecture...", file=sys.stderr)
    
    if total_columns > 250:
        estimated_output_tokens = total_columns * 50
        if estimated_output_tokens > args.max_tokens * 0.8:
            print(f"// ⚠️  WARNING: Large context detected ({total_columns} columns).", file=sys.stderr)
            print(f"//    Estimated output tokens: ~{estimated_output_tokens}. Current max_tokens: {args.max_tokens}", file=sys.stderr)
            print(f"//    Consider increasing --max-tokens if migration is incomplete.", file=sys.stderr)
    
    print(f"// This may take 60-120 seconds for large packages...", file=sys.stderr)

    user_prompt = build_pure_java_prompt(context, rpg_source_override=rpg_source_override, rpg_range=rpg_range)
    prompt_size_kb = len(user_prompt) / 1024
    print(f"// Prompt size: {prompt_size_kb:.1f} KB", file=sys.stderr)

    client = anthropic.Anthropic(api_key=api_key)

    import time
    start_time = time.time()
    
    print("// Starting Pure Java migration (Track B)...", file=sys.stderr, flush=True)
    
    try:
        if args.stream:
            print("// Streaming response (you'll see code appear incrementally)...", file=sys.stderr)
            with client.messages.stream(
                model=args.model,
                max_tokens=args.max_tokens,
                temperature=0.2,
                messages=[
                    {
                        "role": "user",
                        "content": user_prompt,
                    }
                ],
                timeout=1200.0,  # 20 minutes
            ) as stream:
                token_count = 0
                java_code_stream = ""
                for text_block in stream.text_stream:
                    print(text_block, end="", flush=True)
                    java_code_stream += text_block
                    token_count += len(text_block.split())
                    if token_count % 100 == 0:
                        print(f"\n// Generated ~{token_count} tokens so far...", file=sys.stderr, end="")
                print(f"\n// Stream complete. Total tokens: ~{token_count}", file=sys.stderr)
                
                java_code_stream = clean_java_code(java_code_stream)
                if check_code_truncation(java_code_stream, args.max_tokens):
                    print("// ⚠️  WARNING: Code may be truncated (near token limit).", file=sys.stderr)
                    print(f"//    Consider increasing --max-tokens (current: {args.max_tokens}) or splitting the migration.", file=sys.stderr)
                    print(f"//    Generated code length: {len(java_code_stream)} chars (~{len(java_code_stream)/4:.0f} tokens)", file=sys.stderr)
                java_code_stream = fix_unbalanced_braces(java_code_stream)
                if java_code_stream.count("{") != java_code_stream.count("}"):
                    print("// Warning: braces still unbalanced after auto-fix", file=sys.stderr)
                
                # Parse and write multi-file output
                print(f"\n// Parsing multi-file output...", file=sys.stderr)
                files = parse_multi_file_output(java_code_stream, Path(args.output_dir))
                print(f"// Found {len(files)} Java files to write", file=sys.stderr)
                if files:
                    write_multi_file_output(files, Path(args.output_dir), unit_id, node_id)
                else:
                    print("// ⚠️  WARNING: No files extracted from output. Check parser logic.", file=sys.stderr)
        else:
            print("// Sending request to LLM (this may take 60-120 seconds for large prompts)...", file=sys.stderr)
            response = None
            max_retries = 3
            retry_delay_sec = 15
            for attempt in range(max_retries):
                try:
                    response = client.messages.create(
                        model=args.model,
                        max_tokens=args.max_tokens,
                        temperature=0.2,
                        messages=[
                            {
                                "role": "user",
                                "content": user_prompt,
                            }
                        ],
                        timeout=1200.0,  # 20 minutes
                    )
                    break
                except Exception as e:
                    err_str = str(e)
                    is_500 = "500" in err_str or "Internal server error" in err_str or (getattr(e, "status_code", None) == 500)
                    if is_500 and attempt < max_retries - 1:
                        print(f"// API server error (attempt {attempt + 1}/{max_retries}). Retrying in {retry_delay_sec}s...", file=sys.stderr)
                        time.sleep(retry_delay_sec)
                    else:
                        raise
            elapsed = time.time() - start_time
            print(f"// LLM response received in {elapsed:.1f} seconds", file=sys.stderr)
            
            java_code = ""
            for block in response.content:
                if getattr(block, "type", None) == "text":
                    java_code += block.text
            
            java_code = clean_java_code(java_code)
            
            if check_code_truncation(java_code, args.max_tokens):
                print("// ⚠️  WARNING: Code may be truncated (near token limit).", file=sys.stderr)
                print(f"//    Consider increasing --max-tokens (current: {args.max_tokens}) or splitting the migration.", file=sys.stderr)
                print(f"//    Generated code length: {len(java_code)} chars (~{len(java_code)/4:.0f} tokens)", file=sys.stderr)
            
            java_code = fix_unbalanced_braces(java_code)
            if java_code.count("{") != java_code.count("}"):
                print("// Warning: braces still unbalanced after auto-fix", file=sys.stderr)
            
            # Parse and write multi-file output
            print(f"// Parsing multi-file output...", file=sys.stderr)
            files = parse_multi_file_output(java_code, Path(args.output_dir))
            print(f"// Found {len(files)} Java files to write", file=sys.stderr)
            if files:
                write_multi_file_output(files, Path(args.output_dir), unit_id, node_id)
                print(f"// ✅ Migration complete! Files written to: {Path(args.output_dir) / f'{unit_id}_{node_id}_pure_java'}", file=sys.stderr)
            else:
                print("// ⚠️  WARNING: No files extracted from output. Check parser logic.", file=sys.stderr)
                print("// Output preview (first 500 chars):", file=sys.stderr)
                print(java_code[:500], file=sys.stderr)
            
            # Also print to stdout for compatibility (UI server)
            print(java_code)
    except anthropic.RateLimitError as e:
        elapsed = time.time() - start_time
        print(f"// Rate limit (429) after {elapsed:.1f} seconds.", file=sys.stderr)
        print("// Wait one minute and retry, or use a smaller node.", file=sys.stderr)
        raise SystemExit(129)
    except Exception as e:
        elapsed = time.time() - start_time
        print(f"// Error after {elapsed:.1f} seconds: {e}", file=sys.stderr)
        raise


if __name__ == "__main__":
    main()
