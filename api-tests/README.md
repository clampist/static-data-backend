# API测试脚本目录

这个目录包含了API接口测试相关的脚本，用于测试后端服务的各种API接口。

## 脚本说明

### 核心脚本

- **api-coverage-test.sh** - API覆盖率测试主脚本 🎯
  - 使用JaCoCo Agent模式启动目标程序
  - 运行完整的API接口测试套件
  - 停止应用并dump覆盖率数据
  - 将jacoco.exec文件复制到api-tests/cov目录

- **quick-api-coverage.sh** - 快速API测试脚本 ⚡
  - 仅负责调用API接口测试
  - 不涉及任何覆盖率相关操作
  - 快速验证API功能

- **generate-report-standalone.sh** - 独立覆盖率报告生成器 📊
  - 复制依赖的源码包和class包到api-tests/cov目录
  - 解压预编译包
  - 使用jacococli.jar生成覆盖率报告
  - 生成HTML、CSV、XML格式报告

### 工具脚本

- **setup-jacoco-agent.sh** - JaCoCo Agent设置脚本
  - 下载并安装JaCoCo Agent和CLI工具
  - 配置必要的JaCoCo组件

### API测试脚本

- **test-auth-apis.sh** - 认证API测试
  - 测试用户登录、注册、token验证等功能
  - 验证认证流程的完整性

- **test-organization-apis.sh** - 组织管理API测试
  - 测试组织创建、更新、删除等操作
  - 验证组织层级关系管理

- **test-final-datafile.sh** - 数据文件API测试
  - 测试数据文件上传、下载、查询等功能
  - 验证数据文件管理流程

- **test-datafile-query-fix.sh** - 数据文件查询修复验证
  - 专门验证PostgreSQL bytea问题的修复
  - 确保数据文件查询功能正常

## 使用方法

### 完整API覆盖率测试流程

```bash
# 1. 运行API覆盖率测试（Agent模式）
./api-coverage-test.sh

# 2. 生成覆盖率报告
./generate-report-standalone.sh
```

### 快速API测试

```bash
# 仅运行API测试，不涉及覆盖率
./quick-api-coverage.sh
```

### 单独运行API测试

```bash
# 运行特定API测试
./test-auth-apis.sh
./test-organization-apis.sh
./test-final-datafile.sh
./test-datafile-query-fix.sh
```

## 脚本职责分工

### api-coverage-test.sh
- ✅ 使用JaCoCo Agent启动目标程序
- ✅ 运行完整的API测试套件
- ✅ 停止应用并dump覆盖率数据
- ✅ 复制jacoco.exec到api-tests/cov目录

### quick-api-coverage.sh
- ✅ 仅负责调用API接口
- ✅ 不涉及覆盖率相关操作
- ✅ 快速验证API功能

### generate-report-standalone.sh
- ✅ 复制预编译包到api-tests/cov目录
- ✅ 解压源码包和class包
- ✅ 使用jacococli.jar生成报告
- ✅ 生成HTML、CSV、XML报告

### API测试脚本
- ✅ 只负责调用API接口
- ✅ 验证API响应和功能
- ✅ 不涉及覆盖率操作

## 目录结构

```
api-tests/
├── api-coverage-test.sh          # 主覆盖率测试脚本
├── quick-api-coverage.sh         # 快速API测试脚本
├── generate-report-standalone.sh # 独立报告生成器
├── setup-jacoco-agent.sh         # JaCoCo Agent设置
├── test-auth-apis.sh             # 认证API测试
├── test-organization-apis.sh     # 组织管理API测试
├── test-final-datafile.sh        # 数据文件API测试
├── test-datafile-query-fix.sh    # 数据文件查询修复验证
├── jacoco/                       # JaCoCo工具目录
│   ├── jacocoagent.jar          # JaCoCo Agent
│   └── jacococli.jar            # JaCoCo CLI
└── cov/                         # 覆盖率数据目录
    ├── jacoco.exec              # JaCoCo执行数据
    ├── compiled-artifacts_*.tar.gz  # 编译产物包
    ├── source-code_*.tar.gz     # 源码包
    └── reports/                 # 报告目录
        ├── latest/              # 最新报告
        └── report_YYYYMMDD_HHMMSS/  # 历史报告
```

## 前置条件

1. **Java环境**: 确保Java 8+已安装
2. **Maven环境**: 确保Maven已安装
3. **JaCoCo工具**: 运行`./setup-jacoco-agent.sh`安装JaCoCo工具
4. **预编译包**: 确保存在预编译包文件（在`../ut-scripts/packages/`目录）

## 测试用户凭据

- 用户名: `testuser` 或 `admin`
- 密码: `password123` 或 `admin123`

## 报告查看

- **最新报告**: `api-tests/cov/reports/latest/index.html`
- **历史备份**: `api-tests/cov/reports/report_YYYYMMDD_HHMMSS/index.html`
- **Jacoco数据**: `api-tests/cov/jacoco.exec`

## 故障排除

### 应用启动失败
如果应用启动失败（如编译错误），可以：
1. 检查项目编译状态
2. 修复编译错误
3. 重新运行`./api-coverage-test.sh`

### 缺少预编译包
如果缺少预编译包：
1. 确保在`../ut-scripts/packages/`目录存在包文件
2. 包文件命名格式：`compiled-artifacts_YYYYMMDD_HHMMSS.tar.gz`和`source-code_YYYYMMDD_HHMMSS.tar.gz`

### JaCoCo工具缺失
如果JaCoCo工具缺失：
1. 运行`./setup-jacoco-agent.sh`安装工具
2. 确保网络连接正常（需要下载工具）

## 注意事项

1. **脚本解耦**: 各脚本职责明确，相互独立
2. **数据管理**: 所有覆盖率数据统一存储在`api-tests/cov`目录
3. **报告生成**: 使用预编译包确保报告一致性
4. **错误处理**: 各脚本包含完整的错误处理和用户友好的提示信息