# H2 Database Tables

**Database:** `jdbc:h2:file:./data/warranty_db`  
**H2 Version:** 2.2.224 (2023-09-17)

## Table Overview

| Table Name | Friendly Name / Purpose |
|------------|-------------------------|
| **HSAHKLF3** | Invoices – customer invoice header (order, date, customer, vehicle) |
| **HSAHKPF** | Invoice Key Header – alternate invoice key structure for V4 validation |
| **HSAHWPF** | Work Positions – work order line items linked to invoices |
| **HSEPAF** | Extended Part Agreement – EPA parameters for V4 validation |
| **HSFLALF1** | External Services – external service / Bes-Auftrag line items |
| **HSG70F** | Warranty Release – release requests and submission deadlines |
| **HSG71LF2** | Claims – claim list view (main warranty claim records) |
| **HSG73PF** | Claim Errors – failure lines per claim |
| **HSGPSPF** | Position Schedule – schedule/position data for claim creation |
| **INFORMATION_SCHEMA** | H2 system schema (metadata) |
| **Users** | H2 system table |

---

## HSAHKLF3 (Invoices) – Column Descriptions

| Column | Description |
|--------|-------------|
| **AHK000** | Company code (PKZ) |
| **AHK010** | Invoice number |
| **AHK015** | Invoice number (extended, 10 chars) |
| **AHK020** | Invoice date (YYYYMMDD) |
| **AHK030** | Storno/cancel indicator (S = cancelled) |
| **AHK040** | Order number |
| **AHK050** | Workshop type (e.g. A, 1) |
| **AHK060** | Area / branch (Bereich) |
| **AHK070** | Split type (04, V4, Q4, etc.) |
| **AHK080** | Order date (YYYYMMDD) |
| **AHK090** | Invoice description / text |
| **AHK100** | Reference number 1 |
| **AHK105** | Reference number 2 |
| **AHK106** | Date field 1 |
| **AHK107** | Reference number 3 |
| **AHK108** | Date field 2 |
| **AHK110** | Flag / indicator 1 |
| **AHK120** | Flag / indicator 2 |
| **AHK130** | Amount / rate 1 |
| **AHK140** | Amount / rate 2 |
| **AHK150** | Code 1 |
| **AHK160** | Number 1 |
| **AHK170** | Number 2 |
| **AHK180** | Amount 1 |
| **AHK190** | Amount 2 |
| **AHK200** | Amount 3 |
| **AHK205** | Amount 4 |
| **AHK210** | Chassis number (7 chars) |
| **AHK220** | Vehicle reference |
| **AHK221** | Customer/branch reference |
| **AHK222** | Company/branch code |
| **AHK223** | Customer number (internal) |
| **AHK224** | Customer number (alternate) |
| **AHK225** | Customer indicator |
| **AHK226** | Customer type |
| **AHK230** | Customer number (6 chars) |
| **AHK240** | Customer short name |
| **AHK250** | Customer name |
| **AHK260** | Customer address line 1 |
| **AHK270** | Customer address line 2 |
| **AHK280** | Customer address line 3 |
| **AHK290** | Customer country |
| **AHK300** | Customer code |
| **AHK310** | Customer reference |
| **AHK320** | Vehicle/order reference |
| **AHK325** | Vehicle reference 2 |
| **AHK330** | Indicator |
| **AHK340** | Code 2 |
| **AHK350** | Code 3 |
| **AHK360** | Currency / country |
| **AHK370** | Branch code |
| **AHK380** | Indicator 3 |
| **AHK390** | Description 1 |
| **AHK400** | Description 2 |
| **AHK410** | Reference |
| **AHK420** | Description 3 |
| **AHK430** | Company code |
| **AHK440** | Number 3 |
| **AHK450** | Reference 2 |
| **AHK460** | Amount 5 |
| **AHK470** | Indicator 4 |
| **AHK480** | Indicator 5 |
| **AHK490** | Indicator 6 |
| **AHK500** | Company / branch |
| **AHK505** | Invoice ID / USTID |
| **AHK510** | Chassis number (17 chars, for claim) |
| **AHK520** | Registration / repair date |
| **AHK530** | Number 4 |
| **AHK540** | Vehicle type code (M = motorcycle) |
| **AHK550** | Registration date (8 chars) |
| **AHK560** | Currency code |
| **AHK570** | Amount 6 |
| **AHK580** | Date 3 |
| **AHK590** | Amount 7 |
| **AHK595** | Repair date (alternate) |
| **AHK600** | Repair date (alternate 2) |
| **AHK610** | Amount 8 |
| **AHK620** | Date 4 (release request) |
| **AHK625** | Code 4 |
| **AHK630** | Date 5 |
| **AHK640** | Code 5 |
| **AHK650** | Dealer / workshop name |
| **AHK660** | Country code |
| **AHK670** | Branch code 2 |
| **AHK680** | Indicator 7 |
| **AHK690** | Amount 9 |
| **AHK691** | Reference 3 |
| **AHK699** | Amount 10 |
| **AHK700** | Amount 11 |
| **AHK710** | Net amount |
| **AHK720** | Tax amount |
| **AHK730** | Gross amount |
| **AHK740** | Amount 12 |
| **AHK750** | Amount 13 |
| **AHK760** | Amount 14 |
| **AHK770** | Indicator 8 |
| **AHK775** | Indicator 9 |
| **AHK780** | Indicator 10 |
| **AHK790** | Rate / percentage |
| **AHK800** | Amount 15 |
| **AHK810** | Date 6 |
| **AHK815** | Mileage (km) |
| **AHK820** | Rate 2 |
| **AHK830** | Rate 3 |
| **AHK840** | Count 1 |
| **AHK845** | Count 2 |
| **AHK850** | Amount 16 |
| **AHK855** | Reference 4 |
| **AHK860** | Code 6 |
| **AHK870** | Code 7 |
| **AHK880** | Indicator 11 |
| **AHK890** | Rate 4 |
| **AHK900** | Year |
| **AHK901** | ZAGA valid-to date |
| **AHK910** | Reference 5 |
| **AHK920** | KL extension amount |
| **AHK930** | Code 8 |
| **AHK940** | Description 4 |
| **AHK950** | Vehicle type (FZ-Art) |
| **AHK960** | Manufacturer |
| **AHK970** | Vehicle data 1 |
| **AHK980** | Vehicle data 2 |
| **AHK990** | Vehicle data 3 |
| **AHK1000** | Vehicle data 4 |
| **AHK1010** | Vehicle data 5 |
| **AHK1020** | Vehicle data 6 |
| **AHK1030** | Vehicle data 7 |
| **AHK1040** | Vehicle data 8 |
| **AHK1050** | Vehicle data 9 |
| **AHK1060** | Reference 6 |
| **AHK1070** | Indicator 12 |
| **AHK1080** | Number 5 |
| **AHK1100** | Free text 1 |
| **AHK1110** | Free text 2 |

*Descriptions inferred from RPG migration and warranty domain. Key fields for claim creation: AHK000, AHK010, AHK020, AHK040 (lookup); AHK510, AHK520, AHK550, AHK815 (claim data); AHK230, AHK250 (customer).*

---

## Troubleshooting: Invoices Empty, Cannot Create Claim

If **HSG71LF2** (claims) has records but **HSAHKLF3** (invoices) is empty, claim creation will fail with "Invoice not found".

**Fix 1 – Seed invoices only:**
```bash
curl -X POST http://0.0.0.0:8081/api/seed-invoices
```

**Fix 2 – Full reset (fresh schema):**
1. Stop the application
2. Delete the H2 database files: `rm -rf warranty_demo/data/warranty_db*`
3. Restart the application
4. Call `POST /api/seed` to seed both invoices and claims
