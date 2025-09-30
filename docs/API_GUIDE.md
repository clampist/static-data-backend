# 静态数据平台 API 完整指南
# Static Data Platform API Complete Guide

## 📋 目录 / Table of Contents

- [概述 / Overview](#概述--overview)
- [快速开始 / Quick Start](#快速开始--quick-start)
- [认证API / Authentication APIs](#认证api--authentication-apis)
- [组织管理API / Organization Management APIs](#组织管理api--organization-management-apis)
- [数据文件API / Data File APIs](#数据文件api--data-file-apis)
- [API测试 / API Testing](#api测试--api-testing)
- [API覆盖率 / API Coverage](#api覆盖率--api-coverage)
- [错误处理 / Error Handling](#错误处理--error-handling)
- [故障排除 / Troubleshooting](#故障排除--troubleshooting)

## 概述 / Overview

本文档提供了静态数据平台所有API端点的完整使用指南，包括认证、组织管理、数据文件管理等核心功能。

This document provides a complete guide for all API endpoints of the Static Data Platform, including authentication, organization management, data file management, and other core features.

### 基础信息 / Basic Information

- **基础URL**: `http://localhost:8080/api`
- **认证方式**: JWT Bearer Token
- **内容类型**: `application/json`
- **API文档**: http://localhost:8080/api/swagger-ui.html

## 快速开始 / Quick Start

### 前置条件 / Prerequisites

1. **启动后端服务**
   ```bash
   cd /Users/clampist/Workspace/Java/JavaPro/backend
   mvn spring-boot:run
   ```

2. **验证服务状态**
   ```bash
   curl http://localhost:8080/api/actuator/health
   ```
   预期响应: `{"status":"UP"}`

### 默认测试账户 / Default Test Accounts

系统提供了以下测试账户：

#### 管理员账户
- **用户名**: `admin`
- **密码**: `admin123`
- **邮箱**: `admin@example.com`
- **角色**: `USER`

#### 测试用户账户
- **用户名**: `testuser`
- **密码**: `password123`
- **邮箱**: `testuser@example.com`
- **角色**: `USER`

> ⚠️ **安全提醒**: 这些是开发环境的默认账户，不要在生产环境使用

### 快速测试 / Quick Testing

使用提供的测试脚本快速验证所有API：

```bash
# 认证API测试
./api-tests/test-auth-apis.sh

# 组织管理API测试
./api-tests/test-organization-apis.sh

# 数据文件API测试
./api-tests/test-final-datafile.sh

# 完整API测试
./api-tests/api-coverage-test.sh
```

## 认证API / Authentication APIs

### 基础URL
```
http://localhost:8080/api/auth
```

### 1. 用户注册 / User Registration

**POST** `/auth/register`

```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "testuser@example.com",
    "password": "password123",
    "confirmPassword": "password123",
    "fullName": "Test User"
  }'
```

**响应示例**:
```json
{
  "id": 1,
  "username": "testuser",
  "email": "testuser@example.com",
  "fullName": "Test User",
  "role": "USER",
  "enabled": true,
  "createdAt": "2024-01-01T10:00:00"
}
```

### 2. 用户登录 / User Login

**POST** `/auth/login`

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

**响应示例**:
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "testuser@example.com",
    "fullName": "Test User",
    "role": "USER",
    "enabled": true
  }
}
```

### 3. Token验证 / Token Validation

**GET** `/auth/validate`

```bash
curl -X GET "http://localhost:8080/api/auth/validate" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4. 获取当前用户 / Get Current User

**GET** `/auth/me`

```bash
curl -X GET "http://localhost:8080/api/auth/me" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 5. 刷新Token / Refresh Token

**POST** `/auth/refresh`

```bash
curl -X POST "http://localhost:8080/api/auth/refresh" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 6. 检查用户名可用性 / Check Username Availability

**GET** `/auth/check-username?username=newuser`

```bash
curl -X GET "http://localhost:8080/api/auth/check-username?username=newuser"
```

### 7. 检查邮箱可用性 / Check Email Availability

**GET** `/auth/check-email?email=newuser@example.com`

```bash
curl -X GET "http://localhost:8080/api/auth/check-email?email=newuser@example.com"
```

## 组织管理API / Organization Management APIs

### 基础URL
```
http://localhost:8080/api/organization
```

### 节点类型 / Node Types

组织架构遵循四级层次结构：

1. **DEPARTMENT** - 部门（顶级组织单位）
2. **TEAM** - 团队（部门内的团队）
3. **BUSINESS_DIRECTION** - 业务方向（团队内的特定业务领域）
4. **MODULE** - 模块（特定功能模块，叶节点）

### 1. 获取组织树 / Get Organization Tree

**GET** `/organization/tree`

```bash
curl -X GET "http://localhost:8080/api/organization/tree" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**响应示例**:
```json
[
  {
    "id": 1,
    "name": "总公司",
    "description": "公司总部",
    "type": "DEPARTMENT",
    "parentId": null,
    "parentName": null,
    "sortOrder": 1,
    "children": [
      {
        "id": 2,
        "name": "产品部",
        "description": "产品研发部门",
        "type": "DEPARTMENT",
        "parentId": 1,
        "parentName": "总公司",
        "sortOrder": 1,
        "children": [...],
        "childrenCount": 1,
        "dataFilesCount": 0
      }
    ],
    "childrenCount": 1,
    "dataFilesCount": 0
  }
]
```

### 2. 获取子节点 / Get Children by Parent ID

**GET** `/organization/nodes?parentId={parentId}`

```bash
curl -X GET "http://localhost:8080/api/organization/nodes?parentId=1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3. 获取节点详情 / Get Node Details

**GET** `/organization/nodes/{id}`

```bash
curl -X GET "http://localhost:8080/api/organization/nodes/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4. 创建组织节点 / Create Organization Node

**POST** `/organization/nodes`

```bash
curl -X POST "http://localhost:8080/api/organization/nodes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "产品部",
    "description": "产品研发部门",
    "type": "DEPARTMENT",
    "parentId": 1,
    "sortOrder": 1
  }'
```

**必填字段**:
- `name`: 节点名称（2-50个字符）
- `type`: 节点类型（DEPARTMENT, TEAM, BUSINESS_DIRECTION, MODULE）

**可选字段**:
- `description`: 节点描述（最多200个字符）
- `parentId`: 父节点ID（根节点为null）
- `sortOrder`: 排序顺序（默认：0）

### 5. 更新组织节点 / Update Organization Node

**PUT** `/organization/nodes/{id}`

```bash
curl -X PUT "http://localhost:8080/api/organization/nodes/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "前端开发团队",
    "description": "负责前端界面开发的团队",
    "sortOrder": 1
  }'
```

**注意**: 不能更改节点类型或父子关系

### 6. 删除组织节点 / Delete Organization Node

**DELETE** `/organization/nodes/{id}`

```bash
curl -X DELETE "http://localhost:8080/api/organization/nodes/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**注意**: 不能删除有子节点或关联数据文件的节点

### 7. 搜索组织节点 / Search Organization Nodes

**GET** `/organization/search?keyword={keyword}`

```bash
curl -X GET "http://localhost:8080/api/organization/search?keyword=产品" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 8. 移动节点 / Move Node

**PUT** `/organization/nodes/{id}/move`

```bash
curl -X PUT "http://localhost:8080/api/organization/nodes/1/move" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "parentId": 2
  }'
```

### 9. 获取节点类型 / Get Node Types

**GET** `/organization/node-types`

```bash
curl -X GET "http://localhost:8080/api/organization/node-types" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 10. 获取节点统计 / Get Node Statistics

**GET** `/organization/nodes/{id}/stats`

```bash
curl -X GET "http://localhost:8080/api/organization/nodes/1/stats" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 数据文件API / Data File APIs

### 基础URL
```
http://localhost:8080/api/data-files
```

### 重要说明 / Important Notes

- **认证要求**: 所有端点都需要JWT认证
- **组织节点要求**: 数据文件只能挂在MODULE类型的组织节点下
- **访问控制**: 支持PUBLIC（公开）和PRIVATE（私有）两种访问级别

### 1. 创建数据文件 / Create Data File

**POST** `/data-files`

```bash
curl -X POST "http://localhost:8080/api/data-files" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "用户基础数据表",
    "description": "存储用户基础信息的数据表",
    "organizationNodeId": 1,
    "accessLevel": "PRIVATE",
    "columnDefinitions": [
      {
        "name": "id",
        "dataType": "INTEGER",
        "required": true,
        "defaultValue": null,
        "maxLength": null,
        "description": "用户唯一标识",
        "validationRule": null,
        "sortOrder": 1
      },
      {
        "name": "username",
        "dataType": "STRING",
        "required": true,
        "defaultValue": "",
        "maxLength": 50,
        "description": "用户登录名",
        "validationRule": "^[a-zA-Z0-9_]+$",
        "sortOrder": 2
      }
    ],
    "dataRows": [
      {
        "id": 1,
        "username": "john_doe",
        "email": "john@example.com"
      }
    ]
  }'
```

### 2. 更新数据文件 / Update Data File

**PUT** `/data-files/{id}`

```bash
curl -X PUT "http://localhost:8080/api/data-files/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "用户基础数据表（更新版）",
    "description": "更新后的用户基础信息数据表",
    "accessLevel": "PUBLIC",
    "dataRows": [
      {
        "id": 1,
        "username": "john_doe_updated",
        "email": "john.updated@example.com"
      }
    ]
  }'
```

### 3. 删除数据文件 / Delete Data File

**DELETE** `/data-files/{id}`

```bash
curl -X DELETE "http://localhost:8080/api/data-files/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4. 根据ID获取数据文件 / Get Data File by ID

**GET** `/data-files/{id}`

```bash
curl -X GET "http://localhost:8080/api/data-files/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 5. 查询数据文件 / Query Data Files

**POST** `/data-files/query`

```bash
curl -X POST "http://localhost:8080/api/data-files/query" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "用户",
    "organizationNodeId": 1,
    "ownerId": 1,
    "accessLevel": "PUBLIC",
    "dataType": "STRING",
    "page": 1,
    "size": 10,
    "sortBy": "createdAt",
    "sortDirection": "desc"
  }'
```

### 6. 根据组织节点获取数据文件 / Get Data Files by Organization Node

**GET** `/data-files/organization/{organizationNodeId}`

```bash
curl -X GET "http://localhost:8080/api/data-files/organization/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 7. 根据所有者获取数据文件 / Get Data Files by Owner

**GET** `/data-files/owner/{ownerId}`

```bash
curl -X GET "http://localhost:8080/api/data-files/owner/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 8. 搜索数据文件 / Search Data Files

**GET** `/data-files/search?keyword={keyword}`

```bash
curl -X GET "http://localhost:8080/api/data-files/search?keyword=用户" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 9. 根据数据类型获取数据文件 / Get Data Files by Data Type

**GET** `/data-files/data-type/{dataType}`

```bash
curl -X GET "http://localhost:8080/api/data-files/data-type/STRING" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**支持的数据类型**:
- `STRING` - 字符串
- `INTEGER` - 整数
- `DECIMAL` - 小数
- `BOOLEAN` - 布尔值
- `DATE` - 日期
- `DATETIME` - 日期时间
- `JSON` - JSON对象

### 10. 获取最近的数据文件 / Get Recent Data Files

**GET** `/data-files/recent?limit={limit}`

```bash
curl -X GET "http://localhost:8080/api/data-files/recent?limit=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 11. 获取数据文件统计信息 / Get Data File Statistics

**GET** `/data-files/statistics`

```bash
curl -X GET "http://localhost:8080/api/data-files/statistics" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**响应示例**:
```json
{
  "totalFiles": 100,
  "publicFiles": 60,
  "privateFiles": 40,
  "avgRowCount": 150.5,
  "avgColumnCount": 8.2
}
```

### 12. 获取可访问的数据文件 / Get Accessible Data Files

**GET** `/data-files/accessible?page={page}&size={size}`

```bash
curl -X GET "http://localhost:8080/api/data-files/accessible?page=1&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 13. 获取支持的数据类型 / Get Supported Data Types

**GET** `/data-files/data-types`

```bash
curl -X GET "http://localhost:8080/api/data-files/data-types" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## API测试 / API Testing

### 自动化测试脚本

项目提供了完整的API测试脚本：

```bash
# 认证API测试
./api-tests/test-auth-apis.sh

# 组织管理API测试
./api-tests/test-organization-apis.sh

# 数据文件API测试
./api-tests/test-final-datafile.sh

# 前端集成测试
./api-tests/test-frontend-integration.sh

# 完整API覆盖率测试
./api-tests/api-coverage-test.sh
```

### Postman测试

1. **导入集合**
   - 导入 `postman-collection.json`
   - 导入 `postman-environment.json`

2. **设置环境**
   - 选择 "Static Data Platform - Local Environment"

3. **运行测试**
   - 手动测试：按顺序运行各个请求
   - 自动测试：运行整个集合

### 测试场景覆盖

#### 成功流程 / Success Flows
- ✅ 用户注册和登录
- ✅ Token验证和刷新
- ✅ 组织节点CRUD操作
- ✅ 数据文件CRUD操作
- ✅ 用户名/邮箱可用性检查

#### 错误处理 / Error Handling
- ❌ 无效输入数据
- ❌ 重复用户名/邮箱
- ❌ 无效凭据
- ❌ 无效Token
- ❌ 无Token访问受保护端点

## API覆盖率 / API Coverage

### 覆盖率配置

项目已配置JaCoCo代码覆盖率分析，支持：

- **单元测试覆盖率**
- **集成测试覆盖率**
- **API接口覆盖率**
- **实时覆盖率监控**

### 生成覆盖率报告

```bash
# 运行测试并生成覆盖率报告
mvn clean test jacoco:report

# 查看覆盖率报告
open target/site/jacoco/index.html

# 使用专用脚本
./ut-scripts/generate-coverage-report.sh
```

### 覆盖率目标

- **API Controller层**: ≥ 80%
- **API Service层**: ≥ 75%
- **Security层**: ≥ 70%
- **整体项目**: ≥ 65%

### 当前覆盖率状态

- **指令覆盖率**: 69.0%
- **分支覆盖率**: 83.6%

## 错误处理 / Error Handling

### 标准错误响应格式

```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "具体错误信息",
  "path": "/api/endpoint"
}
```

### 常见HTTP状态码

- **200 OK**: 请求成功
- **201 Created**: 资源创建成功
- **204 No Content**: 删除成功
- **400 Bad Request**: 请求参数错误
- **401 Unauthorized**: 未认证
- **403 Forbidden**: 无权限
- **404 Not Found**: 资源不存在
- **409 Conflict**: 资源冲突
- **500 Internal Server Error**: 服务器内部错误

### 业务错误示例

#### 400 Bad Request
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 400,
  "error": "Business Error",
  "message": "数据文件只能挂在功能模块下，当前节点类型为: DEPARTMENT",
  "path": "/api/data-files"
}
```

