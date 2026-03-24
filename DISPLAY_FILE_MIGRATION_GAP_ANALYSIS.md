# Display File Migration Context – Gap Analysis

**Date:** 2026-02-23  
**Scope:** PoC_20260211.zip (RPG AST) and HS1210D_20260216 delivery (DDS AST + DDS source)  
**Expectation baseline:** Scania – Process Approach and Schema Justification / Ways of working (initially shared expectation document), plus project display-file docs: `AST_DISPLAY_FILES.md`, `CHECKPOINT_DISPLAY_FILES.md`, `README_HS1210D_COMPREHENSION.md`.

---

## 1. Deliverables Considered

| Artifact | Description | Location / contents |
|----------|-------------|----------------------|
| **PoC_20260211.zip** | RPG AST delivery | Contains `HS1210-ast.json`, `HS1212-ast.json` (RPG ASTs, rpg-ast/1.0). Extracted equivalent: `JSON_ast/JSON_20260211/`. |
| **HS1210D_20260216** | Display file (DDS) delivery | Folder: `HS1210D_20260216/` with `HS1210D-ast.json` (DDS AST dds-ast/1.0), `HS1210D.DSPF` (raw DDS source), `README_HS1210D_COMPREHENSION.md`. A zip of this folder may be supplied as `HS1210D_20260216.zip`. |

---

## 2. Expectations for Display File Migration (Summary)

From the project’s display-file documentation, the following are expected for **display files to be migratable and to support UI building**:

1. **RPG AST**
   - **Symbol table:** Display file symbols with `kind: "dspf"` (e.g. `sym.dspf.HS1210D`).
   - **files[]:** One entry per display file with `id` (e.g. `qsys:HSSRC/QDDSSRC/HS1210D`) and `path` for DDS lookup.
   - **Edges:** For each procedure (or statement) that **uses** the display file (EXFMT, READ, WRITE, etc.), an edge `{ "src": "<procedure_node_id>", "dst": "sym.dspf.HS1210D", "kind": "refers_to" }` (or `"uses"`).

2. **DDS source (optional but recommended)**
   - Raw DDS (e.g. `.DSPF` / `.mbr`) available under a `--ddsDir` so the enricher can attach **ddsSource** to context packages (screen layout, keywords, literals).

3. **DDS AST (optional but recommended for UI)**
   - DDS AST (version `dds-ast/1.0`) with **uiContracts** (recordFormats, fields, types, keywords) for DTOs, form fields, and validation.
   - Used as `--ddsAstDir` so the enricher can attach **uiContracts** to display file entries in context.

4. **Context enrichment**
   - Enricher (`enrich_context_with_display_files.py`) adds a **displayFiles** array to each context package, with optional **ddsSource** and **uiContracts**, so the migration prompt gets a “Display files (DSPF) – for UI building” section.

5. **Pipeline behaviour**
   - Procedure-level context packages (e.g. HS1210_n404, HS1210_n1779) should receive the correct display file(s) **by structure** (edges from procedure nodes to `sym.dspf.*`), so that UI-aware migration and UI generation can be scoped per procedure without attaching all unit DSPFs to every context.

---

## 3. What Is Present (Current State)

### 3.1 PoC_20260211 (RPG AST)

| Expectation | Status | Evidence |
|-------------|--------|----------|
| symbolTable entry `sym.dspf.HS1210D` with `kind: "dspf"` | ✅ Present | HS1210-ast.json contains `"sym.dspf.HS1210D": { "symbolId": "sym.dspf.HS1210D", "name": "HS1210D", "kind": "dspf" }`. |
| files[] entry for display file (fileId/path) | ✅ Present | `"id": "qsys:HSSRC/QDDSSRC/HS1210D"` and path in `files[]`. |
| Edges from **procedure nodes that use** the display to `sym.dspf.HS1210D` | ❌ Missing | Only one edge to `sym.dspf.HS1210D`: `{ "src": "n149", "dst": "sym.dspf.HS1210D", "kind": "declares" }`. So only the node that **declares** the display file is linked; no `refers_to` / `uses` edges from procedure nodes that perform EXFMT/READ/WRITE on HS1210D. |

**Conclusion (RPG AST):** Structure for **declaration** is correct; structure for **use** is missing. Procedure-level context packages do not get display files by graph structure; the pipeline relies on the workaround `--attachUnitDspf` to attach all unit-level DSPFs to every context.

### 3.2 HS1210D_20260216 (DDS delivery)

