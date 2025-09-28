#!/bin/bash

# Jacoco报告重新生成脚本
# 用于在远程环境解包并重新生成Jacoco报告

echo "=========================================="
echo "🔄 恢复并重新生成Jacoco报告"
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

# 检查Java环境
echo -e "${BLUE}☕ 检查Java环境...${NC}"
if command -v java >/dev/null 2>&1; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    echo -e "${GREEN}✅ Java版本: $JAVA_VERSION${NC}"
    if [ "$JAVA_VERSION" -lt 17 ]; then
        echo -e "${YELLOW}⚠️  建议使用Java 17或更高版本${NC}"
    fi
else
    echo -e "${RED}❌ 未找到Java环境，请先安装Java${NC}"
    exit 1
fi

# 检查Maven环境
echo -e "${BLUE}🔧 检查Maven环境...${NC}"
if [ -f "mvnw" ]; then
    echo -e "${GREEN}✅ 找到Maven包装器${NC}"
    MVN_CMD="./mvnw"
elif command -v mvn >/dev/null 2>&1; then
    echo -e "${GREEN}✅ 找到Maven命令${NC}"
    MVN_CMD="mvn"
else
    echo -e "${RED}❌ 未找到Maven环境，请先安装Maven或使用Maven包装器${NC}"
    exit 1
fi

# 检查是否在项目根目录
if [ ! -f "pom.xml" ]; then
    echo -e "${RED}❌ 请在项目根目录（包含pom.xml的目录）运行此脚本${NC}"
    exit 1
fi

echo -e "${GREEN}✅ 当前目录: $(pwd)${NC}"

# 查找最新的包文件
echo ""
echo -e "${BLUE}📦 查找最新的包文件...${NC}"

if [ ! -d "$PACKAGES_DIR" ]; then
    echo -e "${RED}❌ 未找到packages目录: $PACKAGES_DIR${NC}"
    echo -e "${YELLOW}💡 请先运行 package-jacoco-dependencies.sh 生成包文件${NC}"
    exit 1
fi

# 查找最新的最小化项目快照
LATEST_MINIMAL=$(ls -t "$PACKAGES_DIR"/minimal-jacoco-project_*.tar.gz 2>/dev/null | head -n 1)
if [ -z "$LATEST_MINIMAL" ]; then
    echo -e "${YELLOW}⚠️  未找到最小化项目快照包${NC}"
    echo -e "${YELLOW}💡 将尝试使用现有文件重新生成报告${NC}"
