### New AST Technical Analysis Report

**Project:** Scania RPG → Java Migration Pipeline  
**Audience:** Technical Architecture Review  
**Date:** 2026‑02‑12

---

### 1. Purpose

This document provides a **technical analysis** of the new compiler‑grade RPG ASTs (`JSON_ast/JSON_20260211/*.json`). It evaluates their **structure, capabilities, and limitations**, identifies **missing technical elements** needed for complete migration support, and outlines **required enhancements** to fully exploit AST richness, especially for DSPF (display file) migration.

---

### 2. AST Structure Analysis

**Location:** `JSON_ast/JSON_20260211/*.json`  
**Example:** `JSON_ast/JSON_20260211/HS1212-ast.json`  
**Format:** JSON, version `rpg-ast/1.0`  
**Generator:** PKS-RPG-FrontEnd v9.2.3.12

#### 2.1 Core Components

**`unit` Section:**
- Program metadata: library, source file, member name, member type (SQLRPGLE, RPGLE)
- Compiler settings: dialect (ILE-RPG), CCSID (37), encoding (EBCDIC-CP037)
- Compiler options (`ctlOpt`): Debug, ExprOpts, DFTACTGRP, BNDDIR
- Include graph: nested includes with line ranges
- Content integrity: SHA-256 hash for versioning

**`files` Section:**
- Complete inventory of all referenced DDS members
- Each entry: `id` (stable identifier), `path` (full system path), `hash` (content hash)
- Examples from HS1212: `AUFWSKF`, `FARSTLF4`, `HSEPAL2`, `HSG71PF`, `S3F003`, etc.
- **Note:** Does not distinguish PF vs LF vs DSPF vs PRTF (all listed as QDDSSRC members)

**`nodes` Section:**
- AST node graph with structural representation
- Each node: `id`, `kind` (CompilationUnit, Subroutine, Procedure, Chain, Write, If, etc.)
- `range`: Source code spans (fileId, startLine, endLine, columns, byte offsets)
- `sem`: Symbol references (`sym.file.*`, `sym.var.*`, `sym.ds.*`, `sym.ind.*`, `sym.dspf.*`)
- `props`: Operation-specific properties (e.g., `opcode`, `target`, `keys` for CHAIN operations)

**`symbolTable` Section:**
- Complete symbol definitions
- Categories: `sym.file.*`, `sym.var.*`, `sym.ds.*`, `sym.ind.*`, `sym.dspf.*`, `sym.proc.*`
- Each symbol: `symbolId`, `name`, `kind`, `declNodeId`, `scopeId`, `typeId`

**`dbContracts` Section:**
- Structured database contract definitions
- For each file: `symbolId`, `name`, `library`, `recordFormat`, `usage`, `keys`, `columns`
- Column details: `name`, `typeId`, `nullable`
- Key definitions: `name`, `ascending`
- **Note:** Only covers PF/LF files, not DSPF display files

**`edges` Section:**
- Graph edges connecting nodes and symbols
- Edge types: `declares`, `references`, `calls`, etc.

---

### 3. Comparative Analysis: Old vs New ASTs

#### 3.1 Previous (Legacy) AST Generation

**Structure:**
- Basic AST with node IDs, kinds, and ranges
- Limited symbol table coverage
- Incomplete file inventory

**Strengths:**
- Smaller file sizes (easier to process)
- Sufficient for basic control-flow analysis

**Shortcomings:**
- **Incomplete DB schema:** Not all DDS members fully parsed; column lists often partial
- **No DSPF distinction:** Display files not systematically identified
- **Partial symbol coverage:** Symbol table did not mirror compiler's full semantic view
- **No operation details:** File operations (READ, WRITE, CHAIN) lacked structured metadata

#### 3.2 New ASTs (`JSON_20260211`) – Technical Merits

**Complete File Inventory:**
- `files` section enumerates **every** QDDSSRC member referenced
- Each entry has stable `id`, full system `path`, and content `hash`
- Enables reliable DDS parsing and version tracking

