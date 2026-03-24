## Global Context & Agentic Migration (Prototype)

This folder contains **non-invasive tooling** for a new, persistent
global context model that sits *beside* the existing pipeline
(`migrate_to_pure_java.py`, `ui_server.py`, `warranty_demo`).

Initial goals:

- **DB contract registry**: one canonical view of all DB files
  (`dbContracts.nativeFiles`) across ASTs such as `HS1210-ast.json`,
  `HS1212-ast.json`, so all migrations share the same schema.
- **Program-level context**: per-program view of symbols, shared
  variables, and nodes (which procedures/subroutines exist, which
  symbols they reference).
- **Call graph** (next step): which nodes call which, to support
  feature-based migration.

The first script, `build_db_registry.py`, reads all `*-ast.json`
under `JSON_ast/**/` and writes a merged registry to
`global_context/db_registry.json`. This registry can later feed both:

- the **LLM prompts** (so it reuses entities instead of inventing new
  ones per slice), and
- a future **Knowledge Graph** (Neo4j or similar) as the canonical
  source of truth for DB schema.

The second script, `build_program_context.py`, produces one
`*.program.json` per AST file under `global_context/programs/`
(e.g. `HS1210.program.json`). Each file summarises:

- program metadata (unit id, library, member),
- all AST nodes with basic info (id, kind, name, procedureName),
- all symbols from `symbolTable` plus which nodes reference them and
  whether they are **shared** (referenced by more than one node).

Together, these two artifacts form the first layer of a persistent,
structured global model that can later be imported into a graph
database or queried directly by migration scripts/agents.


