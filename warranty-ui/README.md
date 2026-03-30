# Warranty UI – Angular Frontend

Angular UI for the Warranty Claim Management system, generated from display file (DDS/RPG) metadata.

## Screens

- **Welcome** – Home screen with navigation links (from `Welcome.json` schema)
- **HS1210D** – Warranty claims list (from `HS1210D.json` schema, DDS display file)

## Development

```bash
# Install dependencies (if needed)
npm install

# Start Angular dev server (port 4200)
# API requests are proxied to http://0.0.0.0:8081
npm start
```

**Prerequisite:** Start the Spring Boot backend:

```bash
cd ../warranty_demo && mvn spring-boot:run
```

Then open http://0.0.0.0:4200

## Build for Spring Boot

```bash
npm run build:spring
```

This builds the Angular app and copies the output to `warranty_demo/src/main/resources/static/angular/`. After that, the Angular UI is available at:

**http://0.0.0.0:8081/angular/** (uses hash routing: /angular/#/claims)

(When the Spring Boot app is running.)

## Data Sources

- UI schemas: `/api/ui-schemas/{screenId}` (e.g. Welcome, HS1210D)
- Claims search: `POST /api/claims/search`
