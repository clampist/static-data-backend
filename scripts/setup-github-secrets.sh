#!/bin/bash

# GitHub Secrets 配置脚本
# 用于在GitHub仓库中配置必要的环境变量

echo "🔧 GitHub Secrets 配置助手"
echo "=========================="
echo ""

# 检查是否安装了GitHub CLI
if ! command -v gh &> /dev/null; then
    echo "❌ 错误: 未安装GitHub CLI (gh)"
    echo "请先安装: https://cli.github.com/"
    exit 1
fi

# 检查是否已登录
if ! gh auth status &> /dev/null; then
    echo "❌ 错误: 未登录GitHub CLI"
    echo "请先登录: gh auth login"
    exit 1
fi

echo "✅ GitHub CLI 已安装并登录"
echo ""

# 获取仓库信息
REPO=$(gh repo view --json nameWithOwner -q .nameWithOwner)
echo "📦 当前仓库: $REPO"
echo ""

# 生成示例密钥
JWT_SECRET=$(openssl rand -base64 64 | tr -d "=+/" | cut -c1-64)
DB_PASSWORD=$(openssl rand -base64 32 | tr -d "=+/" | cut -c1-32)

echo "🔐 生成示例密钥:"
echo "JWT_SECRET: ${JWT_SECRET:0:20}..."
echo "DB_PASSWORD: ${DB_PASSWORD:0:10}..."
echo ""

# 询问用户是否要设置密钥
read -p "是否要设置GitHub Secrets? (y/n): " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🚀 开始配置GitHub Secrets..."
    echo ""
    
    # 设置数据库配置
    echo "📊 配置数据库相关密钥..."
    gh secret set DATABASE_URL --body "jdbc:postgresql://localhost:5432/static_data_platform_test"
    gh secret set DATABASE_USERNAME --body "sdp_user"
    gh secret set DATABASE_PASSWORD --body "$DB_PASSWORD"
    
    # 设置JWT配置
    echo "🔑 配置JWT相关密钥..."
    gh secret set JWT_SECRET --body "$JWT_SECRET"
    gh secret set JWT_EXPIRATION --body "86400000"
    
    # 设置Redis配置
    echo "📦 配置Redis相关密钥..."
    gh secret set REDIS_HOST --body "localhost"
    gh secret set REDIS_PORT --body "6379"
    
    # 设置CORS配置
    echo "🌐 配置CORS相关密钥..."
    gh secret set CORS_ALLOWED_ORIGINS --body "http://localhost:3000,http://localhost:5173"
    
    echo ""
    echo "✅ GitHub Secrets 配置完成!"
    echo ""
    echo "📋 已配置的密钥:"
    gh secret list
    
else
    echo "⏭️  跳过GitHub Secrets配置"
    echo ""
    echo "📝 手动配置指南:"
    echo "1. 访问 https://github.com/$REPO/settings/secrets/actions"
    echo "2. 添加以下密钥:"
    echo "   - DATABASE_URL: jdbc:postgresql://localhost:5432/static_data_platform_test"
    echo "   - DATABASE_USERNAME: sdp_user"
    echo "   - DATABASE_PASSWORD: $DB_PASSWORD"
    echo "   - JWT_SECRET: $JWT_SECRET"
    echo "   - JWT_EXPIRATION: 86400000"
    echo "   - REDIS_HOST: localhost"
    echo "   - REDIS_PORT: 6379"
    echo "   - CORS_ALLOWED_ORIGINS: http://localhost:3000,http://localhost:5173"
fi

echo ""
echo "🎉 配置完成! 现在可以安全地推送代码到GitHub了。"
