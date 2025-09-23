# Data File API Guide
# 数据文件API指南

## Overview / 概述

This document describes the Data File Management API endpoints for the Static Data Platform. Data files must be attached to MODULE-type organization nodes and support multiple data types.

本文档描述了静态数据平台的数据文件管理API端点。数据文件必须挂在MODULE类型的组织节点下，并支持多种数据类型。

## 🚀 Quick Start / 快速开始

### Prerequisites / 前置条件

1. **Authentication Required** / 需要认证
   - All endpoints require JWT authentication
   - 所有端点都需要JWT认证

2. **Organization Structure** / 组织架构
   - Data files can only be created under MODULE-type organization nodes
   - 数据文件只能在MODULE类型的组织节点下创建

### Base URL / 基础URL
```
http://localhost:8080/api/data-files
```

## 📋 API Endpoints / API端点

### 1. Create Data File / 创建数据文件

**POST** `/data-files`

Creates a new data file attached to a MODULE organization node.
创建新的数据文件，挂在MODULE组织节点下。

#### Request Body / 请求体
```json
{
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
}
```

#### Response / 响应
```json
{
  "id": 1,
  "name": "用户基础数据表",
  "description": "存储用户基础信息的数据表",
  "fileHash": "abc123def456",
  "organizationNodeId": 1,
  "organizationNodeName": "用户管理模块",
  "organizationNodePath": "总公司/产品部/前端团队/用户管理模块",
  "ownerId": 1,
  "ownerName": "张三",
  "accessLevel": "PRIVATE",
  "columnDefinitions": [...],
  "dataRows": [...],
  "rowCount": 1,
  "columnCount": 2,
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00",
  "createdBy": "testuser",
  "updatedBy": "testuser",
  "versionCount": 0,
  "lastModifiedBy": "testuser",
  "lastModifiedAt": "2024-01-01T10:00:00"
}
```

### 2. Update Data File / 更新数据文件

**PUT** `/data-files/{id}`

Updates an existing data file. Only the owner can update.
更新现有数据文件。只有所有者可以更新。

#### Request Body / 请求体
```json
{
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
}
```

### 3. Delete Data File / 删除数据文件

**DELETE** `/data-files/{id}`

Deletes a data file. Only the owner can delete.
删除数据文件。只有所有者可以删除。

#### Response / 响应
- **204 No Content** - Successfully deleted / 成功删除

### 4. Get Data File by ID / 根据ID获取数据文件

**GET** `/data-files/{id}`

Retrieves a single data file by its ID.
根据ID获取单个数据文件。

#### Access Control / 访问控制
- Public files: Accessible by all authenticated users
- 公开文件：所有认证用户可访问
- Private files: Only accessible by the owner
- 私有文件：只有所有者可访问

### 5. Query Data Files / 查询数据文件

**POST** `/data-files/query`

Queries data files with various conditions and pagination.
使用各种条件和分页查询数据文件。

#### Request Body / 请求体
```json
{
  "name": "用户",
  "organizationNodeId": 1,
  "ownerId": 1,
  "accessLevel": "PUBLIC",
  "dataType": "STRING",
  "page": 1,
  "size": 10,
  "sortBy": "createdAt",
  "sortDirection": "desc"
}
```

#### Response / 响应
```json
{
  "content": [...],
  "pageable": {...},
  "totalElements": 10,
  "totalPages": 1,
  "size": 10,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 10
}
```

### 6. Get Data Files by Organization Node / 根据组织节点获取数据文件

**GET** `/data-files/organization/{organizationNodeId}`

Retrieves all data files under a specific organization node.
获取特定组织节点下的所有数据文件。

### 7. Get Data Files by Owner / 根据所有者获取数据文件

**GET** `/data-files/owner/{ownerId}`

Retrieves all data files owned by a specific user.
获取特定用户拥有的所有数据文件。

### 8. Search Data Files / 搜索数据文件

**GET** `/data-files/search?keyword={keyword}`

Searches data files by name keyword.
根据名称关键词搜索数据文件。

### 9. Get Data Files by Data Type / 根据数据类型获取数据文件

**GET** `/data-files/data-type/{dataType}`

Retrieves data files containing columns of a specific data type.
获取包含特定数据类型列的数据文件。

