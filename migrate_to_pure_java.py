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
import copy
import json
import os
import re
import sys
import time
from collections import Counter
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

# RPG symbol/indicator semantic mapping for accurate translation
RPG_SYMBOL_GLOSSARY = """
## RPG Symbol & Indicator Mapping (for semantic accuracy)

When translating RPG conditions to Java, use these mappings to preserve meaning:

**Status codes (map to ClaimStatus enum):**
- 99 → ClaimStatus.EXCLUDED (excluded/inactive)
- 20 → ClaimStatus.APPROVED
- 11 → ClaimStatus.REJECTED
- 0 → ClaimStatus.PENDING
- 5 → ClaimStatus.MINIMUM

**Indicators (MARKxx, etc.):** RPG indicators like MARK12, MARK11 often mean "record selected" or "valid for processing" in subfile/list contexts. When the semantic intent is "record is active/valid", translate to Java as:
  - `status != null && status != ClaimStatus.EXCLUDED` (i.e. statusCodeSde != 99)
  - Or `existing != null` when checking for presence

**Common variable mappings:**
- STATUS, STATUSCODESDE → statusCodeSde (entity field)
- PAKZ → pakz (company code)
- RECHNR → rechNr (invoice number)
- RECHDATUM, RECH_DATUM → rechDatum (invoice date)
- CLAIMNR, CLANO → claimNr (claim number)
- FILART, SR_FILART → filter/type fields

**Error handling:** EXSR (external subroutine) for error paths → Java: throw new IllegalArgumentException(...) or similar.
"""


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


def check_duplicate_column_names(db_contracts: List[Dict]) -> List[Tuple[str, str, int]]:
    """
    Check if AST schema has multiple columns sharing the same logical name.
    Returns list of (table_name, column_name, count) for each duplicate.
    Use this to report AST enhancement request (point 1) to PKS.
    """
    duplicates: List[Tuple[str, str, int]] = []
    for contract in db_contracts:
        table_name = (contract.get("fileName") or contract.get("name") or "?").strip()
        cols = contract.get("columns") or []
        if not cols:
            continue
        names = [str(c.get("name", "")).strip() for c in cols if c.get("name")]
        counts = Counter(names)
        for name, count in counts.items():
            if name and count > 1:
                duplicates.append((table_name, name, count))
    return duplicates


def resolve_duplicate_column_names(db_contracts: List[Dict]) -> List[Dict]:
    """
    Resolve duplicate column names within each contract.
    Legacy RPG/DB2 schemas often have multiple columns named RESERVE (or similar).
    JPA requires unique @Column(name="...") values. When duplicates exist,
    assign unique names: RESERVE1, RESERVE2, RESERVE3, RESERVE4, etc.
    """
    contracts = copy.deepcopy(db_contracts)
    for contract in contracts:
        cols = contract.get("columns") or []
        if not cols:
            continue
        # Count occurrences of each base name
        names = [str(c.get("name", "")).strip() for c in cols]
        counts = Counter(names)
        # Assign unique names for duplicates
        seen: Dict[str, int] = {}
        for col in cols:
            base = str(col.get("name", "")).strip()
            if not base:
                continue
            seen[base] = seen.get(base, 0) + 1
            occurrence = seen[base]
            if counts[base] > 1:
                col["name"] = base + str(occurrence)
    return contracts


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


