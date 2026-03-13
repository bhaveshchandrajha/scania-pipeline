# Migration Pipeline – Full UAT Deployment Plan

## 1. Scope

Deploy the **entire RPG-to-Java migration pipeline** so UAT users can:

1. **Build global context** from AST JSON and RPG source
2. **Migrate features** (nodes) to Java using LLM-assisted code generation
3. **Validate** generated code (structure, DB mapping, traceability)
4. **Build** the application (Maven + LLM autofix for compile errors)
5. **Run** the generated Spring Boot app with Angular UI

This is **not** just the Spring Boot app; it is the full pipeline UI and toolchain.

---

## 2. Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                    MIGRATION PIPELINE CONTAINER (UAT)                                     │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                           │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐    │
│  │  Pipeline UI Server (Python)  :8003                                              │    │
│  │  ui_global_context_server.py  →  ui_global_context.html (5 Tabs)                 │    │
│  └─────────────────────────────────────────────────────────────────────────────────┘    │
│                                         │                                                 │
│  ┌─────────────────────────────────────┼─────────────────────────────────────────────┐  │
│  │  Tab 1: Build Global Context         │  Tab 2: Migrate Feature                     │  │
│  │  • build_db_registry.py              │  • migrate_to_pure_java.py (LLM)             │  │
│  │  • build_program_context.py          │  • ui_schema_generator.py                   │  │
│  │  • build_call_graph*.py              │  • inject_origin_annotations.py             │  │
│  │  • build_context_index.py            │  • fix_idclass, fix_ambiguous_mapping      │  │
│  │  • build_node_index.py               │  Requires: ANTHROPIC_API_KEY                │  │
│  │  • export_neo4j_cypher.py            │                                             │  │
│  │  Input: AST dir, RPG dir (mounted)   │  Input: Program ID, Entry Node ID           │  │
│  └─────────────────────────────────────┴─────────────────────────────────────────────┘  │
│                                         │                                                 │
│  ┌─────────────────────────────────────┼─────────────────────────────────────────────┐  │
│  │  Tab 3: Validation                   │  Tab 4: Build Application                  │  │
│  │  • validate_pure_java.py             │  • Maven (mvn compile/package)             │  │
│  │  • Code Quality Score Card           │  • fix_compile_errors.py (LLM)             │  │
│  │  • Traceability viewer               │  • fix_logic_gaps.py (LLM)                 │  │
│  └─────────────────────────────────────┴─────────────────────────────────────────────┘  │
│                                         │                                                 │
│  ┌─────────────────────────────────────┴─────────────────────────────────────────────┐  │
│  │  Tab 5: Run & Demo                                                                │  │
│  │  • mvn spring-boot:run  (warranty_demo)  :8081                                    │  │
│  │  • Angular UI, demo.html, hs1210d.html, Swagger, H2 Console                       │  │
│  └─────────────────────────────────────────────────────────────────────────────────┘  │
│                                                                                           │
│  Mounted Volumes:                                                                          │
│  • /workspace/ast     → User's AST JSON files (*-ast.json, *D-ast.json)                   │
│  • /workspace/rpg     → User's RPG source (.sqlrpgle, .rpgle)                            │
│  • /workspace/output  → Generated context_index, global_context, warranty_demo            │
│                                                                                           │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Runtime Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| Python | 3.10+ | Pipeline scripts, UI server |
| Node.js | 20 LTS | Angular build (warranty-ui) |
| Maven | 3.9+ | Java build |
| Java (JDK) | 17 | Spring Boot, compilation |
| Anthropic SDK | latest | LLM (Claude) for migration & autofix |

---

## 4. Container Design Options

### Option A: Single All-in-One Container

One container with Python, Node, Maven, Java. User runs pipeline UI; all steps execute inside the same container.

**Pros:** Simple, single `docker run`  
**Cons:** Large image (~2–3 GB), all tools always present

### Option B: Multi-Container (Pipeline UI + Build Agent)

- **Container 1:** Pipeline UI (Python server) – lightweight
- **Container 2:** Build agent (Node, Maven, Java) – invoked when Tab 4/5 runs

**Pros:** Smaller UI image, build tools isolated  
**Cons:** More orchestration, shared volumes

### Option C: Docker Compose with Services

- **pipeline-ui:** Python server :8003
- **builder:** Optional sidecar for heavy builds (or run Maven/Node on host via volume-mounted scripts)

**Pros:** Clear separation, scalable  
**Cons:** More moving parts

---

## 5. Recommended Approach: Single Container

For UAT “AS IS” deployment, a **single container** is recommended:

1. One `docker run` or `docker-compose up`
2. All tools (Python, Node, Maven, Java) in one image
3. User mounts AST/RPG dirs and an output dir
4. User provides `ANTHROPIC_API_KEY` via env
5. Access Pipeline UI at http://localhost:8003
6. After migration + build, Run & Demo starts Spring Boot on :8081

