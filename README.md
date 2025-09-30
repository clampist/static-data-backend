# 静态数据托管平台 - 后端

基于Spring Boot 3.4.0构建的RESTful API后端服务。

## 📚 文档导航 / Documentation Navigation

### 核心文档 / Core Documentation

- **[API完整指南](docs/API_GUIDE.md)** - 所有API端点的完整使用指南
- **[Docker部署指南](docs/DOCKER_GUIDE.md)** - 完整的Docker部署解决方案
- **[测试指南](docs/TESTING_GUIDE.md)** - 单元测试、集成测试和覆盖率分析
- **[问题排查指南](docs/TROUBLESHOOTING_GUIDE.md)** - 常见问题的排查和解决方案
- **[技术参考文档](docs/HELP.md)** - 技术栈参考和项目特定信息
- **[集成测试指南](docs/INTEGRATION_TEST_GUIDE.md)** - 前后端集成测试指南

### 快速开始 / Quick Start

#### 方法1: 本地开发环境
```bash
# 环境要求
- Java 17+
- Maven 3.9+
- PostgreSQL 15+
- Redis 6+

# 配置数据库
createdb static_data_platform_dev
createdb static_data_platform_test

# 配置用户权限
psql postgres
CREATE USER sdp_user WITH PASSWORD 'dev_password';
GRANT ALL PRIVILEGES ON DATABASE static_data_platform_dev TO sdp_user;
GRANT ALL PRIVILEGES ON DATABASE static_data_platform_test TO sdp_user;

# 启动应用
mvn spring-boot:run
```

#### 方法2: Docker环境（推荐）
```bash
# 一键启动所有服务
./setup.sh

# 或者手动启动
./start-docker.sh
```

应用将在 http://localhost:8080 启动

## 📋 主要功能

### 核心实体
- **用户管理**: 用户注册、登录、权限控制
- **组织结构**: 四级树状结构管理（部门→团队→业务方向→模块）
- **数据文件**: 动态数据结构和内容管理
- **版本控制**: 数据文件版本历史
- **审计日志**: 操作记录和追踪
- **API统计**: 使用情况监控

### API端点概览

#### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/me` - 获取当前用户信息
- `POST /api/auth/refresh` - 刷新Token

#### 组织管理
- `GET /api/organization/tree` - 获取完整组织树
- `POST /api/organization/nodes` - 创建组织节点
- `PUT /api/organization/nodes/{id}` - 更新组织节点
- `DELETE /api/organization/nodes/{id}` - 删除组织节点

#### 数据文件
- `POST /api/data-files` - 创建数据文件
- `GET /api/data-files/{id}` - 获取数据文件详情
- `PUT /api/data-files/{id}` - 更新数据文件
- `DELETE /api/data-files/{id}` - 删除数据文件
- `POST /api/data-files/query` - 查询数据文件
- `GET /api/data-files/statistics` - 获取统计信息

> 📖 **详细API文档**: 查看 [API完整指南](docs/API_GUIDE.md) 获取所有端点的详细使用说明

## 🧪 测试

### 快速测试
```bash
# 运行所有测试
mvn test

# 运行集成测试
mvn verify

# 生成覆盖率报告
mvn jacoco:report
```

### API测试
```bash
# 认证API测试
./api-tests/test-auth-apis.sh

# 组织管理API测试
./api-tests/test-organization-apis.sh

# 数据文件API测试
./api-tests/test-final-datafile.sh
```

> 🧪 **详细测试文档**: 查看 [测试指南](docs/TESTING_GUIDE.md) 获取完整的测试方案和覆盖率分析

## 📖 API文档

### 在线文档
启动应用后访问：
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **API Docs**: http://localhost:8080/api/api-docs

### 离线文档
- **[API完整指南](docs/API_GUIDE.md)** - 详细的API使用说明和示例
- **Postman集合**: `postman-collection.json` - 可直接导入Postman的测试集合

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

### Docker部署（推荐）
```bash
# 一键部署
./setup.sh

# 手动部署
docker-compose up -d
```

### 生产部署
```bash
# 构建生产镜像
docker build -t static-data-platform-backend:latest .

# 运行生产容器
docker run -d -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://db:5432/static_data_platform \
  -e JWT_SECRET=your_production_secret \
  static-data-platform-backend:latest
```

