# 本地开发环境配置指南

## 概述

本文档说明如何配置本地开发环境，使用环境变量管理敏感信息，确保开发过程的安全性和灵活性。

## 快速开始

### 1. 自动生成环境配置

```bash
# 生成包含安全密钥的环境变量文件
./scripts/generate-env.sh
```

### 2. 启动开发环境

```bash
# 使用环境变量启动应用
./start-backend.sh
```

## 环境变量配置

### 配置文件说明

- `.env.local` - 本地开发环境变量（不提交到Git）
- `env.local.example` - 环境变量配置示例（可提交到Git）
- `scripts/generate-env.sh` - 自动生成环境变量脚本
- `start-backend.sh` - 开发环境启动脚本

### 环境变量类型

#### 应用配置
```bash
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080
```

#### 数据库配置
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/static_data_platform_dev
SPRING_DATASOURCE_USERNAME=sdp_user
SPRING_DATASOURCE_PASSWORD=your_secure_password
```

#### JWT配置
```bash
APP_JWT_SECRET=your_very_long_and_secure_jwt_secret_key_minimum_256_bits
APP_JWT_EXPIRATION=86400000
```

#### Redis配置
```bash
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
```

#### CORS配置
```bash
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

## 手动配置步骤

### 步骤1: 创建环境变量文件

```bash
# 复制示例文件
cp env.local.example .env.local

# 编辑配置文件
vim .env.local
```

### 步骤2: 配置数据库

确保PostgreSQL服务正在运行：

```bash
# 使用Docker启动数据库
docker run -d \
  --name postgres-dev \
  -e POSTGRES_DB=static_data_platform_dev \
  -e POSTGRES_USER=sdp_user \
  -e POSTGRES_PASSWORD=your_password \
  -p 5432:5432 \
  postgres:15

# 或使用本地安装的PostgreSQL
brew services start postgresql  # macOS
sudo systemctl start postgresql  # Linux
```

### 步骤3: 配置Redis

```bash
# 使用Docker启动Redis
docker run -d \
  --name redis-dev \
  -p 6379:6379 \
  redis:7-alpine

# 或使用本地安装的Redis
brew services start redis  # macOS
sudo systemctl start redis  # Linux
```

### 步骤4: 生成安全密钥

```bash
# 生成JWT密钥
openssl rand -base64 64 | tr -d "=+/" | cut -c1-64

# 生成数据库密码
openssl rand -base64 32 | tr -d "=+/" | cut -c1-32
```

## 启动方式

### 方式1: 使用启动脚本（推荐）

```bash
./start-backend.sh
```

**特性：**
- 自动检查环境变量文件
- 验证数据库和Redis连接
- 显示配置信息
- 错误处理和提示

### 方式2: 手动加载环境变量

```bash
# 加载环境变量并启动
export $(grep -v '^#' .env.local | grep -v '^$' | xargs)
./mvnw spring-boot:run
```

### 方式3: 使用Maven环境变量

```bash
# 设置环境变量并启动
SPRING_PROFILES_ACTIVE=dev \
SPRING_DATASOURCE_PASSWORD=your_password \
APP_JWT_SECRET=your_jwt_secret \
./mvnw spring-boot:run
```

## 配置文件优先级

Spring Boot按以下优先级加载配置：

1. 命令行参数
2. 环境变量
3. `application-{profile}.yml`
4. `application.yml`
5. `application.properties`

环境变量会覆盖配置文件中的值。

## 开发工具配置

### Spring Boot DevTools

```yaml
spring:
  devtools:
    restart:
      enabled: true
    livereload:
      enabled: true
```

### 热重载

启用DevTools后，修改Java代码会自动重启应用，修改静态资源会自动刷新浏览器。

### 日志配置

```bash
# 开发环境日志级别
LOGGING_LEVEL_COM_STATICDATA_PLATFORM=DEBUG
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WEB=DEBUG
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=DEBUG
```

## 数据库管理

### 初始化数据库

```bash
# 使用Docker Compose
docker-compose up -d postgres

# 手动连接数据库
psql -h localhost -U sdp_user -d static_data_platform_dev
```

### 数据库迁移

```bash
# 查看迁移状态
./mvnw flyway:info

# 执行迁移
./mvnw flyway:migrate
```

## 前端开发集成

### CORS配置

```bash
# 允许前端开发服务器
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

### API代理配置

前端开发服务器可以代理API请求：

```javascript
// vite.config.ts
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```

## 故障排除

### 常见问题

#### 1. 数据库连接失败

```bash
# 检查数据库服务状态
docker ps | grep postgres
brew services list | grep postgres

# 检查连接
nc -z localhost 5432
```

#### 2. Redis连接失败

```bash
# 检查Redis服务状态
docker ps | grep redis
brew services list | grep redis

# 检查连接
nc -z localhost 6379
```

#### 3. 环境变量未生效

```bash
# 检查环境变量
env | grep SPRING
env | grep APP_

# 验证配置文件语法
cat .env.local | grep -v '^#' | grep -v '^$'
```

#### 4. JWT验证失败

```bash
# 检查JWT密钥长度
echo $APP_JWT_SECRET | wc -c

# 生成新的JWT密钥
openssl rand -base64 64 | tr -d "=+/" | cut -c1-64
```

### 调试技巧

#### 1. 启用详细日志

```bash
# 在.env.local中添加
LOGGING_LEVEL_COM_STATICDATA_PLATFORM=TRACE
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=DEBUG
```

#### 2. 检查配置加载

```bash
# 访问配置端点
curl http://localhost:8080/api/actuator/configprops
```

#### 3. 监控应用状态

```bash
# 健康检查
curl http://localhost:8080/api/actuator/health

# 应用信息
curl http://localhost:8080/api/actuator/info
```

## 安全最佳实践

### 1. 密钥管理

- ✅ 使用强密钥（至少256位）
- ✅ 定期轮换密钥
- ✅ 不同环境使用不同密钥
- ❌ 不要硬编码敏感信息

### 2. 文件保护

- ✅ `.env.local` 在 `.gitignore` 中
- ✅ 设置适当的文件权限
- ✅ 定期清理敏感文件

### 3. 网络安全

- ✅ 使用HTTPS（生产环境）
- ✅ 配置防火墙规则
- ✅ 限制数据库访问

## 相关脚本

### 环境管理脚本

```bash
# 生成环境变量
./scripts/generate-env.sh

# 启动开发环境
./start-backend.sh

# 设置GitHub Secrets
./scripts/setup-github-secrets.sh
```

### Docker开发环境

#### 方式1: 使用脚本（推荐）

```bash
# 启动数据库和Redis服务
./start-docker.sh

# 查看服务日志
./logs-docker.sh

# 停止服务
./stop-docker.sh

# 重建服务
./rebuild-docker.sh
```

#### 方式2: 直接使用Docker Compose

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 停止服务
docker-compose down
```

## 相关文档

- [Spring Boot外部配置](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.devtools)
- [PostgreSQL文档](https://www.postgresql.org/docs/)
- [Redis文档](https://redis.io/documentation)
