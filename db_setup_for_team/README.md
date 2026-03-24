# DB Setup – Mock Data for Warranty Claim Schema

Use this package to set up the database and load mock data for the warranty claim tables.

## Contents

| File | Description |
|------|-------------|
| `01_schema_create.sql` | CREATE TABLE statements (HSAHKPF, HSG71PF, HSG73PF) |
| `02_mock_data.sql` | INSERT statements for sample data |
| `SCHEMA_MAPPING.md` | Column mapping: physical names (G71000) ↔ logical names (PAKZ) |

## Quick Start

1. Run the schema script to create tables:
   ```bash
   db2 -tf 01_schema_create.sql
   ```

2. Load mock data:
   ```bash
   db2 -tf 02_mock_data.sql
   ```

**Note:** Adjust schema qualifiers (CDBIB, HDLZENTRAL) if your environment uses different schemas.

## Mock Data Summary

| Table | Records | Description |
|-------|---------|-------------|
| **HSG71PF** (Claim Header) | 1 | Claim 00000001, company 001, invoice 12345 |
| **HSG73PF** (Claim Details) | 1 | Failure 01/01 for the above claim |
| **HSAHKPF** (Order) | 0 | Schema only; add mock rows if needed |

## Relationships

```
HSAHKPF (Order)  ──►  HSG71PF (Claim Header)  ──►  HSG73PF (Claim Details)
   Order                  Claim 00000001              Failure 01/01
   (AHK000-AHK1110)      (G71000-G71200)             (G73000-G73500)
```

## Support

See `SCHEMA_MAPPING.md` for column mapping between client physical names and application entity fields.