#### Supported Data Types / 支持的数据类型
- `STRING` - 字符串
- `INTEGER` - 整数
- `DECIMAL` - 小数
- `BOOLEAN` - 布尔值
- `DATE` - 日期
- `DATETIME` - 日期时间
- `JSON` - JSON对象

### 10. Get Recent Data Files / 获取最近的数据文件

**GET** `/data-files/recent?limit={limit}`

Retrieves recently created data files.
获取最近创建的数据文件。

### 11. Get Data File Statistics / 获取数据文件统计信息

**GET** `/data-files/statistics`

Retrieves statistics about data files in the system.
获取系统中数据文件的统计信息。

#### Response / 响应
```json
{
  "totalFiles": 100,
  "publicFiles": 60,
  "privateFiles": 40,
  "avgRowCount": 150.5,
  "avgColumnCount": 8.2
}
```

### 12. Get Accessible Data Files / 获取可访问的数据文件

**GET** `/data-files/accessible?page={page}&size={size}`

Retrieves all data files accessible by the current user (public files + owned files).
获取当前用户可访问的所有数据文件（公开文件 + 拥有的文件）。

### 13. Get Supported Data Types / 获取支持的数据类型

**GET** `/data-files/data-types`

Retrieves all supported data types.
获取所有支持的数据类型。

## 🔧 Data Types / 数据类型

### Column Definition / 列定义

Each column in a data file has the following properties:
数据文件中的每个列都有以下属性：

```json
{
  "name": "column_name",           // 列名
  "dataType": "STRING",            // 数据类型
  "required": true,                // 是否必填
  "defaultValue": "",              // 默认值
  "maxLength": 50,                 // 最大长度
  "description": "列描述",         // 列描述
  "validationRule": "^[a-zA-Z0-9_]+$", // 验证规则
  "sortOrder": 1                   // 列排序
}
```

### Supported Data Types / 支持的数据类型

| Type | Description | Example |
|------|-------------|---------|
| STRING | Text data | "Hello World" |
| INTEGER | Whole numbers | 123, -456 |
| DECIMAL | Decimal numbers | 123.45, -67.89 |
| BOOLEAN | True/False values | true, false |
| DATE | Date only | "2024-01-01" |
| DATETIME | Date and time | "2024-01-01T10:30:00" |
| JSON | JSON objects | {"key": "value"} |

## 🔐 Access Control / 访问控制

### Access Levels / 访问级别

- **PUBLIC**: Accessible by all authenticated users
- **公开**：所有认证用户可访问
- **PRIVATE**: Only accessible by the owner
- **私有**：只有所有者可访问

### Permission Rules / 权限规则

1. **Create**: Any authenticated user can create data files
   - 创建：任何认证用户都可以创建数据文件

2. **Read**: 
   - Public files: All authenticated users
   - Private files: Owner only
   - 读取：
     - 公开文件：所有认证用户
     - 私有文件：只有所有者

3. **Update**: Owner only
   - 更新：只有所有者

4. **Delete**: Owner only
   - 删除：只有所有者

## 📊 Business Rules / 业务规则

### File Creation Rules / 文件创建规则

1. **Organization Node Requirement**: Data files must be attached to MODULE-type organization nodes
   - 组织节点要求：数据文件必须挂在MODULE类型的组织节点下

2. **Unique Naming**: File names must be unique within the same organization node
   - 唯一命名：在同一组织节点下文件名必须唯一

3. **Column Definitions**: At least one column definition is recommended
   - 列定义：建议至少有一个列定义

4. **Data Validation**: Data rows are validated against column definitions
   - 数据验证：数据行根据列定义进行验证

### File Management Rules / 文件管理规则

1. **Ownership**: Files are owned by the user who created them
   - 所有权：文件由创建它们的用户拥有

2. **Version Control**: Each update creates a new version (implemented via DataFileVersion)
   - 版本控制：每次更新都会创建新版本（通过DataFileVersion实现）

3. **File Hash**: Each file has a unique hash based on its content
   - 文件哈希：每个文件都有基于其内容的唯一哈希

## 🧪 Testing / 测试

### Test Script / 测试脚本

Run the test script to verify all endpoints:
运行测试脚本来验证所有端点：

```bash
./test-datafile-apis.sh
```

### Manual Testing with curl / 使用curl手动测试

