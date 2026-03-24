# Pipeline Resilience & Call Graph Enhancement – Advisory

This document addresses two concerns for deployment-ready pipelines:

1. **Deployment resilience**: How clients can run the pipeline without manual intervention when schema conflicts or runtime errors occur.
2. **Call graph in prompt context**: How application-level understanding (call flows, dependencies) can improve migration quality.

---

## Part 1: Pipeline Resilience for Deployment

### Current State

The pipeline already has several auto-fix layers:

| Layer | Script | Triggers |
|-------|--------|----------|
| Build | `fix_idclass` | IdClass mismatch |
| Build | `fix_ambiguous_mapping` | Duplicate MVC endpoints |
| Build | `fix_compile_errors.py` | LLM-based compile fixes |
| Build | `fix_test_alignment` | Test failures |
| Runtime | `fix_runtime_errors.py` | IdClass, Ambiguous mapping |

**Gaps identified** (from recent "No claims found" issue):

- **Entity–table schema conflicts**: Multiple entities (Invoice, Hsahklf3; ClaimError, Hsg73pf) map to the same table with different column schemas → `NULL not allowed for column "AHK000"`.

- **DataInitializer failures**: Application starts but demo data is empty.

- **No post-run validation**: Pipeline does not verify that the demo returns expected data.

### Recommended Remediation (Inside the Pipeline)

#### 1. Extend `fix_runtime_errors.py` with Data-Integrity Patterns

Add detection and handling for:

- `NULL not allowed for column "AHK000"` → apply `fix_data_initializer_seed_only.py` (or equivalent)
- `NULL not allowed for column "G73000"` → same pattern

**Fix script** (e.g. `fix_data_initializer.py`):

- Ensures `DataInitializer` seeds only claims (no Invoice/HSAHKLF3).
- Wraps ClaimError seeding in try/catch so failures are non-fatal.

This keeps the fix in the pipeline, not in user code.

#### 2. Add `fix_entity_table_conflicts.py` (Post-Migration)

Run after migration, before build:

- Scan `@Table(name = "X")` across entities.
- Detect multiple entities mapping to the same table with different schemas.
- Apply one of:
  - **Option A**: Rename demo-only entities (e.g. `Invoice` → `@Table(name = "INVOICE_DEMO") for demo use`).
  - **Option B**: Exclude conflicting entities from persistence (e.g. `@Entity` + `@Table` + `@MappedSuperclass` or `@SecondaryTable`).
  - **Option C**: Use a single canonical entity per table and add a note in the migration prompt to avoid duplicates.

#### 3. Post-Run Validation Step

After "Run Application":

1. Wait for app to be ready (e.g. 60 s).
2. Call `POST /api/claims/search` with `{"companyCode":"001"}`.
3. If response is `[]` and `DataInitializer` ran in logs:
   - Run `fix_data_initializer.py` (or equivalent).
   - Rebuild and restart.
   - Retry validation once.

This gives the pipeline a self-healing loop.

#### 4. Kill Stale Process Before Run

Before starting the app:

- Kill any process on port 8081, or
- Use a different port when `RUN_PROFILE` is set.

This avoids "port already in use" and ensures a clean run.

#### 5. Pipeline Step Order (Recommended)

```
1. Build Global Context
2. Migrate Feature
3. fix_entity_table_conflicts.py (new)
4. Build Application (with existing fix chain)
5. [Optional] fix_data_initializer.py if DataInitializer detected
6. Run Application
7. Post-run validation (call /api/claims/search)
8. If validation fails → apply fix_data_initializer → retry
```

### Summary: Deployment Resilience

| Action | Effort | Impact |
|--------|--------|--------|
| Add Data-Integrity patterns to `fix_runtime_errors.py` | Low | High |
| Add `fix_data_initializer.py` | Low | High |
| Add `fix_entity_table_conflicts.py` | Medium | High |
| Add post-run validation | Low | Medium |
| Kill stale process before run | Low | Medium |

---

## Part 2: Call Graph in Prompt Context

### Why Add a Call Graph?

- **Entity consolidation**: Avoid multiple entities for the same table (e.g. Invoice vs Hsahklf3).
- **Dependency flow**: Clarify which procedures read/write which tables.
- **Entry points**: Identify main flows vs subroutines.
- **Schema alignment**: Pick the schema used by the main entry path.

### Current Context Structure

`context_index/*.json` typically includes:

