# 默认组织架构结构 (英文版)

## 概述

系统已成功初始化了一个完整的英文版组织架构，包含34个组织节点，采用4层树形结构：

- **DEPARTMENT** (部门) - 顶级部门
- **TEAM** (团队) - 部门下的团队
- **BUSINESS_DIRECTION** (业务方向) - 团队下的业务方向
- **MODULE** (模块) - 业务方向下的具体模块

## 组织架构层次

### 1. 总部 (Headquarters)
- **ID**: 1
- **类型**: DEPARTMENT
- **描述**: Main company headquarters and executive leadership

### 2. 产品部 (Product Department)
- **ID**: 2
- **父级**: Headquarters
- **类型**: DEPARTMENT
- **描述**: Product development, design, and innovation

#### 2.1 产品管理团队 (Product Management)
- **ID**: 6
- **类型**: TEAM
- **描述**: Product strategy, roadmap, and requirements

#### 2.2 UX/UI设计团队 (UX/UI Design)
- **ID**: 7
- **类型**: TEAM
- **描述**: User experience and interface design

#### 2.3 产品分析团队 (Product Analytics)
- **ID**: 8
- **类型**: TEAM
- **描述**: Data analysis and product insights

### 3. 工程部 (Engineering Department)
- **ID**: 3
- **父级**: Headquarters
- **类型**: DEPARTMENT
- **描述**: Software development and technical operations

#### 3.1 前端团队 (Frontend Team)
- **ID**: 9
- **类型**: TEAM
- **描述**: Frontend development and user interface

##### 3.1.1 网页开发 (Web Development)
- **ID**: 14
- **类型**: BUSINESS_DIRECTION
- **描述**: Web application development

###### 3.1.1.1 用户仪表板 (User Dashboard)
- **ID**: 20
- **类型**: MODULE
- **描述**: Main user interface and dashboard

###### 3.1.1.2 管理面板 (Admin Panel)
- **ID**: 21
- **类型**: MODULE
- **描述**: Administrative interface and controls

###### 3.1.1.3 身份验证 (Authentication)
- **ID**: 22
- **类型**: MODULE
- **描述**: Login, registration, and security

##### 3.1.2 移动开发 (Mobile Development)
- **ID**: 15
- **类型**: BUSINESS_DIRECTION
- **描述**: Mobile application development

###### 3.1.2.1 iOS应用 (iOS App)
- **ID**: 23
- **类型**: MODULE
- **描述**: Native iOS application

###### 3.1.2.2 Android应用 (Android App)
- **ID**: 24
- **类型**: MODULE
- **描述**: Native Android application

###### 3.1.2.3 React Native
- **ID**: 25
- **类型**: MODULE
- **描述**: Cross-platform mobile development

##### 3.1.3 设计系统 (Design System)
- **ID**: 16
- **类型**: BUSINESS_DIRECTION
- **描述**: Component library and design standards

#### 3.2 后端团队 (Backend Team)
- **ID**: 10
- **类型**: TEAM
- **描述**: Backend services and API development

##### 3.2.1 API开发 (API Development)
- **ID**: 17
- **类型**: BUSINESS_DIRECTION
- **描述**: RESTful APIs and microservices

###### 3.2.1.1 用户管理API (User Management API)
- **ID**: 26
- **类型**: MODULE
- **描述**: User authentication and profile management

###### 3.2.1.2 数据管理API (Data Management API)
- **ID**: 27
- **类型**: MODULE
- **描述**: Data file and content management

###### 3.2.1.3 组织架构API (Organization API)
- **ID**: 28
- **类型**: MODULE
- **描述**: Organizational structure management

##### 3.2.2 数据库管理 (Database Management)
- **ID**: 18
- **类型**: BUSINESS_DIRECTION
- **描述**: Database design and optimization

##### 3.2.3 集成服务 (Integration Services)
- **ID**: 19
- **类型**: BUSINESS_DIRECTION
- **描述**: Third-party integrations and middleware

#### 3.3 DevOps团队 (DevOps Team)
- **ID**: 11
- **类型**: TEAM
- **描述**: Infrastructure, deployment, and operations

#### 3.4 质量保证团队 (QA Team)
- **ID**: 12
- **类型**: TEAM
- **描述**: Quality assurance and testing

#### 3.5 数据工程团队 (Data Engineering)
- **ID**: 13
- **类型**: TEAM
- **描述**: Data pipeline and analytics infrastructure

### 4. 销售与市场部 (Sales & Marketing)
- **ID**: 4
- **父级**: Headquarters
- **类型**: DEPARTMENT
- **描述**: Sales, marketing, and customer acquisition

#### 4.1 销售团队 (Sales Team)
- **ID**: 29
- **类型**: TEAM
- **描述**: Direct sales and customer acquisition

#### 4.2 市场团队 (Marketing Team)
- **ID**: 30
- **类型**: TEAM
- **描述**: Brand marketing and lead generation

#### 4.3 客户成功团队 (Customer Success)
- **ID**: 31
- **类型**: TEAM
- **描述**: Customer onboarding and support

### 5. 运营部 (Operations)
- **ID**: 5
- **父级**: Headquarters
- **类型**: DEPARTMENT
- **描述**: Business operations, HR, and administration

#### 5.1 人力资源团队 (Human Resources)
- **ID**: 32
- **类型**: TEAM
- **描述**: Recruitment, benefits, and employee relations

#### 5.2 财务团队 (Finance)
- **ID**: 33
- **类型**: TEAM
- **描述**: Accounting, budgeting, and financial planning

#### 5.3 法务与合规团队 (Legal & Compliance)
- **ID**: 34
- **类型**: TEAM
- **描述**: Legal affairs and regulatory compliance

## 统计信息

- **总节点数**: 34个
- **部门数**: 5个 (包括总部)
- **团队数**: 15个
- **业务方向数**: 6个
- **模块数**: 8个

## API访问

### 获取组织架构树
```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/organization/tree
```

### 获取特定节点
```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/organization/1
```

### 创建新节点
```bash
curl -X POST -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"New Team","type":"TEAM","parentId":9}' \
  http://localhost:8080/api/organization
```

## 数据库初始化

组织架构数据通过以下方式初始化：

1. **SQL脚本**: `docker/postgres/init/02-organization-data.sql`
2. **API端点**: `/api/organization/init/default` (需要认证)
3. **重置端点**: `/api/organization/init/reset` (需要认证)

## 前端展示

组织架构在前端以树形结构展示，支持：
- 展开/折叠节点
- 显示节点类型标签
- 显示子节点数量
- 显示关联的数据文件数量

---
**创建时间**: 2025-09-30 03:40  
**状态**: 组织架构初始化完成 ✅
