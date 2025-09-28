#!/bin/bash

# API覆盖率测试脚本 - Agent模式启动程序
# API Coverage Test Script - Agent Mode Application Launcher
# 
# 流程:
# 1. 清理Jacoco数据
# 2. 编译项目
# 3. 构建前清理项目
# 4. 构建JAR包
# 5. 启动应用（JaCoCo Agent TCP模式）
# 6. 运行API测试
# 7. Dump覆盖率数据
# 8. 停止应用

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
AGENT_JAR="$JACOCO_DIR/jacocoagent.jar"

# 应用相关配置
APP_PORT=8080
APP_PID_FILE="$SCRIPT_DIR/.app.pid"
JACOCO_EXEC_FILE="$PROJECT_ROOT/jacoco.exec"

echo "🚀 API覆盖率测试 - Agent模式启动程序"
echo "API Coverage Test - Agent Mode Launcher"
echo "======================================"
echo ""

# 检查必要工具
check_prerequisites() {
    print_info "检查必要工具..."
    
    local missing_tools=()
    
    # 检查Java
    if ! command -v java >/dev/null 2>&1; then
        missing_tools+=("java")
    fi
    
    # 检查Maven
    if ! command -v mvn >/dev/null 2>&1; then
        missing_tools+=("mvn")
    fi
    
    # 检查JaCoCo Agent
    if [ ! -f "$AGENT_JAR" ]; then
        missing_tools+=("JaCoCo Agent: $AGENT_JAR")
    fi
    
    if [ ${#missing_tools[@]} -gt 0 ]; then
        print_error "缺少必要工具: ${missing_tools[*]}"
        echo "请安装缺少的工具或运行: ./setup-jacoco-agent.sh"
        exit 1
    fi
    
    print_status "必要工具检查完成"
}

# 清理之前的覆盖率数据
clean_jacoco_data() {
    print_info "清理之前的Jacoco覆盖率数据..."
    
    cd "$PROJECT_ROOT"
    
    # 清理Jacoco数据文件
    rm -f jacoco*.exec 2>/dev/null || true
    rm -rf target/site/jacoco 2>/dev/null || true
    
    print_status "Jacoco数据清理完成"
    
    cd "$SCRIPT_DIR"
}

# 编译项目
compile_project() {
    print_info "编译项目..."
    
    cd "$PROJECT_ROOT"
    
    if mvn compile -q; then
        print_status "项目编译成功"
    else
        print_error "项目编译失败"
        exit 1
    fi
    
    cd "$SCRIPT_DIR"
}

# 构建前清理
clean_before_build() {
    print_info "构建前清理项目..."
    
    cd "$PROJECT_ROOT"
    
    if mvn clean -q; then
        print_status "项目清理成功"
    else
        print_error "项目清理失败"
        exit 1
    fi
    
    cd "$SCRIPT_DIR"
}

# 构建JAR包
build_jar_package() {
    print_info "构建JAR包..."
    
    cd "$PROJECT_ROOT"
    
    if mvn package -DskipTests -q; then
        print_status "JAR包构建成功"
        
        # 验证JAR包
        local target_jar="$PROJECT_ROOT/target/platform-0.0.1-SNAPSHOT.jar"
        if [ -f "$target_jar" ]; then
            local size=$(du -h "$target_jar" | awk '{print $1}')
            print_status "JAR包已生成: $(basename "$target_jar") ($size)"
        else
            print_error "JAR包文件未找到"
            exit 1
        fi
    else
        print_error "JAR包构建失败"
        exit 1
    fi
    
    cd "$SCRIPT_DIR"
}

# 启动应用（使用JaCoCo Agent）
start_app_with_agent() {
    print_info "启动应用（使用JaCoCo Agent）..."
    
    cd "$PROJECT_ROOT"
    
    # 构建JaCoCo Agent参数（TCP模式）
    local agent_args="-javaagent:$AGENT_JAR=destfile=jacoco.exec,output=tcpserver,address=*,port=6300,append=false"
    
    # 目标JAR包路径
    local target_jar="$PROJECT_ROOT/target/platform-0.0.1-SNAPSHOT.jar"
    
    print_info "启动命令: java $agent_args -jar $target_jar"
    
    # 启动应用
    java $agent_args -jar "$target_jar" &
    local app_pid=$!
    echo $app_pid > "$APP_PID_FILE"
    
    # 等待应用启动
    print_info "等待应用启动..."
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "http://localhost:$APP_PORT/api/actuator/health" > /dev/null 2>&1; then
            print_status "应用启动成功，PID: $app_pid"
            print_info "JaCoCo Agent已附加，开始收集覆盖率数据"
            return 0
        fi
        
        print_info "等待应用启动... ($attempt/$max_attempts)"
        sleep 2
        attempt=$((attempt + 1))
    done
    
    print_error "应用启动超时"
    kill $app_pid 2>/dev/null || true
    rm -f "$APP_PID_FILE"
    exit 1
}

# 运行API测试
run_api_tests() {
    print_info "运行API接口测试..."
    
    local test_results=()
    local failed_tests=()
    
    # 测试脚本列表
    local tests=(
        "test-auth-apis.sh:认证API测试"
        "test-organization-apis.sh:组织管理API测试"
        "test-final-datafile.sh:数据文件API测试"
        "test-datafile-query-fix.sh:数据文件查询修复验证"
        "test-final-fix.sh:最终修复验证"
        "test-frontend-integration.sh:前端集成测试"
        "test-simple-datafile.sh:简单数据文件测试"
        "test-simple-query.sh:简单查询测试"
    )
    
    # 运行每个测试
    for test_info in "${tests[@]}"; do
        IFS=':' read -r test_script test_name <<< "$test_info"
        
        print_info "运行测试: $test_name"
        
        # 运行测试脚本
        local test_script_path="$SCRIPT_DIR/$test_script"
        
        if [ -f "$test_script_path" ] && [ -x "$test_script_path" ]; then
            if "$test_script_path"; then
                print_status "$test_name - 通过"
                test_results+=("✅ $test_name")
            else
                print_error "$test_name - 失败"
                test_results+=("❌ $test_name")
                failed_tests+=("$test_name")
            fi
        else
            print_warning "测试脚本不存在或无执行权限: $test_script_path"
            test_results+=("⚠️  $test_name (脚本不存在)")
        fi
        
        echo ""
    done
    
    # 显示测试结果汇总
    echo "📊 测试结果汇总:"
    echo "=================="
    for result in "${test_results[@]}"; do
        echo "  $result"
    done
    
    if [ ${#failed_tests[@]} -gt 0 ]; then
        echo ""
        print_warning "有 ${#failed_tests[@]} 个测试失败"
        echo "失败的测试:"
        for failed_test in "${failed_tests[@]}"; do
            echo "  - $failed_test"
        done
    else
        echo ""
        print_status "所有API测试通过！"
    fi
}

# Dump JaCoCo覆盖率数据
dump_jacoco_data() {
    print_info "Dump JaCoCo覆盖率数据..."
    
    # 清理cov目录
    if [ -d "$SCRIPT_DIR/cov" ]; then
        rm -rf "$SCRIPT_DIR/cov"/*
        print_info "已清理cov目录"
    fi
    
    # 创建cov目录
    mkdir -p "$SCRIPT_DIR/cov"
    
    # 使用JaCoCo CLI dump数据
    local cli_jar="$JACOCO_DIR/jacococli.jar"
    if [ -f "$cli_jar" ]; then
        print_info "使用JaCoCo CLI dump数据到: $SCRIPT_DIR/cov/jacoco.exec"
        if java -jar "$cli_jar" dump --address localhost --port 6300 --destfile "$SCRIPT_DIR/cov/jacoco.exec"; then
            print_status "JaCoCo数据dump成功"
            
            # 显示文件信息
            if [ -f "$SCRIPT_DIR/cov/jacoco.exec" ]; then
                local size=$(du -h "$SCRIPT_DIR/cov/jacoco.exec" | awk '{print $1}')
                echo "  - 文件大小: $size"
            fi
        else
            print_warning "JaCoCo数据dump失败，可能应用未运行或端口不可用"
        fi
    else
        print_error "JaCoCo CLI工具未找到: $cli_jar"
        print_info "请先运行: ./setup-jacoco-agent.sh"
    fi
}

# 停止应用
stop_app() {
    print_info "停止应用..."
    
    if [ -f "$APP_PID_FILE" ]; then
        local app_pid=$(cat "$APP_PID_FILE")
        if kill $app_pid 2>/dev/null; then
            print_status "应用已停止 (PID: $app_pid)"
        else
            print_warning "应用可能已经停止"
        fi
        rm -f "$APP_PID_FILE"
    else
        print_warning "未找到应用PID文件"
    fi
    
    # 强制清理可能的残留进程
    pkill -f "platform-0.0.1-SNAPSHOT.jar" 2>/dev/null || true
}


# 清理函数
cleanup() {
    print_info "清理临时文件..."
    rm -f "$APP_PID_FILE"
    print_status "清理完成"
}

# 主函数
main() {
    echo "开始API覆盖率测试（Agent模式）..."
    echo "Starting API Coverage Test (Agent Mode)..."
    echo ""
    
    # 设置信号处理
    trap cleanup EXIT
    trap 'stop_app; cleanup; exit 1' INT TERM
    
    # 执行步骤
    check_prerequisites
    echo ""
    
    clean_jacoco_data
    echo ""
    
    compile_project
    echo ""
    
    clean_before_build
    echo ""
    
    build_jar_package
    echo ""
    
    start_app_with_agent
    echo ""
    
    run_api_tests
    echo ""
    
    dump_jacoco_data
    echo ""
    
    stop_app
    echo ""
    echo ""
    
        print_status "API覆盖率测试完成！"
        echo ""
        echo "📋 总结:"
        echo "  - JAR包已构建完成"
        echo "  - 应用已使用JaCoCo Agent (TCP模式) 启动并停止"
        echo "  - API接口测试已完成"
        echo "  - Jacoco数据已通过TCP dump到: api-tests/cov/jacoco.exec"
        echo "  - 下一步: 运行 ./generate-report-standalone.sh 生成报告"
        echo ""
}

# 运行主函数
main