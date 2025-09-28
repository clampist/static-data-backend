# JaCoCo报告生成依赖打包指南

## 概述

本指南说明如何将生成JaCoCo报告所需的文件打包，以便在远程环境重新生成覆盖率报告。

## JaCoCo报告生成的三类必需依赖

根据JaCoCo的工作原理，生成覆盖率报告需要以下三类核心依赖：

### 1. 编译产物 (Classes)
- **target/classes/** - 主代码编译产物
- **target/test-classes/** - 测试代码编译产物
- **作用**: JaCoCo需要字节码文件来计算覆盖率

### 2. 测试执行数据 (Execution Data)
- **target/jacoco.exec** - JaCoCo agent记录的核心数据
- **target/surefire-reports/** - 单元测试执行结果
- **target/failsafe-reports/** - 集成测试执行结果
- **作用**: 记录哪些字节码被执行，是生成报告的核心数据

### 3. 插件配置依赖 (Plugin Configuration)
- **pom.xml** - 包含JaCoCo插件配置
- **.mvn/, mvnw, mvnw.cmd** - Maven包装器
- **作用**: 配置JaCoCo插件和Maven环境

## 打包脚本使用

### 运行打包脚本
```bash
./ut-scripts/package-jacoco-dependencies.sh
```

### 生成的包文件

脚本会生成以下包文件：

1. **compiled-artifacts_YYYYMMDD_HHMMSS.tar.gz** (约100KB)
   - 包含编译产物
   - 用于直接生成报告

2. **test-execution-data_YYYYMMDD_HHMMSS.tar.gz** (约300KB)
   - 包含测试执行数据
   - JaCoCo agent记录的核心数据

3. **plugin-config_YYYYMMDD_HHMMSS.tar.gz** (约10KB)
   - 包含插件配置和Maven包装器
   - 用于配置环境

4. **complete-jacoco-package_YYYYMMDD_HHMMSS.tar.gz** (约5MB)
   - 包含所有三类必需依赖
   - **推荐使用** - 一个包包含所有必需文件

## 远程环境使用

### 方法1: 使用完整包（推荐）
```bash
# 1. 传输完整包到远程环境
scp ut-scripts/packages/complete-jacoco-package_*.tar.gz remote-server:/path/to/project/

# 2. 在远程环境解压
tar -xzf complete-jacoco-package_*.tar.gz

# 3. 生成报告
./mvnw jacoco:report
```

### 方法2: 使用恢复脚本
```bash
# 1. 传输整个packages目录
scp -r ut-scripts/packages/ remote-server:/path/to/project/ut-scripts/

# 2. 运行恢复脚本
./ut-scripts/restore-and-regenerate.sh
```

### 方法3: 分别解压各个包
```bash
# 1. 解压插件配置
tar -xzf plugin-config_*.tar.gz

# 2. 解压编译产物
tar -xzf compiled-artifacts_*.tar.gz

# 3. 解压测试执行数据
tar -xzf test-execution-data_*.tar.gz

# 4. 生成报告
./mvnw jacoco:report
```

## 环境要求

- **Java 17+** - 运行JaCoCo和Maven
- **Maven** - 或使用Maven包装器（已包含在包中）
- **网络连接** - 用于下载Maven依赖（如果需要）

## 验证报告生成

生成报告后，检查以下文件：
- `target/site/jacoco/index.html` - 主报告文件
- `target/site/jacoco/jacoco.csv` - CSV格式数据
- `target/site/jacoco/jacoco.xml` - XML格式数据

## 故障排除

### 常见问题

1. **缺少jacoco.exec文件**
   - 原因: 测试未运行或JaCoCo agent未正确配置
   - 解决: 确保测试已运行并生成了jacoco.exec

2. **缺少编译产物**
   - 原因: 项目未编译
   - 解决: 运行 `mvn compile test-compile`

3. **Maven配置错误**
   - 原因: pom.xml中JaCoCo插件配置不正确
   - 解决: 检查pom.xml中的jacoco-maven-plugin配置

### 调试命令
```bash
# 检查必需文件
ls -la target/classes/
ls -la target/test-classes/
ls -la target/jacoco.exec
ls -la target/surefire-reports/

# 验证Maven配置
./mvnw help:describe -Dplugin=org.jacoco:jacoco-maven-plugin

# 手动生成报告
./mvnw jacoco:report
```

## 最佳实践

1. **使用完整包** - 包含所有必需文件，减少配置错误
2. **定期打包** - 在测试完成后立即打包，确保数据最新
3. **版本控制** - 为包文件添加时间戳，便于版本管理
4. **环境隔离** - 在不同环境使用相同的包文件，确保一致性

## 相关文件

- `ut-scripts/package-jacoco-dependencies.sh` - 打包脚本
- `ut-scripts/restore-and-regenerate.sh` - 恢复脚本
- `ut-scripts/packages/` - 打包文件目录
- `pom.xml` - Maven配置（包含JaCoCo插件）
