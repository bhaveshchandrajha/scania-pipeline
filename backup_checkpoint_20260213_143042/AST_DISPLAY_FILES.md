# How display files (DSPF) should appear in the AST

This document describes how the AST should structurally represent display files so that the enrichment script (`enrich_context_with_display_files.py`) can attach them to context packages for UI-aware migration.

---

## 1. What the current AST (JSON_20260211) already has

- **Symbol table**: Display file symbols exist with `kind: "dspf"`, e.g.  
  `"sym.dspf.HS1210D": { "symbolId": "sym.dspf.HS1210D", "name": "HS1210D", "kind": "dspf" }`
- **files[]**: The AST lists files; display members are typically under a path like `qsys:HSSRC/QDDSSRC/HS1210D`. The script matches by member name (last segment of `id`) to get `fileId` and `path`.
- **One edge to DSPF**: Only a **declares** edge exists, e.g.  
  `{ "src": "n149", "dst": "sym.dspf.HS1210D", "kind": "declares" }`  
  So only the node that *declares* the display file (e.g. `n149`) is linked; procedure nodes that *use* it (EXFMT/READ) have no edge to `sym.dspf.*`.

So the AST **does** have display **symbols** and **one** node (the declaration) linked to them. It does **not** yet link **procedure nodes that use** the display.

---

## 2. How the enrichment script resolves “which display files for this node?”

The script considers:

1. **Context `astNode.sem`**: If the node’s `sem` object has keys starting with `sym.dspf.`, those count as refs.
2. **Context `astNode.outgoingEdges`**: If that array contains strings like `sym.dspf.HS1210D`, those count as refs.
3. **AST top-level `edges[]`**: For the context’s node id (e.g. `n1779`), any edge with `src == node_id` and `dst` starting with `sym.dspf.` is treated as a ref (any `kind`, e.g. `refers_to`, `uses`, `declares`).

So for a **procedure** context (e.g. `HS1210_n1779`), the script will attach display files only if:

- That node’s `sem` or `outgoingEdges` in the context include `sym.dspf.*`, **or**
- The AST has an edge `{ "src": "n1779", "dst": "sym.dspf.HS1210D", "kind": "..." }`.

Right now there is **no** such edge from `n1779` (or any other procedure node) to `sym.dspf.HS1210D`; only `n149` has a `declares` edge. So only the context for **n149** would get display files from edges. If your indexer only emits context for procedure nodes (not for `n149`), you get 0 enriched contexts unless you use `--attachUnitDspf`.

---

## 3. How the AST should represent “this procedure uses this display file”

To have **procedure nodes** (and thus their context packages) get the right display files **by structure**, the AST producer should do both of the following.

### 3.1 Keep display file symbols and files (already done)

- **symbolTable**: One entry per display file, e.g.  
  `"sym.dspf.HS1210D": { "symbolId": "sym.dspf.HS1210D", "name": "HS1210D", "kind": "dspf" }`
- **files[]**: One entry per member, with `id` (e.g. `qsys:HSSRC/QDDSSRC/HS1210D`) and `path` so the script can resolve `fileId`/path and optional DDS.

### 3.2 Emit edges from nodes that *use* the display file

For every AST node that **uses** the display file (e.g. EXFMT HS1210D, READ HS1210D, WRITE to the display, etc.), add an edge in the top-level **edges** array:

```json
{ "src": "<node_id_of_procedure_or_statement>", "dst": "sym.dspf.HS1210D", "kind": "refers_to" }
```

(or `"kind": "uses"` — the script does not filter on `kind` for edges).

- **src**: The AST node id that corresponds to the procedure (or the specific statement node) that references the display.
- **dst**: The display file symbol id, e.g. `sym.dspf.HS1210D`.
- **kind**: e.g. `refers_to` or `uses`. A `declares` edge (as for `n149`) is also picked up.

If the **indexer** that builds context packages copies “outgoing” edges from the AST (edges where `src` = this node) into `astNode.outgoingEdges`, then those procedure nodes will have `outgoingEdges` containing `sym.dspf.HS1210D`, and the enrichment script will find refs from the context as well.

---

## 4. Optional: per-node `sem` and `outgoingEdges` in the AST node

Some pipelines store semantic refs directly on the node in the AST:

- **sem**: A map whose **keys** can include symbol IDs. If the node uses the display file, include a key `sym.dspf.HS1210D` (value can be e.g. `{ "refersTo": "sym.dspf.HS1210D" }`).
- **outgoingEdges**: An array of symbol/node IDs. Including `"sym.dspf.HS1210D"` here also counts as a ref.

The enrichment script already reads both from the **context** `astNode`. So if the indexer copies `sem` and `outgoingEdges` from the AST node into the context, then having them in the AST is enough; the script will use them. The top-level **edges[]** are still useful because the script now also resolves refs from them when the context’s node has an id but might not have `outgoingEdges`/`sem` populated.

---

## 5. Summary table

| Location in AST | Purpose |
|-----------------|--------|
| **symbolTable** `sym.dspf.<NAME>` with `kind: "dspf"` | Defines the display file symbol. |
| **files[]** entry with `id` like `.../QDDSSRC/HS1210D` | Provides `fileId`/path and DDS lookup. |
| **edges[]** `{ "src": "<procedure_node_id>", "dst": "sym.dspf.HS1210D", "kind": "refers_to" }` | Links the procedure (or statement) that *uses* the display to the DSPF symbol. |
| **Node** `sem` / **outgoingEdges** (if indexer copies to context) | Alternative way to record that this node references `sym.dspf.*`. |

So: the current AST **does** have display **nodes** (symbols) and one **declares** edge. To have **procedure** contexts get display files by structure, the AST should **additionally** expose **edges from each procedure (or statement) that uses the display** to the corresponding `sym.dspf.*` (e.g. `refers_to` or `uses`). The enrichment script will then pick those up from the top-level `edges[]` and attach `displayFiles` to the right context packages.

---

## 6. Workaround without AST changes: `--attachUnitDspf`

If you cannot change the AST yet, run:

```bash
python3 enrich_context_with_display_files.py --astDir JSON_ast/JSON_20260211 --contextDir context_index --attachUnitDspf
```

This attaches **all** unit-level DSPFs (from the symbol table) to **every** context of that unit, so every migration for that program sees the display files and can use them for UI building.