#### 403 Forbidden
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "只有文件所有者可以修改数据文件",
  "path": "/api/data-files/1"
}
```

#### 404 Not Found
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 404,
  "error": "Resource Not Found",
  "message": "数据文件不存在，ID: 999",
  "path": "/api/data-files/999"
}
```

## 故障排除 / Troubleshooting

### 常见问题

#### 1. 连接被拒绝 / Connection Refused
**症状**: `curl: (7) Failed to connect to localhost port 8080: Connection refused`

**解决方案**:
- 确保Spring Boot应用正在运行
- 检查端口8080是否被占用
- 验证应用日志中的启动错误

#### 2. 401未授权 / 401 Unauthorized
**症状**: `{"status":401,"error":"Unauthorized"}`

**解决方案**:
- 检查Authorization头格式：`Bearer <token>`
- 验证Token是否有效且未过期
- 确保已正确登录获取Token

#### 3. 400请求错误 / 400 Bad Request
**症状**: `{"status":400,"error":"Bad Request"}`

**解决方案**:
- 验证请求体JSON格式
- 检查必填字段是否提供
- 验证数据类型是否匹配

#### 4. 数据库连接问题 / Database Connection Issues
**症状**: 应用启动失败或数据库相关错误

**解决方案**:
- 确保PostgreSQL正在运行
- 验证数据库连接配置
- 检查数据库是否存在

