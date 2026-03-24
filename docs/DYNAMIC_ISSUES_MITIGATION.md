# Mitigating Dynamic Issues Before Run

This document describes how the pipeline catches **dynamic/runtime issues** (JPA mapping errors, Spring context failures, etc.) **before** you run the application.

---

## What Are Dynamic Issues?

Unlike compile errors (syntax, missing methods), these surface when Spring loads the ApplicationContext:

| Issue | Symptom | Cause |
|-------|---------|-------|
| **Duplicate column** | `Column 'RESERVE' is duplicated in mapping` | Multiple `@Column(name="RESERVE")` on same entity |
| **Not a managed type** | `Not a managed type: class XxxKey` | `JpaRepository<XxxKey, XxxKey>` instead of `JpaRepository<XxxEntity, XxxKey>` |
| **ApplicationContext failed** | `Failed to load ApplicationContext` | JPA/Hibernate or bean wiring error |

---

## Pipeline Mitigations

### 1. Build Gate (Step 4)

**Build Application** now fails if **any** of these fail:

- `mvn compile`
- `mvn package -DskipTests`
- `mvn test`

Previously, only compile had to pass. Now **tests must pass** for `buildSuccess` to be true. This catches ApplicationContext load failures (duplicate column, managed type, etc.) during `mvn test`.

**Effect:** Run & Demo is only viable after a successful build (including tests).

### 2. Generator Fixes (Prevention)

- **Duplicate columns:** `resolve_duplicate_column_names()` in `migrate_to_pure_java.py` rewrites schema columns (e.g. 4× RESERVE → RESERVE1, RESERVE2, RESERVE3, RESERVE4) before the LLM prompt.
- **Prompt instructions:** Migration prompt instructs the LLM to use exact column names from the schema, so generated entities use unique names.

### 3. Standalone Validation Script

```bash
# Full validation (compile + test)
python3 validate_before_run.py warranty_demo

# Quick validation (compile only)
python3 validate_before_run.py --quick
```

Use after migration or before Run & Demo to:

- Run `mvn test` (or `mvn compile` with `--quick`)
- Detect known error patterns and show hints
- Exit 1 on failure

---

## Recommended Flow

1. **Migrate Feature** (Step 2) → generates Java
2. **Build Application** (Step 4) → compile + package + test; fails if tests fail
3. If build fails → fix issues (or use LLM autofix for compile errors)
4. **Run & Demo** (Step 5) → only after successful build

---

## Extending Mitigations

### Add New Error Patterns

Edit `validate_before_run.py` and add to `KNOWN_ERROR_PATTERNS`:

```python
(r"your regex", "Human-readable hint"),
```

### LLM Autofix for Runtime Errors

The current `fix_compile_errors.py` handles **compile** errors. To auto-fix **runtime** errors (e.g. "Not a managed type"):

1. Parse `mvn test` output for known patterns
2. Feed the error + relevant source files to the LLM
3. Apply suggested fixes and re-run tests

This would extend the autofix loop in `_run_maven_build_with_autofix()`.
