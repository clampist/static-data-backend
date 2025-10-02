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
| `SONAR_TOKEN` | SonarCloud 认证令牌 | `squ_1234567890abcdef...` |

> **注意**：使用 SonarCloud 时不需要配置 `SONAR_HOST_URL`，系统会自动使用 `https://sonarcloud.io`。

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
sonar.projectKey=clampist_static-data-backend
sonar.projectName=Static Data Platform - Backend
sonar.projectVersion=1.0
sonar.organization=clampist
sonar.host.url=https://sonarcloud.io
```

> **重要**：`sonar.host.url=https://sonarcloud.io` 配置是必需的，它告诉 SonarQube 插件连接到 SonarCloud 而不是本地服务器。

### 2. pom.xml 属性配置

在 `pom.xml` 中已添加 SonarCloud 组织配置：

```xml
<properties>
    <sonar.organization>clampist</sonar.organization>
</properties>
```

### 3. pom.xml Maven 插件

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
- **缓存优化**：添加了 SonarQube 包缓存以提高分析速度
- **项目密钥**：使用 `clampist_static-data-backend` 作为项目密钥

## 工作流程

1. **代码提交**：推送到 main 或 develop 分支
2. **运行测试**：执行单元测试和 API 测试
3. **代码分析**：SonarQube 分析代码质量
4. **质量门禁**：检查是否通过质量门禁
5. **结果反馈**：在 GitHub 中显示分析结果

## JaCoCo vs SonarQube 配置说明

### 当前配置选择

为了避免重复的覆盖率检查，我们采用了以下策略：

1. **JaCoCo 插件**：仅用于生成覆盖率报告，不进行阈值检查
2. **SonarQube**：负责代码质量门禁和覆盖率阈值检查

这样配置的好处：
- 避免 JaCoCo 和 SonarQube 的重复检查
- SonarQube 提供更全面的代码质量分析
- 更灵活的质量门禁配置

### 如果需要启用 JaCoCo 检查

如果你想同时使用 JaCoCo 的覆盖率检查，可以在 `pom.xml` 中取消注释 JaCoCo 的 `check` 执行配置。

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

1. **连接被拒绝 (Connection refused)**
   - 错误信息：`SonarQube server [http://localhost:9000] can not be reached`
   - 解决方案：
     - 确保 `sonar-project.properties` 中包含 `sonar.host.url=https://sonarcloud.io`
     - 或者在命令行中明确指定：`-Dsonar.host.url=https://sonarcloud.io`
   - 原因：默认情况下 SonarQube 插件会尝试连接本地服务器

2. **认证失败**
   - 检查 `SONAR_TOKEN` 是否正确
   - 确认令牌有足够的权限

3. **项目不存在**
   - 检查项目密钥 `clampist_static-data-backend` 是否正确
   - 确认项目已在 SonarCloud 中创建

4. **覆盖率报告缺失**
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

# 运行分析 (使用 SonarCloud) - 推荐方式
./mvnw -B verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=clampist_static-data-backend -Dsonar.host.url=https://sonarcloud.io
```

或者使用简化的命令：

```bash
# 使用配置文件中的设置 (确保 sonar-project.properties 包含 sonar.host.url)
./mvnw sonar:sonar
```

## 更多信息

- [SonarQube Maven Plugin 文档](https://docs.sonarqube.org/latest/analysis/scan/sonarscanner-for-maven/)
- [SonarCloud 文档](https://docs.sonarcloud.io/)
- [GitHub Actions 集成指南](https://docs.sonarcloud.io/advanced-setup/ci-based-analysis/github-actions/)
