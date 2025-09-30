#!/bin/bash

# 最终修复验证脚本
echo "🎯 PostgreSQL错误最终修复验证"
echo "=================================="


# 测试登录
echo "1. 测试登录API..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}')

if echo "$LOGIN_RESPONSE" | grep -q "accessToken"; then
    TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken')
    echo "✅ 登录成功"
else
    echo "❌ 登录失败: $LOGIN_RESPONSE"
    exit 1
fi

echo ""
echo "2. 测试数据文件查询API..."
QUERY_RESPONSE=$(curl -s -X POST http://localhost:8080/api/data-files/query \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"page": 1, "size": 10, "sortBy": "createdAt", "sortDirection": "desc"}')

if echo "$QUERY_RESPONSE" | grep -q "content"; then
    echo "✅ 数据文件查询API正常"
    echo "响应: $QUERY_RESPONSE"
elif echo "$QUERY_RESPONSE" | grep -q "500"; then
    echo "❌ 数据文件查询API仍有500错误"
    echo "错误: $QUERY_RESPONSE"
else
    echo "⚠️ 数据文件查询API响应异常"
    echo "响应: $QUERY_RESPONSE"
fi

echo ""
echo "3. 测试数据文件统计API..."
STATS_RESPONSE=$(curl -s -X GET http://localhost:8080/api/data-files/statistics \
    -H "Authorization: Bearer $TOKEN")

if echo "$STATS_RESPONSE" | grep -q "totalFiles"; then
    echo "✅ 数据文件统计API正常"
    echo "统计: $STATS_RESPONSE"
else
    echo "❌ 数据文件统计API异常"
    echo "响应: $STATS_RESPONSE"
fi

echo ""
echo "4. 测试组织节点API..."
ORG_RESPONSE=$(curl -s -X GET http://localhost:8080/api/organization/tree \
    -H "Authorization: Bearer $TOKEN")

if echo "$ORG_RESPONSE" | grep -q "id"; then
    echo "✅ 组织节点API正常"
else
    echo "❌ 组织节点API异常"
    echo "响应: $ORG_RESPONSE"
fi

echo ""
echo "🎯 测试完成！"
echo "如果数据文件查询API正常，说明PostgreSQL bytea问题已完全修复"
