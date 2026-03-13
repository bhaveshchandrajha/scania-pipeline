# UAT Deployment Plan – Warranty Claim Management (Containerized)

## Overview

Deploy the Warranty Claim Management application for UAT as a self-contained Docker container. No external platform, database, or configuration is required beyond Docker.

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

## Build & Run

### Option A: Docker only

```bash
# From project root (ScaniaRPG2JavaRevisedDesign)
docker build -t warranty-claim-uat:1.0.0 .
docker run -p 8081:8081 -v warranty_data:/data warranty-claim-uat:1.0.0
```

### Option B: Docker Compose (recommended)

```bash
# From project root
docker-compose up -d
```

---

## Deliverables

| File | Purpose |
|------|---------|
| `Dockerfile` | Multi-stage build: Angular → Maven → JAR → runtime image |
| `.dockerignore` | Exclude source, tests, and dev files from build context |
| `docker-compose.yml` | One-command run with port mapping and data volume |
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
# Example GitHub Actions / GitLab CI
build:
  - cd warranty_demo
  - docker build -t warranty-claim-uat:$VERSION .
  - docker push registry/warranty-claim-uat:$VERSION
```

---

## Checklist Before UAT

- [ ] Angular UI built and copied to `warranty_demo/src/main/resources/static/angular/` (`npm run build:spring`)
- [ ] `mvn package -DskipTests` succeeds in `warranty_demo`
- [ ] Docker image builds without errors
- [ ] Container starts and responds on port 8081
- [ ] Claims list loads and create flow works
- [ ] H2 console accessible and DB tables visible
