# SonarQube 集成设置指南

本文档说明如何在 GitHub Actions 中集成 SonarQube 代码质量检查。

## 前置条件

1. **SonarQube 服务器**：你需要有一个可访问的 SonarQube 实例
   - 可以使用 SonarCloud (https://sonarcloud.io) - 免费的云端服务
   - 或者自建的 SonarQube 服务器

2. **项目配置**：在 SonarQube 中创建项目并获取必要的配置信息

## GitHub Secrets 配置

在 GitHub 仓库的 Settings > Secrets and variables > Actions 中添加以下 secrets：

### 必需的 Secrets

| Secret 名称 | 描述 | 示例值 |
|------------|------|--------|
| `SONAR_TOKEN` | SonarQube 认证令牌 | `squ_1234567890abcdef...` |
| `SONAR_HOST_URL` | SonarQube 服务器地址 | `https://sonarcloud.io` 或 `https://your-sonar-server.com` |

### 获取 SonarQube Token

#### 使用 SonarCloud (推荐)

1. 访问 [SonarCloud](https://sonarcloud.io)
2. 使用 GitHub 账号登录
3. 创建新组织 (如果还没有)
4. 导入 GitHub 仓库作为项目
5. 进入项目设置 > Administration > Analysis Method
6. 复制项目密钥和认证令牌

#### 使用自建 SonarQube

1. 登录 SonarQube 服务器
2. 进入 User > My Account > Security
3. 生成新的令牌
4. 记录令牌和服务器地址

## 项目配置

### 1. sonar-project.properties

项目根目录下的 `sonar-project.properties` 文件已配置好：

```properties
sonar.projectKey=static-data-platform-backend
sonar.projectName=Static Data Platform - Backend
sonar.projectVersion=1.0
```

### 2. pom.xml

已添加 SonarQube Maven 插件：

```xml
<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>3.11.0.3922</version>
</plugin>
```

## GitHub Actions 工作流

SonarQube 分析任务已集成到 CI/CD 流程中：

- **触发时机**：在单元测试和 API 测试完成后运行
- **依赖关系**：需要 `test` 和 `api-test` 任务成功完成
- **质量门禁**：配置了 `sonar.qualitygate.wait=true`，如果质量门禁失败，整个工作流会失败

## 工作流程

1. **代码提交**：推送到 main 或 develop 分支
2. **运行测试**：执行单元测试和 API 测试
3. **代码分析**：SonarQube 分析代码质量
4. **质量门禁**：检查是否通过质量门禁
5. **结果反馈**：在 GitHub 中显示分析结果

## 质量门禁配置

建议在 SonarQube 中配置以下质量门禁规则：

### 代码覆盖率
- 代码覆盖率 > 60%
- 分支覆盖率 > 50%

### 代码质量
- 新增代码覆盖率 > 80%
- 重复代码 < 3%
- 技术债务 < 5%

### 安全漏洞
- 安全热点 = 0
- 漏洞 = 0

### 代码异味
- 代码异味 < 20
- 严重代码异味 = 0

## 故障排除

### 常见问题

1. **认证失败**
   - 检查 `SONAR_TOKEN` 是否正确
   - 确认令牌有足够的权限

2. **项目不存在**
   - 检查 `SONAR_HOST_URL` 是否正确
   - 确认项目已在 SonarQube 中创建

3. **覆盖率报告缺失**
   - 确认 JaCoCo 插件正确配置
   - 检查测试是否正常执行

### 调试步骤

1. 查看 GitHub Actions 日志
2. 检查 SonarQube 项目页面
3. 验证配置文件格式
4. 确认所有依赖服务正常运行

## 本地测试

你可以在本地运行 SonarQube 分析：

```bash
# 设置环境变量
export SONAR_TOKEN=your_token_here
export SONAR_HOST_URL=https://sonarcloud.io

# 运行分析
./mvnw sonar:sonar
```

## 更多信息

- [SonarQube Maven Plugin 文档](https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-maven/)
- [SonarCloud 文档](https://docs.sonarcloud.io/)
- [GitHub Actions 集成指南](https://docs.sonarcloud.io/advanced-setup/ci-based-analysis/github-actions/)
