# Application-Level Manifest and Context: Shared Variables and I/O

This document explains **how** the pipeline currently keeps (or does not keep) application-level context for **shared variables** and **I/O operations**, and what is planned for the future.

---

## 1. What Exists Today

### 1.1 Application-level “manifest”

- **File:** `context_index/manifest.json`
- **Produced by:** `com.pks.migration.IndexAll` (Build Context step).
- **Content:** A **catalog of nodes**, not a bundle of shared state or I/O:
  - `unitId`, `nodeId`, `kind`, `sourceFileId`
  - `dbFilesUsed` — which DB files (tables) this node references
  - `calls`, `calledBy` — **currently always empty** (not populated by the AST)

So the manifest is an **index** of “which nodes exist and which DB files they use.” It does **not** contain:
- Shared variable definitions or “used by which nodes”
- A list of I/O operations (read/write/display) per node or per application
- Call graph (caller/callee) — those fields are placeholders

### 1.2 Per-node context (the real “context” for migration)

- **Files:** `context_index/<unitId>_<nodeId>.json` (e.g. `HS1210_n1779.json`)
- **Produced by:** `IndexAll` → `ContextAssembler` (per node), then optionally **enricher** (`enrich_context_with_display_files.py`) for display files.
- **Content:** One JSON per **procedure/subroutine node**. This is what the migrator (and LLM) see.

Each context package contains:

| Section        | Role |
|----------------|------|
| **astNode**    | Node id, kind, `sem` (symbol refs), `outgoingEdges` (variables + files this node uses), `range`, `raw` AST. |
| **narrative**  | Human-readable summary: “Files/DB contracts,” “Variables” (with types where known), “Outgoing edges,” raw sem JSON. Built by `SemanticNarrativeBuilder` from the node’s referenced symbols. |
| **rpgSnippet**  | RPG source lines for this node’s `range` (the “how”). |
| **dbContracts**| List of **DB file contracts** (name, columns, types) for every **file** symbol this node references (e.g. HSG71PF, HSG73PF, HSGPSLF3). So **DB I/O is represented by “which tables and columns this node touches.”** |
| **displayFiles**| (After enricher) Which display files (DSPF) this node uses; optional **ddsSource** (DDS snippet) and **uiContracts** (record formats, fields) for UI-aware generation. So **display I/O** is “which screens/forms this node uses.” |

So:
- **Shared variables** and **I/O** are only described **per node** inside each node’s context file.
- There is **no** separate “application-level manifest” that aggregates shared variables or I/O across all nodes.

---

## 2. How Shared Variables Are Represented

- **In RPG:** Program-level or copybook variables (e.g. `SR06`, `KEYG71`) can be used by **several** procedures/subroutines (“shared”).
- **In the pipeline today:**
  - Each node’s context has that node’s **sem** and **outgoingEdges** (e.g. `sym.var.create`, `sym.var.maintenance`, `sym.var.groups`).
  - The **narrative** turns these into a “Variables” list with names and types for **that node only**.
  - There is **no**:
    - Single “application-level” list of shared variables
    - “Used by nodes: n404, n1919” per variable
    - Copybook / program-level symbol table

So **shared variables are only kept at “node level”**: each context file describes what **that** node uses. The migrator (and LLM) do **not** get an explicit “application-level manifest” of shared variables. Design doc (see below) describes adding **program-level context** (e.g. which symbols are shared across nodes) in a later phase.

---

## 3. How I/O Operations Are Represented

### 3.1 Database I/O

- **Representation:** Via **dbContracts** in each node’s context.
  - **dbContracts** = “which DB files (tables) this node references” + their column definitions.
  - The **narrative** summarizes these as “Files / DB Contracts” with column lists.
  - The **rpgSnippet** contains the actual RPG (CHAIN, READ, UPDATE, WRITE, etc.), so the **operations** are in the source text, not in a separate “I/O manifest.”
- **Application-level:** The **manifest** has `dbFilesUsed` per node (which tables the node uses), but no “read/write/update list” or transaction boundaries. So:
  - **“Which tables does this node use?”** → yes (per-node context + manifest).
  - **“Full application-level I/O manifest (every read/write per node)”** → no; that would require extra analysis (e.g. from AST opcodes) and is not stored today.

### 3.2 Display file (screen) I/O

- **Representation:** Via **displayFiles** in each node’s context (after the enricher).
  - Each entry: display file id, optional **ddsSource** (DDS snippet), optional **uiContracts** (record formats, fields, keywords).
  - So “which screens/forms does this node use?” and “what do those screens look like?” are in the **per-node** context.
- **Application-level:** There is no single “application display-file manifest” (e.g. “all DSPFs used by HS1210”); it’s aggregated only implicitly by having every node’s context list its own displayFiles.

---

## 4. Summary Table

| Concern                | Where it lives today                          | Application-level manifest? |
|------------------------|-----------------------------------------------|----------------------------|
| **Which nodes exist**  | `manifest.json` (entries with unitId, nodeId) | Yes (manifest)             |
| **Which DB files a node uses** | manifest `dbFilesUsed` + context `dbContracts` | Per-node only in context; manifest has list only |
| **DB schema (columns)**| Per-node `dbContracts` in context             | No (per-node)              |
| **Shared variables**   | Per-node `sem` / `outgoingEdges` / narrative | No (per-node only)         |
| **Call graph**         | manifest `calls` / `calledBy` (empty)        | Placeholder only           |
| **Display files (DSPF)** | Per-node `displayFiles` in context          | No (per-node only)         |
| **I/O operation list** | Implicit in rpgSnippet + dbContracts          | No                         |

So: **application-level** we only have a **node index** (manifest) and **per-node DB file usage**. Shared variables and a full I/O manifest are **not** kept at application level; they exist only inside each node’s context.

---

## 5. What the Design Doc Proposes (Future)

From **ENTERPRISE_MIGRATION_DESIGN.md** and **CALL_GRAPH_STRATEGY.md**:

- **Phase 2 – Contract registry:** One canonical “file name → columns” so all nodes share the same DB view; no duplicate entity definitions.
- **Phase 3 – Program-level context:** For each unit (e.g. HS1210), a **program-level** context (e.g. `HS1210_program.json`) with:
  - Program-level symbols and copybooks
  - Which symbols are referenced by more than one node (“shared”)
  - So shared variables become explicit “shared” and can be migrated to APIs or shared DTOs.
- **Phase 4 – Call graph:** Populate `calls` / `calledBy` in the manifest (or separate file) so service boundaries and APIs can be derived.

Until those are implemented, **shared variables and I/O are only kept in per-node context**, and the only application-level “manifest” is the node index in `manifest.json` plus per-node `dbFilesUsed`.
