# DB2 Schema Mapping: Client Schema vs Generated Entities

This document explains the relationship between the **client-provided DB2 schema** (physical column names like G71000, G73000) and our **generated JPA entities** (logical names like PAKZ, RECH.-NR.).

## Table Overview

| Client DB2 Table | Schema | Our Entity | Java Class | Notes |
|------------------|--------|------------|------------|-------|
| **HSG71PF** | CDBIB | Claim Header | `Claim` | ✅ Direct mapping |
| **HSG73PF** | CDBIB | Claim Details/Failure | `ClaimError` | ✅ Direct mapping |
| **HSAHKPF** | HDLZENTRAL | Order Header | — | ⚠️ No entity yet; we use **HSAHKLF3** (Invoice) |

## Why Column Names Differ

The client schema uses **physical/system column names** (G71000, G71010, G73000, etc.) typical of:
- DB2 system catalog exports
- CREATE TABLE DDL with generated names

Our entities use **logical/field names** from the IBM i DDS (Data Description Specifications):
- Names used in RPG programs (PAKZ, RECH.-NR., CLAIM-NR., etc.)
- Same physical table, different name representation

**On IBM i/DB2 for i**: The DDS defines both. The RPG compiler resolves logical names to physical storage.

---

## HSG71PF (Claim Header) – Column Mapping

| Client Column | Type | Our Entity Column | Java Field | Description |
|---------------|------|-------------------|------------|-------------|
| G71000 | CHAR(3) | PAKZ | pakz | Company code |
| G71010 | CHAR(5) | RECH.-NR. | rechNr | Invoice number |
| G71020 | CHAR(8) | RECH.-DATUM | rechDatum | Invoice date |
| G71030 | CHAR(5) | AUFTRAGS-NR. | auftragsNr | Order number |
| G71040 | CHAR(1) | WETE | wete | Workshop type |
| G71050 | CHAR(8) | CLAIM-NR. | claimNr | **Claim number** |
| G71060 | CHAR(7) | CHASSIS-NR. | chassisNr | Chassis number |
| G71070 | CHAR(10) | KENNZEICHEN | kennzeichen | License plate |
| G71080 | DECIMAL(8,0) | ZUL.-DATUM | zulDatum | Registration date |
| G71090 | DECIMAL(8,0) | REP.-DATUM | repDatum | Repair date |
| G71100 | DECIMAL(3,0) | KM-STAND | kmStand | Mileage |
| G71110 | DECIMAL(1,0) | PRODUKT-TYP | produktTyp | Product type |
| G71120 | CHAR(1) | ANHANG | anhang | Attachment flag |
| G71130 | CHAR(1) | AUSL#NDER | auslaender | Foreigner flag |
| G71140 | CHAR(6) | KD-NR. | kdNr | Customer number |
| G71150 | CHAR(30) | KD-NAME | kdName | Customer name |
| G71160 | CHAR(8) | CLAIM-NR. SDE | claimNrSde | SDE claim number |
| G71170 | DECIMAL(2,0) | STATUS CODE SDE | statusCodeSde | SDE status code |
| G71180 | DECIMAL(2,0) | ANZ. FEHLER | anzFehler | Failure count |
| G71190 | CHAR(1) | BEREICH | bereich | Area |
| G71200 | CHAR(10) | AUF.NR. | aufNr | Order reference |

**Primary Key**: (PAKZ, RECH.-NR.) → `ClaimId`

---

## HSG73PF (Claim Details/Failure) – Column Mapping

| Client Column | Type | Our Entity Column | Java Field | Description |
|---------------|------|-------------------|------------|-------------|
| G73000 | CHAR(3) | PAKZ | pakz | Company code |
| G73010 | CHAR(5) | RECH.-NR. | rechNr | Invoice number |
| G73020 | CHAR(8) | RECH.-DATUM | rechDatum | Invoice date |
| G73030 | CHAR(5) | AUFTRAGS-NR. | auftragsNr | Order number |
| G73040 | CHAR(1) | BEREICH | bereich | Area |
| G73050 | CHAR(8) | CLAIM-NR. | claimNr | Claim number |
| G73060 | CHAR(2) | FEHLER-NR. | fehlerNr | **Failure number** |
| G73065 | CHAR(2) | FOLGE-NR. | folgeNr | Sequence number |
| G73070 | CHAR(18) | FEHLER-TEIL | fehlerTeil | Failure part |
| G73080 | CHAR(2) | HAUPTGRUPPE | hauptgruppe | Main group |
| G73090 | CHAR(2) | NEBENGRUPPE | nebengruppe | Sub group |
| ... | ... | ... | ... | (see ClaimError.java for full list) |

**Primary Key**: (PAKZ, RECH.-NR., BEREICH, FEHLER-NR., FOLGE-NR.) → `ClaimErrorId`

---

## HSAHKPF (Order Table) – Not Yet Mapped

| Client Table | Schema | Status |
|--------------|--------|--------|
| **HSAHKPF** | HDLZENTRAL | Order/Invoice header with 100+ columns (AHK000–AHK1110) |

**Relationship to our codebase:**
- We use **HSAHKLF3** for Invoice (`Invoice` entity)
- **HSAHKPF** and **HSAHKLF3** are different tables; both appear in RPG migrations
- HSAHKPF may be the **physical order header**; HSAHKLF3 may be a **logical view** or related invoice table
- **Action**: Request DDS or field-level mapping from client to add HSAHKPF entity if needed

---

## Entity Relationship

```
HSAHKPF (Order)          HSG71PF (Claim Header)     HSG73PF (Claim Details)
     │                            │                            │
     │  (no entity yet)           │  Claim                      │  ClaimError
     │                            │  PK: pakz, rechNr           │  PK: pakz, rechNr, bereich, fehlerNr, folgeNr
     │                            │                            │
     └────────────────────────────┴────────────────────────────┘
                    Links via: pakz, rechNr, rechDatum, auftragsNr, bereich, claimNr
```

---

## Mock Data

### 1. Application seed (DataInitializer)

Mock data is seeded at startup for:
- **Claim** (HSG71PF) – company 001, claim 00000001
- **ClaimError** (HSG73PF) – one failure (01/01) for the demo claim
- **Invoice** (HSAHKLF3) – optional; may fail if required fields are missing

Run the app and access `http://localhost:8081/demo.html` to use the demo data.

### 2. SQL script for client DB2 schema

`warranty_demo/src/main/resources/data/mock_data_client_schema.sql` contains INSERT statements for the client’s physical column names (G71000, G73000, etc.). Use this to load sample data into DB2 tables that match the client’s CREATE TABLE definitions.

**Note:** If your DB2 tables use logical names (PAKZ, RECH.-NR., etc.) instead of physical names, adjust the script or use the application’s DataInitializer.
