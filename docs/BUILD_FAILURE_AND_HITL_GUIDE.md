# Build Failure Analysis & Human-in-the-Loop (HITL) Guide

**Purpose:** Answer why the build fails, whether H2/mock data can help, and how to introduce HITL for LLM fixes.

---

## 1. Why Is the Build Still Not Successful?

**Build success** = compile OK + package OK + **tests OK**.

The current failure is **test failures**, not compile or package. Tests fail because:

### Root cause: `createClaimFromInvoice` returns `null`

`ClaimManagementService.createClaimFromInvoice()` does:

```java
String claimNr = claimCreationService.generateClaimNumber(...);  // returns "00000001"
return claimRepository.findByPakzAndClaimNr(companyCode, claimNr).orElse(null);
```

It **never creates a claim**. It only:
1. Generates a placeholder claim number (`"00000001"`)
2. Looks up that claim in the DB
3. Returns `null` because no claim was created

### DataInitializer skips in test profile

`DataInitializer` seeds Invoice + Claim for demo, but **skips seeding when `isTestProfile()` is true**:

```java
if (existingClaims.isEmpty() && !isTestProfile()) {
    // create and save Claim
}
```

So in tests:
- No claim is seeded
- `createClaimFromInvoice` returns `null`
- Tests fail with `NullPointerException`

### Summary

| Issue | Cause |
|-------|--------|
| H2 DB | H2 works; schema is created. No data-fetch issue. |
| Missing data | DataInitializer skips seeding in test profile |
| Logic gap | `createClaimFromInvoice` does not create a claim; it only looks up |

---

## 2. Can We Use Mock Data?

Yes. Several options:

### Option A: Seed in test profile

Change `DataInitializer` to seed claims in test profile too:

```java
if (existingClaims.isEmpty()) {  // remove && !isTestProfile()
    // seed Claim
}
```

Then `findByPakzAndClaimNr("001", "00000001")` will find the seeded claim.

### Option B: Mock `ClaimManagementService`

```java
@SpringBootTest
@ActiveProfiles("test")
class ClaimFailureAssignmentTest {
    @MockBean
    ClaimManagementService claimManagementService;

    @BeforeEach
    void setup() {
        Claim mockClaim = new Claim();
        mockClaim.setClaimNr("00000001");
        mockClaim.setPakz(SEED_COMPANY);
        when(claimManagementService.createClaimFromInvoice(...)).thenReturn(mockClaim);
    }
}
```

### Option C: Skip tests for build success (temporary)

Add a config flag so build success = compile + package only (no tests). Use when tests are known to be broken and you want Run & Demo to work.

---

## 3. How to Introduce Human-in-the-Loop (HITL)

The plan is in `docs/HITL_AND_BUILD_SUCCESS_PLAN.md`. Here is a condensed implementation path.

### Current flow (auto-apply)

```
Build fail → fix_compile_errors.py → write files → rebuild
```

### Target flow (HITL)

```
Build fail → analyze errors → LLM propose fixes → return to UI
→ Human reviews in HITL panel
→ Human clicks "Apply" → POST /api/apply-approved-fixes → write files → rebuild
→ Human clicks "Reject" → discard; user can fix manually
```

### Implementation steps

#### Step 1: Add `--propose-only` to `fix_compile_errors.py`

- When `--propose-only` is set: return JSON with suggested fixes instead of writing files
- Output: `{ "files": { "path": "content" }, "summary": "..." }`

#### Step 2: Modify build API

- **Before:** On compile failure → call `fix_compile_errors.py` → auto-apply → rebuild
- **After:** On compile failure → call `fix_compile_errors.py --propose-only` → return `needsReview: true` + `suggestedFixes` (no write)

#### Step 3: Add HITL endpoints

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/apply-approved-fixes` | Apply human-approved fixes; then rebuild |
| GET | `/api/pending-fixes` | Return current pending suggestions |

#### Step 4: Add HITL UI panel

- **Location:** Tab 4 (Build Application), below build output
- **When visible:** When `needsReview: true` in build response
- **Content:**
  - Error summary
  - List of files to be changed
  - Per-file diff (old vs new)
  - Buttons: **Apply All**, **Reject**

#### Step 5: Extend runtime error handling

- Parse `mvn test` output for ApplicationContext / runtime errors
- Call LLM fix generator (same pattern as compile errors)
- Return suggested fixes for HITL instead of auto-applying

### Quick reference

| File | Change |
|------|--------|
| `fix_compile_errors.py` | Add `--propose-only`; return JSON |
| `ui_global_context_server.py` | On build fail: return `needsReview` + `suggestedFixes`; add `/api/apply-approved-fixes` |
| `ui_global_context.html` | Add HITL panel; show diff; Apply/Reject buttons |

---

## 4. Recommended Next Steps

1. **Short term:** Fix build by either:
   - Seeding data in test profile (Option A), or
   - Using `-DskipTests` for build (compile + package only).

2. **Medium term:** Implement HITL:
   - Phase 1: `--propose-only` + build API changes
   - Phase 2: HITL UI panel

3. **Long term:** Fix `createClaimFromInvoice` so it actually creates claims (or calls the correct creation logic).
