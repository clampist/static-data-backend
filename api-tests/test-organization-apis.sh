#!/bin/bash

# Organization Management API Testing Script
# 组织管理API测试脚本

BASE_URL="http://localhost:8080/api"
TOKEN=""

echo "=== Organization Management API Testing ==="
echo "组织管理API测试"
echo ""

# Function to get auth token
get_token() {
    echo "Getting authentication token..."
    echo "获取认证token..."
    
    RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"username":"testuser","password":"password123"}')
    
    TOKEN=$(echo $RESPONSE | jq -r '.accessToken')
    
    if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
        echo "❌ Failed to get authentication token"
        echo "❌ 获取认证token失败"
        echo "Response: $RESPONSE"
        exit 1
    fi
    
    echo "✅ Token obtained successfully"
    echo "✅ 成功获取token"
    echo ""
}

# Function to test API endpoint
test_endpoint() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    
    echo "Testing: $description"
    echo "测试: $description"
    echo "Endpoint: $method $endpoint"
    
    if [ "$method" = "GET" ]; then
        RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" "$BASE_URL$endpoint")
    elif [ "$method" = "POST" ]; then
        RESPONSE=$(curl -s -X POST "$BASE_URL$endpoint" \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json" \
            -d "$data")
    elif [ "$method" = "PUT" ]; then
        RESPONSE=$(curl -s -X PUT "$BASE_URL$endpoint" \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: application/json" \
            -d "$data")
    elif [ "$method" = "DELETE" ]; then
        RESPONSE=$(curl -s -X DELETE "$BASE_URL$endpoint" \
            -H "Authorization: Bearer $TOKEN")
    fi
    
    echo "Response:"
    echo "$RESPONSE" | jq . 2>/dev/null || echo "$RESPONSE"
    echo ""
}

# Main testing flow
main() {
    # Get authentication token
    get_token
    
    echo "=== Testing Organization APIs ==="
    echo "=== 测试组织管理API ==="
    echo ""
    
    # 1. Get organization tree (should be empty initially)
    test_endpoint "GET" "/organization/tree" "" "Get organization tree / 获取组织树"
    
    # 2. Get node types
    test_endpoint "GET" "/organization/node-types" "" "Get node types / 获取节点类型"
    
    # 3. Create root organization node
    test_endpoint "POST" "/organization/nodes" '{
        "name": "Headquarters",
        "description": "Main company headquarters",
        "type": "DEPARTMENT",
        "sortOrder": 1
    }' "Create root organization node / 创建根组织节点"
    
    # 4. Create child organization node
    test_endpoint "POST" "/organization/nodes" '{
        "name": "Product Department",
        "description": "Product development department",
        "type": "DEPARTMENT",
        "parentId": 1,
        "sortOrder": 1
    }' "Create child organization node / 创建子组织节点"
    
    # 5. Create team node
    test_endpoint "POST" "/organization/nodes" '{
        "name": "Frontend Team",
        "description": "Frontend development team",
        "type": "TEAM",
        "parentId": 2,
        "sortOrder": 1
    }' "Create team node / 创建团队节点"
    
    # 6. Get organization tree (should show hierarchy)
    test_endpoint "GET" "/organization/tree" "" "Get organization tree with data / 获取包含数据的组织树"
    
    # 7. Get children of specific parent
    test_endpoint "GET" "/organization/nodes?parentId=2" "" "Get children of parent ID 2 / 获取父节点ID为2的子节点"
    
    # 8. Get specific node details
    test_endpoint "GET" "/organization/nodes/3" "" "Get node details / 获取节点详情"
    
    # 9. Update node
    test_endpoint "PUT" "/organization/nodes/3" '{
        "name": "Frontend Development Team",
        "description": "Team responsible for frontend interface development",
        "sortOrder": 1
    }' "Update node / 更新节点"
    
    # 10. Get node statistics
    test_endpoint "GET" "/organization/nodes/2/stats" "" "Get node statistics / 获取节点统计信息"
    
    # 10.5. Create business direction node
    test_endpoint "POST" "/organization/nodes" '{
        "name": "Web Development",
        "description": "Web application development",
        "type": "BUSINESS_DIRECTION",
        "parentId": 3,
        "sortOrder": 1
    }' "Create business direction node / 创建业务方向节点"
    
    # 10.6. Create module nodes for data file testing
    test_endpoint "POST" "/organization/nodes" '{
        "name": "User Dashboard",
        "description": "Main user interface and dashboard",
        "type": "MODULE",
        "parentId": 4,
        "sortOrder": 1
    }' "Create module node / 创建模块节点"
    
    test_endpoint "POST" "/organization/nodes" '{
        "name": "Data Management",
        "description": "Data file and content management",
        "type": "MODULE",
        "parentId": 4,
        "sortOrder": 2
    }' "Create module node / 创建模块节点"
    
    # 11. Move node
    test_endpoint "PUT" "/organization/nodes/3/move" '{
        "parentId": 1
    }' "Move node to different parent / 移动节点到不同的父节点"
    
    # 12. Search nodes
    test_endpoint "GET" "/organization/search?keyword=前端" "" "Search nodes / 搜索节点"
    
    echo "=== Testing Complete ==="
    echo "=== 测试完成 ==="
    echo ""
    echo "✅ All organization management APIs have been tested!"
    echo "✅ 所有组织管理API已测试完成！"
}

# Run the tests
main
