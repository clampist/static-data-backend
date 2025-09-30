# 测试策略指南

## 概述

本文档描述了在GitHub Actions中运行单元测试的最佳实践，以及本地开发环境的测试配置。

## 为什么不在GitHub Actions中使用Docker运行测试？

### Docker方式的局限性
1. **性能开销大**: 容器启动、镜像拉取消耗大量时间
2. **资源消耗高**: GitHub Actions免费额度有限
3. **复杂性增加**: 需要维护额外的Docker配置
4. **调试困难**: 容器内的问题难以排查

### 推荐方案
使用GitHub Actions的**Services**功能，直接在Runner中运行测试，通过服务容器提供数据库支持。

## 测试环境配置

### 1. GitHub Actions配置 (`.github/workflows/ci.yml`)

```yaml
services:
  postgres:
    image: postgres:15-alpine
    env:
      POSTGRES_DB: static_data_platform_test
      POSTGRES_USER: sdp_user
      POSTGRES_PASSWORD: test_password
    ports:
      - 5432:5432
  
  redis:
    image: redis:7-alpine
    ports:
      - 6379:6379
```

**优势**:
- 服务容器轻量级，启动快
- 直接暴露端口，无需复杂网络配置
- 自动健康检查
- 资源消耗低

### 2. 本地开发测试

#### 快速测试 (推荐)
```bash
# 使用本地Maven + Docker服务
./run-tests-local.sh
```

#### 完整Docker测试 (可选)
```bash
# 使用Docker容器运行测试
./run-tests-docker.sh
```

## 测试配置文件

### CI环境 (`application-ci.yml`)
- 优化的日志级别
- 简化的连接池配置
- 关闭审计功能
- 适合CI环境的超时设置

### 测试环境 (`application-test.yml`)
- 详细的调试日志
- 完整的数据库配置
- 适合本地开发调试

## 测试执行流程

### GitHub Actions流程
1. **Checkout代码**
2. **设置Java 17环境**
3. **缓存Maven依赖**
4. **启动PostgreSQL和Redis服务**
5. **运行单元测试**
6. **生成测试报告**
7. **上传覆盖率报告**

### 本地开发流程
1. **检查环境依赖**
2. **启动测试服务容器**
3. **等待服务就绪**
4. **运行Maven测试**
5. **生成覆盖率报告**
6. **清理测试环境**

## 性能对比

| 方案 | 启动时间 | 资源消耗 | 维护成本 | 调试难度 |
|------|----------|----------|----------|----------|
| GitHub Actions Services | ~30s | 低 | 低 | 简单 |
| Docker容器测试 | ~2-3分钟 | 高 | 高 | 复杂 |

## 最佳实践

### 1. 测试隔离
- 每个测试使用独立的数据库
- 使用`@Transactional`和`@Rollback`确保数据隔离
- 使用TestContainers进行集成测试

### 2. 测试数据管理
```java
@Test
@Transactional
@Rollback
void testUserCreation() {
    // 测试代码
}
```

### 3. 环境变量配置
```bash
# CI环境
SPRING_PROFILES_ACTIVE=ci

# 本地测试
SPRING_PROFILES_ACTIVE=test
```

### 4. 测试报告
- 使用Surefire生成JUnit报告
- 使用JaCoCo生成覆盖率报告
- 集成到GitHub Actions的测试报告功能

## 故障排除

### 常见问题

#### 1. 数据库连接失败
```bash
# 检查服务状态
docker ps | grep postgres
docker exec sdp-postgres-test pg_isready -U sdp_user
```

#### 2. Redis连接失败
```bash
# 检查Redis状态
docker exec sdp-redis-test redis-cli ping
```

#### 3. 端口冲突
```bash
# 检查端口占用
lsof -i :5433
lsof -i :6380
```

### 调试技巧

#### 1. 查看测试日志
```bash
# 运行测试时显示详细日志
mvn test -Dspring.profiles.active=test -X
```

#### 2. 检查测试数据库
```bash
# 连接到测试数据库
docker exec -it sdp-postgres-test psql -U sdp_user -d static_data_platform_test
```

## 总结

对于GitHub Actions中的单元测试：

✅ **推荐**: 使用Services + 本地Maven  
❌ **不推荐**: 使用Docker容器运行测试  

这种方案能够：
- 显著减少CI执行时间
- 降低资源消耗
- 简化配置和维护
- 提高调试效率

---
**创建时间**: 2025-09-30 02:05  
**状态**: 测试策略配置完成 ✅
