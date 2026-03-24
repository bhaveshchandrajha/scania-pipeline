# Warranty Claim Management System - Demo Guide

## Quick Start

### Option 1: Automated Script (Recommended)

```bash
chmod +x run_demo.sh
./run_demo.sh
```

This script will:
1. Check prerequisites (Maven, Java 17+)
2. Set up the project structure
3. Build the application
4. Start the Spring Boot server

### Option 2: Manual Setup

```bash
# 1. Set up project structure
mkdir -p warranty_demo/src/main/java/com/scania/warranty
mkdir -p warranty_demo/src/main/resources
mkdir -p warranty_demo/src/main/resources/static

# 2. Copy Java files
cp -r HS1210_n404_pure_java/* warranty_demo/src/main/java/com/scania/warranty/

# 3. Copy configuration
cp HS1210_n404_pure_java/application.properties warranty_demo/src/main/resources/
cp pom_warranty.xml warranty_demo/pom.xml
cp demo.html warranty_demo/src/main/resources/static/

# 4. Build and run
cd warranty_demo
mvn clean package
mvn spring-boot:run
```

## Access Points

Once the application is running:

- **Demo UI**: http://localhost:8080/demo.html
- **API Base**: http://localhost:8080/api/claims
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:warranty_db`
  - Username: `sa`
  - Password: (empty)

## API Endpoints

### Search Claims
```bash
POST http://localhost:8080/api/claims/search
Content-Type: application/json

{
  "companyCode": "001",
  "openClaimsOnly": false,
  "ascending": true
}
```

### Create Claim
```bash
POST http://localhost:8080/api/claims
Content-Type: application/json

{
  "companyCode": "001",
  "invoiceNumber": "12345",
  "invoiceDate": "20240101",
  "jobNumber": "67890",
  "workshopType": "1"
}
```

## Demo Features

### 1. Architecture Showcase
- **Layered Architecture**: Domain/Service/Repository/DTO/Web layers
- **Domain-Driven Design**: Entities use domain names (Claim, not HSG71LF2)
- **Modern Java**: Records, Streams, Optional, Enums

### 2. Database Mapping
- **100% Column Mapping**: All 293 columns from 6 database contracts mapped
- **JPA Entities**: Proper @Entity and @Column annotations
- **Spring Data JPA**: Repository interfaces with custom queries

### 3. REST API
- **RESTful Design**: Proper HTTP methods and status codes
- **JSON DTOs**: Java Records for data transfer
- **Swagger Documentation**: Interactive API docs

### 4. Validation
- **91.7% Overall Score**
- **100% Database Mapping**
- **100% Architecture Compliance**
- **100% Modern Java Features**

## Troubleshooting

### Port Already in Use
If port 8080 is already in use, edit `application.properties`:
```properties
server.port=8081
```

### Java Version
Requires Java 17 or higher:
```bash
java -version
```

### Maven Not Found
Install Maven: https://maven.apache.org/install.html

### Build Errors
If you encounter build errors:
```bash
cd warranty_demo
mvn clean
mvn dependency:resolve
mvn package
```

## Project Structure

```
warranty_demo/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/scania/warranty/
│   │   │       ├── WarrantyApplication.java (Main class)
│   │   │       ├── domain/ (8 files)
│   │   │       ├── service/ (2 files)
│   │   │       ├── repository/ (6 files)
│   │   │       ├── dto/ (3 files)
│   │   │       └── web/ (1 file)
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           └── demo.html
│   └── test/
└── pom.xml
```

## Next Steps

1. **Add Real Database**: Replace H2 with PostgreSQL/MySQL
2. **Add Authentication**: Spring Security integration
3. **Add Tests**: Unit and integration tests
4. **Add Logging**: Structured logging with Logback
5. **Add Monitoring**: Actuator endpoints for health checks

## Support

For issues or questions:
- Check `VALIDATION_RESULTS.md` for validation details
- Review `TRACK_B_QUICK_START.md` for migration information
- Check application logs in console output
