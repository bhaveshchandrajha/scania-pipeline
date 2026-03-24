# Enterprise-Grade Migration Pipeline: Shared State and Global View

This document addresses how to scale the current **single-node** migration (e.g. HS1210 n404) into an **enterprise-grade pipeline** when migrating **many nodes or units**. The main concern: **how are shared variables and other global-view items managed across migrated modules?**

---

## 1. The Problem in Short

Today:

- **One node** (e.g. HS1210 n404) → **one context file** (e.g. `HS1210_n404.json`) → **one Pure Java application** (e.g. `HS1210_n404_pure_java`).
- Each context is **node-scoped**: it only contains that node’s AST slice, its **sem** (semantic refs), **outgoingEdges**, **dbContracts**, **narrative**, **rpgSnippet**, and **symbols**).
- There is **no program-level or global scope**: no shared copybooks, no “this variable is shared with other nodes,” and (in current data) no call graph (`calls` / `calledBy` are empty).

When you migrate **other nodes** (e.g. HS1210 n1919, n1779, or HS1212 n498):

1. **Shared variables**  
   In RPG, variables like `HS1210C1`, `SR06`, `KEYG71`, `STATUS` can be defined at **program level** (main source or copybooks) and used by **several** subroutines. Each node’s context only lists the symbols *that* node references. So:
   - Migrated Java for n404 has no idea that `SR06` is shared with n1919.
   - If n1919 is migrated to another service, we need a clear rule: **shared state becomes explicit API or shared persistence**, not in-process globals.

2. **Shared DB files (tables)**  
   Multiple nodes use the same physical files (e.g. HSG71LF2/Claim, HSG73PF/ClaimError). If every node is migrated to a **separate** Java service, we would duplicate entities (Claim, ClaimError) in each service unless we introduce a **shared domain** or **shared library**.

3. **Call graph**  
   If in RPG n404 calls a procedure (e.g. n1919), in Java we need either:
   - One service calling another via **REST/gRPC**, or
   - Both in the same process with **in-process interfaces**.  
   That requires the pipeline to know **calls** / **calledBy** and to generate or use **shared APIs**.

4. **Copybooks / includes**  
   RPG often uses `/COPY` or similar to pull in shared data structures and constants. Today these are not modeled as a “global view” that multiple nodes inherit; they appear only indirectly via each node’s symbols. For enterprise scale we need **program-level or application-level context** that includes shared definitions.

So: **shared variables and global-view items** must be turned into one of:

- **Shared domain** (entities, value objects, enums) in a **shared library** used by all migrated services.
- **Explicit APIs** between services (e.g. “get/set filter state” instead of global `FILTER`).
- **Shared persistence** (same DB, same tables) with a **single source of truth** for schema and entity definitions.

---

## 2. Current vs Target Behavior

| Aspect | Current (single-node) | Target (enterprise) |
|--------|------------------------|----------------------|
| Scope | One node → one context → one app | Program or application scope + per-node slice |
| Shared variables | Only in-node symbols; no “shared” flag | Program-level symbol table; shared → API or shared DTOs |
| DB contracts | Per-node dbContracts | Shared contract registry; one entity set per table |
| Call graph | Not used (calls/calledBy empty) | calls/calledBy populated → service boundaries and APIs |
| Copybooks | Only via symbols in node | Explicit “global” or “program” context (copybook content + scope) |

---

## 3. How to Manage Shared Variables and Global View (Design)

### 3.1 Program-level (unit-level) context

- **Idea:** For each **unit** (e.g. HS1210), build a **program-level context** that includes:
  - **All nodes** in that unit (from manifest).
  - **Program-level symbols**: variables and data structures that are defined in the main source or in copybooks and that are referenced by more than one node (or that are “global” in the RPG sense).
  - **Copybook / include content**: for each referenced copybook, include its full text or a normalized form so that “global” structures and constants are visible.
- **Use:** When migrating **any** node of that unit, the migrator receives:
  - The **node context** (as today), plus
  - A **program-level appendix**: “Shared symbols,” “Shared data structures,” “Copybooks used by this program.”
- **Shared variables** then get explicit treatment:
  - **Option A (preferred for microservices):** Don’t migrate globals as process memory. Migrate **behavior**; shared state becomes **persisted** (DB) or **passed via API** (e.g. “filter state” is a DTO in the request or stored in a small “session” or “context” table/key).
  - **Option B (monolith or single service):** Migrate the whole unit to one Java application; “shared” variables become fields in a shared context object or service state passed between methods (still no true globals; explicit passing or a well-defined context).

So: **global view** = program-level context (symbols + copybooks + which symbols are shared). **Management** = either shared library + API + DB, or one service per program with explicit context object.

### 3.2 Shared domain and DB contracts

- **Single source of truth for schema:**  
  Define **one** place for “table X → entity Y, columns Z.” For example:
  - A **shared DB contract registry** (e.g. JSON or schema repo) that lists every physical file and its canonical column set.
  - All node contexts that use that file **reference** this contract (by name) instead of embedding a full copy.
- **Shared Java artifact:**  
  Build a **shared domain library** (e.g. `warranty-domain-api` JAR) containing:
  - **Entities** (Claim, Invoice, ClaimError, etc.) with JPA mappings.
  - **Value objects** (ClaimSearchCriteria, ClaimStatus, etc.).
  - **Repository interfaces** (if in-process) or **API DTOs** (if services call each other via REST/gRPC).
