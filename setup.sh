#!/bin/bash

# Static Data Platform - Docker Setup Script
# This script sets up all dependencies using Docker containers

set -e  # Exit on any error

echo "🐳 Static Data Platform - Docker Setup"
echo "======================================"
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

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check Docker installation
check_docker() {
    if command_exists docker; then
        DOCKER_VERSION=$(docker --version)
        print_success "Docker is installed: $DOCKER_VERSION"
        
        # Check if Docker daemon is running
        if docker info &> /dev/null; then
            print_success "Docker daemon is running"
            return 0
        else
            print_error "Docker daemon is not running. Please start Docker Desktop"
            return 1
        fi
    else
        print_error "Docker is not installed. Please install Docker Desktop"
        return 1
    fi
}

# Function to check Docker Compose
check_docker_compose() {
    if command_exists docker-compose; then
        COMPOSE_VERSION=$(docker-compose --version)
        print_success "Docker Compose is installed: $COMPOSE_VERSION"
        return 0
    elif docker compose version &> /dev/null; then
        COMPOSE_VERSION=$(docker compose version)
        print_success "Docker Compose (plugin) is available: $COMPOSE_VERSION"
        return 0
    else
        print_error "Docker Compose is not installed"
        return 1
    fi
}

# Function to create Docker Compose file
create_docker_compose() {
    print_status "Creating docker-compose.yml..."
    
    cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  # PostgreSQL Database
  postgres:
    image: postgres:15-alpine
    container_name: sdp-postgres
    environment:
      POSTGRES_DB: static_data_platform_dev
      POSTGRES_USER: sdp_user
      POSTGRES_PASSWORD: dev_password
      POSTGRES_INITDB_ARGS: "--encoding=UTF-8"
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/postgres/init:/docker-entrypoint-initdb.d
    networks:
      - sdp-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U sdp_user -d static_data_platform_dev"]
      interval: 10s
      timeout: 5s
      retries: 5

  # MySQL Database (Alternative)
  mysql:
    image: mysql:8.0
    container_name: sdp-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: static_data_platform_dev
      MYSQL_USER: sdp_user
      MYSQL_PASSWORD: dev_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./docker/mysql/init:/docker-entrypoint-initdb.d
    networks:
      - sdp-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    profiles:
      - mysql

  # Redis Cache
  redis:
    image: redis:7-alpine
    container_name: sdp-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - sdp-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    command: redis-server --appendonly yes

  # Java Application
  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: sdp-backend
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/static_data_platform_dev
      SPRING_DATASOURCE_USERNAME: sdp_user
      SPRING_DATASOURCE_PASSWORD: dev_password
      SPRING_DATA_REDIS_HOST: redis
      SPRING_DATA_REDIS_PORT: 6379
      APP_JWT_SECRET: devSecretKey1234567890123456789012345678901234567890123456789012345678901234567890
      APP_JWT_EXPIRATION: 86400000
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - sdp-network
    volumes:
      - ./logs:/app/logs
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  postgres_data:
  mysql_data:
  redis_data:

networks:
  sdp-network:
    driver: bridge
EOF

    print_success "docker-compose.yml created"
}

# Function to create Dockerfile
create_dockerfile() {
    print_status "Creating Dockerfile..."
    
    cat > Dockerfile << 'EOF'
# Multi-stage build for Spring Boot application
FROM maven:3.9 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create app user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# Switch to app user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
EOF

    print_success "Dockerfile created"
}

# Function to create Docker directories and init scripts
create_docker_structure() {
    print_status "Creating Docker directory structure..."
    
    # Create directories
    mkdir -p docker/postgres/init
    mkdir -p docker/mysql/init
    mkdir -p logs
    
    # Create PostgreSQL init script
    cat > docker/postgres/init/01-init.sql << 'EOF'
-- Initialize PostgreSQL database
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Create additional schemas if needed
-- CREATE SCHEMA IF NOT EXISTS audit;
-- CREATE SCHEMA IF NOT EXISTS reporting;
EOF

    # Create MySQL init script
    cat > docker/mysql/init/01-init.sql << 'EOF'
-- Initialize MySQL database
-- Set charset and collation
ALTER DATABASE static_data_platform_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create additional schemas if needed
-- CREATE SCHEMA IF NOT EXISTS audit;
-- CREATE SCHEMA IF NOT EXISTS reporting;
EOF

    print_success "Docker directory structure created"
}

