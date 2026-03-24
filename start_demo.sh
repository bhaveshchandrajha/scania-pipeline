#!/bin/bash
# Quick start script for Warranty Claim Management Demo

set -e

echo "=========================================="
echo "Starting Warranty Claim Management Demo"
echo "=========================================="
echo ""

# Check if warranty_demo exists
if [ ! -d "warranty_demo" ]; then
    echo "❌ Error: warranty_demo directory not found"
    echo "Please run ./run_demo.sh first to set up the project"
    exit 1
fi

cd warranty_demo

# Check if JAR exists, if not build it
if [ ! -f "target/warranty-claim-management-1.0.0.jar" ]; then
    echo "Building application..."
    mvn clean package -DskipTests
    echo ""
fi

# Set Java 17+ if available
if [ -d "$HOME/.sdkman/candidates/java" ]; then
    if [ -n "$JAVA_HOME" ] && [ -f "$JAVA_HOME/bin/java" ]; then
        CURRENT_VERSION=$("$JAVA_HOME/bin/java" -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$CURRENT_VERSION" -ge 17 ] 2>/dev/null; then
            export JAVA_HOME="$JAVA_HOME"
        fi
    fi
    
    if [ -z "$JAVA_HOME" ] || [ ! -f "$JAVA_HOME/bin/java" ]; then
        for jdk_dir in ~/.sdkman/candidates/java/*/; do
            if [ -d "$jdk_dir" ] && [ -f "$jdk_dir/bin/java" ]; then
                VERSION=$("$jdk_dir/bin/java" -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
                if [ "$VERSION" -ge 17 ] 2>/dev/null; then
                    export JAVA_HOME="$jdk_dir"
                    break
                fi
            fi
        done
    fi
fi

export PATH="$JAVA_HOME/bin:$PATH"

echo "Starting Spring Boot application on port 8081..."
echo ""
echo "Access points:"
echo "  - Demo UI: http://localhost:8081/demo.html"
echo "  - Swagger UI: http://localhost:8081/swagger-ui.html"
echo "  - H2 Console: http://localhost:8081/h2-console"
echo ""
echo "Press Ctrl+C to stop"
echo ""
echo "Tip: Use ./start_demo_rds.sh to run with AWS RDS PostgreSQL"
echo ""

# Default uses H2 in-memory (robust, no external DB required)
mvn spring-boot:run
