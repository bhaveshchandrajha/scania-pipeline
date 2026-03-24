# Migration Orchestrator Plan — Extension / Scaling (Future)

**Version:** 1.0  
**Date:** March 2026  
**Status:** Extension plan for future scaling. **Current focus:** See `CURRENT_PIPELINE_IMPROVEMENTS.md` for PoC delivery.

---

## 1. Executive Summary

This plan describes enhancements to the migration pipeline to:

1. **Improve the call graph** so Procedure/Subroutine calls (EXSR, CALLP) are fully resolved to node IDs.
2. **Introduce an orchestrator** that drives BFS-style migration across linked nodes, one node per LLM call.
3. **Auto-identify entry points** and their dependency graph.
4. **Track application-wide migration state** and show "Building feature X" in the UI.
5. **Eliminate manual integration** — the orchestrator ensures all linked nodes are migrated and merged in dependency order.

---

## 2. Current State (Brief)

| Component | Current Behavior | Gap |
|-----------|------------------|-----|
| **Call graph** | `build_call_graph_with_rpg.py` resolves EXSR → Subroutine (BEGSR) only. Procedures (CreateClaim, CheckV4) are not mapped. | Procedures and some EXSR targets have `calleeNodeId: null` |
| **Migration** | (New) Automatic entry point per program. BFS from entry, topological sort, one LLM call per node. | If call graph is sparse, BFS yields only entry node; linked nodes (n1779, n1983) are never included |
| **LLM** | One prompt per context file (per node). Large nodes (n404) = one huge prompt. | No orchestration across nodes; no "feature" concept |
| **UI** | Progress: "Migrating 1/3: HS1210_n404.json..." | No "Building feature: Claim Creation with V4 Validation" |

---

## 3. Proposed Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                     MIGRATION ORCHESTRATOR                                        │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  Phase 1: Call Graph Enhancement                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐   │
│  │ build_call_graph_with_rpg.py (enhanced)                                   │   │
│  │  • Resolve EXSR → Subroutine (existing)                                    │   │
│  │  • Resolve EXSR/CALLP → Procedure (NEW: procedureName → nodeId)             │   │
│  │  • Output: call_graph_enriched.json with calleeNodeId populated            │   │
│  └──────────────────────────────────────────────────────────────────────────┘   │
│                                      │                                           │
│  Phase 2: Entry Point & Slice Discovery                                          │
│  ┌──────────────────────────────────────────────────────────────────────────┐   │
│  │ MigrationOrchestrator                                                      │   │
│  │  • Identify entry node automatically (heuristic: largest subroutine, etc.)   │   │
│  │  • BFS from entry over enriched call graph → full node slice               │   │
│  │  • Topological sort (callees before callers) → migration order             │   │
│  │  • Persist: migration_state.json (programId, entryNodeId, slice, status)   │   │
│  └──────────────────────────────────────────────────────────────────────────┘   │
│                                      │                                           │
│  Phase 3: Per-Node Migration Loop                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐   │
│  │ FOR each node in migration_order:                                         │   │
│  │   1. Load global context (db_registry, existing_entities, migration_state) │   │
│  │   2. Load node context (context_index/HS1210_<node>.json)                  │   │
│  │   3. LLM call: generate Java for this node only                           │   │
│  │   4. Parse output, merge into warranty_demo (dedupe entities)             │   │
│  │   5. Update migration_state (node X done)                                  │   │
│  │   6. Stream progress to UI: "Building feature: <feature_label> (node X)"   │   │
│  └──────────────────────────────────────────────────────────────────────────┘   │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Detailed Plan

### 4.1 Phase 1: Call Graph Enhancement

**Goal:** Resolve `calleeNodeId` for Procedure calls (e.g. CreateClaim → n1779, CheckV4 → n1983).

**Changes to `build_call_graph_with_rpg.py`:**

1. **Procedure index** (new): Build `procedureName (upper) → nodeId` from `programs/*.program.json` for nodes with `kind == "Procedure"` and `procedureName` or `name`.
2. **Callee resolution** (extend): For each call:
   - If `opcode == "EXSR"`: try Subroutine index (existing), then Procedure index (new).
   - If `opcode in ("CALLP", "CALL")`: try Procedure index.
