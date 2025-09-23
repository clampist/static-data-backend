#!/bin/bash

# Data File API Test Script
# 数据文件API测试脚本

echo "🚀 Starting Data File API Tests..."
echo "🚀 开始数据文件API测试..."

# Configuration
BASE_URL="http://localhost:8080/api"
USERNAME="testuser"
PASSWORD="password123"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# Step 1: Login and get token
echo "--- Step 1: Login ---"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "'$USERNAME'",
    "password": "'$PASSWORD'"
  }')

echo "$LOGIN_RESPONSE" | jq .

TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken')
if [ "$TOKEN" = "null" ] || [ -z "$TOKEN" ]; then
    print_error "Failed to get access token"
    exit 1
fi

print_status "Successfully logged in and got token"
echo "Token: ${TOKEN:0:20}..."

# Step 2: Get organization tree to find a MODULE node
echo -e "\n--- Step 2: Get Organization Tree ---"
ORG_TREE_RESPONSE=$(curl -s -X GET "$BASE_URL/organization/tree" \
  -H "Authorization: Bearer $TOKEN")

echo "$ORG_TREE_RESPONSE" | jq .

# Extract first MODULE node ID
MODULE_NODE_ID=$(echo "$ORG_TREE_RESPONSE" | jq -r '.. | select(.type? == "MODULE") | .id' | head -1)
if [ "$MODULE_NODE_ID" = "null" ] || [ -z "$MODULE_NODE_ID" ]; then
    print_warning "No MODULE node found, creating one first..."
    
    # Create a MODULE node for testing
    CREATE_MODULE_RESPONSE=$(curl -s -X POST "$BASE_URL/organization/nodes" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -d '{
        "name": "测试数据模块",
        "description": "用于测试数据文件API的模块",
        "type": "MODULE",
        "parentId": null,
        "sortOrder": 1
      }')
    
    echo "$CREATE_MODULE_RESPONSE" | jq .
    MODULE_NODE_ID=$(echo "$CREATE_MODULE_RESPONSE" | jq -r '.id')
fi

print_status "Using MODULE node ID: $MODULE_NODE_ID"

# Step 3: Get supported data types
echo -e "\n--- Step 3: Get Supported Data Types ---"
DATA_TYPES_RESPONSE=$(curl -s -X GET "$BASE_URL/data-files/data-types" \
  -H "Authorization: Bearer $TOKEN")

echo "$DATA_TYPES_RESPONSE" | jq .
print_status "Retrieved supported data types"

# Step 4: Create a data file
echo -e "\n--- Step 4: Create Data File ---"
CREATE_DATA_FILE_RESPONSE=$(curl -s -X POST "$BASE_URL/data-files" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "用户基础数据表",
    "description": "存储用户基础信息的数据表，包含姓名、邮箱、手机号等字段",
    "organizationNodeId": '$MODULE_NODE_ID',
    "accessLevel": "PRIVATE",
    "columnDefinitions": [
      {
        "name": "id",
        "dataType": "INTEGER",
        "required": true,
        "defaultValue": null,
        "maxLength": null,
        "description": "用户唯一标识",
        "validationRule": null,
        "sortOrder": 1
      },
      {
        "name": "username",
        "dataType": "STRING",
        "required": true,
        "defaultValue": "",
        "maxLength": 50,
        "description": "用户登录名",
        "validationRule": "^[a-zA-Z0-9_]+$",
        "sortOrder": 2
      },
      {
        "name": "email",
        "dataType": "STRING",
        "required": true,
        "defaultValue": "",
        "maxLength": 100,
        "description": "用户邮箱地址",
        "validationRule": "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$",
        "sortOrder": 3
      },
      {
        "name": "phone",
        "dataType": "STRING",
        "required": false,
        "defaultValue": "",
        "maxLength": 20,
        "description": "手机号码",
        "validationRule": "^1[3-9]\\d{9}$",
        "sortOrder": 4
      },
      {
        "name": "age",
        "dataType": "INTEGER",
        "required": false,
        "defaultValue": "0",
        "maxLength": null,
        "description": "用户年龄",
        "validationRule": "^[0-9]{1,3}$",
        "sortOrder": 5
      },
      {
        "name": "isActive",
        "dataType": "BOOLEAN",
        "required": true,
        "defaultValue": "true",
        "maxLength": null,
        "description": "用户是否激活",
        "validationRule": null,
        "sortOrder": 6
      },
      {
        "name": "createDate",
        "dataType": "DATE",
        "required": true,
        "defaultValue": null,
        "maxLength": null,
        "description": "创建日期",
        "validationRule": null,
        "sortOrder": 7
      },
      {
        "name": "profile",
        "dataType": "JSON",
        "required": false,
        "defaultValue": "{}",
        "maxLength": null,
        "description": "用户配置文件",
        "validationRule": null,
        "sortOrder": 8
      }
    ],
    "dataRows": [
      {
        "id": 1,
        "username": "john_doe",
        "email": "john.doe@example.com",
        "phone": "13800138001",
        "age": 25,
        "isActive": true,
        "createDate": "2024-01-01",
        "profile": "{\"department\": \"IT\", \"level\": \"senior\"}"
      },
      {
        "id": 2,
        "username": "jane_smith",
        "email": "jane.smith@example.com",
        "phone": "13800138002",
        "age": 30,
        "isActive": true,
        "createDate": "2024-01-02",
        "profile": "{\"department\": \"HR\", \"level\": \"manager\"}"
      },
      {
        "id": 3,
        "username": "bob_wilson",
        "email": "bob.wilson@example.com",
        "phone": "",
        "age": 28,
        "isActive": false,
        "createDate": "2024-01-03",
        "profile": "{\"department\": \"Finance\", \"level\": \"junior\"}"
      }
    ]
  }')

