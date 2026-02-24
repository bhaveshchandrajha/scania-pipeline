# 🚀 Quick Demo Start Guide

## One-Command Demo

Simply run:

```bash
./run_demo.sh
```

This will:
1. ✅ Check prerequisites (Maven, Java 17+)
2. ✅ Set up project structure
3. ✅ Copy all Java files to proper locations
4. ✅ Build the application
5. ✅ Start the Spring Boot server

## What You'll See

After running the script, the application will start on **http://localhost:8080**

### Access Points:

1. **Demo UI**: http://localhost:8080/demo.html
   - Interactive demo page with API testing
   - Architecture overview
   - Status checking

2. **Swagger UI**: http://localhost:8080/swagger-ui.html
   - Interactive API documentation
   - Test endpoints directly

3. **H2 Database Console**: http://localhost:8080/h2-console
   - JDBC URL: `jdbc:h2:mem:warranty_db`
   - Username: `sa`
   - Password: (empty)

## Demo Features

### ✅ Architecture
- **Layered Architecture**: Domain/Service/Repository/DTO/Web
- **20 Java Files**: Properly organized by layer
- **100% Database Mapping**: All 293 columns mapped

### ✅ Modern Java
- **Java Records**: For DTOs
- **Streams API**: For data processing
- **Optional**: For null safety
- **Enums**: For status codes

### ✅ REST API
- **POST /api/claims/search**: Search claims
- **POST /api/claims**: Create claim
- **Swagger Documentation**: Interactive API docs

## Troubleshooting

### Port Already in Use?
Change port in `warranty_demo/src/main/resources/application.properties`:
```properties
server.port=8081
```

### Java Version Issue?
Requires Java 17+:
```bash
java -version
```

### Build Errors?
```bash
cd warranty_demo
mvn clean
mvn dependency:resolve
mvn package
```

## Files Created

- `warranty_demo/` - Complete Spring Boot project
- `demo.html` - Interactive demo UI
- `DEMO_GUIDE.md` - Detailed guide

## Next Steps After Demo

1. Review the code structure in `warranty_demo/src/main/java/com/scania/warranty/`
2. Test the API endpoints using Swagger UI
3. Check the database using H2 Console
4. Review validation results in `VALIDATION_RESULTS.md`

---

**Ready to demo?** Just run `./run_demo.sh`! 🎉
