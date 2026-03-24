# Human-in-the-Loop (HITL) and Build Success Plan

**Version:** 1.0  
**Date:** March 2026  
**Purpose:** Address persistent build failures, introduce Human-in-the-Loop confirmation for LLM fixes, and ensure application build succeeds reliably.

---

## 1. Executive Summary

The pipeline currently has two gaps:

1. **Persistent dynamic issues** – Compilation succeeds, but `mvn test` fails due to JPA/Hibernate runtime errors (e.g. `@IdClass` property mismatch, duplicate columns, "Not a managed type"). These are not caught by `fix_compile_errors.py`, which only handles compile errors.

2. **No human confirmation** – LLM-suggested fixes are applied automatically. There is no opportunity for a human to review or reject changes before they are written to disk.

This plan proposes:
- **Human-in-the-Loop (HITL)** for all LLM-suggested fixes (compile and runtime)
- **Runtime error autofix** – Extend the pipeline to detect and propose fixes for test/ApplicationContext failures
- **Build success guarantee** – Stricter validation and optional "build-only" mode until human approves fixes

---

## 2. Current State Analysis

### 2.1 Build Flow (Tab 4)

```
mvn clean compile → [if fail] filename autofix → [if fail] LLM fix_compile_errors.py (up to 4 passes)
→ mvn package -DskipTests → mvn test
```

**Build success** = compile OK AND package OK AND tests OK.

### 2.2 Where It Fails Today

| Phase | Status | Issue |
|-------|--------|-------|
| Compile | ✓ Passes | `fix_compile_errors.py` handles compile errors |
| Package | ✓ Passes | JAR builds |
| Tests | ✗ Fails | ApplicationContext fails to load |

**Example root cause (from current build):**
```
Property 'com.scania.warranty.domain.Hsahkpf.g71000' belongs to an '@IdClass' 
but has no matching property in entity class 'com.scania.warranty.domain.Hsahkpf'
```

`HsahkpfKey` uses `g71000`, `g71010`, etc., while `Hsahkpf` entity uses `ahk000`, `ahk010`, etc. The `@IdClass` property names must exactly match the entity's `@Id` field names.

### 2.3 Current LLM Fix Flow (No HITL)

1. `fix_compile_errors.py` receives build log + file contents
2. LLM returns suggested code blocks
3. `parse_fixed_files()` extracts code
4. **Immediately writes to disk** – no human review

Same pattern in `fix_logic_gaps.py` (logic completeness fixes).

---

## 3. Proposed Solution

### 3.1 Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                    ENHANCED BUILD PIPELINE WITH HITL                               │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                   │
│  Build fails (compile or test)                                                    │
│         │                                                                         │
│         ▼                                                                         │
│  ┌──────────────────┐                                                            │
│  │ Error Analyzer   │  Parse mvn compile / mvn test output                         │
│  │ (compile + test) │  Extract error type, affected files, hints                  │
│  └────────┬─────────┘                                                            │
│           │                                                                       │
│           ▼                                                                       │
│  ┌──────────────────┐                                                            │
│  │ LLM Fix Generator │  Produce suggested fixes (no write)                        │
│  └────────┬─────────┘                                                            │
│           │                                                                       │
│           ▼                                                                       │
│  ┌──────────────────┐     ┌─────────────────────────────────────┐               │
│  │ HITL Review UI   │◀────▶│ Human: Approve / Reject / Edit       │               │
│  │ (new component)  │     │ - View diff, error context, files     │               │
│  └────────┬─────────┘     └─────────────────────────────────────┘               │
│           │                                                                       │
│           │  [Approve]                                                            │
│           ▼                                                                       │
│  ┌──────────────────┐                                                            │
│  │ Apply Fixes      │  Write approved changes to disk                             │
│  └────────┬─────────┘                                                            │
│           │                                                                       │
│           ▼                                                                       │
│  Rebuild (compile + test) → success or next iteration                             │
│                                                                                   │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Components

| Component | Purpose |
|-----------|---------|
| **Error Analyzer** | Parse `mvn compile` and `mvn test` output; classify as compile vs runtime; extract affected files and error hints |
| **Runtime Fix Generator** | New script/module (like `fix_compile_errors.py`) that feeds test/ApplicationContext errors to LLM and returns suggested fixes (without applying) |
| **HITL Review UI** | Modal or panel in pipeline UI showing: error summary, suggested diff, Approve/Reject buttons |
| **Fix Queue / Session** | Store pending LLM suggestions; apply only on human approval |