def _build_static_system_prompt() -> str:
    """Build the static system prompt for prompt caching. Same for all migrations; enables 2x+ faster responses on 2nd+ run within 5 min."""
    return dedent("""
        You are an expert IBM i (AS/400) RPG and Java architect specializing in **Pure Java application architecture**.
        Your task is to migrate a single RPG subroutine/procedure to **Pure Java** following modern Java/Spring best practices,
        with **zero hallucinations** on data structures and **layered architecture**.

        ## RPG Symbol & Indicator Mapping (for semantic accuracy)
        When translating RPG conditions to Java, use these mappings to preserve meaning:

        **Status codes (map to ClaimStatus enum):**
        - 99 → ClaimStatus.EXCLUDED (excluded/inactive)
        - 20 → ClaimStatus.APPROVED
        - 11 → ClaimStatus.REJECTED
        - 0 → ClaimStatus.PENDING
        - 5 → ClaimStatus.MINIMUM

        **Indicators (MARKxx, etc.):** RPG indicators like MARK12, MARK11 often mean "record selected" or "valid for processing" in subfile/list contexts. When the semantic intent is "record is active/valid", translate to Java as:
          - `status != null && status != ClaimStatus.EXCLUDED` (i.e. statusCodeSde != 99)
          - Or `existing != null` when checking for presence

        **Common variable mappings:**
        - STATUS, STATUSCODESDE → statusCodeSde (entity field)
        - PAKZ → pakz (company code)
        - RECHNR → rechNr (invoice number)
        - RECHDATUM, RECH_DATUM → rechDatum (invoice date)
        - CLAIMNR, CLANO → claimNr (claim number)
        - FILART, SR_FILART → filter/type fields

        **Error handling:** EXSR (external subroutine) for error paths → Java: throw new IllegalArgumentException(...) or similar.

        ## Traceability Annotations (MANDATORY)
        For every generated Java statement that corresponds to RPG logic, add an inline comment: `javaStatement; // @rpg-trace: <nodeId>`
        Use the nodeId from the RPG SOURCE MAP provided in the user message. For entity fields from DB contracts: // @rpg-trace: schema

        ## Logic Completeness (ENFORCED)
        - **Empty loops** → FAIL. Every loop MUST contain real statements.
        - **Stub-only methods** (return true/false/constant) → FAIL. Every method MUST implement real logic.
        - **Missing entities** → FAIL. Every table in dbContracts MUST have a matching @Entity.

        ## Requirements
        1. **Target**: Pure Java with layered architecture (domain/service/repository/dto/web), domain names, enums, Java Records, Streams, Optional.
        2. **SQL Translation**: Translate ALL RPG embedded SQL to repository @Query methods. Every SQL statement MUST have a corresponding method.
        3. **Data structures**: 100% column mapping MANDATORY. @Column(name="EXACT_DB_NAME"). Use camelCase for Java fields.
        4. **Domain-driven design**: Value objects, enums for magic values, domain language in method names.
        5. **Modern Java**: Records for DTOs, Streams, Optional, constructor injection, stateless services.
        6. **Output format**: Raw Java, no markdown. Separate files with // === path/File.java ===
        7. **Syntax**: Valid compilable Java. Matching braces. Exact setter names.
        8. **Complete logic**: No empty loops, no stub returns. Full implementation from RPG/narrative.
        9. **JPA composite keys (MANDATORY)**: Entities with multiple @Id fields MUST have @IdClass(XxxId.class) and a separate XxxId class (Serializable, fields matching @Id names exactly, equals/hashCode). Repositories MUST use JpaRepository<Entity, XxxId> not JpaRepository<Entity, String>. Single @Id uses simple type.
        10. **Controller mappings (MANDATORY)**: No two @RestController methods may map to the same HTTP method + path. If adding a new endpoint that would duplicate an existing one (e.g. POST /api/claims/create), use a distinct path (e.g. /create-from-request) to avoid "Ambiguous mapping" at startup.
        """)


