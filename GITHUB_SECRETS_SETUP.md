# GitHub Repository Secrets 配置指南

## 📋 需要在GitHub Repository中设置的Secrets

### 🔧 设置步骤：
1. 进入你的GitHub仓库
2. 点击 **Settings** 标签
3. 在左侧菜单中找到 **Secrets and variables** → **Actions**
4. 点击 **Repository secrets** 标签
5. 点击 **New repository secret** 按钮
6. 添加以下每个secret：

### 📝 必需的Repository Secrets：

> **注意**: 根据当前配置，只有最敏感的信息使用secrets，其他配置都使用明文。

#### 1. 数据库密码（必需）
```
Name: DATABASE_PASSWORD
Value: aP5WeC8GNHzQXfvNyEIrDnCkkmi4Jc8g
```

#### 2. JWT密钥（必需）
```
Name: JWT_SECRET
Value: JTWcdW5n2XI5NSFNAsTNNZ0lLgX4VGq9kI1oBAS0xUr0N57xlhYMDZhLSkjNCalGcoLr0Qb4HmufSIuXvMA
```

### 📋 明文配置（不需要secrets）：

以下配置在workflow中直接使用明文值：

- **数据库URL**: `jdbc:postgresql://localhost:5432/static_data_platform_test`
- **数据库用户名**: `sdp_user`
- **JWT过期时间**: `86400000`
- **Redis主机**: `localhost`
- **Redis端口**: `6379`
- **CORS允许的源**: `http://localhost:3000,http://localhost:5173`

### 🎯 生产环境Secrets（可选）：

如果你有生产环境，还需要添加：

```
Name: PROD_DATABASE_URL
Value: [你的生产数据库URL]
```

```
Name: PROD_JWT_SECRET
Value: [你的生产JWT密钥]
```

## ✅ 验证配置

设置完所有secrets后，你的GitHub Actions workflow应该能够：

1. ✅ 成功启动PostgreSQL服务
2. ✅ 成功启动Redis服务  
3. ✅ 正确连接数据库
4. ✅ 运行所有测试
5. ✅ 生成测试报告

## 🔍 故障排除

如果仍然遇到问题，请检查：

1. **Secret名称**：确保名称完全匹配（区分大小写）
2. **Secret值**：确保没有多余的空格或特殊字符
3. **权限**：确保workflow有权限访问这些secrets
4. **分支**：确保在正确的分支上运行workflow

## 📊 当前Workflow使用的Secrets

根据 `.github/workflows/ci.yml` 配置，以下secrets被使用：

- `DATABASE_URL` - 数据库连接URL
- `DATABASE_USERNAME` - 数据库用户名
- `DATABASE_PASSWORD` - 数据库密码（也用于PostgreSQL服务）
- `JWT_SECRET` - JWT签名密钥
- `JWT_EXPIRATION` - JWT过期时间（可选，有默认值）
- `REDIS_HOST` - Redis主机（可选，默认为localhost）
- `REDIS_PORT` - Redis端口（可选，默认为6379）
- `CORS_ALLOWED_ORIGINS` - CORS允许的源（可选，有默认值）

---
**创建时间**: 2025-09-30  
**状态**: GitHub Secrets配置指南完成 ✅