**Rich Symbol Table:**
- Exhaustive coverage: `sym.file.*`, `sym.var.*`, `sym.ds.*`, `sym.ind.*`, `sym.dspf.*`
- Supports accurate mapping of:
  - Working variables → Java method parameters/DTOs
  - Composite keys → JPA entity keys
  - Status indicators → Java boolean/enum fields
  - Display files → UI components (via `sym.dspf.*`)

**Structured DB Contracts:**
- `dbContracts` section provides complete column definitions
- Includes: column names, types (`typeId`), nullability, keys, record formats
- Enables 100% column mapping validation

**Operation Metadata:**
- Nodes have `props` with operation details:
  - `opcode`: "CHAIN", "WRITE", "READ", etc.
  - `target`: file name
  - `keys`: key field names (for CHAIN operations)
- Links operations to files via `sem` symbol references

**DSPF Awareness:**
- `sym.dspf.*` symbols exist (e.g., `sym.dspf.HS1212D`)
- Display files appear in `files` section
- RPG operations can reference DSPF files via `sem`

**Versioning and Integrity:**
- Content hashes enable change detection
- Full paths enable reliable source retrieval

#### 3.3 Technical Shortcomings

**No File Type Classification:**
- `files` section does not distinguish:
  - Physical files (PF) vs Logical files (LF) vs Display files (DSPF) vs Printer files (PRTF)
- Must infer from naming conventions or parse DDS headers separately
- **Impact:** Cannot automatically route DSPF files to display contract extraction

**No Inlined DDS/DSPF Layouts:**
- AST does **not** contain:
  - Record format definitions for DSPF (screen layouts)
  - Field positions, lengths, attributes for display fields
  - Indicator conditions and meanings
  - Screen field-to-DB column bindings
- These must be parsed separately from DDS source files
- **Impact:** Requires separate DDS parser to extract UI semantics

**No Explicit Cross-File Relationships:**
- Logical file "based-on" relationships not explicit
- Join relationships (multi-file logical files) not structured
- DSPF field-to-DB column bindings not exposed
- **Impact:** Must infer relationships from DDS parsing

**Limited Operation Format Information:**
- Operations have `opcode`, `target`, `keys` but not:
  - Format name for EXFMT operations (which screen format is displayed)
  - Format name for READ/WRITE operations on display files
  - Indicator-to-operation mappings
- **Impact:** Cannot directly correlate RPG operations to specific display formats

**No Indicator Semantics:**
- `sym.ind.*` exists but lacks:
  - Meaning/usage context (UI vs. program logic)
  - Indicator-to-field/operation mappings
  - Conditional logic semantics
- **Impact:** Must infer indicator meanings from RPG code analysis

**Large File Sizes:**
- Full ASTs are very large (HS1212-ast.json: ~126K lines)
- Includes low-level compiler details
- **Impact:** Requires efficient parsing and selective extraction

---

### 4. DSPF (Display File) Technical Analysis

#### 4.1 What the ASTs Provide

**Discovery:**
- ✅ `files` section lists all QDDSSRC members (including DSPF)
- ✅ `sym.dspf.*` symbols exist (e.g., `sym.dspf.HS1212D`)
- ✅ RPG operations can reference DSPF files via `sem`

**Richness Score: HIGH** for discovery and traceability

#### 4.2 What is Missing

**Critical Missing Elements:**

1. **File Type Classification:**
   - AST does not mark files as DSPF vs PF vs LF
   - Must parse DDS header to determine file type
   - **Required:** Add `fileType` field to `files[]` entries or parse DDS A/R/E/F records

2. **Display Format Definitions:**
   - No record format structures (screen layouts)
   - No field definitions (position, length, type, attributes)
   - No indicator-to-field mappings
   - **Required:** DDS parser to extract from DSPF source files

3. **Operation-to-Format Mapping:**
   - `EXFMT formatName` operations exist but format name not structured
   - Cannot directly map `EXFMT AUFWSKF MAIN` → display format "MAIN"
   - **Required:** Enhance `props` to include `format` field for display operations