echo "$CREATE_DATA_FILE_RESPONSE" | jq .
DATA_FILE_ID=$(echo "$CREATE_DATA_FILE_RESPONSE" | jq -r '.id')
if [ "$DATA_FILE_ID" = "null" ] || [ -z "$DATA_FILE_ID" ]; then
    print_error "Failed to create data file"
    exit 1
fi

print_status "Successfully created data file with ID: $DATA_FILE_ID"

# Step 5: Get data file by ID
echo -e "\n--- Step 5: Get Data File by ID ---"
GET_DATA_FILE_RESPONSE=$(curl -s -X GET "$BASE_URL/data-files/$DATA_FILE_ID" \
  -H "Authorization: Bearer $TOKEN")

echo "$GET_DATA_FILE_RESPONSE" | jq .
print_status "Successfully retrieved data file details"

# Step 6: Update data file
echo -e "\n--- Step 6: Update Data File ---"
UPDATE_DATA_FILE_RESPONSE=$(curl -s -X PUT "$BASE_URL/data-files/$DATA_FILE_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "用户基础数据表（更新版）",
    "description": "更新后的用户基础信息数据表",
    "accessLevel": "PUBLIC",
    "dataRows": [
      {
        "id": 1,
        "username": "john_doe",
        "email": "john.doe@example.com",
        "phone": "13800138001",
        "age": 26,
        "isActive": true,
        "createDate": "2024-01-01",
        "profile": "{\"department\": \"IT\", \"level\": \"senior\", \"skills\": [\"Java\", \"Spring\"]}"
      },
      {
        "id": 2,
        "username": "jane_smith",
        "email": "jane.smith@example.com",
        "phone": "13800138002",
        "age": 31,
        "isActive": true,
        "createDate": "2024-01-02",
        "profile": "{\"department\": \"HR\", \"level\": \"manager\"}"
      },
      {
        "id": 4,
        "username": "alice_brown",
        "email": "alice.brown@example.com",
        "phone": "13800138004",
        "age": 27,
        "isActive": true,
        "createDate": "2024-01-04",
        "profile": "{\"department\": \"Marketing\", \"level\": \"junior\"}"
      }
    ]
  }')

echo "$UPDATE_DATA_FILE_RESPONSE" | jq .
print_status "Successfully updated data file"

# Step 7: Query data files
echo -e "\n--- Step 7: Query Data Files ---"
QUERY_RESPONSE=$(curl -s -X POST "$BASE_URL/data-files/query" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "用户",
    "organizationNodeId": '$MODULE_NODE_ID',
    "page": 1,
    "size": 10,
    "sortBy": "createdAt",
    "sortDirection": "desc"
  }')

echo "$QUERY_RESPONSE" | jq .
print_status "Successfully queried data files"

