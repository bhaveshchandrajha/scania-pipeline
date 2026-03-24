# Testing: Angular UI Generation Without DDS Source

**Date:** February 13, 2026  
**Goal:** Test what we can accomplish for Angular UI generation using only:
- Migrated Java services
- Display file metadata (`HS1210D`, `HS1212D`)
- RPG snippets and narrative
- DB contracts and symbol metadata

---

## Test Case: HS1210D Display File → Angular UI

### Step 1: Analyze Generated Java Service

**File:** `HS1210_n404.java` (ClaimProcessingService)

**Findings:**
- ✅ Service already includes display file reference in JavaDoc:
  ```java
  /**
   * Display file: HS1210D
   * - Manages claim list display with filtering and sorting
   * - Supports claim creation, modification, deletion, and status changes
   * - Provides various selection options (2=Change, 4=Delete, 5=Display, ...)
   */
  ```

**Key Methods Identified:**
1. `initializeSubfileProcessing()` - Initialize screen state
2. `buildClaimSubfile()` - Load and display claim list
3. `processMarkSelection()` - Handle user selections
4. Various claim CRUD operations

**DTOs/Entities:**
- `SubfileContext` - Screen state
- `SubfileFilter` - Filter criteria
- `SubfileResult` - Display data
- Various entity classes (HSG71LF2, HSG73PF, etc.)

---

### Step 2: Map Java Service → Angular Components

**Angular Component Structure (Inferred):**

```
ClaimListComponent (for HS1210D)
├── ClaimListService (calls Java REST API)
├── Template:
│   ├── Filter Section (SubfileFilter)
│   ├── Subfile/Table (SubfileResult)
│   └── Action Buttons (2=Change, 4=Delete, etc.)
└── Component Logic:
    ├── loadClaims() → buildClaimSubfile()
    ├── handleSelection() → processMarkSelection()
    └── initialize() → initializeSubfileProcessing()
```

**REST API Endpoints Needed:**
- `GET /api/claims/subfile` - Load claim list (buildClaimSubfile)
- `POST /api/claims/initialize` - Initialize screen (initializeSubfileProcessing)
- `POST /api/claims/selection` - Handle selection (processMarkSelection)

---

### Step 3: Design Angular Form Fields (From Java DTOs)

**From `SubfileFilter`:**
- Filter fields (inferred from method parameters and narrative)

**From `SubfileResult`:**
- Claim list items (columns inferred from entities and narrative)

**From `SubfileContext`:**
- Screen state variables (mark11, mark12, zl4, etc.)

**Limitation:** Without DDS source, we don't know:
- Exact field labels
- Field positions/layout
- Required vs optional fields
- Field validation rules
- Indicator meanings

---

### Step 4: Create Angular Component Structure

**Files to Create:**
1. `claim-list.component.ts` - Component logic
2. `claim-list.component.html` - Template (form/table)
3. `claim-list.component.css` - Styles
4. `claim.service.ts` - HTTP service calling Java REST API
5. `claim.models.ts` - TypeScript interfaces matching Java DTOs

**Manual Steps Required:**
- Design form layout (without DDS field positions)
- Define TypeScript interfaces from Java DTOs
- Create REST API endpoints in Java (Spring MVC)
- Map Java entities to response DTOs
- Design UI/UX (modern web, not 5250 terminal)

---

## Test Plan

### Test 1: Extract Service Methods for HS1210D
- [x] Identify Java service methods related to display file
- [ ] Document method signatures and purposes
- [ ] Map to potential Angular operations

### Test 2: Design Angular Component Structure
- [ ] Create component outline (without implementation)
- [ ] Define TypeScript interfaces
- [ ] Design template structure (form/table)

### Test 3: Create REST API Layer
- [ ] Add Spring MVC controllers
- [ ] Map service methods to REST endpoints
- [ ] Create request/response DTOs

### Test 4: Build Angular Service
- [ ] Create Angular HTTP service
- [ ] Implement methods calling REST API
- [ ] Handle errors and loading states

### Test 5: Build Angular Component
- [ ] Create component with template
- [ ] Implement form/table display
- [ ] Add action handlers

### Test 6: Test End-to-End
- [ ] Verify data flow: Angular → REST → Java Service → DB
- [ ] Test CRUD operations
- [ ] Validate UI behavior matches RPG logic

---

## What We Can Infer Without DDS Source

### ✅ From Java Service:
- **Operations:** What actions the screen supports (load, filter, select, etc.)
- **Data structures:** DTOs and entities (fields, types)
- **Business logic:** Method names and narrative descriptions

### ✅ From RPG Snippets:
- **Variable names:** Field hints (e.g., `MARK12`, `SUB15X`)
- **Operations:** EXFMT, READ, WRITE patterns
- **Flow:** Subroutine calls and logic flow

### ✅ From Narrative:
- **Purpose:** What the screen does
- **User actions:** Selection options (2=Change, 4=Delete, etc.)
- **Business rules:** Filtering, sorting, validation logic

### ❌ What We Cannot Infer:
- **Exact layout:** Field positions, grouping
- **Field attributes:** Required, hidden, display-only
- **Indicators:** Which indicators control what
- **Screen sections:** Record format boundaries
- **Field labels:** User-facing text

---

## Next Steps

1. **Create Angular component structure** (manual design)
2. **Add REST API layer** to Java services
3. **Build Angular service** calling REST API
4. **Design Angular template** (modern web UI, not 5250)
5. **Test integration** end-to-end

---

## Expected Outcome

**With current data (no DDS source):**
- ✅ Functional Angular UI that works with Java backend
- ✅ Correct data structures and operations
- ✅ Modern web UI (not terminal-style)
- ❌ May not match original screen layout exactly
- ❌ Field labels and validation may need manual refinement

**With DDS source (future):**
- ✅ Exact screen layout preservation
- ✅ Field attributes and validation rules
- ✅ Indicator-based conditional logic
- ✅ Automatic form generation from DDS structure
