#!/bin/bash

# JaCoCo CLI报告重新生成脚本
# 用于在远程环境解包并使用jacococli.jar重新生成报告

echo "=========================================="
echo "🔄 恢复并使用JaCoCo CLI重新生成报告"
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

# 检查jacococli.jar
echo -e "${BLUE}🔧 检查jacococli.jar...${NC}"
JACOCOCLI_JAR="jacococli.jar"
if [ -f "$JACOCOCLI_JAR" ]; then
    echo -e "${GREEN}✅ 找到jacococli.jar${NC}"
elif [ -f "ut-scripts/$JACOCOCLI_JAR" ]; then
    JACOCOCLI_JAR="ut-scripts/jacococli.jar"
    echo -e "${GREEN}✅ 找到jacococli.jar在ut-scripts目录${NC}"
else
    echo -e "${YELLOW}⚠️  未找到jacococli.jar${NC}"
    echo -e "${YELLOW}💡 请下载jacococli.jar到当前目录或ut-scripts目录${NC}"
    echo -e "${YELLOW}💡 下载地址: https://www.jacoco.org/jacoco/trunk/doc/cli.html${NC}"
    
    # 询问是否自动下载
    echo -e "${YELLOW}❓ 是否尝试自动下载jacococli.jar？(y/N)${NC}"
    read -r response
    if [[ "$response" =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}📥 下载jacococli.jar...${NC}"
        if command -v curl >/dev/null 2>&1; then
            curl -L -o "$JACOCOCLI_JAR" "https://repo1.maven.org/maven2/org/jacoco/jacoco/0.8.12/jacoco-0.8.12.zip"
            if [ $? -eq 0 ]; then
                echo -e "${GREEN}✅ jacococli.jar下载成功${NC}"
            else
                echo -e "${RED}❌ jacococli.jar下载失败${NC}"
                exit 1
            fi
        else
            echo -e "${RED}❌ 未找到curl命令，无法自动下载${NC}"
            exit 1
        fi
    else
        echo -e "${YELLOW}💡 请手动下载jacococli.jar后重新运行此脚本${NC}"
        exit 1
    fi
fi

# 查找最新的包文件
echo ""
echo -e "${BLUE}📦 查找最新的包文件...${NC}"

if [ ! -d "$PACKAGES_DIR" ]; then
    echo -e "${RED}❌ 未找到packages目录: $PACKAGES_DIR${NC}"
    echo -e "${YELLOW}💡 请先运行 package-jacococli-dependencies.sh 生成包文件${NC}"
    exit 1
fi

# 查找最新的完整包
LATEST_COMPLETE=$(ls -t "$PACKAGES_DIR"/complete-jacococli-package_*.tar.gz 2>/dev/null | head -n 1)
if [ -z "$LATEST_COMPLETE" ]; then
    echo -e "${YELLOW}⚠️  未找到完整JaCoCo CLI包${NC}"
    echo -e "${YELLOW}💡 将尝试使用现有文件重新生成报告${NC}"
else
    echo -e "${GREEN}✅ 找到完整JaCoCo CLI包: $(basename "$LATEST_COMPLETE")${NC}"
    
    # 询问是否解压
    echo -e "${YELLOW}❓ 是否解压最新的包？(y/N)${NC}"
    read -r response
    if [[ "$response" =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}📦 解压包...${NC}"
        tar -xzf "$LATEST_COMPLETE"
        echo -e "${GREEN}✅ 包解压完成${NC}"
    fi
fi

# 检查必要的文件
echo ""
echo -e "${BLUE}🔍 检查必要文件...${NC}"

MISSING_FILES=()

if [ ! -d "target/classes" ]; then
    MISSING_FILES+=("target/classes/")
fi

if [ ! -d "src/main/java" ]; then
    MISSING_FILES+=("src/main/java/")
fi

if [ ! -f "target/jacoco.exec" ]; then
    MISSING_FILES+=("target/jacoco.exec")
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

# 选择报告生成方式
echo ""
echo -e "${BLUE}🎯 选择报告生成方式:${NC}"
echo "  1. 生成HTML报告"
echo "  2. 生成XML报告"
echo "  3. 生成CSV报告"
echo "  4. 生成所有格式的报告"

read -p "请选择 (1-4): " choice

case $choice in
    1)
        echo -e "${BLUE}📊 生成HTML报告...${NC}"
        java -jar "$JACOCOCLI_JAR" report target/jacoco.exec \
             --classfiles target/classes \
             --sourcefiles src/main/java \
             --html report
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ HTML报告生成完成${NC}"
            echo "  报告位置: report/index.html"
        else
            echo -e "${RED}❌ HTML报告生成失败${NC}"
            exit 1
        fi
        ;;
    2)
        echo -e "${BLUE}📊 生成XML报告...${NC}"
        java -jar "$JACOCOCLI_JAR" report target/jacoco.exec \
             --classfiles target/classes \
             --sourcefiles src/main/java \
             --xml report.xml
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ XML报告生成完成${NC}"
            echo "  报告位置: report.xml"
        else
            echo -e "${RED}❌ XML报告生成失败${NC}"
            exit 1
        fi
        ;;
    3)
        echo -e "${BLUE}📊 生成CSV报告...${NC}"
        java -jar "$JACOCOCLI_JAR" report target/jacoco.exec \
             --classfiles target/classes \
             --sourcefiles src/main/java \
             --csv report.csv
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ CSV报告生成完成${NC}"
            echo "  报告位置: report.csv"
        else
            echo -e "${RED}❌ CSV报告生成失败${NC}"
            exit 1
        fi
        ;;
    4)
        echo -e "${BLUE}📊 生成所有格式的报告...${NC}"
        java -jar "$JACOCOCLI_JAR" report target/jacoco.exec \
             --classfiles target/classes \
             --sourcefiles src/main/java \
             --html report \
             --xml report.xml \
             --csv report.csv
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ 所有格式报告生成完成${NC}"
            echo "  报告位置:"
            echo "    - HTML: report/index.html"
            echo "    - XML: report.xml"
            echo "    - CSV: report.csv"
        else
            echo -e "${RED}❌ 报告生成失败${NC}"
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

