#!/bin/bash
# Start Warranty Claim Management with AWS RDS PostgreSQL
# App uses RDS only (no H2). Tests use H2 via profile "test".

set -e

if [ ! -d "warranty_demo" ]; then
    echo "Error: warranty_demo directory not found"
    exit 1
fi

cd warranty_demo

# Optional: override password via env (recommended for production)
# export SPRING_DATASOURCE_PASSWORD=your_secure_password

echo "=========================================="
echo "Starting with AWS RDS PostgreSQL"
echo "=========================================="
echo ""
echo "Database: PostgreSQL on AWS RDS (ap-southeast-2)"
echo ""
echo "Access points:"
echo "  - Angular UI: http://localhost:8081/angular/"
echo "  - Demo: http://localhost:8081/demo.html"
echo "  - Swagger: http://localhost:8081/swagger-ui.html"
echo ""
echo "Press Ctrl+C to stop"
echo ""

mvn spring-boot:run -Dspring-boot.run.profiles=rds