else
    echo -e "${GREEN}✅ 找到最小化项目快照: $(basename "$LATEST_MINIMAL")${NC}"
    
    # 询问是否解压
    echo -e "${YELLOW}❓ 是否解压最新的项目快照？(y/N)${NC}"
    read -r response
    if [[ "$response" =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}📦 解压项目快照...${NC}"
        tar -xzf "$LATEST_MINIMAL"
        echo -e "${GREEN}✅ 项目快照解压完成${NC}"
    fi
fi

# 查找编译产物包
LATEST_COMPILED=$(ls -t "$PACKAGES_DIR"/compiled-artifacts_*.tar.gz 2>/dev/null | head -n 1)
if [ -n "$LATEST_COMPILED" ]; then
    echo -e "${GREEN}✅ 找到编译产物包: $(basename "$LATEST_COMPILED")${NC}"
    
    # 询问是否解压编译产物
    echo -e "${YELLOW}❓ 是否解压编译产物？(y/N)${NC}"
    read -r response
    if [[ "$response" =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}📦 解压编译产物...${NC}"
        tar -xzf "$LATEST_COMPILED"
        echo -e "${GREEN}✅ 编译产物解压完成${NC}"
    fi
fi

# 查找覆盖率相关包
LATEST_COVERAGE=$(ls -t "$PACKAGES_DIR"/coverage-files_*.tar.gz 2>/dev/null | head -n 1)
if [ -n "$LATEST_COVERAGE" ]; then
    echo -e "${GREEN}✅ 找到覆盖率相关包: $(basename "$LATEST_COVERAGE")${NC}"
    
    # 询问是否解压覆盖率文件
    echo -e "${YELLOW}❓ 是否解压覆盖率相关文件？(y/N)${NC}"
    read -r response
    if [[ "$response" =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}📦 解压覆盖率相关文件...${NC}"
        tar -xzf "$LATEST_COVERAGE"
        echo -e "${GREEN}✅ 覆盖率相关文件解压完成${NC}"
    fi
fi

# 检查必要的文件
echo ""
echo -e "${BLUE}🔍 检查必要文件...${NC}"

MISSING_FILES=()

if [ ! -f "pom.xml" ]; then
    MISSING_FILES+=("pom.xml")
fi

if [ ! -d "src" ]; then
    MISSING_FILES+=("src/")
fi

if [ ! -d "ut-scripts" ]; then
    MISSING_FILES+=("ut-scripts/")
fi

if [ ${#MISSING_FILES[@]} -gt 0 ]; then
    echo -e "${RED}❌ 缺少必要文件:${NC}"
    for file in "${MISSING_FILES[@]}"; do
        echo "  - $file"
    done
    echo -e "${YELLOW}💡 请确保已解压相应的包文件${NC}"
    exit 1
fi

echo -e "${GREEN}✅ 所有必要文件检查通过${NC}"

# 选择重新生成方式
echo ""
echo -e "${BLUE}🎯 选择重新生成方式:${NC}"
echo "  1. 仅生成覆盖率报告（使用现有编译产物）"
echo "  2. 重新编译并生成覆盖率报告"
echo "  3. 运行完整测试并生成覆盖率报告"
echo "  4. 使用ut-scripts中的脚本"

read -p "请选择 (1-4): " choice

case $choice in
    1)
        echo -e "${BLUE}📊 仅生成覆盖率报告...${NC}"
        if [ -d "target/classes" ] || [ -d "target/test-classes" ]; then
            $MVN_CMD jacoco:report
            echo -e "${GREEN}✅ 覆盖率报告生成完成${NC}"
        else
            echo -e "${RED}❌ 未找到编译产物，请先编译项目${NC}"
            exit 1
        fi
        ;;
    2)
        echo -e "${BLUE}🔨 重新编译并生成覆盖率报告...${NC}"
        $MVN_CMD clean compile test-compile jacoco:report
        echo -e "${GREEN}✅ 编译和覆盖率报告生成完成${NC}"
        ;;
    3)
        echo -e "${BLUE}🏃 运行完整测试并生成覆盖率报告...${NC}"
        $MVN_CMD clean test jacoco:report
        echo -e "${GREEN}✅ 测试和覆盖率报告生成完成${NC}"
        ;;
    4)
        echo -e "${BLUE}📜 使用ut-scripts中的脚本...${NC}"
        if [ -f "ut-scripts/generate-coverage-report.sh" ]; then
            chmod +x ut-scripts/*.sh
            ./ut-scripts/generate-coverage-report.sh
        else
            echo -e "${RED}❌ 未找到ut-scripts/generate-coverage-report.sh${NC}"
            exit 1
        fi
        ;;
    *)
        echo -e "${RED}❌ 无效选择${NC}"
        exit 1
        ;;
esac

# 检查报告生成结果
echo ""
echo -e "${BLUE}📋 检查报告生成结果...${NC}"

if [ -d "target/site/jacoco" ]; then
    echo -e "${GREEN}✅ 覆盖率报告生成成功${NC}"
    echo "  报告位置: target/site/jacoco/index.html"
    
    # 复制报告到ut-scripts目录
    if [ -d "ut-scripts" ]; then
        echo -e "${BLUE}📁 复制报告到ut-scripts目录...${NC}"
        COVERAGE_DIR="ut-scripts/coverage-reports"
        mkdir -p "$COVERAGE_DIR"
        
        # 复制整个jacoco目录
        cp -r target/site/jacoco "$COVERAGE_DIR/"
        
        # 创建带时间戳的备份
        TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
        cp -r target/site/jacoco "$COVERAGE_DIR/jacoco_$TIMESTAMP"
        
        echo -e "${GREEN}✅ 报告已复制到: $COVERAGE_DIR${NC}"
    fi
    
    # 尝试解析覆盖率数据
    if [ -f "target/site/jacoco/jacoco.csv" ]; then
        echo -e "${BLUE}📈 覆盖率统计:${NC}"
        if command -v awk >/dev/null 2>&1; then
            TOTAL_LINES=$(awk -F',' 'NR>1 {sum+=$4+$5} END {print sum}' target/site/jacoco/jacoco.csv 2>/dev/null)
            COVERED_LINES=$(awk -F',' 'NR>1 {sum+=$4} END {print sum}' target/site/jacoco/jacoco.csv 2>/dev/null)
            if [ ! -z "$TOTAL_LINES" ] && [ ! -z "$COVERED_LINES" ] && [ "$TOTAL_LINES" -gt 0 ]; then
                COVERAGE_PERCENT=$(echo "scale=1; $COVERED_LINES * 100 / $TOTAL_LINES" | bc 2>/dev/null || echo "N/A")
                echo "  指令覆盖率: ${COVERAGE_PERCENT}%"
            fi
        fi
    fi
    
    # 提供打开报告的选项
    echo ""
    echo -e "${YELLOW}💡 使用以下命令查看详细报告:${NC}"
    echo "  - 原始位置: target/site/jacoco/index.html"
    if [ -d "ut-scripts/coverage-reports" ]; then
        echo "  - ut-scripts目录: ut-scripts/coverage-reports/jacoco/index.html"
    fi
    echo "  - 或者运行: open target/site/jacoco/index.html (macOS)"
    echo "  - 或者运行: xdg-open target/site/jacoco/index.html (Linux)"
    
else
    echo -e "${RED}❌ 覆盖率报告生成失败${NC}"
    echo -e "${YELLOW}💡 请检查编译和测试是否成功${NC}"
    exit 1
fi

echo ""
echo "=========================================="
echo "🎉 Jacoco报告重新生成完成"
echo "=========================================="
