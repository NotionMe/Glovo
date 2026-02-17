#!/usr/bin/env bash
set -e

APP_NAME="Delivery Dispatch Core"
DEFAULT_PORT=8080

echo "=== $APP_NAME ==="
echo ""

# Check Java version
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed. Java 21+ is required."
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VER" -lt 21 ] 2>/dev/null; then
    echo "ERROR: Java 21+ is required. Found: Java $JAVA_VER"
    exit 1
fi

echo "Java version: $(java -version 2>&1 | head -1)"

# Parse arguments
ACTION="${1:-run}"

case "$ACTION" in
    run)
        echo "Starting application on port $DEFAULT_PORT..."
        echo "Swagger UI: http://localhost:$DEFAULT_PORT/swagger-ui.html"
        echo ""
        ./mvnw spring-boot:run -q
        ;;
    build)
        echo "Building project..."
        ./mvnw clean package -DskipTests
        echo ""
        echo "Build complete: target/delivery-dispatch-core-1.0.0-SNAPSHOT.jar"
        ;;
    test)
        echo "Running tests..."
        ./mvnw test
        ;;
    jar)
        JAR_FILE="target/delivery-dispatch-core-1.0.0-SNAPSHOT.jar"
        if [ ! -f "$JAR_FILE" ]; then
            echo "JAR not found. Building first..."
            ./mvnw clean package -DskipTests
        fi
        echo "Starting from JAR on port $DEFAULT_PORT..."
        echo "Swagger UI: http://localhost:$DEFAULT_PORT/swagger-ui.html"
        echo ""
        java -jar "$JAR_FILE"
        ;;
    *)
        echo "Usage: ./run.sh [run|build|test|jar]"
        echo ""
        echo "  run    - Start the application with Maven (default)"
        echo "  build  - Build the JAR without running tests"
        echo "  test   - Run all tests"
        echo "  jar    - Build (if needed) and run the JAR directly"
        exit 1
        ;;
esac
