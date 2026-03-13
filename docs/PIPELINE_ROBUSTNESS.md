# Pipeline Robustness Guide

This document describes how the pipeline is designed to run successfully without manual intervention.

---

## Design Principles

1. **Build always succeeds** – Compile + tests must pass before Run & Demo
2. **Run works offline** – Default database is H2 in-memory; no RDS or network required
3. **Clear failure paths** – When something fails, the pipeline provides actionable feedback

---

## Database Strategy

| Scenario | Database | How |
|----------|----------|-----|
| **Pipeline Run & Demo** | H2 in-memory | Default; no profile needed |
| **Local development** | H2 in-memory | `mvn spring-boot:run` or `./start_demo.sh` |
| **RDS / staging** | PostgreSQL | `./start_demo_rds.sh` or `-Dspring-boot.run.profiles=rds` |
| **Unit tests** | H2 in-memory | Profile `test` (automatic) |

**Why H2 as default:** PostgreSQL (RDS) requires network access and can time out when unreachable (VPN, firewall, offline). H2 runs in-process and always works. The pipeline Run & Demo uses the default, so it succeeds automatically.

---

## Build Gate

Step 4 (Build Application) requires:

- `mvn compile` ✓
- `mvn package -DskipTests` ✓
- `mvn test` ✓

If any step fails, the build is marked failed and Run & Demo is not offered. This catches JPA mapping errors (e.g. IdClass mismatch) before runtime.

---

## Run & Demo Flow

1. User clicks "Run Application" in Tab 5
2. Server runs `mvn spring-boot:run -DskipTests` (no profile = H2 default)
3. App starts on port 8081
4. UI polls until app responds, then shows "Application is running"

No RDS, VPN, or external services required.

---

## Troubleshooting

| Issue | Cause | Fix |
|-------|-------|-----|
| Connection timeout to PostgreSQL | Running with RDS profile when RDS unreachable | Use default (H2) or `./start_demo.sh` |
| ApplicationContext failed (IdClass) | Entity/IdClass property mismatch | See `docs/HITL_AND_BUILD_SUCCESS_PLAN.md`; fix applied for Hsahkpf |
| Build fails (compile) | Syntax or missing symbols | LLM autofix in Tab 4; or manual fix |
| Build fails (tests) | JPA/runtime error | Run `validate_before_run.py` for hints |

---

## Scripts Summary

| Script | Database | Use when |
|--------|----------|----------|
| `./start_demo.sh` | H2 | Default; always works |
| `./start_demo_local.sh` | H2 | Same as default (explicit local) |
| `./start_demo_rds.sh` | PostgreSQL | RDS available and needed |