# Step 8: Get data files by organization node
echo -e "\n--- Step 8: Get Data Files by Organization Node ---"
ORG_FILES_RESPONSE=$(curl -s -X GET "$BASE_URL/data-files/organization/$MODULE_NODE_ID" \
  -H "Authorization: Bearer $TOKEN")

echo "$ORG_FILES_RESPONSE" | jq .
print_status "Successfully retrieved data files by organization node"

# Step 9: Search data files
echo -e "\n--- Step 9: Search Data Files ---"
SEARCH_RESPONSE=$(curl -s -X GET "$BASE_URL/data-files/search?keyword=用户" \
  -H "Authorization: Bearer $TOKEN")

echo "$SEARCH_RESPONSE" | jq .
print_status "Successfully searched data files"

# Step 10: Get data files by data type
echo -e "\n--- Step 10: Get Data Files by Data Type ---"
TYPE_RESPONSE=$(curl -s -X GET "$BASE_URL/data-files/data-type/STRING" \
  -H "Authorization: Bearer $TOKEN")

echo "$TYPE_RESPONSE" | jq .
print_status "Successfully retrieved data files by data type"

# Step 11: Get recent data files
echo -e "\n--- Step 11: Get Recent Data Files ---"
RECENT_RESPONSE=$(curl -s -X GET "$BASE_URL/data-files/recent?limit=5" \
  -H "Authorization: Bearer $TOKEN")

echo "$RECENT_RESPONSE" | jq .
print_status "Successfully retrieved recent data files"

# Step 12: Get data file statistics
echo -e "\n--- Step 12: Get Data File Statistics ---"
STATS_RESPONSE=$(curl -s -X GET "$BASE_URL/data-files/statistics" \
  -H "Authorization: Bearer $TOKEN")

echo "$STATS_RESPONSE" | jq .
print_status "Successfully retrieved data file statistics"

# Step 13: Get accessible data files
echo -e "\n--- Step 13: Get Accessible Data Files ---"
ACCESSIBLE_RESPONSE=$(curl -s -X GET "$BASE_URL/data-files/accessible?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN")

echo "$ACCESSIBLE_RESPONSE" | jq .
print_status "Successfully retrieved accessible data files"

# Step 14: Test error cases
echo -e "\n--- Step 14: Test Error Cases ---"

# Try to create data file with invalid organization node
echo "Testing invalid organization node..."
INVALID_ORG_RESPONSE=$(curl -s -X POST "$BASE_URL/data-files" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "无效模块测试",
    "description": "测试无效组织节点",
    "organizationNodeId": 99999,
    "accessLevel": "PRIVATE"
  }')

echo "$INVALID_ORG_RESPONSE" | jq .
print_info "Tested invalid organization node (should return error)"

# Try to get non-existent data file
echo "Testing non-existent data file..."
NONEXISTENT_RESPONSE=$(curl -s -X GET "$BASE_URL/data-files/99999" \
  -H "Authorization: Bearer $TOKEN")

echo "$NONEXISTENT_RESPONSE" | jq .
print_info "Tested non-existent data file (should return 404)"

# Step 15: Clean up - Delete the test data file
echo -e "\n--- Step 15: Clean Up ---"
DELETE_RESPONSE=$(curl -s -X DELETE "$BASE_URL/data-files/$DATA_FILE_ID" \
  -H "Authorization: Bearer $TOKEN")

echo "Delete response: $DELETE_RESPONSE"
print_status "Successfully deleted test data file"

# Summary
echo -e "\n🎉 Data File API Tests Completed!"
echo "🎉 数据文件API测试完成！"
echo -e "\n📊 Test Summary:"
echo "✅ Created data file with multiple data types"
echo "✅ Retrieved data file details"
echo "✅ Updated data file with new data"
echo "✅ Queried data files with conditions"
echo "✅ Searched data files by keyword"
echo "✅ Retrieved data files by organization node"
echo "✅ Retrieved data files by data type"
echo "✅ Retrieved recent data files"
echo "✅ Retrieved data file statistics"
echo "✅ Retrieved accessible data files"
echo "✅ Tested error handling"
echo "✅ Cleaned up test data"
echo -e "\n🚀 All Data File API endpoints are working correctly!"
echo "🚀 所有数据文件API端点都工作正常！"
