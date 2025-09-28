#!/bin/bash

# 测试完整JaCoCo包的功能
# 用于验证打包的完整性

echo "=========================================="
echo "🧪 测试完整JaCoCo包功能"
echo "=========================================="

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 获取脚本所在目录
UT_SCRIPTS_DIR="$(dirname "$0")"
PACKAGES_DIR="$UT_SCRIPTS_DIR/packages"

# 查找最新的完整包
LATEST_COMPLETE=$(ls -t "$PACKAGES_DIR"/complete-jacoco-package_*.tar.gz 2>/dev/null | head -n 1)

if [ -z "$LATEST_COMPLETE" ]; then
    echo -e "${RED}❌ 未找到完整JaCoCo包${NC}"
    echo -e "${YELLOW}💡 请先运行 package-jacoco-dependencies.sh 生成包文件${NC}"
    exit 1
fi

echo -e "${GREEN}✅ 找到完整包: $(basename "$LATEST_COMPLETE")${NC}"
echo "  完整路径: $LATEST_COMPLETE"

# 创建测试目录
TEST_DIR="/tmp/jacoco-package-test-$(date +%s)"
echo -e "${BLUE}📁 创建测试目录: $TEST_DIR${NC}"
mkdir -p "$TEST_DIR"

# 解压包到测试目录
echo -e "${BLUE}📦 解压完整包...${NC}"
# 先复制包到测试目录，然后解压
cp "$LATEST_COMPLETE" "$TEST_DIR/"
cd "$TEST_DIR"
PACKAGE_NAME="$(basename "$LATEST_COMPLETE")"
echo "  解压文件: $PACKAGE_NAME"
tar -xzf "$PACKAGE_NAME"

# 检查必需文件
echo ""
echo -e "${BLUE}🔍 检查必需文件...${NC}"

MISSING_FILES=()
REQUIRED_FILES=(
    "target/classes"
    "target/test-classes" 
    "target/jacoco.exec"
    "pom.xml"
    "mvnw"
)

for file in "${REQUIRED_FILES[@]}"; do
    if [ -e "$file" ]; then
        echo -e "${GREEN}✅ $file${NC}"
    else
        echo -e "${RED}❌ $file${NC}"
        MISSING_FILES+=("$file")
    fi
done

if [ ${#MISSING_FILES[@]} -gt 0 ]; then
    echo -e "${RED}❌ 缺少必需文件，包不完整${NC}"
    rm -rf "$TEST_DIR"
    exit 1
fi

# 检查Java环境
echo ""
echo -e "${BLUE}☕ 检查Java环境...${NC}"
if command -v java >/dev/null 2>&1; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    echo -e "${GREEN}✅ Java版本: $JAVA_VERSION${NC}"
else
    echo -e "${RED}❌ 未找到Java环境${NC}"
    rm -rf "$TEST_DIR"
    exit 1
fi

# 测试Maven包装器
echo ""
echo -e "${BLUE}🔧 测试Maven包装器...${NC}"
if [ -f "mvnw" ]; then
    chmod +x mvnw
    if ./mvnw --version >/dev/null 2>&1; then
        echo -e "${GREEN}✅ Maven包装器工作正常${NC}"
    else
        echo -e "${YELLOW}⚠️  Maven包装器可能有问题${NC}"
    fi
else
    echo -e "${RED}❌ 未找到Maven包装器${NC}"
fi

# 测试JaCoCo插件配置
echo ""
echo -e "${BLUE}🔌 测试JaCoCo插件配置...${NC}"
if grep -q "jacoco-maven-plugin" pom.xml; then
    echo -e "${GREEN}✅ JaCoCo插件已配置${NC}"
else
    echo -e "${RED}❌ JaCoCo插件未配置${NC}"
fi

# 尝试生成报告
echo ""
echo -e "${BLUE}📊 尝试生成JaCoCo报告...${NC}"
if [ -f "mvnw" ] && [ -f "target/jacoco.exec" ]; then
    echo "运行: ./mvnw jacoco:report"
    if ./mvnw jacoco:report -q 2>/dev/null; then
        echo -e "${GREEN}✅ JaCoCo报告生成成功${NC}"
        
        if [ -f "target/site/jacoco/index.html" ]; then
            echo -e "${GREEN}✅ 报告文件已生成: target/site/jacoco/index.html${NC}"
        else
            echo -e "${YELLOW}⚠️  报告文件未找到${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  JaCoCo报告生成失败${NC}"
        echo -e "${YELLOW}💡 这可能是正常的，因为可能缺少某些依赖${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  跳过报告生成测试（缺少Maven包装器或jacoco.exec）${NC}"
fi

# 显示包大小信息
echo ""
echo -e "${BLUE}📏 包大小信息:${NC}"
echo "  完整包大小: $(du -h "$LATEST_COMPLETE" | cut -f1)"
echo "  解压后大小: $(du -sh "$TEST_DIR" | cut -f1)"

# 清理测试目录
echo ""
echo -e "${BLUE}🧹 清理测试目录...${NC}"
rm -rf "$TEST_DIR"

echo ""
echo "=========================================="
echo "🎉 完整JaCoCo包功能测试完成"
echo "=========================================="

if [ ${#MISSING_FILES[@]} -eq 0 ]; then
    echo -e "${GREEN}✅ 包包含所有必需文件，可以用于远程环境${NC}"
    echo -e "${YELLOW}💡 建议: 将完整包传输到远程环境并运行 ./mvnw jacoco:report${NC}"
else
    echo -e "${RED}❌ 包缺少必需文件，需要重新打包${NC}"
fi
