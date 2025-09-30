#!/bin/bash

# Docker日志查看脚本
echo "📋 Docker服务日志"
echo "=================="
echo ""

# 检查docker-compose.yml是否存在
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ 错误: 未找到docker-compose.yml文件"
    exit 1
fi

# 检查服务是否运行
if ! docker-compose ps | grep -q "Up"; then
    echo "⚠️  警告: 没有运行中的服务"
    echo "💡 提示: 先运行 ./start-docker.sh 启动服务"
    echo ""
fi

echo "选择要查看的服务日志:"
echo "1) 查看所有服务日志"
echo "2) 查看PostgreSQL日志"
echo "3) 查看Redis日志"
echo "4) 查看应用日志（如果运行）"
echo ""
read -p "请选择 (1-4): " choice

case $choice in
    1)
        echo "📋 查看所有服务日志..."
        docker-compose logs -f
        ;;
    2)
        echo "📋 查看PostgreSQL日志..."
        docker-compose logs -f postgres
        ;;
    3)
        echo "📋 查看Redis日志..."
        docker-compose logs -f redis
        ;;
    4)
        echo "📋 查看应用日志..."
        docker-compose logs -f app
        ;;
    *)
        echo "❌ 无效选择，查看所有服务日志..."
        docker-compose logs -f
        ;;
esac
