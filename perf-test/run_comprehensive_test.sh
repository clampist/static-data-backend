#!/bin/bash

# Comprehensive API Performance Test Runner
# 综合API性能测试运行脚本

echo "🎯 Starting Comprehensive API Performance Test"
echo "🎯 启动综合API性能测试"

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

# Run Locust test
echo "🏃 Running Locust comprehensive performance test..."
echo "🏃 运行Locust综合性能测试..."

locust -f locustfile_comprehensive.py \
    --host=http://localhost:8080 \
    --users=100 \
    --spawn-rate=2 \
    --run-time=600s \
    --headless \
    --html=reports/comprehensive_performance_report.html \
    --csv=reports/comprehensive_stats \
    --csv-full-history \
    --logfile=logs/comprehensive_test.log \
    --loglevel=INFO

echo "✅ Comprehensive performance test completed!"
echo "✅ 综合性能测试完成！"
echo "📊 Report available at: reports/comprehensive_performance_report.html"
echo "📊 报告位置：reports/comprehensive_performance_report.html"
