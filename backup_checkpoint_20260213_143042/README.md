<<<<<<< HEAD
# scania-demo-pipeline
=======
## PKS AST Migration Pipeline – RPG → Java (Quickstart)

- Build Java indexer: `mvn clean package` (requires JDK 17+).
- Index AST + RPG into `context_index/`:
  - `com.pks.migration.IndexAll --astDir JSON_ast/JSON --rpgDir PoC_HS1210 --outputDir .`
- Install Python deps: `python3 -m venv .venv && source .venv/bin/activate && pip install -r requirements.txt`.
- Set `ANTHROPIC_API_KEY` in your shell.
- Run UI server: `python3 ui_server.py`, then open `ui_index.html` in a browser.
- Use the UI to migrate individual nodes and see structural / semantic / behavioral validation.

**Compilation check:** Validation can run `javac` on the generated Java to ensure it compiles (no syntax errors). To enable it: (1) set `JAVA_HOME` or have `javac` on PATH, (2) from project root run `mvn dependency:copy-dependencies -DoutputDirectory=lib` so `lib/` contains JPA and other deps. Then validation will report "✓ Compiles (javac)" or show compiler errors. To skip: pass `--no-check-compile` to `validate_java.py`.

---

## Testing with fresh ASTs

When you have a **new AST export** (new or updated `*-ast.json` files), do the following to run the demo against them.

### 1. Place the new AST files

- Put your `*-ast.json` files in a directory the indexer can read, e.g.:
  - **Option A:** Replace or add files under `JSON_ast/JSON/` (e.g. `JSON_ast/JSON/HS1215-ast.json`).
  - **Option B:** Use a new directory, e.g. `JSON_ast/JSON_v2/`, and point the indexer at it in step 3.

### 2. RPG source that matches the ASTs

- Each AST node has a **range** (`sourceId`, `startLine`, `endLine`) pointing at RPG source. The indexer resolves `sourceId` under the **RPG directory** you pass as `--rpgDir`.
- Ensure the RPG source files that your ASTs reference are present under that directory (e.g. `PoC_HS1210/` or a new folder like `PoC_HS1215/`).  
  If the AST was produced from a different library or path, put the corresponding source tree under a folder and use it as `--rpgDir`.

### 3. Re-run the indexer (Java 17)

From the project root, with the JAR already built (`mvn clean package`):

```bash
# Use your AST directory and RPG directory; output goes to current dir (context_index/).
/opt/homebrew/opt/openjdk@17/bin/java \
  -cp target/pks-ast-migration-pipeline-0.1.0-SNAPSHOT.jar \
  com.pks.migration.IndexAll \
  --astDir JSON_ast/JSON \
  --rpgDir PoC_HS1210 \
  --outputDir .
```

- If you used a different AST dir: set `--astDir` to it (e.g. `--astDir JSON_ast/JSON_v2`).
- If your RPG source is elsewhere: set `--rpgDir` to that path (e.g. `--rpgDir PoC_HS1215`).

This **overwrites** `context_index/manifest.json` and all `context_index/<unitId>_<nodeId>.json` with the new ASTs’ nodes.

### 3b. Optional: Enrich context with display files (DSPF) for UI building

To add display file (DSPF) metadata to each context package so migrations can produce UI-aware Java (screen DTOs, form fields, EXFMT/READ comments):

```bash
python3 enrich_context_with_display_files.py --astDir JSON_ast/JSON --contextDir context_index
```

Optionally pass `--ddsDir <path-to-DDS-source>` to include DDS source in the context (screen layout). Then run migrations as usual; the prompt will include a "Display files (DSPF)" section when present.

### 4. Use the UI (no server restart needed)

- If the UI server is already running, **refresh the browser** so it fetches the new `manifest` and node list.
- If not: `python3 ui_server.py` then open `ui_index.html`.
- Migrate and validate nodes as before; they now correspond to the fresh ASTs.

### Summary

| Step | Action |
|------|--------|
| 1 | Put `*-ast.json` in `--astDir` (e.g. `JSON_ast/JSON` or `JSON_ast/JSON_v2`). |
| 2 | Ensure RPG source for those ASTs is under `--rpgDir`. |
| 3 | Run `IndexAll` with your `--astDir` and `--rpgDir`; `--outputDir .` updates `context_index/`. |
| 3b | (Optional) Run `enrich_context_with_display_files.py` to add DSPF info for UI-aware code gen. |
| 4 | Refresh the UI (or start the server) and run migrations as usual. |

# PKS AST Migration Pipeline

This project provides a **Java 17**-based migration pipeline that converts PKS Systems AST JSON files into:

- **Intermediate English Representation (Semantic Narrative)** – a human-readable but structurally aligned description of the legacy RPG logic.
- **Knowledge Base (KB)** – a searchable index that ties together the narrative, RPG source snippets, and AST metadata.
- **Context Packages** – self-contained bundles of information suitable for **hallucination-safe RPG-to-Java code generation**.

## High-Level Architecture

- **AST Parser**
  - Reads PKS AST JSON.
  - Extracts symbols (`sym.var`, `sym.file`, etc.), `dbContracts`, and `nodes`.
  - Preserves links to **RPG source ranges** where available.

- **Semantic Transformer**
  - Converts node `sem` (semantics) plus edges and symbol metadata into English sentences.
  - Produces a **Lossless Narrative** for each subroutine / procedure.

- **Knowledge Base**
  - Stores an index where:
    - The **Index** is the Semantic Narrative.
    - The **Payload** contains RPG snippet(s) and AST metadata.
  - Implemented as a structured JSON index, but designed such that a vector DB can be plugged in later.

- **Context Assembler (Prompt Orchestrator)**
  - Given a **Subroutine ID**, it returns a **Context Package** composed of:
    - Narrative (business intent).
    - RPG code snippet(s) (legacy implementation).
    - AST metadata (types, `dbContracts`, field precisions).
  - Enforces an **anti-hallucination rule** by keeping AST `dbContracts` as the single source of truth for database structure.

## Usage Overview

1. **Build the project**

```bash
mvn clean package
```

2. **Run the sample CLI**

Once implemented, the `Main` class will accept:

- Path to a PKS AST JSON file.
- A Subroutine / Procedure node identifier.

It will print a **Context Package** as JSON to stdout.

Example (to be adapted to your environment):

```bash
java -jar target/pks-ast-migration-pipeline-0.1.0-SNAPSHOT.jar \
  --ast /path/to/ast.json \
  --subroutine n1058
```

## Customising for Your AST Schema

The AST schema for PKS Systems may vary between installations. This project:

- Uses **Jackson** and `JsonNode` for resilient JSON navigation.
- Provides **typed wrappers** around common concepts:
  - Nodes (with `id`, `kind`, `sem`, `range`, etc.).
  - Symbols (`sym.var`, `sym.file`).
  - Database contracts (`dbContracts`).
- Concentrates all schema-dependent paths in the `PksAstParser` and related helper classes.

You should:

- Point the parser to a **real PKS AST sample**.
- Adjust field names or paths in `PksAstParser` to match your exact schema.
- Extend the **Semantic Transformer** with richer NL templates as needed.

## Next Steps

- Integrate the **Context Assembler** with your LLM orchestration layer.
- Implement additional checks that cross-verify generated Java JPA entities against `dbContracts` before accepting them.
- Optionally back the Knowledge Base with a **vector database** for semantic search over the narrative.

>>>>>>> f51f496 (Initial demo pipeline snapshot)
