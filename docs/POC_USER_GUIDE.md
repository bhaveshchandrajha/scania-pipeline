# PoC User Guide — End-to-End Pipeline and Migrated Application

**Version:** 1.0  
**Date:** March 2026  
**Audience:** PoC users, demo participants, testers

This guide walks you through the **entire migration pipeline** from locating ASTs and RPGs to running the migrated Spring Boot application with Angular UI. The Maven build and application run are the **final steps** of the pipeline.

---

## Table of Contents

### Part 1: Running the Full Pipeline

1. [Prerequisites](#1-prerequisites)
2. [Step 1: Locate ASTs and RPGs](#2-step-1-locate-asts-and-rpgs)
3. [Step 2: Start the Pipeline UI](#3-step-2-start-the-pipeline-ui)
4. [Step 3: Tab 1 — Build Global Context](#4-step-3-tab-1--build-global-context)
5. [Step 4: Tab 2 — Migrate Feature](#5-step-4-tab-2--migrate-feature)
6. [Step 5: Tab 3 — Validation](#6-step-5-tab-3--validation)
7. [Step 6: Tab 4 — Build Application](#7-step-6-tab-4--build-application)
8. [Step 7: Tab 5 — Run & Demo](#8-step-7-tab-5--run--demo)

### Part 2: Using the Migrated Application

9. [Application Access Points](#9-application-access-points)
10. [Screen 1: Welcome / Home](#10-screen-1-welcome--home)
11. [Screen 2: Claims List (HS1210D)](#11-screen-2-claims-list-hs1210d)
12. [Screen 3: Create Claim](#12-screen-3-create-claim)
13. [Screen 4: Claim Detail](#13-screen-4-claim-detail)
14. [Troubleshooting](#14-troubleshooting)

---

## 1. Prerequisites

| Requirement | How to Check |
|-------------|--------------|
| **Python 3.x** | `python3 --version` |
| **Java 17+** | `java -version` |
| **Maven** | `mvn -version` |
| **ANTHROPIC_API_KEY** | Required for LLM code generation (Tab 2) and autofix (Tab 4). Set in environment or `.env` |

---

## 2. Step 1: Locate ASTs and RPGs

### 2.1 AST Directory

AST (Abstract Syntax Tree) files are JSON exports from PKS Systems tooling. They are typically under:

```
JSON_ast/
├── JSON_20260211/     # Example
├── JSON_20260227/
└── JSON_20260311/     # Often the latest
    ├── HS1210-ast.json
    ├── HS1210D-ast.json   # Display file
    ├── HS1212-ast.json
    └── HS1212D-ast.json
```

**What to use:** Pick the AST directory that contains the program you want to migrate (e.g. `JSON_ast/JSON_20260311` for HS1210).

### 2.2 RPG Directory

RPG source files (`.sqlrpgle`, `.rpgle`) are used for:

- Call graph enrichment (resolving procedure calls)
- RPG snippet extraction in context packages

Typical locations:

- `PoC_HS1210/` — PoC RPG source
- `HS1210D_20260216/` — Display file source
- Or a path to your RPG project

**Note:** RPG directory is **optional** for Tab 1. Without it, the call graph will be less complete, but migration can still run.

---

## 3. Step 2: Start the Pipeline UI

### 3.1 Start the server

From the project root:

```bash
./start_global_context_ui.sh
```

Or manually:

```bash
export UI_PORT=8003
python3 ui_global_context_server.py
```

### 3.2 Open the UI

Open a browser and go to: **http://127.0.0.1:8003/**

You will see the **Pipeline UI** with 5 tabs:

| Tab | Name |
|-----|------|
| 1 | Build Global Context |
| 2 | Migrate Feature |
| 3 | Validation |
| 4 | Build Application |
| 5 | Run & Demo |

---

## 4. Step 3: Tab 1 — Build Global Context

**Purpose:** Create the foundation for migration — DB registry, program context, call graph, and per-node context packages.

### 4.1 Discover directories

1. In **Tab 1**, click **Discover Directories** (or the equivalent control).
2. The UI populates dropdowns with:
   - **AST directories** under `JSON_ast/`
   - **RPG directories** (project root, `PoC_HS1210`, etc.)

### 4.2 Select directories

1. **AST Directory:** Select e.g. `JSON_ast/JSON_20260311`
2. **RPG Directory:** Select e.g. `PoC_HS1210` (or leave empty if not available)

### 4.3 Build Global Context

1. Click **Build Global Context**.
2. Wait for all 7 sub-steps to complete (typically 1–3 minutes).

**Outputs created:**

| Artifact | Location |
|----------|----------|
| DB registry | `global_context/db_registry.json` |
| Program context | `global_context/programs/*.program.json` |
| Call graph | `global_context/call_graph.json`, `call_graph_enriched.json` |
| Context index | `context_index/<unit>_<node>.json` |
| Manifest | `context_index/manifest.json` |
| Neo4j export | `global_context/neo4j_export.cypher` |

**Success:** The output area shows completion messages. You can proceed to Tab 2.

---

## 5. Step 4: Tab 2 — Migrate Feature

**Purpose:** Generate Java code from RPG using the LLM. This is the core migration step.

### 5.1 Refresh feature list

1. In **Tab 2**, click **Refresh Feature List**.
2. Programs and features (entry nodes) are loaded from the context built in Tab 1.

### 5.2 Select program and feature

1. **Program ID:** Select e.g. `HS1210`
2. **Entry Node ID:** Select e.g. `n404` (main subroutine)
3. **RPG Directory:** (optional) Same as Tab 1 for best results

### 5.3 Migrate feature

1. Click **Migrate Feature**.
2. **Wait:** Large nodes (e.g. n404) can take 15–25 minutes. Progress is streamed.
3. On success, Java files are written to `warranty_demo/src/main/java/com/scania/warranty/`.

**Output:** Domain entities, repositories, services, controllers, DTOs. A migration manifest is written to `global_context/migrations/`.

---

## 6. Step 5: Tab 3 — Validation

**Purpose:** Validate the generated Java — structure, DB mapping, logic completeness, traceability.

### 6.1 Run validation

1. In **Tab 3**, select **Program** and **Feature** (for traceability).
2. Click **Run Validation**.
3. Review the **Code Quality Score Card** (Structural, Semantic, Behavioral).

**Score thresholds:**

| Score | Status |
|-------|--------|
| ≥ 95% | PASSED |
| ≥ 80% | WARNING |
| < 80% | FAILED |

**Optional:** Use the traceability viewer to see Java ↔ RPG line mapping.

---

## 7. Step 6: Tab 4 — Build Application

**Purpose:** Compile the Java application and fix compilation errors using an LLM-based autofix loop.

### 7.1 Build application

1. In **Tab 4**, click **Build Application**.
2. The pipeline runs `mvn clean compile` (or `mvn clean package -DskipTests`).
3. If compile errors occur, the **autofix loop** runs (up to 4 LLM passes).
4. On success, the JAR is built and tests run (if configured).

**Success:** Build completes with no errors. You can proceed to Tab 5.

---

## 8. Step 7: Tab 5 — Run & Demo

**Purpose:** Start the migrated Spring Boot application and access the demo UIs.

### 8.1 Run application

1. In **Tab 5**, click **Run Application** (or **Run**).
2. Select profile if needed (default: H2; RDS for PostgreSQL).
3. Spring Boot starts in the background on port 8081.

### 8.2 Open demo

1. Click **Open Demo** (or the link shown).
2. Or open manually: **http://localhost:8081**

### 8.3 Access points

| URL | Description |
|-----|-------------|
| http://localhost:8081 | Index page with links |
| http://localhost:8081/angular/ | Angular UI (main claims interface) |
| http://localhost:8081/demo.html | Demo overview and API status |
| http://localhost:8081/swagger-ui.html | Swagger API documentation |
| http://localhost:8081/h2-console | H2 database console |

**Optional:** If claim creation fails with "Invoice not found", use **Seed Demo Data** (POST /api/seed) from Tab 5 or call the API directly.

---

## 9. Application Access Points

After the pipeline completes and the application is running:

| Screen / Tool | URL |
|---------------|-----|
| Index (entry) | http://localhost:8081 |
| Angular UI | http://localhost:8081/angular/ |
| Demo page | http://localhost:8081/demo.html |
| Swagger API | http://localhost:8081/swagger-ui.html |
| H2 Console | http://localhost:8081/h2-console |

---

## 10. Screen 1: Welcome / Home

### How to access

- Click **Angular UI** on the index page, or
- Go to: **http://localhost:8081/angular/#/**

### What you see

| Element | Description |
|---------|-------------|
| **Title** | "Warranty Claim Management" |
| **Subtitle** | "HS1210 Migration" |
| **Links** | Claims List, Create Claim, Demo, Swagger API |

### What to do

| Action | Result |
|--------|--------|
| Click **Claims List (HS1210D)** | Opens the claims list |
| Click **Create Claim** | Opens the create claim form |
| Click **Demo** | Opens demo.html (new tab) |
| Click **Swagger API** | Opens Swagger UI (new tab) |

---

## 11. Screen 2: Claims List (HS1210D)

### How to access

- From Welcome: click **Claims List (HS1210D)**
- Direct URL: **http://localhost:8081/angular/#/claims**

### What you see

| Element | Description |
|---------|-------------|
| **Title** | "Warranty Claims – HS1210D" |
| **Company filter** | Text input (default: `001`) |
| **Open claims only** | Checkbox |
| **Action buttons** | CF05 Refresh, CF06 Create, CF11 View, etc. |
| **Table** | Claim Nr., Invoice, Invoice Date, Chassis, Customer, Status, etc. |

### What to do

| Action | How |
|--------|-----|
| Filter by company | Change company code; list refreshes |
| Refresh | Click **CF05 Aktualisieren (Refresh)** |
| Create claim | Click **CF06 Erstellen (Create)** |
| View claim | Select row, click **CF11 Ansicht (View)** |
| Delete claim | Select row, click **Delete Claim** |

---

## 12. Screen 3: Create Claim

### How to access

- From Welcome: click **Create Claim**
- From Claims List: click **CF06 Erstellen (Create)**
- Direct URL: **http://localhost:8081/angular/#/claims/create**

### What you see

| Field | Example |
|-------|---------|
| Company Code | `001` |
| Invoice Number | `88888` |
| Invoice Date | `20240115` |
| Order Number | `003` |
| Workshop Type / Area | `1` |

### What to do

1. Enter values (or use defaults for seeded invoices: 88888/003, 77777/004, 99999/002).
2. Click **Create Claim**.
3. On success, use **View in list** to see the new claim.

---

## 13. Screen 4: Claim Detail

### How to access

- From Claims List: select row, click **CF11 Ansicht (View)**
- Direct URL: **http://localhost:8081/angular/#/claims/001/00000001**

### What to do

| Action | Result |
|--------|--------|
| **Back** | Returns to previous screen |
| **Delete Claim** | Deletes the claim (with confirmation) |

---

## 14. Troubleshooting

### Pipeline (Tabs 1–5)

| Issue | Fix |
|-------|-----|
| Discover Directories returns empty | Ensure `JSON_ast/` has subdirs with `*-ast.json` files |
| Tab 1 fails | Check AST directory path; ensure files exist |
| Tab 2: "ANTHROPIC_API_KEY" | Set `export ANTHROPIC_API_KEY=...` or add to `.env` |
| Tab 2: Migration times out | Large nodes take 15–25 min; wait or try a smaller node |
| Tab 4: Build fails | Autofix runs automatically; check build log for remaining errors |

### Application (after Tab 5)

| Issue | Fix |
|-------|-----|
| "No claims found" | Call `POST /api/seed` or `POST /api/seed-invoices` |
| "Invoice not found" on create | Same as above; or delete `warranty_demo/data/warranty_db*` and restart |
| Port 8081 in use | Change `server.port` in `application.properties` |
| Angular UI not loading | Run `cd warranty-ui && npm run build:spring` to copy Angular assets |

---

## 15. Quick Reference — Pipeline Flow

```
1. Locate ASTs (JSON_ast/...) and RPGs (PoC_HS1210, etc.)
2. Start Pipeline UI (python3 ui_global_context_server.py) → http://127.0.0.1:8003/
3. Tab 1: Build Global Context (AST dir + RPG dir)
4. Tab 2: Migrate Feature (Program + Entry Node)
5. Tab 3: Validation (Code Quality Score Card)
6. Tab 4: Build Application (Maven + autofix)
7. Tab 5: Run & Demo → http://localhost:8081
8. Use Angular UI, demo.html, Swagger, H2 Console
```

---

*For questions or issues, contact the migration team.*
