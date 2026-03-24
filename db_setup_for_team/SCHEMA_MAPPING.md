# DB2 Schema Mapping: Physical vs Logical Column Names

This document maps the **client DB2 physical column names** (G71000, G73000) to **logical/application names** (PAKZ, RECH.-NR.).

## Table Overview

| Table | Schema | Description |
|-------|--------|-------------|
| **HSG71PF** | CDBIB | Claim Header |
| **HSG73PF** | CDBIB | Claim Details/Failure |
| **HSAHKPF** | HDLZENTRAL | Order Header |

## HSG71PF (Claim Header)

| Physical (Client) | Logical (App) | Type | Description |
|-------------------|---------------|------|-------------|
| G71000 | PAKZ | CHAR(3) | Company code |
| G71010 | RECH.-NR. | CHAR(5) | Invoice number |
| G71020 | RECH.-DATUM | CHAR(8) | Invoice date |
| G71030 | AUFTRAGS-NR. | CHAR(5) | Order number |
| G71040 | WETE | CHAR(1) | Workshop type |
| G71050 | CLAIM-NR. | CHAR(8) | **Claim number** |
| G71060 | CHASSIS-NR. | CHAR(7) | Chassis number |
| G71070 | KENNZEICHEN | CHAR(10) | License plate |
| G71080 | ZUL.-DATUM | DECIMAL(8,0) | Registration date |
| G71090 | REP.-DATUM | DECIMAL(8,0) | Repair date |
| G71100 | KM-STAND | DECIMAL(3,0) | Mileage |
| G71110 | PRODUKT-TYP | DECIMAL(1,0) | Product type |
| G71120 | ANHANG | CHAR(1) | Attachment flag |
| G71130 | AUSL#NDER | CHAR(1) | Foreigner flag |
| G71140 | KD-NR. | CHAR(6) | Customer number |
| G71150 | KD-NAME | CHAR(30) | Customer name |
| G71160 | CLAIM-NR. SDE | CHAR(8) | SDE claim number |
| G71170 | STATUS CODE SDE | DECIMAL(2,0) | SDE status code |
| G71180 | ANZ. FEHLER | DECIMAL(2,0) | Failure count |
| G71190 | BEREICH | CHAR(1) | Area |
| G71200 | AUF.NR. | CHAR(10) | Order reference |

**Primary Key**: (G71000, G71010) = (PAKZ, RECH.-NR.)

## HSG73PF (Claim Details)

| Physical (Client) | Logical (App) | Type | Description |
|-------------------|---------------|------|-------------|
| G73000 | PAKZ | CHAR(3) | Company code |
| G73010 | RECH.-NR. | CHAR(5) | Invoice number |
| G73020 | RECH.-DATUM | CHAR(8) | Invoice date |
| G73030 | AUFTRAGS-NR. | CHAR(5) | Order number |
| G73040 | BEREICH | CHAR(1) | Area |
| G73050 | CLAIM-NR. | CHAR(8) | Claim number |
| G73060 | FEHLER-NR. | CHAR(2) | **Failure number** |
| G73065 | FOLGE-NR. | CHAR(2) | Sequence number |
| G73070 | FEHLER-TEIL | CHAR(18) | Failure part |
| G73080 | HAUPTGRUPPE | CHAR(2) | Main group |
| G73090 | NEBENGRUPPE | CHAR(2) | Sub group |
| ... | ... | ... | (see full DDS for remaining columns) |

**Primary Key**: (G73000, G73010, G73040, G73060, G73065) = (PAKZ, RECH.-NR., BEREICH, FEHLER-NR., FOLGE-NR.)
