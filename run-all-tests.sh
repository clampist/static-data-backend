#!/bin/bash

# 后端测试运行脚本
# 用于运行所有测试并生成详细的测试报告

echo "=========================================="
echo "🚀 开始运行后端所有测试"
echo "=========================================="

# 设置颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 记录开始时间
START_TIME=$(date +%s)

# 清理之前的测试结果
echo -e "${BLUE}📁 清理之前的测试结果...${NC}"
mvn clean -q

# 编译项目
echo -e "${BLUE}🔨 编译项目...${NC}"
if ! mvn compile -q; then
    echo -e "${RED}❌ 项目编译失败${NC}"
    exit 1
fi

# 编译测试代码
echo -e "${BLUE}🧪 编译测试代码...${NC}"
if ! mvn test-compile -q; then
    echo -e "${RED}❌ 测试代码编译失败${NC}"
    exit 1
fi

echo -e "${GREEN}✅ 编译成功${NC}"

# 运行所有测试
echo -e "${BLUE}🏃 运行所有测试...${NC}"
echo ""

# 运行测试并捕获输出
TEST_OUTPUT=$(mvn test 2>&1)
TEST_EXIT_CODE=$?

# 记录结束时间
END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

echo ""
echo "=========================================="
echo "📊 测试结果总结"
echo "=========================================="

# 解析测试结果
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}🎉 所有测试通过！${NC}"
else
    echo -e "${RED}❌ 有测试失败${NC}"
fi

# 提取测试统计信息
TESTS_RUN=$(echo "$TEST_OUTPUT" | grep -o "Tests run: [0-9]*" | tail -1 | grep -o "[0-9]*")
FAILURES=$(echo "$TEST_OUTPUT" | grep -o "Failures: [0-9]*" | tail -1 | grep -o "[0-9]*")
ERRORS=$(echo "$TEST_OUTPUT" | grep -o "Errors: [0-9]*" | tail -1 | grep -o "[0-9]*")
SKIPPED=$(echo "$TEST_OUTPUT" | grep -o "Skipped: [0-9]*" | tail -1 | grep -o "[0-9]*")

# 显示测试统计
echo -e "${BLUE}📈 测试统计:${NC}"
echo "  总测试数: $TESTS_RUN"
echo "  通过: $((TESTS_RUN - FAILURES - ERRORS - SKIPPED))"
echo "  失败: $FAILURES"
echo "  错误: $ERRORS"
echo "  跳过: $SKIPPED"
echo "  耗时: ${DURATION}秒"

# 显示测试类结果
echo ""
echo -e "${BLUE}📋 测试类结果:${NC}"
echo "$TEST_OUTPUT" | grep -E "(Tests run:|Running|PASSED|FAILED)" | while read line; do
    if [[ $line == *"Tests run:"* ]]; then
        echo "  $line"
    elif [[ $line == *"Running"* ]]; then
        echo "  $line"
    fi
done

# 如果有失败的测试，显示详细信息
if [ $FAILURES -gt 0 ] || [ $ERRORS -gt 0 ]; then
    echo ""
    echo -e "${RED}❌ 失败的测试详情:${NC}"
    echo "$TEST_OUTPUT" | grep -A 5 -B 5 "FAILURE\|ERROR" | head -20
fi

# 生成测试报告
echo ""
echo -e "${BLUE}📄 生成测试报告...${NC}"
REPORT_DIR="target/surefire-reports"
if [ -d "$REPORT_DIR" ]; then
    echo "  测试报告位置: $REPORT_DIR"
    echo "  主要报告文件:"
    find "$REPORT_DIR" -name "*.txt" -type f | head -5 | while read file; do
        echo "    - $(basename "$file")"
    done
fi

# 检查测试覆盖率（如果有的话）
echo ""
echo -e "${BLUE}📊 检查测试覆盖率...${NC}"
if [ -d "target/site/jacoco" ]; then
    echo -e "${GREEN}✅ 找到测试覆盖率报告${NC}"
    echo "  覆盖率报告: target/site/jacoco/index.html"
else
    echo -e "${YELLOW}⚠️  未找到测试覆盖率报告${NC}"
fi

# 最终结果
echo ""
echo "=========================================="
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}🎉 测试完成 - 所有测试通过！${NC}"
else
    echo -e "${RED}❌ 测试完成 - 有测试失败${NC}"
    echo -e "${YELLOW}💡 建议检查失败的测试并修复问题${NC}"
fi
echo "=========================================="

# 返回测试退出码
exit $TEST_EXIT_CODE