---

## 4. Detailed Plan

### Phase 1: Error Analysis & Runtime Fix Proposal (Backend)

**Goal:** Detect runtime errors and produce LLM-suggested fixes without applying them.

#### 4.1.1 Extend Error Detection

- **File:** `validate_before_run.py` or new `analyze_build_errors.py`
- **Input:** Full `mvn test` output (or `mvn compile` output)
- **Output:** Structured JSON:
  ```json
  {
    "errorType": "runtime|compile",
    "summary": "Property 'Hsahkpf.g71000' has no matching property in entity",
    "hint": "IdClass property names must match entity @Id field names",
    "affectedFiles": ["domain/Hsahkpf.java", "domain/HsahkpfKey.java"],
    "rawOutput": "..."
  }
  ```

- **Known patterns** (extend `KNOWN_ERROR_PATTERNS`):
  - `@IdClass` property mismatch
  - Duplicate column
  - Not a managed type
  - BeanCreationException / entityManagerFactory
  - Failed to load ApplicationContext

#### 4.1.2 Runtime Fix Generator (No Apply)

- **New script:** `fix_runtime_errors.py` (or extend `fix_compile_errors.py` with `--dry-run` / `--propose-only`)
- **Behavior:**
  - Accept build log (compile or test)
  - Call LLM with error context + file contents
  - Return suggested fixes as JSON (file path → new content)
  - **Do NOT write to disk**

#### 4.1.3 Modify `fix_compile_errors.py` for HITL

- Add `--propose-only` flag
- When set: return JSON with `{ "files": { "path": "content" }, "summary": "..." }` instead of writing files
- Enables reuse for both compile and runtime flows

---

### Phase 2: Human-in-the-Loop UI

**Goal:** Human reviews and approves/rejects LLM suggestions before any file write.

#### 4.2.1 API Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/build-application` | **Modified:** On build failure, return `needsReview: true` + `suggestedFixes` (no auto-apply) |
| POST | `/api/apply-approved-fixes` | Apply human-approved fixes; then rebuild |
| GET | `/api/pending-fixes` | Return current pending suggestions (for UI refresh) |

#### 4.2.2 Build Flow Change

**Current (auto-apply):**
```
Build fail → fix_compile_errors.py → write files → rebuild
```

**New (HITL):**
```
Build fail → analyze errors → LLM propose fixes → return to UI
→ Human reviews in HITL panel
→ Human clicks "Apply" → POST /api/apply-approved-fixes → write files → rebuild
→ Human clicks "Reject" → discard; user can fix manually
```

#### 4.2.3 HITL Review Panel (UI)

- **Location:** Tab 4 (Build Application), below build output
- **When visible:** When `needsReview: true` in build response
- **Content:**
  - Error summary (one-line)
  - List of files to be changed
  - Per-file: side-by-side diff (old vs new) or unified diff
  - Buttons: **Apply All**, **Reject** (approve all at once; no per-file or edit)
- **No editing:** Approve/Reject only; no in-place editing of suggested code

---

### Phase 3: Build Success Guarantee

**Goal:** Ensure build does not "fail at any cost" in the sense of leaving the pipeline in a broken state without a clear path forward.

#### 4.3.1 Strategies

1. **Never auto-apply without approval**
   - All LLM fixes require human confirmation
   - Reduces risk of bad fixes making things worse

2. **Prevention at migration time**
   - Strengthen `migrate_to_pure_java.py` prompts for `@IdClass` / composite key patterns
   - Add schema validation: IdClass fields must map 1:1 to entity @Id fields
   - Document common pitfalls (e.g. `HsahkpfKey` vs `Hsahkpf` naming)

3. **Validation before build**
   - Run `validate_before_run.py` (or equivalent) as part of Tab 3 or early in Tab 4
   - If validation fails, show hints before user clicks Build

4. **Build modes**
   - **Strict (default):** Build success = compile + package + test. No Run & Demo until success.
   - **Compile-only (optional):** For quick iteration; skip tests. User explicitly opts in.
   - **Bypass tests (temporary):** Config flag to allow Run & Demo even if tests fail (with clear warning). Use only for demos when runtime issues are known and acceptable.

