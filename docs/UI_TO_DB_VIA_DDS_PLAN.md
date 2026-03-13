# Plan: Connecting UI to DB via Backend Using DDS Files

**Purpose:** Explain how the UI makes real queries to the database and how DDS (Display File) definitions flow through the pipeline to drive the UI.

---

## 1. Current Architecture (Already Working)

The pipeline **already connects** the UI to the DB. Here is the end-to-end flow:

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                    UI → Backend → DB DATA FLOW                                             │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                           │
│  DDS (HS1210D.DSPF)          Pipeline Step 1              UI Schema                       │
│  ┌─────────────────┐         ┌──────────────────┐        ┌─────────────────────────┐    │
│  │ Record formats  │         │ build_context_   │        │ HS1210D.json             │    │
│  │ Fields (IN-93,  │   →     │ index.py         │   →    │ dataSource.url:          │    │
│  │ SUB000, etc.)   │         │ displayFiles     │        │   /api/claims/search     │    │
│  │ CF03, CF05...   │         │ uiContracts      │        │ columns: claimNr,        │    │
│  └─────────────────┘         └──────────────────┘        │   rechNr, chassisNr...   │    │
│           │                            │                   └───────────┬─────────────┘    │
│           │                            │                               │                  │
│           ▼                            ▼                               ▼                  │
│  ┌─────────────────┐         ┌──────────────────┐        ┌─────────────────────────┐    │
│  │ HS1210D-ast.json│         │ context_index/    │        │ Angular UI               │    │
│  │ uiContracts     │         │ HS1210_n404.json  │        │ claims-list.component    │    │
│  │ recordFormats   │         │ displayFiles[]    │        │ - GET /api/ui-schemas/   │    │
│  └─────────────────┘         └──────────────────┘        │   HS1210D                │    │
│                                                                 │ - POST /api/claims/   │    │
│                                                                 │   search             │    │
│                                                                 └───────────┬─────────┘    │
│                                                                             │              │
│                                                                             ▼              │
│  ┌─────────────────────────────────────────────────────────────────────────────────────┐ │
│  │  Backend (Spring Boot)                                                                │ │
│  │  ClaimController → ClaimSearchService → ClaimRepository → JPA → H2/PostgreSQL        │ │
│  └─────────────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                           │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Data Flow (Step by Step)

### Step 1: DDS → Context (Pipeline Tab 1)

| Input | Process | Output |
|-------|---------|--------|
| `HS1210D-ast.json` (DDS AST) | `build_context_index.py` | `context_index/HS1210_n404.json` with `displayFiles` |
| Record formats (HS1210S1, HS1210S2, …) | Extracts `uiContracts.recordFormats`, `fields` | Structured display file metadata |
| Function keys (CF03, CF05, CF06, …) | Matched from RPG `sym.dspf.HS1210D` | Action definitions |

### Step 2: Context → Migration (Pipeline Tab 2)

| Input | Process | Output |
|-------|---------|--------|
| `displayFiles` in context | `migrate_to_pure_java.py` (LLM) | Java services, DTOs, controllers |
| `dbContracts` (HSG71LF2, HSAHKLF3, …) | LLM generates JPA entities | `Claim.java`, `ClaimRepository.java` |
| `uiContracts` (recordFormats, fields) | LLM generates UI schema + API | `HS1210D.json`, `ClaimController.java` |

### Step 3: UI Schema (HS1210D.json)

Location: `warranty_demo/src/main/resources/ui-schemas/HS1210D.json`

```json
{
  "screenId": "HS1210D",
  "type": "list",
  "dataSource": {
    "method": "GET",
    "url": "/api/claims/search",
    "params": { "companyCode": { "source": "fixed", "value": "001" } }
  },
  "columns": [
    { "id": "claimNr", "label": "Claim Nr.", "dtoField": "claimNr" },
    { "id": "rechNr", "label": "Invoice", "dtoField": "rechNr" },
    ...
  ]
}
```

- **dataSource.url** → Backend API endpoint
- **columns[].dtoField** → Maps to `ClaimListItemDto` fields returned by the API

### Step 4: Angular UI → Backend

| Component | Action | API Call |
|-----------|--------|----------|
| `UiSchemaService` | Load screen definition | `GET /api/ui-schemas/HS1210D` |
| `ClaimService` | Search claims | `POST /api/claims/search` with `{ companyCode, openClaimsOnly, ascending }` |
| `ClaimsListComponent` | Display data | Renders rows using `schema.columns` and `dtoField` mapping |

### Step 5: Backend → Database

| Layer | Component | Responsibility |
|-------|-----------|----------------|
| Controller | `ClaimController.searchClaims()` | Receives `ClaimSearchCriteria`, returns `List<ClaimListItemDto>` |
| Service | `ClaimSearchService.searchClaims()` | Fetches from DB, applies filters, maps to DTO |
| Repository | `ClaimRepository.findActiveClaimsByCompanyCode()` | JPA query → real SQL to H2/PostgreSQL |
| Entity | `Claim`, `ClaimError`, `Invoice`, … | Maps to tables (HSG71LF2, HSG73PF, HSAHKLF3, …) |

