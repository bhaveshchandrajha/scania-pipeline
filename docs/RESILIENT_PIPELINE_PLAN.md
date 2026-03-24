# Resilient End-to-End Migration Pipeline Plan

**Version:** 1.0  
**Date:** March 2026  
**Goal:** A pipeline that successfully migrates the full application (DDS → Backend → DB → UI) every time, with auto-fix and user confirmation for any issues—**no manual error checking or fixing required**.

---

## 1. Vision

| Current State | Target State |
|---------------|--------------|
| Migration may produce compile errors, runtime errors, or UI mismatches | Migration produces a working app or surfaces fixable issues |
| User must manually find and fix errors | Pipeline detects issues and proposes fixes |
| Fixes are auto-applied without review | User confirms each fix before apply |
| No validation that UI actually shows data | End-to-end smoke test validates UI renders with DB data |

---

## 2. Failure Points & Auto-Fix Coverage

| Failure Point | Detection | Auto-Fix (LLM) | HITL |
|---------------|-----------|----------------|------|
| **Compile errors** | `mvn compile` fails | `fix_compile_errors.py` | ✓ User approves |
| **Runtime errors** (IdClass, duplicate column, etc.) | `mvn test` fails | `fix_runtime_errors.py` (new) | ✓ User approves |
| **UI schema vs DTO mismatch** | UI shows empty list; API returns data | Schema/DTO alignment fixer (new) | ✓ User approves |
| **API contract mismatch** | API returns 4xx/5xx or wrong shape | Controller/DTO fixer (new) | ✓ User approves |
| **DB not reachable** | Connection timeout | Use H2 default (already done) | N/A |

---

## 3. Pipeline Architecture (Resilient)

