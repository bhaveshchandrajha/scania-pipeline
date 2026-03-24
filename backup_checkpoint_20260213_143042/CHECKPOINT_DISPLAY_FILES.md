# Checkpoint: Display Files Integration for UI Building

**Date:** February 13, 2026  
**Status:** Display file metadata integrated; awaiting DDS source files

---

## 1. What Has Been Completed

### 1.1 Display File Extraction from AST
- **Script:** `enrich_context_with_display_files.py`
- **Functionality:**
  - Reads AST files to find `sym.dspf.*` symbols (display files)
  - Resolves display file metadata from AST `files[]` array
  - Matches display files to context packages based on:
    - Node `sem` references
    - Node `outgoingEdges` references
    - AST top-level `edges[]` (src → sym.dspf.*)
  - Optionally loads DDS source from `--ddsDir` if provided
  - Adds `displayFiles` array to each context JSON

### 1.2 Context Package Enrichment
- **Command:** `python3 enrich_context_with_display_files.py --astDir JSON_ast/JSON_20260211 --contextDir context_index --attachUnitDspf`
- **Result:** 23 context files enriched with display file metadata
- **Display files found:**
  - `HS1210D` (used by HS1210 units)
  - `HS1212D` (used by HS1212 units)
- **Metadata included:**
  - `symbolId`: e.g., `sym.dspf.HS1210D`
  - `name`: e.g., `HS1210D`
  - `fileId`: e.g., `qsys:HSSRC/QDDSSRC/HS1210D`
  - `path`: e.g., `/QSYS.LIB/HSSRC.LIB/QDDSSRC.FILE/HS1210D.MBR`
  - `ddsSource`: **Not yet available** (awaiting DDS source files)

### 1.3 Migration Prompt Enhancement
- **File:** `migrate_with_claude.py`
- **Changes:**
  - Added `displayFiles` extraction from context
  - Added "Display files (DSPF) – for UI building" section to prompt
  - Includes display file names, symbolIds, fileIds
  - Includes DDS source (if available) up to 8000 chars
  - Added requirement #7: Use display files to inform UI-related code generation

### 1.4 Migration Script Improvements
- **Auto-output file naming:** Generated Java files now use same base name as context JSON
  - Input: `context_index/HS1210_n404.json`
  - Output: `HS1210_n404.java` (in cwd)
- **Timeout increases:** UI server and browser timeouts increased to 20 minutes for large contexts

### 1.5 Documentation
- **`AST_DISPLAY_FILES.md`:** Guide on how AST should represent display files
- **`README.md`:** Updated with enrichment workflow (step 3b)

---

## 2. Current Capabilities (Without DDS Source)

### 2.1 What We Have
- ✅ **Display file metadata:** Names, IDs, paths for `HS1210D`, `HS1212D`
- ✅ **Java services:** Migrated services that reference display files
- ✅ **RPG snippets:** Original RPG code showing EXFMT/READ/WRITE operations
- ✅ **Narrative:** Business logic descriptions
- ✅ **DB contracts:** Database schema information
- ✅ **Symbol metadata:** Variables, data structures referenced

### 2.2 What We Can Do Without DDS Source
- ✅ **Identify which services relate to which display files** (via context `displayFiles`)
- ✅ **Generate Java with UI-aware comments** (mentioning display file names)
- ✅ **Design Angular UI manually** based on:
  - Java service methods (operations)
  - Java DTOs/entities (data structures)
  - Narrative (business logic)
  - Variable names from RPG (field hints)
- ✅ **Infer screen structure** from:
  - Method names (e.g., `loadClaim`, `submitClaim`)
  - DTO field names
  - RPG variable names

### 2.3 What We Cannot Do Without DDS Source
- ❌ **Exact screen layout** (field positions, record formats)
- ❌ **Field attributes** (display-only, required, hidden)
- ❌ **Indicator definitions** (which indicators control what)
- ❌ **Screen grouping** (which fields appear together)
- ❌ **Automatic Angular form generation** from DDS structure

---

## 3. Next Steps (When DDS Source Arrives)

### 3.1 Enrichment with DDS Source
```bash
# Place DDS source files in directory structure:
# dds_source/QDDSSRC/HS1210D.mbr
# dds_source/QDDSSRC/HS1212D.mbr

python3 enrich_context_with_display_files.py \
  --astDir JSON_ast/JSON_20260211 \
  --contextDir context_index \
  --ddsDir dds_source \
  --attachUnitDspf
```

### 3.2 Enhanced Capabilities with DDS Source
- ✅ Parse DDS record formats → Angular form sections
- ✅ Extract field definitions → Angular form fields
- ✅ Map indicators → conditional UI logic
- ✅ Generate Angular components from DDS structure
- ✅ Preserve original screen layout in Angular UI

---

## 4. Testing Plan (Without DDS Source)

### 4.1 Test Migration with Display File Context
- [x] Enrich contexts with display file metadata
- [x] Verify prompt includes display files section
- [x] Run migration for `HS1210_n404` (has `HS1210D`)
- [ ] Check if generated Java includes UI-related comments/DTOs

### 4.2 Test Angular UI Design Workflow
- [ ] Identify Java service methods for `HS1210D`
- [ ] Map Java DTOs to potential Angular form fields
- [ ] Design Angular component structure manually
- [ ] Create Angular service calling Java REST API
- [ ] Build Angular form/list based on inferred structure

### 4.3 Document Findings
- [ ] What information is sufficient for UI design?
- [ ] What gaps exist without DDS source?
- [ ] What manual steps are required?

---

## 5. Files Modified/Created

### Modified Files
- `migrate_with_claude.py` - Added display files section to prompt, auto-output naming
- `ui_server.py` - Increased timeout to 20 minutes
- `ui_index.html` - Increased browser timeout to 20 minutes
- `README.md` - Added enrichment workflow documentation

### New Files
- `enrich_context_with_display_files.py` - Display file enrichment script
- `AST_DISPLAY_FILES.md` - AST structure guide for display files
- `CHECKPOINT_DISPLAY_FILES.md` - This checkpoint document

### Enriched Context Files
- 23 context JSON files in `context_index/` now include `displayFiles` array

---

## 6. Known Limitations

1. **No DDS source yet:** Cannot extract exact screen layouts
2. **AST edges incomplete:** Only "declares" edges exist; no "uses" edges from procedures
3. **Workaround in use:** `--attachUnitDspf` attaches all unit DSPFs to every context
4. **Manual UI design:** Angular UI must be designed manually without DDS source

---

## 7. Dependencies

- **AST files:** `JSON_ast/JSON_20260211/*-ast.json`
- **Context packages:** `context_index/*.json`
- **Python:** `enrich_context_with_display_files.py`, `migrate_with_claude.py`
- **Anthropic API:** For Java migration (requires `ANTHROPIC_API_KEY`)

---

**Next Action:** Test Angular UI generation workflow using Java services + display file metadata (without DDS source).