def _build_trace_source_map(statement_nodes: List[Dict]) -> str:
    """Build a compact RPG source line → AST node ID map for LLM traceability annotations."""
    if not statement_nodes:
        return ""
    lines = []
    skip_kinds = {"Comment", "EndIf", "EndDo", "EndSelect", "EndMon", "EndSubroutine"}
    for sn in statement_nodes:
        kind = sn.get("kind", "")
        if kind in skip_kinds:
            continue
        nid = sn.get("id", "?")
        opcode = sn.get("opcode") or kind
        sl = sn.get("startLine")
        el = sn.get("endLine")
        target = sn.get("target", "")
        syms = sn.get("symbols", [])
        desc_parts = [opcode]
        if target:
            desc_parts.append(target.replace("sym.var.", "").replace("sym.file.", ""))
        elif syms:
            desc_parts.append(", ".join(s.replace("sym.var.", "").replace("sym.file.", "") for s in syms[:3]))
        desc = " ".join(desc_parts)
        if sl and el and sl != el:
            lines.append(f"  Line {sl}-{el}: {nid} ({desc})")
        elif sl:
            lines.append(f"  Line {sl}: {nid} ({desc})")
        else:
            lines.append(f"  {nid} ({desc})")
    if not lines:
        return ""
    return "\n".join(lines)


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
    dupes = check_duplicate_column_names(db_contracts)
    if dupes:
        for tbl, col, cnt in dupes:
            print(f"// AST has duplicate column: {tbl}.{col} x{cnt} (pipeline will rewrite to {col}1..{col}{cnt})", file=sys.stderr)
        print(f"// Consider requesting AST enhancement: unique physical column names for duplicates (see docs/DRAFT_EMAIL_AST_ENHANCEMENTS.md)", file=sys.stderr)
    db_contracts = resolve_duplicate_column_names(db_contracts)
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
    
    # Build RPG source line → AST node traceability map
    statement_nodes = context.get("statementNodes", [])
    line_to_node_map = context.get("lineToNodeMap", {})
    trace_source_map = _build_trace_source_map(statement_nodes)

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
        
        ## RPG SOURCE MAP (use these nodeIds for // @rpg-trace: <nodeId> annotations)
        --- RPG SOURCE MAP START ---
        {trace_source_map if trace_source_map else "(No statement nodes available)"}
        --- RPG SOURCE MAP END ---
        
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
    after_code_block = False  # True after closing ```; prevents capturing explanatory text
    
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
                in_code_block = False
                after_code_block = False
                file_found = True
                break
        
        if file_found:
            i += 1
            continue
        
        # Check for markdown code block start/end
        if line.strip().startswith("```"):
            if in_code_block:
                if current_file and current_content:
                    files[current_file] = "\n".join(current_content)
                current_content = []
                in_code_block = False
                after_code_block = True  # Do not capture text until next file marker
            else:
                in_code_block = True
                after_code_block = False
            i += 1
            continue
        
        # Collect content only when inside code block, or in raw format (current_file set, no markdown)
        if in_code_block or (current_file and not after_code_block):
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


# Layer descriptions for file-level Javadoc (helps maintainers understand generated code)
_FILE_DESCRIPTION_BY_LAYER = {
    "domain": "Domain entity or value object for the warranty claims model.",
    "repository": "Spring Data JPA repository for warranty claim data access.",
    "service": "Application service implementing warranty claim business logic.",
    "dto": "Data transfer object for API or display.",
    "config": "Spring configuration bean.",
    "web": "REST controller for warranty claim APIs.",
}

# Package-level descriptions for package-info.java (integration mode)
_PACKAGE_DESCRIPTION = {
    "domain": "Domain entities and value objects for the warranty claims application.",
    "repository": "Spring Data JPA repositories for warranty claim data access.",
    "service": "Application services implementing warranty claim business logic.",
    "dto": "Data transfer objects for API and UI.",
    "config": "Spring configuration and beans.",
    "web": "REST controllers for warranty claim APIs.",
}


def _file_description_from_path(file_path: str) -> str:
    """Derive a short file-level description from the path (e.g. domain/Claim.java -> domain)."""
    normalized = file_path.replace("\\", "/")
    parts = [p for p in normalized.split("/") if p and p != "com" and p != "scania" and p != "warranty"]
    if len(parts) >= 2:
        layer = parts[-2].lower()  # e.g. domain, service
    elif len(parts) == 1:
        layer = "domain"  # fallback
    else:
        layer = "domain"
    return _FILE_DESCRIPTION_BY_LAYER.get(layer, "Generated type for the warranty claims application.")


