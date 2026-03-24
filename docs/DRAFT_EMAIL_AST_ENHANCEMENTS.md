# Draft Email: AST Enhancement Requests for RPG→Java Migration Pipeline

---

**To:** PKS Systems  
**Subject:** AST Enhancement Requests – Schema & Structure (RPG→Java Migration)

---

Dear PKS Team,

We are using the PKS RPG AST as the primary input for our enterprise-grade RPG→Java migration pipeline. The current AST provides strong coverage for control flow, data model, symbols, and traceability. We have identified a few schema and structure enhancements that would significantly improve our migration quality and reduce manual fixes. All requests are within the AST’s scope of representing RPG program structure and metadata.

---

## 1. Unique Physical Column Names for Duplicate Logical Names

**Source:** DDS (Data Description Specifications) – the schema is defined in DDS, not in RPG. The AST exposes `dbContracts` derived from DDS.

**Issue:** Some DDS-defined physical files have multiple columns with the same logical name (e.g. four columns all named `RESERVE`). In DB2/DDS this is valid, but JPA requires unique `@Column(name)` values. We currently work around this in our pipeline by rewriting duplicate names.

**Request:** When the AST exposes schema from DDS, and that schema has multiple columns sharing the same logical name, could we receive either:
- Unique physical column names (e.g. `RESERVE1`, `RESERVE2`, `RESERVE3`, `RESERVE4`), or
- An ordinal/position indicator so we can derive unique names ourselves?

**Fallback:** If unique names are not yet feasible, could the AST at least indicate when a DDS-defined table has multiple columns sharing the same logical name (e.g. a `hasDuplicateColumnNames` flag per table, or a list of tables/columns with duplicates)? Our pipeline currently detects this heuristically; an explicit indicator would be more reliable.

**Example:** A table with four `RESERVE` columns (DECIMAL(5,2), DECIMAL(9,2), DECIMAL(2,0), DECIMAL(2,0)) – we need a way to distinguish them in the column metadata.

**Sample occurrences** (for PKS experts to identify and make changes):

| AST file | Source unit | Table (DDS member) | Column | Count |
|----------|-------------|--------------------|--------|-------|
| HS1210-ast.json | qsys:HSSRC/QRPGLESRC/HS1210 | AUFWKO | RESERVE | 2 |
| HS1210-ast.json | qsys:HSSRC/QRPGLESRC/HS1210 | AUFWKO | SP | 2 |
| HS1210-ast.json | qsys:HSSRC/QRPGLESRC/HS1210 | HSAHKLF3 | RESERVE | 4 |
| HS1210-ast.json | qsys:HSSRC/QRPGLESRC/HS1210 | HSAHKLF8 | RESERVE | 4 |
| HS1210-ast.json | qsys:HSSRC/QRPGLESRC/HS1210 | HSAHKPF | RESERVE | 4 |
| HS1210-ast.json | qsys:HSSRC/QRPGLESRC/HS1210 | HSAHTPF | RESERVE | 2 |
| HS1210-ast.json | qsys:HSSRC/QRPGLESRC/HS1210 | S3F009 | Outcome Of Claim | 2 |
| HS1210-ast.json | qsys:HSSRC/QRPGLESRC/HS1210 | S3L004H | Owner Address | 2 |
| HS1212-ast.json | qsys:HSSRC/QRPGLESRC/HS1212 | S3F004 | Owner Address | 2 |
| HS1212-ast.json | qsys:HSSRC/QRPGLESRC/HS1212 | S3F101 | Dealer Outcome Desc | 4 |
| HS1212-ast.json | qsys:HSSRC/QRPGLESRC/HS1212 | S3F101 | Reason For Claim L-1 | 2 |
| HS1212-ast.json | qsys:HSSRC/QRPGLESRC/HS1212 | S3F101 | Reason For Claim L-2 | 2 |
| HS1212-ast.json | qsys:HSSRC/QRPGLESRC/HS1212 | S3F101 | Reason For Claim L-3 | 2 |
| HS1212-ast.json | qsys:HSSRC/QRPGLESRC/HS1212 | S3F101 | Reason For Claim L-4 | 2 |
| HS1212-ast.json | qsys:HSSRC/QRPGLESRC/HS1212 | S3F101 | Supplier Labour % | 2 |
| HS1212-ast.json | qsys:HSSRC/QRPGLESRC/HS1212 | S3F101 | Supplier Outcome Desc | 4 |
| HS1212-ast.json | qsys:HSSRC/QRPGLESRC/HS1212 | S3L091A | Page No | 2 |
| HS1212-ast.json | qsys:HSSRC/QRPGLESRC/HS1212 | S3L091AA | Page No | 2 |
| HS1212-ast.json | qsys:HSSRC/QRPGLESRC/HS1212 | S3L091BA | Page No | 2 |
| HS1212-ast.json | qsys:HSSRC/QRPGLESRC/HS1212 | S7F001 | Description | 3 |
| HS1212-ast.json | qsys:HSSRC/QRPGLESRC/HS1212 | S7F003 | Description | 3 |
| HS1212-ast.json | qsys:HSSRC/QRPGLESRC/HS1212 | S7F006 | Unscheduled Description | 3 |
| HS1212-ast.json | qsys:HSSRC/QRPGLESRC/HS1212 | WPCODF | TEXT L | 2 |

