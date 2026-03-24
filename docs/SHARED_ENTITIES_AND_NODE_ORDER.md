# Shared Entities and Node Migration Order

This document describes how the migration pipeline handles shared entities across nodes and the dependency order of node migrations.

## Problem

When migrating multiple RPG nodes into a single Java application:

1. **Shared tables**: Multiple nodes may use the same physical table (e.g. HSAHKLF3, HSG71LF2). If each node migration generates its own entity for the same table, we get duplicates (Invoice, InvoiceHeader, Hsahklf3 for HSAHKLF3).

2. **Node order**: If node n404 is typically migrated first and creates shared entities, migrating another node before n404 may fail because:
   - Table mappings or column definitions may not exist yet
   - Dependencies between nodes (call graph) require a specific order

3. **Inter-node dependencies**: Some nodes call others; shared entities must exist before dependent code references them.

## Current Strategy

### 1. Migration Order (Derived from Call Graph Dependencies)

The `ui_global_context_server.py` migrate-feature flow derives migration order from the call graph:

1. **BFS from entry node** to collect the full slice of reachable nodes.
2. **Topological sort** so that **callees are migrated before callers** (dependency order).

```
nodes_set = BFS(entry_node_id, adjacency)  # adjacency: caller -> [callee]
reverse_adjacency = callee -> [caller]
nodes_in_slice = topological_sort(reverse_adjacency)  # callees first
```

- If A calls B, B (callee) is migrated before A (caller), so B's entities and services exist when A references them.
- The **entry node** (e.g. n404 for HS1210) is chosen by the user in the UI.
- If the call graph has cycles or sparse edges, any remaining nodes are appended in sorted order.

### 2. One Entity Per Physical Table

The pipeline enforces **one entity per physical table**:

| Mechanism | Location | Purpose |
|-----------|----------|---------|
| `consolidate_db_contracts_by_table()` | `migrate_to_pure_java.py` | Merges multiple dbContracts for the same table into one (union of columns) |
| `TABLE_TO_CANONICAL_ENTITY` | `migrate_to_pure_java.py` | Maps table → canonical entity name (e.g. HSAHKLF3 → Invoice) |
| `_deduplicate_entities_by_table()` | `migrate_to_pure_java.py` | Post-parse: drops duplicate entity files, keeps canonical |
| `_get_existing_entities_from_project()` | `migrate_to_pure_java.py` | Scans target project for existing entities |
| `existing_entities` in prompt | `build_pure_java_prompt()` | Tells LLM which entities already exist; do not regenerate |

### 3. Shared Entity Flow

When migrating **node B** after **node A**:

1. **Before migration**: `_get_existing_entities_from_project(target_root)` scans `warranty_demo/src/main/java` for `@Entity` classes and their `@Table(name="X")` mappings.

2. **Prompt**: The prompt includes:
   ```
   ## Existing entities in project (REUSE - do NOT regenerate)
   - **HSAHKLF3** → use existing `Invoice` (do not create Invoice.java again)
   - **HSG71LF2** → use existing `Claim` (do not create Claim.java again)
   ```

3. **LLM**: The LLM is instructed to use these entities in repositories, services, and DTOs—and **not** to generate new entity classes for these tables.

4. **Post-parse**: If the LLM still outputs duplicate entities (e.g. Hsahklf3.java for HSAHKLF3), `_deduplicate_entities_by_table()` removes them and keeps only the canonical one (Invoice).

### 4. Canonical Entity Mapping

| Physical Table | Canonical Entity | Notes |
|----------------|------------------|-------|
| HSAHKLF3 | Invoice | Invoice header; not InvoiceHeader, not Hsahklf3 |
| HSG71LF2 | Claim | Claim line |
| HSG71PF | Claim | Same domain |
| HSG73PF | ClaimError | Claim failure/error |
| HSG70F | ClaimHeader | Claim header |

Add new mappings to `TABLE_TO_CANONICAL_ENTITY` in `migrate_to_pure_java.py` as you discover shared tables.

### 5. First-Node Migration (e.g. n404)

When migrating the **first** node (e.g. n404):

- `existing_entities` is empty (project is clean or freshly cleaned).
- The LLM generates all entities for that node's dbContracts.
- `consolidate_db_contracts_by_table()` ensures one contract per table.
- `TABLE_TO_CANONICAL_ENTITY` guides the LLM to use `Invoice` for HSAHKLF3, not Hsahklf3.

### 6. Cleaning Before Fresh Migration

To avoid conflicts between old and new migrations, run:

```bash
python3 clean_migration_targets.py
```

This removes:
- `warranty_demo`: generated Java (domain, repository, service, dto, web, config), built UI, ui-schemas, target, data
- `warranty-ui`: dist/

Then run migrate-feature from the UI or:

```bash
python3 migrate_to_pure_java.py context_index/HS1210_n404.json --target-project warranty_demo
```

## Recommendations

1. **Migrate entry node first**: Use the main subroutine/procedure (e.g. n404) as the entry node so shared entities are created first.

2. **Clean before re-migration**: If you change node order or re-run migrations, clean first with `clean_migration_targets.py`.

3. **Extend canonical mapping**: When new shared tables appear, add them to `TABLE_TO_CANONICAL_ENTITY`.

4. **Validate after migration**: Run `validate_migration_output.py` to check entity consolidation:

   ```bash
   python3 validate_migration_output.py context_index/HS1210_n404.json warranty_demo
   ```
