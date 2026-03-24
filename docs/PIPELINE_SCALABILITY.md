# Pipeline Scalability — Challenges and Mitigation Strategy

**Version:** 1.0  
**Date:** March 2026  
**Audience:** Technical leads, stakeholders, migration team

This document describes **challenges in scaling** the RPG-to-Java migration pipeline beyond the current PoC (HS1210, single program) and **our strategy to mitigate** them.

---

## 1. Executive Summary

The current PoC successfully migrates **HS1210** (Warranty Claim Processing) to a Spring Boot application. Scaling to **many modules** (HS1212, HS1213, …), **many nodes per program**, and **enterprise-wide migration** introduces significant challenges. This document identifies those challenges and outlines our mitigation strategy.

| Challenge Area | Key Risk | Mitigation |
|----------------|----------|------------|
| **Call graph** | Incomplete slice → missing logic | Procedure index; EXSR/CALLP resolution |
| **Node integration** | Migrated nodes don't call each other | Existing-services prompt injection |
| **Shared state** | Globals, copybooks, cross-node variables | Program-level context; API/persistence |
| **Cross-program** | HS1212 calling HS1210 not modeled | Cross-program call graph (future) |
| **Performance** | Large nodes (23k lines) → slow LLM | Node splitting; prompt trimming |
| **Entity conflicts** | Duplicate entities across nodes | TABLE_TO_CANONICAL_ENTITY; deduplication |
| **Runtime errors** | JPA, Spring context failures post-build | Build gate; validate_before_run |

---

## 2. Scalability Challenges

### 2.1 Call Graph Completeness

**Challenge:** The call graph resolves `EXSR` → Subroutine (BEGSR) only. **Procedure calls** (EXSR/CALLP to Procedures like CreateClaim, CheckV4) have `calleeNodeId: null`. As a result:

- BFS from entry node (e.g. n404) yields **only the entry node**
- Linked nodes (n1779 CreateClaim, n1983 CheckV4) are **never in the slice**
- Migrated code lacks validation logic, ClaimError creation, V4 checks

**Impact:** Each migrated feature is **incomplete**. Manual integration is required.

---

### 2.2 Node Integration and Service Reuse

**Challenge:** Even when multiple nodes are migrated (manually or via improved slice), the LLM generates **standalone code**. ClaimCreationService does not call V4ValidationService; each node is migrated in isolation.

**Impact:** No automatic wiring between migrated nodes. Logic that spans nodes (e.g. "validate before create") is missing or duplicated.

---

### 2.3 Shared State and Global View

**Challenge:** RPG programs use **shared variables** (e.g. `SR06`, `KEYG71`, `STATUS`) and **copybooks** at program level. Each node's context only contains **in-node symbols**. There is no:

- Program-level symbol table (which variables are shared)
- Copybook / include content in context
- Explicit "shared state → API or persistence" mapping

**Impact:** When migrating multiple nodes of the same program (or across programs), shared state becomes undefined. Risk of inconsistent behavior or duplicate definitions.

---

### 2.4 Cross-Program Dependencies

**Challenge:** Programs call each other (e.g. HS1212 calling HS1210). The call graph is **per-program**. Cross-program calls are not modeled.

**Impact:** Migrating HS1212 in isolation misses HS1210 dependencies. Integration must be done manually.

---

### 2.5 Performance and Large Nodes

**Challenge:** Large nodes (e.g. n404 ~23k lines, 87KB rpgSnippet) produce **huge prompts** (~100–200KB). LLM processing is slow; context truncation may drop critical logic.

**Impact:** Long migration times (15–25 min per node); risk of incomplete logic for very large nodes.

---

### 2.6 Entity and Schema Conflicts

**Challenge:** Multiple nodes (or programs) use the same physical files (HSG71LF2, HSAHKLF3). Without canonical mapping, the LLM may generate **duplicate or conflicting entities** (e.g. Claim vs. ClaimHeader, different column names).

**Impact:** Build failures, JPA "duplicate column" errors, inconsistent schema.

---

### 2.7 Runtime and Dynamic Errors

**Challenge:** Code compiles but **fails at runtime** (Spring ApplicationContext, JPA, duplicate column, "Not a managed type"). These surface only when running `mvn test` or the application.

**Impact:** Build appears successful; Run & Demo fails. Late discovery of integration issues.

---

## 3. Mitigation Strategy

### 3.1 Call Graph Enhancement (Priority 1)

**Strategy:** Add **Procedure index** and resolve EXSR/CALLP to Procedures.

| Action | Description |
|--------|-------------|
| Build Procedure index | `procedureName (upper) → nodeId` from `programs/*.program.json` |
| Extend callee resolution | For EXSR: try Subroutine index, then Procedure index |
| Extend for CALLP/CALL | Resolve to Procedure index |
| Output | `call_graph_enriched.json` with `calleeNodeId` populated |

**Result:** BFS from n404 includes n1779, n1983. Full feature slice is migrated in dependency order.

**Status:** Planned (see `CURRENT_PIPELINE_IMPROVEMENTS.md`). Effort: 1–2 days.

---

### 3.2 Existing-Services Prompt Injection (Priority 2)

**Strategy:** When migrating a node that is **not** the first in the slice, inject into the prompt:

