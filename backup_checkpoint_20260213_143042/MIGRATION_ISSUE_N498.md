# Migration Issue: HS1212 n498

## Problem Summary

**Node:** `HS1212 n498` (Subroutine)  
**Status:** ❌ Migration failed  
**Issues:**
1. ✗ Compilation failed: 2 syntax errors
2. ✗ Only 10/11 dbContracts mapped (1 missing)
3. ✗ Only 204/313 columns mapped (109 missing)

## Root Cause Analysis

### Context Size Comparison

| Node | Context Size | DB Contracts | Total Columns | Status |
|------|--------------|--------------|---------------|--------|
| **n1** (CompilationUnit) | ~100 KB | 2 | ~52 | ✅ Success (100%) |
| **n498** (Subroutine) | **245.7 KB** | **11** | **313** | ❌ Failed (65% mapped) |

### Issue Identified

**Output Truncation:** The LLM response hit the `max_tokens` limit (32768 tokens), causing:
- Incomplete entity generation (only 10/11 entities)
- Missing columns (204/313 = 65% mapped)
- Syntax errors from truncated code (missing closing braces)

**Estimated Output Size:**
- 313 columns × ~50 tokens/column (entity field + getter/setter) ≈ **15,650 tokens**
- Plus service logic, repositories, imports ≈ **25,000-30,000 tokens**
- **Total estimated: ~30,000-35,000 tokens** (near/exceeds 32K limit)

## Solutions

### Option 1: Increase max_tokens (Recommended)

Try migrating with a higher token limit:

```bash
python3 migrate_with_claude.py context_index/HS1212_n498.json --max-tokens 65536 --stream
```

**Note:** Check if your Claude model (claude-sonnet-4-5) supports 65536 tokens. If not, try 49152.

### Option 2: Split Migration (If Option 1 Fails)

For very large contexts, consider splitting:

1. **Generate entities separately:**
   - Create a prompt that only generates JPA entities (no service logic)
   - This reduces output size significantly

2. **Generate service logic separately:**
   - Use the generated entities as input
   - Generate only the service class

### Option 3: Optimize Context Package

Reduce context size by:
- Removing unused symbol metadata
- Truncating very long RPG snippets
- Only including essential dbContracts

## Immediate Fix Applied

Updated `migrate_with_claude.py` to:
1. ✅ **Detect large contexts** (>250 columns) and warn before migration
2. ✅ **Better truncation warnings** with token estimates
3. ✅ **Prompt enhancement** to prioritize completeness for large contexts

## Next Steps

1. **Try Option 1 first:**
   ```bash
   python3 migrate_with_claude.py context_index/HS1212_n498.json --max-tokens 65536 --stream
   ```

2. **If still failing:**
   - Check Claude model limits for max_tokens
   - Consider Option 2 (split migration)
   - Or Option 3 (optimize context)

3. **Monitor output:**
   - Watch for truncation warnings in stderr
   - Check validation results for column mapping percentage

## Validation After Fix

After re-migrating, verify:
- ✅ All 11 dbContracts mapped
- ✅ All 313 columns mapped (100%)
- ✅ No syntax errors
- ✅ Code compiles successfully
