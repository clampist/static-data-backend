#!/bin/bash

# 本地开发依赖安装脚本
# 适用于macOS系统

set -e  # Exit on any error

echo "🚀 安装本地开发依赖..."
echo "=========================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check if we're on macOS
if [[ "$OSTYPE" != "darwin"* ]]; then
    print_error "This script is designed for macOS. Please install dependencies manually."
    exit 1
fi

# Check if Homebrew is installed
if ! command_exists brew; then
    print_status "安装Homebrew..."
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    
    # Add Homebrew to PATH for Apple Silicon Macs
    if [[ $(uname -m) == "arm64" ]]; then
        echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zshrc
        eval "$(/opt/homebrew/bin/brew shellenv)"
    fi
    
    print_success "Homebrew安装完成"
else
    print_success "Homebrew已安装"
fi

# Install Java 17
print_status "安装Java 17..."
if ! command_exists java || ! java -version 2>&1 | grep -q "17\."; then
    brew install openjdk@17
    
    # Create symlink for system Java
    sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
    
    print_success "Java 17安装完成"
else
    print_success "Java 17已安装"
fi

# Install Maven
print_status "安装Maven..."
if ! command_exists mvn; then
    brew install maven
    print_success "Maven安装完成"
else
    print_success "Maven已安装"
fi

# Install Node.js
print_status "安装Node.js..."
if ! command_exists node; then
    brew install node@18
    print_success "Node.js安装完成"
else
    print_success "Node.js已安装"
fi

# Install Docker Desktop
print_status "检查Docker Desktop..."
if ! command_exists docker; then
    print_warning "Docker Desktop未安装"
    print_status "请手动安装Docker Desktop:"
    print_status "1. 访问: https://www.docker.com/products/docker-desktop/"
    print_status "2. 下载并安装Docker Desktop"
    print_status "3. 启动Docker Desktop"
else
    print_success "Docker已安装"
fi

# Configure environment variables
print_status "配置环境变量..."

# Get the shell configuration file
SHELL_CONFIG=""
if [[ -n "$ZSH_VERSION" ]]; then
    SHELL_CONFIG="$HOME/.zshrc"
elif [[ -n "$BASH_VERSION" ]]; then
    SHELL_CONFIG="$HOME/.bash_profile"
fi

if [[ -n "$SHELL_CONFIG" ]]; then
    # Add Java environment variables
    if ! grep -q "JAVA_HOME" "$SHELL_CONFIG"; then
        echo "" >> "$SHELL_CONFIG"
        echo "# Java Environment Variables" >> "$SHELL_CONFIG"
        echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> "$SHELL_CONFIG"
        echo 'export PATH=$JAVA_HOME/bin:$PATH' >> "$SHELL_CONFIG"
        print_success "Java环境变量已添加到 $SHELL_CONFIG"
    fi
    
    # Add Maven environment variables
    if ! grep -q "MAVEN_HOME" "$SHELL_CONFIG"; then
        echo "" >> "$SHELL_CONFIG"
        echo "# Maven Environment Variables" >> "$SHELL_CONFIG"
        echo 'export MAVEN_HOME=/opt/homebrew/Cellar/maven/3.9.6/libexec' >> "$SHELL_CONFIG"
        echo 'export PATH=$MAVEN_HOME/bin:$PATH' >> "$SHELL_CONFIG"
        print_success "Maven环境变量已添加到 $SHELL_CONFIG"
    fi
fi

# Verify installations
echo ""
print_status "验证安装..."

# Check Java
if command_exists java; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    print_success "Java: $JAVA_VERSION"
else
    print_error "Java安装失败"
fi

# Check Maven
if command_exists mvn; then
    MAVEN_VERSION=$(mvn -version 2>&1 | head -n 1)
    print_success "Maven: $MAVEN_VERSION"
else
    print_error "Maven安装失败"
fi

# Check Node.js
if command_exists node; then
    NODE_VERSION=$(node -version)
    print_success "Node.js: $NODE_VERSION"
else
    print_error "Node.js安装失败"
fi

# Check Docker
if command_exists docker; then
    DOCKER_VERSION=$(docker --version)
    print_success "Docker: $DOCKER_VERSION"
else
    print_warning "Docker未安装，请手动安装Docker Desktop"
fi

echo ""
print_success "🎉 依赖安装完成！"
echo ""
print_warning "请执行以下操作："
echo "1. 重新打开终端或运行: source $SHELL_CONFIG"
echo "2. 如果安装了Docker Desktop，请启动它"
echo "3. 运行测试: ./run-tests-local.sh"
echo ""
print_status "安装的依赖："
echo "  ☕ Java 17"
echo "  🔨 Maven 3.9+"
echo "  📦 Node.js 18+"
echo "  🐳 Docker Desktop (需要手动安装)"
echo ""
