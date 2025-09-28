#!/bin/bash

# 简单查询测试脚本
echo "🔧 简单查询测试"
echo "=================="

# 登录获取token
echo "1. 登录获取token..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}')

echo "登录响应: $LOGIN_RESPONSE"

if echo "$LOGIN_RESPONSE" | grep -q "accessToken"; then
    TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken')
    echo "✅ 登录成功，token: ${TOKEN:0:50}..."
else
    echo "❌ 登录失败"
    exit 1
fi

echo ""
echo "2. 测试数据文件统计API..."
STATS_RESPONSE=$(curl -s -X GET http://localhost:8080/api/data-files/statistics \
    -H "Authorization: Bearer $TOKEN")
echo "统计API响应: $STATS_RESPONSE"

echo ""
echo "3. 测试数据文件查询API..."
QUERY_RESPONSE=$(curl -s -X POST http://localhost:8080/api/data-files/query \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"page": 1, "size": 10, "sortBy": "createdAt", "sortDirection": "desc"}')
echo "查询API响应: $QUERY_RESPONSE"

echo ""
echo "4. 测试组织节点API..."
ORG_RESPONSE=$(curl -s -X GET http://localhost:8080/api/organization/tree \
    -H "Authorization: Bearer $TOKEN")
echo "组织节点API响应: $ORG_RESPONSE"

echo ""
echo "测试完成！"
