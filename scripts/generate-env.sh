#!/bin/bash

# 环境变量生成脚本
# 自动生成安全的本地开发环境变量配置

echo "🔧 本地开发环境变量生成器"
echo "=========================="
echo ""

# 生成安全的JWT密钥
JWT_SECRET=$(openssl rand -base64 64 | tr -d "=+/" | cut -c1-64)
DB_PASSWORD=$(openssl rand -base64 32 | tr -d "=+/" | cut -c1-32)

echo "✅ 生成安全密钥:"
echo "JWT_SECRET: ${JWT_SECRET:0:20}..."
echo "DB_PASSWORD: ${DB_PASSWORD:0:10}..."
echo ""

# 检查是否已存在.env.local文件
if [ -f ".env.local" ]; then
    echo "⚠️  检测到现有的 .env.local 文件"
    read -p "是否要覆盖现有文件? (y/n): " -n 1 -r
    echo ""
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "⏭️  跳过文件生成"
        exit 0
    fi
fi

# 生成.env.local文件
cat > .env.local << EOF
# 本地开发环境变量配置
# 自动生成于: $(date)
# 警告: 此文件包含敏感信息，不要提交到Git仓库

# ===========================================
# 应用配置
# ===========================================
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080

# ===========================================
# 数据库配置
# ===========================================
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/static_data_platform_dev
SPRING_DATASOURCE_USERNAME=sdp_user
SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD

# ===========================================
# JWT配置
# ===========================================
APP_JWT_SECRET=$JWT_SECRET
APP_JWT_EXPIRATION=86400000

# ===========================================
# Redis配置
# ===========================================
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
SPRING_DATA_REDIS_TIMEOUT=60000

# ===========================================
# CORS配置
# ===========================================
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# ===========================================
# 日志配置
# ===========================================
LOGGING_LEVEL_COM_STATICDATA_PLATFORM=DEBUG
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WEB=DEBUG
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=DEBUG

# ===========================================
# 开发工具配置
# ===========================================
SPRING_DEVTOOLS_RESTART_ENABLED=true
SPRING_DEVTOOLS_LIVERELOAD_ENABLED=true

# ===========================================
# 文件上传配置
# ===========================================
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=10MB
SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=10MB

# ===========================================
# 审计配置
# ===========================================
APP_AUDIT_ENABLED=true
APP_DATA_MAX_VERSIONS_PER_FILE=10
EOF

echo "✅ 已生成 .env.local 文件"
echo ""
echo "🔒 安全提醒:"
echo "   - 此文件包含敏感信息，已自动添加到.gitignore"
echo "   - 不要将此文件提交到Git仓库"
echo "   - 定期轮换JWT密钥和数据库密码"
echo ""

# 检查.gitignore是否包含.env.local
if grep -q "\.env\.local" .gitignore; then
    echo "✅ .env.local 已在 .gitignore 中"
else
    echo "⚠️  正在添加 .env.local 到 .gitignore"
    echo ".env.local" >> .gitignore
fi

echo ""
echo "🚀 下一步:"
echo "1. 确保数据库服务正在运行"
echo "2. 运行启动脚本: ./start-backend.sh"
echo "3. 或者直接运行: ./mvnw spring-boot:run"
echo ""
echo "🎉 环境配置完成!"
