# Call Graph: Confirmation and Strategy

## 1. Confirmation: Call Graph Is NOT in the AST (But Edges Exist!)

**The AST DOES contain an `edges` array, but it does NOT contain explicit `calls`/`calledBy` relationships.**

- In **HS1210-ast.json**, there **IS** an `"edges"` array at the root level (starting around line 54672).
- The edges array contains **two edge kinds**:
  - **`"declares"`** (40 edges): `src` = node id (e.g. n114), `dst` = symbol (e.g. sym.file.AUFWKO)
  - **`"refers_to"`** (1681 edges): `src` = node id (e.g. n1, n404), `dst` = symbol (e.g. sym.var.CNR, sym.file.HSG71LF2)
- **However**, there are **NO** edge kinds for:
  - **`"calls"`** — no edges from caller node to callee node/subroutine/procedure
  - **`"reads_from"`** — no explicit edges for DB reads (CHAIN, READ, etc.)
  - **`"writes_to"`** — no explicit edges for DB writes (UPDATE, WRITE, DELETE, etc.)
  - **`"controls"`** — no explicit control-flow edges (IF/ELSE, DO/ENDDO, etc.)
- The **manifest** (`context_index/manifest.json`) has `"calls": [], "calledBy": []` for every entry — always empty.
- So **calls/calledBy** are **not** produced by the current AST/indexing pipeline. They must be **derived** from the AST structure + source code.

---

## 2. What the AST *Does* Contain (Raw Material for a Call Graph)

### 2.0 Edge Types Present in AST

The AST has an **`edges`** array with:
- **`"declares"`**: Node → Symbol (e.g. n114 declares sym.file.AUFWKO)
- **`"refers_to"`**: Node → Symbol (e.g. n404 refers_to sym.var.CNR, sym.file.HSG71LF2)

**Missing edge types** (that would be useful for call graph, impact analysis, transaction boundaries):
- **`"calls"`**: Node → Subroutine/Procedure (caller → callee)
- **`"reads_from"`**: Node → File (DB read operations: CHAIN, READ, READE, READPE, SETLL)
- **`"writes_to"`**: Node → File (DB write operations: UPDATE, WRITE, DELETE)
- **`"controls"`**: Node → Node (control flow: IF → children, DO → ENDDO, etc.)

**Note:** These relationships are **implicit** in the AST structure:
- **Reads/Writes:** Can be inferred from nodes with `opcode: "CHAIN"`, `opcode: "UPDATE"`, `opcode: "WRITE"`, etc., and their `target` field (which is the file name).
- **Calls:** Can be inferred from `ExSr` and `CallP` nodes (but callee name must come from source).
- **Controls:** Can be inferred from AST `children` arrays (IF has children, DO has children, etc.).

### 2.1 Call-site nodes

The AST is a **PKS-RPG-FrontEnd** (rpg-ast/1.0) tree. It contains the following, which can be used to build a call graph:

### 2.1 Call-site nodes

| AST `kind` | RPG opcode | Meaning |
|------------|------------|---------|
| **ExSr**   | EXSR       | Execute Subroutine — calls a subroutine by name |
| **CallP**  | CALLP      | Call Procedure — calls a procedure (possibly with parms) |

- Each **ExSr** node has: `id`, `kind`, `range` (fileId, startLine, startCol, endLine, endCol), and `props: { "opcode": "EXSR" }`.
- **The callee name is not stored in the ExSr node.** The AST does not expose the subroutine name in `props` or `sem` for the ExSr nodes inspected (e.g. n280, n312, n330).
- **CallP** nodes similarly have `opcode: "CALLP"` and a `range`; the procedure name may be on the source line or in a child/reference not always present in props.

So: **call sites are in the AST (ExSr, CallP); the callee name is not reliably in the AST and must be read from the RPG source at the node’s `range` (e.g. the line at `startLine`).**

### 2.2 Callee definitions (targets)

| AST `kind`   | RPG concept | Name in AST? |
|--------------|-------------|--------------|
| **Subroutine** | BEGSR/ENDSR block | Not in the inspected nodes; name comes from the **label on the BEGSR line** in source. |
| **Procedure**  | P ... BEGPROC/ENDPROC | Yes — many have `props.procedure_name` and/or `props.name` (e.g. "CheckClaim", "CreateClaim", "AddAttachments"). |

