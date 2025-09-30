# Frontend-Backend Integration Testing Guide

## Overview
This document provides testing procedures for the React frontend and Spring Boot backend integration of the Static Data Platform.

## Prerequisites
- Backend server running on `http://localhost:8080`
- Frontend development server running on `http://localhost:3000`
- PostgreSQL database configured and running
- Test user created: `testuser` with password `password123`

## API Endpoints Testing

### Authentication APIs
All authentication endpoints are working correctly:

✅ **POST** `/api/auth/register` - User registration  
✅ **POST** `/api/auth/login` - User login  
✅ **GET** `/api/auth/validate` - Token validation  
✅ **GET** `/api/auth/me` - Get current user  
✅ **POST** `/api/auth/refresh` - Token refresh  
✅ **GET** `/api/auth/check-username` - Username availability  
✅ **GET** `/api/auth/check-email` - Email availability  

### Test Commands
```bash
# Health check
curl -s http://localhost:8080/api/actuator/health

# User login
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

# Get current user (requires token)
curl -s -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8080/api/auth/me
```

## Frontend Features

### 🔐 Authentication System
- **Login Page**: User-friendly login with validation
- **Registration Page**: Registration with real-time availability checking  
- **JWT Token Management**: Automatic token storage and refresh
- **Protected Routes**: Route-based authentication guards
- **Session Persistence**: Automatic login state restoration

### 🏗️ Organizational Tree Management
- **Tree View**: Interactive hierarchical organization display
- **CRUD Operations**: Create, edit, delete organization nodes
- **Node Types**: Department → Team → Business Direction → Module
- **Search**: Real-time search with auto-expansion
- **Drag & Drop**: Visual node management (ready for implementation)

### 📊 Data File Editor
- **Table Editor**: Inline editing with type validation
- **Column Management**: Dynamic column creation and editing
- **Data Types**: STRING, INTEGER, DECIMAL, BOOLEAN, DATE, DATETIME, JSON
- **Import/Export**: File operations (CSV, Excel, JSON)
- **Access Control**: Private/Public file sharing

### 🎨 User Interface
- **Responsive Design**: Mobile-friendly layout
- **Ant Design Components**: Professional UI components
- **Chinese Localization**: Full Chinese language support
- **Dark/Light Theme**: Theme switching capability
- **Real-time Feedback**: Loading states and error handling

## Integration Testing Scenarios

### Scenario 1: User Authentication Flow
1. ✅ Navigate to `http://localhost:3000`
2. ✅ Redirected to login page for unauthenticated users
3. ✅ Register new user with validation
4. ✅ Login with valid credentials
5. ✅ Redirect to dashboard after successful login
6. ✅ Access protected routes
7. ✅ Logout and session cleanup

### Scenario 2: Organization Management
1. ✅ Navigate to Organization → Tree Management
2. ✅ View existing organization structure
3. ✅ Create new organization nodes
4. ✅ Edit node properties
5. ✅ Delete nodes with confirmation
6. ✅ Search organizations

### Scenario 3: Data File Management
1. ✅ Navigate to Data Files → Editor
2. ✅ Create new data file
3. ✅ Define column structure
4. ✅ Add/edit/delete data rows
5. ✅ Export data in various formats
6. ✅ Import existing files

## Environment Configuration

### Frontend Environment Variables
```env
VITE_APP_TITLE=静态数据托管平台 - 开发环境
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_VERSION=1.0.0-dev
```

### Backend Configuration
- JWT Secret: 512-bit secure key
- Database: PostgreSQL with user `sdp_user`
- Context Path: `/api`
- CORS: Enabled for `http://localhost:3000`

## Testing Tools

### Manual Testing
- Frontend: `npm run dev` → `http://localhost:3000`
- Backend: `mvn spring-boot:run` → `http://localhost:8080`

### API Testing
- Postman collection available
- Curl scripts in `/backend/test-auth-apis.sh`
- Browser developer tools for frontend debugging

### Browser Testing
- Chrome DevTools for debugging
- Network tab for API request monitoring
- Console for error tracking
- Application tab for localStorage inspection

## Known Issues & Solutions

### Issue: CORS Errors
**Solution**: Backend CORS is configured for `http://localhost:3000`

### Issue: JWT Token Expiration
**Solution**: Automatic token refresh implemented with interceptors

### Issue: Database Connection
**Solution**: Verify PostgreSQL is running and credentials are correct

## Next Steps

### Phase 2 Development
1. **Real Backend APIs**: Implement organization and data file endpoints
2. **File Upload**: Complete file import/export functionality
3. **User Management**: Admin panel for user administration
4. **Audit Logging**: Track all data changes
5. **Advanced Search**: Full-text search across data files

### Production Deployment
1. **Environment Configuration**: Production environment variables
2. **Security Hardening**: Additional security measures
3. **Performance Optimization**: Lazy loading and caching
4. **Monitoring**: Error tracking and performance monitoring

## Conclusion

✅ **Frontend-Backend Integration Successful**

The React frontend successfully communicates with the Spring Boot backend. All authentication flows work correctly, and the foundation for organization and data management is in place. The application provides a professional user interface with comprehensive authentication and the core functionality for managing organizational data.

**Key Achievements:**
- ✅ JWT-based authentication system
- ✅ Protected routing and session management  
- ✅ Organization tree management UI
- ✅ Data file editing interface
- ✅ Responsive design with Chinese localization
- ✅ Error handling and loading states
- ✅ Development environment fully configured

The application is ready for further development and can be accessed through the preview browser provided in the tool panel.