#!/usr/bin/env python3
"""
Test Setup Validation Script
验证测试环境设置脚本
"""

import sys
import os
import requests
import json
from config import Config

def check_backend_health():
    """Check if backend is running and healthy"""
    try:
        response = requests.get(Config.HEALTH_CHECK, timeout=5)
        if response.status_code == 200:
            print("✅ Backend health check passed")
            return True
        else:
            print(f"❌ Backend health check failed: HTTP {response.status_code}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"❌ Cannot connect to backend: {e}")
        return False

def register_test_user():
    """Register test user if not exists"""
    try:
        response = requests.post(
            Config.AUTH_REGISTER,
            json={
                "username": Config.DEFAULT_USERNAME,
                "email": f"{Config.DEFAULT_USERNAME}@test.com",
                "password": Config.DEFAULT_PASSWORD,
                "confirmPassword": Config.DEFAULT_PASSWORD,
                "fullName": "Test User"
            },
            timeout=10
        )
        
        if response.status_code in [200, 201]:
            print(f"✅ Test user registered successfully")
            return True
        elif response.status_code == 400 and "already exists" in response.text.lower():
            print(f"ℹ️  Test user already exists")
            return True
        else:
            print(f"⚠️  User registration failed: HTTP {response.status_code}")
            print(f"Response: {response.text}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"⚠️  User registration failed: {e}")
        return False

def test_authentication():
    """Test authentication endpoint"""
    try:
        response = requests.post(
            Config.AUTH_LOGIN,
            json={
                "username": Config.DEFAULT_USERNAME,
                "password": Config.DEFAULT_PASSWORD
            },
            timeout=10
        )
        
        if response.status_code == 200:
            data = response.json()
            if "accessToken" in data:
                print("✅ Authentication test passed")
                return data["accessToken"]
            else:
                print("❌ Authentication test failed: No access token in response")
                return None
        elif response.status_code == 401:
            print("⚠️  Authentication failed, trying to register test user...")
            if register_test_user():
                # Try login again after registration
                response = requests.post(
                    Config.AUTH_LOGIN,
                    json={
                        "username": Config.DEFAULT_USERNAME,
                        "password": Config.DEFAULT_PASSWORD
                    },
                    timeout=10
                )
                if response.status_code == 200:
                    data = response.json()
                    if "accessToken" in data:
                        print("✅ Authentication test passed after user registration")
                        return data["accessToken"]
            
            print(f"❌ Authentication test failed: HTTP {response.status_code}")
            print(f"Response: {response.text}")
            return None
        else:
            print(f"❌ Authentication test failed: HTTP {response.status_code}")
            print(f"Response: {response.text}")
            return None
    except requests.exceptions.RequestException as e:
        print(f"❌ Authentication test failed: {e}")
        return None

def test_organization_api(token):
    """Test organization API endpoint"""
    if not token:
        print("⏭️ Skipping organization API test (no token)")
        return False
    
    try:
        headers = {"Authorization": f"Bearer {token}"}
        response = requests.get(Config.ORG_TREE, headers=headers, timeout=10)
        
        if response.status_code == 200:
            print("✅ Organization API test passed")
            return True
        else:
            print(f"❌ Organization API test failed: HTTP {response.status_code}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"❌ Organization API test failed: {e}")
        return False

def test_datafile_api(token):
    """Test data file API endpoint"""
    if not token:
        print("⏭️ Skipping data file API test (no token)")
        return False
    
    try:
        headers = {"Authorization": f"Bearer {token}"}
        response = requests.get(
            f"{Config.BACKEND_BASE_URL}/data-files/data-types",
            headers=headers,
            timeout=10
        )
        
        if response.status_code == 200:
            print("✅ Data file API test passed")
            return True
        else:
            print(f"❌ Data file API test failed: HTTP {response.status_code}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"❌ Data file API test failed: {e}")
        return False

def check_dependencies():
    """Check if all required dependencies are installed"""
    required_packages = [
        ('locust', 'locust'),
        ('requests', 'requests'),
        ('python-dotenv', 'dotenv'),
        ('faker', 'faker')
    ]
    
    missing_packages = []
    for package_name, import_name in required_packages:
        try:
            __import__(import_name)
        except ImportError:
            missing_packages.append(package_name)
    
    if missing_packages:
        print(f"❌ Missing packages: {', '.join(missing_packages)}")
        print("Run: pip install -r requirements.txt")
        return False
    else:
        print("✅ All required packages are installed")
        return True

def check_config():
    """Check configuration"""
    print(f"🔧 Configuration:")
    print(f"   Backend Host: {Config.BACKEND_HOST}")
    print(f"   Backend Base URL: {Config.BACKEND_BASE_URL}")
    print(f"   Default Username: {Config.DEFAULT_USERNAME}")
    print(f"   Max Users: {Config.DEFAULT_MAX_USERS}")
    print(f"   Test Duration: {Config.DEFAULT_TEST_DURATION}s")
    return True

def main():
    """Main test function"""
    print("🧪 Performance Test Environment Validation")
    print("🧪 性能测试环境验证")
    print("=" * 50)
    
    # Check dependencies
    print("\n1. Checking dependencies...")
    deps_ok = check_dependencies()
    
    # Check configuration
    print("\n2. Checking configuration...")
    config_ok = check_config()
    
    # Check backend health
    print("\n3. Checking backend health...")
    backend_ok = check_backend_health()
    
    # Test authentication
    print("\n4. Testing authentication...")
    token = test_authentication()
    auth_ok = token is not None
    
    # Test organization API
    print("\n5. Testing organization API...")
    org_ok = test_organization_api(token)
    
    # Test data file API
    print("\n6. Testing data file API...")
    datafile_ok = test_datafile_api(token)
    
    # Summary
    print("\n" + "=" * 50)
    print("📊 Test Summary:")
    print(f"   Dependencies: {'✅' if deps_ok else '❌'}")
    print(f"   Configuration: {'✅' if config_ok else '❌'}")
    print(f"   Backend Health: {'✅' if backend_ok else '❌'}")
    print(f"   Authentication: {'✅' if auth_ok else '❌'}")
    print(f"   Organization API: {'✅' if org_ok else '❌'}")
    print(f"   Data File API: {'✅' if datafile_ok else '❌'}")
    
    all_ok = deps_ok and config_ok and backend_ok and auth_ok and org_ok and datafile_ok
    
    if all_ok:
        print("\n🎉 All tests passed! Ready to run performance tests.")
        print("🎉 所有测试通过！可以运行性能测试。")
        print("\nRun one of the following commands:")
        print("运行以下命令之一：")
        print("  ./run_auth_test.sh")
        print("  ./run_organization_test.sh")
        print("  ./run_datafile_test.sh")
        print("  ./run_comprehensive_test.sh")
        print("  ./run_all_tests.sh")
        return 0
    else:
        print("\n❌ Some tests failed. Please fix the issues before running performance tests.")
        print("❌ 部分测试失败。请在运行性能测试前修复问题。")
        return 1

if __name__ == "__main__":
    sys.exit(main())
