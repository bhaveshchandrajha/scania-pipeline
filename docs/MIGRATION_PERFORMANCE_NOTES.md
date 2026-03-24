# Migration Performance Investigation

## Summary

**Previous migrations do NOT contribute to new migrations.** Each migration reads only the single context file for the node being migrated. The slowdown is due to:

1. **Large context size** – n404 has ~23k lines of context (87KB rpgSnippet, 1,338 statementNodes, 1,297 lineToNodeMap entries)
2. **LLM processing time** – Larger prompts take longer; n404’s prompt is ~100–200KB
3. **Post-migration: inject_origin_annotations** – Scans all Java files in `warranty_demo`; more migrations → more files → longer runs

## Context Size (HS1210_n404)

| Field           | Size              |
|----------------|-------------------|
| rpgSnippet     | 87,697 chars      |
| statementNodes | 1,338 items       |
| lineToNodeMap  | 1,297 keys        |
| narrative      | 13,181 chars      |
| displayFiles   | 11 items          |

## What Does NOT Affect Migration Time

- **Previous migrations** – Not loaded; each run uses only the current context file
- **Polling interval** – Server progress checks do not add to LLM time
- **Compilation** – Handled in a separate phase (Build Application tab)

## Optimizations Applied

1. **`--no-inline-origin`** – Skips `inject_origin_annotations` (saves 1–3+ min as the project grows)
2. **Poll interval** – Reduced from 45s to 15s for better progress feedback
3. **System prompt separation** – Static instructions (role, glossary, requirements) moved to `system`; dynamic content in `user`. To enable prompt caching (2nd+ run faster within 5 min), upgrade `anthropic` SDK to 0.83+ and add `cache_control={"type": "ephemeral"}` to the API calls.

## Further Optimizations (Optional)

- **Prompt trimming** – For very large nodes, consider summarizing `statementNodes` or `lineToNodeMap`
- **Smaller nodes first** – Migrate smaller subroutines (e.g. n2020) before large ones (n404)
- **`--no-traceability`** – Slightly reduces prompt size and skips traceability comments
