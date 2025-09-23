#!/bin/bash

# 数据文件查询API修复验证脚本
# Data File Query API Fix Verification Script

echo "🔧 数据文件查询API修复验证"
echo "🔧 Data File Query API Fix Verification"
echo "=========================================="

# 设置基础URL
BASE_URL="http://localhost:8080/api"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 函数：打印带颜色的消息
print_status() {
    local status=$1
    local message=$2
    case $status in
        "SUCCESS")
            echo -e "${GREEN}✅ $message${NC}"
            ;;
        "ERROR")
            echo -e "${RED}❌ $message${NC}"
            ;;
        "WARNING")
            echo -e "${YELLOW}⚠️  $message${NC}"
            ;;
        "INFO")
            echo -e "${BLUE}ℹ️  $message${NC}"
            ;;
    esac
}

# 检查服务是否运行
check_service() {
    print_status "INFO" "检查后端服务状态..."
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        print_status "SUCCESS" "后端服务正在运行"
        return 0
    else
        print_status "ERROR" "后端服务未运行，请先启动服务"
        return 1
    fi
}

# 登录获取token
login() {
    print_status "INFO" "正在登录..."
    local response=$(curl -s -X POST "$BASE_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"username":"admin","password":"admin123"}')
    
    if echo "$response" | grep -q "token"; then
        TOKEN=$(echo "$response" | jq -r '.token')
        print_status "SUCCESS" "登录成功，获取到token"
        return 0
    else
        print_status "ERROR" "登录失败: $response"
        return 1
    fi
}

# 测试数据文件查询API
test_datafile_query() {
    print_status "INFO" "测试数据文件查询API..."
    
    local query_data='{
        "page": 1,
        "size": 10,
        "sortBy": "createdAt",
        "sortDirection": "desc"
    }'
    
    local response=$(curl -s -X POST "$BASE_URL/data-files/query" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d "$query_data")
    
    if echo "$response" | grep -q "content"; then
        print_status "SUCCESS" "数据文件查询API正常"
        echo "响应内容:"
        echo "$response" | jq '.'
        return 0
    else
        print_status "ERROR" "数据文件查询API失败: $response"
        return 1
    fi
}

# 测试数据文件统计API
test_datafile_statistics() {
    print_status "INFO" "测试数据文件统计API..."
    
    local response=$(curl -s -X GET "$BASE_URL/data-files/statistics" \
        -H "Authorization: Bearer $TOKEN")
    
    if echo "$response" | grep -q "totalFiles"; then
        print_status "SUCCESS" "数据文件统计API正常"
        echo "统计信息:"
        echo "$response" | jq '.'
        return 0
    else
        print_status "ERROR" "数据文件统计API失败: $response"
        return 1
    fi
}

# 测试组织节点API
test_organization_tree() {
    print_status "INFO" "测试组织节点API..."
    
    local response=$(curl -s -X GET "$BASE_URL/organization/tree" \
        -H "Authorization: Bearer $TOKEN")
    
    if echo "$response" | grep -q "id"; then
        print_status "SUCCESS" "组织节点API正常"
        echo "组织树结构:"
        echo "$response" | jq '.'
        return 0
    else
        print_status "ERROR" "组织节点API失败: $response"
        return 1
    fi
}

# 创建测试数据文件
create_test_datafile() {
    print_status "INFO" "创建测试数据文件..."
    
    # 首先获取一个MODULE类型的组织节点
    local org_response=$(curl -s -X GET "$BASE_URL/organization/tree" \
        -H "Authorization: Bearer $TOKEN")
    
    local module_id=$(echo "$org_response" | jq -r '.[] | select(.nodeType == "MODULE") | .id' | head -1)
    
    if [ "$module_id" = "null" ] || [ -z "$module_id" ]; then
        print_status "WARNING" "没有找到MODULE类型的组织节点，跳过创建测试数据文件"
        return 1
    fi
    
    local test_data='{
        "name": "测试数据文件",
        "description": "用于测试的数据文件",
        "organizationNodeId": '$module_id',
        "accessLevel": "PUBLIC",
        "columnDefinitions": [
            {
                "name": "id",
                "dataType": "INTEGER",
                "description": "主键ID"
            },
            {
                "name": "name",
                "dataType": "STRING",
                "description": "名称"
            }
        ],
        "dataRows": [
            {
                "id": 1,
                "name": "测试数据1"
            },
            {
                "id": 2,
                "name": "测试数据2"
            }
        ]
    }'
    
    local response=$(curl -s -X POST "$BASE_URL/data-files" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d "$test_data")
    
    if echo "$response" | grep -q "id"; then
        print_status "SUCCESS" "测试数据文件创建成功"
        echo "创建的数据文件:"
        echo "$response" | jq '.'
        return 0
    else
        print_status "ERROR" "测试数据文件创建失败: $response"
        return 1
    fi
}

# 主函数
main() {
    echo "开始验证数据文件查询API修复..."
    echo "Starting data file query API fix verification..."
    echo ""
    
    # 检查服务
    if ! check_service; then
        exit 1
    fi
    
    # 登录
    if ! login; then
        exit 1
    fi
    
    echo ""
    print_status "INFO" "开始API测试..."
    echo ""
    
    # 测试组织节点API
    test_organization_tree
    echo ""
    
    # 测试数据文件统计API
    test_datafile_statistics
    echo ""
    
    # 创建测试数据文件
    create_test_datafile
    echo ""
    
    # 测试数据文件查询API
    test_datafile_query
    echo ""
    
    print_status "SUCCESS" "所有测试完成！"
    echo ""
    print_status "INFO" "如果所有测试都通过，说明PostgreSQL bytea问题已修复"
    print_status "INFO" "If all tests pass, the PostgreSQL bytea issue has been fixed"
}

# 运行主函数
main