4. **Indicator Semantics:**
   - Indicators exist in `sym.ind.*` but no meaning/usage context
   - Cannot determine which indicators control field visibility/editing
   - **Required:** DDS parser to extract indicator definitions and meanings

5. **Field-to-DB Bindings:**
   - No explicit mapping of display fields to DB columns
   - No data structure bindings (RPG DS → display fields)
   - **Required:** DDS parser + RPG code analysis to establish bindings

**Richness Score: MEDIUM** for UI semantics (requires DDS parsing layer)

---

### 5. Required AST Enhancements

#### 5.1 Must-Have: File Type Classification

**Current State:**
- `files[]` entries only have `id`, `path`, `hash`
- No `fileType` field to distinguish PF/LF/DSPF/PRTF

**Required Enhancement:**
```json
{
  "id": "qsys:HSSRC/QDDSSRC/AUFWSKF",
  "path": "/QSYS.LIB/HSSRC.LIB/QDDSSRC.FILE/AUFWSKF.MBR",
  "hash": "...",
  "fileType": "DSPF"  // NEW: "PF", "LF", "DSPF", "PRTF"
}
```

**Alternative:** Parse DDS header (A/R/E/F record types) to infer file type

#### 5.2 Must-Have: Operation Format Information

**Current State:**
- Operations have `opcode`, `target`, `keys` but not format names
- Example: `EXFMT AUFWSKF MAIN` → format "MAIN" not captured

**Required Enhancement:**
```json
{
  "id": "n1234",
  "kind": "Exfmt",
  "props": {
    "opcode": "EXFMT",
    "target": "AUFWSKF",
    "format": "MAIN"  // NEW: format name for display operations
  }
}
```

**Impact:** Enables direct mapping of RPG operations to display formats

#### 5.3 Must-Have: Cross-File Relationship Metadata

**Current State:**
- Logical files exist but "based-on" relationships not explicit
- Join relationships not structured

**Required Enhancement:**
```json
{
  "id": "qsys:HSSRC/QDDSSRC/HSG71LF2",
  "fileType": "LF",
  "relationships": {  // NEW
    "basedOn": "sym.file.HSG71PF",
    "joinFiles": [],
    "keyInheritance": "override"
  }
}
```

**Impact:** Enables accurate logical file → physical file mapping

#### 5.4 Must-Have: Indicator Semantics

**Current State:**
- `sym.ind.*` exists but no meaning/usage context

**Required Enhancement:**
```json
{
  "symbolId": "sym.ind.IN01",
  "name": "IN01",
  "kind": "indicator",
  "semantics": {  // NEW
    "meaning": "Error condition",
    "usage": "UI",
    "controls": ["FLD001", "FLD002"],
    "setBy": ["validation", "file operations"]
  }
}
```

**Impact:** Enables UI-aware code generation with proper indicator handling

#### 5.5 Should-Have: Display Format Structures

**Current State:**
- No display format definitions in AST

**Required Enhancement (if vendor-supported):**
```json
{
  "displayContracts": {  // NEW section
    "sym.dspf.HS1212D": {
      "fileId": "qsys:HSSRC/QDDSSRC/HS1212D",
      "recordFormats": [
        {
          "name": "MAIN",
          "fields": [...],
          "indicators": [...]
        }
      ]
    }
  }
}
```

**Note:** This may require vendor AST enhancements; otherwise must parse DDS separately

#### 5.6 Should-Have: Enhanced Operation Metadata

**Current State:**
- Operations have basic `props` but limited detail

**Required Enhancement:**
```json
{
  "id": "n1234",
  "kind": "Chain",
  "props": {
    "opcode": "CHAIN",
    "target": "HSG71PF",
    "keys": ["PAKZ", "RECHNR"],
    "format": null,  // NEW: format for display files
    "indicator": "IN80",  // NEW: indicator set by operation
    "errorHandling": "notFound"  // NEW: behavior when not found
  }
}
```

**Impact:** Enables more accurate behavioral validation

---

### 6. Missing Technical Elements Summary

#### 6.1 Currently Missing from AST

