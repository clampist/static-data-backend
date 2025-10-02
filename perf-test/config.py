"""
Configuration module for Locust performance tests
"""
import os
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

class Config:
    """Configuration class for performance tests"""
    
    # Backend Configuration
    BACKEND_HOST = os.getenv('BACKEND_HOST', 'http://localhost:8080')
    BACKEND_BASE_URL = os.getenv('BACKEND_BASE_URL', 'http://localhost:8080/api')
    
    # Test Credentials
    DEFAULT_USERNAME = os.getenv('DEFAULT_USERNAME', 'testuser')
    DEFAULT_PASSWORD = os.getenv('DEFAULT_PASSWORD', 'password123')
    ADMIN_USERNAME = os.getenv('ADMIN_USERNAME', 'admin')
    ADMIN_PASSWORD = os.getenv('ADMIN_PASSWORD', 'admin123')
    
    # Performance Test Configuration
    DEFAULT_RAMP_UP_TIME = int(os.getenv('DEFAULT_RAMP_UP_TIME', 60))
    DEFAULT_RAMP_DOWN_TIME = int(os.getenv('DEFAULT_RAMP_DOWN_TIME', 10))
    DEFAULT_SPAWN_RATE = int(os.getenv('DEFAULT_SPAWN_RATE', 2))
    DEFAULT_MAX_USERS = int(os.getenv('DEFAULT_MAX_USERS', 100))
    DEFAULT_TEST_DURATION = int(os.getenv('DEFAULT_TEST_DURATION', 300))
    
    # Test Data Configuration
    TEST_DATA_SIZE = int(os.getenv('TEST_DATA_SIZE', 1000))
    MAX_ORGANIZATION_NODES = int(os.getenv('MAX_ORGANIZATION_NODES', 50))
    MAX_DATA_FILES = int(os.getenv('MAX_DATA_FILES', 100))
    
    # API Endpoints
    AUTH_LOGIN = f"{BACKEND_BASE_URL}/auth/login"
    AUTH_REGISTER = f"{BACKEND_BASE_URL}/auth/register"
    AUTH_REFRESH = f"{BACKEND_BASE_URL}/auth/refresh"
    AUTH_VALIDATE = f"{BACKEND_BASE_URL}/auth/validate"
    AUTH_ME = f"{BACKEND_BASE_URL}/auth/me"
    AUTH_CHECK_USERNAME = f"{BACKEND_BASE_URL}/auth/check-username"
    AUTH_CHECK_EMAIL = f"{BACKEND_BASE_URL}/auth/check-email"
    
    ORG_TREE = f"{BACKEND_BASE_URL}/organization/tree"
    ORG_NODES = f"{BACKEND_BASE_URL}/organization/nodes"
    ORG_NODE_BY_ID = f"{BACKEND_BASE_URL}/organization/nodes/{{id}}"
    
    DATA_FILES = f"{BACKEND_BASE_URL}/data-files"
    DATA_FILE_BY_ID = f"{BACKEND_BASE_URL}/data-files/{{id}}"
    DATA_FILES_QUERY = f"{BACKEND_BASE_URL}/data-files/query"
    DATA_FILES_STATISTICS = f"{BACKEND_BASE_URL}/data-files/statistics"
    
    # Health Check
    HEALTH_CHECK = f"{BACKEND_BASE_URL}/actuator/health"
