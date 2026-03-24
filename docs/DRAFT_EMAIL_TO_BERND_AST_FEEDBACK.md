# Draft Email to Bernd — AST Feedback

---

**To:** Bernd  
**Subject:** Feedback on Latest AST (JSON_20260311) — Pipeline Usage

---

Hi Bernd,

I hope this email finds you well. I am writing to share feedback on the latest AST delivery (JSON_20260311) that we are using in our RPG-to-Java migration pipeline.

---

## AST Version in Use

We are currently using the **JSON_20260311** AST delivery as the primary input for our migration pipeline. The column redundancy fix in this delivery has resolved the duplicate-column issues we previously encountered.

---

## What Works Well

The AST has proven to be a solid foundation for our migration:

- **Control flow and structure** — Nodes, call graph, procedure boundaries, and subroutines are well captured.
- **Data model** — The `dbContracts` and `nativeFiles` provide the schema we need to generate JPA entities (tables, columns, types).
- **Symbol references** — Variables, files, and data structures are well represented.
- **Traceability** — `statementNodes` and `lineToNodeMap` allow us to trace generated Java back to the original RPG lines.
- **Display files** — The DDS AST (`*D-ast.json`) for HS1210D and HS1212D provides the UI contracts we need for Angular UI generation.

---

## AST-Specific Note (Workaround in Place)

The only information we cannot derive from other sources (RPG, our own tooling) is **default / initial values from DDS** — we do not have direct access to DDS and rely on the AST for schema metadata. We work around this by hardcoding defaults where we know the semantics, so we are not blocked.

**Examples where we had to hardcode defaults:**

| AST file | Table (DDS member) | Column | Issue | Our workaround |
|----------|--------------------|--------|-------|----------------|
| HS1210-ast.json | HSG71LF2 (Claim) | ANHANG | NOT NULL, no default in AST | `claim.setAnhang(" ")` in ClaimCreationService |
| HS1210-ast.json | HSAHKLF3 (Invoice) | AHK070 (SPLITT) | Semantic default `"04"` for warranty | `SEED_SPLITT = "04"` in SeedService, TestDataFactory |
| HS1210-ast.json | HSAHKLF3 (Invoice) | AHK240 | NOT NULL, length 1; no default in AST | `inv.setAhk240(" ")` in SeedService |

**Where to enrich:** In the AST files (`HS1210-ast.json`, `HS1212-ast.json`, etc.), under:

```
dbContracts.nativeFiles[].columns[]
```

**Suggested format** (add to each column object where DDS provides them):

```json
{
  "name": "ANHANG",
  "heading": "Anhang",
  "typeId": "t.char.1",
  "nullable": false,
  "default": " ",
  "initialValue": " "
}
```

- **`default`** or **`initialValue`**: The DDS `INZ` value as a string or number (e.g. `" "`, `"04"`, `0`). Omit if DDS has no initial value.
- **`nullable`**: Already present in the 20260311 AST for many columns; ensure it reflects DDS/DB schema where available.

---

## Summary

| Aspect | Status |
|--------|--------|
| **Overall** | AST is effective; pipeline runs successfully |
| **Column redundancy** | Resolved in 20260311 delivery |
| **Blocking issues** | None |

Thank you for your continued support.

Best regards,  
[Your name]  
[Your organisation]
