#!/bin/bash

# Backend Startup Script for Static Data Platform
# This script starts the Spring Boot backend application with environment variable support

echo "🚀 Starting Static Data Platform Backend..."
echo "============================================="

# Set script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Load environment variables from .env.local if exists
ENV_FILE=".env.local"
if [ -f "$ENV_FILE" ]; then
    echo "📋 Loading environment variables from $ENV_FILE..."
    # Export environment variables (ignore comments and empty lines)
    export $(grep -v '^#' "$ENV_FILE" | grep -v '^$' | xargs)
    echo "✅ Environment variables loaded"
else
    echo "⚠️  No .env.local file found. Using default configuration."
    echo "💡 Tip: Run './scripts/generate-env.sh' to create environment configuration"
fi

# Load shell environment variables
if [ -f ~/.zshrc ]; then
    source ~/.zshrc
fi

# Check Java version
echo "📋 Checking Java environment..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n1 | awk -F '"' '{print $2}')
    echo "✅ Java version: $JAVA_VERSION"
else
    echo "❌ Java not found. Please install Java 17+ and try again."
    exit 1
fi

# Check Maven
echo "📋 Checking Maven..."
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version 2>&1 | head -n1)
    echo "✅ $MVN_VERSION"
else
    echo "❌ Maven not found. Please install Maven and try again."
    exit 1
fi

# Check PostgreSQL connection
echo "📋 Checking PostgreSQL connection..."
DB_HOST=${SPRING_DATASOURCE_URL#*://}
DB_HOST=${DB_HOST%%:*}
DB_PORT=${SPRING_DATASOURCE_URL#*:}
DB_PORT=${DB_PORT%%/*}

if [ -n "$DB_HOST" ] && [ -n "$DB_PORT" ]; then
    if nc -z "$DB_HOST" "$DB_PORT" 2>/dev/null; then
        echo "✅ PostgreSQL is running ($DB_HOST:$DB_PORT)"
    else
        echo "⚠️  PostgreSQL connection failed ($DB_HOST:$DB_PORT)"
        echo "   Please ensure PostgreSQL is running"
        echo "   💡 Tip: Use './start-docker.sh' to start PostgreSQL container"
    fi
else
    # Fallback to default check
    if nc -z localhost 5432 2>/dev/null; then
        echo "✅ PostgreSQL is running (localhost:5432)"
    else
        echo "⚠️  PostgreSQL connection failed (localhost:5432)"
        echo "   Please start PostgreSQL service"
        echo "   💡 Tip: Use './start-docker.sh' to start PostgreSQL container"
    fi
fi

# Check Redis connection
echo "📋 Checking Redis connection..."
REDIS_HOST=${SPRING_DATA_REDIS_HOST:-localhost}
REDIS_PORT=${SPRING_DATA_REDIS_PORT:-6379}

if nc -z "$REDIS_HOST" "$REDIS_PORT" 2>/dev/null; then
    echo "✅ Redis is running ($REDIS_HOST:$REDIS_PORT)"
else
    echo "⚠️  Redis connection failed ($REDIS_HOST:$REDIS_PORT)"
    echo "   Please ensure Redis is running"
    echo "   💡 Tip: Use './start-docker.sh' to start Redis container"
fi

# Clean and compile
echo "🔧 Cleaning and compiling project..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed. Please check the error messages above."
    exit 1
fi

echo "✅ Compilation successful"

# Start the application
echo "🚀 Starting Spring Boot application..."
echo "Backend will be available at: http://localhost:8080"
echo "Health check: http://localhost:8080/api/actuator/health"
echo "API Documentation: http://localhost:8080/api/swagger-ui.html"
echo ""
echo "Press Ctrl+C to stop the server"
echo "============================================="

# Run with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

echo ""
echo "👋 Backend stopped."