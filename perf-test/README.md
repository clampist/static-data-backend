# 静态数据平台性能测试框架
# Static Data Platform Performance Testing Framework

基于 Locust 的性能测试框架，用于测试后端 Spring Boot API 的性能表现。

## 📋 目录结构

```
perf-test/
├── requirements.txt              # Python依赖包
├── config.py                     # 配置文件
├── utils.py                      # 工具类和辅助函数
├── .env                          # 环境变量配置
├── .env.example                  # 环境变量示例
├── locust.conf                   # Locust配置文件
├── locustfile_auth.py            # 认证API性能测试
├── locustfile_organization.py    # 组织管理API性能测试
├── locustfile_datafile.py        # 数据文件API性能测试
├── locustfile_comprehensive.py   # 综合API性能测试
├── run_auth_test.sh              # 认证API测试运行脚本
├── run_organization_test.sh      # 组织管理API测试运行脚本
├── run_datafile_test.sh          # 数据文件API测试运行脚本
├── run_comprehensive_test.sh     # 综合API测试运行脚本
├── logs/                         # 测试日志目录
└── reports/                      # 测试报告目录
```

## 🚀 快速开始

### 1. 环境准备

```bash
# 激活虚拟环境
pyenv activate perf

# 安装依赖
pip install -r requirements.txt
```

### 2. 启动后端服务

```bash
cd ../
mvn spring-boot:run
```

### 3. 运行性能测试

#### 认证API性能测试
```bash
chmod +x run_auth_test.sh
./run_auth_test.sh
```

#### 组织管理API性能测试
```bash
chmod +x run_organization_test.sh
./run_organization_test.sh
```

#### 数据文件API性能测试
```bash
chmod +x run_datafile_test.sh
./run_datafile_test.sh
```

#### 综合API性能测试
```bash
chmod +x run_comprehensive_test.sh
./run_comprehensive_test.sh
```

## 📊 测试场景

### 认证API测试 (locustfile_auth.py)
- 用户登录 (权重: 3)
- 用户注册 (权重: 1)
- 获取当前用户信息 (权重: 2)
- Token验证 (权重: 1)
- Token刷新 (权重: 1)
- 用户名可用性检查 (权重: 2)
- 邮箱可用性检查 (权重: 2)
- 无效登录测试 (权重: 1)

### 组织管理API测试 (locustfile_organization.py)
- 获取组织树 (权重: 5)
- 获取节点类型 (权重: 2)
- 创建组织节点 (权重: 1)
- 获取子节点 (权重: 3)
- 获取节点详情 (权重: 2)
- 更新组织节点 (权重: 1)
- 获取节点统计 (权重: 1)
- 移动节点 (权重: 1)
- 搜索节点 (权重: 2)
- 删除节点 (权重: 1)

### 数据文件API测试 (locustfile_datafile.py)
- 获取支持的数据类型 (权重: 4)
- 创建数据文件 (权重: 1)
- 根据ID获取数据文件 (权重: 3)
- 更新数据文件 (权重: 2)
- 查询数据文件 (权重: 5)
- 搜索数据文件 (权重: 3)
- 根据组织获取数据文件 (权重: 2)
- 获取最近的数据文件 (权重: 2)
- 获取可访问的数据文件 (权重: 3)
- 获取统计信息 (权重: 2)
- 根据数据类型查询 (权重: 1)
- 删除数据文件 (权重: 1)

### 综合API测试 (locustfile_comprehensive.py)
- 认证操作 (20% 流量)
- 组织管理操作 (30% 流量)
- 数据文件操作 (50% 流量)
- 健康检查 (偶尔)

## ⚙️ 配置说明

### 环境变量 (.env)
```bash
# 后端配置
BACKEND_HOST=http://localhost:8080
BACKEND_BASE_URL=http://localhost:8080/api

# 测试账户
DEFAULT_USERNAME=testuser
DEFAULT_PASSWORD=password123
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin123

# 性能测试配置
DEFAULT_RAMP_UP_TIME=60
DEFAULT_RAMP_DOWN_TIME=10
DEFAULT_SPAWN_RATE=2
DEFAULT_MAX_USERS=100
DEFAULT_TEST_DURATION=300

# 测试数据配置
TEST_DATA_SIZE=1000
MAX_ORGANIZATION_NODES=50
MAX_DATA_FILES=100
```

### Locust配置 (locust.conf)
```ini
[locust]
web-host = 0.0.0.0
web-port = 8089
host = http://localhost:8080
users = 50
spawn-rate = 2
run-time = 300s
headless = false
```

## 📈 性能指标

测试将生成以下指标：
- **响应时间**: 平均、最小、最大响应时间
- **RPS**: 每秒请求数
- **成功率**: 请求成功率
- **并发用户**: 支持的并发用户数
- **错误率**: 请求失败率

## 📊 报告说明

### HTML报告
- 位置: `reports/*_performance_report.html`
- 包含: 图表、统计表格、响应时间分布

### CSV报告
- 位置: `reports/*_stats.csv`
- 包含: 详细的统计数据，可用于进一步分析

### 日志文件
- 位置: `logs/*_test.log`
- 包含: 详细的测试执行日志

## 🔧 自定义测试

### 修改测试参数
编辑对应的 `run_*_test.sh` 脚本中的参数：
```bash
--users=100        # 最大并发用户数
--spawn-rate=2     # 用户增长速率
--run-time=300s    # 测试运行时间
```

### 添加新的测试场景
1. 在相应的 locustfile 中添加新的 `@task` 方法
2. 使用 `catch_response=True` 来验证响应
3. 调用相应的验证方法

### 修改测试数据
编辑 `utils.py` 中的 `TestDataGenerator` 类来生成不同的测试数据。

## 🐛 故障排除

### 常见问题

1. **后端服务未运行**
   ```
   ❌ Backend is not running on localhost:8080
   ```
   解决: 启动后端服务 `mvn spring-boot:run`

2. **认证失败**
   ```
   HTTP 401
   ```
   解决: 检查 `.env` 中的用户名和密码配置

3. **端口被占用**
   ```
   Address already in use
   ```
   解决: 修改 `--web-port` 参数或停止占用端口的进程

### 日志分析
查看 `logs/` 目录下的日志文件来诊断问题。

## 📝 最佳实践

1. **渐进式测试**: 从小并发开始，逐步增加
2. **监控资源**: 监控服务器CPU、内存、数据库连接
3. **数据清理**: 测试后清理测试数据
4. **多轮测试**: 进行多轮测试以获得稳定结果
5. **环境隔离**: 在独立的测试环境中运行

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来改进测试框架。
