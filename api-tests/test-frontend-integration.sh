#!/bin/bash

echo "=== 前端集成测试脚本 ==="
echo "测试后端API是否正常工作..."

# 等待后端服务启动
echo "等待后端服务启动..."
sleep 20

# 测试登录
echo "1. 测试登录..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')

if [ $? -eq 0 ] && echo "$LOGIN_RESPONSE" | grep -q "accessToken"; then
  echo "✅ 登录成功"
  TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
  echo "Token: ${TOKEN:0:50}..."
else
  echo "❌ 登录失败"
  echo "Response: $LOGIN_RESPONSE"
  exit 1
fi

# 测试组织节点API
echo "2. 测试组织节点API..."
ORG_RESPONSE=$(curl -s -X GET "http://localhost:8080/api/organization/tree" \
  -H "Authorization: Bearer $TOKEN")

if [ $? -eq 0 ] && echo "$ORG_RESPONSE" | grep -q "总公司"; then
  echo "✅ 组织节点API正常"
  MODULE_COUNT=$(echo "$ORG_RESPONSE" | grep -o '"type":"MODULE"' | wc -l)
  echo "找到 $MODULE_COUNT 个MODULE类型节点"
else
  echo "❌ 组织节点API失败"
  echo "Response: $ORG_RESPONSE"
fi

# 测试数据文件统计API
echo "3. 测试数据文件统计API..."
STATS_RESPONSE=$(curl -s -X GET "http://localhost:8080/api/data-files/statistics" \
  -H "Authorization: Bearer $TOKEN")

if [ $? -eq 0 ] && echo "$STATS_RESPONSE" | grep -q "totalFiles"; then
  echo "✅ 数据文件统计API正常"
  echo "统计信息: $STATS_RESPONSE"
else
  echo "❌ 数据文件统计API失败"
  echo "Response: $STATS_RESPONSE"
fi

# 测试支持的数据类型API
echo "4. 测试支持的数据类型API..."
TYPES_RESPONSE=$(curl -s -X GET "http://localhost:8080/api/data-files/data-types" \
  -H "Authorization: Bearer $TOKEN")

if [ $? -eq 0 ] && echo "$TYPES_RESPONSE" | grep -q "STRING"; then
  echo "✅ 数据类型API正常"
  echo "支持的数据类型: $TYPES_RESPONSE"
else
  echo "❌ 数据类型API失败"
  echo "Response: $TYPES_RESPONSE"
fi

echo "=== 测试完成 ==="
echo "如果所有测试都通过，前端应该可以正常工作了！"
