#!/bin/bash

# Local Unit Tests Runner Script
# This script runs unit tests locally with Docker services

set -e  # Exit on any error

echo "🧪 Static Data Platform - Local Unit Tests"
echo "==========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    print_error "pom.xml not found. Please run this script from the backend directory"
    exit 1
fi

# Check if Java is available
if ! command -v java &> /dev/null; then
    print_error "Java is not installed. Please install Java 17"
    exit 1
fi

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed. Please install Maven"
    exit 1
fi

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker"
    exit 1
fi

print_success "All prerequisites are available"

# Start test services
print_status "Starting test services..."
docker-compose -f docker-compose.test.yml up -d postgres-test redis-test

# Wait for services to be ready
print_status "Waiting for services to be ready..."
timeout=60
while [ $timeout -gt 0 ]; do
    if docker exec sdp-postgres-test pg_isready -U sdp_user -d static_data_platform_test &> /dev/null; then
        print_success "PostgreSQL is ready"
        break
    fi
    sleep 2
    timeout=$((timeout - 2))
done

if [ $timeout -le 0 ]; then
    print_error "PostgreSQL failed to start within 60 seconds"
    docker-compose -f docker-compose.test.yml down -v
    exit 1
fi

timeout=30
while [ $timeout -gt 0 ]; do
    if docker exec sdp-redis-test redis-cli ping &> /dev/null; then
        print_success "Redis is ready"
        break
    fi
    sleep 2
    timeout=$((timeout - 2))
done

if [ $timeout -le 0 ]; then
    print_error "Redis failed to start within 30 seconds"
    docker-compose -f docker-compose.test.yml down -v
    exit 1
fi

# Run tests
print_status "Running unit tests..."
if mvn clean test -Dspring.profiles.active=test \
    -Dspring.datasource.url=jdbc:postgresql://localhost:5433/static_data_platform_test \
    -Dspring.datasource.username=sdp_user \
    -Dspring.datasource.password=test_password \
    -Dspring.data.redis.host=localhost \
    -Dspring.data.redis.port=6380; then
    print_success "All tests passed! 🎉"
else
    print_error "Some tests failed! ❌"
    docker-compose -f docker-compose.test.yml down -v
    exit 1
fi

# Generate coverage report
print_status "Generating coverage report..."
mvn jacoco:report

# Clean up
print_status "Cleaning up test services..."
docker-compose -f docker-compose.test.yml down -v

echo ""
print_success "Local unit tests completed!"
echo ""
echo "Test results:"
echo "  📊 Surefire reports: ./target/surefire-reports/"
echo "  📈 Coverage report: ./target/site/jacoco/index.html"
echo ""
echo "To view coverage report:"
echo "  open ./target/site/jacoco/index.html"
echo ""
