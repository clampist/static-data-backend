#!/usr/bin/env python3
"""
Quick Authentication Test (No Virtual Environment Required)
快速认证测试（不需要虚拟环境）
"""

import requests
import json
import sys

# Configuration
BACKEND_URL = "http://localhost:8080/api"
USERNAME = "testuser"
PASSWORD = "password123"

def test_backend():
    """Test backend health"""
    try:
        response = requests.get(f"{BACKEND_URL.replace('/api', '')}/api/actuator/health", timeout=5)
        if response.status_code == 200:
            print("✅ Backend is running")
            return True
        else:
            print(f"❌ Backend health check failed: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ Cannot connect to backend: {e}")
        return False

def register_user():
    """Register test user"""
    try:
        data = {
            "username": USERNAME,
            "email": f"{USERNAME}@test.com",
            "password": PASSWORD,
            "confirmPassword": PASSWORD,
            "fullName": "Test User"
        }
        
        response = requests.post(f"{BACKEND_URL}/auth/register", json=data, timeout=10)
        
        if response.status_code in [200, 201]:
            print("✅ User registered successfully")
            return True
        elif response.status_code == 400:
            try:
                error_data = response.json()
                if "already exists" in error_data.get("message", "").lower():
                    print("ℹ️  User already exists")
                    return True
            except:
                pass
            print(f"❌ Registration failed: {response.text}")
            return False
        else:
            print(f"❌ Registration failed: HTTP {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ Registration failed: {e}")
        return False

def test_login():
    """Test user login"""
    try:
        data = {"username": USERNAME, "password": PASSWORD}
        response = requests.post(f"{BACKEND_URL}/auth/login", json=data, timeout=10)
        
        if response.status_code == 200:
            result = response.json()
            if "accessToken" in result:
                print("✅ Login successful")
                return result["accessToken"]
            else:
                print("❌ No access token in response")
                return None
        else:
            print(f"❌ Login failed: HTTP {response.status_code}")
            print(f"Response: {response.text}")
            return None
    except Exception as e:
        print(f"❌ Login failed: {e}")
        return None

def main():
    print("🚀 Quick Authentication Test")
    print("🚀 快速认证测试")
    print("=" * 40)
    
    # Test backend
    if not test_backend():
        print("\n❌ Backend is not running. Start it with:")
        print("cd ../ && mvn spring-boot:run")
        return 1
    
    # Register user
    print("\n📝 Registering test user...")
    if not register_user():
        print("⚠️  User registration failed, but continuing...")
    
    # Test login
    print("\n🔐 Testing login...")
    token = test_login()
    
    if token:
        print("\n🎉 Authentication test passed!")
        print("🎉 认证测试通过！")
        print("\nYou can now run performance tests:")
        print("现在可以运行性能测试：")
        print("  python test_setup.py")
        return 0
    else:
        print("\n❌ Authentication test failed")
        print("❌ 认证测试失败")
        return 1

if __name__ == "__main__":
    sys.exit(main())
