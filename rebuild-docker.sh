#!/bin/bash

# Docker服务重建脚本
echo "🔨 重建Docker服务"
echo "=================="
echo ""

# 检查Docker是否运行
if ! docker info >/dev/null 2>&1; then
    echo "❌ 错误: Docker未运行"
    echo "请启动Docker Desktop或Docker服务"
    exit 1
fi

echo "✅ Docker已运行"
echo ""

# 检查docker-compose.yml是否存在
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ 错误: 未找到docker-compose.yml文件"
    exit 1
fi

echo "⚠️  警告: 这将停止所有服务并重建容器"
read -p "是否继续? (y/n): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "⏭️  操作已取消"
    exit 0
fi

echo ""
echo "🛑 停止现有服务..."
docker-compose down

echo ""
echo "🔨 重建容器..."
docker-compose build --no-cache

if [ $? -eq 0 ]; then
    echo ""
    echo "🚀 启动重建的服务..."
    docker-compose up -d postgres redis
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ 服务重建并启动成功"
        echo ""
        
        # 等待服务启动
        echo "⏳ 等待服务启动..."
        sleep 5
        
        # 检查服务状态
        echo "📊 服务状态:"
        docker-compose ps postgres redis
        
        echo ""
        echo "🚀 现在可以运行: ./start-backend.sh"
        
    else
        echo ""
        echo "❌ 服务启动失败"
        exit 1
    fi
    
else
    echo ""
    echo "❌ 容器重建失败"
    exit 1
fi