- **Procedure** nodes often have `procedure_name` and `name` in `props`, so procedure names can be collected from the AST.
- **Subroutine** nodes (e.g. n404) have `id`, `kind`, `sem`, `range` but **no** `name` or `procedure_name` in the snippet inspected. So the subroutine name (the BEGSR label) must be obtained from the **source line** at the node’s `range.startLine`.

---

## 3. Conclusion: Source of Truth

- **Call graph is not explicitly in the AST edges.** The AST has `declares` and `refers_to` edges, but **not** `calls`, `reads_from`, `writes_to`, or `controls`.
- **Call sites:** Present in the AST as **ExSr** and **CallP** nodes (with file/line/column range).
- **Callee names:**
  - **Procedures:** Often available in AST `props` (`procedure_name`, `name`).
  - **Subroutines:** Not reliably in AST; must be read from **RPG source** (BEGSR label).
  - **EXSR target:** Must be read from **RPG source** at the ExSr node’s line (e.g. the token after EXSR).
  - **CALLP target:** Same idea — source line or AST if the front-end adds a target reference later.
- **Reads/Writes:** Can be **inferred** from AST nodes with opcodes like `CHAIN`, `UPDATE`, `WRITE`, `DELETE`, `SETLL` and their `target` field (file name). These are **not** explicit `reads_from`/`writes_to` edges, but the information exists in the AST node structure.

So the strategy to build a call graph (and reads/writes/controls) is: **use the AST to find call sites, DB operations, and control structures, and use the RPG source to resolve names where the AST does not provide them.**

---

## 4. Strategy to Build the Call Graph (and Reads/Writes/Controls)

### 4.0 Building Reads/Writes/Controls from AST

Since the AST has nodes with opcodes but **not** explicit `reads_from`/`writes_to` edges, we can **derive** them:

1. **Reads from DB:**
   - Find nodes with `opcode: "CHAIN"`, `opcode: "READ"`, `opcode: "READE"`, `opcode: "READPE"`, `opcode: "SETLL"`.
   - Extract `target` field (file name) from these nodes.
   - Create **`reads_from`** edges: `{ src: nodeId, dst: sym.file.FILENAME, kind: "reads_from" }`.

2. **Writes to DB:**
   - Find nodes with `opcode: "UPDATE"`, `opcode: "WRITE"`, `opcode: "DELETE"`.
   - Extract `target` field (file name).
   - Create **`writes_to`** edges: `{ src: nodeId, dst: sym.file.FILENAME, kind: "writes_to" }`.

3. **Controls:**
   - Use AST `children` arrays: IF nodes have children (condition, then-branch, else-branch), DO/ENDDO pairs, etc.
   - Create **`controls`** edges: `{ src: parentNodeId, dst: childNodeId, kind: "controls" }`.

This gives you **impact analysis** (which nodes read/write which files), **transaction boundaries** (group nodes that write to the same files), and **hot paths** (nodes that read/write frequently-used files).

## 5. Strategy to Build the Call Graph

### 5.1 Option A: Post-process AST + RPG source (Python or similar)

1. **Load AST** (e.g. HS1210-ast.json) and **RPG source** for the program (e.g. HS1210).
2. **Build callee name → node id map (within this program):**
   - For each node with `kind === "Procedure"`: if `props.procedure_name` or `props.name` exists, record `name → node.id`.
   - For each node with `kind === "Subroutine"`: read the RPG source at `range.startLine` (and optionally startCol/endCol), find the BEGSR line, and take the **label** (first token) as the subroutine name; record `label → node.id`.
3. **Find caller → callee:**
   - For each node with `kind === "ExSr"`: read the RPG source at `range.startLine` (and startCol if needed), parse the line to get the token after EXSR (the subroutine name); resolve to node id via the map; record **(callerNodeId, calleeNodeId)**.  
     **Caller** = the node that **contains** this ExSr (e.g. the Subroutine or Procedure node whose `range` contains this ExSr’s line, or the parent in the AST tree).
   - For each node with `kind === "CallP"`: similarly get the procedure name from source (or from AST if present); resolve to node id; record (callerNodeId, calleeNodeId).
4. **Resolve caller node id:**  
   For each ExSr/CallP, determine the **containing** Subroutine or Procedure (walk parent chain in the AST, or find the Subroutine/Procedure node whose `range` includes the ExSr/CallP line). That container is the **caller**.
