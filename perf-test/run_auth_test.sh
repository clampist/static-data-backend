#!/bin/bash

# Authentication API Performance Test Runner
# 认证API性能测试运行脚本

echo "🚀 Starting Authentication API Performance Test"
echo "🚀 启动认证API性能测试"

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
echo "🏃 Running Locust authentication performance test..."
echo "🏃 运行Locust认证性能测试..."

locust -f locustfile_auth.py \
    --host=http://localhost:8080 \
    --users=100 \
    --spawn-rate=2 \
    --run-time=300s \
    --headless \
    --html=reports/auth_performance_report.html \
    --csv=reports/auth_stats \
    --csv-full-history \
    --logfile=logs/auth_test.log \
    --loglevel=INFO

echo "✅ Authentication performance test completed!"
echo "✅ 认证性能测试完成！"
echo "📊 Report available at: reports/auth_performance_report.html"
echo "📊 报告位置：reports/auth_performance_report.html"
