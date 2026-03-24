# Strategy: From RPG-Mapped Java to Pure Java

The current pipeline produces **workable, schema-faithful Java** that mirrors RPG structure and control flow. This document explains the gap and how to move toward **idiomatic, production-grade Java** that fits a modern application.

---

## 1. What “RPG-Mapped” vs “Pure Java” Means

| Aspect | RPG-mapped (current) | Pure / idiomatic Java |
|--------|----------------------|-------------------------|
| **Structure** | One or few large classes; methods often mirror RPG procedures 1:1 | Layered: entities, services, repositories; small, single-purpose classes |
| **Naming** | RPG-like (e.g. `EXFMT_HS1212`, `READC_REC`); column names preserved in @Column | Domain language: `displayConfirmationScreen()`, `findNextClaim()`; camelCase everywhere except inside `@Column(name="...")` |
| **Control flow** | Sequential, step-by-step like the RPG (read, update, chain, etc.) | Use cases and services; streams/optional where appropriate; clear separation of “what” vs “how” |
| **Data access** | Record-at-a-time style (read next, update, delete) | Repository/DAO abstraction; JPA or Spring Data; batch/query where it fits |
| **State** | Often module-level or static-like variables | Injected dependencies; immutable where possible; clear ownership |
| **Libraries** | Minimal; basic types and loops | Java 17+ features (records, sealed, Optional, Stream); Spring/Boot if that’s the target stack |

The pipeline today optimises for **correctness and traceability** (no schema hallucination, all columns mapped). It does **not** yet enforce a strict Java architecture or style. That’s the next step.

---

## 2. How to Build a Good Java Application From Here

### 2.1 Define a target architecture (then encode it in the prompt)

- **Choose a stack** (e.g. Spring Boot, plain Java + JPA, Quarkus) and a **package layout**:
  - `domain` – entities, value objects
  - `service` – use-case / application services
  - `repository` – data access (JPA repositories or DAOs)
  - `web` or `api` – controllers (if you add REST later)
- **Document it** in a short “Target Java architecture” section (in this repo or in your team docs). Include:
  - Naming: camelCase for types/methods/fields; `@Column(name="...")` only for DB names.
  - One service class per “program” or use case; entities per DB file; repositories per entity/aggregate.
  - No giant “god” class with 50 procedures; extract small methods and delegate to repositories.
- **Feed that into the migration prompt** (see 2.2). The model will then generate code that aims at this layout.

### 2.2 Evolve the prompt toward “pure Java” (short term)

In `migrate_with_claude.py` you can add a **Java style** section to the prompt, for example:

- **Do:**
  - Use camelCase for all Java identifiers (class, method, field); keep RPG column names only inside `@Column(name="...")`.
  - Prefer small methods; name them by intent (e.g. `loadClaimHeader()`, `validateCreditDate()`).
  - Use `List`, `Optional`, and clear conditionals instead of long if/else chains where it improves readability.
  - Prefer a service class that delegates to repositories; avoid one class that both does business logic and mimics “read next” loops.
- **Do not:**
  - Do not name Java methods after RPG op-codes (e.g. avoid `exfmtHs1212c4`; use `displayConfirmationScreen()` or similar).
  - Do not put all logic in one huge method; break into private methods or separate service calls.
  - Do not use static or global mutable state for business data; pass parameters or use injected dependencies.

You can also add a **Target structure** line, e.g.:

- “Produce a single service class and one @Entity per DB contract; use repository-style method names (findBy…, save) in comments or stubs where data access is implied.”

That keeps the current “one unit → one migration” scope but nudges the style toward idiomatic Java.

### 2.3 Two-phase migration (medium term)

- **Phase 1 (current):** Keep generating “RPG-mapped” Java that is correct and validated (all columns, no hallucination). This is your **source of truth** for behaviour and schema.
- **Phase 2 (refactor):** Treat Phase 1 output as input to a **refactoring step**:
  - **Option A – Manual:** Developers refactor into your target layers (services, repositories, domain names). Good for critical paths and learning.
  - **Option B – LLM-assisted:** A second prompt takes Phase 1 Java + (optionally) the same context package and says: “Refactor this into a Spring Boot service layer: extract a service class, repository interfaces, and domain entities; use camelCase and domain names; keep all @Column mappings unchanged.” Run it once per migrated unit and then review.

This way you keep **traceability and correctness** from Phase 1 and gain **architecture and style** from Phase 2.

### 2.4 Naming and domain language (ongoing)

- **Glossary:** Keep a small mapping of RPG concepts → Java/domain names (e.g. “claim header file” → `ClaimHeader` entity, “read next” → `findNext()`, “EXFMT” → “display and wait for input”). Put it in the prompt or in a shared doc the model can follow.
- **Consistency:** Once you choose names (e.g. `ClaimProcessingService`), reuse them across migrations so the final application has a consistent vocabulary.

### 2.5 Post-processing and checks (longer term)

- **Static checks:** After generation (or after refactor), run:
  - Naming: e.g. “all non-@Column identifiers in camelCase”.
  - Structure: “no class with more than N methods”, “every entity has a corresponding repository interface”.
- **Refactoring scripts:** Optional; e.g. a script that renames Java fields to camelCase while leaving `@Column(name="...")` unchanged. Use with care and review.

### 2.6 Incremental improvement

- Migrate and validate one node at a time (as now).
- For each node, decide: keep as “RPG-mapped” or run through the “refactor to pure Java” pass.
- Collect **before/after** examples and add them to your prompt or docs so the model learns your preferred style.
- Over time, tighten the prompt so that Phase 1 output is already closer to your target (fewer refactors needed).

---

## 3. Summary: What to do next

1. **Document** your target Java architecture (packages, naming, layers) in this repo or in team docs.
2. **Add a “Java style” section** to the migration prompt (camelCase, method naming, no RPG op-codes in names, small methods).
3. **Keep** the current pipeline as the “correctness” phase (schema and columns); add an optional **refactor** phase (manual or LLM) for “pure Java”.
4. **Maintain a short glossary** (RPG → domain names) and feed it into prompts.
5. **Use static checks** and incremental refactors so the codebase converges to a single, idiomatic style.

That way you continue to get **workable, validated Java** from the pipeline while moving toward a **good, maintainable Java application** with clear layers and domain language.