5. **Output:**  
   A call graph structure, e.g.:
   - `call_graph.json`: `{ "unitId": "HS1210", "edges": [ { "caller": "n404", "callee": "n1919" }, ... ] }`, or
   - **Enriched manifest:** for each entry, `"calls": ["n1919", ...]`, `"calledBy": ["n404", ...]`.

**Pros:** No change to the AST producer; works with current AST and source.  
**Cons:** Depends on RPG source being available and line numbers matching; requires parsing lines (fixed-format or free-format).

### 5.2 Option B: Enhance the indexer (Java JAR)

- In **IndexAll** (or the component that writes the manifest), while traversing the AST:
  - Collect all **Subroutine** and **Procedure** nodes; for each, resolve name (from props or from source at BEGSR/P line).
  - Collect all **ExSr** and **CallP** nodes; for each, resolve callee name (from source at node’s range) and containing caller (parent or range containment).
  - Write **calls** and **calledBy** into the manifest (or a separate call-graph file) when writing context packages.
- **Pros:** Single place to maintain; call graph always produced with context.  
- **Cons:** Requires changing and redeploying the Java indexer; need access to RPG source in that pipeline.

### 5.3 Option C: Hybrid (recommended short term)

1. **Implement Option A** as a **Python script** (e.g. `build_call_graph.py`):
   - Input: path to AST JSON, path to RPG source directory (or map from unit id to source file).
   - Output: `call_graph.json` per program and/or an enriched manifest with `calls`/`calledBy`.
2. **Run this script** after context build (or as part of the pipeline) so downstream steps (e.g. migration, enterprise design) can use the call graph.
3. **Later**, if the pipeline is stable, move this logic into the indexer (Option B) so the AST/source contract and the call graph are built in one place.

---

## 6. RPG Source Parsing Notes (for EXSR / BEGSR)

- **EXSR** in fixed-format: typically columns 27–34 (or similar) for the subroutine name; in free-format, the name is the token after `EXSR` (e.g. `EXSR CheckClaim;`).
- **BEGSR** in fixed-format: the **label** is usually in columns 7–14 (or 8–15); that label is the subroutine name. In free-format, the name may be on the same line as `BEGSR` or the previous line.
- Use `range.startLine` (and `startLinePP` if different) to index into the source; handle both fixed-format and free-format if your codebase uses both.

---

## 7. Summary

| Question | Answer |
|----------|--------|
| Are there edges in the AST? | **Yes!** There is an `edges` array with `"declares"` (40) and `"refers_to"` (1681) edges. |
| Are there `calls`, `reads_from`, `writes_to`, `controls` edges? | **No.** These edge kinds are **not** present in the AST. |
| Can we derive reads/writes/calls/controls? | **Yes!** From AST node opcodes (`CHAIN`, `UPDATE`, `WRITE` = reads/writes), `ExSr`/`CallP` nodes (calls), and `children` arrays (controls). |
| Is the call graph in the AST? | **No.** There are no `calls`/`calledBy` in the AST. |
| Is it “the AST” we’re looking at? | **Yes.** The file (e.g. HS1210-ast.json) is the PKS-RPG-FrontEnd AST. The manifest is derived from it but does not currently include call graph data. |
| Where is call information? | **Call sites:** AST nodes with `kind` **ExSr** or **CallP** (with range). **Callee names:** Procedures often in AST `props`; subroutines and EXSR target must be read from **RPG source** at the node’s line. |
| How to build the call graph? | **(1)** Build name→nodeId for Subroutine/Procedure (names from AST props or from BEGSR/P line in source). **(2)** For each ExSr/CallP, get callee name from source (and caller from AST parent/containment). **(3)** Output edges (caller, callee) and optionally enrich manifest. Prefer a **post-processing script** (AST + source) first; optionally move into the indexer later. |
| How to build reads/writes/controls? | **(1)** Scan AST for nodes with opcodes `CHAIN`/`READ`/`SETLL` → create `reads_from` edges. **(2)** Scan for `UPDATE`/`WRITE`/`DELETE` → create `writes_to` edges. **(3)** Use AST `children` arrays → create `controls` edges. **(4)** Optionally add these to the `edges` array or output a separate graph file. |

If you want, the next step can be a concrete design for `build_call_graph.py` (input/output format, how to find RPG source per unit, and how to wire it into the existing pipeline).
