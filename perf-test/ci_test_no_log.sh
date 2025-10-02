#!/bin/bash

# CI Performance Test Script (Without Log File)
# CI性能测试脚本（不生成日志文件）

set -e  # Exit on any error

echo "🚀 CI Performance Test (No Log File)"
echo "🚀 CI性能测试（无日志文件）"

# Create all necessary directories
echo "📁 Creating directories..."
mkdir -p reports

# Test backend connectivity
echo "🌐 Testing backend connectivity..."
for i in {1..30}; do
    if curl -f http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
        echo "✅ Backend is ready"
        break
    fi
    echo "⏳ Waiting for backend... ($i/30)"
    sleep 2
done

if ! curl -f http://localhost:8080/api/actuator/health > /dev/null 2>&1; then
    echo "❌ Backend is not ready after 60 seconds"
    exit 1
fi

# Run performance test without log file
echo "🏃 Running performance test (no log file)..."
echo "Command: locust -f locustfile_auth.py --host=http://localhost:8080 --users=10 --spawn-rate=2 --run-time=60s --headless --html=reports/ci_performance_report.html --csv=reports/ci_performance_stats --csv-full-history --loglevel=INFO"

locust -f locustfile_auth.py \
    --host=http://localhost:8080 \
    --users=10 \
    --spawn-rate=2 \
    --run-time=60s \
    --headless \
    --html=reports/ci_performance_report.html \
    --csv=reports/ci_performance_stats \
    --csv-full-history \
    --loglevel=INFO

# Check if test completed successfully
if [ $? -eq 0 ]; then
    echo "✅ Performance test completed successfully"
    
    # Verify output files
    if [ -f "reports/ci_performance_report.html" ]; then
        echo "✅ HTML report generated"
    else
        echo "❌ HTML report not found"
    fi
    
    if [ -f "reports/ci_performance_stats_stats.csv" ]; then
        echo "✅ CSV stats generated"
    else
        echo "❌ CSV stats not found"
    fi
    
    # Show test results summary
    echo ""
    echo "📊 Test Results Summary:"
    echo "📊 测试结果摘要："
    if [ -f "reports/ci_performance_stats_stats.csv" ]; then
        echo "Last few lines of stats:"
        tail -3 reports/ci_performance_stats_stats.csv
    fi
    
    echo ""
    echo "📄 Files generated:"
    ls -la reports/
    
else
    echo "❌ Performance test failed"
    exit 1
fi

echo ""
echo "🎉 CI performance test completed successfully!"
echo "🎉 CI性能测试成功完成！"
