# Showing successful migration to the client (HS1210_n1)

After the Maven build, you can execute the **migrated read SQLs** and show the client that the migration works.

**Important:** The endpoint `/api/demo/migrated-queries` is served by the **Spring Boot (Java) application**, not by the Python pipeline UI server. If you call it on the UI server port (e.g. 8002), the UI server can **proxy** the request to the Spring Boot app (default port 8081) when the app is running. If you get "Not Implemented" or 503, start the Spring Boot app that includes this module (see step 1 below).

## 1. Run the application

- **Option A – Use an existing Spring Boot app**  
  Copy the contents of `HS1210_n1_pure_java` (domain, repository, service, dto, web) into your Spring Boot project’s `src/main/java/com/scania/warranty/` so that:
  - `MigrationDemoController` is in package `com.scania.warranty.web`
  - The app has a `main` class and `pom.xml` with `spring-boot-starter-web` and `spring-boot-starter-data-jpa`.

- **Option B – Create a small runner**  
  Create a new Spring Boot app, add this module’s Java files and the same dependencies, define a `main` class, and run it.

Then:

```bash
mvn clean package
java -jar target/<your-app>.jar
```

Or:

```bash
mvn spring-boot:run
```

By default the app runs on **port 8080 or 8081** (check `application.properties` or `server.port`). If you use the **Python pipeline UI server** (e.g. port 8002), it can proxy `/api/demo/*` to the Spring Boot app: set `DEMO_BACKEND_PORT` to your app port (default 8081) and call `http://0.0.0.0:8002/api/demo/migrated-queries` — the UI server will forward to the Java app.

## 2. Execute the migrated read SQLs and show the client

### One URL – full report (recommended)

Open in a browser or call with `curl`:

```text
GET http://0.0.0.0:8080/api/demo/migrated-queries
```

(Replace `8080` with your server port if different.)

**Response:** A JSON report listing each migrated read SQL, its execution status (`OK` or `ERROR`), and the result (or error message). Example:

```json
{
  "title": "HS1210_n1 migrated read SQLs – execution report",
  "summary": "Each entry below corresponds to a read SQL migrated from RPG to Spring Data JPA @Query.",
  "queries": [
    { "name": "findDefaultConfiguration", "sql": "SELECT s FROM SystemConfiguration s WHERE s.key = '1'", "status": "OK", "result": { ... } },
    { "name": "toUpperCase", "sql": "SELECT UPPER(:text) FROM SYSIBM.SYSDUMMY1", "status": "OK", "result": { ... } },
    { "name": "findWorkTicketSidById", "sql": "SELECT wkt_sid FROM hswktf WHERE wkt_id = :id", "status": "OK", "result": { ... } },
    { "name": "findAggregatedPositionsByDealerAndClaim", "sql": "SELECT LISTAGG(...) FROM HSGPSPF ...", "status": "OK", "result": { ... } }
  ],
  "successCount": 4,
  "totalCount": 4,
  "migrationSuccessful": true
}
```

Use this to show the client: **“After build, we call this URL and every migrated read SQL runs successfully.”**

### Individual endpoints (optional)

You can also demonstrate each query separately (e.g. from Swagger or Postman):

| What it shows | Method | URL |
|----------------|--------|-----|
| Default config (SELECT … WHERE key = '1') | GET | `/api/demo/default-config` |
| UPPER (SELECT UPPER(:text) FROM SYSIBM.SYSDUMMY1) | GET | `/api/demo/uppercase?text=hello` |
| Work ticket SID (SELECT wkt_sid FROM hswktf …) | GET | `/api/demo/workticket-sid?id=1` |
| Aggregated positions (LISTAGG from HSGPSPF) | GET | `/api/demo/aggregated-positions?dealerId=00001&claimNo=CLAIM001` |

## 3. Database

- For a **real demo**, point the app to a database that has the tables used by these queries (e.g. `SystemConfiguration`, `FISTAM`, `hswktf`, `HSGPSPF`), or create them via JPA/schema scripts.
- For an **in-memory demo** (e.g. H2), add schema/data scripts so those tables exist and return at least one row where needed; then the same URLs above will show the queries executing successfully.

## 4. Short script for the client

```bash
# After app is running (e.g. on port 8080):
curl -s http://0.0.0.0:8080/api/demo/migrated-queries | jq .
# Or without jq:
curl -s http://0.0.0.0:8080/api/demo/migrated-queries
```

If `migrationSuccessful` is `true` and all entries in `queries` have `"status": "OK"`, the migrated read SQLs are executing successfully after the Maven build.