```
┌─────────────────────────────────────────────────────────────────────────────────────────────┐
│                    RESILIENT END-TO-END MIGRATION PIPELINE                                    │
├─────────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                               │
│  Tab 1: Build Global Context                                                                  │
│  Tab 2: Migrate Feature (LLM generates Java + UI schema)                                     │
│                                                                                               │
│  Tab 3: Build & Validate (unified)                                                            │
│  ┌───────────────────────────────────────────────────────────────────────────────────────┐  │
│  │  Step 3a: Build (mvn compile + package + test)                                          │  │
│  │           ↓ if fail                                                                     │  │
│  │  Step 3b: Error Analyzer → LLM Fix Proposal → HITL Panel → Apply on Approve            │  │
│  │           ↓ if fail                                                                     │  │
│  │  Step 3c: Rebuild (loop until success or max passes)                                     │  │
│  └───────────────────────────────────────────────────────────────────────────────────────┘  │
│                                                                                               │
│  Tab 4: Run & Smoke Test                                                                      │
│  ┌───────────────────────────────────────────────────────────────────────────────────────┐  │
│  │  Step 4a: Start app (mvn spring-boot:run)                                                │  │
│  │  Step 4b: Smoke test: GET /api/ui-schemas/HS1210D, POST /api/claims/search               │  │
│  │  Step 4c: If API returns empty but DB has data → UI mismatch → LLM Fix Proposal → HITL  │  │
│  │  Step 4d: Open UI, verify at least one row renders                                       │  │
│  └───────────────────────────────────────────────────────────────────────────────────────┘  │
│                                                                                               │
└─────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Implementation Phases

### Phase 1: HITL for Compile & Runtime Fixes (Foundation)

**Scope:** Extend existing build flow with HITL; add runtime error fixer.

| Task | Description |
|------|-------------|
| 1.1 | Add `--propose-only` to `fix_compile_errors.py` – return JSON instead of writing |
| 1.2 | Create `fix_runtime_errors.py` – parse `mvn test` output, LLM proposes fix, return JSON |
| 1.3 | Modify `_run_maven_build_with_autofix` to NOT auto-apply; return `{ needsReview, suggestedFixes }` |
| 1.4 | Add `POST /api/apply-approved-fixes` – apply human-approved fixes, then rebuild |
| 1.5 | Build HITL Review Panel in Tab 4 (Build) – show diff, Approve All / Reject |

**Ref:** `docs/HITL_AND_BUILD_SUCCESS_PLAN.md` (confirmed: HITL on by default, approve all at once, no edit, fallback to auto-apply when disabled)

---

### Phase 2: Prevention at Migration Time

**Scope:** Reduce issues by improving LLM prompts and adding validation.

| Task | Description |
|------|-------------|
| 2.1 | **IdClass prompt:** Add explicit instruction: "IdClass property names MUST exactly match entity @Id field names. Example: entity has ahk000, ahk010 → IdClass must have ahk000, ahk010, NOT g71000, g71010." |
| 2.2 | **DTO ↔ UI schema prompt:** Add instruction: "When generating UI schema (HS1210D.json), columns[].dtoField MUST match the exact JSON property names of the ClaimListItemDto (or equivalent) returned by the API. Use claimNumber not claimNr, invoiceNumber not rechNr, etc." |
| 2.3 | **Post-migration validation:** After migration, run a script that: (a) parses generated DTO record fields, (b) parses UI schema dtoField values, (c) reports mismatches. Optionally: LLM proposes fix. |

---

### Phase 3: UI Schema / DTO Alignment Auto-Fix

**Scope:** Detect and fix UI-empty-but-API-has-data scenarios.

| Task | Description |
|------|-------------|
| 3.1 | **Smoke test script:** `smoke_test_ui.py` – start app (or assume running), call `POST /api/claims/search` with companyCode=001, check response is non-empty. Call `GET /api/ui-schemas/HS1210D`, parse columns, verify each dtoField exists in first API response. |
| 3.2 | **Alignment fixer:** If dtoField mismatch detected: (a) read DTO source, extract field names; (b) read UI schema; (c) LLM or rule-based: update schema dtoField to match DTO. Return proposed fix. |
| 3.3 | **Integrate into pipeline:** After Run & Demo, run smoke test. If UI empty + API has data → show HITL with schema fix proposal. |

---

### Phase 4: End-to-End Validation Gate

**Scope:** Pipeline only succeeds when app runs and UI shows data.

| Task | Description |
|------|-------------|
| 4.1 | **Validation gate:** "Migration Success" = Build OK + Tests OK + App starts + Smoke test passes (API returns data, UI schema aligns). |
| 4.2 | **UI:** Show clear status: "Migrated" vs "Build OK, awaiting smoke test" vs "Issues detected – review fixes". |
| 4.3 | **Config:** `pipeline.strictMode` – if true, Run & Demo disabled until full validation passes. |

---

### Phase 5: Error Pattern Library

**Scope:** Predefined patterns for known issues to speed up fixes.

| Pattern | Detection | Fix Template |
|---------|-----------|--------------|
| IdClass property mismatch | `Property 'X.g71000' belongs to '@IdClass' but has no matching property` | Align IdClass field names with entity @Id fields |
| Duplicate column | `Column 'RESERVE' is duplicated` | Rename to RESERVE1, RESERVE2, etc. |
| Not a managed type | `Not a managed type: class XxxKey` | Change Repository to use entity type, not key type |
| UI schema dtoField mismatch | API returns `claimNumber`, schema has `claimNr` | Update schema dtoField to match DTO |
| DTO constructor order wrong | API returns wrong values in fields | Reorder mapToListItem parameters to match DTO |

**Implementation:** Store patterns in `known_issue_patterns.json`; Error Analyzer matches and suggests fixes (rule-based or LLM-assisted).

---

## 5. User Flow (No Manual Fixing)

1. **User:** Clicks "Migrate Feature" (Tab 2).
2. **Pipeline:** Runs migration; then Build & Validate (Tab 3).
3. **If build fails:** Error Analyzer runs → LLM proposes fix → HITL panel appears. User sees diff, clicks "Approve All". Fixes applied, rebuild runs.
4. **If build succeeds:** Run & Smoke Test (Tab 4). App starts. Smoke test runs.
5. **If smoke test fails (UI empty):** Alignment fixer proposes schema/DTO fix → HITL panel. User approves. Fix applied. Re-run smoke test.
6. **Success:** User sees "Migration complete. App running at http://localhost:8081. UI: http://localhost:8081/angular/#/claims".

**User never:** Opens IDE, manually edits files, or debugs. User only: Approves or rejects proposed fixes.

---

## 6. Configuration

```json
{
  "pipeline": {
    "strictMode": true,
    "requireSmokeTestPass": true
  },
  "hitl": {
    "enabled": true,
    "approveAllAtOnce": true,
    "fallbackToAutoApply": false
  },
  "autoFix": {
    "maxCompilePasses": 4,
    "maxRuntimePasses": 2,
    "maxAlignmentPasses": 1
  }
}
```

---

## 7. Implementation Order

| Step | Phase | Task | Effort |
|------|-------|------|--------|
| 1 | 1 | `--propose-only` in fix_compile_errors.py | 1–2 hrs |
| 2 | 1 | fix_runtime_errors.py | 1–2 hrs |
| 3 | 1 | Modify build flow to return suggested fixes instead of auto-apply | 2–3 hrs |
| 4 | 1 | HITL Review Panel (UI) | 2–3 hrs |
| 5 | 1 | POST /api/apply-approved-fixes | 1 hr |
| 6 | 2 | Migration prompts (IdClass, DTO/schema) | 1 hr |
| 7 | 2 | Post-migration validation script | 1–2 hrs |
| 8 | 3 | smoke_test_ui.py | 1–2 hrs |
| 9 | 3 | Alignment fixer | 2 hrs |
| 10 | 4 | Validation gate + UI status | 1 hr |
| 11 | 5 | known_issue_patterns.json + pattern matcher | 1–2 hrs |

**Total estimated:** ~15–20 hours.

---

## 8. Success Criteria

1. **Zero manual fixes:** User never edits Java, JSON, or config files to fix migration errors.
2. **HITL:** All fixes require user approval (when HITL enabled).
3. **End-to-end:** Pipeline considers success only when: build succeeds, tests pass, app runs, and UI shows at least one row from DB.
4. **Resilience:** Known issues (IdClass, duplicate column, dtoField mismatch) are detected and fixable via the pipeline.

---

## 9. Related Documents

- `docs/HITL_AND_BUILD_SUCCESS_PLAN.md` – HITL details, confirmed decisions
- `docs/UI_TO_DB_VIA_DDS_PLAN.md` – DDS → UI → DB flow
- `docs/DYNAMIC_ISSUES_MITIGATION.md` – Runtime error patterns
- `docs/PIPELINE_ROBUSTNESS.md` – Database and build robustness

---

*Plan ready for implementation. Start with Phase 1 (HITL foundation) to establish the confirmation flow, then add prevention and validation.*
