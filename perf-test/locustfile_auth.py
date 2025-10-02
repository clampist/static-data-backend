"""
Locust performance test for Authentication APIs
Based on backend/api-tests/test-auth-apis.sh
"""
import json
import random
from locust import HttpUser, task, between
from config import Config
from utils import TestDataGenerator, ResponseValidator, token_manager

class AuthUser(HttpUser):
    """User behavior for authentication API performance testing"""
    
    wait_time = between(1, 3)
    
    def on_start(self):
        """Called when a user starts"""
        self.user_id = f"user_{random.randint(1000, 9999)}"
        self.test_data = TestDataGenerator()
        self.validator = ResponseValidator()
    
    @task(3)
    def test_login(self):
        """Test user login - most common operation"""
        with self.client.post(
            Config.AUTH_LOGIN,
            json={
                "username": Config.DEFAULT_USERNAME,
                "password": Config.DEFAULT_PASSWORD
            },
            catch_response=True,
            name="auth_login"
        ) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    if "accessToken" in data and "user" in data:
                        # Store token for other requests
                        token = data.get("accessToken")
                        if token:
                            token_manager.set_token(self.user_id, token)
                        response.success()
                    else:
                        response.failure("Invalid response format")
                except json.JSONDecodeError:
                    response.failure("Invalid JSON response")
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(1)
    def test_register(self):
        """Test user registration"""
        user_data = self.test_data.generate_user_data()
        with self.client.post(
            Config.AUTH_REGISTER,
            json=user_data,
            catch_response=True,
            name="auth_register"
        ) as response:
            if response.status_code in [200, 201, 400, 409]:  # 400 and 409 for existing user
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(2)
    def test_get_current_user(self):
        """Test get current user info"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return  # Skip if no token available
        
        with self.client.get(
            Config.AUTH_ME,
            headers=headers,
            catch_response=True,
            name="auth_me"
        ) as response:
            if response.status_code == 200:
                if self.validator.validate_user_response(response):
                    response.success()
                else:
                    response.failure("Invalid user response format")
            elif response.status_code == 401:
                response.success()  # Expected for some users without valid tokens
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(1)
    def test_validate_token(self):
        """Test token validation"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return  # Skip if no token available
        
        with self.client.get(
            Config.AUTH_VALIDATE,
            headers=headers,
            catch_response=True,
            name="auth_validate"
        ) as response:
            if response.status_code in [200, 401]:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(1)
    def test_refresh_token(self):
        """Test token refresh"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return  # Skip if no token available
        
        with self.client.post(
            Config.AUTH_REFRESH,
            headers=headers,
            catch_response=True,
            name="auth_refresh"
        ) as response:
            if response.status_code in [200, 401]:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(2)
    def test_check_username_availability(self):
        """Test username availability check"""
        username = f"testuser_{random.randint(1000, 9999)}"
        with self.client.get(
            f"{Config.AUTH_CHECK_USERNAME}?username={username}",
            catch_response=True,
            name="auth_check_username"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(2)
    def test_check_email_availability(self):
        """Test email availability check"""
        email = f"test_{random.randint(1000, 9999)}@example.com"
        with self.client.get(
            f"{Config.AUTH_CHECK_EMAIL}?email={email}",
            catch_response=True,
            name="auth_check_email"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(1)
    def test_invalid_login(self):
        """Test invalid login attempt"""
        with self.client.post(
            Config.AUTH_LOGIN,
            json={
                "username": Config.DEFAULT_USERNAME,
                "password": "wrongpassword"
            },
            catch_response=True,
            name="auth_invalid_login"
        ) as response:
            if response.status_code == 401:
                response.success()  # Expected failure
            else:
                response.failure(f"Expected 401, got {response.status_code}")