# Function to create Docker environment configuration
create_docker_env() {
    print_status "Creating Docker environment configuration..."
    
    cat > .env.docker << 'EOF'
# Docker Environment Configuration
# Generated by setup.sh

# Database Configuration (PostgreSQL)
DB_HOST=postgres
DB_PORT=5432
DB_NAME=static_data_platform_dev
DB_USER=sdp_user
DB_PASSWORD=dev_password

# Database Configuration (MySQL - Alternative)
MYSQL_HOST=mysql
MYSQL_PORT=3306
MYSQL_DATABASE=static_data_platform_dev
MYSQL_USER=sdp_user
MYSQL_PASSWORD=dev_password
MYSQL_ROOT_PASSWORD=root_password

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379

# JWT Configuration
JWT_SECRET=devSecretKey1234567890123456789012345678901234567890123456789012345678901234567890
JWT_EXPIRATION=86400000

# Application Configuration
APP_PORT=8080
APP_CONTEXT_PATH=/api
APP_PROFILE=docker

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# Docker Configuration
COMPOSE_PROJECT_NAME=sdp
EOF

    print_success "Docker environment file created: .env.docker"
}

# Function to create Docker application properties
create_docker_properties() {
    print_status "Creating Docker application properties..."
    
    cat > src/main/resources/application-docker.yml << 'EOF'
# Docker Environment Configuration
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/static_data_platform_dev
    username: sdp_user
    password: dev_password
    driver-class-name: org.postgresql.Driver
  
  # JPA Docker Settings
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: false
        dialect: org.hibernate.dialect.PostgreSQLDialect
  
  # Redis Docker Settings
  data:
    redis:
      host: redis
      port: 6379
      timeout: 60000
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0

# Logging Docker Settings
logging:
  level:
    com.staticdata.platform: INFO
    org.springframework.web: INFO
    org.springframework.security: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /app/logs/application.log

# Security Docker Settings
app:
  jwt:
    secret: ${APP_JWT_SECRET:devSecretKey1234567890123456789012345678901234567890123456789012345678901234567890}
    expiration: ${APP_JWT_EXPIRATION:86400000}
  
  # CORS Docker Settings
  security:
    cors:
      allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173}
  
  # Docker Features
  audit:
    enabled: true
EOF

    print_success "Docker application properties created"
}

# Function to start Docker services
start_docker_services() {
    print_status "Starting Docker services..."
    
    # Load environment variables
    if [ -f ".env.docker" ]; then
        export $(cat .env.docker | grep -v '^#' | xargs)
    fi
    
    # Start services
    if command_exists docker-compose; then
        docker-compose --env-file .env.docker up -d
    else
        docker compose --env-file .env.docker up -d
    fi
    
    print_success "Docker services started"
}

# Function to wait for services to be ready
wait_for_services() {
    print_status "Waiting for services to be ready..."
    
    # Wait for PostgreSQL
    print_status "Waiting for PostgreSQL..."
    timeout=60
    while [ $timeout -gt 0 ]; do
        if docker exec sdp-postgres pg_isready -U sdp_user -d static_data_platform_dev &> /dev/null; then
            print_success "PostgreSQL is ready"
            break
        fi
        sleep 2
        timeout=$((timeout - 2))
    done
    
    if [ $timeout -le 0 ]; then
        print_error "PostgreSQL failed to start within 60 seconds"
        return 1
    fi
    
    # Wait for Redis
    print_status "Waiting for Redis..."
    timeout=30
    while [ $timeout -gt 0 ]; do
        if docker exec sdp-redis redis-cli ping &> /dev/null; then
            print_success "Redis is ready"
            break
        fi
        sleep 2
        timeout=$((timeout - 2))
    done
    
    if [ $timeout -le 0 ]; then
        print_error "Redis failed to start within 30 seconds"
        return 1
    fi
    
    print_success "All services are ready"
}

