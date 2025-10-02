#!/bin/bash

# Fix Authentication Test Script
# 修复认证测试脚本

echo "🔧 Fixing Authentication Issues"
echo "🔧 修复认证问题"

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

# Check Python
echo "📦 Checking Python..."
if command -v python3 &> /dev/null; then
    PYTHON_CMD="python3"
elif command -v python &> /dev/null; then
    PYTHON_CMD="python"
else
    echo "❌ Python not found"
    exit 1
fi

echo "✅ Using Python: $PYTHON_CMD"

# Install requests if needed
echo "📦 Checking requests package..."
if ! $PYTHON_CMD -c "import requests" 2>/dev/null; then
    echo "⚠️  Installing requests package..."
    $PYTHON_CMD -m pip install requests
fi

# Run authentication test
echo "🧪 Running authentication test..."
echo "🧪 运行认证测试..."

$PYTHON_CMD quick_auth_test.py

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Authentication fix completed!"
    echo "✅ 认证修复完成！"
    echo ""
    echo "Now you can run performance tests:"
    echo "现在可以运行性能测试："
    echo "  $PYTHON_CMD test_setup.py"
    echo "  ./run_quick_test.sh"
else
    echo ""
    echo "❌ Authentication fix failed"
    echo "❌ 认证修复失败"
    echo ""
    echo "Please check the backend logs and try again."
    echo "请检查后端日志并重试。"
    exit 1
fi