---

## 3. How to Verify Real DB Queries

### 3.1 Check H2 Console

1. Run app: `mvn spring-boot:run`
2. Open: http://localhost:8081/h2-console
3. Connect: `jdbc:h2:mem:warranty_db`, user `sa`, password empty
4. Run: `SELECT * FROM HSG71LF2;` (Claim table)

### 3.2 Check API Response

```bash
curl -X POST http://localhost:8081/api/claims/search \
  -H "Content-Type: application/json" \
  -d '{"companyCode":"001","openClaimsOnly":false}'
```

Returns JSON array of claim list items from the database.

### 3.3 Check Angular UI

1. Open: http://localhost:8081/angular/#/claims
2. UI loads schema from `/api/ui-schemas/HS1210D`
3. UI calls `POST /api/claims/search` with `companyCode: "001"`
4. Backend queries DB via `ClaimRepository`
5. Data is displayed in the table

---

## 4. Adding New DDS Screens / Real Queries

To connect a **new** DDS display file to the UI and DB:

### Phase A: Pipeline (DDS → Context → Java)

| Step | Action |
|------|--------|
| 1 | Ensure `HS1212D-ast.json` (or new DDS) exists in AST directory |
| 2 | Run Tab 1 (Build Global Context) – `displayFiles` will include the new DDS |
| 3 | Run Tab 2 (Migrate Feature) for the node that uses this display file |
| 4 | LLM generates: entities, repositories, services, controllers, DTOs, UI schema |

### Phase B: UI Schema

| Step | Action |
|------|--------|
| 1 | Create or update `ui-schemas/<ScreenId>.json` (e.g. `HS1212D.json`) |
| 2 | Set `dataSource.url` to the backend endpoint (e.g. `/api/claims/search` or new endpoint) |
| 3 | Set `columns[].dtoField` to match DTO field names |

### Phase C: Backend Endpoint

| Step | Action |
|------|--------|
| 1 | Add or extend controller (e.g. `ClaimController` or new controller) |
| 2 | Service calls repository → JPA executes SQL |
| 3 | Map entity to DTO and return JSON |

### Phase D: Angular Route

| Step | Action |
|------|--------|
| 1 | Add route in `app.routes.ts` for the new screen |
| 2 | Create component that loads schema and calls the API |
| 3 | Build: `cd warranty-ui && npm run build:spring` |
| 4 | Copy output to `warranty_demo/src/main/resources/static/angular/` |

---

## 5. DDS Field → DB Column Mapping

The mapping is established during migration:

| DDS (Display File) | RPG DB File | JPA Entity | DB Table |
|--------------------|-------------|------------|----------|
| HS1210D fields     | HSG71LF2    | Claim      | HSG71LF2 |
| Subfile data       | HSAHKLF3    | Invoice    | HSAHKLF3 |
| Error subfile      | HSG73PF     | ClaimError | HSG73PF  |

- **dbContracts** in context define which DB files a node uses
- **displayFiles** define which screens/record formats are used
- LLM generates entities with `@Column(name="...")` matching the DB schema
- Repositories use Spring Data JPA / custom `@Query` for real SQL

---

## 6. Summary: Is the UI Already Connected?

**Yes.** The current setup already:

1. Loads UI schema from `/api/ui-schemas/HS1210D` (derived from DDS)
2. Calls `POST /api/claims/search` with search criteria
3. Backend runs `ClaimRepository.findActiveClaimsByCompanyCode()` → real DB query
4. Returns `ClaimListItemDto[]` from DB data
5. Angular renders the table using `dtoField` mapping

**To add more screens or queries:**

1. Add new DDS to context (Tab 1)
2. Migrate the feature (Tab 2) – or manually add UI schema + controller
3. Ensure `dataSource.url` points to an endpoint that queries the DB
4. Add Angular route and component if needed

---

## 7. Quick Reference: Key Files

| File | Purpose |
|------|---------|
| `ui-schemas/HS1210D.json` | UI schema (columns, dataSource URL) – drives Angular |
| `ClaimController.java` | REST API – `/api/claims/search` |
| `ClaimSearchService.java` | Business logic – calls repository, maps to DTO |
| `ClaimRepository.java` | JPA – real SQL to DB |
| `Claim.java` | Entity – maps to HSG71LF2 table |
| `warranty-ui/.../claim.service.ts` | Angular – calls `/api/claims/search` |
| `warranty-ui/.../claims-list.component.ts` | Angular – loads schema, displays data |
| `HS1210D-ast.json` | DDS AST – source of record formats and fields |

---

*Plan ready. The UI is already connected to the DB; this document explains the flow and how to extend it for new DDS screens.*
