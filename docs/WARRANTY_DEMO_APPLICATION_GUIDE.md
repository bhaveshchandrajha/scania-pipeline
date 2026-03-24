# Warranty Demo Application Guide

**Purpose:** Map the generated Java application to business features and RPG source, so you can quickly find which files govern execution and how they relate to claim creation, search, validation, etc.

---

## 1. Entry Point and Execution Flow

```
WarrantyApplication.java (main)
    └── Spring Boot starts → scans com.scania.warranty
        └── Discovers all @RestController, @Service, @Repository
            └── HTTP requests → Controllers → Services → Repositories → DB
```

**Main class:** `com.scania.warranty.WarrantyApplication`  
**Run:** `mvn spring-boot:run` or `java -jar target/warranty-claim-management-1.0.0.jar`  
**Port:** 8081

---

## 2. Key Programs by Business Feature

| Business Feature | Key Java Files | API Endpoints | RPG Origin |
|------------------|----------------|---------------|------------|
| **Claim creation** (from invoice) | `ClaimCreationController`, `ClaimCreationService` | `POST /api/claims/create`, `POST /api/claims/create-from-invoice` | HS1210 n404 (main subfile) → CreateClaim logic |
| **Claim list / search** | `ClaimController`, `ClaimSearchService` | `GET /api/claims/search`, `POST /api/claims/search` | HS1210 n404 |
| **Claim actions** (delete, update status, book minimum, release) | `ClaimController`, `ClaimSearchService` | `POST /api/claims/delete`, `/update-status`, `/book-minimum`, `/release-request` | HS1210 n404 |
| **Check claim** (validation) | `CheckClaimController`, `CheckClaimService` | `POST /api/claims/check` | HS1210 n1919 (CheckClaim procedure) |
| **Check V4** (V4 validation) | `CheckV4Controller`, `CheckV4Service` | `POST /api/claims/check-v4` | HS1210 n1983 (CheckV4 procedure) |
| **Subfile / list state** | `ClaimSubfileController`, `ClaimSubfileService` | `GET /api/claims/subfile` | HS1210 n404 |
| **Invoices** | `InvoiceController` | `GET /api/invoices` | HS1210 n404 (HSAHKLF3) |
| **UI schemas** | `UiSchemaController` | `GET /api/ui-schemas/{screenId}` | DDS display files |
| **Seed data** (dev only) | `SeedController` | `POST /api/seed`, `POST /api/seed-invoices` | Config, not migrated |

---

## 3. RPG-to-Java Traceability

### 3.1 Main entry: HS1210 n404 (Subroutine)

The main subfile logic in RPG program **HS1210** lives in node **n404**. This single RPG subroutine was migrated into multiple Java layers:

| Java Layer | Files (all from n404) | Role |
|------------|------------------------|------|
| **web** | `ClaimController`, `ClaimCreationController`, `ClaimSubfileController` | HTTP entry points |
| **service** | `ClaimSearchService`, `ClaimCreationService`, `ClaimSubfileService` | Business logic |
| **repository** | `ClaimRepository`, `InvoiceRepository`, `LaborRepository`, `ClaimErrorRepository`, `ExternalServiceRepository`, `ClaimReleaseRequestRepository` | Data access |
| **domain** | `Claim`, `Invoice`, `Labor`, `ClaimError`, `ClaimReleaseRequest`, `ClaimSearchCriteria`, `ClaimStatus`, etc. | Entities and value objects |
| **dto** | `ClaimListItemDto`, `ClaimSearchRequestDto`, `ClaimCreationRequestDto`, `ClaimDeleteRequestDto`, etc. | Request/response shapes |

### 3.2 Called procedures (from n404)

| RPG Node | Procedure Name | Java Files | API |
|----------|----------------|------------|-----|
| **n1919** | CheckClaim | `CheckClaimController`, `CheckClaimService` | `POST /api/claims/check` |
| **n1983** | CheckV4 | `CheckV4Controller`, `CheckV4Service` | `POST /api/claims/check-v4` |

### 3.3 How to find the RPG origin of any Java file

1. **Javadoc:** Look at the top of the file:
   ```java
   /**
    * Generated from RPG: unit {@code HS1210}, node {@code n404}.
    */
   ```
2. **Inline trace:** Look for `// @rpg-trace: nXXX` comments on individual statements.
3. **Context index:** `context_index/HS1210_n404.json` contains the full context package for n404.

---

## 4. Claim Creation Flow (End-to-End)

```
User (Angular) → POST /api/claims/create-from-invoice
    → ClaimCreationController.createClaimFromInvoice()
    → ClaimCreationService.createClaimFromInvoice()
        → InvoiceRepository.findByKey()           // find invoice
        → InvoiceRepository.findStornoByKey()     // check cancellation
        → ClaimRepository.findByInvoiceKey()      // check existing claim
        → ClaimRepository.findMaxClaimNrByCompany()  // next claim number
        → new Claim() + claimRepository.save()   // create and persist
    ← claim number
```

**Key files for claim creation:**
- `ClaimCreationController.java` — HTTP handler
- `ClaimCreationService.java` — Core logic (invoice lookup, validation, claim creation)
- `ClaimRepository.java` — Claim persistence
- `InvoiceRepository.java` — Invoice lookup

---

## 5. Package Structure Overview

```
com.scania.warranty/
├── WarrantyApplication.java      # Main entry
├── config/                       # Spring config (DataInitializer, WebMvcConfig)
├── domain/                       # JPA entities, enums, value objects
├── dto/                          # Request/response DTOs
├── repository/                   # JPA repositories (data access)
├── service/                     # Business logic
└── web/                          # REST controllers (HTTP layer)
```

**Rule of thumb:** To understand a feature, start with the **web** controller, then follow the **service** it calls.

---

## 6. Quick Reference: "Where is X?"

| I want to find... | Look in... |
|-------------------|------------|
| Where the app starts | `WarrantyApplication.java` |
| Claim creation logic | `ClaimCreationService.createClaimFromInvoice()` |
| Claim search/list logic | `ClaimSearchService.searchClaims()` |
| Check-claim validation | `CheckClaimService` |
| V4 validation | `CheckV4Service` |
| Database schema (entities) | `domain/` package |
| API endpoints | `web/` package (each controller has `@RequestMapping`, `@GetMapping`, `@PostMapping`) |
| RPG traceability | Javadoc `Generated from RPG: unit X, node nYYY` or `// @rpg-trace: nYYY` |

---

## 7. Related Documentation

| Document | Description |
|----------|-------------|
| `context_index/manifest.json` | Full list of migrated nodes (unitId, nodeId, kind, calls, calledBy) |
| `TECHNICAL_ARCHITECTURE_AND_CONTEXT_BUILDING.md` | Pipeline, BFS migration, prompt |
| `POC_USER_GUIDE.md` | How to run the app and use the UI |