3. **Cross-program calls** (optional, later): If callee not found in same program, check other programs' procedure index (e.g. HS1212 calling HS1210).

**Output:** `call_graph_enriched.json` with `calleeNodeId` populated for CreateClaim, CheckV4, etc.

**Validation:** After Build Global Context, run a small script to report: "X calls have calleeNodeId, Y calls still null."

---

### 4.2 Phase 2: Entry Point & Slice Discovery

**Goal:** Automatically identify the entry node and the full slice of linked nodes.

**Entry point identification: Automatic (no user selection)**

| Option | Description | Pros | Cons |
|--------|--------------|------|------|
| **A. Heuristic** | Largest Subroutine by line count; or node with `procedureName` matching program (e.g. main entry). | Fully automatic. | May need tuning per program. |
| **B. Config override** | `migration_config.json`: `{"HS1210": {"entryNodeId": "n404"}}` — used only when heuristic is uncertain or for override. | Explicit when needed. | Optional file. |

**Heuristic (automatic):**

1. **Primary:** Largest Subroutine by `range.endLine - range.startLine` (main logic block).
2. **Fallback:** Node with `procedureName` or `name` equal to programId (e.g. HS1210).
3. **Fallback:** First Subroutine in program context (by node id order).
4. **Override:** If `migration_config.json` exists with `entryNodeId` for this program, use it.

**Recommendation:** **Automatic by default** using the heuristic above. Config override only for edge cases.

**Slice computation (existing, enhanced):**

1. BFS from `entryNodeId` over `adjacency` (caller → callee).
2. Topological sort so callees are migrated before callers.
3. Result: `nodes_in_slice` = ordered list, e.g. `[n1983, n1779, n404]` (CheckV4 first, then CreateClaim, then main).

**New artifact: `migration_state.json`**

```json
{
  "programId": "HS1210",
  "entryNodeId": "n404",
  "featureLabel": "Claim Creation with V4 Validation",
  "nodesInSlice": ["n1983", "n1779", "n404"],
  "migrationOrder": ["n1983", "n1779", "n404"],
  "completedNodes": ["n1983", "n1779"],
  "currentNode": "n404",
  "status": "in_progress",
  "startedAt": "2026-03-09T10:00:00Z",
  "lastUpdatedAt": "2026-03-09T10:25:00Z"
}
```

**Feature label:** Derived from entry node (e.g. "Claim Creation" for n404) or from config. Used in UI.

---

### 4.3 Phase 3: Orchestrator & Per-Node Migration

**Goal:** One LLM call per node; orchestrator tracks state and merges output.

**New component: `migration_orchestrator.py`**

Responsibilities:

1. **Load migration state** (or create new from entry + slice).
2. **For each node in `migrationOrder`:**
   - If `node in completedNodes`: skip.
   - Load `context_index/<unit>_<node>.json`.
   - Load `existing_entities` from `warranty_demo` (entities already migrated).
   - Build prompt: global context + this node only (not all nodes).
   - Call LLM (via `migrate_to_pure_java.py` or equivalent).
   - Parse output, write files to `warranty_demo`, deduplicate entities.
   - Append node to `completedNodes`, update `currentNode`, persist state.
   - Stream progress: `{"progress": "Building feature: Claim Creation (3/3: n404)", "node": "n404", "done": 2, "total": 3}`.
3. **On completion:** Write final manifest, mark `status: "completed"`.

**Integration with `migrate_to_pure_java.py`:**

- **Option A:** Orchestrator invokes `migrate_to_pure_java.py` per node (minimal change). Each run gets `--existing-entities` from orchestrator.
- **Option B:** Refactor `migrate_to_pure_java.py` into a library; orchestrator calls `migrate_node(node_id, existing_entities)` directly. Cleaner but more refactoring.

**Recommendation:** Option A initially; Option B when stable.

---

### 4.4 UI Changes (Tab 2: Migrate Feature)

