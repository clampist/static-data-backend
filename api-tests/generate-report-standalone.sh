#!/bin/bash

# 独立覆盖率报告生成脚本
# Standalone Coverage Report Generation Script

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印函数
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# 获取脚本目录和路径
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
JACOCO_DIR="$SCRIPT_DIR/jacoco"
CLI_JAR="$JACOCO_DIR/jacococli.jar"

# 预编译包路径
PACKAGES_DIR="$PROJECT_ROOT/ut-scripts/packages"

# 工作目录
COV_DIR="$SCRIPT_DIR/cov"
TEMP_DIR="$COV_DIR/temp"
REPORT_DIR="$COV_DIR/reports"

echo "📊 独立覆盖率报告生成器"
echo "Standalone Coverage Report Generator"
echo "===================================="
echo ""

# 检查必要文件
check_prerequisites() {
    print_info "检查必要文件..."
    
    local missing_files=()
    
    # 检查 JaCoCo CLI
    if [ ! -f "$CLI_JAR" ]; then
        missing_files+=("JaCoCo CLI: $CLI_JAR")
    fi
    
    # 检查 Jacoco 数据文件
    if [ ! -f "$COV_DIR/jacoco.exec" ]; then
        missing_files+=("Jacoco数据文件: $COV_DIR/jacoco.exec")
    fi
    
    if [ ${#missing_files[@]} -gt 0 ]; then
        print_error "缺少必要文件:"
        for file in "${missing_files[@]}"; do
            echo "  - $file"
        done
        echo ""
        echo "请确保："
        echo "1. 已运行 setup-jacoco-agent.sh"
        echo "2. 已运行 api-coverage-test.sh 生成 jacoco.exec"
        exit 1
    fi
    
    print_status "必要文件检查完成"
}

# 查找最新的预编译包
find_latest_packages() {
    print_info "查找最新的预编译包..."
    
    # 查找编译产物包
    local compiled_artifacts=$(find "$PACKAGES_DIR" -name "compiled-artifacts_*.tar.gz" | sort -r | head -1)
    # 查找源码包
    local source_code=$(find "$PACKAGES_DIR" -name "source-code_*.tar.gz" | sort -r | head -1)
    
    if [ -z "$compiled_artifacts" ] || [ -z "$source_code" ]; then
        print_error "未找到预编译包文件"
        echo "请确保以下文件存在:"
        echo "  - $PACKAGES_DIR/compiled-artifacts_*.tar.gz"
        echo "  - $PACKAGES_DIR/source-code_*.tar.gz"
        exit 1
    fi
    
    print_status "找到预编译包:"
    echo "  - 编译产物: $(basename "$compiled_artifacts")"
    echo "  - 源码: $(basename "$source_code")"
    
    # 设置全局变量
    COMPILED_ARTIFACTS_TAR="$compiled_artifacts"
    SOURCE_CODE_TAR="$source_code"
}

# 复制预编译包到cov目录
copy_packages_to_cov() {
    print_info "复制预编译包到cov目录..."
    
    # 创建目录结构
    mkdir -p "$TEMP_DIR"
    mkdir -p "$REPORT_DIR"
    
    # 复制包文件到cov目录
    cp "$COMPILED_ARTIFACTS_TAR" "$COV_DIR/"
    cp "$SOURCE_CODE_TAR" "$COV_DIR/"
    
    print_status "预编译包已复制到cov目录"
    echo "  - $(basename "$COMPILED_ARTIFACTS_TAR")"
    echo "  - $(basename "$SOURCE_CODE_TAR")"
}

# 解压预编译包
extract_packages() {
    print_info "解压预编译包..."
    
    cd "$TEMP_DIR"
    
    # 解压编译产物
    print_info "解压编译产物包..."
    if tar -xzf "$COV_DIR/$(basename "$COMPILED_ARTIFACTS_TAR")"; then
        print_status "编译产物解压成功"
    else
        print_error "编译产物解压失败"
        exit 1
    fi
    
    # 解压源码
    print_info "解压源码包..."
    if tar -xzf "$COV_DIR/$(basename "$SOURCE_CODE_TAR")"; then
        print_status "源码解压成功"
    else
        print_error "源码解压失败"
        exit 1
    fi
    
    # 验证解压结果
    if [ ! -d "target/classes" ]; then
        print_error "编译产物目录未找到: target/classes"
        exit 1
    fi
    
    if [ ! -d "src/main/java" ]; then
        print_error "源码目录未找到: src/main/java"
        exit 1
    fi
    
    print_status "包解压验证完成"
    echo "  - 编译产物: $(du -sh target/classes | awk '{print $1}')"
    echo "  - 源码: $(du -sh src/main/java | awk '{print $1}')"
}

# 复制Jacoco数据到临时目录
copy_jacoco_data() {
    print_info "复制Jacoco数据到临时目录..."
    
    if [ -f "$COV_DIR/jacoco.exec" ]; then
        cp "$COV_DIR/jacoco.exec" "$TEMP_DIR/"
        print_status "Jacoco数据文件已复制"
        
        local size=$(du -h "$TEMP_DIR/jacoco.exec" | awk '{print $1}')
        echo "  - 文件大小: $size"
    else
        print_error "Jacoco数据文件未找到: $COV_DIR/jacoco.exec"
        exit 1
    fi
}

# 生成覆盖率报告
generate_coverage_report() {
    print_info "使用 JaCoCo CLI 生成覆盖率报告..."
    
    cd "$TEMP_DIR"
    
    # 创建报告目录
    local report_output_dir="report"
    mkdir -p "$report_output_dir"
    
    print_info "执行报告生成命令..."
    echo "命令: java -jar $CLI_JAR report jacoco.exec --classfiles target/classes --sourcefiles src/main/java --html $report_output_dir --csv $report_output_dir/jacoco.csv"
    
    # 重定向警告到临时文件
    local warning_file="jacoco_warnings.log"
    
    if java -jar "$CLI_JAR" report jacoco.exec \
        --classfiles target/classes \
        --sourcefiles src/main/java \
        --html "$report_output_dir" \
        --csv "$report_output_dir/jacoco.csv" > "$warning_file" 2>&1; then
        
        print_status "覆盖率报告生成成功"
        
        # 检查警告
        if [ -f "$warning_file" ] && grep -q "WARN" "$warning_file"; then
            local warning_count=$(grep -c "WARN" "$warning_file")
            print_warning "检测到 $warning_count 个类匹配警告"
            echo "  这是正常的，因为预编译的类和运行时收集的数据可能不完全匹配"
            echo "  但报告仍然有效，覆盖率数据基于实际执行的代码"
        fi
        
        # 检查报告文件
        if [ -f "$report_output_dir/index.html" ]; then
            print_status "HTML报告已生成"
        else
            print_error "HTML报告文件未找到"
            exit 1
        fi
        
        if [ -f "$report_output_dir/jacoco.csv" ]; then
            print_status "CSV报告已生成"
        else
            print_warning "CSV报告文件未找到"
        fi
        
        copy_report_to_final_location
        
    else
        print_error "覆盖率报告生成失败"
        if [ -f "$warning_file" ]; then
            echo "错误信息:"
            cat "$warning_file"
        fi
        exit 1
    fi
}

# 复制报告到最终位置
copy_report_to_final_location() {
    print_info "复制报告到最终位置..."
    
    local source_report_dir="$TEMP_DIR/report"
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local latest_dir="$REPORT_DIR/latest"
    local timestamped_dir="$REPORT_DIR/report_${timestamp}"
    
    # 创建目录
    mkdir -p "$latest_dir" "$timestamped_dir"
    
    # 复制报告文件
    if cp -r "$source_report_dir"/* "$latest_dir/"; then
        print_status "报告已复制到最新目录"
        
        # 同时复制到时间戳目录
        cp -r "$source_report_dir"/* "$timestamped_dir/"
        print_status "报告已备份到时间戳目录"
        
        # 复制 Jacoco 数据文件
        cp "$TEMP_DIR/jacoco.exec" "$latest_dir/"
        cp "$TEMP_DIR/jacoco.exec" "$timestamped_dir/"
        
        # 显示报告信息
        echo ""
        print_info "报告位置:"
        echo "  - 最新报告: $latest_dir/index.html"
        echo "  - 历史备份: $timestamped_dir/index.html"
        echo "  - Jacoco数据: $latest_dir/jacoco.exec"
        
    else
        print_error "报告复制失败"
        exit 1
    fi
}

# 显示覆盖率统计
show_coverage_stats() {
    print_info "解析覆盖率统计..."
    
    cd "$TEMP_DIR"
    
    # 生成CSV报告用于统计
    local csv_report="jacoco-stats.csv"
    if java -jar "$CLI_JAR" report jacoco.exec \
        --classfiles target/classes \
        --csv "$csv_report" >/dev/null 2>&1; then
        
        if [ -f "$csv_report" ] && command -v awk >/dev/null 2>&1; then
            echo ""
            print_info "覆盖率概览:"
            TOTAL_LINES=$(awk -F',' 'NR>1 {sum+=$4+$5} END {print sum}' "$csv_report" 2>/dev/null)
            COVERED_LINES=$(awk -F',' 'NR>1 {sum+=$4} END {print sum}' "$csv_report" 2>/dev/null)
            if [ ! -z "$TOTAL_LINES" ] && [ ! -z "$COVERED_LINES" ] && [ "$TOTAL_LINES" -gt 0 ]; then
                COVERAGE_PERCENT=$(echo "scale=1; $COVERED_LINES * 100 / $TOTAL_LINES" | bc 2>/dev/null || echo "N/A")
                echo "  指令覆盖率: ${COVERAGE_PERCENT}%"
                echo "  覆盖行数: $COVERED_LINES / $TOTAL_LINES"
            fi
            
            # 显示各包覆盖率（前10个）
            echo ""
            echo "  各包覆盖率 (前10个):"
            awk -F',' 'NR>1 && $4+$5>0 {printf "    %-50s %6.1f%%\n", $2, ($4*100)/($4+$5)}' "$csv_report" 2>/dev/null | head -10
        fi
    fi
}

# 清理函数
cleanup() {
    print_info "清理临时文件..."
    if [ -d "$TEMP_DIR" ]; then
        rm -rf "$TEMP_DIR"
    fi
    print_status "清理完成"
}

# 主函数
main() {
    echo "开始生成独立覆盖率报告..."
    echo "Starting standalone coverage report generation..."
    echo ""
    
    # 设置信号处理
    trap cleanup EXIT
    trap 'cleanup; exit 1' INT TERM
    
    # 执行步骤
    check_prerequisites
    echo ""
    
    find_latest_packages
    echo ""
    
    copy_packages_to_cov
    echo ""
    
    extract_packages
    echo ""
    
    copy_jacoco_data
    echo ""
    
    generate_coverage_report
    echo ""
    
    show_coverage_stats
    echo ""
    
    print_status "独立覆盖率报告生成完成！"
    echo ""
    echo "📋 总结:"
    echo "  - 已复制预编译包到 cov 目录"
    echo "  - 已生成覆盖率报告"
    echo "  - 详细报告请查看: api-tests/cov/reports/latest/index.html"
    echo "  - Jacoco数据文件: api-tests/cov/jacoco.exec"
    echo ""
    echo "💡 使用方法:"
    echo "  - 打开报告: open api-tests/cov/reports/latest/index.html"
    echo "  - 或使用浏览器访问: file://$(realpath "$REPORT_DIR/latest/index.html")"
    echo ""
}

# 运行主函数
main