> 🐳 **详细部署文档**: 查看 [Docker部署指南](DOCKER_GUIDE.md) 获取完整的部署方案

### 环境变量
- `DATABASE_URL` - 数据库连接URL
- `REDIS_URL` - Redis连接URL
- `JWT_SECRET` - JWT签名密钥
- `CORS_ALLOWED_ORIGINS` - 允许的跨域来源

## 🚨 问题排查

### 常见问题
- **数据库连接失败**: 检查PostgreSQL服务状态和连接配置
- **API返回500错误**: 查看应用日志，检查数据库查询
- **Docker容器启动失败**: 检查端口占用和内存配置
- **测试失败**: 检查测试数据库配置和依赖服务

### 快速诊断
```bash
# 检查服务状态
curl http://localhost:8080/api/actuator/health

# 查看应用日志
tail -f logs/application.log

# 检查Docker服务
docker-compose ps
```

> 🔧 **详细排查文档**: 查看 [问题排查指南](TROUBLESHOOTING_GUIDE.md) 获取完整的问题解决方案

## 📚 完整文档索引

### 核心文档
| 文档 | 描述 | 适用场景 |
|------|------|----------|
| [API完整指南](docs/API_GUIDE.md) | 所有API端点的详细使用说明 | API开发、集成、测试 |
| [Docker部署指南](docs/DOCKER_GUIDE.md) | 完整的Docker部署解决方案 | 部署、运维、生产环境 |
| [测试指南](docs/TESTING_GUIDE.md) | 单元测试、集成测试和覆盖率分析 | 测试开发、质量保证 |
| [问题排查指南](docs/TROUBLESHOOTING_GUIDE.md) | 常见问题的排查和解决方案 | 故障排除、问题诊断 |
| [技术参考文档](docs/HELP.md) | 技术栈参考和项目特定信息 | 开发参考、技术查阅 |
| [集成测试指南](docs/INTEGRATION_TEST_GUIDE.md) | 前后端集成测试指南 | 集成测试、端到端测试 |

### 历史文档（已整合到上述核心文档中）
- ~~API_TESTING_GUIDE.md~~ → 已整合到 [API完整指南](API_GUIDE.md)
- ~~API_COVERAGE_GUIDE.md~~ → 已整合到 [测试指南](TESTING_GUIDE.md)
- ~~DATAFILE_API_GUIDE.md~~ → 已整合到 [API完整指南](API_GUIDE.md)
- ~~ORGANIZATION_API_GUIDE.md~~ → 已整合到 [API完整指南](API_GUIDE.md)
- ~~DOCKER_SETUP_README.md~~ → 已整合到 [Docker部署指南](DOCKER_GUIDE.md)
- ~~DOCKER_SETUP_SUCCESS.md~~ → 已整合到 [Docker部署指南](DOCKER_GUIDE.md)
- ~~POSTGRESQL_FINAL_FIX.md~~ → 已整合到 [问题排查指南](TROUBLESHOOTING_GUIDE.md)
- ~~POSTGRESQL_FIX_GUIDE.md~~ → 已整合到 [问题排查指南](TROUBLESHOOTING_GUIDE.md)
- ~~POSTGRESQL_FIX_SUCCESS.md~~ → 已整合到 [问题排查指南](TROUBLESHOOTING_GUIDE.md)
- ~~STATISTICS_API_FIX.md~~ → 已整合到 [问题排查指南](TROUBLESHOOTING_GUIDE.md)
- ~~JACOCO_SETUP_GUIDE.md~~ → 已整合到 [测试指南](TESTING_GUIDE.md)

### 快速导航
- 🚀 **快速开始**: 查看本README的"快速开始"部分
- 📖 **API使用**: 查看 [API完整指南](docs/API_GUIDE.md)
- 🐳 **Docker部署**: 查看 [Docker部署指南](docs/DOCKER_GUIDE.md)
- 🧪 **测试开发**: 查看 [测试指南](docs/TESTING_GUIDE.md)
- 🔧 **问题排查**: 查看 [问题排查指南](docs/TROUBLESHOOTING_GUIDE.md)
- 📚 **技术参考**: 查看 [技术参考文档](docs/HELP.md)
- 🔗 **集成测试**: 查看 [集成测试指南](docs/INTEGRATION_TEST_GUIDE.md)

---

**文档版本**: 1.0.0  
**最后更新**: 2024-01-01  
**状态**: 生产就绪 🚀