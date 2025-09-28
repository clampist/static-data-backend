#!/bin/bash

# JaCoCo Agent 设置脚本
# Setup JaCoCo Agent for API Testing

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印函数
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# JaCoCo 版本
JACOCO_VERSION="0.8.12"
JACOCO_BASE_URL="https://repo1.maven.org/maven2/org/jacoco"

# 获取脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
JACOCO_DIR="$SCRIPT_DIR/jacoco"

echo "🔧 JaCoCo Agent 设置脚本"
echo "Setup JaCoCo Agent for API Testing"
echo "================================="
echo ""

print_info "设置目录结构..."
mkdir -p "$JACOCO_DIR"
cd "$JACOCO_DIR"

print_info "下载 JaCoCo Agent..."
if [ ! -f "jacocoagent.jar" ]; then
    print_info "正在下载 jacocoagent.jar (v${JACOCO_VERSION})..."
    if curl -L -o jacocoagent.jar "${JACOCO_BASE_URL}/org.jacoco.agent/${JACOCO_VERSION}/org.jacoco.agent-${JACOCO_VERSION}-runtime.jar"; then
        print_status "jacocoagent.jar 下载成功"
    else
        print_error "jacocoagent.jar 下载失败"
        exit 1
    fi
else
    print_status "jacocoagent.jar 已存在"
fi

print_info "下载 JaCoCo CLI..."
if [ ! -f "jacococli.jar" ]; then
    print_info "正在下载 jacococli.jar (v${JACOCO_VERSION})..."
    if curl -L -o jacococli.jar "${JACOCO_BASE_URL}/org.jacoco.cli/${JACOCO_VERSION}/org.jacoco.cli-${JACOCO_VERSION}-nodeps.jar"; then
        print_status "jacococli.jar 下载成功"
    else
        print_error "jacococli.jar 下载失败"
        exit 1
    fi
else
    print_status "jacococli.jar 已存在"
fi

# 验证下载的文件
print_info "验证下载的文件..."
if [ -f "jacocoagent.jar" ] && [ -f "jacococli.jar" ]; then
    AGENT_SIZE=$(du -h jacocoagent.jar | awk '{print $1}')
    CLI_SIZE=$(du -h jacococli.jar | awk '{print $1}')
    print_status "文件验证成功"
    echo "  - jacocoagent.jar: $AGENT_SIZE"
    echo "  - jacococli.jar: $CLI_SIZE"
else
    print_error "文件验证失败"
    exit 1
fi

# 测试 JaCoCo CLI
print_info "测试 JaCoCo CLI..."
if java -jar jacococli.jar --help > /dev/null 2>&1; then
    print_status "JaCoCo CLI 测试成功"
else
    print_error "JaCoCo CLI 测试失败"
    exit 1
fi

echo ""
print_status "JaCoCo Agent 设置完成！"
echo ""
echo "📋 文件位置:"
echo "  - Agent: $JACOCO_DIR/jacocoagent.jar"
echo "  - CLI:   $JACOCO_DIR/jacococli.jar"
echo ""
echo "🚀 使用方法:"
echo "  1. 启动应用: java -javaagent:$JACOCO_DIR/jacocoagent.jar=destfile=jacoco.exec,output=file -jar your-app.jar"
echo "  2. 运行API测试"
echo "  3. Dump数据: java -jar $JACOCO_DIR/jacococli.jar dump --address localhost --port 6300 --destfile jacoco.exec"
echo "  4. 生成报告: java -jar $JACOCO_DIR/jacococli.jar report jacoco.exec --classfiles target/classes --sourcefiles src/main/java --html report"
echo ""

cd "$SCRIPT_DIR"