# Function to verify Docker setup
verify_docker_setup() {
    print_status "Verifying Docker setup..."
    
    # Check if containers are running
    if docker ps | grep -q sdp-postgres; then
        print_success "PostgreSQL container is running"
    else
        print_error "PostgreSQL container is not running"
        return 1
    fi
    
    if docker ps | grep -q sdp-redis; then
        print_success "Redis container is running"
    else
        print_error "Redis container is not running"
        return 1
    fi
    
    # Test database connection
    if docker exec sdp-postgres psql -U sdp_user -d static_data_platform_dev -c "SELECT 1;" &> /dev/null; then
        print_success "Database connection test passed"
    else
        print_error "Database connection test failed"
        return 1
    fi
    
    # Test Redis connection
    if docker exec sdp-redis redis-cli ping | grep -q PONG; then
        print_success "Redis connection test passed"
    else
        print_error "Redis connection test failed"
        return 1
    fi
    
    print_success "All Docker verifications passed! 🎉"
    return 0
}

# Function to create Docker management scripts
create_docker_scripts() {
    print_status "Creating Docker management scripts..."
    
    # Create start script
    cat > start-docker.sh << 'EOF'
#!/bin/bash
echo "🐳 Starting Static Data Platform with Docker..."
docker-compose --env-file .env.docker up -d
echo "✅ Services started. Check status with: docker-compose ps"
EOF

    # Create stop script
    cat > stop-docker.sh << 'EOF'
#!/bin/bash
echo "🛑 Stopping Static Data Platform Docker services..."
docker-compose down
echo "✅ Services stopped"
EOF

    # Create logs script
    cat > logs-docker.sh << 'EOF'
#!/bin/bash
echo "📋 Showing Docker service logs..."
docker-compose logs -f
EOF

    # Create rebuild script
    cat > rebuild-docker.sh << 'EOF'
#!/bin/bash
echo "🔨 Rebuilding and restarting Docker services..."
docker-compose down
docker-compose build --no-cache
docker-compose --env-file .env.docker up -d
echo "✅ Services rebuilt and restarted"
EOF

    # Make scripts executable
    chmod +x start-docker.sh stop-docker.sh logs-docker.sh rebuild-docker.sh
    
    print_success "Docker management scripts created"
}

# Main setup function
main() {
    print_status "Starting Docker setup process..."
    echo ""
    
    # Check if we're in the right directory
    if [ ! -f "pom.xml" ]; then
        print_error "pom.xml not found. Please run this script from the backend directory"
        exit 1
    fi
    
    # Check Docker installation
    print_status "Checking Docker installation..."
    if ! check_docker; then
        exit 1
    fi
    echo ""
    
    # Check Docker Compose
    print_status "Checking Docker Compose..."
    if ! check_docker_compose; then
        exit 1
    fi
    echo ""
    
    # Create Docker files
    print_status "Creating Docker configuration files..."
    create_docker_compose
    create_dockerfile
    create_docker_structure
    create_docker_env
    create_docker_properties
    create_docker_scripts
    echo ""
    
    # Start Docker services
    print_status "Starting Docker services..."
    start_docker_services
    echo ""
    
    # Wait for services
    wait_for_services
    echo ""
    
    # Verify setup
    print_status "Verifying Docker setup..."
    if verify_docker_setup; then
        echo ""
        print_success "🎉 Docker setup completed successfully!"
        echo ""
        echo "Services running:"
        echo "  📊 PostgreSQL: localhost:5432"
        echo "  🔴 Redis: localhost:6379"
        echo "  ☕ Java App: localhost:8080 (will start after build)"
        echo ""
        echo "Management commands:"
        echo "  ./start-docker.sh    - Start all services"
        echo "  ./stop-docker.sh     - Stop all services"
        echo "  ./logs-docker.sh     - View service logs"
        echo "  ./rebuild-docker.sh  - Rebuild and restart"
        echo ""
        echo "Next steps:"
        echo "1. Build and start the Java application:"
        echo "   docker-compose up --build app"
        echo "2. The API will be available at: http://localhost:8080/api"
        echo "3. API documentation: http://localhost:8080/api/swagger-ui.html"
        echo "4. Health check: http://localhost:8080/api/actuator/health"
        echo ""
        echo "Database credentials:"
        echo "  Host: localhost:5432"
        echo "  Database: static_data_platform_dev"
        echo "  Username: sdp_user"
        echo "  Password: dev_password"
        echo ""
        echo "To use MySQL instead of PostgreSQL:"
        echo "  docker-compose --profile mysql up -d"
        echo ""
    else
        print_error "Docker setup verification failed. Please check the errors above."
        exit 1
    fi
}

# Run main function
main "$@"
