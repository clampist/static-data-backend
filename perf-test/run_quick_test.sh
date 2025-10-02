#!/bin/bash

# Quick Performance Test Runner
# 快速性能测试运行脚本

echo "🚀 Starting Quick Performance Test"
echo "🚀 启动快速性能测试"

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
    echo "cd /Users/clampist/Workspace/Java/JavaPro/backend && mvn spring-boot:run"
    exit 1
fi

echo "✅ Backend is running"
echo "✅ 后端服务正在运行"

# Run Locust test with better parameters
echo "🏃 Running Locust quick performance test..."
echo "🏃 运行Locust快速性能测试..."

locust -f locustfile_auth.py \
    --host=http://localhost:8080 \
    --users=10 \
    --spawn-rate=2 \
    --run-time=60s \
    --headless \
    --html=reports/quick_test_report.html \
    --csv=reports/quick_test_stats \
    --csv-full-history \
    --logfile=logs/quick_test.log \
    --loglevel=INFO

echo "✅ Quick performance test completed!"
echo "✅ 快速性能测试完成！"
echo ""
echo "📊 Reports generated:"
echo "📊 生成的报告："
echo "   📄 HTML Report: reports/quick_test_report.html"
echo "   📊 CSV Stats: reports/quick_test_stats*.csv"
echo "   📝 Log: logs/quick_test.log"
echo ""
echo "🌐 Open HTML report in browser:"
echo "🌐 在浏览器中打开HTML报告："
echo "   open reports/quick_test_report.html"
