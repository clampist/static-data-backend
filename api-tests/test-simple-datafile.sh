#!/bin/bash

echo "🚀 Simple Data File API Test..."

# Login
echo "--- Login ---"
LOGIN_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "password123"}')

echo "$LOGIN_RESPONSE" | jq .

TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken')
echo "Token: ${TOKEN:0:20}..."

# Get available organization nodes
echo -e "\n--- Get Organization Nodes ---"
ORG_RESPONSE=$(curl -s -X GET "http://localhost:8080/api/organization/nodes" \
  -H "Authorization: Bearer $TOKEN")

echo "$ORG_RESPONSE" | jq .

# Extract the first available organization node ID
ORG_NODE_ID=$(echo "$ORG_RESPONSE" | jq -r '.[0].id')
echo "Using organization node ID: $ORG_NODE_ID"

# Create a simple data file
echo -e "\n--- Create Simple Data File ---"
CREATE_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/data-files" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "简单测试数据表",
    "description": "测试用数据表",
    "organizationNodeId": '$ORG_NODE_ID',
    "accessLevel": "PRIVATE",
    "columnDefinitions": [
      {
        "name": "id",
        "dataType": "INTEGER",
        "required": true,
        "sortOrder": 1
      }
    ],
    "dataRows": [
      {"id": 1}
    ]
  }')

echo "$CREATE_RESPONSE" | jq .

if echo "$CREATE_RESPONSE" | jq -e '.id' > /dev/null; then
    echo "✅ Successfully created data file!"
    DATA_FILE_ID=$(echo "$CREATE_RESPONSE" | jq -r '.id')
    echo "Data file ID: $DATA_FILE_ID"
    
    # Test get by ID
    echo -e "\n--- Get Data File by ID ---"
    GET_RESPONSE=$(curl -s -X GET "http://localhost:8080/api/data-files/$DATA_FILE_ID" \
      -H "Authorization: Bearer $TOKEN")
    echo "$GET_RESPONSE" | jq .
    
    # Clean up
    echo -e "\n--- Delete Data File ---"
    DELETE_RESPONSE=$(curl -s -X DELETE "http://localhost:8080/api/data-files/$DATA_FILE_ID" \
      -H "Authorization: Bearer $TOKEN")
    echo "Delete response: $DELETE_RESPONSE"
    
else
    echo "❌ Failed to create data file"
fi