### 调试步骤

#### 1. 检查应用日志
```bash
tail -f logs/application.log
```

#### 2. 验证数据库连接
```bash
psql -h localhost -U sdp_user -d static_data_platform_dev
```

#### 3. 测试健康端点
```bash
curl http://localhost:8080/api/actuator/health
```

#### 4. 查看Swagger文档
访问: http://localhost:8080/api/swagger-ui.html

### 性能优化建议

1. **分页**: 对大型结果集使用分页
2. **索引**: 数据库索引已创建在经常查询的字段上
3. **缓存**: 考虑缓存经常访问的数据文件
4. **懒加载**: 相关实体懒加载以提高性能

## 安全注意事项 / Security Notes

- 🔒 所有密码使用BCrypt加密
- 🔑 JWT Token默认24小时过期
- 🛡️ CORS已配置用于localhost开发
- 🚫 公共端点不需要认证
- 📝 所有API交互都有日志记录用于监控

## 总结 / Summary

本指南涵盖了静态数据平台的所有API端点，包括：

- ✅ **认证管理**: 用户注册、登录、Token管理
- ✅ **组织管理**: 四级层次结构的组织节点管理
- ✅ **数据文件管理**: 结构化数据文件的完整生命周期管理
- ✅ **测试支持**: 完整的测试脚本和覆盖率分析
- ✅ **错误处理**: 统一的错误响应格式和处理机制
- ✅ **故障排除**: 常见问题的解决方案和调试步骤

通过本指南，您可以：
1. 快速了解所有可用的API端点
2. 使用提供的测试脚本验证API功能
3. 集成API到前端应用或其他系统
4. 解决常见的API使用问题
5. 监控API的测试覆盖率

---

**最后更新**: 2024-01-01  
**版本**: 1.0.0  
**状态**: 所有API正常工作 🚀
