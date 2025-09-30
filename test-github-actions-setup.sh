#!/bin/bash

# Test script to verify GitHub Actions setup locally
# This simulates the GitHub Actions environment

set -e

echo "🧪 Testing GitHub Actions Setup Locally"
echo "========================================"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

echo "✅ Docker is running"

# Start PostgreSQL and Redis services (simulating GitHub Actions Services)
echo "🐘 Starting PostgreSQL service..."
docker run -d \
  --name test-postgres \
  -e POSTGRES_DB=static_data_platform_test \
  -e POSTGRES_USER=sdp_user \
  -e POSTGRES_PASSWORD=test_password \
  -p 5432:5432 \
  postgres:15-alpine

echo "🔴 Starting Redis service..."
docker run -d \
  --name test-redis \
  -p 6379:6379 \
  redis:7-alpine

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check PostgreSQL
until docker exec test-postgres pg_isready -U sdp_user -d static_data_platform_test; do
  echo "Waiting for PostgreSQL..."
  sleep 2
done

# Check Redis
until docker exec test-redis redis-cli ping; do
  echo "Waiting for Redis..."
  sleep 2
done

echo "✅ Services are ready"

# Initialize database (simulating GitHub Actions step)
echo "🔧 Initializing test database..."
docker exec test-postgres psql -U sdp_user -d static_data_platform_test -c "
CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";
CREATE EXTENSION IF NOT EXISTS \"pg_trgm\";
"

echo "✅ Database initialized"

# Set environment variables (simulating GitHub Actions env)
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/static_data_platform_test
export SPRING_DATASOURCE_USERNAME=sdp_user
export SPRING_DATASOURCE_PASSWORD=test_password
export SPRING_DATA_REDIS_HOST=localhost
export SPRING_DATA_REDIS_PORT=6379
export APP_JWT_SECRET=testSecretKey1234567890123456789012345678901234567890123456789012345678901234567890

# Run tests (simulating GitHub Actions test step)
echo "🧪 Running tests..."
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn clean test -Dspring.profiles.active=test

echo "✅ Tests completed successfully"

# Cleanup
echo "🧹 Cleaning up..."
docker stop test-postgres test-redis
docker rm test-postgres test-redis

echo "🎉 GitHub Actions setup test completed successfully!"
echo ""
echo "📋 Summary:"
echo "  ✅ PostgreSQL service configured correctly"
echo "  ✅ Redis service configured correctly"
echo "  ✅ Database initialization working"
echo "  ✅ Tests running successfully"
echo "  ✅ Environment variables set correctly"
echo ""
echo "🚀 Your GitHub Actions CI/CD pipeline should work correctly!"
