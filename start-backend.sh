#!/bin/bash

# Backend Startup Script for Static Data Platform
# This script starts the Spring Boot backend application

echo "🚀 Starting Static Data Platform Backend..."
echo "============================================="

# Set script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Load environment variables
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
if pg_isready -h localhost -p 5432 &> /dev/null; then
    echo "✅ PostgreSQL is running"
else
    echo "⚠️  PostgreSQL might not be running. Starting services..."
    brew services start postgresql@15 || echo "Could not start PostgreSQL automatically"
fi

# Check Redis connection
echo "📋 Checking Redis connection..."
if redis-cli ping &> /dev/null; then
    echo "✅ Redis is running"
else
    echo "⚠️  Redis might not be running. Starting services..."
    brew services start redis || echo "Could not start Redis automatically"
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