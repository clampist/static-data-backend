#!/bin/bash

# 快速API测试脚本 - 仅负责调用API
# Quick API Test Script - Only Responsible for API Calls

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

# 获取脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 应用配置
BASE_URL="http://localhost:8080/api"
APP_PORT=8080

echo "⚡ 快速API测试"
echo "Quick API Test"
echo "=============="
echo ""

# 检查后端服务状态
check_service_status() {
    print_info "检查后端服务状态..."
    
    if curl -s "http://localhost:$APP_PORT/api/actuator/health" > /dev/null 2>&1; then
        print_status "后端服务正在运行"
        return 0
    else
        print_error "后端服务未运行"
        echo "请先启动后端服务: cd .. && mvn spring-boot:run"
        exit 1
    fi
}

# 运行快速API测试
run_quick_api_tests() {
    print_info "运行快速API测试..."
    
    # 运行核心测试（快速版本）
    local tests=(
        "test-auth-apis.sh:认证API测试"
        "test-organization-apis.sh:组织管理API测试"
        "test-simple-datafile.sh:简单数据文件测试"
        "test-simple-query.sh:简单查询测试"
    )
    
    local test_results=()
    local failed_tests=()
    
    for test_info in "${tests[@]}"; do
        IFS=':' read -r test_script test_name <<< "$test_info"
        
        print_info "运行测试: $test_name"
        
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
            print_warning "测试脚本不存在: $test_script_path"
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
        return 1
    else
        echo ""
        print_status "所有快速API测试通过！"
        return 0
    fi
}

# 主函数
main() {
    echo "开始快速API测试..."
    echo "Starting Quick API Test..."
    echo ""
    
    # 执行步骤
    check_service_status
    echo ""
    
    run_quick_api_tests
    echo ""
    
    print_status "快速API测试完成！"
    echo ""
    echo "📋 总结:"
    echo "  - 快速API测试已完成"
    echo "  - 本脚本只负责调用API，不涉及覆盖率操作"
    echo "  - 如需覆盖率测试，请运行: ./api-coverage-test.sh"
    echo ""
}

# 运行主函数
main