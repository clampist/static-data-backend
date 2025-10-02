#!/usr/bin/env python3
"""
Authentication Fix Test Script
认证修复测试脚本

This script tests the authentication fix by trying to register and login.
这个脚本通过尝试注册和登录来测试认证修复。
"""

import requests
import json
import sys
from config import Config

def test_backend_health():
    """Test if backend is running"""
    try:
        response = requests.get('http://localhost:8080/api/actuator/health', timeout=5)
        if response.status_code == 200:
            print("✅ Backend is running")
            return True
        else:
            print(f"❌ Backend health check failed: {response.status_code}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"❌ Cannot connect to backend: {e}")
        return False

def register_test_user():
    """Register test user"""
    try:
        user_data = {
            "username": Config.DEFAULT_USERNAME,
            "email": f"{Config.DEFAULT_USERNAME}@test.com",
            "password": Config.DEFAULT_PASSWORD,
            "confirmPassword": Config.DEFAULT_PASSWORD,
            "fullName": "Test User"
        }
        
        response = requests.post(
            Config.AUTH_REGISTER,
            json=user_data,
            timeout=10
        )
        
        print(f"Registration response: {response.status_code}")
        print(f"Registration body: {response.text}")
        
        if response.status_code in [200, 201]:
            print("✅ User registered successfully")
            return True
        elif response.status_code == 400:
            data = response.json()
            if "already exists" in data.get("message", "").lower():
                print("ℹ️  User already exists")
                return True
            else:
                print(f"❌ Registration failed: {data.get('message', 'Unknown error')}")
                return False
        else:
            print(f"❌ Registration failed: HTTP {response.status_code}")
            return False
            
    except requests.exceptions.RequestException as e:
        print(f"❌ Registration request failed: {e}")
        return False

def test_login():
    """Test user login"""
    try:
        login_data = {
            "username": Config.DEFAULT_USERNAME,
            "password": Config.DEFAULT_PASSWORD
        }
        
        response = requests.post(
            Config.AUTH_LOGIN,
            json=login_data,
            timeout=10
        )
        
        print(f"Login response: {response.status_code}")
        print(f"Login body: {response.text}")
        
        if response.status_code == 200:
            data = response.json()
            if "accessToken" in data:
                print("✅ Login successful")
                print(f"Token (first 20 chars): {data['accessToken'][:20]}...")
                return data["accessToken"]
            else:
                print("❌ No access token in response")
                return None
        else:
            print(f"❌ Login failed: HTTP {response.status_code}")
            return None
            
    except requests.exceptions.RequestException as e:
        print(f"❌ Login request failed: {e}")
        return None

def test_authenticated_request(token):
    """Test authenticated request"""
    if not token:
        print("⏭️ Skipping authenticated request test (no token)")
        return False
        
    try:
        headers = {"Authorization": f"Bearer {token}"}
        response = requests.get(
            Config.AUTH_ME,
            headers=headers,
            timeout=10
        )
        
        print(f"Authenticated request response: {response.status_code}")
        print(f"Authenticated request body: {response.text}")
        
        if response.status_code == 200:
            data = response.json()
            print(f"✅ Authenticated request successful")
            print(f"User: {data.get('username', 'Unknown')}")
            return True
        else:
            print(f"❌ Authenticated request failed: HTTP {response.status_code}")
            return False
            
    except requests.exceptions.RequestException as e:
        print(f"❌ Authenticated request failed: {e}")
        return False

def main():
    """Main test function"""
    print("🧪 Testing Authentication Fix")
    print("🧪 测试认证修复")
    print("=" * 50)
    
    # Test backend health
    if not test_backend_health():
        print("\n❌ Backend is not running. Please start it first:")
        print("cd ../ && mvn spring-boot:run")
        sys.exit(1)
    
    print()
    
    # Test user registration
    print("📝 Testing user registration...")
    register_success = register_test_user()
    print()
    
    # Test login
    print("🔐 Testing user login...")
    token = test_login()
    print()
    
    # Test authenticated request
    print("🔒 Testing authenticated request...")
    auth_success = test_authenticated_request(token)
    print()
    
    # Summary
    print("=" * 50)
    print("📊 Test Summary:")
    print(f"   Backend Health: ✅")
    print(f"   User Registration: {'✅' if register_success else '❌'}")
    print(f"   User Login: {'✅' if token else '❌'}")
    print(f"   Authenticated Request: {'✅' if auth_success else '❌'}")
    
    if register_success and token and auth_success:
        print("\n🎉 All authentication tests passed!")
        print("🎉 所有认证测试通过！")
        print("\nYou can now run performance tests:")
        print("现在可以运行性能测试：")
        print("  python test_setup.py")
        print("  ./run_quick_test.sh")
        return 0
    else:
        print("\n❌ Some authentication tests failed")
        print("❌ 部分认证测试失败")
        return 1

if __name__ == "__main__":
    sys.exit(main())
