# Current Pipeline Improvements — PoC Delivery Focus

**Version:** 1.0  
**Date:** March 2026  
**Goal:** Deliver PoC with feature-wise migration and successful build. Fill current shortcomings with improved call graph and logic migration/merge.

**Related:** `MIGRATION_ORCHESTRATOR_PLAN.md` is the future extension plan (orchestrator, auto-entry, etc.). This document focuses on **incremental improvements to the existing pipeline**.

---

## 1. Current Shortcomings

| Shortcoming | Impact | Root Cause |
|-------------|--------|------------|
| **Sparse call graph** | Migrating n404 includes only n404; n1779 (CreateClaim), n1983 (CheckV4) never in slice | `build_call_graph_with_rpg.py` resolves EXSR → Subroutine only; Procedures have `calleeNodeId: null` |
| **Missing validation logic** | ClaimCreationService lacks V4 checks, ClaimError creation on rejection | Linked nodes (n1983, n1779) not migrated; logic lives in those nodes |
| **No integration between nodes** | Even if n1983 migrated separately, ClaimCreationService doesn't call V4ValidationService | No prompt guidance to use existing services; no merge step |

---

## 2. Proposed Improvements (Incremental)

### 2.1 Call Graph Enhancement (Priority 1)

**File:** `global_context/build_call_graph_with_rpg.py`

**Change:** Add **Procedure index** and resolve EXSR/CALLP to Procedures.

| Step | Action |
|------|--------|
| 1 | Build `procedure_name (upper) → nodeId` from `programs/*.program.json` for nodes with `kind == "Procedure"` and `procedureName` or `name` |
| 2 | For each call: if `opcode == "EXSR"` and Subroutine index misses, try Procedure index |
| 3 | For each call: if `opcode in ("CALLP", "CALL")`, try Procedure index |
| 4 | Set `calleeNodeId` when match found |

**Result:** When user migrates n404, BFS will include n1779 (CreateClaim) and n1983 (CheckV4) if n404 calls them. The **existing** migrate-feature flow (BFS + topological sort + per-node migration) will then migrate all three in order.

**Effort:** 1–2 days  
**Risk:** Low — additive change, no breaking changes.

---

### 2.2 Ensure RPG Source Is Loaded for Logic (Priority 2)

**File:** `migrate_to_pure_java.py` / `ui_global_context_server.py`

**Current:** `--rpg-file` is passed when `rpgDir` is provided. The LLM gets RPG snippet from context for logic mapping.

**Check:** Ensure that when migrating each node, the RPG source for that node's range is included in the prompt. Large nodes (n404) may truncate. Consider:
- Splitting very large nodes (future)
- For now: ensure `rpgSnippet` in context has the key validation branches (n1983, n1779 logic)

**Action:** Verify `build_context_index.py` includes sufficient RPG for n1983, n1779. If n1983 context is small (~24 lines per migration manifest), it should be fine. Add a validation: "RPG lines included ≥ N" for logic-heavy nodes.

**Effort:** 0.5 day (verification + small fixes)

---

### 2.3 Prompt: Existing Services and Integration (Priority 3)

**File:** `migrate_to_pure_java.py` (prompt builder)

**Change:** When migrating a node that is **not** the first in the slice, include in the prompt:

```
## Existing services in project (USE THEM — do not duplicate)
- V4ValidationService.validate(criteria) — call before creating claim when V4 split
- ClaimErrorRepository.save(ClaimError) — use when recording validation failures
```

**How:** The migrate-feature API runs one `migrate_to_pure_java.py` per context file. It does **not** currently pass "which nodes are already done" or "existing services". We need to:
- Before each migration run, scan `warranty_demo/src/main/java` for `@Service` classes.
- Pass `--existing-services` or inject into the prompt: "Existing services: X, Y. Generate code that calls them where appropriate."

**Effort:** 1 day (scan + prompt injection)

---

### 2.4 Entity Consolidation (Already in Place)

**Current:** `TABLE_TO_CANONICAL_ENTITY`, `_deduplicate_entities_by_table`, `existing_entities` in prompt.

**Action:** Ensure `ClaimError` (not `ClaimFailure`) is used for HSG73PF. Add any missing tables from n1983 (HSAHKPF, HSEPAF) to canonical mapping if they appear.

**Effort:** 0.5 day (add mappings as needed)

---

## 3. Implementation Order

| # | Improvement | Effort | Dependency |
|---|-------------|--------|------------|
| 1 | Call graph: Procedure index | 1–2 days | None |
| 2 | Verify RPG source in context for n1983, n1779 | 0.5 day | None |
| 3 | Prompt: existing services injection | 1 day | #1 (need slice to know order) |
| 4 | Entity consolidation (HSAHKPF, HSEPAF if needed) | 0.5 day | After first n1983 migration |

**Total:** ~3–4 days for PoC delivery improvements.

---

## 4. Success Criteria (PoC)

- [x] Run Build Global Context (with RPG dir) → `call_graph_enriched.json` has `calleeNodeId` for CreateClaim, CheckV4
- [ ] Migrate Feature: HS1210, n404 → slice includes n1983, n404 (n1779 in n1 slice; n1→createClaim, n404→checkV4)
- [ ] Migration produces: V4ValidationService (or equivalent), ClaimCreationService with validation, ClaimError creation on rejection
- [ ] Build Application succeeds (Tab 4)
- [ ] Run & Demo: claim creation works; validation failures produce ClaimError records

---

## 5. Out of Scope (For Now)

- Migration orchestrator (state machine, resume, etc.)
- Automatic entry point (user still selects Program + Entry Node)
- "Building feature X" UI (keep current progress text)
- Cross-program migration

These remain in `MIGRATION_ORCHESTRATOR_PLAN.md` for future scaling.
