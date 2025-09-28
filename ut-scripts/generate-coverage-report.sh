#!/bin/bash

# Jacoco代码覆盖率报告生成脚本
# 用于生成详细的代码覆盖率报告

echo "=========================================="
echo "📊 生成Jacoco代码覆盖率报告"
echo "=========================================="

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 记录开始时间
START_TIME=$(date +%s)

# 清理并运行测试生成覆盖率数据
echo -e "${BLUE}🧹 清理项目...${NC}"
mvn clean -q

echo -e "${BLUE}🔨 编译项目...${NC}"
if ! mvn compile test-compile -q; then
    echo -e "${RED}❌ 编译失败${NC}"
    exit 1
fi

echo -e "${BLUE}🧪 运行测试并收集覆盖率数据...${NC}"
if ! mvn test -q; then
    echo -e "${RED}❌ 测试运行失败${NC}"
    exit 1
fi

echo -e "${BLUE}📊 生成覆盖率报告...${NC}"
if ! mvn jacoco:report -q; then
    echo -e "${RED}❌ 覆盖率报告生成失败${NC}"
    exit 1
fi

# 记录结束时间
END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo ""
echo "=========================================="
echo "📈 覆盖率报告生成完成"
echo "=========================================="

# 检查报告是否生成成功
if [ -d "target/site/jacoco" ]; then
    echo -e "${GREEN}✅ 覆盖率报告生成成功！${NC}"
    echo "  报告位置: target/site/jacoco/index.html"
    echo "  耗时: ${DURATION}秒"
    
    # 显示覆盖率统计
    if [ -f "target/site/jacoco/jacoco.csv" ]; then
        echo ""
        echo -e "${BLUE}📊 覆盖率统计:${NC}"
        
        # 使用awk解析CSV文件
        if command -v awk >/dev/null 2>&1; then
            # 读取总体统计（跳过标题行）
            TOTAL_LINES=$(awk -F',' 'NR>1 {sum+=$4+$5} END {print sum}' target/site/jacoco/jacoco.csv 2>/dev/null)
            COVERED_LINES=$(awk -F',' 'NR>1 {sum+=$4} END {print sum}' target/site/jacoco/jacoco.csv 2>/dev/null)
            TOTAL_BRANCHES=$(awk -F',' 'NR>1 {sum+=$6+$7} END {print sum}' target/site/jacoco/jacoco.csv 2>/dev/null)
            COVERED_BRANCHES=$(awk -F',' 'NR>1 {sum+=$6} END {print sum}' target/site/jacoco/jacoco.csv 2>/dev/null)
            
            if [ ! -z "$TOTAL_LINES" ] && [ ! -z "$COVERED_LINES" ] && [ "$TOTAL_LINES" -gt 0 ]; then
                INSTRUCTION_COVERAGE=$(echo "scale=1; $COVERED_LINES * 100 / $TOTAL_LINES" | bc 2>/dev/null || echo "N/A")
                echo "  指令覆盖率: ${INSTRUCTION_COVERAGE}%"
            fi
            
            if [ ! -z "$TOTAL_BRANCHES" ] && [ ! -z "$COVERED_BRANCHES" ] && [ "$TOTAL_BRANCHES" -gt 0 ]; then
                BRANCH_COVERAGE=$(echo "scale=1; $COVERED_BRANCHES * 100 / $TOTAL_BRANCHES" | bc 2>/dev/null || echo "N/A")
                echo "  分支覆盖率: ${BRANCH_COVERAGE}%"
            fi
        fi
        
        # 显示各包的覆盖率
        echo ""
        echo -e "${BLUE}📦 各包覆盖率:${NC}"
        awk -F',' 'NR>1 && $4+$5>0 {printf "  %-30s %6.1f%%\n", $2, ($4*100)/($4+$5)}' target/site/jacoco/jacoco.csv 2>/dev/null | head -10
    fi
    
    # 检查覆盖率阈值
    echo ""
    echo -e "${BLUE}🎯 检查覆盖率阈值...${NC}"
    if mvn verify -q 2>/dev/null; then
        echo -e "${GREEN}✅ 覆盖率满足最低要求${NC}"
    else
        echo -e "${YELLOW}⚠️  覆盖率未达到最低要求${NC}"
        echo -e "${YELLOW}💡 当前阈值: 指令覆盖率 >= 60%, 分支覆盖率 >= 50%${NC}"
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
    
    # 提供打开报告的选项
    echo ""
    echo -e "${YELLOW}💡 使用以下命令查看详细报告:${NC}"
    echo "  - 原始位置: target/site/jacoco/index.html"
    echo "  - ut-scripts目录: $COVERAGE_DIR/jacoco/index.html"
    echo "  - 或者运行: open $COVERAGE_DIR/jacoco/index.html (macOS)"
    echo "  - 或者运行: xdg-open $COVERAGE_DIR/jacoco/index.html (Linux)"
    
else
    echo -e "${RED}❌ 覆盖率报告生成失败${NC}"
    echo -e "${YELLOW}💡 请检查Maven配置和测试是否正常运行${NC}"
    exit 1
fi

echo ""
echo "=========================================="
echo -e "${GREEN}🎉 覆盖率报告生成完成！${NC}"
echo "=========================================="
