#!/bin/bash

# 简化的Jacoco测试脚本
# 用于快速验证Jacoco配置是否正常工作

echo "=========================================="
echo "🧪 测试Jacoco配置"
echo "=========================================="

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 清理并运行测试
echo -e "${BLUE}🧹 清理项目...${NC}"
mvn clean -q

echo -e "${BLUE}🧪 运行测试并生成覆盖率报告...${NC}"
if mvn test jacoco:report -q; then
    echo -e "${GREEN}✅ 测试和覆盖率报告生成成功！${NC}"
    
    # 检查报告文件
    if [ -f "target/site/jacoco/index.html" ]; then
        echo -e "${GREEN}✅ 覆盖率报告文件存在${NC}"
        echo "  报告位置: target/site/jacoco/index.html"
        
        # 显示基本统计
        if [ -f "target/site/jacoco/jacoco.csv" ]; then
            echo -e "${BLUE}📊 覆盖率统计:${NC}"
            TOTAL_LINES=$(awk -F',' 'NR>1 {sum+=$4+$5} END {print sum}' target/site/jacoco/jacoco.csv 2>/dev/null)
            COVERED_LINES=$(awk -F',' 'NR>1 {sum+=$4} END {print sum}' target/site/jacoco/jacoco.csv 2>/dev/null)
            if [ ! -z "$TOTAL_LINES" ] && [ ! -z "$COVERED_LINES" ] && [ "$TOTAL_LINES" -gt 0 ]; then
                INSTRUCTION_COVERAGE=$(echo "scale=1; $COVERED_LINES * 100 / $TOTAL_LINES" | bc 2>/dev/null || echo "N/A")
                echo "  指令覆盖率: ${INSTRUCTION_COVERAGE}%"
            fi
        fi
        
        # 复制覆盖率报告到ut-scripts目录
        echo ""
        echo -e "${BLUE}📁 复制覆盖率报告到ut-scripts目录...${NC}"
        UT_SCRIPTS_DIR="$(dirname "$0")"
        COVERAGE_DIR="$UT_SCRIPTS_DIR/coverage-reports"
        mkdir -p "$COVERAGE_DIR"
        
        # 复制整个jacoco目录
        cp -r target/site/jacoco "$COVERAGE_DIR/"
        
        # 创建带时间戳的备份
        TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
        cp -r target/site/jacoco "$COVERAGE_DIR/jacoco_$TIMESTAMP"
        
        echo -e "${GREEN}✅ 覆盖率报告已复制到: $COVERAGE_DIR${NC}"
        
        echo ""
        echo -e "${GREEN}🎉 Jacoco配置测试成功！${NC}"
        echo -e "${YELLOW}💡 可以运行以下命令查看详细报告:${NC}"
        echo "  - 原始位置: open target/site/jacoco/index.html"
        echo "  - ut-scripts目录: open $COVERAGE_DIR/jacoco/index.html"
        
    else
        echo -e "${RED}❌ 覆盖率报告文件未生成${NC}"
        exit 1
    fi
else
    echo -e "${RED}❌ 测试或覆盖率报告生成失败${NC}"
    exit 1
fi

echo "=========================================="