**File Classification:**
- ❌ No `fileType` field in `files[]` entries
- ❌ Cannot distinguish PF/LF/DSPF/PRTF without DDS parsing

**Display File Details:**
- ❌ No `displayContracts` section
- ❌ No record format definitions
- ❌ No field definitions (position, length, attributes)
- ❌ No indicator-to-field mappings

**Operation Metadata:**
- ❌ No `format` field in operation `props` for display operations
- ❌ No indicator semantics in operation results
- ❌ Limited error handling metadata

**Relationships:**
- ❌ No explicit "based-on" relationships for logical files
- ❌ No join relationship structures
- ❌ No DSPF field-to-DB column bindings

**Indicator Semantics:**
- ❌ No meaning/usage context for `sym.ind.*`
- ❌ No indicator-to-field/operation mappings

#### 6.2 Workarounds Required

**Until AST enhancements are available:**

1. **DDS Parser:** Must parse DDS source files (from `files[].path`) to extract:
   - File type (from A/R/E/F record types)
   - Column definitions (for PF/LF)
   - Display formats and fields (for DSPF)
   - Indicator definitions

2. **Code Analysis:** Must analyze RPG source to infer:
   - Operation-to-format mappings (from EXFMT statements)
   - Indicator meanings (from conditional logic)
   - Field-to-DB bindings (from data structure usage)

3. **Metadata Layer:** Build separate metadata layer that:
   - Classifies files by type
   - Extracts display contracts from DDS
   - Correlates operations with formats
   - Maps indicators to UI behavior

---

### 7. Technical Recommendations

#### 7.1 Immediate (Can be done with current AST)

1. **Build DDS Parser:**
   - Parse DDS source files (from `files[].path`)
   - Extract file type, columns, keys, formats, fields, indicators
   - Build display contract structures

2. **Enhance Operation Extraction:**
   - Parse RPG source to extract format names from EXFMT operations
   - Correlate operations with display formats
   - Build operation-to-format mapping

3. **Build Metadata Layer:**
   - Classify files by type (PF/LF/DSPF)
   - Extract relationships (based-on, joins)
   - Map indicators to meanings

#### 7.2 Short-term (Requires AST vendor enhancements)

1. **Request `fileType` field** in `files[]` entries
2. **Request `format` field** in operation `props` for display operations
3. **Request `relationships` section** for logical file metadata

#### 7.3 Long-term (If vendor-supported)

1. **Request `displayContracts` section** with parsed display formats
2. **Request enhanced indicator semantics** in `sym.ind.*` entries
3. **Request operation result metadata** (indicators set, error handling)

---

### 8. AST Requirements for Application-Centric Migration

#### 8.1 Context: Application-Centric Migration Pipeline

**Goal:** Transform "RPG-mapped Java" (which mirrors RPG structure) into **pure, idiomatic Java** with layered architecture:
- **Domain Layer:** Entities, value objects, aggregates
- **Service Layer:** Use-case services, business logic
- **Repository Layer:** Data access abstraction (JPA repositories)
- **API Layer:** Controllers (REST endpoints)

**Challenge:** Current ASTs provide **program-level** information but lack **application-level** context needed to:
- Identify service boundaries
- Group related programs into services
- Understand cross-program dependencies
- Identify transaction boundaries
- Map UI workflows to service methods
- Extract domain models and aggregates

#### 8.2 Missing AST Elements for Application-Centric Migration

##### 8.2.1 Cross-Program Dependencies

**Current State:**
- ASTs are **per-program** (one JSON file per RPG program)
- Program calls exist (`sym.proc.*`, procedure call nodes) but:
  - ❌ No explicit "calls" or "invokes" relationship metadata
  - ❌ No parameter signature information for external program calls
  - ❌ No call graph across programs
  - ❌ No distinction between internal procedures vs external program calls

