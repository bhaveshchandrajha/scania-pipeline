# Warranty Claim System – Table Reference

These tables are derived from the Scania RPG warranty programs (HS1210, HS1212, etc.) and represent the IBM i / DB2 physical files.

| Table | Purpose | Key Entities |
|-------|---------|---------------|
| **HSAHKLF3** | Invoice header | Invoice, InvoiceHeader, Hsahklf3 – customer invoices with order, date, customer data |
| **HSAHKPF** | Invoice key / header (alternate view) | Hsahkpf – used by V4 validation for claim creation lookups |
| **HSAHWPF** | Work position / work order lines | Hsahwpf, WorkPosition – work order line items linked to invoices |
| **HSEPAF** | EPA (external) parameters | Hsepaf – used by V4 validation for external service parameters |
| **HSFLALF1** | External service line items | Hsflalf1, ExternalService – external service / Bes-Auftrag lines |
| **HSG70F** | Warranty release / submission deadline | Hsg70f, WarrantyRelease, ReleaseRequest – release requests and deadlines |
| **HSG71LF2** | Claim list view (logical) | Hsg71lf2 – claim list view for subfile display |
| **HSG71PF** | Claim header | Claim, ClaimHeader – main warranty claim records |
| **HSG73PF** | Claim error / failure details | ClaimError, ClaimFailure, Hsg73pf – failure lines per claim |
| **HSGPSLF3** | Position schedule / GPS lines | PositionSchedule, PositionLine, ClaimPosition – schedule/position data for claim creation |
| **ITLSMF3** | Item master / standard material | ItemMaster, StandardMaterial – item/material master data |

---

## Details

### HSAHKLF3 – Invoice header
- Customer invoices with order number, date, customer, vehicle, etc.
- Used by claim creation to validate invoice exists before creating a claim.
- **Invoice** entity (simplified) and **Hsahklf3** (full RPG schema).

### HSAHKPF – Invoice key header
- Alternate invoice header key structure used by V4 validation.
- Used when checking claim creation eligibility.

### HSAHWPF – Work position / work order lines
- Work order line items linked to invoices.
- Used by claim subfile display and work order lookups.

### HSEPAF – EPA parameters
- External parameters for validation.
- Used by V4ValidationService.

### HSFLALF1 – External service line items
- External service / Bes-Auftrag lines.
- Used by claim subfile and external service display.

### HSG70F – Warranty release
- Warranty release requests and submission deadlines.
- Used for release status and deadline checks.

### HSG71LF2 – Claim list view
- Logical view over claims for subfile display.
- Used by ClaimSubfileService for list display.

### HSG71PF – Claim header
- Main warranty claim records (company, invoice, claim number, date, status, etc.).
- Used by ClaimRepository and ClaimSearchService.

### HSG73PF – Claim error / failure details
- Failure lines per claim (error number, group, amounts, etc.).
- Used by ClaimErrorRepository.

### HSGPSLF3 – Position schedule
- Schedule / position data for claim creation.
- Used by ClaimCreationService and PositionScheduleRepository.

### ITLSMF3 – Item master
- Item/material master data.
- Used by ClaimCreationService for part lookups.

---

## Dummy data for demo

- **Invoices (HSAHKLF3):** 4 invoices – 12345, 99999, 88888, 77777 (company 001, date 20240115).
- **Claims (HSG71PF):** 2 claims – for invoices 12345 and 99999.
- **Claim errors (HSG73PF):** 1 failure line per claim.
- **Create demo:** Invoices 88888 and 77777 have no claims; use them to submit new claims via `POST /api/claims/create`.
