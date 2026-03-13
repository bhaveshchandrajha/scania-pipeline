# Pipeline Enhancement Plan: Test Generation & Repository Push

## Overview

Enhance the Global Context pipeline to:
1. **Generate test cases** based on the Warranty Claim Processing workflow
2. **Push generated Spring Boot application** to `https://github.com/bhaveshchandrajha/scania-java-v2.git` after successful validation (Step 3)
3. **Run tests and push test results** to a configurable test-results repository

---

## Current Pipeline Flow

| Step | Name | Action |
|------|------|--------|
| 1 | Build Global Context | Build context, DB registry, call graph, Neo4j export to `global_context/neo4j_export.cypher` |
| 2 | Migrate Feature | Generate Java (code gen only) |
| 3 | Validation | Run validation (logic completeness, traceability) |
| 4 | Build Application | Maven build, fix compile errors, **run tests** (build fails if tests fail) |
| 5 | Run & Demo | Start app, open demo |

**Dynamic issue mitigation:** Step 4 now gates on `mvn test` passing. This catches JPA mapping errors (duplicate column, "Not a managed type"), ApplicationContext failures, etc. before Run & Demo. See `docs/DYNAMIC_ISSUES_MITIGATION.md`.

---

## Proposed Enhanced Flow

### Phase A: Test Case Generation (after Step 2 / Step 4)

**When:** After the Java Spring Boot application is successfully generated and built (either at end of migrate-feature or at start of build-application).

**What:** A new script `generate_warranty_tests.py` (or integrated into pipeline) that:
- Reads the Warranty Claim Processing workflow spec (provided in your instructions)
- Generates JUnit 5 / Spring Boot Test classes covering:

| Test Category | Scenarios |
|---------------|-----------|
| **Phase 1 – Claim Creation** | |
| Valid workorder | Create claim with valid invoice/order/split/area/type |
| Workorder structure validation | Invoice Number (AHK010), Invoice Date (AHK020), Order Number (AHK040), Split (AHK070), Area (AHK050), Type (AHK060) must match |
| Repair date rule | Reject if workorder creation date (AHK620) > 19 days old |
| Duplicate prevention | Reject if claim already exists for workorder |
| Initial status | New claim has status 00 – OPEN |
| **Phase 2 – Failure Assignment** | |
| Mandatory failure | Claim cannot be transmitted without at least one failure |
| Max 9 failures | Enforce max 9 failures per claim |
| Mandatory failure fields | Description, Failed Part, Demand Code |
| **Phase 3 – Transmission** | |
| Send validation | At least one failure exists before SEND |
| Transmission success | Status changes to 10 – SENT on success |
| Transmission failure | Status unchanged, error displayed |

**Output:** Test classes under `warranty_demo/src/test/java/com/scania/warranty/`:
- `ClaimCreationWorkflowTest.java` – Phase 1 scenarios
- `ClaimFailureAssignmentTest.java` – Phase 2 scenarios  
- `ClaimTransmissionTest.java` – Phase 3 scenarios

**Integration point:** 
- Option A: New pipeline step "4a. Generate Tests" (between Validation and Build)
- Option B: Automatically after successful build in Step 4 (Build Application)
- **Recommended:** Run test generation as part of `/api/build-application` when build succeeds, before returning response

---

### Phase B: Push to Code Repository (after Step 3 – Validation)

**When:** After **Validation (Step 3)** passes successfully (user clicks "Run Validation" and validation returns PASSED).

**What:**
- Push the `warranty_demo/` project (or the generated code subset) to:
  - **Repository:** `https://github.com/bhaveshchandrajha/scania-java-v2.git`
  - **Branch:** New branch per run, e.g. `migration/HS1210_<timestamp>` or `migration/<programId>_<entryNodeId>_<date>`

**Implementation:**
- New endpoint: `POST /api/push-to-repo` (or integrated into validation response flow)
- Server-side logic:
  1. Clone or add remote for `scania-java-v2.git`
  2. Copy/rsync `warranty_demo/` contents to a temp clone
  3. `git add`, `git commit`, `git push origin <new-branch>`
- **Credentials:** Use `GIT_PUSH_TOKEN` or `GITHUB_TOKEN` env var (user must configure)
- **UI:** Add "Push to Repository" button in Step 3 (Validation) tab, enabled when validation passed

**Config (e.g. in `pipeline_config.json` or env):**
```json
{
  "codePushRepo": "https://github.com/bhaveshchandrajha/scania-java-v2.git",
  "codePushBranchPrefix": "migration/"
}
```

---

### Phase C: Run Tests & Push Test Results

**When:** After Build Application (Step 4) succeeds and tests exist.

**What:**
1. Run `mvn test` in `warranty_demo/`
2. Capture test results (JUnit XML, e.g. `target/surefire-reports/*.xml`)
3. Push test results to a **separate test-results repository**

**Test results format:**
- JUnit XML (Surefire default)
- Optional: JSON summary `{ "passed": N, "failed": N, "skipped": N, "total": N, "timestamp": "..." }`

**Push target:** Configurable – e.g. `https://github.com/bhaveshchandrajha/scania-test-results.git` (or similar – **you need to provide the URL**).

**Implementation:**
- Extend `/api/build-application` to:
  1. Run `mvn test` after `mvn package`
  2. Collect `target/surefire-reports/`
  3. Call new helper `push_test_results.py` which commits and pushes to test-results repo
- Or new endpoint: `POST /api/run-tests-and-push` (triggered after build)

**Config:**
```json
{
  "testResultsRepo": "https://github.com/<org>/scania-test-results.git",
  "testResultsBranch": "results"
}
```

---

## Summary of New Components

| Component | Purpose |
|----------|---------|
| `generate_warranty_tests.py` | Generate JUnit tests from workflow spec |
| `push_to_repo.py` | Push warranty_demo to scania-java-v2.git |
| `push_test_results.py` | Push test results to test-results repo |
| `pipeline_config.json` | Repo URLs, branch names, tokens (gitignored) |
| UI: "Push to Repository" button | In Step 3, after validation passes |
| UI: Test results display | In Step 4, show test summary after build |

---

## Configuration Required (Before Implementation)

1. **Code push repo:** `https://github.com/bhaveshchandrajha/scania-java-v2.git` ✓ (provided)
2. **Test results repo URL:** Please provide the repository URL for test results.
3. **Git credentials:** Pipeline will need `GITHUB_TOKEN` or `GIT_PUSH_TOKEN` with push access to both repos.

---

## Execution Order (Final)

1. **Step 2 – Migrate Feature:** Generate Java (code gen only, unchanged)
2. **Step 3 – Validation:** Run validation → **If PASSED → Push code to scania-java-v2.git**
3. **Step 4 – Build Application:** Maven build → **Generate tests (if not exist)** → **Run tests** → **Push test results**
4. **Step 5 – Run & Demo:** Unchanged

---

## Open Questions for Confirmation

1. **Test results repo:** What is the exact URL for the test results repository?
2. **Branch naming:** Prefer `migration/HS1210_20260206` or `migration/<programId>_<entryNodeId>_<timestamp>`?
3. **Test generation trigger:** Generate tests only once per migration, or regenerate every build?
4. **Scope of push:** Push entire `warranty_demo/` folder, or only `src/` (excluding target, etc.)?

---

Please confirm this plan and provide the test results repo URL. After confirmation, implementation will proceed.