**Required Enhancement:**
```json
{
  "programCalls": {  // NEW section
    "external": [
      {
        "callerNodeId": "n1234",
        "targetProgram": "HS1215",
        "targetLibrary": "HSSRC",
        "parameters": [
          { "name": "PARM1", "type": "char(10)", "direction": "inout" },
          { "name": "PARM2", "type": "char(5)", "direction": "in" }
        ],
        "callType": "CALL"  // vs "CALLP", "EXSR"
      }
    ],
    "internal": [
      {
        "callerNodeId": "n5678",
        "targetProcedure": "sym.proc.ValidateClaim",
        "parameters": [...]
      }
    ]
  }
}
```

**Impact:** Enables identification of service boundaries and cross-program dependencies

##### 8.2.2 Application Structure and Boundaries

**Current State:**
- No concept of "application" or "module" grouping
- No metadata about which programs belong together
- No business domain boundaries

**Required Enhancement:**
```json
{
  "applicationContext": {  // NEW section
    "applicationId": "WarrantyClaimManagement",
    "module": "ClaimProcessing",
    "domain": "Warranty",
    "relatedPrograms": [
      { "id": "HS1212", "role": "main", "type": "interactive" },
      { "id": "HS1215", "role": "service", "type": "batch" },
      { "id": "HS1217", "role": "utility", "type": "service" }
    ],
    "aggregates": [
      {
        "name": "Claim",
        "rootEntity": "HSG73PF",
        "relatedEntities": ["HSG71PF", "HSG73LF1"],
        "services": ["HS1212", "HS1215"]
      }
    ]
  }
}
```

**Alternative:** Build this from cross-program call analysis + business domain knowledge

##### 8.2.3 Transaction Boundaries

**Current State:**
- No explicit transaction markers
- Cannot identify where transactions start/end
- No COMMIT/ROLLBACK operation metadata

**Required Enhancement:**
```json
{
  "transactionBoundaries": [  // NEW section
    {
      "startNodeId": "n100",
      "endNodeId": "n500",
      "type": "implicit",  // vs "explicit" (COMMIT/ROLLBACK)
      "scope": "procedure",  // vs "program", "subroutine"
      "isolationLevel": "READ_COMMITTED"
    }
  ]
}
```

**Impact:** Enables proper `@Transactional` annotation placement in service layer

##### 8.2.4 Business Logic Patterns and Use Cases

**Current State:**
- Procedures/subroutines exist but no semantic grouping
- No identification of "use cases" or "business operations"
- No workflow patterns

**Required Enhancement:**
```json
{
  "useCases": [  // NEW section
    {
      "id": "uc-claim-create",
      "name": "Create Warranty Claim",
      "entryPoint": "sym.proc.CreateClaim",
      "procedures": ["sym.proc.ValidateClaim", "sym.proc.SaveClaim"],
      "dataFlow": [
        { "from": "input", "to": "HSG73PF", "operation": "INSERT" },
        { "from": "HSG71PF", "to": "output", "operation": "READ" }
      ],
      "uiWorkflow": ["HS1212D", "MAIN", "CONFIRM"]
    }
  ]
}
```

**Impact:** Enables grouping procedures into service methods with domain names

##### 8.2.5 Data Access Patterns

**Current State:**
- File operations exist (CHAIN, READ, WRITE) but:
  - ❌ No grouping by "entity" or "aggregate"
  - ❌ No identification of CRUD patterns
  - ❌ No query pattern recognition

**Required Enhancement:**
```json
{
  "dataAccessPatterns": {  // NEW section
    "entities": [
      {
        "name": "Claim",
        "primaryFile": "HSG73PF",
        "operations": [
          { "type": "CREATE", "nodeIds": ["n100", "n200"] },
          { "type": "READ", "nodeIds": ["n300"] },
          { "type": "UPDATE", "nodeIds": ["n400"] }
        ],
        "queries": [
          {
            "pattern": "findByKey",
            "keyFields": ["G73060", "G73065"],
            "nodeIds": ["n500"]
          }
        ]
      }
    ]
  }
}
```

**Impact:** Enables automatic generation of repository interfaces and service methods

##### 8.2.6 UI Workflow and Screen Navigation

**Current State:**
- Display files exist (`sym.dspf.*`) but:
  - ❌ No screen navigation flow
  - ❌ No user journey mapping
  - ❌ No screen-to-operation correlation

