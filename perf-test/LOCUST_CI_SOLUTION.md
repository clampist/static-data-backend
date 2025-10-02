# Locust CI 测试解决方案
# Locust CI Testing Solution

## 🎯 目标

使用真正的 **Locust** 性能测试框架进行 CI 测试，而不是纯 Python 实现。

## 🛠️ 解决方案

### 主要方案：Locust CI 测试脚本
- **文件**: `locust_ci_test.py`
- **特点**: 
  - ✅ 使用真正的 Locust 框架
  - ✅ 解决日志文件创建问题
  - ✅ 包含用户注册和认证
  - ✅ 生成完整的 Locust 报告

### 备用方案：直接 Locust 命令
- **文件**: `run_locust_direct.sh`
- **特点**:
  - ✅ 直接使用 Locust 命令行
  - ✅ 预创建日志文件避免问题
  - ✅ 简单的 shell 脚本实现
  - ✅ 完整的错误处理

## 📋 当前 CI 配置

```yaml
- name: Run Quick Performance Tests
  run: |
    cd perf-test
    # Create directories and run Locust with proper logging setup
    mkdir -p reports logs
    # Try Locust CI test first, fallback to direct command if it fails
    python3 locust_ci_test.py || (echo "⚠️  Locust CI test failed, trying direct command..." && chmod +x run_locust_direct.sh && ./run_locust_direct.sh)
```

## 🔧 日志问题解决

### 问题根源
Locust 默认尝试创建日志文件，但 `logs` 目录可能不存在。

### 解决方案
1. **预创建目录**: `mkdir -p reports logs`
2. **预创建日志文件**: `touch logs/locust.log`
3. **明确指定日志文件**: `--logfile=logs/locust_ci.log`
4. **最小化日志级别**: `--loglevel=WARNING`

## 🧪 测试参数

### Locust CI 测试 (`locust_ci_test.py`)
- **用户数**: 10个并发用户
- **生成速率**: 2个用户/秒
- **测试时长**: 60秒
- **测试文件**: `locustfile_auth.py`
- **报告**: HTML + CSV 格式

### 直接命令测试 (`run_locust_direct.sh`)
- **用户数**: 10个并发用户
- **生成速率**: 2个用户/秒
- **测试时长**: 60秒
- **测试文件**: `locustfile_auth.py`
- **报告**: HTML + CSV 格式

## 📊 生成的报告

### HTML 报告
- **CI 测试**: `reports/locust_ci_report.html`
- **直接命令**: `reports/locust_direct_report.html`

### CSV 统计
- **CI 测试**: `reports/locust_ci_stats*.csv`
- **直接命令**: `reports/locust_direct_stats*.csv`

### 日志文件
- **CI 测试**: `logs/locust_ci.log`
- **直接命令**: `logs/locust_direct.log`

## 🔄 故障排除

### 如果 Locust CI 测试失败
系统会自动尝试直接命令方案：
```bash
python3 locust_ci_test.py || ./run_locust_direct.sh
```

### 手动测试
```bash
cd perf-test

# 方案1: Python 脚本
python3 locust_ci_test.py

# 方案2: 直接命令
chmod +x run_locust_direct.sh
./run_locust_direct.sh
```

## ✅ 优势

### 使用真正的 Locust
1. **专业性能测试**: 使用业界标准的性能测试框架
2. **丰富功能**: 支持复杂的测试场景和指标
3. **详细报告**: 生成专业的性能测试报告
4. **可扩展性**: 易于添加新的测试场景

### 双重保障
1. **主要方案**: Python 脚本提供更好的控制
2. **备用方案**: Shell 脚本提供简单可靠的执行

### CI 兼容性
1. **日志问题解决**: 预创建必要的文件和目录
2. **错误处理**: 自动回退到备用方案
3. **报告生成**: 确保生成完整的测试报告

## 🎉 总结

现在我们有了一个完整的 Locust CI 测试解决方案：

1. **主要方案**: `locust_ci_test.py` - 使用 Python 脚本控制 Locust
2. **备用方案**: `run_locust_direct.sh` - 直接使用 Locust 命令行
3. **自动回退**: CI 中自动尝试备用方案
4. **完整报告**: 生成 HTML 和 CSV 格式的详细报告
5. **基准检查**: 自动进行性能基准验证

这确保了我们在 CI 环境中能够稳定地运行真正的 Locust 性能测试！
