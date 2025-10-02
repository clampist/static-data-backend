# GitHub Actions CI/CD 集成指南
# GitHub Actions CI/CD Integration Guide

## 🎯 集成概述

性能测试框架已成功集成到 GitHub Actions CI/CD 流水线中，提供自动化的性能测试和基准检查。

## 📋 集成完成的工作

### ✅ 步骤1: 目录移动
- 将 `perf-test` 目录移动到 `backend/perf-test`
- 更新了所有脚本中的路径引用

### ✅ 步骤2: 路径配置更新
- 更新了所有运行脚本中的启动命令路径
- 修改了 README.md 中的路径说明
- 确保相对路径正确工作

### ✅ 步骤3: CI/CD 配置集成

#### 3.1 主CI流水线集成
在 `.github/workflows/ci.yml` 中添加了 `performance-test` job：

```yaml
performance-test:
  name: Performance Tests
  runs-on: ubuntu-latest
  needs: [test, api-test]
  if: github.event_name == 'pull_request' || github.ref == 'refs/heads/main'
```

**触发条件：**
- Pull Request 时自动运行
- 推送到 main 分支时自动运行

**测试内容：**
- 快速认证API性能测试 (1分钟)
- 性能基准检查
- 测试报告生成

#### 3.2 独立性能测试工作流
创建了 `.github/workflows/performance.yml`：

**触发条件：**
- 每周一凌晨2点定时运行
- 手动触发 (`workflow_dispatch`)
- 代码变更时自动触发

**测试矩阵：**
- auth (认证API)
- organization (组织管理API)
- datafile (数据文件API)
- comprehensive (综合测试)

#### 3.3 性能基准检查
创建了 `scripts/check_baseline.py` 脚本：

**基准配置：**
```python
BASELINE_METRICS = {
    "auth": {
        "max_avg_response_time": 200,  # ms
        "min_success_rate": 0.95,      # 95%
        "min_rps": 10,                 # requests per second
    },
    # ... 其他测试类型
}
```

## 🚀 使用方法

### 本地测试CI集成
```bash
cd backend/perf-test
chmod +x test_ci_integration.sh
./test_ci_integration.sh
```

### 手动触发性能测试
1. 访问 GitHub Actions 页面
2. 选择 "Comprehensive Performance Tests" 工作流
3. 点击 "Run workflow"

### 查看测试结果
1. **GitHub Actions 页面**: 查看工作流执行状态
2. **Artifacts**: 下载详细的测试报告
3. **Step Summary**: 查看关键性能指标

## 📊 测试报告

### CI流水线中的测试
- **文件**: `ci_performance_report.html`
- **CSV**: `ci_performance_stats*.csv`
- **日志**: `ci_performance_test.log`

### 独立性能测试
- **文件**: `{test-type}_performance_report.html`
- **CSV**: `{test-type}_performance_stats*.csv`
- **日志**: `{test-type}_performance_test.log`

## 🔧 配置说明

### 环境变量
CI中使用的环境变量与现有配置保持一致：
- 数据库连接配置
- JWT密钥配置
- Redis配置
- CORS配置

### 服务依赖
- PostgreSQL 15 (测试数据库)
- Redis 6 (缓存服务)
- Java 17 (应用运行环境)
- Python 3.11 (性能测试环境)

### 缓存策略
- Maven依赖缓存
- Python包缓存
- 加速构建过程

## 📈 性能监控

### 关键指标
1. **响应时间**: 平均、最大、百分位响应时间
2. **吞吐量**: RPS (每秒请求数)
3. **成功率**: 请求成功率百分比
4. **并发用户**: 支持的并发用户数

### 基准检查
- 自动检查是否满足性能基准
- 失败时提供详细的指标对比
- 支持不同测试类型的独立基准

## 🎯 触发策略

### 快速测试 (CI流水线)
- **触发**: PR和main分支推送
- **时长**: 1分钟
- **用户数**: 10个并发用户
- **目的**: 快速验证性能回归

### 完整测试 (独立工作流)
- **触发**: 定时、手动、代码变更
- **时长**: 5-10分钟
- **用户数**: 20-50个并发用户
- **目的**: 全面性能评估

## 🔍 故障排除

### 常见问题

1. **应用启动失败**
   ```bash
   # 检查日志
   tail -f logs/ci_performance_test.log
   ```

2. **基准检查失败**
   ```bash
   # 手动运行基准检查
   python scripts/check_baseline.py --test-type auth
   ```

3. **依赖安装失败**
   ```bash
   # 重新安装依赖
   pip install -r requirements.txt
   ```

### 调试技巧

1. **查看完整日志**: 下载 artifacts 中的日志文件
2. **检查环境**: 使用 `python test_setup.py` 验证环境
3. **本地复现**: 使用 `test_ci_integration.sh` 本地测试

## 📝 最佳实践

### 开发流程
1. **提交代码前**: 本地运行快速测试
2. **PR创建后**: 自动触发CI性能测试
3. **合并前**: 确保基准检查通过
4. **定期**: 查看完整性能测试结果

### 性能优化
1. **监控趋势**: 定期查看性能报告趋势
2. **基准调整**: 根据实际情况调整性能基准
3. **容量规划**: 基于测试结果进行容量规划

## 🎉 集成完成

✅ 性能测试框架已成功集成到 GitHub Actions CI/CD 流水线  
✅ 支持自动化的性能测试和基准检查  
✅ 提供详细的测试报告和性能指标  
✅ 支持多种触发策略和测试场景  

现在你的项目具备了完整的性能测试自动化能力！
