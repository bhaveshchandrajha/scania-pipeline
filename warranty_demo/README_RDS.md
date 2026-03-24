# Database Configuration

## Default: H2 in-memory (robust, no external DB)

The application **defaults to H2 in-memory** so it runs successfully without network access or RDS.

```bash
# From project root - works offline, no RDS required
./start_demo.sh
```

Or:

```bash
cd warranty_demo
mvn spring-boot:run
```

- **H2 Console:** http://localhost:8081/h2-connect.html (one-click) or paste `jdbc:h2:file:./data/warranty_db`, User: sa, Driver: Generic H2 (Embedded)
- **Demo:** http://localhost:8081/demo.html
- **Swagger:** http://localhost:8081/swagger-ui.html

---

## AWS RDS PostgreSQL (profile `rds`)

Use the `rds` profile when you need PostgreSQL (staging, production, shared data):

```bash
./start_demo_rds.sh
```

Or manually:

```bash
cd warranty_demo
mvn spring-boot:run -Dspring-boot.run.profiles=rds
```

RDS configuration is in `application-rds.properties`:

- **URL:** `jdbc:postgresql://database-1.cfmmi6uikb1h.ap-southeast-2.rds.amazonaws.com:5432/postgres`
- **Username:** `postgres`
- **Password:** Set via `SPRING_DATASOURCE_PASSWORD` env var, or defaults to `Master123`

### Security (Production)

```bash
export SPRING_DATASOURCE_PASSWORD=your_secure_password
./start_demo_rds.sh
```

---

## Profiles Summary

| Profile | Database | Use case |
|---------|----------|----------|
| (default) | H2 in-memory | Local dev, pipeline Run & Demo, offline |
| `rds` | AWS RDS PostgreSQL | Staging, production, shared data |
| `local` | H2 in-memory | Same as default (legacy alias) |
| `test` | H2 in-memory | Unit tests only |

---

## Schema

With `spring.jpa.hibernate.ddl-auto=update`, Hibernate creates or updates tables on startup. Tables follow the RPG-derived schema (e.g. `HSAHKLF3`, `HSG71LF2`, `HSG73PF`).

### Pre-seeded RDS (rds_seed.sql)

To use a pre-populated schema and demo data on RDS, run:

```bash
psql -h database-1.cfmmi6uikb1h.ap-southeast-2.rds.amazonaws.com -U postgres -d postgres -f warranty_demo/rds_seed.sql
```

Or with password: `PGPASSWORD=xxx psql -h host -U postgres -d postgres -f warranty_demo/rds_seed.sql`

**Note:** If you see `column c1_0.g71000 does not exist` or "No claims found", the RDS schema is out of date. Re-run `rds_seed.sql` to drop and recreate tables with the correct column names (g71000, g71010, etc.). The seed includes 2 dummy claims (invoices 12345, 99999) and 4 invoices (12345, 99999, 88888, 77777); invoices 88888 and 77777 have no claims so you can create new ones via the UI.
