# PostgreSQL 错误修复指南
# PostgreSQL Error Fix Guide

## 🎯 问题描述

前端数据文件列表页面出现多个500错误：

1. **PostgreSQL函数错误**: `function lower(bytea) does not exist`
2. **组织节点API错误**: 500 Internal Server Error
3. **数据文件查询API错误**: 500 Internal Server Error

## 🔍 问题根因分析

### 1. PostgreSQL bytea错误
**错误信息**:
```
ERROR: function lower(bytea) does not exist
建议：No function matches the given name and argument types. You might need to add explicit type casts.
```

**原因**: Spring Data JPA自动生成的查询中，`name`字段被当作`bytea`类型处理，而PostgreSQL的`lower()`函数不能直接用于`bytea`类型。

### 2. 查询方法问题
**问题方法**:
```java
// 这个方法会导致PostgreSQL错误
List<DataFile> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name);
```

## ✅ 修复方案

### 1. 修复PostgreSQL查询问题

**原始方法**:
```java
// 自动生成的查询，可能导致PostgreSQL错误
List<DataFile> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name);
```

**修复后**:
```java
// 使用自定义查询，明确指定字段类型
@Query("SELECT df FROM DataFile df WHERE LOWER(df.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY df.createdAt DESC")
List<DataFile> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(@Param("name") String name);
```

### 2. 修复统计API问题

**原始方法**:
```java
// 复杂的聚合查询，容易出错
@Query("SELECT COUNT(df) as totalFiles, SUM(CASE WHEN df.accessLevel = 'PUBLIC' THEN 1 ELSE 0 END) as publicFiles, ... FROM DataFile df")
Object[] getDataFileStatistics();
```

**修复后**:
```java
// 使用简单的Repository方法
long totalFiles = dataFileRepository.count();
long publicFiles = dataFileRepository.countByAccessLevel(DataFile.AccessLevel.PUBLIC);
long privateFiles = dataFileRepository.countByAccessLevel(DataFile.AccessLevel.PRIVATE);
```

### 3. 添加Repository方法

```java
// 根据访问级别统计数据文件数量
long countByAccessLevel(DataFile.AccessLevel accessLevel);
```

## 🚀 部署步骤

### 步骤1: 重新编译
```bash
cd /Users/clampist/work/JavaPro/backend
mvn clean compile
```

### 步骤2: 重启服务
```bash
# 停止现有服务
pkill -f "StaticDataPlatformApplication"

# 启动新服务
mvn spring-boot:run
```

### 步骤3: 验证修复
```bash
# 测试登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 测试组织节点API
curl -X GET "http://localhost:8080/api/organization/tree" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 测试数据文件统计API
curl -X GET "http://localhost:8080/api/data-files/statistics" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 测试数据文件查询API
curl -X POST "http://localhost:8080/api/data-files/query" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"page": 1, "size": 10}'
```

## 📊 预期结果

修复后，所有API应该正常返回：

1. **组织节点API**: 返回组织树结构
2. **统计API**: 返回正确的统计信息
3. **查询API**: 返回数据文件列表（可能为空）

## 🔍 故障排除

如果仍然有问题：

1. **检查编译**: 确保没有编译错误
2. **检查数据库**: 确保PostgreSQL连接正常
3. **检查日志**: 查看后端日志中的错误信息
4. **测试简单API**: 先测试其他API是否正常

## 📝 总结

通过以下改进解决了PostgreSQL相关问题：

1. ✅ **修复PostgreSQL查询**: 使用自定义查询避免bytea类型问题
2. ✅ **简化统计逻辑**: 使用简单的Repository方法
3. ✅ **改进错误处理**: 更好的异常处理
4. ✅ **优化查询性能**: 避免复杂的聚合查询

这个修复方案更加稳定和可靠，避免了PostgreSQL特定的问题。
