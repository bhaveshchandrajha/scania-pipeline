# Encoding: CCSID 273 (EBCDIC German) and UTF-8

## Reference: CCSID 273

**CCSID 273** = EBCDIC Code Page 273 (German). Used on IBM i (AS/400) for German text (ä, ö, ü, ß).

## Java / Spring Boot Stack

| Layer | Encoding | Notes |
|-------|----------|-------|
| **Java source files** | UTF-8 | `pom.xml`: `project.build.sourceEncoding=UTF-8` |
| **HTTP request/response** | UTF-8 | `application.properties`: `server.servlet.encoding.charset=UTF-8` |
| **JSON (Jackson)** | UTF-8 | `EncodingConfig`: `MappingJackson2HttpMessageConverter` → UTF-8 |
| **String responses** | UTF-8 | `EncodingConfig`: `StringHttpMessageConverter` → UTF-8 |
| **HTML/static** | UTF-8 | `meta charset="UTF-8"` in demo.html, hs1210d.html |

## Data Flow: IBM i → Java

```
IBM i (EBCDIC CCSID 273)  →  JDBC/DB2 driver  →  Java (Unicode/UTF-16)
                                                      ↓
                                              Spring Boot (UTF-8 HTTP/JSON)
                                                      ↓
                                              H2 / PostgreSQL (UTF-8)
```

- **IBM i / DB2**: Data stored in CCSID 273. The JDBC driver converts to Java `String` (Unicode) when reading.
- **H2 (local)**: Stores Unicode; no EBCDIC.
- **PostgreSQL (RDS)**: Uses UTF-8; no EBCDIC.

## Verification

Run the encoding check:

```bash
python check_encoding.py
```

Generate a report listing all files with German characters:

```bash
python check_encoding.py --report docs/GERMAN_CHARACTERS_REPORT.md
```

See **[GERMAN_CHARACTERS_REPORT.md](GERMAN_CHARACTERS_REPORT.md)** for the full list of files with German characters and sample lines.

This verifies:
1. All `.java`, `.properties`, `.html`, `.json`, `.xml` under `warranty_demo/src` are valid UTF-8.
2. German characters (ä, ö, ü, ß) are reported if present.

## Pipeline Writes

- `ui_global_context_server.py`: `target.write_text(content, encoding="utf-8")`
- `fix_compile_errors.py`, `fix_idclass.py`: `write_text(..., encoding="utf-8")`
- `migrate_to_pure_java.py`: Output is written via the server with UTF-8

## If Connecting to IBM i DB2

When the Java app connects to DB2 on IBM i:

1. Use a JDBC driver that supports CCSID conversion (e.g. IBM JTOpen / JT400).
2. Set connection properties for the correct CCSID if needed.
3. The driver converts EBCDIC 273 → Unicode; Spring Boot uses UTF-8 for HTTP/JSON.
