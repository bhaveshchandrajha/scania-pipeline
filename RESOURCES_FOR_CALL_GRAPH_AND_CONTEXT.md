# Resources We Have to Build Call Graph, Context Registry, and Program-Level Context

This document lists **what already exists** in the project that can be used to implement:

1. **Call graph** (calls / calledBy)
2. **Context (contract) registry** (canonical DB schema per file)
3. **Program-level context** (shared symbols, program scope, copybooks)

---

## 1. Call graph

### 1.1 AST (per-program, e.g. HS1210-ast.json)

| Resource | Location | What it gives you |
|----------|----------|--------------------|
| **Call-site nodes** | `nodes[]` with `"kind": "ExSr"` or `"kind": "CallP"` | Every EXSR and CALLP has `id`, `range` (fileId, startLine, endLine, startCol, endCol), `props.opcode` ("EXSR" / "CALLP"). **Callee name is not in the node** — you must read the RPG source at `range.startLine` to get the subroutine or procedure name. |
| **Procedure names** | `nodes[]` where `kind === "Procedure"` → `props.procedure_name` or `props.name` | Many procedures have a name (e.g. "CreateClaim", "CheckClaim"). Build **procedure name → node id** from this. |
| **Subroutine names** | Not in AST | Subroutine name is the **BEGSR label** in source. You need RPG source at the **Subroutine** node’s `range.startLine` to read that label; then **subroutine name → node id**. |
| **Containment** | `nodes[].children` | Each node can have `children: [ "n123", "n456" ]`. So for each ExSr/CallP you can find the **containing** Procedure/Subroutine by walking parent chain (or by finding the Procedure/Subroutine whose `range` includes the ExSr line). That container = **caller** node id. |
| **Edges** | Root `edges[]` | Only `"declares"` and `"refers_to"` today. **No** `calls` edges. So call graph must be **derived** from ExSr/CallP nodes + source + containment. |

**Conclusion:** You have everything needed to build the call graph **without changing the AST producer**: AST gives call-site nodes and ranges; RPG source (see below) gives callee names; containment/range gives caller.

### 1.2 RPG source

| Resource | Location | What it gives you |
|----------|----------|--------------------|
| **Full program source** | `--rpgDir` (e.g. PoC_HS1210) or equivalent; file name from AST `unit.id` (e.g. HS1210) | Build Context / IndexAll resolve RPG source so snippets can be extracted. For call graph you need **full source** (or at least lines) for: (1) at each **ExSr** node’s `range.startLine` → token after EXSR = subroutine name; (2) at each **CallP** node’s line → procedure name if not in AST; (3) at each **Subroutine** node’s `range.startLine` → BEGSR label = subroutine name. |
| **RpgSourceProvider** | `context/InMemoryRpgSourceProvider.java`, `RpgSourceProvider.java` | Interface to get source by fileId. Indexer uses this (or a file-based impl) to get snippets. A **Python** call-graph script would read the same RPG files from disk (e.g. `rpgDir/HS1210.sqlrpgle`) by line number. |

### 1.3 Design and algorithm

| Resource | Location | What it gives you |
|----------|----------|--------------------|
| **Strategy and algorithm** | `CALL_GRAPH_STRATEGY.md` | Step-by-step: (1) Build **name → node id** for Procedures (from props) and Subroutines (from BEGSR in source). (2) For each ExSr/CallP, get callee name from source at node’s line; resolve to node id; get caller from containment or range. (3) Output edges (caller, callee) and optionally enrich `manifest.json` with `calls` / `calledBy`. Suggests **Option A: Python post-process** (AST + source) first; Option B: move into Java indexer later. |

**Summary – call graph:** AST (nodes, children, range, procedure_name) + RPG source (by line) + algorithm in CALL_GRAPH_STRATEGY.md are sufficient. No new AST fields required; a Python script (e.g. `build_call_graph.py`) that reads AST + source is the main missing piece.

---

## 2. Context (contract) registry

### 2.1 AST – canonical DB schema per program

| Resource | Location | What it gives you |
|----------|----------|--------------------|
| **dbContracts** | Root `dbContracts.nativeFiles[]` in each AST file (e.g. HS1210-ast.json) | **Full** list of DB files (tables) for that program: `symbolId`, `name`, `library`, `typeId`, `columns[]` (name, typeId, nullable), `keys[]`. This is already the **canonical schema** for that unit. |
| **Parser** | `PksAstParser.readDbContracts()` | Java code that reads `dbContracts.nativeFiles` and builds `DbContract` models (symbolId, name, library, columns with type resolution via `types`). So the AST is the source of truth; the registry can be **exported** from the AST. |

### 2.2 Per-node context (current)

| Resource | Location | What it gives you |
|----------|----------|--------------------|
| **dbContracts per node** | Each `context_index/<unit>_<node>.json` → `dbContracts[]` | Subset of DB files **referenced by that node** (from node’s `sem` / file symbols). So today each context file carries a **copy** of the relevant contracts, not a reference to a central registry. |

**Conclusion:** The **registry** is already present as `dbContracts.nativeFiles` in the AST. What’s missing is:

- A **single** artifact (e.g. `context_index/HS1210_db_registry.json` or a global registry merging all units) that lists “file name → canonical columns.”
- Context builder **resolving** dbContracts by **reference** to this registry (e.g. by symbolId or file name) instead of embedding full contracts in every node JSON.

So: **data** = in AST; **tooling** = extract from AST (one-time or per build) and optionally have the indexer/context assembler resolve by reference.

---

## 3. Program-level context

### 3.1 AST – symbols and scope

| Resource | Location | What it gives you |
|----------|----------|--------------------|
| **symbolTable** | Root `symbolTable` in AST | Every symbol: `symbolId`, `name`, `kind` (e.g. file, var), `typeId`, `declNodeId`, **scopeId** (e.g. `"scope.global"`). So you can distinguish **program-level** (scope.global) vs procedure/local scope. |
| **refers_to edges** | Root `edges[]` with `"kind": "refers_to"` | `src` = node id, `dst` = symbol id. So “which node uses which symbol.” Invert: for each symbol, collect all nodes that reference it → **symbols referenced by more than one node** = shared. |
| **nodes[].sem** | Each node’s `sem` object | Keys = symbol ids that this node references. So per-node refs are also available without walking edges; you can aggregate over all nodes to get “symbol → list of node ids.” |

### 3.2 Copybooks / includes

| Resource | Location | What it gives you |
|----------|----------|--------------------|
| **includeGraph** | `unit.includeGraph` in AST | In the sample HS1210-ast.json this is `[]`. If the front-end ever fills it, it would list included copybooks; then you could attach copybook content or paths for program-level context. |
| **Copybook content** | Not in AST today | No copybook body in the current AST. Program-level context would need either includeGraph + external copybook files, or a separate extraction step from source (/COPY etc.). |

### 3.3 CompilationUnit (n1)

| Resource | Location | What it gives you |
|----------|----------|--------------------|
| **Root node n1** | `nodes[]` where `id === "n1"`, `kind === "CompilationUnit"` | Has a very large `sem` / referenced symbols (all program-level refs from the main compilation unit). So **n1** is a quick approximation of “what the program as a whole references,” but not “all symbols defined in the program.” For “all symbols” use **symbolTable**; for “which are shared” use symbolTable + refers_to (or sem) to count nodes per symbol. |

**Conclusion – program-level context:** You have:

- **Program-level vs local:** `symbolTable[].scopeId`.
- **Shared variables:** From `edges` (refers_to) or from each node’s `sem`: for each symbol, count nodes that reference it; if > 1, it’s shared.
- **List of node ids:** From AST `nodes[]` (e.g. all nodes with kind Procedure, Subroutine, CompilationUnit) or from manifest `entries[]`.

What’s missing is a **single program-level document** (e.g. `HS1210_program.json`) that contains: node list, program-level symbols, “shared” flag per symbol, and optionally copybook names/content when available (includeGraph + files).

---

## 4. Summary table

| Goal | Data we have | Missing piece |
|------|----------------|---------------|
| **Call graph** | AST: ExSr/CallP nodes (range), Procedure names (props), children (containment). Source: via rpgDir / RpgSourceProvider. Algorithm: CALL_GRAPH_STRATEGY.md. | Script (e.g. Python) that loads AST + source, resolves names, outputs call edges and optionally enriches manifest. |
| **Context registry** | AST: `dbContracts.nativeFiles` (full schema per program). Parser already reads it. | Export step (e.g. one JSON per unit or one global registry) and optionally change context builder to resolve by reference. |
| **Program-level context** | AST: symbolTable (scopeId), refers_to edges (node→symbol), nodes[].sem. So “shared” = symbols referenced by >1 node. | One program-level file (e.g. HS1210_program.json) with node list, program-level/shared symbols, and optionally copybooks when includeGraph/content available. |

---

## 5. Where the indexer fits

- **Build Context** (UI or CLI) runs **`com.pks.migration.IndexAll`** from the JAR (`target/pks-ast-migration-pipeline-*.jar`) with `--astDir`, `--rpgDir`, `--outputDir`. It writes `context_index/<unit>_<node>.json` and `context_index/manifest.json`.
- The **Java** code in this repo includes **PksAstParser**, **ContextAssembler**, **SemanticNarrativeBuilder**, **ContextPackage** — they build **per-node** context from AST + RPG source. The class that **iterates** over nodes and writes manifest + context files (IndexAll) is not present under `src/` in this workspace (it may live in another module or JAR). So:
  - **Call graph:** Can be built **without** changing the indexer, by a **post-processing** script (Python) that reads the same AST and RPG source.
  - **Registry:** Can be built by a script that reads AST and writes e.g. `HS1210_db_registry.json`; the indexer could later be changed to reference it.
  - **Program-level context:** Can be built by a script that reads AST, computes shared symbols and program-level list, and writes `HS1210_program.json`; the indexer could later merge this into the pipeline.

All three can be implemented with **current resources** (AST + source + existing docs); the main work is the scripts and, if desired, integration into the indexer or UI.
