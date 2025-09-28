#!/bin/bash

# Authentication API Testing Script
# Make sure the Spring Boot application is running on localhost:8080

BASE_URL="http://localhost:8080/api"
echo "Testing Authentication APIs for Static Data Platform"
echo "=================================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}1. Testing User Registration${NC}"
echo "POST $BASE_URL/auth/register"
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "testuser@example.com",
    "password": "password123",
    "confirmPassword": "password123",
    "fullName": "Test User"
  }')

echo "Response: $REGISTER_RESPONSE"
echo ""

echo -e "${YELLOW}2. Testing User Login${NC}"
echo "POST $BASE_URL/auth/login"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }')

echo "Response: $LOGIN_RESPONSE"

# Extract token from login response (requires jq)
if command -v jq &> /dev/null; then
    TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken // empty')
    if [ ! -z "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
        echo -e "${GREEN}Token extracted: ${TOKEN:0:50}...${NC}"
        
        echo ""
        echo -e "${YELLOW}3. Testing Token Validation${NC}"
        echo "GET $BASE_URL/auth/validate"
        VALIDATE_RESPONSE=$(curl -s -X GET "$BASE_URL/auth/validate" \
          -H "Authorization: Bearer $TOKEN")
        echo "Response: $VALIDATE_RESPONSE"
        
        echo ""
        echo -e "${YELLOW}4. Testing Get Current User${NC}"
        echo "GET $BASE_URL/auth/me"
        ME_RESPONSE=$(curl -s -X GET "$BASE_URL/auth/me" \
          -H "Authorization: Bearer $TOKEN")
        echo "Response: $ME_RESPONSE"
        
        echo ""
        echo -e "${YELLOW}5. Testing Token Refresh${NC}"
        echo "POST $BASE_URL/auth/refresh"
        REFRESH_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/refresh" \
          -H "Authorization: Bearer $TOKEN")
        echo "Response: $REFRESH_RESPONSE"
        
    else
        echo -e "${RED}Failed to extract token from login response${NC}"
    fi
else
    echo -e "${YELLOW}jq not installed, skipping token-based tests${NC}"
    echo "Install jq with: brew install jq"
fi

echo ""
echo -e "${YELLOW}6. Testing Username Availability Check${NC}"
echo "GET $BASE_URL/auth/check-username?username=newuser"
USERNAME_CHECK=$(curl -s -X GET "$BASE_URL/auth/check-username?username=newuser")
echo "Response: $USERNAME_CHECK"

echo ""
echo -e "${YELLOW}7. Testing Email Availability Check${NC}"
echo "GET $BASE_URL/auth/check-email?email=newuser@example.com"
EMAIL_CHECK=$(curl -s -X GET "$BASE_URL/auth/check-email?email=newuser@example.com")
echo "Response: $EMAIL_CHECK"

echo ""
echo -e "${YELLOW}8. Testing Username Check (Existing)${NC}"
echo "GET $BASE_URL/auth/check-username?username=testuser"
USERNAME_EXISTS=$(curl -s -X GET "$BASE_URL/auth/check-username?username=testuser")
echo "Response: $USERNAME_EXISTS"

echo ""
echo -e "${YELLOW}9. Testing Invalid Login${NC}"
echo "POST $BASE_URL/auth/login"
INVALID_LOGIN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "wrongpassword"
  }')
echo "Response: $INVALID_LOGIN"

echo ""
echo -e "${YELLOW}10. Testing Registration with Existing Username${NC}"
echo "POST $BASE_URL/auth/register"
DUPLICATE_REGISTER=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "different@example.com",
    "password": "password123",
    "confirmPassword": "password123",
    "fullName": "Another User"
  }')
echo "Response: $DUPLICATE_REGISTER"

echo ""
echo -e "${GREEN}Authentication API Testing Complete!${NC}"
echo "=================================================="
echo ""
echo "Usage Instructions:"
echo "1. Start your Spring Boot application: cd backend && mvn spring-boot:run"
echo "2. Run this script: chmod +x test-auth-apis.sh && ./test-auth-apis.sh"
echo "3. Install jq for better JSON parsing: brew install jq"