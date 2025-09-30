# 静态数据平台 问题排查完整指南
# Static Data Platform Troubleshooting Complete Guide

## 📋 目录 / Table of Contents

- [概述 / Overview](#概述--overview)
- [常见问题分类 / Common Issue Categories](#常见问题分类--common-issue-categories)
- [数据库相关问题 / Database Issues](#数据库相关问题--database-issues)
- [API相关问题 / API Issues](#api相关问题--api-issues)
- [Docker部署问题 / Docker Deployment Issues](#docker部署问题--docker-deployment-issues)
- [测试相关问题 / Testing Issues](#测试相关问题--testing-issues)
- [性能问题 / Performance Issues](#性能问题--performance-issues)
- [安全相关问题 / Security Issues](#安全相关问题--security-issues)
- [日志分析 / Log Analysis](#日志分析--log-analysis)
- [调试技巧 / Debugging Tips](#调试技巧--debugging-tips)
- [预防措施 / Prevention Measures](#预防措施--prevention-measures)

## 概述 / Overview

本指南提供了静态数据平台常见问题的完整排查和解决方案。基于项目开发过程中遇到的实际问题和解决方案，帮助开发者和运维人员快速定位和解决问题。

This guide provides comprehensive troubleshooting and solutions for common issues in the Static Data Platform. Based on real problems encountered during project development and their solutions, it helps developers and operations personnel quickly locate and resolve issues.

### 问题分类概览 / Issue Categories Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Problem Categories                      │
│                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │  Database   │  │     API     │  │   Docker    │         │
│  │   Issues    │  │   Issues    │  │   Issues    │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
│                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │   Testing   │  │ Performance │  │  Security   │         │
│  │   Issues    │  │   Issues    │  │   Issues    │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
```

## 常见问题分类 / Common Issue Categories

### 问题严重程度分级 / Issue Severity Levels

- 🔴 **严重 (Critical)**: 系统完全不可用
- 🟡 **重要 (High)**: 核心功能受影响
- 🟢 **一般 (Medium)**: 部分功能受影响
- 🔵 **轻微 (Low)**: 非核心功能问题

### 问题解决优先级 / Issue Resolution Priority

1. **P0 - 紧急**: 生产环境完全不可用
2. **P1 - 高**: 核心功能不可用
3. **P2 - 中**: 部分功能受影响
4. **P3 - 低**: 非关键功能问题

## 数据库相关问题 / Database Issues

### PostgreSQL相关问题 / PostgreSQL Issues

#### 1. PostgreSQL bytea错误 🔴

**问题描述**:
```
ERROR: function lower(bytea) does not exist
建议：No function matches the given name and argument types. You might need to add explicit type casts.
```

**根本原因**:
- Spring Data JPA自动生成的查询中，`name`字段被当作`bytea`类型处理
- PostgreSQL的`lower()`函数不能直接用于`bytea`类型

**解决方案**:

**方案A: 修复查询方法**
```java
// 原始方法（有问题）
List<DataFile> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name);

// 修复后
@Query("SELECT df FROM DataFile df WHERE LOWER(df.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY df.createdAt DESC")
List<DataFile> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(@Param("name") String name);
```

**方案B: 简化查询逻辑**
```java
// 在Service层进行过滤
public Page<DataFileDto> queryDataFiles(DataFileQueryRequest request) {
    Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
    Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), sort);
    
    // 直接使用findAll，在Service层进行过滤
    Page<DataFile> dataFiles = dataFileRepository.findAll(pageable);
    
    // 在Java中进行过滤和转换
    return dataFiles.map(this::convertToDto);
}
```

**验证步骤**:
```bash
# 1. 重新编译
mvn clean compile

# 2. 重启服务
mvn spring-boot:run

# 3. 测试API
curl -X POST http://localhost:8080/api/data-files/query \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"page": 1, "size": 10}'
```

#### 2. 数据库连接失败 🟡

**问题描述**:
```
Connection refused: connect
Could not connect to database
```

**排查步骤**:

**步骤1: 检查数据库服务状态**
```bash
# 检查PostgreSQL是否运行
sudo systemctl status postgresql

# 检查端口是否监听
netstat -tlnp | grep 5432
lsof -i :5432
```

**步骤2: 检查数据库配置**
```bash
# 检查配置文件
cat src/main/resources/application-dev.yml

# 验证连接参数
psql -h localhost -p 5432 -U sdp_user -d static_data_platform_dev
```

**步骤3: 检查数据库权限**
```sql
-- 检查用户权限
\du sdp_user

-- 检查数据库权限
\l static_data_platform_dev
```

**解决方案**:
```bash
# 1. 重启数据库服务
sudo systemctl restart postgresql

# 2. 重新创建用户和权限
psql postgres
CREATE USER sdp_user WITH PASSWORD 'dev_password';
GRANT ALL PRIVILEGES ON DATABASE static_data_platform_dev TO sdp_user;

# 3. 检查防火墙设置
sudo ufw status
sudo ufw allow 5432
```

#### 3. 数据库表结构问题 🟡

**问题描述**:
```
Table 'data_files' doesn't exist
Column 'name' doesn't exist
```

**排查步骤**:
```sql
-- 检查表是否存在
\dt

-- 检查表结构
\d data_files

-- 检查列信息
SELECT column_name, data_type, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'data_files';
```

**解决方案**:
```bash
# 1. 重新运行数据库迁移
mvn flyway:migrate

# 2. 或者重新创建数据库
mvn clean compile
mvn spring-boot:run
```

### Redis相关问题 / Redis Issues

#### 1. Redis连接失败 🟡

**问题描述**:
```
Connection refused: connect
Redis connection failed
```

**排查步骤**:
```bash
# 检查Redis服务状态
sudo systemctl status redis

# 检查Redis连接
redis-cli ping

# 检查端口
netstat -tlnp | grep 6379
```

**解决方案**:
```bash
# 1. 重启Redis服务
sudo systemctl restart redis

# 2. 检查Redis配置
cat /etc/redis/redis.conf | grep bind

# 3. 测试连接
redis-cli -h localhost -p 6379 ping
```

## API相关问题 / API Issues

### 认证相关问题 / Authentication Issues

#### 1. JWT Token无效 🟡

**问题描述**:
```
401 Unauthorized
JWT token is invalid or expired
```

**排查步骤**:
```bash
# 1. 检查Token格式
echo "YOUR_TOKEN" | base64 -d

# 2. 验证Token内容
# 使用jwt.io在线工具验证Token

# 3. 检查Token过期时间
curl -X GET "http://localhost:8080/api/auth/validate" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**解决方案**:
```bash
# 1. 重新登录获取新Token
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'

# 2. 检查JWT配置
cat src/main/resources/application-dev.yml | grep jwt
```

#### 2. 权限不足 🟡

**问题描述**:
```
403 Forbidden
Access denied
```

**排查步骤**:
```java
// 检查用户角色
@GetMapping("/auth/me")
public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
    UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
    System.out.println("User roles: " + userPrincipal.getAuthorities());
    // ...
}
```

**解决方案**:
```bash
# 1. 检查用户角色
curl -X GET "http://localhost:8080/api/auth/me" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 2. 更新用户角色（如果需要）
# 在数据库中直接更新用户角色
```

### API响应问题 / API Response Issues

#### 1. 500内部服务器错误 🔴

**问题描述**:
```
500 Internal Server Error
```

**排查步骤**:
```bash
# 1. 查看应用日志
tail -f logs/application.log

# 2. 检查堆栈跟踪
grep -A 20 "Exception" logs/application.log

# 3. 检查数据库连接
curl http://localhost:8080/api/actuator/health
```

**常见原因和解决方案**:

**原因1: 数据库查询错误**
```java
// 解决方案：简化查询
@Query("SELECT df FROM DataFile df")
Page<DataFile> findAllDataFiles(Pageable pageable);
```

**原因2: 空指针异常**
```java
// 解决方案：添加空值检查
if (dataFile != null && dataFile.getName() != null) {
    // 处理逻辑
}
```

**原因3: 类型转换错误**
```java
// 解决方案：安全的类型转换
try {
    Long id = Long.valueOf(request.getId());
} catch (NumberFormatException e) {
    throw new BusinessException("Invalid ID format");
}
```

#### 2. 400请求错误 🟡

**问题描述**:
```
400 Bad Request
Validation failed
```

**排查步骤**:
```bash
# 1. 检查请求格式
curl -X POST "http://localhost:8080/api/data-files" \
  -H "Content-Type: application/json" \
  -d '{"name": "test", "organizationNodeId": 1}' \
  -v

# 2. 验证JSON格式
echo '{"name": "test"}' | jq .
```

**解决方案**:
```java
// 添加请求验证
@PostMapping("/data-files")
public ResponseEntity<DataFileDto> createDataFile(
    @Valid @RequestBody CreateDataFileRequest request,
    Authentication authentication) {
    
    // 验证必填字段
    if (request.getName() == null || request.getName().trim().isEmpty()) {
        throw new BusinessException("文件名不能为空");
    }
    
    // 处理逻辑
}
```

## Docker部署问题 / Docker Deployment Issues

### 容器启动问题 / Container Startup Issues

#### 1. 端口冲突 🟡

**问题描述**:
```
bind: address already in use
Port 8080 is already in use
```

**排查步骤**:
```bash
# 1. 检查端口占用
lsof -i :8080
lsof -i :5432
lsof -i :6379

# 2. 检查Docker容器
docker ps -a
docker-compose ps
```

**解决方案**:
```bash
# 方案1: 停止占用端口的进程
sudo kill -9 $(lsof -t -i:8080)

# 方案2: 修改端口映射
# 编辑docker-compose.yml
ports:
  - "8081:8080"  # 使用8081端口

# 方案3: 停止所有Docker容器
docker-compose down
docker stop $(docker ps -aq)
```

#### 2. 内存不足 🟡

**问题描述**:
```
Container killed due to memory limit
Out of memory
```

**排查步骤**:
```bash
# 1. 检查系统内存
free -h
docker stats

# 2. 检查Docker内存限制
docker system df
docker system prune -f
```

**解决方案**:
```bash
# 1. 增加Docker内存限制
# 在Docker Desktop设置中增加内存到8GB+

# 2. 优化容器资源使用
# 在docker-compose.yml中添加资源限制
services:
  app:
    deploy:
      resources:
        limits:
          memory: 2G
          cpus: '1.0'
```

#### 3. 镜像构建失败 🔴

**问题描述**:
```
Failed to build Docker image
Maven build failed
```

**排查步骤**:
```bash
# 1. 检查Dockerfile
cat Dockerfile

# 2. 检查Maven配置
mvn clean compile

# 3. 检查网络连接
ping maven.aliyun.com
```

**解决方案**:
```dockerfile
# 修复Dockerfile
FROM eclipse-temurin:17-jdk AS build

# 设置代理（如果需要）
ENV https_proxy=http://127.0.0.1:6152
ENV http_proxy=http://127.0.0.1:6152

# 手动安装Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests
```

### 网络连接问题 / Network Connection Issues

#### 1. 容器间通信失败 🟡

**问题描述**:
```
Connection refused between containers
Database connection failed from app container
```

**排查步骤**:
```bash
# 1. 检查Docker网络
docker network ls
docker network inspect backend_default

# 2. 测试容器间连接
docker exec -it sdp-backend ping sdp-postgres
```

**解决方案**:
```yaml
# 确保使用相同的网络
version: '3.8'
services:
  app:
    depends_on:
      - postgres
      - redis
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/static_data_platform_dev
      - SPRING_REDIS_HOST=redis
```

## 测试相关问题 / Testing Issues

### 单元测试问题 / Unit Test Issues

#### 1. 测试编译失败 🟡

**问题描述**:
```
Compilation failed
Test class not found
```

**排查步骤**:
```bash
# 1. 检查测试目录结构
find src/test -name "*.java"

# 2. 检查测试依赖
mvn dependency:tree | grep test

# 3. 检查Java版本
java -version
mvn -version
```

**解决方案**:
```bash
# 1. 清理并重新编译
mvn clean compile test-compile

# 2. 跳过测试编译（临时解决）
mvn clean compile -DskipTests

# 3. 检查测试配置
cat pom.xml | grep -A 10 surefire
```

#### 2. 测试数据库连接失败 🟡

**问题描述**:
```
Test database connection failed
Testcontainers not working
```

**排查步骤**:
```bash
# 1. 检查Docker是否运行
docker ps

# 2. 检查Testcontainers配置
grep -r "Testcontainers" src/test/

# 3. 检查测试配置文件
cat src/test/resources/application-test.yml
```

**解决方案**:
```java
// 确保Testcontainers正确配置
@Testcontainers
@SpringBootTest
public class BaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

### 覆盖率报告问题 / Coverage Report Issues

#### 1. 覆盖率报告未生成 🟡

**问题描述**:
```
JaCoCo report not generated
Coverage data not found
```

**排查步骤**:
```bash
# 1. 检查JaCoCo配置
mvn help:effective-pom | grep jacoco

# 2. 检查覆盖率数据文件
ls -la target/jacoco.exec

# 3. 检查测试是否运行
mvn test
```

**解决方案**:
```bash
# 1. 手动生成报告
mvn clean test jacoco:report

# 2. 检查报告位置
ls -la target/site/jacoco/

# 3. 使用专用脚本
./ut-scripts/generate-coverage-report.sh
```

## 性能问题 / Performance Issues

### 数据库性能问题 / Database Performance Issues

#### 1. 查询缓慢 🟡

**问题描述**:
```
Slow query execution
Database timeout
```

**排查步骤**:
```sql
-- 1. 检查慢查询日志
SELECT query, mean_time, calls 
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 10;

-- 2. 检查索引使用情况
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read, idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;
```

**解决方案**:
```sql
-- 1. 添加索引
CREATE INDEX idx_data_files_name ON data_files(name);
CREATE INDEX idx_data_files_created_at ON data_files(created_at);

-- 2. 优化查询
EXPLAIN ANALYZE SELECT * FROM data_files WHERE name LIKE '%test%';
```

#### 2. 内存泄漏 🟡

**问题描述**:
```
OutOfMemoryError
Memory usage keeps increasing
```

**排查步骤**:
```bash
# 1. 检查JVM内存使用
jstat -gc <pid>

# 2. 生成堆转储
jmap -dump:format=b,file=heap.hprof <pid>

# 3. 分析内存使用
jhat heap.hprof
```

**解决方案**:
```bash
# 1. 增加JVM内存
export JAVA_OPTS="-Xmx2g -Xms1g"

# 2. 优化垃圾回收
export JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 3. 检查代码中的内存泄漏
# 确保正确关闭资源
```

## 安全相关问题 / Security Issues

### 认证安全问题 / Authentication Security Issues

#### 1. JWT密钥泄露 🟡

**问题描述**:
```
JWT secret exposed
Token validation failed
```

**排查步骤**:
```bash
# 1. 检查配置文件
grep -r "jwt.secret" src/main/resources/

# 2. 检查环境变量
env | grep JWT

# 3. 检查日志中的敏感信息
grep -i "secret\|password\|token" logs/application.log
```

**解决方案**:
```bash
# 1. 生成新的JWT密钥
openssl rand -base64 64

# 2. 更新配置文件
# 使用环境变量而不是硬编码
JWT_SECRET=your_new_secret_key

# 3. 重启应用
mvn spring-boot:run
```

#### 2. CORS配置问题 🟡

**问题描述**:
```
CORS policy blocked
Cross-origin request blocked
```

**排查步骤**:
```bash
# 1. 检查CORS配置
grep -r "cors" src/main/java/

# 2. 检查前端请求
# 在浏览器开发者工具中查看Network标签
```

**解决方案**:
```java
// 配置CORS
@Configuration
public class CorsConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
```

## 日志分析 / Log Analysis

### 日志级别配置 / Log Level Configuration

#### 1. 日志级别设置
```yaml
# application-dev.yml
logging:
  level:
    com.staticdata.platform: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    org.testcontainers: INFO
```

#### 2. 日志文件位置
```bash
# 应用日志
tail -f logs/application.log

# Docker日志
docker logs sdp-backend

# 系统日志
journalctl -u postgresql
journalctl -u redis
```

### 常见日志模式 / Common Log Patterns

#### 1. 错误日志模式
```bash
# 查找ERROR级别日志
grep "ERROR" logs/application.log

# 查找异常堆栈
grep -A 10 "Exception" logs/application.log

# 查找特定错误
grep "Connection refused" logs/application.log
```

#### 2. 性能日志模式
```bash
# 查找慢查询
grep "slow query" logs/application.log

# 查找内存使用
grep "OutOfMemoryError" logs/application.log

# 查找GC信息
grep "GC" logs/application.log
```

## 调试技巧 / Debugging Tips

### 远程调试 / Remote Debugging

#### 1. 启用远程调试
```bash
# 启动应用时启用调试
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# 在IDE中配置远程调试
# Host: localhost
# Port: 5005
```

#### 2. 使用调试工具
```bash
# 使用jstack查看线程状态
jstack <pid>

# 使用jmap查看内存使用
jmap -histo <pid>

# 使用jstat查看GC情况
jstat -gc <pid> 1s
```

### 网络调试 / Network Debugging

#### 1. 网络连接测试
```bash
# 测试端口连通性
telnet localhost 8080
nc -zv localhost 5432

# 测试HTTP请求
curl -v http://localhost:8080/api/actuator/health

# 测试数据库连接
psql -h localhost -p 5432 -U sdp_user -d static_data_platform_dev
```

#### 2. 网络抓包分析
```bash
# 使用tcpdump抓包
sudo tcpdump -i lo -n port 8080

# 使用wireshark分析
# 启动wireshark并监听lo接口
```

## 预防措施 / Prevention Measures

### 监控和告警 / Monitoring and Alerting

#### 1. 健康检查配置
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

#### 2. 监控指标
```bash
# 应用健康状态
curl http://localhost:8080/api/actuator/health

# JVM指标
curl http://localhost:8080/api/actuator/metrics/jvm.memory.used

# HTTP请求指标
curl http://localhost:8080/api/actuator/metrics/http.server.requests
```

### 备份和恢复 / Backup and Recovery

#### 1. 数据库备份
```bash
# 自动备份脚本
#!/bin/bash
BACKUP_DIR="/opt/backups"
DATE=$(date +%Y%m%d_%H%M%S)

# PostgreSQL备份
pg_dump -h localhost -U sdp_user static_data_platform_dev | gzip > $BACKUP_DIR/postgres_backup_$DATE.sql.gz

# 清理旧备份（保留7天）
find $BACKUP_DIR -name "*.gz" -mtime +7 -delete
```

#### 2. 配置备份
```bash
# 备份配置文件
cp -r src/main/resources/ /opt/backups/config_$(date +%Y%m%d)/

# 备份Docker配置
cp docker-compose.yml /opt/backups/
cp Dockerfile /opt/backups/
```

### 代码质量保证 / Code Quality Assurance

#### 1. 代码审查检查清单
- [ ] 异常处理是否完善
- [ ] 日志记录是否充分
- [ ] 性能是否有问题
- [ ] 安全性是否考虑
- [ ] 测试覆盖率是否足够

#### 2. 自动化检查
```bash
# 代码质量检查
mvn checkstyle:check
mvn spotbugs:check
mvn pmd:check

# 安全漏洞扫描
mvn org.owasp:dependency-check-maven:check
```

## 总结 / Summary

本指南提供了静态数据平台常见问题的完整排查和解决方案，包括：

### ✅ 问题分类
- **数据库问题**: PostgreSQL bytea错误、连接失败、表结构问题
- **API问题**: 认证失败、权限不足、响应错误
- **Docker问题**: 端口冲突、内存不足、镜像构建失败
- **测试问题**: 编译失败、数据库连接、覆盖率报告
- **性能问题**: 查询缓慢、内存泄漏
- **安全问题**: JWT密钥泄露、CORS配置

### ✅ 排查方法
- **系统化排查**: 从症状到根因的完整排查流程
- **工具使用**: 日志分析、网络调试、性能监控
- **预防措施**: 监控告警、备份恢复、代码质量保证

### ✅ 解决方案
- **立即解决**: 针对紧急问题的快速修复方案
- **长期优化**: 系统架构和代码质量的持续改进
- **最佳实践**: 基于实际经验的最佳实践建议

通过本指南，您可以：
1. 快速定位和解决常见问题
2. 建立系统化的问题排查流程
3. 预防问题的发生
4. 提高系统的稳定性和可靠性

---

**最后更新**: 2024-01-01  
**版本**: 1.0.0  
**状态**: 生产就绪 🚀
