#!/bin/bash
# Demo Runner Script for Warranty Claim Management System
# Pure Java Application migrated from RPG

set -e

echo "=========================================="
echo "Warranty Claim Management System - Demo"
echo "=========================================="
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Error: Maven is not installed"
    echo "Please install Maven: https://maven.apache.org/install.html"
    exit 1
fi

# Check for Java 17+ installation
# Try SDKMAN first (common on Mac)
JAVA_17_HOME=""
if [ -d "$HOME/.sdkman/candidates/java" ]; then
    # Check if current SDKMAN Java is 17+
    if [ -n "$JAVA_HOME" ] && [ -f "$JAVA_HOME/bin/java" ]; then
        CURRENT_VERSION=$("$JAVA_HOME/bin/java" -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$CURRENT_VERSION" -ge 17 ] 2>/dev/null; then
            JAVA_17_HOME="$JAVA_HOME"
        fi
    fi
    
    # If not, look for Java 17+ in SDKMAN
    if [ -z "$JAVA_17_HOME" ]; then
        for jdk_dir in ~/.sdkman/candidates/java/*/; do
            if [ -d "$jdk_dir" ] && [ -f "$jdk_dir/bin/java" ]; then
                VERSION=$("$jdk_dir/bin/java" -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
                if [ "$VERSION" -ge 17 ] 2>/dev/null; then
                    JAVA_17_HOME="$jdk_dir"
                    break
                fi
            fi
        done
    fi
fi

# Try macOS java_home utility
if [ -z "$JAVA_17_HOME" ] && [ -f /usr/libexec/java_home ]; then
    JAVA_17_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null)
fi

# Try Homebrew OpenJDK 17
if [ -z "$JAVA_17_HOME" ] && [ -d "/opt/homebrew/opt/openjdk@17" ]; then
    JAVA_17_HOME="/opt/homebrew/opt/openjdk@17"
fi

if [ -z "$JAVA_17_HOME" ] || [ ! -f "$JAVA_17_HOME/bin/java" ]; then
    echo "❌ Error: Java 17 or higher is not found"
    echo "Current JAVA_HOME: ${JAVA_HOME:-not set}"
    echo "Please install Java 17 or set JAVA_HOME to Java 17+"
    exit 1
fi

# Set JAVA_HOME to Java 17+
export JAVA_HOME="$JAVA_17_HOME"
export PATH="$JAVA_HOME/bin:$PATH"

# Verify Java 17+ is being used
JAVA_VERSION=$("$JAVA_HOME/bin/java" -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ] 2>/dev/null; then
    echo "❌ Error: Java 17 or higher is required (found Java $JAVA_VERSION)"
    exit 1
fi

echo "✓ Maven found: $(mvn -version | head -n 1)"
echo "✓ Java $JAVA_VERSION found: $($JAVA_HOME/bin/java -version 2>&1 | head -n 1)"
echo "✓ JAVA_HOME set to: $JAVA_HOME"
echo ""

# Set up project structure
APP_DIR="HS1210_n404_pure_java"
DEMO_DIR="warranty_demo"

echo "Setting up demo project structure..."
mkdir -p "$DEMO_DIR/src/main/java/com/scania/warranty"
mkdir -p "$DEMO_DIR/src/main/resources"
mkdir -p "$DEMO_DIR/src/main/resources/static"

# Copy Java files maintaining package structure
echo "Copying Java source files..."
# Copy domain, service, repository, dto, web directories
for dir in domain service repository dto web; do
    if [ -d "$APP_DIR/$dir" ]; then
        mkdir -p "$DEMO_DIR/src/main/java/com/scania/warranty/$dir"
        cp -r "$APP_DIR/$dir"/* "$DEMO_DIR/src/main/java/com/scania/warranty/$dir/" 2>/dev/null || true
    fi
done
# Copy main application class and config files
if [ -f "$APP_DIR/WarrantyApplication.java" ]; then
    cp "$APP_DIR/WarrantyApplication.java" "$DEMO_DIR/src/main/java/com/scania/warranty/"
fi
# Also check root directory for these files
if [ -f "WarrantyApplication.java" ]; then
    cp "WarrantyApplication.java" "$DEMO_DIR/src/main/java/com/scania/warranty/"
fi

# Copy application.properties
if [ -f "$APP_DIR/application.properties" ]; then
    cp "$APP_DIR/application.properties" "$DEMO_DIR/src/main/resources/"
else
    echo "⚠ Warning: application.properties not found, using defaults"
fi

# Copy pom.xml
if [ -f "pom_warranty.xml" ]; then
    cp "pom_warranty.xml" "$DEMO_DIR/pom.xml"
else
    echo "❌ Error: pom_warranty.xml not found"
    exit 1
fi

# Copy demo HTML if exists
if [ -f "demo.html" ]; then
    cp "demo.html" "$DEMO_DIR/src/main/resources/static/"
fi

echo "✓ Project structure ready"
echo ""

# Build and run
cd "$DEMO_DIR"

echo "Building application with Java 17..."
mvn clean package -DskipTests -Dmaven.compiler.source=17 -Dmaven.compiler.target=17

echo ""
echo "=========================================="
echo "Starting application..."
echo "=========================================="
echo ""
echo "The application will start on: http://localhost:8080"
echo "API Endpoints:"
echo "  - GET  http://localhost:8080/api/claims/search"
echo "  - POST http://localhost:8080/api/claims"
echo "  - H2 Console: http://localhost:8080/h2-console"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""

JAVA_HOME="$JAVA_17_HOME" mvn spring-boot:run