**Required Enhancement:**
```json
{
  "uiWorkflows": [  // NEW section
    {
      "workflowId": "claim-entry",
      "screens": [
        {
          "format": "MAIN",
          "displayFile": "HS1212D",
          "operations": [
            { "type": "EXFMT", "nodeId": "n100" },
            { "type": "READ", "nodeId": "n200" }
          ],
          "transitions": [
            { "trigger": "F3", "to": "EXIT" },
            { "trigger": "ENTER", "to": "CONFIRM" }
          ]
        }
      ],
      "entryPoint": "sym.proc.MainMenu",
      "exitPoints": ["sym.proc.Exit"]
    }
  ]
}
```

**Impact:** Enables generation of REST controllers with proper endpoint mapping

##### 8.2.7 Domain Model Extraction

**Current State:**
- Data structures exist (`sym.ds.*`) but:
  - ❌ No identification of "entities" vs "DTOs" vs "value objects"
  - ❌ No relationships between data structures
  - ❌ No domain model boundaries

**Required Enhancement:**
```json
{
  "domainModel": {  // NEW section
    "entities": [
      {
        "name": "Claim",
        "primaryFile": "HSG73PF",
        "dataStructures": ["sym.ds.ClaimDS"],
        "relationships": [
          { "type": "one-to-many", "target": "ClaimPosition", "via": "HSG71PF" }
        ],
        "businessRules": [
          { "rule": "validateCreditDate", "nodeId": "n300" }
        ]
      }
    ],
    "valueObjects": [
      {
        "name": "ClaimKey",
        "fields": ["G73060", "G73065"],
        "immutable": true
      }
    ]
  }
}
```

**Impact:** Enables proper domain-driven design with entities, aggregates, and value objects

##### 8.2.8 Service Layer Identification

**Current State:**
- Procedures/subroutines exist but:
  - ❌ No grouping into "services"
  - ❌ No identification of service boundaries
  - ❌ No dependency injection requirements

**Required Enhancement:**
```json
{
  "serviceBoundaries": [  // NEW section
    {
      "serviceName": "ClaimProcessingService",
      "programs": ["HS1212"],
      "procedures": [
        "sym.proc.CreateClaim",
        "sym.proc.UpdateClaim",
        "sym.proc.DeleteClaim"
      ],
      "dependencies": [
        { "type": "repository", "entity": "Claim" },
        { "type": "service", "name": "ValidationService" }
      ],
      "transactional": true
    }
  ]
}
```

**Impact:** Enables automatic service class generation with proper dependencies

#### 8.3 Workarounds Until AST Enhancements

**Until application-level AST metadata is available:**

1. **Build Call Graph Analyzer:**
   - Parse all ASTs in an application
   - Extract program calls from `sym.proc.*` and procedure call nodes
   - Build cross-program dependency graph
   - Infer service boundaries from call patterns

2. **Domain Model Extractor:**
   - Analyze `dbContracts` to identify entities
   - Group related files into aggregates
   - Extract data structures that map to entities
   - Build domain model from file relationships

3. **Use Case Identifier:**
   - Analyze procedure entry points
   - Group procedures by data flow patterns
   - Identify workflows from UI operations (EXFMT sequences)
   - Map procedures to business operations

4. **Transaction Analyzer:**
   - Identify COMMIT/ROLLBACK operations
   - Analyze file operation sequences
   - Infer transaction boundaries from data access patterns
   - Map to service method boundaries

5. **UI Workflow Mapper:**
   - Parse DSPF files to extract screen formats
   - Analyze EXFMT/READ sequences to build navigation flow
   - Map screens to service operations
   - Generate REST controller structure

#### 8.4 Required AST Enhancements Priority

**Must-Have (for application-centric migration):**

1. **Cross-Program Call Metadata:**
   - Program call relationships with parameters
   - Distinction between internal vs external calls
   - Call graph across programs

2. **Transaction Boundaries:**
   - COMMIT/ROLLBACK operation markers
   - Implicit transaction scope identification
   - Transaction isolation level metadata

