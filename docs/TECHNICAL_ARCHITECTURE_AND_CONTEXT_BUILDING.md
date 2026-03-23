# Technical Architecture and Context Building Process

**Version:** 1.0  
**Date:** March 2026  
**Audience:** Technical team, developers

This document provides an **updated and precise** technical architecture of the Scania RPG-to-Java migration pipeline, with emphasis on the **design** and **context building process**.

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Inputs](#2-inputs)
3. [Context Building Pipeline (Tab 1)](#3-context-building-pipeline-tab-1)
4. [Artifact Dependencies](#4-artifact-dependencies)
5. [Data Structures](#5-data-structures)
6. [Migration Flow (Tabs 2–5)](#6-migration-flow-tabs-25)
7. [Migration Prompt](#7-migration-prompt)
8. [BFS-Based Iterative Node Migration](#8-bfs-based-iterative-node-migration)
9. [Output Application](#9-output-application)
10. [Directory Layout](#10-directory-layout)
11. [Build Resilience (Auto-Fix Pipeline)](#11-build-resilience-auto-fix-pipeline)
12. [API Summary](#12-api-summary)
13. [Related Documentation](#13-related-documentation)

---

## 1. System Overview

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                    RPG-TO-JAVA MIGRATION PIPELINE                                         │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                          │
│  INPUTS                    CONTEXT BUILDING (Tab 1)              DOWNSTREAM              │
│  ┌──────────────┐          ┌─────────────────────────────┐       ┌─────────────────────┐  │
│  │ AST JSON     │─────────▶│ 1. build_db_registry         │       │ Tab 2: Migrate      │  │
│  │ *-ast.json   │          │ 2. program context + call    │       │ Tab 3: Validate     │  │
│  │ *D-ast.json  │          │    graph (with RPG)         │       │ Tab 4: Build        │  │
│  └──────────────┘          │ 3. build_context_index       │       │ Tab 5: Run & Demo   │  │
│  ┌──────────────┐          │ 4. build_node_index          │       └──────────┬──────────┘  │
│  │ RPG Source   │─────────▶│ 5. export_neo4j_cypher       │                  │             │
│  │ .sqlrpgle    │          │                             │                  ▼             │
│  └──────────────┘          └─────────────────────────────┘       warranty_demo (Spring Boot)│
│                                     │                            Angular UI, demo.html     │
│                                     ▼                                                      │
│                            global_context/                                                 │
│                            context_index/                                                  │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

**Pipeline UI:** `ui_global_context_server.py` (port 8003)  
**Default AST:** `JSON_ast/JSON_20260311` (PKS revised, no column redundancy)

---

## 2. Inputs

| Input | Location | Description |
|-------|----------|-------------|
| **RPG AST** | `JSON_ast/<version>/*-ast.json` | PKS Systems AST exports (e.g. HS1210-ast.json, HS1212-ast.json). Excludes *D-ast.json. |
| **DDS AST** | `JSON_ast/<version>/*D-ast.json` | Display file ASTs (e.g. HS1210D-ast.json). Used for UI contracts. |
| **RPG Source** | `PoC_HS1210/` or similar | `.sqlrpgle`, `.rpgle` files. **Required** for call graph enrichment and RPG snippet extraction. |

**AST structure (per file):**
- `unit` — program metadata (id, library, member, sourceFile)
- `nodes` — AST nodes (id, kind, name, sem, outgoingEdges, range)
- `sym` — symbol table (sym.file.*, sym.var.*, sym.ds.*)
- `types` — type definitions
- `dbContracts.nativeFiles` — physical file schemas (name, library, columns, keys)

---

## 3. Context Building Pipeline (Tab 1)

The context building process runs **5 main steps** in sequence. RPG source is **always required** for context building and call graphs.

### 3.1 Step 1: build_db_registry.py

| Aspect | Detail |
|--------|--------|
| **Location** | `global_context/build_db_registry.py` |
| **Input** | AST directory (`--astDir`). Scans all `*-ast.json` (excludes *D-ast.json). |
| **Output** | `global_context/db_registry.json` |

**Process:** For each AST file, extracts `dbContracts.nativeFiles`. Merges by (library, name). Each file has: symbolId, name, library, typeId, columns (name, typeId, sqlType, length, scale, nullable, key), sourceUnits.

**Column schema:** name, typeId, sqlType, length, scale, nullable, key. Nullable from AST or default false.

---

### 3.2 Step 2: Program context and call graph (with RPG)

Three scripts run in sequence; **RPG source is required**.

| Script | Input | Output |
|--------|-------|--------|
| `build_program_context.py` | AST dir | `global_context/programs/<programId>.program.json` |
| `build_call_graph.py` | AST dir | `global_context/call_graph.json` |
| `build_call_graph_with_rpg.py` | call_graph, programs, **RPG dir** | `global_context/call_graph_enriched.json` |

**Process:** (1) Extract unit metadata, nodes, symbols from AST. (2) Build caller→callee edges from AST. (3) Enrich by parsing RPG source for EXSR, CALLP, procName(); resolve calleeNodeId; add synthetic edges for procedure calls discovered from RPG.

---

### 3.3 Step 3: build_context_index.py

| Aspect | Detail |
|--------|--------|
| **Location** | `build_context_index.py` (project root) |
| **Input** | AST dir (`--astDir`), RPG dir (`--rpgDir`), `call_graph_enriched.json`, `db_registry.json`, `programs/*.program.json` |
| **Output** | `context_index/<unitId>_<nodeId>.json`, `context_index/manifest.json` |
| **RPG** | Required for rpgSnippet extraction |

**Process:**
1. Loads AST, call graph, db_registry, programs
2. For each migratable node (Subroutine, Procedure, etc.): builds context package
3. Resolves dbContracts from node's sym.file refs + db_registry
4. Extracts rpgSnippet from RPG source for node's range
5. Builds narrative from AST semantics
6. Adds displayFiles from `{unitId}D-ast.json` if present (uiContracts.displayFiles)
7. Adds callGraph (calls, calledBy) from call_graph_enriched
8. Writes `context_index/<unit>_<node>.json` and manifest.json

**Context package fields:** astNode, narrative, rpgSnippet, dbContracts, symbolMetadata, displayFiles, callGraph

---

### 3.4 Step 4: build_node_index.py

| Aspect | Detail |
|--------|--------|
| **Location** | `build_node_index.py` (project root) |
| **Input** | AST dir (`--astDir`), RPG dir (`--rpgDir`) |
| **Output** | `context_index/<unitId>_nodes.json` (one per AST file) |
| **RPG** | Required for line-to-node mapping |

**Process:** Fine-grained node index for traceability. All nodes, parent-child tree, RPG line mapping.

---

### 3.5 Step 5: export_neo4j_cypher.py

| Aspect | Detail |
|--------|--------|
| **Location** | `global_context/export_neo4j_cypher.py` |
| **Input** | `db_registry.json`, `programs/*.program.json`, `call_graph_enriched.json` (fallback: call_graph.json) |
| **Output** | `global_context/neo4j_export.cypher` |
| **RPG** | Not used |

**Process:** Exports knowledge graph as Cypher script for Neo4j. Nodes: Program, DbFile, Column, Node. Relationships: USES_DB, HAS_COLUMN, HAS_NODE, CALLS.

---

## 4. Artifact Dependencies

```
                    AST (*-ast.json, *D-ast.json)     RPG source
                              │                              │
         ┌────────────────────┘                              │
         │                                                    │
         ▼                                                    │
  build_db_registry                                           │
         │                                                    │
         ▼                                                    │
   db_registry.json                                           │
         │                                                    │
         │    ┌────────────────────────────────────────────────┘
         │    │  (program context + call graph: build_program_context,
         │    │   build_call_graph, build_call_graph_with_rpg)
         │    ▼
         │   programs/*.json, call_graph_enriched.json
         │                    │
         ▼                    ▼
  build_context_index ◀───────┘
         │
         ▼
  context_index/*.json
  manifest.json
         │
         └──────────────────────┬─────────────────────────────┐
                                │                             │
                                ▼                             ▼
                         build_node_index            export_neo4j_cypher
                                │                             │
                                ▼                             ▼
                         *_nodes.json                 neo4j_export.cypher
```

---

## 5. Data Structures

### 5.1 db_registry.json

```json
{
  "root": "<project_root>",
  "astRoot": "<ast_dir>",
  "fileCount": N,
  "files": [
    {
      "symbolId": "sym.file.HSG71LF2",
      "name": "HSG71LF2",
      "library": "HSSRC",
      "typeId": "t.file.HSG71LF2",
      "sourceUnits": ["qsys:HSSRC/QRPGLESRC/HS1210"],
      "columns": [
        {
          "name": "G71000",
          "typeId": "t.char.3",
          "sqlType": "CHAR(3)",
          "length": 3,
          "scale": null,
          "nullable": false,
          "key": true
        }
      ]
    }
  ]
}
```

### 5.2 programs/<id>.program.json

```json
{
  "programId": "HS1210",
  "unit": { "id": "...", "library": "...", "member": "HS1210" },
  "nodes": [{ "id": "n404", "kind": "Subroutine", "name": null, "range": {...} }],
  "symbols": {
    "sym.var.X": { "name": "X", "kind": "variable", "referencedBy": ["n404"], "shared": false }
  }
}
```

### 5.3 call_graph_enriched.json

```json
{
  "adjacency": { "n404": ["n1919", "n1983"], "n1779": [] },
  "edges": [
    { "callerNodeId": "n404", "calleeNodeId": "n1919", "calleeName": "CreateClaim", "opcode": "CALLP" }
  ]
}
```

### 5.4 context_index/<unit>_<node>.json (Context Package)

```json
{
  "astNode": { "id": "n404", "kind": "Subroutine", "name": null, "sem": "{...}", "outgoingEdges": [...], "range": {...} },
  "narrative": "## Business narrative in Markdown...",
  "rpgSnippet": "     C     *entry  plist\n     C                   parm ...",
  "dbContracts": [
    { "name": "HSG71LF2", "library": "HSSRC", "columns": [...] }
  ],
  "symbolMetadata": { ... },
  "displayFiles": [ { "recordFormat": "...", "fields": [...] } ],
  "callGraph": { "calls": ["n1919", "n1983"], "calledBy": [] }
}
```

### 5.5 context_index/manifest.json

```json
{
  "entries": [
    {
      "unitId": "HS1210",
      "nodeId": "n404",
      "kind": "Subroutine",
      "dbFilesUsed": ["HSAHKLF3", "HSG71LF2", ...],
      "calls": ["n1919", "n1983"],
      "calledBy": []
    }
  ]
}
```

---

## 6. Migration Flow (Tabs 2–5)

| Tab | Component | Input | Output |
|-----|-----------|-------|--------|
| **2** | migrate_to_pure_java.py | context_index/*.json, db_registry, LLM | warranty_demo/src/main/java/ |
| **3** | validate_pure_java.py | warranty_demo, manifest | Score card, traceability |
| **4** | Maven + fix_compile_errors.py | warranty_demo | JAR, tests |
| **5** | mvn spring-boot:run | warranty_demo | Running app :8081 |

---

## 7. Migration Prompt

**Location:** `migrate_to_pure_java.py` — `_build_static_system_prompt()`, `build_pure_java_prompt()`

The migration uses a two-part prompt:

### 7.1 System Prompt (static, cached)

Same for all nodes; enables prompt caching (2×+ faster on 2nd+ run within 5 min).

- **Role:** Expert IBM i RPG and Java architect for Pure Java migration
- **RPG symbol mapping:** Status codes (99→EXCLUDED, 20→APPROVED, etc.), indicators (MARKxx), variable names (STATUS→statusCodeSde, PAKZ→pakz)
- **Traceability:** Mandatory `// @rpg-trace: <nodeId>` annotations on generated Java
- **Logic completeness:** No empty loops, no stub returns, no missing entities
- **Requirements:** Layered architecture (domain/service/repository/dto/web), domain names, enums, Records, Streams, Optional, JPA composite keys, unique controller mappings

### 7.2 User Prompt (per-node)

Built from the context package for the current node.

| Section | Source | Purpose |
|--------|--------|---------|
| **Architecture guidance** | Extracted entities, value objects, enums | Target package layout, domain classes |
| **Unit to migrate** | astNode | nodeId, kind |
| **Narrative** | context.narrative | Business intent (Markdown) |
| **RPG source** | context.rpgSnippet or `--rpg-file` | Full source or snippet; primary control flow |
| **Embedded SQL checklist** | Extracted from RPG | Numbered list; each must map to a repository @Query |
| **DB contracts** | context.dbContracts | JSON schema; 100% column mapping required |
| **Column checklist** | Derived from contracts | Total columns; must match @Column count |
| **Display files** | context.displayFiles | DDS UI contracts for DTOs |
| **Symbol metadata** | context.symbolMetadata | Variables, data structures, k-lists |
| **Existing entities** | Scanned from warranty_demo | Tables already migrated; reuse, do not regenerate |
| **Call graph** | context.callGraph | Tables used, subroutines invoked, call details, canonical entity rules |
| **RPG source map** | statementNodes | Line→nodeId for traceability annotations |

### 7.3 Output format

- Raw Java only; no markdown code blocks
- File separators: `// === domain/Claim.java ===`

---

## 8. BFS-Based Iterative Node Migration

**Location:** `ui_global_context_server.py` (migrate-feature), `build_context_index` + `call_graph_enriched.json`

Tab 2 migrates a **feature slice** (multiple nodes) in dependency order. Each node is migrated with a **separate LLM call**.

### 8.1 Slice computation

1. **Entry node:** User selects entry node (e.g. n404) in the UI.
2. **BFS:** From `entry_node_id` over `adjacency` (caller → callee) in `call_graph_enriched.json`:
   ```
   visited = set()
   queue = [entry_node_id]
   while queue:
       nid = queue.pop(0)
       if nid in visited: continue
       visited.add(nid)
       for succ in adjacency.get(nid, []):
           if succ not in visited: queue.append(succ)
   nodes_set = visited
   ```
3. **Topological sort:** Callees before callers (dependency order). Uses reverse graph (callee → caller) and Kahn’s algorithm:
   - Nodes with no incoming edges (callees) first
   - Then nodes that depend on them
   - Ensures when A calls B, B is migrated before A.

### 8.2 Iteration order

```
nodes_in_slice = topological_sort(reverse_adjacency)  # callees first
for nid in nodes_in_slice:
    context_file = context_index/{unitId}_{nid}.json
    if context_file.exists():
        migrate_to_pure_java.py context_file --target-project warranty_demo
```

**One LLM call per node.** Each run writes to `warranty_demo/src/main/java/`; later runs see existing entities.

### 8.3 Entity deduplication

| Mechanism | Purpose |
|-----------|---------|
| `_get_existing_entities_from_project()` | Scan warranty_demo for `@Entity` with `@Table(name="X")` |
| `existing_entities` in prompt | Instruct LLM: “HSAHKLF3 → use existing Invoice; do not create again” |
| `TABLE_TO_CANONICAL_ENTITY` | HSAHKLF3→Invoice, HSG71LF2→Claim, etc. |
| `_deduplicate_entities_by_table()` | Post-parse: drop duplicate entity files |

---

## 9. Output Application

### 9.1 Database

| Profile | Database | URL |
|---------|----------|-----|
| **default** | H2 file-based | `jdbc:h2:file:./data/warranty_db` |
| **rds** | AWS RDS PostgreSQL | From application-rds.properties |

**Schema:** Hibernate `ddl-auto=update`. `data.sql` seeds HSAHKLF3 (invoices) via MERGE after schema creation (`defer-datasource-initialization=true`).

### 9.2 Access Points

| URL | Description |
|-----|-------------|
| http://localhost:8081 | Index page |
| http://localhost:8081/angular/ | Angular UI (claims list, create, detail) |
| http://localhost:8081/demo.html | Demo overview |
| http://localhost:8081/swagger-ui.html | Swagger API |
| http://localhost:8081/h2-console | H2 console (default profile) |

---

## 10. Directory Layout

```
<project_root>/
├── JSON_ast/
│   └── JSON_20260311/           # Default AST (PKS revised)
│       ├── HS1210-ast.json
│       ├── HS1210D-ast.json
│       ├── HS1212-ast.json
│       └── HS1212D-ast.json
├── global_context/
│   ├── db_registry.json         # Step 1
│   ├── programs/                # Step 2
│   │   ├── HS1210.program.json
│   │   └── HS1212.program.json
│   ├── call_graph.json          # Step 2 (internal)
│   ├── call_graph_enriched.json # Step 2
│   ├── neo4j_export.cypher      # Step 5
│   └── migrations/             # Tab 2 output
├── context_index/              # Steps 3, 4
│   ├── manifest.json
│   ├── HS1210_n404.json
│   ├── HS1210_n1779.json
│   └── HS1210_nodes.json
├── warranty_demo/              # Generated application
│   ├── src/main/java/com/scania/warranty/
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   ├── data.sql            # Seeds HSAHKLF3
│   │   ├── static/
│   │   │   ├── demo.html
│   │   │   ├── index.html
│   │   │   └── angular/        # Angular build output
│   │   └── ui-schemas/
│   └── data/                   # H2 DB files (runtime)
├── warranty-ui/                # Angular source
├── build_context_index.py      # Step 3
├── build_node_index.py         # Step 4
├── migrate_to_pure_java.py     # Tab 2
├── fix_compile_errors.py       # Tab 4 autofix
├── validate_pure_java.py       # Tab 3
└── ui_global_context_server.py # Pipeline server
```

---

## 11. Build Resilience (Auto-Fix Pipeline)

The build pipeline includes several **automatic fixers** that run before and during Maven builds to prevent deployment failures:

| Fixer | Purpose |
|-------|---------|
| `fix_idclass` | JPA @IdClass alignment |
| `fix_test_alignment` | Test getter names vs entity (getClaimNumber → getClaimNr) |
| `fix_ambiguous_mapping` | Duplicate controller endpoint paths |
| **fix_int_bigdecimal** | int literal → BigDecimal.ZERO when entity setter expects BigDecimal |
| **fix_compile_errors** | LLM-based fix for remaining compilation errors |

**Flow:** Initial build uses `mvn test-compile` (includes test sources). On failure, fixers run; `fix_compile_errors.py` handles test files (`src/test/java`) and includes entity types for int→BigDecimal fixes. Retries up to 4 LLM passes.

---

## 12. API Summary

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/discover-directories` | List AST/RPG directories |
| POST | `/api/build-global-context` | Run all 5 context-building steps |
| POST | `/api/migrate-feature` | Generate Java from context |
| POST | `/api/validate` | Run validation |
| POST | `/api/build-application` | Maven build + autofix |
| POST | `/api/run-application` | Start Spring Boot |

---

## 13. Related Documentation

| Document | Description |
|----------|-------------|
| `WARRANTY_DEMO_APPLICATION_GUIDE.md` | Key Java files, RPG traceability, claim creation flow |
| `ARCHITECTURE_AND_PIPELINE.md` | Client-facing architecture |
| `POC_USER_GUIDE.md` | End-to-end user guide |
| `PIPELINE_SCALABILITY.md` | Scalability challenges and mitigation |
| `AST_20260311_SETUP.md` | PKS revised AST setup |

---

*Document reflects pipeline state as of March 2026.*
