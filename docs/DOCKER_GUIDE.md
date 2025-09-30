# 静态数据平台 Docker 部署完整指南
# Static Data Platform Docker Deployment Complete Guide

## 📋 目录 / Table of Contents

- [概述 / Overview](#概述--overview)
- [系统要求 / System Requirements](#系统要求--system-requirements)
- [快速开始 / Quick Start](#快速开始--quick-start)
- [服务配置 / Service Configuration](#服务配置--service-configuration)
- [环境配置 / Environment Configuration](#环境配置--environment-configuration)
- [管理命令 / Management Commands](#管理命令--management-commands)
- [开发模式 / Development Mode](#开发模式--development-mode)
- [故障排除 / Troubleshooting](#故障排除--troubleshooting)
- [生产部署 / Production Deployment](#生产部署--production-deployment)
- [监控和健康检查 / Monitoring and Health Checks](#监控和健康检查--monitoring-and-health-checks)
- [备份和恢复 / Backup and Recovery](#备份和恢复--backup-and-recovery)

## 概述 / Overview

本项目支持完全基于Docker的部署方式，所有依赖服务（PostgreSQL、MySQL、Redis、Java应用）都运行在Docker容器中，提供了一致的开发和生产环境。

This project supports fully Docker-based deployment, with all dependent services (PostgreSQL, MySQL, Redis, Java application) running in Docker containers, providing consistent development and production environments.

### 架构图 / Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Docker Host                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │ PostgreSQL  │  │    Redis    │  │    MySQL    │         │
│  │   :5432     │  │   :6379     │  │   :3306     │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
│           │              │              │                  │
│           └──────────────┼──────────────┘                  │
│                          │                                 │
│  ┌─────────────────────────────────────────────────────────┐│
│  │              Java Application                           ││
│  │                :8080                                   ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

## 系统要求 / System Requirements

### 硬件要求 / Hardware Requirements

- **内存**: 至少4GB可用内存
- **磁盘空间**: 至少10GB可用磁盘空间
- **CPU**: 2核心以上推荐

### 软件要求 / Software Requirements

- **Docker Desktop**: 最新版本（推荐4.0+）
- **Docker Compose**: 通常随Docker Desktop一起安装
- **操作系统**: 
  - macOS 10.15+
  - Windows 10/11 (WSL2)
  - Linux (Ubuntu 18.04+, CentOS 7+)

### 网络要求 / Network Requirements

- **端口**: 确保以下端口可用
  - 5432 (PostgreSQL)
  - 6379 (Redis)
  - 3306 (MySQL, 可选)
  - 8080 (Java应用)

## 快速开始 / Quick Start

### 方法1: 自动设置脚本（推荐）

```bash
# 进入backend目录
cd /Users/clampist/Workspace/Java/JavaPro/backend

# 运行自动设置脚本
./setup.sh
```

这个脚本会自动：
- 检查Docker和Docker Compose安装
- 创建所有必要的Docker配置文件
- 启动PostgreSQL、Redis等基础服务
- 验证服务状态

### 方法2: 手动启动

```bash
# 启动所有服务
./start-docker.sh

# 或者使用docker-compose
docker-compose up -d
```

### 方法3: 分步启动

```bash
# 1. 启动基础服务
docker-compose up -d postgres redis

# 2. 等待服务就绪
sleep 10

# 3. 启动Java应用
docker-compose up --build app
```

### 验证部署

```bash
# 检查服务状态
docker-compose ps

# 测试健康检查
curl http://localhost:8080/api/actuator/health

# 查看日志
./logs-docker.sh
```

## 服务配置 / Service Configuration

### 数据库服务 / Database Services

#### PostgreSQL (默认数据库)

**配置信息**:
- **容器名**: `sdp-postgres`
- **端口**: 5432
- **数据库**: `static_data_platform_dev`
- **用户名**: `sdp_user`
- **密码**: `dev_password`
- **数据卷**: `postgres_data`

**连接示例**:
```bash
# 使用psql连接
docker exec -it sdp-postgres psql -U sdp_user -d static_data_platform_dev

# 使用外部客户端连接
psql -h localhost -p 5432 -U sdp_user -d static_data_platform_dev
```

#### MySQL (可选数据库)

**配置信息**:
- **容器名**: `sdp-mysql`
- **端口**: 3306
- **数据库**: `static_data_platform_dev`
- **用户名**: `sdp_user`
- **密码**: `dev_password`
- **Root密码**: `root_password`
- **数据卷**: `mysql_data`

**启动MySQL**:
```bash
# 启动MySQL服务
docker-compose --profile mysql up -d

# 连接MySQL
docker exec -it sdp-mysql mysql -u sdp_user -p static_data_platform_dev
```

### Redis缓存服务 / Redis Cache Service

**配置信息**:
- **容器名**: `sdp-redis`
- **端口**: 6379
- **持久化**: 启用AOF
- **数据卷**: `redis_data`

**连接示例**:
```bash
# 使用redis-cli连接
docker exec -it sdp-redis redis-cli

# 测试连接
docker exec -it sdp-redis redis-cli ping
```

### Java应用服务 / Java Application Service

**配置信息**:
- **容器名**: `sdp-backend`
- **内部端口**: 8080
- **外部端口**: 8080
- **健康检查**: HTTP健康检查端点
- **日志卷**: `./logs`

**访问地址**:
- **API基础地址**: http://localhost:8080/api
- **健康检查**: http://localhost:8080/api/actuator/health
- **API文档**: http://localhost:8080/api/swagger-ui.html

## 环境配置 / Environment Configuration

### 配置文件结构 / Configuration File Structure

```
backend/
├── docker-compose.yml              # Docker Compose主配置
├── Dockerfile                      # Java应用容器配置
├── .env.docker                     # Docker环境变量
├── src/main/resources/
│   └── application-docker.yml      # Spring Boot Docker配置
└── docker/
    ├── mysql/init/01-init.sql      # MySQL初始化脚本
    └── postgres/init/01-init.sql   # PostgreSQL初始化脚本
```

### 环境变量配置 / Environment Variables

#### .env.docker文件
```bash
# 数据库配置
POSTGRES_DB=static_data_platform_dev
POSTGRES_USER=sdp_user
POSTGRES_PASSWORD=dev_password

# MySQL配置
MYSQL_DATABASE=static_data_platform_dev
MYSQL_USER=sdp_user
MYSQL_PASSWORD=dev_password
MYSQL_ROOT_PASSWORD=root_password

# Redis配置
REDIS_PASSWORD=

# 应用配置
SPRING_PROFILES_ACTIVE=docker
SERVER_PORT=8080
```

### 数据持久化 / Data Persistence

所有数据都存储在Docker volumes中：

```yaml
volumes:
  postgres_data:    # PostgreSQL数据
  mysql_data:       # MySQL数据
  redis_data:       # Redis数据
  ./logs:           # 应用日志
```

**数据位置**:
- **PostgreSQL**: `/var/lib/postgresql/data`
- **MySQL**: `/var/lib/mysql`
- **Redis**: `/data`
- **应用日志**: `./logs/application.log`

## 管理命令 / Management Commands

### 服务管理 / Service Management

```bash
# 查看服务状态
docker-compose ps

# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 重启特定服务
docker-compose restart [service_name]

# 停止并删除所有数据
docker-compose down -v

# 查看服务日志
docker-compose logs [service_name]

# 实时查看日志
docker-compose logs -f [service_name]
```

### 使用管理脚本 / Using Management Scripts

```bash
# 启动所有服务
./start-docker.sh

# 停止所有服务
./stop-docker.sh

# 查看日志
./logs-docker.sh

# 重新构建并启动
./rebuild-docker.sh
```

### 数据库操作 / Database Operations

```bash
# 连接PostgreSQL
docker exec -it sdp-postgres psql -U sdp_user -d static_data_platform_dev

# 连接MySQL
docker exec -it sdp-mysql mysql -u sdp_user -p static_data_platform_dev

# 连接Redis
docker exec -it sdp-redis redis-cli

# 查看数据库大小
docker exec sdp-postgres psql -U sdp_user -d static_data_platform_dev -c "SELECT pg_size_pretty(pg_database_size('static_data_platform_dev'));"
```

### 应用管理 / Application Management

```bash
# 查看应用日志
docker logs sdp-backend

# 进入应用容器
docker exec -it sdp-backend bash

# 重启应用
docker-compose restart app

# 重新构建应用
docker-compose build app
docker-compose up -d app
```

## 开发模式 / Development Mode

### 热重载开发 / Hot Reload Development

对于开发环境，推荐以下配置：

#### 1. 只启动数据库和Redis服务

```bash
# 启动基础服务
docker-compose up -d postgres redis

# 在本地运行Java应用
./start-backend.sh
```

#### 2. 代码修改后的处理

```bash
# 重新构建应用
docker-compose build app
docker-compose up -d app

# 或者使用重建脚本
./rebuild-docker.sh
```

### 开发环境配置 / Development Environment Configuration

#### application-docker.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/static_data_platform_dev
    username: sdp_user
    password: dev_password
  
  redis:
    host: redis
    port: 6379
    timeout: 2000ms

server:
  port: 8080

logging:
  level:
    com.staticdata.platform: DEBUG
    org.springframework.security: DEBUG
```

### 调试配置 / Debug Configuration

```bash
# 启动调试模式
docker-compose -f docker-compose.yml -f docker-compose.debug.yml up

# 或者修改Dockerfile添加调试端口
EXPOSE 8080 5005
```

## 故障排除 / Troubleshooting

### 常见问题 / Common Issues

#### 1. 端口冲突 / Port Conflicts

**症状**: `bind: address already in use`

**解决方案**:
```bash
# 检查端口占用
lsof -i :8080
lsof -i :5432
lsof -i :6379

# 修改docker-compose.yml中的端口映射
ports:
  - "8080:8080"  # 使用8080端口
```

#### 2. 内存不足 / Insufficient Memory

**症状**: 容器启动失败或运行缓慢

**解决方案**:
- 增加Docker Desktop的内存限制（推荐8GB+）
- 关闭不必要的容器
- 检查系统资源使用情况

#### 3. 数据库连接失败 / Database Connection Failed

**症状**: 应用启动失败，数据库连接错误

**解决方案**:
```bash
# 检查数据库容器状态
docker-compose ps postgres

# 检查网络连接
docker network ls
docker network inspect backend_default

# 验证数据库连接
docker exec sdp-postgres pg_isready -U sdp_user
```

#### 4. 应用启动失败 / Application Startup Failed

**症状**: Java应用容器启动失败

**解决方案**:
```bash
# 查看应用日志
docker logs sdp-backend

# 检查编译错误
docker-compose build app

# 验证配置文件
docker exec sdp-backend cat /app/application-docker.yml
```

### 日志查看 / Log Viewing

```bash
# 查看所有服务日志
docker-compose logs

# 查看特定服务日志
docker-compose logs app
docker-compose logs postgres
docker-compose logs redis

# 实时查看日志
docker-compose logs -f app

# 查看应用日志文件
tail -f logs/application.log
```

### 重置环境 / Reset Environment

```bash
# 停止所有服务并删除数据
docker-compose down -v

# 删除所有镜像
docker-compose down --rmi all

# 清理未使用的资源
docker system prune -a

# 重新运行设置
./setup.sh
```

## 生产部署 / Production Deployment

### 生产环境配置 / Production Environment Configuration

#### 1. 安全配置 / Security Configuration

```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  app:
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - JWT_SECRET=${JWT_SECRET}
      - DATABASE_PASSWORD=${DATABASE_PASSWORD}
    deploy:
      resources:
        limits:
          memory: 2G
          cpus: '1.0'
```

#### 2. 环境变量 / Environment Variables

```bash
# .env.prod
POSTGRES_PASSWORD=your_secure_password
JWT_SECRET=your_jwt_secret_key
CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

#### 3. 资源限制 / Resource Limits

```yaml
services:
  app:
    deploy:
      resources:
        limits:
          memory: 2G
          cpus: '1.0'
        reservations:
          memory: 1G
          cpus: '0.5'
```

### 生产部署步骤 / Production Deployment Steps

#### 1. 准备生产环境

```bash
# 创建生产环境配置
cp .env.docker .env.prod
# 修改生产环境变量

# 创建生产Docker Compose文件
cp docker-compose.yml docker-compose.prod.yml
# 修改生产配置
```

#### 2. 部署到生产服务器

```bash
# 上传代码到服务器
scp -r backend/ user@server:/opt/static-data-platform/

# 在服务器上启动
ssh user@server
cd /opt/static-data-platform/backend
docker-compose -f docker-compose.prod.yml up -d
```

#### 3. 配置反向代理

```nginx
# nginx.conf
server {
    listen 80;
    server_name yourdomain.com;
    
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 生产环境建议 / Production Recommendations

1. **使用外部数据库服务** (如AWS RDS, Google Cloud SQL)
2. **配置SSL/TLS证书**
3. **设置资源限制和监控**
4. **配置日志轮转**
5. **使用Docker Swarm或Kubernetes进行编排**
6. **设置自动备份**
7. **配置健康检查和自动重启**

## 监控和健康检查 / Monitoring and Health Checks

### 健康检查配置 / Health Check Configuration

所有服务都配置了健康检查：

```yaml
services:
  postgres:
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U sdp_user"]
      interval: 30s
      timeout: 10s
      retries: 3

  redis:
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3

  app:
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

### 查看健康状态 / Check Health Status

```bash
# 查看所有服务健康状态
docker-compose ps

# 查看特定服务健康状态
docker inspect sdp-postgres --format='{{.State.Health.Status}}'
docker inspect sdp-redis --format='{{.State.Health.Status}}'
docker inspect sdp-backend --format='{{.State.Health.Status}}'
```

### 监控指标 / Monitoring Metrics

#### 应用指标
```bash
# 查看应用指标
curl http://localhost:8080/api/actuator/metrics

# 查看JVM指标
curl http://localhost:8080/api/actuator/metrics/jvm.memory.used

# 查看HTTP请求指标
curl http://localhost:8080/api/actuator/metrics/http.server.requests
```

#### 容器指标
```bash
# 查看容器资源使用
docker stats

# 查看特定容器资源使用
docker stats sdp-backend
```

## 备份和恢复 / Backup and Recovery

### 数据库备份 / Database Backup

#### PostgreSQL备份
```bash
# 创建备份
docker exec sdp-postgres pg_dump -U sdp_user static_data_platform_dev > backup_$(date +%Y%m%d_%H%M%S).sql

# 压缩备份
docker exec sdp-postgres pg_dump -U sdp_user static_data_platform_dev | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz
```

#### MySQL备份
```bash
# 创建备份
docker exec sdp-mysql mysqldump -u sdp_user -p static_data_platform_dev > backup_$(date +%Y%m%d_%H%M%S).sql
```

#### Redis备份
```bash
# 创建Redis备份
docker exec sdp-redis redis-cli BGSAVE
docker cp sdp-redis:/data/dump.rdb ./redis_backup_$(date +%Y%m%d_%H%M%S).rdb
```

### 数据恢复 / Data Recovery

#### PostgreSQL恢复
```bash
# 恢复数据库
docker exec -i sdp-postgres psql -U sdp_user static_data_platform_dev < backup_20240101_120000.sql

# 从压缩备份恢复
gunzip -c backup_20240101_120000.sql.gz | docker exec -i sdp-postgres psql -U sdp_user static_data_platform_dev
```

#### MySQL恢复
```bash
# 恢复数据库
docker exec -i sdp-mysql mysql -u sdp_user -p static_data_platform_dev < backup_20240101_120000.sql
```

#### Redis恢复
```bash
# 停止Redis服务
docker-compose stop redis

# 复制备份文件
docker cp redis_backup_20240101_120000.rdb sdp-redis:/data/dump.rdb

# 启动Redis服务
docker-compose start redis
```

### 自动备份脚本 / Automated Backup Script

```bash
#!/bin/bash
# backup.sh

BACKUP_DIR="/opt/backups"
DATE=$(date +%Y%m%d_%H%M%S)

# 创建备份目录
mkdir -p $BACKUP_DIR

# PostgreSQL备份
docker exec sdp-postgres pg_dump -U sdp_user static_data_platform_dev | gzip > $BACKUP_DIR/postgres_backup_$DATE.sql.gz

# Redis备份
docker exec sdp-redis redis-cli BGSAVE
docker cp sdp-redis:/data/dump.rdb $BACKUP_DIR/redis_backup_$DATE.rdb

# 清理旧备份（保留7天）
find $BACKUP_DIR -name "*.gz" -mtime +7 -delete
find $BACKUP_DIR -name "*.rdb" -mtime +7 -delete

echo "Backup completed: $DATE"
```

### 设置定时备份 / Schedule Automated Backups

```bash
# 添加到crontab
crontab -e

# 每天凌晨2点执行备份
0 2 * * * /opt/static-data-platform/backend/backup.sh
```

## 总结 / Summary

本指南提供了静态数据平台Docker部署的完整解决方案，包括：

### ✅ 核心功能
- **一键部署**: 自动设置脚本快速启动所有服务
- **多数据库支持**: PostgreSQL（默认）和MySQL（可选）
- **缓存服务**: Redis提供高性能缓存
- **健康检查**: 所有服务都有健康检查机制
- **数据持久化**: 使用Docker volumes确保数据安全

### ✅ 开发支持
- **热重载**: 支持开发模式下的代码热重载
- **调试支持**: 完整的调试配置和日志查看
- **环境隔离**: 开发、测试、生产环境完全隔离

### ✅ 生产就绪
- **安全配置**: 生产环境安全最佳实践
- **资源管理**: 内存和CPU限制配置
- **监控告警**: 健康检查和指标监控
- **备份恢复**: 完整的备份和恢复策略

### ✅ 运维友好
- **管理脚本**: 简化的服务管理命令
- **故障排除**: 详细的问题诊断和解决方案
- **文档完整**: 从安装到维护的完整文档

通过本指南，您可以：
1. 快速部署完整的开发环境
2. 轻松管理Docker容器和服务
3. 解决常见的部署和运行问题
4. 配置生产环境的最佳实践
5. 实现自动化的备份和监控

---

**最后更新**: 2024-01-01  
**版本**: 1.0.0  
**状态**: 生产就绪 🚀