3. **Data Access Patterns:**
   - Entity identification from file operations
   - CRUD pattern recognition
   - Query pattern extraction

**Should-Have (for enhanced architecture):**

4. **Application Structure:**
   - Program grouping by application/module
   - Aggregate identification
   - Service boundary suggestions

5. **Use Case Identification:**
   - Business operation grouping
   - Workflow pattern extraction
   - Entry/exit point identification

6. **UI Workflow Mapping:**
   - Screen navigation flows
   - User journey mapping
   - Screen-to-operation correlation

**Nice-to-Have (for advanced refactoring):**

7. **Domain Model Metadata:**
   - Entity vs DTO vs value object classification
   - Relationship identification
   - Business rule extraction

8. **Service Layer Suggestions:**
   - Automatic service boundary identification
   - Dependency injection requirements
   - Service method grouping

---

### 9. Summary: AST Technical Richness Assessment

#### 8.1 Current Capabilities

**Strengths:**
- ✅ Complete file inventory (all PF/LF/DSPF members)
- ✅ Rich symbol table (variables, DS, files, indicators, DSPF symbols)
- ✅ Structured DB contracts (complete column definitions)
- ✅ Operation metadata (`opcode`, `target`, `keys`)
- ✅ Stable IDs, paths, hashes for versioning
- ✅ DSPF symbols exist (`sym.dspf.*`)

**Gaps:**
- ❌ No file type classification
- ❌ No display format definitions
- ❌ No operation format information
- ❌ No cross-file relationships
- ❌ No indicator semantics
- ❌ No field-to-DB bindings

#### 8.2 Net Assessment

- **AST Richness for DB Migration: HIGH** ✅
  - Complete file inventory + structured `dbContracts` enables 100% column mapping
  - DDS parsing fills remaining gaps (file type, relationships)

- **AST Richness for DSPF Migration: MEDIUM** ⚠️
  - Excellent discovery (`sym.dspf.*`, `files` section)
  - Requires DDS parser + metadata layer to reach HIGH
  - Foundation is solid; complementary parsing needed

#### 9.3 Required Enhancements Priority

**Must-Have (for full DSPF support):**
1. File type classification (enhance AST or parse DDS)
2. DDS parser for display formats
3. Operation format extraction (enhance AST or parse RPG source)
4. Indicator semantics extraction (parse DDS + analyze RPG)

**Must-Have (for application-centric migration):**
5. Cross-program call metadata (program dependencies, parameters)
6. Transaction boundary identification (COMMIT/ROLLBACK markers)
7. Data access pattern recognition (entity identification, CRUD patterns)

**Should-Have (for enhanced migration):**
8. Cross-file relationship metadata
9. Enhanced operation result metadata
10. Field-to-DB binding extraction
11. Application structure metadata (program grouping, aggregates)
12. Use case identification (business operation grouping)
13. UI workflow mapping (screen navigation flows)

**Nice-to-Have (for advanced refactoring):**
14. Domain model metadata (entity classification, relationships)
15. Service layer suggestions (automatic boundary identification)

---

### 10. Conclusion

The new ASTs provide a **strong technical foundation** for:
- ✅ **DB Migration:** Complete file inventory + structured `dbContracts` enables 100% column mapping
- ✅ **UI Discovery:** DSPF symbols and file inventory enable display file identification
- ⚠️ **DSPF Migration:** Requires DDS parser + metadata layer to reach HIGH richness
- ⚠️ **Application-Centric Migration:** Requires cross-program analysis + domain modeling layer

**Key Gaps:**
- **Program-level:** Missing file type classification, display formats, operation formats
- **Application-level:** Missing cross-program dependencies, transaction boundaries, service boundaries

**Path Forward:**
1. **Immediate:** Build DDS parser and metadata extraction layer for DSPF support
2. **Short-term:** Build call graph analyzer and domain model extractor for application structure
3. **Long-term:** Request AST vendor enhancements for application-level metadata

The ASTs are **excellent for program-level migration** but require **complementary analysis layers** for application-centric, architecture-aware migration to pure Java.