```
## Existing services in project (USE THEM — do not duplicate)
- V4ValidationService.validate(criteria)
- ClaimErrorRepository.save(ClaimError)
```

| Action | Description |
|--------|-------------|
| Scan target project | Before each migration run, list `@Service` classes |
| Pass to LLM | `--existing-services` or prompt appendix |
| Instruct | "Generate code that calls them where appropriate" |

**Result:** ClaimCreationService calls V4ValidationService; no duplicate logic.

**Status:** Planned. Effort: ~1 day.

---

### 3.3 Program-Level Context and Shared State (Priority 3)

**Strategy:** Build **program-level context** (shared symbols, copybooks) and pass it as an appendix when migrating any node of that program.

| Action | Description |
|--------|-------------|
| Program-level symbol table | Variables referenced by >1 node; copybook content |
| Shared state → API/DB | Don't migrate globals as process memory; use persistence or explicit DTOs |
| Shared domain library | One entity set per table; all nodes depend on it |

**Result:** Shared variables become explicit APIs or persisted state. No undefined globals.

**Status:** Design in `ENTERPRISE_MIGRATION_DESIGN.md`. Implementation: future phase.

---

### 3.4 Cross-Program Call Graph (Future)

**Strategy:** Extend call graph to resolve **cross-program** calls. Add `calleeProgramId` for calls to other programs.

| Action | Description |
|--------|-------------|
| Cross-program index | Procedure name → (programId, nodeId) |
| Slice across programs | BFS includes nodes from HS1210 and HS1212 |
| Migration order | Callees first, including cross-program |

**Result:** HS1212 migration automatically includes HS1210 dependencies.

**Status:** Future. Prerequisite: single-program call graph complete.

---

### 3.5 Node Splitting and Prompt Optimization

**Strategy:** For very large nodes, reduce prompt size and improve reliability.

| Action | Description |
|--------|-------------|
| Node splitting | Split n404 into logical sub-nodes (future) |
| Prompt trimming | Summarize `statementNodes` / `lineToNodeMap` for huge nodes |
| Smaller nodes first | Migrate n2020 before n404; build up context |
| `--no-inline-origin` | Skip traceability injection (saves 1–3 min) |

**Result:** Faster migrations; less truncation; more complete logic.

**Status:** `--no-inline-origin` applied. Further optimizations in `MIGRATION_PERFORMANCE_NOTES.md`.

---

### 3.6 Entity Consolidation and Deduplication

**Strategy:** Single source of truth for table → entity mapping.

| Action | Description |
|--------|-------------|
| TABLE_TO_CANONICAL_ENTITY | Map HSG71LF2 → Claim, HSAHKLF3 → Invoice, etc. |
| Deduplicate on merge | `_deduplicate_entities_by_table` when merging nodes |
| Duplicate column fix | `resolve_duplicate_column_names()` before LLM prompt |

**Result:** No duplicate entities; consistent schema across nodes and programs.

**Status:** In place. Extend as new tables appear.

---

### 3.7 Build Gate and Runtime Validation

**Strategy:** Catch runtime errors **before** Run & Demo.

| Action | Description |
|--------|-------------|
| Build gate | Tab 4 fails if `mvn test` fails (not just compile) |
| validate_before_run.py | Run after migration; detect known error patterns |
| LLM autofix for runtime | Extend fix_compile_errors.py to handle "Not a managed type", etc. (future) |

**Result:** Run & Demo only viable after successful build + tests.

**Status:** Build gate and validate_before_run in place. See `DYNAMIC_ISSUES_MITIGATION.md`.

---

## 4. Implementation Roadmap

| Phase | Focus | Deliverables |
|-------|-------|--------------|
| **PoC (current)** | HS1210 single-node migration, build, demo | warranty_demo running; manual integration where needed |
| **Phase 1** | Call graph + existing-services | Full slice (n404, n1779, n1983); integrated ClaimCreationService |
| **Phase 2** | Program-level context | Shared state handling; copybook support |
| **Phase 3** | Orchestrator | Auto-entry; migration state; "Building feature X" UI |
| **Phase 4** | Cross-program | HS1212 + HS1210 in one migration run |
| **Phase 5** | Enterprise | Shared domain library; multi-program monorepo |

---

## 5. Risk Summary

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Procedure names don't match AST | Medium | Logging; fuzzy match; manual mapping file |
| Large slice → very long migration | Medium | Resume from migration_state; cancel/resume |
| Entity conflicts when merging | Low | Strengthen deduplication; TABLE_TO_CANONICAL_ENTITY |
| LLM ignores existing services | Medium | Strong prompt wording; post-migration validation |

---

## 6. Related Documentation

| Document | Description |
|----------|-------------|
| `CURRENT_PIPELINE_IMPROVEMENTS.md` | Incremental PoC improvements |
| `MIGRATION_ORCHESTRATOR_PLAN.md` | Orchestrator, auto-entry, migration state |
| `ENTERPRISE_MIGRATION_DESIGN.md` | Shared state, program-level context |
| `DYNAMIC_ISSUES_MITIGATION.md` | Runtime error prevention |
| `MIGRATION_PERFORMANCE_NOTES.md` | Performance optimizations |

---

*For questions on scalability challenges and mitigation, contact the migration team.*
