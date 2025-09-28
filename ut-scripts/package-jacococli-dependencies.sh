#!/bin/bash

# JaCoCo CLI报告生成依赖打包脚本
# 专门用于使用 jacococli.jar 生成报告的场景

echo "=========================================="
echo "📦 打包JaCoCo CLI报告生成依赖"
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

# 1. 打包编译产物（JaCoCo CLI必需）
echo ""
echo -e "${BLUE}📦 1. 打包编译产物（JaCoCo CLI必需）...${NC}"
COMPILED_PACKAGE="$PACKAGES_DIR/compiled-artifacts_$TIMESTAMP.tar.gz"

if [ -d "target/classes" ]; then
    echo "  包含文件:"
    echo "    - target/classes/ (编译后的字节码文件)"
    
    tar -czf "$COMPILED_PACKAGE" \
        target/classes \
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

# 2. 打包源码（用于生成带源码的报告）
echo ""
echo -e "${BLUE}📦 2. 打包源码（用于生成带源码的报告）...${NC}"
SOURCE_PACKAGE="$PACKAGES_DIR/source-code_$TIMESTAMP.tar.gz"

if [ -d "src/main/java" ]; then
    echo "  包含文件:"
    echo "    - src/main/java/ (源代码文件)"
    
    tar -czf "$SOURCE_PACKAGE" \
        src/main/java \
        2>/dev/null
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 源码打包成功: $(basename "$SOURCE_PACKAGE")${NC}"
        echo "  大小: $(du -h "$SOURCE_PACKAGE" | cut -f1)"
    else
        echo -e "${YELLOW}⚠️  源码打包失败或目录不存在${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  未找到源码目录，跳过源码打包${NC}"
fi

# 3. 打包测试执行数据（JaCoCo CLI核心数据）
echo ""
echo -e "${BLUE}📦 3. 打包测试执行数据（JaCoCo CLI核心数据）...${NC}"
TEST_DATA_PACKAGE="$PACKAGES_DIR/test-execution-data_$TIMESTAMP.tar.gz"

echo "  包含文件:"
echo "    - target/jacoco.exec (测试执行数据 - JaCoCo agent记录的核心数据)"

if [ -f "target/jacoco.exec" ]; then
    tar -czf "$TEST_DATA_PACKAGE" \
        target/jacoco.exec \
        2>/dev/null
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 测试执行数据打包成功: $(basename "$TEST_DATA_PACKAGE")${NC}"
        echo "  大小: $(du -h "$TEST_DATA_PACKAGE" | cut -f1)"
    else
        echo -e "${YELLOW}⚠️  测试执行数据打包失败${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  未找到jacoco.exec文件，跳过测试执行数据打包${NC}"
fi

# 4. 创建完整JaCoCo CLI报告生成包
echo ""
echo -e "${BLUE}📦 4. 创建完整JaCoCo CLI报告生成包...${NC}"
COMPLETE_PACKAGE="$PACKAGES_DIR/complete-jacococli-package_$TIMESTAMP.tar.gz"

echo "  包含文件:"
echo "    - 编译产物 (target/classes/)"
echo "    - 源码 (src/main/java/)"
echo "    - 测试执行数据 (target/jacoco.exec)"

tar -czf "$COMPLETE_PACKAGE" \
    target/classes \
    src/main/java \
    target/jacoco.exec \
    2>/dev/null

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ 完整JaCoCo CLI报告生成包打包成功: $(basename "$COMPLETE_PACKAGE")${NC}"
    echo "  大小: $(du -h "$COMPLETE_PACKAGE" | cut -f1)"
else
    echo -e "${YELLOW}⚠️  完整JaCoCo CLI报告生成包打包失败${NC}"
fi

# 5. 复制jacoco.exec文件到packages目录（单独提供）
echo ""
echo -e "${BLUE}📦 5. 复制jacoco.exec文件到packages目录...${NC}"

if [ -f "target/jacoco.exec" ]; then
    echo "  复制jacoco.exec文件..."
    cp target/jacoco.exec "$PACKAGES_DIR/"
    echo -e "${GREEN}✅ jacoco.exec已复制到packages目录${NC}"
    echo "  大小: $(du -h "$PACKAGES_DIR/jacoco.exec" | cut -f1)"
else
    echo -e "${YELLOW}⚠️  未找到jacoco.exec文件${NC}"
    echo -e "${YELLOW}💡 请确保已运行测试并生成了jacoco.exec文件${NC}"
fi

# 6. 创建打包信息文件
echo ""
echo -e "${BLUE}📄 6. 创建打包信息文件...${NC}"
INFO_FILE="$PACKAGES_DIR/package-info_$TIMESTAMP.txt"

cat > "$INFO_FILE" << EOF
JaCoCo CLI报告生成依赖打包信息
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

# 添加jacoco.exec文件信息
if [ -f "$PACKAGES_DIR/jacoco.exec" ]; then
    echo "- jacoco.exec ($(du -h "$PACKAGES_DIR/jacoco.exec" | cut -f1))" >> "$INFO_FILE"
fi

cat >> "$INFO_FILE" << EOF

使用说明:
1. 编译产物包: 包含已编译的class文件，JaCoCo CLI需要字节码文件
2. 源码包: 包含源代码文件，用于生成带源码的报告
3. 测试执行数据包: 包含jacoco.exec（核心数据），JaCoCo agent记录的执行数据
4. 完整JaCoCo CLI报告生成包: 包含所有必需文件，推荐使用
5. jacoco.exec: 单独提供的测试执行数据文件

JaCoCo CLI报告生成的三类必需依赖:
✅ 1. 编译产物 (target/classes/) - JaCoCo CLI需要字节码文件
✅ 2. 源码 (src/main/java/) - 用于生成带源码的报告
✅ 3. 测试执行数据 (jacoco.exec) - JaCoCo agent记录的核心数据

生成报告命令:
java -jar jacococli.jar report jacoco.exec \\
     --classfiles target/classes \\
     --sourcefiles src/main/java \\
     --html report

解包和重新生成:
1. 解压完整JaCoCo CLI报告生成包到目标目录（推荐）
2. 运行 ut-scripts/restore-and-regenerate-jacococli.sh 脚本
3. 或者手动解压各个包并运行上述命令

注意事项:
- 确保目标环境有Java 17环境
- 需要下载 jacococli.jar 文件
- 完整包包含所有必需文件，可直接生成报告
- jacoco.exec文件已单独复制到packages目录，便于单独使用
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

# 显示jacoco.exec文件
if [ -f "$PACKAGES_DIR/jacoco.exec" ]; then
    echo "  $(ls -lh "$PACKAGES_DIR/jacoco.exec" | awk '{print "  -rw-r--r-- 1 " $3 " " $4 " " $5 " " $6 " " $7 " " $8 " " $9}')"
fi

echo ""
echo -e "${YELLOW}💡 使用建议:${NC}"
echo "  1. 将整个 packages/ 目录传输到远程环境"
echo "  2. 运行 ut-scripts/restore-and-regenerate-jacococli.sh 重新生成报告"
echo "  3. 或者手动解压需要的包"
echo "  4. jacoco.exec文件已单独提供，可直接使用"

echo ""
echo "=========================================="