#### 1. Login / 登录
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}'
```

#### 2. Create Data File / 创建数据文件
```bash
curl -X POST http://localhost:8080/api/data-files \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "测试数据表",
    "description": "测试用数据表",
    "organizationNodeId": 1,
    "accessLevel": "PRIVATE",
    "columnDefinitions": [
      {
        "name": "id",
        "dataType": "INTEGER",
        "required": true,
        "sortOrder": 1
      }
    ],
    "dataRows": [
      {"id": 1}
    ]
  }'
```

#### 3. Query Data Files / 查询数据文件
```bash
curl -X POST http://localhost:8080/api/data-files/query \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "测试",
    "page": 1,
    "size": 10
  }'
```

## 🚨 Error Handling / 错误处理

### Common Error Responses / 常见错误响应

#### 400 Bad Request / 请求错误
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "数据文件只能挂在功能模块下，当前节点类型为: DEPARTMENT",
  "path": "/api/data-files"
}
```

#### 403 Forbidden / 禁止访问
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "只有文件所有者可以修改数据文件",
  "path": "/api/data-files/1"
}
```

#### 404 Not Found / 未找到
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "数据文件不存在，ID: 999",
  "path": "/api/data-files/999"
}
```

## 📈 Performance Considerations / 性能考虑

### Optimization Tips / 优化建议

1. **Pagination**: Use pagination for large result sets
   - 分页：对大型结果集使用分页

2. **Indexing**: Database indexes are created on frequently queried fields
   - 索引：在经常查询的字段上创建数据库索引

3. **Caching**: Consider caching frequently accessed data files
   - 缓存：考虑缓存经常访问的数据文件

4. **Lazy Loading**: Related entities are loaded lazily to improve performance
   - 懒加载：相关实体懒加载以提高性能

## 🔄 Integration with Organization Management / 与组织管理集成

### Module Requirement / 模块要求

Data files are tightly integrated with the organization management system:
数据文件与组织管理系统紧密集成：

1. **Module Validation**: Files can only be created under MODULE-type nodes
   - 模块验证：文件只能在MODULE类型节点下创建

2. **Path Information**: File responses include the full organization path
   - 路径信息：文件响应包含完整的组织路径

3. **Hierarchical Queries**: Can query files by organization hierarchy
   - 层次查询：可以按组织层次查询文件

## 🎯 Use Cases / 用例

### Typical Usage Patterns / 典型使用模式

1. **Data Storage**: Store structured data with schema validation
   - 数据存储：存储具有模式验证的结构化数据

2. **Configuration Management**: Store application configuration data
   - 配置管理：存储应用程序配置数据

3. **User Data**: Store user-related information and preferences
   - 用户数据：存储用户相关信息和偏好

4. **Business Data**: Store business-specific data and metrics
   - 业务数据：存储特定业务数据和指标

## 🚀 Future Enhancements / 未来增强

### Planned Features / 计划功能

1. **File Import/Export**: Support for Excel, CSV import/export
   - 文件导入/导出：支持Excel、CSV导入/导出

2. **Advanced Validation**: More sophisticated data validation rules
   - 高级验证：更复杂的数据验证规则

3. **Data Transformation**: Built-in data transformation capabilities
   - 数据转换：内置数据转换功能

4. **Real-time Collaboration**: Multiple users editing the same file
   - 实时协作：多用户编辑同一文件

5. **Audit Trail**: Detailed audit logs for data changes
   - 审计跟踪：数据变更的详细审计日志

## 📚 Conclusion / 结论

The Data File API provides a comprehensive solution for managing structured data files within the organization hierarchy. It ensures data integrity, access control, and provides flexible querying capabilities.

数据文件API为在组织层次结构内管理结构化数据文件提供了全面的解决方案。它确保数据完整性、访问控制，并提供灵活的查询功能。

Key benefits:
主要优势：

- ✅ **Structured Data Management**: Schema-based data storage with validation
- ✅ **结构化数据管理**：基于模式的数据存储和验证
- ✅ **Access Control**: Fine-grained permissions for data security
- ✅ **访问控制**：细粒度权限确保数据安全
- ✅ **Organization Integration**: Tightly integrated with organization structure
- ✅ **组织集成**：与组织结构紧密集成
- ✅ **Multiple Data Types**: Support for various data types and formats
- ✅ **多种数据类型**：支持各种数据类型和格式
- ✅ **Flexible Querying**: Advanced query and search capabilities
- ✅ **灵活查询**：高级查询和搜索功能