---

## 6. Build Stages (Dockerfile)

| Stage | Base Image | Purpose |
|-------|------------|---------|
| 1 | python:3.11-slim | Install Python deps, copy pipeline scripts |
| 2 | node:20-alpine | Build Angular (warranty-ui) → static/angular |
| 3 | maven:3.9-eclipse-temurin-17 | Build warranty_demo JAR (optional pre-build) |
| 4 | python:3.11-slim + openjdk:17 + maven + node | Final runtime: all tools in one image |

---

## 7. Directory Layout (Inside Container)

```
/opt/pipeline/
├── ui_global_context_server.py
├── ui_global_context.html
├── migrate_to_pure_java.py
├── fix_compile_errors.py
├── feature_build_orchestrator.py
├── build_*.py, fix_*.py, validate_*.py, ...
├── global_context/
├── context_index/
├── warranty_demo/          # Generated + built app
├── warranty-ui/            # Angular source (for rebuild)
└── requirements.txt
```

---

## 8. Volume Mounts

| Host Path | Container Path | Purpose |
|-----------|----------------|---------|
| `./ast` or user path | `/workspace/ast` | AST JSON files |
| `./rpg` or user path | `/workspace/rpg` | RPG source |
| `./output` | `/workspace/output` | global_context, context_index, warranty_demo (persisted) |

---

## 9. Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `ANTHROPIC_API_KEY` | Yes (for Tab 2, 4) | Claude API key for migration and autofix |
| `UI_PORT` | No | Pipeline UI port (default 8003) |
| `APP_PORT` | No | Spring Boot port (default 8081) |

---

## 10. Python Dependencies (requirements.txt)

```
anthropic>=0.18.0
```

(Other scripts use stdlib only; verify with `pip freeze` after local run.)

---

## 11. User Workflow (UAT)

1. **Prepare inputs**
   - Place AST JSON in `./ast` (or chosen path)
   - Place RPG source in `./rpg` (or chosen path)

2. **Start container**
   ```bash
   docker run -d -p 8003:8003 -p 8081:8081 \
     -v $(pwd)/ast:/workspace/ast:ro \
     -v $(pwd)/rpg:/workspace/rpg:ro \
     -v $(pwd)/output:/workspace/output \
     -e ANTHROPIC_API_KEY=sk-ant-... \
     migration-pipeline-uat:1.0.0
   ```

3. **Use Pipeline UI** (http://localhost:8003)
   - Tab 1: Set AST dir `/workspace/ast`, RPG dir `/workspace/rpg` → Build Global Context
   - Tab 2: Select Program + Feature → Migrate Feature
   - Tab 3: Run Validation
   - Tab 4: Build Application
   - Tab 5: Run & Demo → Spring Boot at http://localhost:8081

4. **Inspect output**
   - `./output/global_context/`
   - `./output/context_index/`
   - `./output/warranty_demo/`

---

## 12. Deliverables Checklist

| Item | Description |
|------|-------------|
| `Dockerfile` | Multi-stage build: Python + Node + Maven + Java |
| `requirements.txt` | Python deps (anthropic, etc.) |
| `docker-compose.yml` | Ports 8003, 8081; volumes for ast, rpg, output |
| `.dockerignore` | Exclude .git, __pycache__, target, node_modules, etc. |
| `docs/MIGRATION_PIPELINE_UAT_DEPLOYMENT_PLAN.md` | This plan |
| `scripts/entrypoint.sh` | Start UI server, ensure dirs exist |

---

## 13. Pre-Deployment Checklist

- [ ] `requirements.txt` created and tested
- [ ] All Python scripts runnable from container working dir
- [ ] Pipeline UI discovers `/workspace/ast` and `/workspace/rpg`
- [ ] Output written to `/workspace/output` (or configurable)
- [ ] ANTHROPIC_API_KEY passed via env (not in image)
- [ ] Spring Boot starts with H2 (no RDS required for UAT)
- [ ] Angular built and copied to warranty_demo before image build (or built on first Run)

---

## 14. Limitations & Notes

- **LLM dependency:** Tab 2 (Migrate) and Tab 4 (Autofix) require Anthropic API key and network access.
- **Offline:** Tab 1 (Build Context) and Tab 3 (Validation) can run offline if context is pre-built.
- **RDS:** Tab 5 Run & Demo can use H2 for UAT; RDS is optional.
- **Sample data:** Include sample AST/RPG in image or docs for quick validation.

---

## 15. Next Steps

1. Create `requirements.txt` from actual imports.
2. Implement `Dockerfile` per Section 6.
3. Add `docker-compose.yml` with volumes and env.
4. Test end-to-end: Build Context → Migrate → Validate → Build → Run.
5. Document sample AST/RPG layout for UAT users.
