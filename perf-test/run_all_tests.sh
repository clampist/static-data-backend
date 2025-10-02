#!/bin/bash

# Run All Performance Tests
# 运行所有性能测试

echo "🎯 Starting All Performance Tests"
echo "🎯 启动所有性能测试"

# Create necessary directories
mkdir -p logs reports

# Activate virtual environment
echo "📦 Activating Python virtual environment..."
pyenv activate perf

# Check if backend is running
echo "🔍 Checking if backend is running..."
if ! curl -s http://localhost:8080/api/actuator/health > /dev/null; then
    echo "❌ Backend is not running on localhost:8080"
    echo "❌ 后端服务未在localhost:8080运行"
    echo "Please start the backend first:"
    echo "请先启动后端服务："
    echo "cd ../ && mvn spring-boot:run"
    exit 1
fi

echo "✅ Backend is running"
echo "✅ 后端服务正在运行"

# Make scripts executable
chmod +x run_auth_test.sh
chmod +x run_organization_test.sh
chmod +x run_datafile_test.sh
chmod +x run_comprehensive_test.sh

echo ""
echo "🚀 Running all performance tests in sequence..."
echo "🚀 按顺序运行所有性能测试..."

# Test 1: Authentication API
echo ""
echo "=== Test 1: Authentication API Performance ==="
echo "=== 测试1：认证API性能 ==="
./run_auth_test.sh

# Wait between tests
echo ""
echo "⏳ Waiting 30 seconds before next test..."
sleep 30

# Test 2: Organization API
echo ""
echo "=== Test 2: Organization API Performance ==="
echo "=== 测试2：组织管理API性能 ==="
./run_organization_test.sh

# Wait between tests
echo ""
echo "⏳ Waiting 30 seconds before next test..."
sleep 30

# Test 3: Data File API
echo ""
echo "=== Test 3: Data File API Performance ==="
echo "=== 测试3：数据文件API性能 ==="
./run_datafile_test.sh

# Wait between tests
echo ""
echo "⏳ Waiting 30 seconds before next test..."
sleep 30

# Test 4: Comprehensive API
echo ""
echo "=== Test 4: Comprehensive API Performance ==="
echo "=== 测试4：综合API性能 ==="
./run_comprehensive_test.sh

echo ""
echo "🎉 All performance tests completed!"
echo "🎉 所有性能测试完成！"
echo ""
echo "📊 Reports available in reports/ directory:"
echo "📊 报告位置：reports/ 目录"
echo "   - auth_performance_report.html"
echo "   - org_performance_report.html"
echo "   - datafile_performance_report.html"
echo "   - comprehensive_performance_report.html"
echo ""
echo "📈 CSV statistics available:"
echo "📈 CSV统计数据："
echo "   - auth_stats*.csv"
echo "   - org_stats*.csv"
echo "   - datafile_stats*.csv"
echo "   - comprehensive_stats*.csv"
echo ""
echo "🔍 Check logs/ directory for detailed execution logs"
echo "🔍 查看 logs/ 目录获取详细执行日志"
