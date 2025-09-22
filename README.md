# 静态数据托管平台 - 后端

基于Spring Boot 3.4.0构建的RESTful API后端服务。

## 🚀 快速启动

### 环境要求
- Java 17+
- Maven 3.9+
- PostgreSQL 15+
- Redis 6+

### 配置数据库
```bash
# 创建数据库
createdb static_data_platform_dev
createdb static_data_platform_test

# 配置用户权限
psql postgres
CREATE USER sdp_user WITH PASSWORD 'dev_password';
GRANT ALL PRIVILEGES ON DATABASE static_data_platform_dev TO sdp_user;
GRANT ALL PRIVILEGES ON DATABASE static_data_platform_test TO sdp_user;
```

### 启动应用
```bash
mvn spring-boot:run
```

应用将在 http://localhost:8080 启动

## 📋 主要功能

### 核心实体
- **用户管理**: 用户注册、登录、权限控制
- **组织结构**: 四级树状结构管理
- **数据文件**: 动态数据结构和内容管理
- **版本控制**: 数据文件版本历史
- **审计日志**: 操作记录和追踪
- **API统计**: 使用情况监控

### API端点

#### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/profile` - 获取用户信息

#### 组织管理
- `GET /api/organization/tree` - 获取组织树
- `POST /api/organization/nodes` - 创建组织节点
- `PUT /api/organization/nodes/{id}` - 更新组织节点
- `DELETE /api/organization/nodes/{id}` - 删除组织节点

#### 数据文件
- `GET /api/data-files` - 获取数据文件列表
- `POST /api/data-files` - 创建数据文件
- `GET /api/data-files/{hash}` - 通过hash获取数据文件
- `PUT /api/data-files/{hash}` - 更新数据文件
- `DELETE /api/data-files/{hash}` - 删除数据文件
- `POST /api/data-files/import` - 导入Excel/CSV
- `GET /api/data-files/{id}/export` - 导出文件

#### 版本管理
- `GET /api/data-files/{id}/versions` - 获取版本列表
- `POST /api/data-files/{id}/versions` - 创建新版本
- `GET /api/data-files/{id}/versions/{versionNumber}` - 获取特定版本

## 🧪 测试

### 单元测试
```bash
mvn test
```

### 集成测试
```bash
mvn verify
```

### 测试覆盖率
```bash
mvn jacoco:report
```

## 📖 API文档

启动应用后访问：
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- API Docs: http://localhost:8080/api/api-docs

## 🏗️ 架构设计

### 分层架构
```
Controller → Service → Repository → Entity
     ↓         ↓          ↓
   DTO    →  Domain  →  Database
```

### 技术选型
- **Web框架**: Spring Boot 3.4.0
- **安全**: Spring Security + JWT
- **数据访问**: Spring Data JPA
- **数据库**: PostgreSQL + Redis
- **文档**: SpringDoc OpenAPI
- **测试**: JUnit 5 + TestNG + Testcontainers
- **对象映射**: MapStruct

### 配置文件
- `application.properties` - 基础配置
- `application-dev.yml` - 开发环境配置
- `application-test.yml` - 测试环境配置

## 🔧 开发指南

### 代码规范
- 遵循Google Java Style Guide
- 使用Lombok减少样板代码
- 实体类继承BaseEntity获得公共字段
- Service层处理业务逻辑
- Controller层只负责请求响应转换

### 数据库设计
- 使用JSONB存储动态数据结构
- 支持数据版本控制
- 完整的审计字段
- 合理的索引设计

### 异常处理
- 全局异常处理器统一处理
- 自定义业务异常
- 统一的错误响应格式

### 安全配置
- JWT Token认证
- CORS跨域配置
- 方法级权限控制
- 密码加密存储

## 📦 构建部署

### 本地构建
```bash
mvn clean package
```

### Docker部署
```bash
docker build -t static-data-platform-backend .
docker run -p 8080:8080 static-data-platform-backend
```

### 环境变量
- `DATABASE_URL` - 数据库连接URL
- `REDIS_URL` - Redis连接URL
- `JWT_SECRET` - JWT签名密钥
- `CORS_ALLOWED_ORIGINS` - 允许的跨域来源