- **Per-node migration output:**  
  Each migrated node becomes a **service** (or a set of classes) that:
  - **Depends on** the shared domain JAR (so no duplicate entity definitions).
  - Implements only the **behavior** of that node (use cases, validation, workflow step).
  - Communicates with other migrated nodes via **API** (REST/gRPC) or **in-process** (same JVM, shared interfaces).

This way, **shared variables** that in RPG mean “same program memory” become in Java either **shared entities in DB** or **explicit request/response DTOs** between services.

### 3.3 Call graph and service boundaries

- **Populate calls / calledBy:**  
  The indexer (e.g. `IndexAll` or AST analysis) should fill `calls` and `calledBy` in the manifest (or in a separate call-graph file) so we know: “n404 calls n1919,” “n498 is called by n1,” etc.
- **Service boundaries:**  
  - **Strategy A:** One service per **program** (e.g. one “HS1210 service”) so all nodes of HS1210 live in one process; “shared variables” become in-memory state or a context object inside that service.
  - **Strategy B:** One service per **node** (or per cohesive group of nodes). Then “shared” state is not in memory but in **DB** or **API**: e.g. “n1919” exposes “get/set validation result”; n404 calls it via HTTP or internal API and passes DTOs.
- **API-first between services:**  
  For Strategy B, define **small, explicit APIs** (e.g. OpenAPI) for each migrated node that is called by others. Shared variables that used to be global become **request/response fields** or **stored context** (e.g. claim id + step id in DB).

So: **shared variables and global view** are “managed” by (1) knowing who calls whom and (2) replacing globals with **persistence + API** or **in-process context**.

### 3.4 Copybooks and “global” definitions

- **Extract copybook content:**  
  From AST/source, resolve `/COPY` (or equivalent) and attach the **content** (or a normalized form) to the **program-level context**.
- **Expose in context:**  
  In the “global view” section for the unit, include:
  - “Copybooks: [list of names and content or paths].”
  - “Data structures and constants defined here: [list].”
- **Migration guidance:**  
  When generating Java for a node, the prompt can say: “Shared structures from copybooks must map to **shared** value objects or enums in package `com.scania.warranty.common` (or shared library). Do not redefine the same structure in each service.”

This gives a clear **global view** for the migrator and avoids duplicate, inconsistent definitions of the same logical structure across nodes.

---

## 4. Phased Roadmap

| Phase | What | Shared variables / global view |
|-------|------|---------------------------------|
| **Phase 1 (current)** | Single-node migration, one app per node | Each node is self-contained; “shared” is not modeled. Acceptable for PoC or isolated modules. |
| **Phase 2** | **Shared domain library** + **contract registry** | Same DB tables → one entity set in a shared JAR. All migrated nodes depend on this JAR. Shared variables still per-node in prompts but entities/repos not duplicated. |
| **Phase 3** | **Program-level context** | Per unit: program-level symbols + copybooks + “shared” flag. Migrator gets node context + program appendix. Shared state → explicit API or context DTO. |
| **Phase 4** | **Call-graph-aware migration** | calls/calledBy populated. Option: one service per program (monolith) or one per node with explicit APIs; shared state only via DB and API. |

---

## 5. Concrete Next Steps (What to Build)

1. **Contract registry (Phase 2)**  
   - Single JSON (or DB) of “file name → canonical columns.”  
   - Context builder: for each node, resolve dbContracts by reference to this registry instead of embedding full contracts in every context file.  
   - Migrated Java: one shared project (e.g. `warranty-domain`) that generates entities from the registry; node migrations depend on it.

2. **Program-level context (Phase 3)**  
   - Extend indexer or add a post-step: for each unit, output `HS1210_program.json` (or similar) with:  
     - List of node IDs.  
     - Program-level symbols (e.g. from main source and copybooks).  
     - Which symbols are referenced by more than one node (“shared”).  
     - Copybook names and content (or paths).  
   - Migration prompt: “In addition to this node, consider program-level context in [appendix]. Shared symbols must become API or shared DTOs; do not assume in-process globals.”

3. **Call graph (Phase 4)**  
   - Indexer: populate `calls` and `calledBy` in manifest (or separate file).  
   - Pipeline: when migrating node A that calls node B, either  
     - generate B as part of the same service (same repo), or  
     - generate B as a separate service and generate for A a **client** to B’s API (REST/gRPC) and pass “shared” data as DTOs.

4. **Shared domain JAR (Phase 2+)**  
   - Build `warranty-domain` (or similar) from the contract registry: entities, value objects, repository interfaces.  
   - Each node’s migration output is a **service module** that depends on `warranty-domain` and implements only the node’s use cases.

---

## 6. Summary

- **Why it’s a concern:** Migrating more nodes means dealing with **shared variables**, **shared files (DB)**, and **calls** between nodes. The current pipeline is node-scoped and has no global view.
- **How to manage shared variables and global view:**
  - **Model them:** Program-level context (symbols, copybooks, “shared” flag) and call graph (calls/calledBy).
  - **Don’t carry RPG globals into Java as globals:** Turn them into **persistence** (DB), **explicit API** (request/response DTOs), or **in-process context** (one service per program with a clear context object).
  - **Single source of truth for schema:** Contract registry + shared domain JAR so all migrated modules use the same entities and contracts.
- **Scalable pipeline:** Add **program-level context**, **contract registry**, **shared domain library**, and **call-graph-aware** service boundaries so that migrating “the rest of the application” is consistent and enterprise-grade.

If you tell me your preferred target (e.g. “one service per program” vs “one service per node”), I can narrow the design to a minimal set of pipeline changes (e.g. context schema + indexer changes + one shared JAR layout) and outline exact file formats and CLI steps.