| Expectation | Status | Evidence |
|-------------|--------|----------|
| DDS AST (dds-ast/1.0) with uiContracts | ✅ Present | `HS1210D-ast.json`: `version: "dds-ast/1.0"`, `uiContracts.displayFiles[0]` with recordFormats (e.g. HS1210S1, HS1210C1), fields (name, typeId, row, column, keywords). |
| Raw DDS source for ddsSource | ✅ Present | `HS1210D.DSPF` (729 lines) in the same folder; enricher can use folder as `--ddsDir` (and finds HS1210D.DSPF). |
| Record formats and field metadata | ✅ Present | recordFormats with keywords (SFL, SFLNXTCHG, etc.), fields with typeId (e.g. t.char.8), some row/column, keywords (DSPATR, COLOR). |
| Packaged as zip (HS1210D_20260216.zip) | ⚠️ Optional | Folder exists; a zip of this folder may be used for delivery. Not required for pipeline if folder path is used. |

**Conclusion (DDS delivery):** Meets expectations for DDS AST and DDS source. No structural gaps identified for the current pipeline.

---

## 4. Gaps (What Is Missing)

### 4.1 RPG AST (PoC_20260211) – Critical for correct scoping

| Gap | Description | Impact |
|-----|-------------|--------|
| **No “uses” edges from procedures to display file** | The AST has only a `declares` edge from node `n149` to `sym.dspf.HS1210D`. Procedure nodes that actually use HS1210D (e.g. EXFMT HS1210D, READ, WRITE) have no edge to `sym.dspf.HS1210D`. | Procedure-level context packages (e.g. HS1210_n1779, HS1210_n404) do not get display files by structure. The enricher cannot infer “this procedure uses this display” from the graph. Workaround: `--attachUnitDspf` attaches all unit DSPFs to every context, which is correct for “see all display files” but does not satisfy “only attach display files used by this procedure.” |
| **Recommendation** | AST producer (PKS-RPG-FrontEnd or downstream) should emit, for every node that **uses** the display file (EXFMT/READ/WRITE/etc.), an edge: `{ "src": "<procedure_or_statement_node_id>", "dst": "sym.dspf.HS1210D", "kind": "refers_to" }` (or `"uses"`). See `AST_DISPLAY_FILES.md` §3.2. | Enables procedure-scoped display file attachment and aligns with the “Process Approach” expectation that migration and UI context are tied to the procedures that use the screen. |

### 4.2 Optional refinements (not blocking)

| Item | Status | Note |
|------|--------|------|
| **Row/column in DDS AST** | Some fields have `row: 0, column: 0` | Better row/column coverage would improve layout-aware UI generation; current data is sufficient for field names, types, and keywords. |
| **HS1210D_20260216.zip** | Not found in repo; folder exists | If delivery is by zip, ensure the zip contains `HS1210D-ast.json`, `HS1210D.DSPF`, and (optionally) README. Pipeline can use either folder or extracted zip. |
| **Scania Process Approach document** | Not parsed in this analysis | Expectations above are taken from project docs. Any additional process/schema or “ways of working” criteria from “Scania - Process Approach and Schema Justification” should be checked against this list and added as further acceptance criteria if needed. |

---

## 5. Alignment with “Display File Migration Context” Expectation

- **RPG AST (PoC_20260211):** **Partially aligned.** Symbols and files[] are correct; **missing** edges from procedure/statement nodes that **use** the display file to `sym.dspf.*`. This is the only structural gap for display file migration context.
- **HS1210D_20260216:** **Aligned.** DDS AST and DDS source are present and match the expected format; enricher and migration prompt can use ddsSource and uiContracts as designed.

---

## 6. Recommended Next Steps

1. **AST producer:** Add **refers_to** (or **uses**) edges from each procedure (or statement) node that references the display file (EXFMT, READ, WRITE, etc.) to the corresponding `sym.dspf.HS1210D` (and similarly for HS1212D if applicable). Re-deliver RPG AST (e.g. in a future PoC or patch).
2. **Pipeline:** Continue using `--ddsDir HS1210D_20260216` and `--ddsAstDir HS1210D_20260216` when building context packages so that **ddsSource** and **uiContracts** are attached; once AST has “uses” edges, consider making `--attachUnitDspf` optional for HS1210/HS1212.
3. **Documentation:** If “Scania - Process Approach and Schema Justification” defines further display-file or UI migration criteria, add them to this analysis and to `CHECKPOINT_DISPLAY_FILES.md` / `AST_DISPLAY_FILES.md` as the single expectation baseline.

---

## 7. References

- **AST_DISPLAY_FILES.md** – How display files should appear in the AST; edges from nodes that use the display.
- **CHECKPOINT_DISPLAY_FILES.md** – Display file integration status; what is possible with/without DDS source.
- **README_HS1210D_COMPREHENSION.md** – Contents of HS1210D_20260216; use as `--ddsDir` / `--ddsAstDir`.
- **enrich_context_with_display_files.py** – Enricher that attaches displayFiles (and optionally ddsSource, uiContracts) to context packages.
- **Scania - Process Approach and Schema Justification / Ways of working** – Initial expectation document (referenced; not parsed in this analysis).
