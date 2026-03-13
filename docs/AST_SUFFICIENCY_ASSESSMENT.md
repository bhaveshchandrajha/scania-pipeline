# PKS AST Sufficiency Assessment for Enterprise-Grade Java Migration

**Purpose:** Assess whether the current PKS Systems AST provides sufficient information for enterprise-grade RPG→Java migration, and identify gaps or enhancements.

---

## 1. What the AST Currently Provides (and How It's Used)

| AST / Context Element | Source | Used For |
|-----------------------|--------|----------|
| **Nodes** (id, kind, name, sem, outgoingEdges, range) | `*-ast.json` | Node slicing, call graph, narrative building |
| **dbContracts / nativeFiles** | AST `dbContracts.nativeFiles` | DB schema: tables, columns, types → JPA entities |
| **Symbol table** (sym.var.*, sym.file.*, sym.ds.*) | AST | Narrative, type resolution, variable→field mapping |
| **Types** (t.char.N, t.decP_S) | AST | Column types → `@Column(precision, scale)` |
| **statementNodes** | AST | Traceability (`@origin`), logic completeness checks |
| **lineToNodeMap** | AST | RPG line → AST node mapping |
| **Call graph** (CALLS edges) | Derived from AST | Feature slicing, dependency order |
| **DDS AST** (`*D-ast.json`) | Display file AST | UI contracts, record formats, form fields |
| **RPG source** | **RPG directory** (not AST) | Full logic; AST provides `range` only |

---

## 2. Gaps Encountered in Migration (Workarounds Applied)

| Gap | Symptom | Current Workaround |
|-----|---------|-------------------|
| **Duplicate column names** | Schema has 4× `RESERVE`; JPA requires unique `@Column(name)` | `resolve_duplicate_column_names()` in `migrate_to_pure_java.py` rewrites to RESERVE1, RESERVE2, etc. |
| **Entity vs composite key** | Repository generated as `JpaRepository<HsahkpfKey, String>` instead of `JpaRepository<Hsahkpf, HsahkpfKey>` | Manual entity creation; AST does not distinguish key-only vs full entity |
| **Ambiguous API mapping** | Two controllers map same path | Manual path differentiation (e.g. `/api/claims/subfile/create`) |
| **RPG snippet truncation** | Large nodes (e.g. n404) have truncated rpgSnippet | `--rpg-file` loads full source from RPG directory |
| **Default / semantic values** | ANHANG, splitt=04 for warranty, etc. | Inferred from RPG logic or hardcoded in service |

---

## 3. Assessment: Sufficient for Enterprise?

**Verdict: Largely sufficient, with caveats.**

The AST provides the core structural and semantic information needed for migration:

- ✅ **Control flow** – Nodes, call graph, procedure boundaries  
- ✅ **Data model** – DB files, columns, types  
- ✅ **Symbol references** – Variables, files, data structures  
- ✅ **Traceability** – statementNodes, lineToNodeMap  
- ✅ **Display files** – DDS AST for UI-aware migration  

The pipeline has proven it can produce working Java from the current AST. The gaps above are **addressable in the pipeline** (as done) or require **minimal manual fixes** post-migration.

---

## 4. Recommended AST Enhancements (If PKS Can Extend)

If PKS can extend the AST, these would reduce manual fixes and improve enterprise readiness:

### High Value

| Enhancement | Benefit |
|-------------|---------|
| **Physical column names for duplicates** | When multiple columns share a logical name (e.g. RESERVE), emit unique physical names (RESERVE1, RESERVE2) or ordinal position. Eliminates need for `resolve_duplicate_column_names()`. |
| **Key vs entity distinction** | Mark files/tables that are used only as composite keys (e.g. `@IdClass`) vs full entities. Would prevent "Not a managed type" repository errors. |
| **Procedure/API contract** | For each procedure: inputs, outputs, side effects, and suggested REST path. Would reduce ambiguous controller mappings. |

### Medium Value

| Enhancement | Benefit |
|-------------|---------|
| **Nullable / default hints** | Column-level `nullable`, `default` where inferable. Reduces DataIntegrityViolation and missing-field issues. |
| **Embedded SQL extraction** | Pre-parsed EXEC SQL / DECLARE CURSOR in AST. Currently extracted from RPG source at runtime. |
| **Data structure (DS) subfield layout** | Full qualified subfield paths for nested DS. Improves copy/map accuracy. |

### Lower Priority

| Enhancement | Benefit |
|-------------|---------|
| **Indicator semantics** | Optional description of what each indicator means in context. |
| **External program call signatures** | For CALL/CALLP, parameter types and return values. |

---

## 5. What Can Be Done Without AST Changes

The pipeline can continue to improve without AST changes:

1. **Generator prompts** – Add rules for JpaRepository&lt;Entity, Id&gt;, duplicate column handling, API path conventions.  
2. **Post-migration validation** – Detect "Not a managed type", duplicate columns, ambiguous mappings; optionally trigger LLM autofix.  
3. **Domain glossary** – Expand mappings (file→entity, procedure→service) to reduce wrong types.  
4. **RPG source integration** – Always use `--rpg-file` for large nodes to avoid truncation.

---

## 6. Summary

| Question | Answer |
|----------|--------|
| **Is the AST sufficient for enterprise migration today?** | Yes, with pipeline workarounds and some manual fixes. |
| **Are AST enhancements necessary?** | No, but they would reduce manual effort and improve consistency. |
| **Highest-impact enhancement** | Physical column names for duplicate logical names + key/entity distinction. |
