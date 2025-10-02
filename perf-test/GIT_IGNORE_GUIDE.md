# Git Ignore 配置指南
# Git Ignore Configuration Guide

## 📋 概述

本文档说明了 perf-test 目录中哪些文件应该被 Git 跟踪，哪些应该被忽略。

## ✅ 被 Git 跟踪的文件

### 核心配置文件
- `requirements.txt` - Python 依赖包列表
- `config.py` - 性能测试配置
- `utils.py` - 工具类和辅助函数
- `locust.conf` - Locust 配置文件

### 测试脚本
- `locustfile_*.py` - 所有 Locust 测试文件
- `scripts/` - 脚本目录及其内容
- `run_*.sh` - 所有运行脚本

### 文档文件
- `README.md` - 使用说明
- `PROJECT_SUMMARY.md` - 项目总结
- `CI_INTEGRATION_GUIDE.md` - CI 集成指南
- `GIT_IGNORE_GUIDE.md` - 本文件

### 环境文件
- `.env.example` - 环境变量示例（如果存在）

## ❌ 被 Git 忽略的文件

### 测试结果和报告
- `logs/` - 所有日志文件目录
- `reports/` - 所有测试报告目录
- `*.log` - 单个日志文件

### Python 缓存和编译文件
- `__pycache__/` - Python 字节码缓存
- `*.pyc` - Python 编译文件
- `*.pyo` - Python 优化编译文件

### 环境配置
- `.env` - 实际环境变量文件（包含敏感信息）

## 🔧 当前 .gitignore 配置

```gitignore
### Performance Test Results ###
perf-test/logs/
perf-test/reports/
perf-test/__pycache__/
perf-test/.env
perf-test/*.pyc
perf-test/*.pyo
```

## 🤔 为什么这样配置？

### 忽略 logs/ 和 reports/
1. **文件体积大**: 测试报告文件通常很大（HTML 报告可能几MB）
2. **频繁变化**: 每次测试都会生成新的报告
3. **临时性**: 这些文件是测试的产物，不是源代码
4. **CI/CD 处理**: GitHub Actions 会通过 artifacts 处理这些文件

### 忽略 __pycache__/
1. **自动生成**: Python 自动创建这些缓存文件
2. **平台相关**: 不同操作系统的字节码可能不兼容
3. **可重现**: 可以从源代码重新生成

### 跟踪配置和脚本文件
1. **源代码**: 这些是性能测试的核心代码
2. **版本控制**: 需要跟踪配置变更和脚本更新
3. **团队协作**: 团队成员需要共享这些文件

## 📊 文件大小对比

### 应该忽略的文件（示例）
```
logs/quick_test.log          - 几KB到几MB
reports/quick_test_report.html - 1-10MB
__pycache__/config.cpython-311.pyc - 几KB
```

### 应该跟踪的文件
```
requirements.txt            - 几百字节
config.py                   - 1-2KB
locustfile_auth.py          - 3-5KB
```

## 🔄 CI/CD 中的处理

### GitHub Actions
- **测试报告**: 通过 `actions/upload-artifact` 上传
- **日志文件**: 在工作流中显示和保存
- **临时文件**: 在 CI 环境中生成，不需要提交到仓库

### 本地开发
- **测试结果**: 本地运行测试时生成
- **缓存文件**: Python 自动管理
- **配置变更**: 通过 Git 跟踪和同步

## 🚀 最佳实践

### 开发流程
1. **提交代码**: 只提交源代码和配置文件
2. **运行测试**: 本地生成测试报告（被忽略）
3. **查看结果**: 在浏览器中查看 HTML 报告
4. **清理**: 定期清理旧的测试结果

### 团队协作
1. **共享配置**: 通过 Git 共享配置变更
2. **统一环境**: 使用 requirements.txt 确保依赖一致
3. **文档更新**: 及时更新 README 和配置文档

### CI/CD 集成
1. **自动化测试**: CI 中自动运行性能测试
2. **报告归档**: 通过 artifacts 保存测试报告
3. **基准检查**: 自动验证性能基准

## 📝 注意事项

### 不要提交的文件
- ❌ 包含敏感信息的 `.env` 文件
- ❌ 大型的测试报告文件
- ❌ Python 缓存和编译文件
- ❌ 临时日志文件

### 确保提交的文件
- ✅ 所有 Python 源代码文件
- ✅ 配置文件和脚本
- ✅ 文档和说明文件
- ✅ 依赖管理文件

## 🔍 验证配置

### 检查被忽略的文件
```bash
git status --ignored
```

### 检查文件大小
```bash
du -sh perf-test/logs/ perf-test/reports/
```

### 清理被忽略的文件
```bash
# 清理 Python 缓存
find perf-test -name "__pycache__" -type d -exec rm -rf {} +

# 清理测试结果
rm -rf perf-test/logs/* perf-test/reports/*
```

这样的配置确保了：
- 🎯 **仓库保持精简**: 只包含必要的源代码
- 🔄 **团队协作顺畅**: 共享配置和脚本
- 🚀 **CI/CD 高效**: 自动处理测试结果
- 📊 **性能监控**: 通过 artifacts 查看详细报告
