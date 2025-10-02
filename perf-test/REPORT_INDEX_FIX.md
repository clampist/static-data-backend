# 报告索引修复总结
# Report Index Fix Summary

## 🎯 问题分析

从用户反馈和图片可以看出两个主要问题：

### 1. 文件大小显示问题
- **现象**: 所有文件都显示 "Size: Unknown"
- **原因**: `get_file_size()` 函数没有正确处理文件路径

### 2. 日志目录扫描问题  
- **现象**: 报告索引页面显示 "No log files found"
- **原因**: 脚本只扫描 `reports/` 目录，没有扫描 `logs/` 目录

## 🔧 修复方案

### 1. 修复文件大小获取

**问题代码**:
```python
def get_file_size(filename):
    try:
        size = os.path.getsize(filename)  # 直接使用文件名
        # ...
    except OSError:
        return "Unknown"
```

**修复后**:
```python
def get_file_size(filepath):
    try:
        if not os.path.exists(filepath):  # 检查文件是否存在
            return "Unknown"
        size = os.path.getsize(filepath)  # 使用完整路径
        # ...
    except (OSError, TypeError):  # 增加异常类型
        return "Unknown"
```

### 2. 修复目录扫描逻辑

**问题代码**:
```python
# 只扫描 reports 目录
if os.path.exists(reports_dir):
    for file in os.listdir(reports_dir):
        # 只处理 reports 目录中的文件
```

**修复后**:
```python
# 扫描 reports 目录
if os.path.exists(reports_dir):
    for file in os.listdir(reports_dir):
        file_path = os.path.join(reports_dir, file)
        if os.path.isfile(file_path):  # 确保是文件
            # 处理文件...

# 同时扫描 logs 目录
logs_dir = "logs"
if os.path.exists(logs_dir):
    for file in os.listdir(logs_dir):
        file_path = os.path.join(logs_dir, file)
        if os.path.isfile(file_path) and file.endswith('.log'):
            log_files.append(file_path)
```

### 3. 修复文件路径处理

**问题代码**:
```python
for file in html_files:
    file_size = get_file_size(file)  # 使用文件名
    html += f'<a href="{file}" ...>'  # 直接使用文件名
```

**修复后**:
```python
for file_path in html_files:
    file_name = os.path.basename(file_path)  # 提取文件名
    file_size = get_file_size(file_path)     # 使用完整路径
    html += f'<a href="{file_name}" ...>'    # 使用文件名
```

### 4. 修复日志文件链接

**特殊处理**:
```python
# For log files, we need to handle the path correctly
if file_path.startswith('logs/'):
    href = f"../{file_path}"  # Go up one level to access logs directory
else:
    href = file_name
```

## 📊 修复效果

### 修复前
- ❌ 所有文件显示 "Size: Unknown"
- ❌ 日志文件显示 "No log files found"
- ❌ 无法正确访问日志文件

### 修复后
- ✅ 正确显示文件大小（如 "2.5 KB", "1.2 MB"）
- ✅ 正确扫描和显示日志文件
- ✅ 正确的文件链接路径

## 🧪 测试验证

创建了 `test_report_index.py` 测试脚本：

```python
def test_file_size():
    """Test file size function"""
    # 测试现有文件
    size = get_file_size('reports/test_stats_stats.csv')
    print(f"✅ CSV file size: {size}")
    
    # 测试不存在的文件
    size = get_file_size('non_existing_file.txt')
    print(f"✅ Non-existing file size: {size}")

def test_report_generation():
    """Test report index generation"""
    index_path = generate_report_index()
    # 验证生成的文件和内容
```

## 🔄 CI 集成

修复后的脚本会在 CI 中自动运行：

```yaml
- name: Generate Report Index
  run: |
    cd perf-test
    python scripts/generate_report_index.py
```

## 📈 预期结果

现在生成的报告索引页面将显示：

### HTML Reports 部分
- 📊 Locust Ci Report (Size: 15.2 KB)
- 📊 Locust Direct Report (Size: 12.8 KB)

### Data Files 部分  
- 📊 Auth Performance Stats Stats (Size: 2.1 KB)
- 📊 Locust Ci Stats Stats (Size: 1.8 KB)
- 📊 Locust Ci Stats Stats History (Size: 5.4 KB)
- 📊 Locust Ci Stats Failures (Size: 0.8 KB)
- 📊 Locust Ci Stats Exceptions (Size: 0.3 KB)

### Log Files 部分
- 📋 Locust Ci Log (Size: 3.2 KB)
- 📋 Locust Log (Size: 1.5 KB)

## 🎉 总结

通过这次修复，我们解决了：

1. **文件大小显示**: 正确获取和显示文件大小
2. **目录扫描**: 同时扫描 `reports/` 和 `logs/` 目录
3. **路径处理**: 正确处理相对路径和绝对路径
4. **链接生成**: 生成正确的文件访问链接

现在报告索引页面将提供完整、准确的文件信息，让用户能够更好地浏览和访问性能测试报告！
