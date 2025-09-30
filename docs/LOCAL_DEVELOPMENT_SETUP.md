# 本地开发环境配置指南

## 必需依赖

### 1. Java 17
```bash
# 使用Homebrew安装 (macOS)
brew install openjdk@17

# 或者使用SDKMAN (推荐)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 17.0.9-tem

# 验证安装
java -version
javac -version
```

### 2. Maven 3.9+
```bash
# 使用Homebrew安装 (macOS)
brew install maven

# 或者使用SDKMAN
sdk install maven

# 验证安装
mvn -version
```

### 3. Docker Desktop
```bash
# 下载并安装Docker Desktop
# https://www.docker.com/products/docker-desktop/

# 验证安装
docker --version
docker-compose --version
```

### 4. Node.js 18+ (前端开发)
```bash
# 使用Homebrew安装 (macOS)
brew install node@18

# 或者使用nvm (推荐)
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
nvm install 18
nvm use 18

# 验证安装
node -version
npm -version
```

## 环境变量配置

### Java环境变量
```bash
# 添加到 ~/.zshrc 或 ~/.bash_profile
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH

# 重新加载配置
source ~/.zshrc
```

### Maven环境变量
```bash
# 添加到 ~/.zshrc 或 ~/.bash_profile
export MAVEN_HOME=/opt/homebrew/Cellar/maven/3.9.6/libexec
export PATH=$MAVEN_HOME/bin:$PATH

# 重新加载配置
source ~/.zshrc
```

## 快速安装脚本

### macOS一键安装脚本
```bash
#!/bin/bash
# install-dependencies.sh

echo "🚀 安装本地开发依赖..."

# 检查Homebrew
if ! command -v brew &> /dev/null; then
    echo "📦 安装Homebrew..."
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
fi

# 安装Java 17
echo "☕ 安装Java 17..."
brew install openjdk@17

# 安装Maven
echo "🔨 安装Maven..."
brew install maven

# 安装Node.js
echo "📦 安装Node.js..."
brew install node@18

# 配置环境变量
echo "⚙️ 配置环境变量..."
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.zshrc
echo 'export MAVEN_HOME=/opt/homebrew/Cellar/maven/3.9.6/libexec' >> ~/.zshrc
echo 'export PATH=$MAVEN_HOME/bin:$PATH' >> ~/.zshrc

echo "✅ 安装完成！请重新打开终端或运行: source ~/.zshrc"
```

## 验证安装

### 检查所有依赖
```bash
# 检查Java
java -version
# 应该显示: openjdk version "17.x.x"

# 检查Maven
mvn -version
# 应该显示: Apache Maven 3.9.x

# 检查Docker
docker --version
# 应该显示: Docker version 24.x.x

# 检查Node.js
node -version
# 应该显示: v18.x.x

# 检查npm
npm -version
# 应该显示: 9.x.x 或更高版本
```

## 项目设置

### 1. 克隆项目
```bash
git clone <your-repo-url>
cd JavaPro
```

### 2. 后端设置
```bash
cd backend

# 安装依赖
mvn clean install

# 运行测试
./run-tests-local.sh
```

### 3. 前端设置
```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

## 常见问题解决

### 1. Maven命令未找到
```bash
# 检查Maven安装路径
which mvn

# 如果未找到，重新安装
brew reinstall maven

# 检查PATH
echo $PATH
```

### 2. Java版本问题
```bash
# 检查Java版本
java -version

# 如果版本不对，设置JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# 或者使用SDKMAN管理多个Java版本
sdk list java
sdk use java 17.0.9-tem
```

### 3. Docker问题
```bash
# 启动Docker Desktop
open -a Docker

# 检查Docker状态
docker info

# 如果Docker未运行，启动服务
sudo systemctl start docker  # Linux
# 或启动Docker Desktop应用  # macOS/Windows
```

### 4. 端口冲突
```bash
# 检查端口占用
lsof -i :8080
lsof -i :5432
lsof -i :6379

# 停止占用端口的进程
kill -9 <PID>
```

## 开发工作流

### 1. 启动后端服务
```bash
cd backend

# 启动数据库和Redis
docker-compose up -d postgres redis

# 运行应用
mvn spring-boot:run
```

### 2. 启动前端服务
```bash
cd frontend

# 启动开发服务器
npm run dev
```

### 3. 运行测试
```bash
cd backend

# 运行所有测试
./run-tests-local.sh

# 或运行特定测试
mvn test -Dtest=AuthServiceTest
```

## 推荐工具

### IDE
- **IntelliJ IDEA** (推荐)
- **VS Code** + Java扩展包
- **Eclipse**

### 数据库工具
- **DBeaver** (免费)
- **DataGrip** (JetBrains)
- **pgAdmin** (PostgreSQL专用)

### API测试
- **Postman**
- **Insomnia**
- **curl** (命令行)

## 性能优化

### Maven优化
```bash
# 设置Maven选项
export MAVEN_OPTS="-Xmx2048m -XX:MaxPermSize=512m"

# 使用并行构建
mvn -T 4 clean install
```

### Docker优化
```bash
# 增加Docker内存限制
# Docker Desktop -> Settings -> Resources -> Memory: 4GB+

# 清理Docker缓存
docker system prune -a
```

---
**创建时间**: 2025-09-30 02:10  
**状态**: 本地开发环境配置指南完成 ✅
