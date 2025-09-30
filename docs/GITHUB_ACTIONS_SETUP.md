# GitHub Actions 环境变量配置指南

## 概述

本文档说明如何在GitHub Actions中配置环境变量，以确保敏感信息的安全性。

## 环境变量类型

### 1. 数据库配置
- `DATABASE_URL`: 数据库连接URL
- `DATABASE_USERNAME`: 数据库用户名
- `DATABASE_PASSWORD`: 数据库密码

### 2. JWT配置
- `JWT_SECRET`: JWT签名密钥（至少256位）
- `JWT_EXPIRATION`: JWT过期时间（毫秒）

### 3. Redis配置
- `REDIS_HOST`: Redis主机地址
- `REDIS_PORT`: Redis端口

### 4. CORS配置
- `CORS_ALLOWED_ORIGINS`: 允许的跨域源

## 在GitHub中配置Secrets

### 步骤1: 进入仓库设置
1. 打开你的GitHub仓库
2. 点击 `Settings` 标签
3. 在左侧菜单中选择 `Secrets and variables` > `Actions`

### 步骤2: 添加Repository Secrets
点击 `New repository secret` 添加以下密钥：

#### 必需的环境变量：
```
DATABASE_URL=jdbc:postgresql://your-ci-db-host:5432/static_data_platform_test
DATABASE_USERNAME=your_ci_db_user
DATABASE_PASSWORD=your_secure_ci_password
JWT_SECRET=your_very_long_and_secure_jwt_secret_key_at_least_256_bits_long
```

#### 可选的环境变量：
```
JWT_EXPIRATION=86400000
REDIS_HOST=your-redis-host
REDIS_PORT=6379
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://your-frontend-domain.com
```

### 步骤3: 配置Environment Secrets（用于生产部署）
1. 在 `Settings` > `Environments` 中创建 `production` 环境
2. 为生产环境添加特定的密钥：
```
PROD_DATABASE_URL=jdbc:postgresql://prod-db-host:5432/static_data_platform_prod
PROD_DATABASE_USERNAME=prod_db_user
PROD_DATABASE_PASSWORD=your_secure_prod_password
PROD_JWT_SECRET=your_very_long_and_secure_prod_jwt_secret_key
```

## 工作流配置说明

### 环境变量注入方式

#### 1. 全局环境变量
```yaml
env:
  SPRING_PROFILES_ACTIVE: ci
  APP_VERSION: ${{ github.sha }}
```

#### 2. 从Secrets注入
```yaml
env:
  SPRING_DATASOURCE_URL: ${{ secrets.DATABASE_URL }}
  SPRING_DATASOURCE_PASSWORD: ${{ secrets.DATABASE_PASSWORD }}
  APP_JWT_SECRET: ${{ secrets.JWT_SECRET }}
```

#### 3. 带默认值的环境变量
```yaml
env:
  SPRING_DATA_REDIS_HOST: ${{ secrets.REDIS_HOST || 'localhost' }}
  SPRING_DATA_REDIS_PORT: ${{ secrets.REDIS_PORT || '6379' }}
```

### 步骤级别的环境变量
```yaml
steps:
- name: Run tests
  run: ./mvnw test
  env:
    SPRING_PROFILES_ACTIVE: test
    SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/static_data_platform_test
```

## 安全最佳实践

### 1. 密钥管理
- ✅ 使用GitHub Secrets存储敏感信息
- ✅ 定期轮换密钥
- ✅ 使用强密码（至少32位字符）
- ❌ 不要在代码中硬编码敏感信息

### 2. 环境隔离
- ✅ 为不同环境使用不同的密钥
- ✅ 使用Environment保护生产部署
- ✅ 限制密钥访问权限

### 3. 监控和审计
- ✅ 启用GitHub Actions审计日志
- ✅ 监控密钥使用情况
- ✅ 定期检查访问权限

## 本地开发环境配置

### 创建本地环境变量文件
创建 `.env.local` 文件（不要提交到Git）：
```bash
# 本地开发环境变量
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/static_data_platform_dev
SPRING_DATASOURCE_USERNAME=sdp_user
SPRING_DATASOURCE_PASSWORD=your_local_password
APP_JWT_SECRET=your_local_jwt_secret_key
```

### 使用环境变量运行应用
```bash
# 加载环境变量并运行
source .env.local && ./mvnw spring-boot:run

# 或者使用Docker Compose
docker-compose --env-file .env.local up
```

## 故障排除

### 常见问题

#### 1. 环境变量未生效
- 检查变量名是否正确
- 确认Secrets已正确配置
- 验证YAML语法

#### 2. 数据库连接失败
- 检查DATABASE_URL格式
- 确认数据库服务正在运行
- 验证网络连接

#### 3. JWT验证失败
- 确认JWT_SECRET长度足够
- 检查密钥是否包含特殊字符
- 验证时间同步

### 调试技巧
```yaml
- name: Debug environment variables
  run: |
    echo "Database URL: ${{ secrets.DATABASE_URL }}"
    echo "JWT Secret length: ${#JWT_SECRET}"
    # 注意：不要直接打印敏感信息到日志
```

## 示例配置

### 完整的GitHub Secrets配置
```
# 数据库
DATABASE_URL=jdbc:postgresql://ci-db.example.com:5432/static_data_platform_test
DATABASE_USERNAME=ci_user
DATABASE_PASSWORD=SecurePassword123!@#

# JWT
JWT_SECRET=MyVeryLongAndSecureJWTSecretKeyThatIsAtLeast256BitsLongForSecurity123456789012345678901234567890
JWT_EXPIRATION=86400000

# Redis
REDIS_HOST=ci-redis.example.com
REDIS_PORT=6379

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://ci-frontend.example.com
```

## 相关文档
- [GitHub Secrets文档](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
- [Spring Boot外部配置](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [环境变量最佳实践](https://12factor.net/config)