*Note:* Line numbers for column definitions are not present in the current AST (`dbContracts.nativeFiles[].columns[]`). Adding `range` or `line` to each column would help PKS experts locate the DDS source (e.g. `HSSRC/QDDSSRC/{table}.MBR`) for physical files.

---

## 2. Key vs Entity Distinction for Physical Files

**Source:** RPG – the program determines how each file is used (CHAIN, READ, ReadE, SetLL, field access). The AST has statement nodes with opcodes; usage can be derived from RPG semantics.

**Issue:** Some physical files have composite keys and are used both for key-based access (CHAIN, SetLL) and full-record reads (ReadE, field access). Our code generator sometimes produces `JpaRepository<KeyClass, String>` instead of `JpaRepository<EntityClass, KeyClass>`, causing Spring Data JPA "Not a managed type" errors. Without an explicit indication of usage, we cannot reliably decide whether to generate a full entity + `JpaRepository<Entity, Key>` or a key-only reference.

**Request:** Could the AST indicate, for each file referenced in a node, whether it is used as:
- A **full record** (read/write with all columns – e.g. CHAIN, READ, ReadE, SetLL+ReadE with field access), or
- A **key-only reference** (e.g. CHAIN for %Found check only, no subsequent field access)?

This could be a simple flag or usage type in the file/contract metadata (e.g. `usage: "full" | "keyOnly"`), or per-statement usage derived from opcodes (CHAIN, READ, ReadE, SetLL, etc.) and data flow.

**Sample occurrences** (for PKS experts to identify and make changes):

| Context file | AST node | Source unit | Lines | Physical file | Usage in RPG | Confusion |
|--------------|----------|-------------|-------|---------------|--------------|-----------|
| HS1210_n1983.json | n1983 (Procedure CheckV4) | qsys:HSSRC/QRPGLESRC/HS1210 | 3035–3058 | HSAHKPF | CHAIN (full record load), then AHK000/AHK040 used | Generator produced `JpaRepository<HsahkpfKey, String>`; should be `JpaRepository<Hsahkpf, HsahkpfKey>` |
| HS1210_n1983.json | n1983 (Procedure CheckV4) | qsys:HSSRC/QRPGLESRC/HS1210 | 3035–3058 | HSEPAF | SetLL + ReadE (full record read), EPA_DATV accessed | Same: Key used as entity type; needs Entity + Key distinction |

*Note:* Both HSAHKPF and HSEPAF have composite keys and are used as full records in this procedure. An AST-level `usage: "full"` (or per-reference usage) would allow our generator to produce the correct `JpaRepository<Entity, Key>` instead of `JpaRepository<Key, ?>`.

---

## 3. Nullable / Default Hints for Columns

**Source:** DDS (schema) – nullable and initial values (`INZ`) are defined in DDS, not in RPG. The AST exposes `dbContracts` derived from DDS.

**Issue:** When generating JPA entities, we sometimes infer `nullable` incorrectly or miss default values, leading to `DataIntegrityViolation` at runtime. We do not have direct access to DDS; we rely on the AST to expose schema metadata.

**Request:** When PKS builds `dbContracts` from DDS, could it include, for each column, where DDS provides them:
- `nullable: true | false` (from DDS / DB schema), and/or
- `default` or `initialValue` (from DDS `INZ` or DB `DEFAULT`)?

**Sample occurrences** (columns where we had to hardcode defaults to avoid `DataIntegrityViolation`):

| Table (DDS member) | Column | Issue | Our workaround |
|--------------------|--------|-------|----------------|
| HSG71LF2 (Claim) | ANHANG | NOT NULL, no default in AST | `claim.setAnhang(" ")` in ClaimCreationService |
| HSAHKLF3 (Invoice) | SPLITT | Semantic default `"04"` for warranty | `SEED_SPLITT = "04"` in TestDataFactory; hardcoded in repository queries |

---

## Summary

| # | Enhancement | Priority |
|---|-------------|----------|
| 1 | Unique physical column names for duplicates | High |
| 2 | Key vs entity distinction for files | High |
| 3 | Nullable / default hints for columns | Medium |

We are happy to provide sample AST files or discuss any of these in more detail. Thank you for your support.

Best regards,  
[Your name]  
[Your organisation]
