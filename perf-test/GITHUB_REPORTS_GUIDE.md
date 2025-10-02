# GitHub 上查看 Locust 报告指南
# GitHub Locust Reports Guide

## 🎯 方案概览

我们提供了多种在 GitHub 上查看 Locust 性能测试报告的方法：

1. **GitHub Actions Artifacts**（推荐）
2. **GitHub Pages 部署**
3. **GitHub Step Summary**
4. **本地查看**

## 📊 方案一：GitHub Actions Artifacts（最简单）

### 如何查看
1. 进入 GitHub 仓库的 **Actions** 页面
2. 点击最新的 CI 运行记录
3. 滚动到页面底部，找到 **Artifacts** 部分
4. 下载 `performance-test-reports-{run_number}` 文件
5. 解压后打开 `index.html` 查看所有报告

### 包含的文件
- 📈 `locust_ci_report.html` - 主要 HTML 报告
- 📊 `locust_ci_stats_stats.csv` - 详细统计数据
- 📋 `locust_ci.log` - 测试日志
- 📄 `index.html` - 报告导航页面

### 优势
- ✅ 简单直接
- ✅ 保留 30 天
- ✅ 包含所有文件
- ✅ 无需额外配置

## 🌐 方案二：GitHub Pages（在线查看）

### 设置步骤
1. 在仓库设置中启用 GitHub Pages
2. 配置部署源为 GitHub Actions
3. 报告将自动部署到 `https://{username}.github.io/{repository}/`

### 访问方式
- 直接访问 GitHub Pages URL
- 在 Actions 页面查看部署状态
- 报告会自动更新（仅成功构建后）

### 优势
- ✅ 在线直接查看
- ✅ 无需下载
- ✅ 自动更新
- ✅ 永久保存

## 📋 方案三：GitHub Step Summary

### 查看位置
- 在 Actions 运行页面的 **Summary** 标签
- 显示关键性能指标
- 包含报告下载链接

### 显示内容
```markdown
## 📊 Performance Test Results
✅ Locust performance tests completed successfully
📈 [📊 View HTML Report](link) - Download artifacts to view
📊 Test Statistics:
[CSV 数据预览]
```

## 💻 方案四：本地查看

### 生成报告索引
```bash
cd perf-test
python scripts/generate_report_index.py
```

### 查看方式
- 打开 `reports/index.html`
- 美观的导航界面
- 直接链接到各个报告文件

## 🔧 技术实现

### CI 配置更新
```yaml
- name: Upload Performance Test Reports
  uses: actions/upload-artifact@v4
  with:
    name: performance-test-reports-${{ github.run_number }}
    path: |
      perf-test/reports/
      perf-test/logs/
    retention-days: 30

- name: Generate Report Index
  run: |
    cd perf-test
    python scripts/generate_report_index.py
```

### 报告索引生成器
- 自动扫描报告目录
- 生成美观的 HTML 导航页面
- 显示文件大小和类型
- 响应式设计

## 📈 报告内容

### HTML 报告包含
- 📊 实时性能指标
- 📈 响应时间分布
- 🔥 失败率统计
- 📋 详细请求日志
- 📊 可视化图表

### CSV 数据包含
- 每个端点的详细统计
- 响应时间百分位数
- 请求/失败计数
- 吞吐量数据

## 🎯 推荐使用流程

### 日常开发
1. **查看 Artifacts**：快速下载查看最新报告
2. **检查 Summary**：了解关键指标概览

### 性能分析
1. **下载完整 Artifacts**：获取所有数据文件
2. **查看 HTML 报告**：可视化分析性能趋势
3. **分析 CSV 数据**：深入分析具体指标

### 团队协作
1. **分享 Artifacts 链接**：直接分享给团队成员
2. **使用 GitHub Pages**：设置在线报告门户
3. **集成到文档**：将报告链接添加到项目文档

## 🚀 高级功能

### 报告比较
- 下载不同版本的 Artifacts
- 对比性能指标变化
- 分析性能回归

### 自动化通知
- 集成到 Slack/Teams
- 性能阈值告警
- 报告自动分享

### 数据持久化
- 导出到外部存储
- 集成到监控系统
- 历史数据追踪

## 📝 总结

通过以上方案，你可以：

1. **快速查看**：通过 Artifacts 快速获取报告
2. **在线访问**：通过 GitHub Pages 在线查看
3. **团队分享**：轻松分享报告给团队成员
4. **数据分析**：获取完整的性能数据

选择最适合你工作流程的方案，开始享受便捷的性能测试报告查看体验！
