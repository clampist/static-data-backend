#!/bin/bash

echo "🚀 Medium Complexity Data File API Test..."

# Login
echo "--- Login ---"
LOGIN_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}')

TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken')
echo "Token: ${TOKEN:0:20}..."

# Create a medium complexity data file
echo -e "\n--- Create Medium Complexity Data File ---"
CREATE_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/data-files" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "用户基础数据表",
    "description": "存储用户基础信息的数据表",
    "organizationNodeId": 7,
    "accessLevel": "PRIVATE",
    "columnDefinitions": [
      {
        "name": "id",
        "dataType": "INTEGER",
        "required": true,
        "sortOrder": 1
      },
      {
        "name": "username",
        "dataType": "STRING",
        "required": true,
        "maxLength": 50,
        "sortOrder": 2
      },
      {
        "name": "email",
        "dataType": "STRING",
        "required": true,
        "maxLength": 100,
        "sortOrder": 3
      },
      {
        "name": "age",
        "dataType": "INTEGER",
        "required": false,
        "sortOrder": 4
      },
      {
        "name": "isActive",
        "dataType": "BOOLEAN",
        "required": true,
        "sortOrder": 5
      }
    ],
    "dataRows": [
      {
        "id": 1,
        "username": "john_doe",
        "email": "john@example.com",
        "age": 25,
        "isActive": true
      },
      {
        "id": 2,
        "username": "jane_smith",
        "email": "jane@example.com",
        "age": 30,
        "isActive": true
      }
    ]
  }')

echo "$CREATE_RESPONSE" | jq .

if echo "$CREATE_RESPONSE" | jq -e '.id' > /dev/null; then
    echo "✅ Successfully created medium complexity data file!"
    DATA_FILE_ID=$(echo "$CREATE_RESPONSE" | jq -r '.id')
    echo "Data file ID: $DATA_FILE_ID"
    
    # Test update
    echo -e "\n--- Update Data File ---"
    UPDATE_RESPONSE=$(curl -s -X PUT "http://localhost:8080/api/data-files/$DATA_FILE_ID" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -d '{
        "name": "用户基础数据表（更新版）",
        "description": "更新后的用户基础信息数据表",
        "accessLevel": "PUBLIC"
      }')
    echo "$UPDATE_RESPONSE" | jq .
    
    # Test query
    echo -e "\n--- Query Data Files ---"
    QUERY_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/data-files/query" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TOKEN" \
      -d '{
        "name": "用户",
        "organizationNodeId": 7,
        "page": 1,
        "size": 10
      }')
    echo "$QUERY_RESPONSE" | jq .
    
    # Test search
    echo -e "\n--- Search Data Files ---"
    SEARCH_RESPONSE=$(curl -s -X GET "http://localhost:8080/api/data-files/search?keyword=用户" \
      -H "Authorization: Bearer $TOKEN")
    echo "$SEARCH_RESPONSE" | jq .
    
    # Test get by organization
    echo -e "\n--- Get Data Files by Organization ---"
    ORG_RESPONSE=$(curl -s -X GET "http://localhost:8080/api/data-files/organization/7" \
      -H "Authorization: Bearer $TOKEN")
    echo "$ORG_RESPONSE" | jq .
    
    # Test get by data type
    echo -e "\n--- Get Data Files by Data Type ---"
    TYPE_RESPONSE=$(curl -s -X GET "http://localhost:8080/api/data-files/data-type/STRING" \
      -H "Authorization: Bearer $TOKEN")
    echo "$TYPE_RESPONSE" | jq .
    
    # Test statistics
    echo -e "\n--- Get Statistics ---"
    STATS_RESPONSE=$(curl -s -X GET "http://localhost:8080/api/data-files/statistics" \
      -H "Authorization: Bearer $TOKEN")
    echo "$STATS_RESPONSE" | jq .
    
    # Clean up
    echo -e "\n--- Delete Data File ---"
    DELETE_RESPONSE=$(curl -s -X DELETE "http://localhost:8080/api/data-files/$DATA_FILE_ID" \
      -H "Authorization: Bearer $TOKEN")
    echo "Delete response: $DELETE_RESPONSE"
    
else
    echo "❌ Failed to create medium complexity data file"
fi