**Current:** Dropdowns for Program, Feature (node). Progress: "Migrating 1/N: HS1210_n404.json...".

**Proposed:**

1. **Before migration:**
   - User selects **Program only** (e.g. HS1210). Entry node is **auto-identified**.
   - Show "Entry node: n404 (auto-detected: Main Subroutine)".
   - Button: "Migrate" or "Analyze dependencies" → orchestrator computes slice. Shows: "This migration will include 3 nodes: n1983 (CheckV4), n1779 (CreateClaim), n404 (Main). Migrate in this order."
2. **During migration:**
   - Progress area:
     - "Building feature: Claim Creation with V4 Validation"
     - "Step 1/3: n1983 (CheckV4) — generating V4ValidationService..."
     - "Step 2/3: n1779 (CreateClaim) — generating ClaimCreationService..."
     - "Step 3/3: n404 (Main) — generating ClaimController, subfile logic..."
   - Optional: Collapsible "Migration state" showing `migration_state.json`.
3. **After migration:**
   - "Feature built. 3 nodes migrated. See manifest: global_context/migrations/HS1210_n404_<ts>.json."

---

### 4.5 Feature Label Derivation

**Source options:**

1. **From entry node:** Use `procedureName` or `name` from program context (e.g. n404 → "Main", n1779 → "CreateClaim").
2. **From config:** `migration_config.json`: `{"HS1210": {"n404": {"featureLabel": "Claim Creation with V4 Validation"}}}`.
3. **Heuristic:** Concatenate node names in slice: "CheckV4 + CreateClaim + Main".

**Recommendation:** Use `procedureName` or `name` when available; else node id (e.g. "n404").

---

## 5. Implementation Phases

| Phase | Deliverable | Effort (est.) |
|-------|-------------|---------------|
| **5.1** | Call graph: Procedure index + resolution | 1–2 days |
| **5.2** | `migration_state.json` schema + slice computation | 0.5 day |
| **5.3** | `migration_orchestrator.py` (invoke migrate_to_pure_java per node, track state) | 2–3 days |
| **5.4** | UI: "Analyze dependencies", "Building feature X (step Y/Z)" | 1 day |
| **5.5** | Integration testing: HS1210 n404 → full slice (n1983, n1779, n404) | 1 day |

**Total:** ~6–8 days.

---

## 6. Risks & Mitigations

| Risk | Mitigation |
|------|-------------|
| Procedure names in RPG don't match AST `procedureName` | Add logging; fallback to fuzzy match or manual mapping file |
| Large slice (e.g. 10+ nodes) → long migration | Add "Resume" from `migration_state.json`; allow cancel and resume |
| Entity conflicts when merging multiple nodes | Strengthen `TABLE_TO_CANONICAL_ENTITY` and `_deduplicate_entities_by_table` |
| LLM produces code that doesn't call previously migrated services | Prompt engineering: "Existing services: V4ValidationService. Use them." |

---

## 7. Out of Scope (This Plan)

- Cross-program migration (HS1210 + HS1212 in one run).
- Automatic "Resume" after build failure (Tab 4).
- Migration of display-file-only nodes (no DB).

---

## 8. Confirmation Checklist

Please confirm or adjust:

- [ ] **Call graph:** Add Procedure index and resolve EXSR/CALLP to Procedures. Agree?
- [ ] **Entry point:** Automatic (heuristic); optional config override only. No user selection. Agree?
- [ ] **Orchestrator:** One LLM call per node; state in `migration_state.json`. Agree?
- [ ] **UI:** "Building feature X (step Y/Z)" and optional "Analyze dependencies". Agree?
- [ ] **No manual integration:** Orchestrator ensures merge order; LLM prompt includes existing services. Agree?

---

## 9. Next Steps After Confirmation

1. Implement Phase 5.1 (call graph enhancement).
2. Implement Phase 5.2 + 5.3 (orchestrator).
3. Wire orchestrator into `/api/migrate-feature`.
4. Update UI (Phase 5.4).
5. Test with HS1210, entry n404; verify n1983, n1779 are in slice and migrated.
