# UAT Deployment Plan – Warranty Claim Management

## Overview

> **Docker:** The repository root `Dockerfile` builds the **migration pipeline** only (`ui_global_context_server.py` on port 8003). See `docker-compose-pipeline.yml` and `docs/PIPELINE_LOCAL_DOCKER_README.md`. The warranty Spring Boot app is **not** containerized by that image; run it with Maven or a JAR as below.

Deploy the Warranty Claim Management application for UAT. For a fully self-contained **container** UAT of the Spring Boot app, build a JAR (Angular + Maven) and run it with Docker or Java on the host—no warranty-specific `Dockerfile` is maintained at the repo root.

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│  Docker Container (warranty-claim-management)           │
│  ┌───────────────────────────────────────────────────┐  │
│  │  Spring Boot (port 8081)                            │  │
│  │  • REST API (/api/claims/*)                        │  │
│  │  • Angular UI (/angular/, /demo.html, /hs1210d)    │  │
│  │  • H2 Console (/h2-console)                        │  │
│  │  • Swagger UI (/swagger-ui.html)                   │  │
│  └───────────────────────────────────────────────────┘  │
│  ┌───────────────────────────────────────────────────┐  │
│  │  H2 Database (file-based for persistence)           │  │
│  │  • Data in /data/warranty_db (mounted volume)      │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## Prerequisites

- **Docker** 20.10+ (or Podman with Docker compatibility)
- **Docker Compose** v2+ (optional, for `docker-compose up`)

---

## Build & Run (Spring Boot, no root Dockerfile)

### Option A: Maven (recommended for local UAT)

```bash
# Build Angular into warranty_demo static, then run Spring Boot
cd warranty-ui && npm ci && npm run build -- --base-href /angular/
# Copy dist to warranty_demo per project convention, then:
cd ../warranty_demo && mvn spring-boot:run
```

### Option B: JAR

```bash
cd warranty_demo && mvn package -DskipTests
java -jar target/warranty-claim-management-1.0.0.jar
```

Use profile `docker` and a host directory for H2 if you need file persistence: `-Dspring-boot.run.profiles=docker` and ensure `./data` or `/data` is writable per `application-docker.properties`.

---

## Deliverables

| File | Purpose |
|------|---------|
| `Dockerfile` | Migration pipeline image: Python UI (`ui_global_context_server.py`) + Node/Maven/Java tooling |
| `docker-compose-pipeline.yml` | Run migration pipeline UI (port 8003) |
| `.dockerignore` | Smaller build context for pipeline image |
| `warranty_demo/src/main/resources/application-docker.properties` | Docker profile: H2 file-based DB at `/data` |

---

## UAT Access Points

| URL | Description |
|-----|-------------|
| http://localhost:8081/angular/#/claims | Claims list (Angular) |
| http://localhost:8081/angular/#/claims/create | Create claim |
| http://localhost:8081/demo.html | Demo page |
| http://localhost:8081/hs1210d.html | HS1210D HTML UI |
| http://localhost:8081/h2-console | H2 DB console (JDBC: `jdbc:h2:file:/data/warranty_db`) |
| http://localhost:8081/swagger-ui.html | API docs |

---

## Data Persistence

- **Profile**: `docker`
- **H2 URL**: `jdbc:h2:file:/data/warranty_db;DB_CLOSE_DELAY=-1`
- **Volume**: `warranty_data` → `/data` in container
- Data survives container restarts; remove volume to reset.

---

## Environment Variables (optional)

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | 8081 | HTTP port |
| `SPRING_PROFILES_ACTIVE` | docker | Active profile |

---

## Build Pipeline (CI/CD)

```yaml
# Example: build warranty JAR (no warranty Docker image at repo root)
build:
  - cd warranty_demo
  - mvn package -DskipTests
  # Deploy the JAR artifact to your runtime (EC2, ECS with a custom image, etc.)
```

---

## Checklist Before UAT

- [ ] Angular UI built and copied to `warranty_demo/src/main/resources/static/angular/` (`npm run build:spring`)
- [ ] `mvn package -DskipTests` succeeds in `warranty_demo`
- [ ] Application starts and responds on port 8081
- [ ] Claims list loads and create flow works
- [ ] H2 console accessible and DB tables visible
