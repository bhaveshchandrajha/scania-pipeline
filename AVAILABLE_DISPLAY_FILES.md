# Display screen files in the provided ASTs

This document explains **which display files exist** in the ASTs you have and **what each screen is supposed to display**. No implementation—explanation only.

---

## 1. Where display files come from

- **RPG AST** (e.g. `JSON_ast/JSON_20260211/HS1210-ast.json`, `HS1212-ast.json`): defines **symbols** for display files (`sym.dspf.HS1210D`, `sym.dspf.HS1212D`) and which program uses them.
- **Context index** (`context_index/*.json`): after enrichment, each context has a **displayFiles** array listing those DSPFs (with fileId, path, and optionally ddsSource / uiContracts).
- **DDS delivery**: a **separate** delivery (e.g. folder `HS1210D_20260216`) can provide the **DDS AST** (`HS1210D-ast.json`) and **raw DDS** (`HS1210D.DSPF`) for a display file. That gives you record formats, fields, and layout for UI building.

So: “available” in **RPG AST** = two display files. “Available” with **full DDS (layout + fields)** = only one in the repo today.

---

## 2. Display files present in the RPG ASTs

| Display file   | RPG program | In AST | DDS delivered in repo? | What the screen is for |
|----------------|-------------|--------|------------------------|------------------------|
| **HS1210D**    | HS1210      | Yes (`sym.dspf.HS1210D` in `HS1210-ast.json`) | **Yes** – `HS1210D_20260216/` | Warranty claims **list / administration** (see below). |
| **HS1212D**    | HS1212      | Yes (`sym.dspf.HS1212D` in `HS1212-ast.json`) | **No** – no `HS1212D_*` folder | Claim **detail / editor** (create or edit a single claim). |

So in the **provided ASTs** there are **two** display screen files: **HS1210D** and **HS1212D**. Only **HS1210D** has DDS (and DDS AST) in the repo; **HS1212D** is only known by name and program reference.

---

## 3. What each display file is supposed to display

### 3.1 HS1210D (warranty claims list / administration)

- **Source:** RPG program **HS1210**; display file **HS1210D**.
- **Delivered:** Folder **HS1210D_20260216** with:
  - `HS1210D-ast.json` (DDS AST, dds-ast/1.0): record formats, fields, types.
  - `HS1210D.DSPF`: raw DDS (729 lines) – device size 24×80, subfile, function keys, literals.

**What it displays (from DDS and README_HS1210D_COMPREHENSION):**

- **Title:** “SDPS Verwaltung Garantieanträge” (warranty claims administration).
- **Main content:** A **subfile** (list) of claims:
  - One row per claim: Claim Nr., Rechnungs-Nr. (invoice), Datum, Chassis Nr., Kunde, Name, Dm, Status, Fe/An (errors), etc.
  - Record formats: **HS1210S1** (subfile rows), **HS1210C1** (subfile control).
- **Controls:** Filter (FILTERA, FILTER, FILART, FILTAG, SUCHEN, XXDAT), pagination (REC1, PAG1), sort (F16TXT, F17TXT).
- **Function keys:** CF03 Verlassen (Exit), CF05 Aktualisieren (Refresh), CF06 Erstellen (Create), CF09 Listenanfang, CF11 Ansicht, CF12 Zurück, CF16/CF17 Sortierung, CF19 Filter setzen.
- **Purpose:** List and select warranty claims, filter/sort, then choose an action (e.g. open claim detail / create new). So this is the **main claims list/selection screen**.

---

### 3.2 HS1212D (claim detail / editor)

- **Source:** RPG program **HS1212**; display file **HS1212D**.
- **Delivered in repo:** **No** DDS folder and no DDS AST. Only the **RPG AST** and **context_index** reference `sym.dspf.HS1212D` and `qsys:HSSRC/QDDSSRC/HS1212D`.

**What it is supposed to display (inferred from project docs, not from DDS):**

- **Role:** From `IMPLEMENTATION_GAP_ANALYSIS.md`, `SHARED_STATE_AND_CROSS_MODULE_FLOW.md`, `TEST_STRATEGY.md`:
  - HS1212 is the **Claim Editor** program.
  - Flow: HS1210 (claims list) → user chooses “Erstellen” (Create) or “Ändern” (Change) → **CALL HS1212** (claim detail).
- So **HS1212D** is the **claim detail / editor** screen: create or edit a single claim (header, positions, status, etc.). There is no DDS in the repo, so we do not know exact layout, record formats, or field names—only that it exists and is used by HS1212.

---

## 4. Summary

| Display file | In RPG AST? | DDS in repo? | Purpose (what it displays) |
|--------------|-------------|--------------|----------------------------|
| **HS1210D**  | Yes (HS1210) | Yes (HS1210D_20260216) | Warranty claims **list/administration**: subfile of claims, filters, sort, function keys (Exit, Refresh, Create, View, Back, Sort, Filter). |
| **HS1212D**  | Yes (HS1212) | No | Claim **detail/editor**: create or edit one claim (inferred from docs; no layout/fields in repo). |

So: **two** display screen files are available in the **provided ASTs**; only **one** of them (**HS1210D**) has full DDS (and thus is suitable for building a concrete UI from the AST/DDS). **HS1212D** is known as a display file and as the claim-editor screen conceptually, but without DDS you cannot derive its exact layout or fields from the current artifacts.
