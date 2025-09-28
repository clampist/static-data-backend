# JaCoCo报告生成方式对比

## 概述

根据您的需求，我们提供了两种JaCoCo报告生成方式的打包解决方案：

1. **Maven插件方式** - 使用 `mvn jacoco:report`
2. **JaCoCo CLI方式** - 使用 `java -jar jacococli.jar report`

## 方式对比

### 1. Maven插件方式

**命令示例：**
```bash
mvn jacoco:report
```

**必需依赖：**
- ✅ 编译产物 (`target/classes/`, `target/test-classes/`)
- ✅ 测试执行数据 (`target/jacoco.exec`)
- ✅ 测试运行器 (Surefire/Failsafe插件配置)
- ✅ Maven配置 (`pom.xml`, `.mvn/`, `mvnw`)

**打包脚本：** `package-jacoco-dependencies.sh`
**恢复脚本：** `restore-and-regenerate.sh`

**包大小：**
- 编译产物包: ~100KB
- 测试执行数据包: ~300KB  
- 插件配置包: ~10KB
- **完整包: ~3MB**

### 2. JaCoCo CLI方式

**命令示例：**
```bash
java -jar jacococli.jar report jacoco.exec \
     --classfiles target/classes \
     --sourcefiles src/main/java \
     --html report
```

**必需依赖：**
- ✅ 编译产物 (`target/classes/`)
- ✅ 源码 (`src/main/java/`)
- ✅ 测试执行数据 (`target/jacoco.exec`)

**打包脚本：** `package-jacococli-dependencies.sh`
**恢复脚本：** `restore-and-regenerate-jacococli.sh`

**包大小：**
- 编译产物包: ~55KB
- 源码包: ~30KB
- 测试执行数据包: ~290KB
- **完整包: ~370KB**

## 优势对比

### Maven插件方式优势
- ✅ 集成度高，与Maven构建流程无缝集成
- ✅ 配置简单，只需在pom.xml中配置
- ✅ 支持覆盖率阈值检查
- ✅ 支持多种报告格式
- ✅ 与CI/CD工具集成良好

### JaCoCo CLI方式优势
- ✅ **包更小** (370KB vs 3MB，减少90%+)
- ✅ **依赖更少** (不需要Maven环境)
- ✅ **更灵活** (可以独立运行)
- ✅ **更轻量** (只需要Java环境)
- ✅ **更精确** (只包含必需文件)

## 使用场景建议

### 选择Maven插件方式的情况：
- 项目使用Maven构建
- 需要集成到CI/CD流程
- 需要覆盖率阈值检查
- 团队熟悉Maven工具链

### 选择JaCoCo CLI方式的情况：
- 需要在没有Maven的环境中生成报告
- 需要最小的包大小
- 需要独立运行报告生成
- 只需要基本的覆盖率报告

## 文件结构对比

### Maven插件方式包内容：
```
complete-jacoco-package_*.tar.gz
├── target/
│   ├── classes/           # 编译产物
│   ├── test-classes/      # 测试编译产物
│   ├── jacoco.exec        # 测试执行数据
│   ├── surefire-reports/  # 测试报告
│   └── site/jacoco/       # 现有报告
├── pom.xml                # Maven配置
├── .mvn/                  # Maven包装器
├── mvnw                   # Maven包装器脚本
└── ut-scripts/            # UT脚本
```

### JaCoCo CLI方式包内容：
```
complete-jacococli-package_*.tar.gz
├── target/
│   ├── classes/           # 编译产物
│   └── jacoco.exec        # 测试执行数据
└── src/
    └── main/java/         # 源码
```

## 使用示例

### Maven插件方式：
```bash
# 1. 打包
./ut-scripts/package-jacoco-dependencies.sh

# 2. 传输到远程环境
scp ut-scripts/packages/complete-jacoco-package_*.tar.gz remote-server:/path/to/project/

# 3. 解压并生成报告
tar -xzf complete-jacoco-package_*.tar.gz
./mvnw jacoco:report
```

### JaCoCo CLI方式：
```bash
# 1. 打包
./ut-scripts/package-jacococli-dependencies.sh

# 2. 传输到远程环境
scp ut-scripts/packages/complete-jacococli-package_*.tar.gz remote-server:/path/to/project/

# 3. 解压并生成报告
tar -xzf complete-jacococli-package_*.tar.gz
java -jar jacococli.jar report jacoco.exec \
     --classfiles target/classes \
     --sourcefiles src/main/java \
     --html report
```

## 总结

根据您的需求"是不是只要打包源码和classes文件就行"，**JaCoCo CLI方式**确实更符合您的要求：

- ✅ 包更小 (370KB vs 3MB，减少90%+)
- ✅ 依赖更少 (只需要Java环境)
- ✅ 更精确 (只包含必需文件)
- ✅ 更灵活 (可以独立运行)

**推荐使用JaCoCo CLI方式**，特别是当您需要在远程环境生成报告时。
