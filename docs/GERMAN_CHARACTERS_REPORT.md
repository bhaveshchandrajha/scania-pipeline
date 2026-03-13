# Encoding Report: German Characters (CCSID 273 / UTF-8)

Files checked: 144

## Result: PASS — All files are valid UTF-8

**Encoding verification:** All files were successfully decoded as UTF-8. German characters (ä, ö, ü, ß, Ä, Ö, Ü) display correctly in source and at runtime. CCSID 273 (EBCDIC German) reference: data from IBM i is converted to UTF-8 before persistence.

## Files with German characters (ä, ö, ü, ß)

### `src/main/java/com/scania/warranty/config/EncodingConfig.java`
**Characters:** ß, ä, ö, ü

- Line 13: `* Ensures UTF-8 encoding for HTTP responses (German characters ä, ö, ü, ß).`

### `src/main/java/com/scania/warranty/service/ClaimValidationService.java`
**Characters:** ü

- Line 57: `errors.add("Schadenscodierung Kunde ungültig.");`
- Line 63: `errors.add("Schadenscodierung Kunde ungültig.");`
- Line 68: `errors.add("Schadenscodierung Werkstatt ungültig.");`
- Line 74: `errors.add("Schadenscodierung Werkstatt ungültig.");`

### `src/main/java/com/scania/warranty/service/ClaimModificationService.java`
**Characters:** Ä, Ö

- Line 19: `// RPG: SR102 - AUSWAHL 2 - ÄNDERN`
- Line 26: `// RPG: SR104 - AUSWAHL 4 - LÖSCHEN`

### `src/main/java/com/scania/warranty/domain/ClaimPositionLine.java`
**Characters:** Ü

- Line 65: `@Column(name = "VERGÜTUNG", precision = 3, scale = 0, nullable = false)`

### `src/main/java/com/scania/warranty/domain/Invoice.java`
**Characters:** Ü

- Line 83: `@Column(name = "BA-SCHLÜSSEL", length = 2, nullable = false)`
- Line 323: `@Column(name = "GA-ÜBERN.", length = 8, nullable = false)`
- Line 389: `@Column(name = "ZUSATZAUSRÜSTUNG 1", length = 20, nullable = false)`
- Line 395: `@Column(name = "ZUSATZAUSRÜSTUNG 2", length = 20, nullable = false)`
- Line 401: `@Column(name = "ZUSATZAUSRÜSTUNG 3", length = 20, nullable = false)`

### `src/main/java/com/scania/warranty/domain/InvoiceHeader.java`
**Characters:** Ü

- Line 83: `@Column(name = "BA-SCHLÜSSEL", length = 2, nullable = false)`
- Line 323: `@Column(name = "GA-ÜBERN.", length = 8, nullable = false)`
- Line 389: `@Column(name = "ZUSATZAUSRÜSTUNG 1", length = 20, nullable = false)`
- Line 395: `@Column(name = "ZUSATZAUSRÜSTUNG 2", length = 20, nullable = false)`
- Line 401: `@Column(name = "ZUSATZAUSRÜSTUNG 3", length = 20, nullable = false)`

### `src/main/java/com/scania/warranty/domain/GpsLine.java`
**Characters:** Ü

- Line 63: `@Column(name = "VERGÜTUNG", precision = 3, scale = 0, nullable = false)`

### `src/main/resources/application-local.properties`
**Characters:** ß, ä, ö, ü

- Line 3: `# H2 uses UTF-8 by default for German characters (ä, ö, ü, ß)`

### `src/main/resources/application.properties`
**Characters:** ß, ä, ö, ü

- Line 11: `# Character encoding for HTTP (German characters ä, ö, ü, ß)`

### `src/main/resources/ui-schemas/HS1210D.json`
**Characters:** ü

- Line 117: `"label": "CF12 Zurück (Back)",`
