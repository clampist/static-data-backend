# Authentication API Testing Guide

This guide provides comprehensive instructions for testing the authentication APIs using both curl commands and Postman.

## Prerequisites

1. **Start the Spring Boot Application**
   ```bash
   cd /Users/clampist/work/JavaPro/backend
   mvn spring-boot:run
   ```

2. **Verify Application is Running**
   ```bash
   curl http://localhost:8080/api/actuator/health
   ```
   Expected response: `{"status":"UP"}`

## Method 1: Quick Testing with Curl

### Run the Automated Test Script
```bash
cd /Users/clampist/work/JavaPro/backend
./test-auth-apis.sh
```

### Individual Curl Commands

#### 1. User Registration
```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "testuser@example.com",
    "password": "password123",
    "confirmPassword": "password123",
    "fullName": "Test User"
  }'
```

#### 2. User Login
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

#### 3. Token Validation (replace TOKEN with actual token)
```bash
TOKEN="your_jwt_token_here"
curl -X GET "http://localhost:8080/api/auth/validate" \
  -H "Authorization: Bearer $TOKEN"
```

#### 4. Get Current User
```bash
curl -X GET "http://localhost:8080/api/auth/me" \
  -H "Authorization: Bearer $TOKEN"
```

#### 5. Refresh Token
```bash
curl -X POST "http://localhost:8080/api/auth/refresh" \
  -H "Authorization: Bearer $TOKEN"
```

#### 6. Check Username Availability
```bash
curl -X GET "http://localhost:8080/api/auth/check-username?username=newuser"
```

#### 7. Check Email Availability
```bash
curl -X GET "http://localhost:8080/api/auth/check-email?email=newuser@example.com"
```

## Method 2: Comprehensive Testing with Postman

### Import the Test Collection

1. **Open Postman**

2. **Import Collection**
   - Click "Import" button
   - Select "Upload Files"
   - Choose `postman-collection.json`
   - Click "Import"

3. **Import Environment**
   - Click on "Environments" tab
   - Click "Import"
   - Select `postman-environment.json`
   - Click "Import"

4. **Set Environment**
   - Select "Static Data Platform - Local Environment" from the environment dropdown

### Run Tests

#### Manual Testing
1. **Run tests in order:**
   - Authentication → User Registration - Success
   - Authentication → User Login - Success
   - Authentication → Validate Token - Success
   - Authentication → Get Current User - Success
   - Authentication → Refresh Token - Success

2. **Test Validation Features:**
   - Validation Tests → Check Username Availability (both cases)
   - Validation Tests → Check Email Availability (both cases)

3. **Test Error Cases:**
   - Error Cases → Registration - Invalid Input
   - Error Cases → Registration - Duplicate Username
   - Error Cases → Login - Invalid Credentials
   - Error Cases → Validate Token - Invalid Token
   - Error Cases → Access Protected Endpoint - No Token

#### Automated Testing
1. **Run Entire Collection:**
   - Right-click on "Static Data Platform - Authentication APIs"
   - Select "Run collection"
   - Choose the environment
   - Click "Run Static Data Platform - Authentication APIs"

2. **Collection Runner Features:**
   - All tests will run automatically
   - Results will show pass/fail status
   - Environment variables will be set automatically
   - Token management is handled automatically

### Test Scenarios Covered

#### Success Flows
- ✅ User registration with valid data
- ✅ User login with correct credentials
- ✅ Token validation with valid token
- ✅ Getting current user information
- ✅ Token refresh functionality
- ✅ Username/email availability checking

#### Error Handling
- ❌ Registration with invalid input data
- ❌ Registration with duplicate username/email
- ❌ Login with invalid credentials
- ❌ Token validation with invalid token
- ❌ Accessing protected endpoints without token
- ❌ Username/email availability for existing values

### Expected Response Formats

#### Successful Login Response
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "testuser@example.com",
    "fullName": "Test User",
    "role": "USER",
    "enabled": true,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
}
```

#### Error Response Format
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "用户名或密码错误",
  "path": "uri=/api/auth/login"
}
```

## Troubleshooting

### Common Issues

1. **Connection Refused**
   - Ensure Spring Boot application is running
   - Check if port 8080 is available
   - Verify application logs for startup errors

2. **401 Unauthorized**
   - Check if token is correctly included in Authorization header
   - Verify token format: "Bearer <token>"
   - Ensure token hasn't expired

3. **400 Bad Request**
   - Validate request body format
   - Check required fields are present
   - Verify data types match expectations

4. **Database Connection Issues**
   - Ensure PostgreSQL is running
   - Verify database credentials in application.properties
   - Check if databases exist

### Debug Steps

1. **Check Application Logs**
   ```bash
   tail -f /Users/clampist/work/JavaPro/backend/logs/application.log
   ```

2. **Verify Database Connection**
   ```bash
   psql -h localhost -U sdp_user -d static_data_platform_dev
   ```

3. **Test Health Endpoint**
   ```bash
   curl http://localhost:8080/api/actuator/health
   ```

4. **Check Swagger Documentation**
   Open: http://localhost:8080/swagger-ui.html

## Next Steps

After successful authentication testing:

1. **Database Verification**: Check if user data is properly stored
2. **Frontend Integration**: Test with React frontend
3. **Role-based Access**: Test ADMIN vs USER permissions
4. **Security Testing**: Test with invalid tokens, expired tokens
5. **Performance Testing**: Test with multiple concurrent users

## Security Notes

- 🔒 All passwords are encrypted using BCrypt
- 🔑 JWT tokens expire after 24 hours by default
- 🛡️ CORS is configured for localhost development
- 🚫 Public endpoints don't require authentication
- 📝 All API interactions are logged for monitoring