5. **Clear failure messaging**
   - When build fails, show:
     - Error type (compile vs runtime)
     - Affected files
     - Link to HITL panel if fixes are proposed
     - Manual fix instructions for known patterns

---

### Phase 4: Implementation Order

| Step | Task | Dependencies |
|------|------|--------------|
| 1 | Add `--propose-only` to `fix_compile_errors.py` | None |
| 2 | Create `fix_runtime_errors.py` (or extend fixer) for test output | Step 1 |
| 3 | Extend `_run_maven_build_with_autofix` to NOT auto-apply; return suggested fixes | Step 1 |
| 4 | Add `/api/apply-approved-fixes` endpoint | Step 3 |
| 5 | Build HITL review panel in `ui_global_context.html` | Step 4 |
| 6 | Wire Tab 4 to show HITL panel on `needsReview` | Step 5 |
| 7 | Add runtime error patterns to analyzer | Step 2 |
| 8 | Strengthen migration prompts for IdClass | None (parallel) |
| 9 | Add `validate_before_run` to Tab 3 or Tab 4 pre-build | None |

---

## 5. Configuration Options

```json
{
  "hitl": {
    "enabled": true,
    "approveAllAtOnce": true,
    "allowEdit": false,
    "fallbackToAutoApply": true
  },
  "build": {
    "strictMode": true,
    "skipTestsOnBuild": false,
    "maxLlmPasses": 4
  }
}
```

- `hitl.enabled`: Master switch; **default true** (HITL on by default)
- `hitl.approveAllAtOnce`: Approve all suggested file changes in one action
- `hitl.allowEdit`: No editing of suggested code (Approve/Reject only)
- `hitl.fallbackToAutoApply`: When HITL disabled, revert to current auto-apply behavior
- `build.strictMode`: If true, Run & Demo disabled until build succeeds
- `build.skipTestsOnBuild`: If true, build success = compile + package only (use for quick iteration)

---

## 6. Risk Mitigation

| Risk | Mitigation |
|------|-------------|
| Human rejects all fixes, build stays broken | Clear manual fix instructions; link to docs; optional "Request new suggestion" to re-run LLM |
| LLM suggests wrong fix | HITL allows human to reject or edit before apply |
| HITL adds friction | Optional "Trust and apply" for users who want auto-apply (configurable) |
| Runtime fix generator produces bad output | Same HITL gate; human can reject |

---

## 7. Success Criteria

1. **HITL:** No LLM-suggested fix is written to disk without explicit human approval (when HITL enabled)
2. **Build path:** Clear path from "build failed" → "human reviews" → "apply" → "build succeeds"
3. **Runtime errors:** Pipeline can propose fixes for ApplicationContext / JPA errors, not just compile errors
4. **Documentation:** Known error patterns documented with hints and manual fix steps

---

## 8. Confirmed Decisions (March 2026)

| Question | Decision |
|----------|----------|
| HITL default | **HITL on by default** |
| Batch approval | **Approve all suggested changes at once** |
| Edit capability | **No editing** – Approve/Reject only |
| Fallback when HITL disabled | **Fallback to auto-apply** (current behavior) |
| Immediate Hsahkpf fix | **Applied** – one-time manual fix for `Hsahkpf`/`HsahkpfKey` IdClass mismatch |

### One-Time Manual Fix Applied

- **HsahkpfKey:** Renamed properties from `g71000`, `g71010`, etc. to `ahk000`, `ahk010`, etc. to match entity `@Id` field names. JPA requires IdClass property names to exactly match the entity's `@Id` fields.
- **HsahkpfRepository** and **V4ValidationService** unchanged – they use `@Param` names in the query; the IdClass is used only for entity identity mapping.

---

## 9. Related Documents

- `docs/DYNAMIC_ISSUES_MITIGATION.md` – Current mitigation strategies
- `docs/ARCHITECTURE_AND_PIPELINE.md` – Pipeline overview
- `docs/PIPELINE_TEST_AND_PUSH_PLAN.md` – Test generation and push

---

*Plan updated with confirmed decisions. Hsahkpf/HsahkpfKey fix applied. Ready for HITL implementation.*
