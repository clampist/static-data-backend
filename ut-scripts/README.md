# UT Scripts 目录

这个目录包含了所有与单元测试相关的shell脚本和生成的覆盖率报告。

## 目录结构

```
ut-scripts/
├── README.md                    # 本说明文件
├── run-all-tests.sh            # 运行所有测试并生成覆盖率报告
├── generate-coverage-report.sh # 生成详细的覆盖率报告
├── test-jacoco.sh              # 快速测试Jacoco配置
└── coverage-reports/           # 覆盖率报告存储目录
    ├── jacoco/                 # 最新的覆盖率报告
    │   ├── index.html          # HTML格式报告
    │   ├── jacoco.csv          # CSV格式数据
    │   ├── jacoco.xml          # XML格式数据
    │   └── ...                 # 其他报告文件
    └── jacoco_YYYYMMDD_HHMMSS/ # 带时间戳的历史报告
```

## 脚本说明

### 1. run-all-tests.sh
**功能**: 运行所有单元测试并生成覆盖率报告
**用法**: 
```bash
cd /path/to/backend
./ut-scripts/run-all-tests.sh
```

**特点**:
- 编译项目
- 运行所有测试
- 生成Jacoco覆盖率报告
- 自动复制报告到ut-scripts/coverage-reports目录
- 创建带时间戳的备份

### 2. generate-coverage-report.sh
**功能**: 生成详细的覆盖率报告
**用法**:
```bash
cd /path/to/backend
./ut-scripts/generate-coverage-report.sh
```

**特点**:
- 清理项目
- 编译和运行测试
- 生成详细的覆盖率报告
- 显示覆盖率统计信息
- 检查覆盖率阈值
- 自动复制报告到ut-scripts/coverage-reports目录

### 3. test-jacoco.sh
**功能**: 快速测试Jacoco配置
**用法**:
```bash
cd /path/to/backend
./ut-scripts/test-jacoco.sh
```

**特点**:
- 快速验证Jacoco配置
- 生成基本覆盖率报告
- 自动复制报告到ut-scripts/coverage-reports目录

## 覆盖率报告

### 报告位置
- **原始位置**: `target/site/jacoco/`
- **ut-scripts目录**: `ut-scripts/coverage-reports/jacoco/`
- **历史备份**: `ut-scripts/coverage-reports/jacoco_YYYYMMDD_HHMMSS/`

### 查看报告
```bash
# 查看最新报告
open ut-scripts/coverage-reports/jacoco/index.html

# 查看特定时间的报告
open ut-scripts/coverage-reports/jacoco_20240928_145230/index.html
```

### 报告内容
- **HTML报告**: 交互式网页报告，包含源代码高亮
- **CSV报告**: 可用于数据分析的表格格式
- **XML报告**: 可用于CI/CD集成的机器可读格式

## 使用示例

### 日常开发
```bash
# 运行所有测试并查看覆盖率
cd /path/to/backend
./ut-scripts/run-all-tests.sh
```

### 详细分析
```bash
# 生成详细报告并分析覆盖率
cd /path/to/backend
./ut-scripts/generate-coverage-report.sh
```

### 快速验证
```bash
# 快速测试Jacoco配置
cd /path/to/backend
./ut-scripts/test-jacoco.sh
```

## 覆盖率阈值

当前设置的覆盖率阈值：
- **指令覆盖率**: 最低60%
- **分支覆盖率**: 最低50%

## 注意事项

1. **运行位置**: 所有脚本都需要在项目根目录（包含pom.xml的目录）运行
2. **权限**: 确保脚本有执行权限
3. **依赖**: 需要Maven和Java环境
4. **报告备份**: 每次运行都会创建带时间戳的备份，避免覆盖历史报告

## 故障排除

### 常见问题

1. **脚本无法执行**
   ```bash
   chmod +x ut-scripts/*.sh
   ```

2. **报告未生成**
   - 检查Maven配置
   - 确保测试成功运行
   - 查看控制台错误信息

3. **覆盖率数据不准确**
   - 确保所有测试都成功运行
   - 检查Jacoco配置是否正确

## 集成到CI/CD

可以将这些脚本集成到CI/CD流程中：

```yaml
# GitHub Actions示例
- name: Run tests with coverage
  run: |
    cd backend
    ./ut-scripts/run-all-tests.sh

- name: Upload coverage reports
  uses: actions/upload-artifact@v3
  with:
    name: coverage-reports
    path: backend/ut-scripts/coverage-reports/
```

## 更新日志

- **2024-09-28**: 创建ut-scripts目录，移动所有UT相关脚本
- **2024-09-28**: 添加自动复制覆盖率报告功能
- **2024-09-28**: 添加带时间戳的备份功能
