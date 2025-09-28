#!/bin/bash

# Jacoco报告生成依赖打包脚本
# 用于将生成Jacoco报告所需的所有文件分别打包，以便在远程环境重新生成

echo "=========================================="
echo "📦 打包Jacoco报告生成依赖"
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
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# 清理旧的打包文件并创建打包目录
echo -e "${BLUE}🧹 清理旧的打包文件...${NC}"
if [ -d "$PACKAGES_DIR" ]; then
    echo "  删除旧的打包文件..."
    rm -rf "$PACKAGES_DIR"/*
    echo -e "${GREEN}✅ 旧的打包文件已清理${NC}"
else
    echo -e "${BLUE}📁 创建打包目录...${NC}"
    mkdir -p "$PACKAGES_DIR"
fi

# 检查是否在项目根目录
if [ ! -f "pom.xml" ]; then
    echo -e "${RED}❌ 请在项目根目录（包含pom.xml的目录）运行此脚本${NC}"
    exit 1
fi

echo -e "${GREEN}✅ 当前目录: $(pwd)${NC}"

# 1. 打包编译产物（JaCoCo必需）
echo ""
echo -e "${BLUE}📦 1. 打包编译产物（JaCoCo必需）...${NC}"
COMPILED_PACKAGE="$PACKAGES_DIR/compiled-artifacts_$TIMESTAMP.tar.gz"

if [ -d "target/classes" ] || [ -d "target/test-classes" ]; then
    echo "  包含文件:"
    echo "    - target/classes/ (主代码编译产物 - JaCoCo需要字节码文件)"
    echo "    - target/test-classes/ (测试代码编译产物 - JaCoCo需要字节码文件)"
    
    tar -czf "$COMPILED_PACKAGE" \
        target/classes \
        target/test-classes \
        2>/dev/null
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 编译产物打包成功: $(basename "$COMPILED_PACKAGE")${NC}"
        echo "  大小: $(du -h "$COMPILED_PACKAGE" | cut -f1)"
    else
        echo -e "${YELLOW}⚠️  编译产物打包失败或目录不存在${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  未找到编译产物目录，跳过编译产物打包${NC}"
fi

# 2. 打包测试执行数据（JaCoCo核心数据）
echo ""
echo -e "${BLUE}📦 2. 打包测试执行数据（JaCoCo核心数据）...${NC}"
TEST_DATA_PACKAGE="$PACKAGES_DIR/test-execution-data_$TIMESTAMP.tar.gz"

echo "  包含文件:"
echo "    - target/jacoco.exec (测试执行数据 - JaCoCo agent记录的核心数据)"
echo "    - target/surefire-reports/ (测试执行结果 - 间接依赖)"
echo "    - target/failsafe-reports/ (集成测试执行结果 - 间接依赖)"

tar -czf "$TEST_DATA_PACKAGE" \
    target/jacoco.exec \
    target/surefire-reports \
    target/failsafe-reports \
    2>/dev/null

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ 测试执行数据打包成功: $(basename "$TEST_DATA_PACKAGE")${NC}"
    echo "  大小: $(du -h "$TEST_DATA_PACKAGE" | cut -f1)"
else
    echo -e "${YELLOW}⚠️  测试执行数据打包失败或文件不存在${NC}"
fi

# 3. 打包插件配置依赖（JaCoCo插件配置）
echo ""
echo -e "${BLUE}📦 3. 打包插件配置依赖（JaCoCo插件配置）...${NC}"
PLUGIN_CONFIG_PACKAGE="$PACKAGES_DIR/plugin-config_$TIMESTAMP.tar.gz"

echo "  包含文件:"
echo "    - pom.xml (Maven配置，包含JaCoCo插件配置)"
echo "    - .mvn/ (Maven包装器配置)"
echo "    - mvnw, mvnw.cmd (Maven包装器脚本)"

tar -czf "$PLUGIN_CONFIG_PACKAGE" \
    pom.xml \
    .mvn/ \
    mvnw \
    mvnw.cmd \
    2>/dev/null

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ 插件配置依赖打包成功: $(basename "$PLUGIN_CONFIG_PACKAGE")${NC}"
    echo "  大小: $(du -h "$PLUGIN_CONFIG_PACKAGE" | cut -f1)"
else
    echo -e "${YELLOW}⚠️  插件配置依赖打包失败${NC}"
fi

# 4. 创建完整JaCoCo报告生成包（包含所有三类必需依赖）
echo ""
echo -e "${BLUE}📦 4. 创建完整JaCoCo报告生成包...${NC}"
COMPLETE_PACKAGE="$PACKAGES_DIR/complete-jacoco-package_$TIMESTAMP.tar.gz"

echo "  包含文件:"
echo "    - 编译产物 (target/classes/, target/test-classes/)"
echo "    - 测试执行数据 (target/jacoco.exec, target/surefire-reports/, target/failsafe-reports/)"
echo "    - 插件配置 (pom.xml, .mvn/, mvnw, mvnw.cmd)"
echo "    - 现有报告 (target/site/jacoco/)"
echo "    - UT脚本 (ut-scripts/)"

tar -czf "$COMPLETE_PACKAGE" \
    target/classes \
    target/test-classes \
    target/jacoco.exec \
    target/surefire-reports \
    target/failsafe-reports \
    target/site/jacoco \
    pom.xml \
    .mvn/ \
    mvnw \
    mvnw.cmd \
    ut-scripts/ \
    2>/dev/null

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ 完整JaCoCo报告生成包打包成功: $(basename "$COMPLETE_PACKAGE")${NC}"
    echo "  大小: $(du -h "$COMPLETE_PACKAGE" | cut -f1)"
else
    echo -e "${YELLOW}⚠️  完整JaCoCo报告生成包打包失败${NC}"
fi

# 5. 创建打包信息文件
echo ""
echo -e "${BLUE}📄 5. 创建打包信息文件...${NC}"
INFO_FILE="$PACKAGES_DIR/package-info_$TIMESTAMP.txt"

cat > "$INFO_FILE" << EOF
Jacoco报告生成依赖打包信息
==========================================
打包时间: $(date)
项目路径: $(pwd)
Git提交: $(git rev-parse HEAD 2>/dev/null || echo "N/A")
Git分支: $(git branch --show-current 2>/dev/null || echo "N/A")

包含的包:
EOF

# 添加包信息
for package in "$PACKAGES_DIR"/*_$TIMESTAMP.tar.gz; do
    if [ -f "$package" ]; then
        echo "- $(basename "$package") ($(du -h "$package" | cut -f1))" >> "$INFO_FILE"
    fi
done

cat >> "$INFO_FILE" << EOF

使用说明:
1. 编译产物包: 包含已编译的class文件，JaCoCo需要字节码文件来计算覆盖率
2. 测试执行数据包: 包含jacoco.exec（核心数据）和测试报告，JaCoCo agent记录的执行数据
3. 插件配置包: 包含JaCoCo插件配置和Maven包装器，用于生成报告
4. 完整JaCoCo报告生成包: 包含所有三类必需依赖，推荐使用

JaCoCo报告生成的三类必需依赖:
✅ 1. 编译产物 (classes / test-classes) - JaCoCo需要字节码文件
✅ 2. 测试执行数据 (jacoco.exec) - JaCoCo agent记录的核心数据  
✅ 3. 测试运行器 (Surefire/Failsafe插件) - 通过pom.xml配置

解包和重新生成:
1. 解压完整JaCoCo报告生成包到目标目录（推荐）
2. 运行 ut-scripts/restore-and-regenerate.sh 脚本
3. 或者手动解压各个包并运行: mvn jacoco:report

注意事项:
- 确保目标环境有Java 17环境
- 完整包包含所有必需文件，可直接生成报告
- 数据库连接可能需要重新配置
EOF

echo -e "${GREEN}✅ 打包信息文件创建成功: $(basename "$INFO_FILE")${NC}"

# 显示打包结果
echo ""
echo "=========================================="
echo "📦 打包完成"
echo "=========================================="
echo -e "${GREEN}✅ 所有包已保存到: $PACKAGES_DIR${NC}"
echo ""
echo -e "${BLUE}📋 打包结果:${NC}"
ls -lh "$PACKAGES_DIR"/*_$TIMESTAMP.* | while read line; do
    echo "  $line"
done

echo ""
echo -e "${YELLOW}💡 使用建议:${NC}"
echo "  1. 将整个 packages/ 目录传输到远程环境"
echo "  2. 运行 ut-scripts/restore-and-regenerate.sh 重新生成报告"
echo "  3. 或者手动解压需要的包"

echo ""
echo "=========================================="