def _make_file_header(file_path: str, unit_id: str, node_id: str, add_traceability: bool) -> str:
    """Build a Javadoc block for the top of a generated Java file (description + optional RPG traceability)."""
    description = _file_description_from_path(file_path)
    lines = [
        "/**",
        f" * {description}",
    ]
    if add_traceability:
        lines.append(" * <p>")
        lines.append(f" * Generated from RPG: unit {{@code {unit_id}}}, node {{@code {node_id}}}.")
    lines.append(" */")
    return "\n".join(lines)


def _prepend_file_header(content: str, header: str) -> str:
    """Prepend a Javadoc header at the very top of the file (before package)."""
    content_stripped = content.strip()
    if not content_stripped:
        return header
    return header + "\n\n" + content_stripped


def _extract_package_from_content(content: str) -> Optional[str]:
    """Extract package name from the first 'package X;' line in Java content."""
    match = re.search(r"^\s*package\s+([\w.]+)\s*;", content, re.MULTILINE)
    return match.group(1) if match else None


def _package_description(package_name: str) -> str:
    """Short description for a package (last segment, e.g. domain, service)."""
    segment = package_name.split(".")[-1].lower()
    return _PACKAGE_DESCRIPTION.get(segment, "Types for the warranty claims application.")


def _write_package_info(
    output_dir: Path,
    package_name: str,
    unit_id: str,
    node_id: str,
    add_traceability: bool,
) -> None:
    """Write package-info.java for the given package (integration mode)."""
    package_path = package_name.replace(".", "/")
    package_dir = output_dir / package_path
    package_dir.mkdir(parents=True, exist_ok=True)
    info_file = package_dir / "package-info.java"
    if info_file.exists():
        return  # Do not overwrite existing package-info
    description = _package_description(package_name)
    lines = [
        "/**",
        f" * {description}",
    ]
    if add_traceability:
        lines.append(" * <p>")
        lines.append(f" * Contains types generated from RPG: unit {{@code {unit_id}}}, node {{@code {node_id}}}.")
    lines.append(" */")
    lines.append(f"package {package_name};")
    info_file.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"// ✅ Wrote package-info.java for {package_name}", file=sys.stderr)


def write_multi_file_output(
    files: Dict[str, str],
    base_output_dir: Path,
    unit_id: str,
    node_id: str,
    integrate_into_project: bool = False,
    add_traceability: bool = True,
) -> Tuple[Path, List[str]]:
    """
    Write multiple Java files to appropriate package directories.

    - Standalone (default): create {unitId}_{nodeId}_pure_java under base_output_dir.
    - Integration mode: write directly under base_output_dir (expected to be e.g. targetProject/src/main/java).
    - Prepends file-level Javadoc (description + optional RPG unit/node traceability) to each file.
    """
    if integrate_into_project:
        output_dir = base_output_dir
    else:
        output_dir = base_output_dir / f"{unit_id}_{node_id}_pure_java"
    output_dir.mkdir(parents=True, exist_ok=True)
    
    written_files = []
    packages_used: Set[str] = set()
    for file_path, content in files.items():
        header = _make_file_header(file_path, unit_id, node_id, add_traceability)
        content = _prepend_file_header(content, header)
        pkg = _extract_package_from_content(content)
        if pkg:
            packages_used.add(pkg)
        # In integration mode, derive path from package so files go under com/scania/warranty/...
        # (LLM often outputs short markers like domain/Claim.java but package is com.scania.warranty.domain)
        normalized_path = file_path.replace("\\", "/")
        file_name = normalized_path.split("/")[-1] if "/" in normalized_path else file_path
        if integrate_into_project and pkg:
            file_dir = output_dir / pkg.replace(".", "/")
            target_file = file_dir / file_name
        elif "/" in normalized_path or "\\" in file_path:
            parts = normalized_path.split("/")
            package_parts = parts[:-1]
            file_dir = output_dir
            for part in package_parts:
                file_dir = file_dir / part
            file_dir.mkdir(parents=True, exist_ok=True)
            target_file = file_dir / file_name
        else:
            target_file = output_dir / file_path
        target_file.parent.mkdir(parents=True, exist_ok=True)
        
        try:
            target_file.write_text(content, encoding="utf-8")
            # For logging, keep paths relative to the logical base (project root or output root)
            rel_base = base_output_dir
            try:
                rel_path = target_file.relative_to(rel_base)
            except ValueError:
                rel_path = target_file.name
            written_files.append(str(rel_path))
            print(f"// ✅ Wrote {rel_path} ({len(content)} chars)", file=sys.stderr)
        except Exception as e:
            print(f"// ❌ Error writing {target_file}: {e}", file=sys.stderr)
            raise
    
    if integrate_into_project and packages_used:
        for pkg in sorted(packages_used):
            _write_package_info(output_dir, pkg, unit_id, node_id, add_traceability)
    
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
    
    return output_dir, written_files


