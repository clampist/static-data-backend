#!/bin/bash

# Docker服务启动脚本
echo "🐳 启动Docker服务"
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

# 启动服务
echo "📋 启动PostgreSQL和Redis服务..."
docker-compose up -d postgres redis

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 服务启动成功"
    echo ""
    
    # 等待服务启动
    echo "⏳ 等待服务启动..."
    sleep 3
    
    # 检查服务状态
    echo "📊 服务状态:"
    docker-compose ps postgres redis
    
    echo ""
    echo "🔍 检查连接..."
    
    # 检查PostgreSQL连接
    if nc -z localhost 5432 2>/dev/null; then
        echo "✅ PostgreSQL连接正常 (localhost:5432)"
    else
        echo "⚠️  PostgreSQL连接失败"
    fi
    
    # 检查Redis连接
    if nc -z localhost 6379 2>/dev/null; then
        echo "✅ Redis连接正常 (localhost:6379)"
    else
        echo "⚠️  Redis连接失败"
    fi
    
    echo ""
    echo "🚀 现在可以运行: ./start-backend.sh"
    
else
    echo ""
    echo "❌ 服务启动失败"
    echo "请检查Docker和docker-compose配置"
    exit 1
fi