if [ -d "report" ] || [ -f "report.xml" ] || [ -f "report.csv" ]; then
    echo -e "${GREEN}✅ 报告生成成功${NC}"
    
    # 复制报告到ut-scripts目录
    if [ -d "ut-scripts" ]; then
        echo -e "${BLUE}📁 复制报告到ut-scripts目录...${NC}"
        COVERAGE_DIR="ut-scripts/coverage-reports"
        mkdir -p "$COVERAGE_DIR"
        
        # 复制报告文件
        if [ -d "report" ]; then
            cp -r report "$COVERAGE_DIR/"
        fi
        if [ -f "report.xml" ]; then
            cp report.xml "$COVERAGE_DIR/"
        fi
        if [ -f "report.csv" ]; then
            cp report.csv "$COVERAGE_DIR/"
        fi
        
        # 创建带时间戳的备份
        TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
        if [ -d "report" ]; then
            cp -r report "$COVERAGE_DIR/report_$TIMESTAMP"
        fi
        
        echo -e "${GREEN}✅ 报告已复制到: $COVERAGE_DIR${NC}"
    fi
    
    # 尝试解析覆盖率数据
    if [ -f "report.csv" ]; then
        echo -e "${BLUE}📈 覆盖率统计:${NC}"
        if command -v awk >/dev/null 2>&1; then
            TOTAL_LINES=$(awk -F',' 'NR>1 {sum+=$4+$5} END {print sum}' report.csv 2>/dev/null)
            COVERED_LINES=$(awk -F',' 'NR>1 {sum+=$4} END {print sum}' report.csv 2>/dev/null)
            if [ ! -z "$TOTAL_LINES" ] && [ ! -z "$COVERED_LINES" ] && [ "$TOTAL_LINES" -gt 0 ]; then
                COVERAGE_PERCENT=$(echo "scale=1; $COVERED_LINES * 100 / $TOTAL_LINES" | bc 2>/dev/null || echo "N/A")
                echo "  指令覆盖率: ${COVERAGE_PERCENT}%"
            fi
        fi
    fi
    
    # 提供打开报告的选项
    echo ""
    echo -e "${YELLOW}💡 使用以下命令查看详细报告:${NC}"
    if [ -d "report" ]; then
        echo "  - HTML报告: report/index.html"
        echo "  - 或者运行: open report/index.html (macOS)"
        echo "  - 或者运行: xdg-open report/index.html (Linux)"
    fi
    if [ -f "report.xml" ]; then
        echo "  - XML报告: report.xml"
    fi
    if [ -f "report.csv" ]; then
        echo "  - CSV报告: report.csv"
    fi
    
else
    echo -e "${RED}❌ 报告生成失败${NC}"
    echo -e "${YELLOW}💡 请检查文件是否存在且jacococli.jar工作正常${NC}"
    exit 1
fi

echo ""
echo "=========================================="
echo "🎉 JaCoCo CLI报告重新生成完成"
echo "=========================================="