def _run_idclass_fixer(target_root: Path) -> None:
    """
    Run post-migration IdClass fixer for entities with composite keys.
    Part of resilient pipeline: prevents runtime 'does not define an IdClass' failures.
    """
    try:
        from fix_idclass import run_fix
        count, messages = run_fix(target_root)
        if count > 0:
            for m in messages:
                print(f"// {m}", file=sys.stderr)
            print(f"// ✅ IdClass fixer: {count} entity(ies) fixed", file=sys.stderr)
    except ImportError as e:
        print(f"// ⚠️  IdClass fixer skipped (fix_idclass not found): {e}", file=sys.stderr)
    except Exception as e:
        print(f"// ⚠️  IdClass fixer failed: {e}", file=sys.stderr)


def _run_ambiguous_mapping_fixer(target_root: Path) -> None:
    """
    Run post-migration ambiguous mapping fixer for duplicate controller endpoints.
    Part of resilient pipeline: prevents Spring 'Ambiguous mapping' startup failures.
    """
    try:
        from fix_ambiguous_mapping import run_fix
        count, msg = run_fix(target_root)
        if count > 0:
            print(f"// ✅ Ambiguous mapping fixer: {msg}", file=sys.stderr)
    except ImportError as e:
        print(f"// ⚠️  Ambiguous mapping fixer skipped (fix_ambiguous_mapping not found): {e}", file=sys.stderr)
    except Exception as e:
        print(f"// ⚠️  Ambiguous mapping fixer failed: {e}", file=sys.stderr)


