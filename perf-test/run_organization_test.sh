#!/bin/bash

# Organization API Performance Test Runner
# 组织管理API性能测试运行脚本

echo "🏢 Starting Organization API Performance Test"
echo "🏢 启动组织管理API性能测试"

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
echo "🏃 Running Locust organization performance test..."
echo "🏃 运行Locust组织管理性能测试..."

locust -f locustfile_organization.py \
    --host=http://localhost:8080 \
    --users=50 \
    --spawn-rate=2 \
    --run-time=300s \
    --headless \
    --html=reports/org_performance_report.html \
    --csv=reports/org_stats \
    --csv-full-history \
    --logfile=logs/org_test.log \
    --loglevel=INFO

echo "✅ Organization performance test completed!"
echo "✅ 组织管理性能测试完成！"
echo "📊 Report available at: reports/org_performance_report.html"
echo "📊 报告位置：reports/org_performance_report.html"
