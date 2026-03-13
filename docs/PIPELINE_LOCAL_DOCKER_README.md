# Migration Pipeline – Local Docker Run

Run the full migration pipeline in Docker for local examination before deploying to AWS.

## Prerequisites

- Docker 20.10+
- Docker Compose v2+
- `ANTHROPIC_API_KEY` (for Tab 2 Migrate and Tab 4 Autofix)

## Quick Start

```bash
# From project root (ScaniaRPG2JavaRevisedDesign)
export ANTHROPIC_API_KEY=sk-ant-...

# Build and run
docker-compose -f docker-compose-pipeline.yml up --build
```

Then open **http://localhost:8003** for the Pipeline UI.

## What Runs

| Port | Service |
|------|---------|
| 8003 | Pipeline UI (5 tabs: Build Context, Migrate, Validate, Build, Run) |
| 8081 | Spring Boot app (after you click "Run & Demo" in Tab 5) |

## Volume Mount

The project directory is mounted at `/workspace` so the pipeline can:

- Read your AST files (e.g. `JSON_ast/`, `context_index/`)
- Read your RPG source (e.g. `PoC_HS1210/`, `HS1210D_20260216/`)
- Write `global_context/`, `context_index/`, `warranty_demo/` back to your host

All changes persist on your machine.

## Workflow

1. **Tab 1 – Build Global Context**  
   Set AST dir and RPG dir (e.g. `JSON_ast/JSON_20260227`, `PoC_HS1210`).

2. **Tab 2 – Migrate Feature**  
   Select Program + Entry Node → generates Java (requires `ANTHROPIC_API_KEY`).

3. **Tab 3 – Validation**  
   Run validation and review the score card.

4. **Tab 4 – Build Application**  
   Maven build + LLM autofix for compile errors.

5. **Tab 5 – Run & Demo**  
   Start Spring Boot → http://localhost:8081

## Build Only (no compose)

```bash
docker build -f Dockerfile.pipeline -t migration-pipeline:1.0 .
docker run -p 8003:8003 -p 8081:8081 -v $(pwd):/workspace -e ANTHROPIC_API_KEY=xxx migration-pipeline:1.0
```

## Without API Key

You can run without `ANTHROPIC_API_KEY` for:

- Tab 1 (Build Global Context)
- Tab 3 (Validation)
- Tab 5 (Run & Demo) if the app is already built

Tab 2 (Migrate) and Tab 4 (Autofix) need the key.

## Next: Deploy to AWS

After local verification, use the same image on AWS (ECS, EKS, EC2, etc.) with:

- Volumes for AST/RPG and output
- `ANTHROPIC_API_KEY` from Secrets Manager or env
- Ports 8003 and 8081 exposed
