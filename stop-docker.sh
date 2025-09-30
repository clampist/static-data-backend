#!/bin/bash

# Docker服务停止脚本
echo "🛑 停止Docker服务"
echo "=================="
echo ""

# 检查docker-compose.yml是否存在
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ 错误: 未找到docker-compose.yml文件"
    exit 1
fi

echo "📋 停止PostgreSQL和Redis服务..."
docker-compose down

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 服务已停止"
    echo ""
    
    # 显示剩余容器状态
    echo "📊 当前容器状态:"
    docker-compose ps
    
    echo ""
    echo "💡 提示:"
    echo "   - 要完全清理数据卷，运行: docker-compose down -v"
    echo "   - 要重新启动服务，运行: ./start-docker.sh"
    
else
    echo ""
    echo "❌ 服务停止失败"
    echo "请检查Docker状态"
    exit 1
fi
