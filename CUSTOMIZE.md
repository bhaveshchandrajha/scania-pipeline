# Customizing the Migrated Warranty Application

This guide helps you modify the migrated Java application to fit your needs.

## Project Structure

  warranty_demo/
  ├── src/main/java/com/scania/warranty/
  │   ├── domain/       # Entities (Claim, Invoice, etc.)
  │   ├── service/      # Business logic (ClaimSearchService, etc.)
  │   ├── repository/   # Data access (Spring Data JPA)
  │   ├── dto/          # Request/response objects (Records)
  │   └── web/          # REST controllers
  └── src/main/resources/
      ├── application.properties
      └── static/demo.html

## Where to Make Changes

### Business Logic
  → service/
  Edit service classes (e.g. ClaimSearchService.java) to change how claims are
  searched, validated, or processed. Services contain the core logic migrated
  from RPG.

### API Endpoints
  → web/
  Controllers define REST endpoints. To add or change endpoints, edit the
  controller classes (ClaimController, ClaimSubfileController, etc.).

### Data Model
  → domain/ and dto/
  - domain/: JPA entities (database mapping)
  - dto/: Java Records for API request/response

### Database Queries
  → repository/
  Spring Data JPA repositories. Add custom queries with @Query or method names
  following Spring Data conventions.

### Configuration
  → src/main/resources/application.properties
  Change server port, database URL, logging, etc.

## Traceability

Generated code includes // @origin comments linking Java to the source RPG:
  // @origin HS1210 L830-833 (IF)

Use these to understand which RPG block a Java section came from. Hover over
@origin lines in the Validation tab to preview the source RPG.

## Running Locally

  cd warranty_demo
  mvn spring-boot:run

Demo UI: http://0.0.0.0:8081/demo.html

## Testing Changes

  1. Build: mvn clean package -DskipTests
  2. Run: mvn spring-boot:run
  3. Test: Use the API Test buttons in the Global Context UI (Run & Demo tab)
     or call endpoints directly (e.g. POST /api/claims/search)

## Re-migrating

If you migrate additional features from the Global Context UI, new code is
merged into warranty_demo. Your customizations in existing files may be
overwritten. Consider:
  - Keeping custom logic in separate classes that extend or wrap generated ones
  - Using feature branches when experimenting with migrations
