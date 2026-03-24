#!/bin/bash
# Start Warranty Claim Management with H2 (local/offline fallback)
# Use when RDS is unreachable or for offline development

set -e

if [ ! -d "warranty_demo" ]; then
    echo "Error: warranty_demo directory not found"
    exit 1
fi

cd warranty_demo

echo "=========================================="
echo "Starting with H2 (local/offline)"
echo "=========================================="
echo ""
echo "Database: H2 in-memory"
echo "H2 Console: http://localhost:8081/h2-console"
echo ""
echo "Access points:"
echo "  - Angular UI: http://localhost:8081/angular/"
echo "  - Demo: http://localhost:8081/demo.html"
echo "  - Swagger: http://localhost:8081/swagger-ui.html"
echo ""
echo "Press Ctrl+C to stop"
echo ""

mvn spring-boot:run -Dspring-boot.run.profiles=local
