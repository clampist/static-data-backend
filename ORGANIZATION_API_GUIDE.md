# Organization Management API Guide
# 组织管理API指南

## Overview / 概述

This document provides comprehensive information about the Organization Management APIs for the Static Data Platform.

本文档提供了静态数据平台组织管理API的全面信息。

## Base URL / 基础URL

```
http://localhost:8080/api/organization
```

## Authentication / 认证

All endpoints require JWT authentication. Include the token in the Authorization header:

所有端点都需要JWT认证。在Authorization头中包含token：

```
Authorization: Bearer <your-jwt-token>
```

## API Endpoints / API端点

### 1. Get Organization Tree / 获取组织树

**Endpoint:** `GET /organization/tree`

**Description:** Get the complete organization tree structure / 获取完整的组织树结构

**Response:** Array of root organization nodes with nested children / 根组织节点数组，包含嵌套的子节点

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
        "createdAt": "2025-09-23T13:09:42.035749",
        "updatedAt": "2025-09-23T13:09:42.035755",
        "createdBy": "testuser",
        "updatedBy": "testuser",
        "childrenCount": 1,
        "dataFilesCount": 0
      }
    ],
    "createdAt": "2025-09-23T13:09:37.389768",
    "updatedAt": "2025-09-23T13:09:37.389783",
    "createdBy": "testuser",
    "updatedBy": "testuser",
    "childrenCount": 1,
    "dataFilesCount": 0
  }
]
```

### 2. Get Children by Parent ID / 根据父节点ID获取子节点

**Endpoint:** `GET /organization/nodes?parentId={parentId}`

**Parameters:**
- `parentId` (optional): Parent node ID. If not provided, returns root nodes / 父节点ID。如果不提供，返回根节点

**Response:** Array of child organization nodes / 子组织节点数组

### 3. Get Node Details / 获取节点详情

**Endpoint:** `GET /organization/nodes/{id}`

**Parameters:**
- `id`: Organization node ID / 组织节点ID

**Response:** Organization node details / 组织节点详情

### 4. Create Organization Node / 创建组织节点

**Endpoint:** `POST /organization/nodes`

**Request Body:**
```json
{
  "name": "产品部",
  "description": "产品研发部门",
  "type": "DEPARTMENT",
  "parentId": 1,
  "sortOrder": 1
}
```

**Required Fields:**
- `name`: Node name (2-50 characters) / 节点名称（2-50个字符）
- `type`: Node type (DEPARTMENT, TEAM, BUSINESS_DIRECTION, MODULE) / 节点类型

**Optional Fields:**
- `description`: Node description (max 200 characters) / 节点描述（最多200个字符）
- `parentId`: Parent node ID (null for root nodes) / 父节点ID（根节点为null）
- `sortOrder`: Sort order (default: 0) / 排序顺序（默认：0）

### 5. Update Organization Node / 更新组织节点

**Endpoint:** `PUT /organization/nodes/{id}`

**Request Body:**
```json
{
  "name": "前端开发团队",
  "description": "负责前端界面开发的团队",
  "sortOrder": 1
}
```

**Note:** Cannot change node type or parent relationship / 不能更改节点类型或父子关系

### 6. Delete Organization Node / 删除组织节点

**Endpoint:** `DELETE /organization/nodes/{id}`

**Response:** 204 No Content (successful deletion) / 204 无内容（删除成功）

**Note:** Cannot delete nodes that have children or associated data files / 不能删除有子节点或关联数据文件的节点

### 7. Search Organization Nodes / 搜索组织节点

**Endpoint:** `GET /organization/search?keyword={keyword}`

**Parameters:**
- `keyword`: Search keyword / 搜索关键词

**Response:** Array of matching organization nodes / 匹配的组织节点数组

### 8. Move Node / 移动节点

**Endpoint:** `PUT /organization/nodes/{id}/move`

**Request Body:**
```json
{
  "parentId": 1
}
```

**Note:** 
- `parentId` can be null to move to root level / `parentId`可以为null以移动到根级别
- Cannot create circular references / 不能创建循环引用

### 9. Get Node Types / 获取节点类型

**Endpoint:** `GET /organization/node-types`

**Response:** Array of available node types / 可用节点类型数组

```json
[
  "DEPARTMENT",
  "TEAM", 
  "BUSINESS_DIRECTION",
  "MODULE"
]
```

### 10. Get Node Statistics / 获取节点统计

**Endpoint:** `GET /organization/nodes/{id}/stats`

**Response:** Node statistics / 节点统计信息

```json
{
  "id": 2,
  "name": "产品部",
  "type": "DEPARTMENT",
  "childrenCount": 1,
  "dataFilesCount": 0,
  "createdAt": "2025-09-23T13:09:42.035749",
  "updatedAt": "2025-09-23T13:09:42.035755"
}
```

## Node Types / 节点类型

The organization structure follows a four-level hierarchy:

组织架构遵循四级层次结构：

1. **DEPARTMENT** - 部门
   - Top-level organizational units / 顶级组织单位
   - Can contain teams / 可以包含团队

2. **TEAM** - 团队
   - Groups within departments / 部门内的团队
   - Can contain business directions / 可以包含业务方向

3. **BUSINESS_DIRECTION** - 业务方向
   - Specific business areas within teams / 团队内的特定业务领域
   - Can contain modules / 可以包含模块

4. **MODULE** - 模块
   - Specific functional modules / 特定功能模块
   - Leaf nodes in the hierarchy / 层次结构中的叶节点

## Error Responses / 错误响应

### 400 Bad Request / 400 请求错误
```json
{
  "timestamp": "2025-09-23T13:10:00",
  "status": 400,
  "error": "Business Error",
  "message": "同一层级下已存在相同名称的节点: 产品部",
  "path": "/api/organization/nodes"
}
```

### 404 Not Found / 404 未找到
```json
{
  "timestamp": "2025-09-23T13:10:00",
  "status": 404,
  "error": "Resource Not Found",
  "message": "组织节点不存在: 999",
  "path": "/api/organization/nodes/999"
}
```

### 401 Unauthorized / 401 未授权
```json
{
  "timestamp": "2025-09-23T13:10:00",
  "status": 401,
  "error": "Authentication Failed",
  "message": "用户名或密码错误",
  "path": "/api/organization/tree"
}
```

## Business Rules / 业务规则

1. **Name Uniqueness** / 名称唯一性
   - Node names must be unique within the same parent level / 同一父级别下的节点名称必须唯一

2. **Hierarchy Validation** / 层次结构验证
   - Cannot create circular references when moving nodes / 移动节点时不能创建循环引用
   - Cannot change node type after creation / 创建后不能更改节点类型

3. **Deletion Constraints** / 删除约束
   - Cannot delete nodes with children / 不能删除有子节点的节点
   - Cannot delete nodes with associated data files / 不能删除有关联数据文件的节点

4. **Permission Requirements** / 权限要求
   - All operations require USER role or higher / 所有操作都需要USER角色或更高权限

## Testing / 测试

Use the provided test script to verify all API functionality:

使用提供的测试脚本来验证所有API功能：

```bash
./test-organization-apis.sh
```

## Swagger Documentation / Swagger文档

Interactive API documentation is available at:

交互式API文档可在以下位置找到：

```
http://localhost:8080/swagger-ui.html
```

## Example Usage / 使用示例

### Creating a Complete Organization Structure / 创建完整的组织架构

1. Create root department / 创建根部门
2. Create sub-departments / 创建子部门  
3. Create teams under departments / 在部门下创建团队
4. Create business directions under teams / 在团队下创建业务方向
5. Create modules under business directions / 在业务方向下创建模块

### Frontend Integration / 前端集成

The frontend can use these APIs to:
- Display the organization tree / 显示组织树
- Allow users to create/edit/delete nodes / 允许用户创建/编辑/删除节点
- Support drag-and-drop reorganization / 支持拖拽重组
- Implement search functionality / 实现搜索功能