def _generate_ui_schema_if_applicable(context: dict, target_root: Path, unit_id: str = "") -> None:
    """
    Generate ui-schemas/<ScreenId>.json from displayFiles when integrating into a project.
    Part of the migration pipeline: display → backend → DB → UI.
    """
    display_files = context.get("displayFiles", [])
    if not display_files:
        return
    try:
        from ui_schema_generator import generate_ui_schema, write_ui_schema
        schema = generate_ui_schema(context, unit_id=unit_id)
        if schema:
            out_path = write_ui_schema(schema, target_root)
            print(f"// ✅ Generated UI schema: {out_path.relative_to(target_root)}", file=sys.stderr)
    except ImportError as e:
        print(f"// ⚠️  UI schema generation skipped (ui_schema_generator not found): {e}", file=sys.stderr)
    except Exception as e:
        print(f"// ⚠️  UI schema generation failed: {e}", file=sys.stderr)


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
    parser.add_argument(
        "--integration-app",
        default=None,
        help="Logical app id for integration mode (e.g. warranty-claims). Optional, for metadata only.",
    )
    parser.add_argument(
        "--target-project",
        default=None,
        help="Existing Spring Boot project root to integrate into (e.g. warranty_demo). "
             "When set, generated Java is written into this project instead of a standalone *_pure_java directory.",
    )
    parser.add_argument(
        "--no-traceability",
        action="store_true",
        help="Do not add RPG unit/node traceability comments to generated files and package-info.",
    )
    parser.add_argument(
        "--no-inline-origin",
        action="store_true",
        help="Do not inject // @origin RPG line annotations into generated Java (traceability in IDE).",
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

    system_prompt = _build_static_system_prompt()
    user_prompt = build_pure_java_prompt(context, rpg_source_override=rpg_source_override, rpg_range=rpg_range)
    prompt_size_kb = (len(system_prompt) + len(user_prompt)) / 1024
    print(f"// Prompt size: {prompt_size_kb:.1f} KB", file=sys.stderr)

    client = anthropic.Anthropic(api_key=api_key)

    import time
    start_time = time.time()
    
    # Determine output strategy
    integration_mode = args.target_project is not None
    if integration_mode:
        target_root = Path(args.target_project)
        if not target_root.is_dir():
            raise SystemExit(f"Target project root not found: {target_root}")
        base_output_dir = target_root / "src/main/java"
        print(f"// Integration mode: writing into existing project {target_root}", file=sys.stderr)
    else:
        base_output_dir = Path(args.output_dir)
        print(f"// Standalone mode: writing into {base_output_dir} / {unit_id}_{node_id}_pure_java", file=sys.stderr)

    print("// Starting Pure Java migration (Track B)...", file=sys.stderr, flush=True)
    
    try:
        if args.stream:
            print("// Streaming response (you'll see code appear incrementally)...", file=sys.stderr)
            with client.messages.stream(
                model=args.model,
                max_tokens=args.max_tokens,
                temperature=0.2,
                system=system_prompt,
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
                files = parse_multi_file_output(java_code_stream, base_output_dir)
                print(f"// Found {len(files)} Java files to write", file=sys.stderr)
                if files:
                    output_dir, written_files = write_multi_file_output(
                        files,
                        base_output_dir,
                        unit_id,
                        node_id,
                        integrate_into_project=integration_mode,
                        add_traceability=not args.no_traceability,
                    )
                    if not args.no_inline_origin and written_files:
                        try:
                            from inject_origin_annotations import inject_annotations
                            root_dir = context_path.resolve().parent.parent
                            inject_annotations(output_dir, unit_id, node_id, root_dir)
                        except Exception as e:
                            print(f"// ⚠️  Inline origin injection skipped: {e}", file=sys.stderr)
                    if integration_mode:
                        print(f"// ✅ Migration complete! Files integrated into project: {output_dir.parent.parent}", file=sys.stderr)
                        _run_idclass_fixer(target_root)
                        _run_ambiguous_mapping_fixer(target_root)
                        _generate_ui_schema_if_applicable(context, target_root, unit_id)
                    else:
                        print(f"// ✅ Migration complete! Files written to: {output_dir}", file=sys.stderr)
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
                        system=system_prompt,
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
            files = parse_multi_file_output(java_code, base_output_dir)
            print(f"// Found {len(files)} Java files to write", file=sys.stderr)
            if files:
                output_dir, written_files = write_multi_file_output(
                    files,
                    base_output_dir,
                    unit_id,
                    node_id,
                    integrate_into_project=integration_mode,
                    add_traceability=not args.no_traceability,
                )
                if not args.no_inline_origin and written_files:
                    try:
                        from inject_origin_annotations import inject_annotations
                        root_dir = context_path.resolve().parent.parent
                        inject_annotations(output_dir, unit_id, node_id, root_dir)
                    except Exception as e:
                        print(f"// ⚠️  Inline origin injection skipped: {e}", file=sys.stderr)
                if integration_mode:
                    print(f"// ✅ Migration complete! Files integrated into project: {output_dir.parent.parent}", file=sys.stderr)
                    _run_idclass_fixer(target_root)
                    _run_ambiguous_mapping_fixer(target_root)
                    _generate_ui_schema_if_applicable(context, target_root, unit_id)
                else:
                    print(f"// ✅ Migration complete! Files written to: {output_dir}", file=sys.stderr)
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