- `astNode` (id, kind, outgoingEdges)
- `dbContracts`
- `displayFiles`
- `rpgSnippet`
- `statementNodes`
- `symbolMetadata`

`outgoingEdges` are file/variable references, not call relationships.

### Call Graph Definition

A call graph for RPG typically includes:

- **Procedure calls**: `CALL`, `EXSR`, `EXTRN` (external subroutine).
- **File operations**: `CHAIN`, `READ`, `READE`, `SETLL`, `SETGT`, `WRITE`, `UPDATE`, `DELETE`.
- **Program flow**: `DOW`, `DOU`, `IF`, `SELECT`.

### Implementation Options

#### Option A: Build Call Graph During Global Context Build

**Where**: In the pipeline that builds `context_index` (e.g. AST parsing).

**Output**:

```json
{
  "callGraph": {
    "nodes": [
      {"id": "n404", "name": "HS1210", "kind": "Subroutine", "range": [1, 500]},
      {"id": "n1824", "name": "SR_FILART", "kind": "Subroutine", "range": [501, 600]}
    ],
    "edges": [
      {"from": "n404", "to": "n1824", "type": "EXSR"},
      {"from": "n404", "to": "HSG71LF2", "type": "CHAIN"},
      {"from": "n404", "to": "HSG73PF", "type": "READ"}
    ],
    "tableAccess": {
      "HSG71LF2": ["n404", "n1824"],
      "HSG73PF": ["n404"],
      "HSAHKLF3": ["n404"]
    }
  }
}
```

**Include in prompt**:

- `tableAccess` → which entities are used by which nodes.
- `edges` → call hierarchy.

#### Option B: Lightweight Parsing in `migrate_to_pure_java.py`

**Where**: Parse RPG snippet in `build_pure_java_prompt()`.

**Process**:

1. Extract `CALL`, `EXSR`, `CHAIN`, `READ`, `WRITE`, etc.
2. Build a minimal `tableAccess` map.
3. Add a short section to the prompt, e.g.:

```
--- APPLICATION DATA FLOW ---
Tables used by this node: HSG71LF2 (CHAIN), HSG73PF (CHAIN, READ), HSAHKLF3 (CHAIN)
Subroutines called: SR_FILART, SR_MINIMUM
--- END ---
```

#### Option C: Use Existing `outgoingEdges` More Explicitly

- `outgoingEdges` already lists `sym.file.HSG71LF2`, etc.
- Add a `callGraph` section that:
  - Groups `sym.file.*` by table.
  - Adds `sym.procedure.*` or `sym.subroutine.*` if available.
  - Formats this for the prompt.

### Recommended Prompt Format

```
--- APPLICATION CALL GRAPH (for entity consolidation) ---
Primary tables used by this node:
  - HSG71LF2 (Claim): CHAIN, READ, UPDATE
  - HSG73PF (ClaimError): CHAIN, READ
  - HSAHKLF3 (Invoice): CHAIN
Subroutines invoked: SR_FILART, SR_MINIMUM
Canonical entity per table: Use Claim for HSG71LF2/HSG71PF; use ClaimError for HSG73PF.
Avoid creating duplicate entities (Invoice, Hsahklf3) for the same table.
--- END ---
```

### Implementation Phases

| Phase | Scope | Effort |
|-------|-------|--------|
| 1 | Parse `sym.file.*` from `outgoingEdges` → format per-node table usage | Low |
| 2 | Add `tableAccess` to context JSON during global context build | Medium |
| 3 | Full call graph (CALL, EXSR) in AST | Medium–High |
| 4 | Use call graph in migration prompt for entity consolidation | Low |

### Migration Prompt Enhancement

Add to `_build_static_system_prompt()` or `build_pure_java_prompt()`:

```
**Entity consolidation (from call graph):**
- Each physical table (e.g. HSG73PF, HSAHKLF3) should have ONE canonical JPA entity.
- If multiple entities map to the same table, consolidate to the one used by the primary entry path.
- For demo/seed data, use the canonical entity; avoid creating entities that conflict with schema.
```

---

## Summary

| Concern | Recommendation |
|---------|----------------|
| **Deployment resilience** | Add `fix_data_initializer.py`, extend `fix_runtime_errors.py` for Data-Integrity patterns, add `fix_entity_table_conflicts.py`, add post-run validation, and kill stale processes before run. |
| **Call graph** | Start with `outgoingEdges` and `tableAccess`; add a formatted section to the migration prompt; later extend to full call graph in the AST pipeline. |

Both changes are pipeline-only and do not require client or user intervention.
