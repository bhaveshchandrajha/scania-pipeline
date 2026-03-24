-- ============================================================
-- Mock Data for Warranty Claim Tables
-- Run after 01_schema_create.sql
-- ============================================================
-- Tables: HSG71PF (Claim Header), HSG73PF (Claim Details)
-- See SCHEMA_MAPPING.md for column mapping (G71000 ↔ PAKZ, etc.)
-- ============================================================

-- HSG71PF (Claim Header) - 1 demo claim
-- ============================================================
INSERT INTO CDBIB.HSG71PF (
  G71000, G71010, G71020, G71030, G71040, G71050, G71060, G71070,
  G71080, G71090, G71100, G71110, G71120, G71130, G71140, G71150,
  G71160, G71170, G71180, G71190, G71200
) VALUES (
  '001',           -- G71000 PAKZ (company code)
  '12345',         -- G71010 RECH.-NR. (invoice number)
  '20240115',      -- G71020 RECH.-DATUM
  '001',           -- G71030 AUFTRAGS-NR.
  'A',             -- G71040 WETE (workshop type)
  '00000001',      -- G71050 CLAIM-NR.
  '1234567',       -- G71060 CHASSIS-NR.
  'ABC123   ',     -- G71070 KENNZEICHEN
  20230101,        -- G71080 ZUL.-DATUM
  20240115,        -- G71090 REP.-DATUM
  45,              -- G71100 KM-STAND
  1,               -- G71110 PRODUKT-TYP
  ' ',             -- G71120 ANHANG
  ' ',             -- G71130 AUSL#NDER
  '100001',        -- G71140 KD-NR.
  'Demo Customer                    ',  -- G71150 KD-NAME
  '        ',      -- G71160 CLAIM-NR. SDE
  0,               -- G71170 STATUS CODE SDE
  1,               -- G71180 ANZ. FEHLER
  '1',             -- G71190 BEREICH
  '001A104  '      -- G71200 AUF.NR.
);

-- HSG73PF (Claim Details/Failure) - 1 failure for above claim
-- ============================================================
INSERT INTO CDBIB.HSG73PF (
  G73000, G73010, G73020, G73030, G73040, G73050, G73060, G73065,
  G73070, G73080, G73090, G73100, G73110, G73120, G73130, G73140,
  G73150, G73160, G73170, G73180, G73190, G73200, G73210, G73220,
  G73230, G73240, G73250, G73260, G73270, G73280, G73285, G73290,
  G73300, G73310, G73320, G73330, G73340, G73350, G73360, G73370,
  G73380, G73390, G73400, G73410, G73420, G73430, G73440, G73450,
  G73460, G73470, G73480, G73490, G73500
) VALUES (
  '001',           -- G73000 PAKZ
  '12345',         -- G73010 RECH.-NR.
  '20240115',      -- G73020 RECH.-DATUM
  '001',           -- G73030 AUFTRAGS-NR.
  '1',             -- G73040 BEREICH
  '00000001',      -- G73050 CLAIM-NR.
  '01',            -- G73060 FEHLER-NR.
  '01',            -- G73065 FOLGE-NR.
  '                  ',  -- G73070 FEHLER-TEIL
  '01',            -- G73080 HAUPTGRUPPE
  '01',            -- G73090 NEBENGRUPPE
  '  ',            -- G73100 SCHAD.-C1
  '  ',            -- G73110 SCHAD.-C2
  'Demo failure description                                                      ',  -- G73120 TEXT1
  '                                                                 ',  -- G73130 TEXT2
  '  ',            -- G73140 STEUER CODE
  '  ',            -- G73150 BEW. CODE1
  0,               -- G73160 BEW. CODE2
  0,               -- G73170 BEW. DATUM
  0, 0, 0,         -- G73180-G73200 VERG. MAT/ARB/SPEZ
  0, 0, 0,         -- G73210-G73230 BEANTR. MAT/ARB/SPEZ
  0, 0, 0, 0, 0,   -- G73240-G73270 CLAIM-ART, V.-REP.-DATUM, etc.
  '        ',      -- G73280 KAMPAGNEN-NR.
  '                    ',  -- G73285 EPS
  0, 0, 0,         -- G73290-G73310
  '                                                                 ',  -- G73320 TEXT3
  '                                                                 ',  -- G73330 TEXT4
  '  ',            -- G73340 FEHLER-NR. SDE
  ' ',             -- G73350 ANHANG
  '     ',         -- G73360 SOURCE
  '     ',         -- G73370 COMPLAIN
  '     ',         -- G73380 SYMPTOM
  '     ',         -- G73390 FAILURE
  '     ',         -- G73400 LOCATION
  '     ',         -- G73410 REPAIR
  '  ',            -- G73420 ERG.CODE
  '  ',            -- G73430 RESULT1
  '     ',         -- G73440 RESULT2
  '  ',            -- G73450 FAULT1
  '     ',         -- G73460 FAULT2
  '  ',            -- G73470 REPLY1
  '     ',         -- G73480 REPLY2
  '  ',            -- G73490 EXPLANATION1
  '     '           -- G73500 EXPLANATION2
);
