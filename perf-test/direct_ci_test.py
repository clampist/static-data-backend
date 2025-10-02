#!/usr/bin/env python3
"""
Direct CI Performance Test
直接CI性能测试

This script runs Locust directly using Python API to avoid command line issues.
这个脚本直接使用Python API运行Locust，避免命令行问题。
"""

import os
import sys
import time
import logging

# Setup logging to console only
logging.basicConfig(level=logging.WARNING, format='%(levelname)s: %(message)s')

def run_direct_test():
    """Run Locust test directly using Python API"""
    
    print("🚀 Direct CI Performance Test")
    print("🚀 直接CI性能测试")
    print("=" * 40)
    
    # Create directories
    os.makedirs('reports', exist_ok=True)
    
    try:
        # Import Locust modules
        from locust import HttpUser, task, between
        from locust.env import Environment
        from locust.stats import stats_printer, stats_history
        from locust.log import setup_logging
        from locust.main import create_environment
        
        print("✅ Locust modules imported successfully")
        
        # Define test user class
        class AuthTestUser(HttpUser):
            wait_time = between(1, 2)
            
            def on_start(self):
                """Login before starting tests"""
                response = self.client.post("/api/auth/login", json={
                    "username": "testuser",
                    "password": "password123"
                })
                if response.status_code == 200:
                    data = response.json()
                    if "accessToken" in data:
                        self.token = data["accessToken"]
                        self.headers = {"Authorization": f"Bearer {self.token}"}
                    else:
                        print("⚠️  No access token in login response")
                        self.headers = {}
                else:
                    print(f"⚠️  Login failed: {response.status_code}")
                    self.headers = {}
            
            @task(3)
            def test_login(self):
                """Test login endpoint"""
                with self.client.post("/api/auth/login", json={
                    "username": "testuser",
                    "password": "password123"
                }, catch_response=True) as response:
                    if response.status_code == 200:
                        try:
                            data = response.json()
                            if "accessToken" in data and "user" in data:
                                response.success()
                            else:
                                response.failure("Invalid response format")
                        except:
                            response.failure("Invalid JSON response")
                    else:
                        response.failure(f"HTTP {response.status_code}")
            
            @task(1)
            def test_get_user(self):
                """Test get current user"""
                if hasattr(self, 'headers') and self.headers:
                    self.client.get("/api/auth/me", headers=self.headers)
        
        print("✅ Test user class defined")
        
        # Create environment
        env = Environment(user_classes=[AuthTestUser])
        env.create_local_runner()
        
        print("✅ Environment created")
        
        # Start test
        print("🏃 Starting performance test...")
        env.runner.start(10, spawn_rate=2)
        
        # Run for 60 seconds
        time.sleep(60)
        
        # Stop test
        env.runner.quit()
        
        print("✅ Test completed")
        
        # Generate reports
        from locust.web import WebUI
        from locust.stats import print_stats
        
        # Print basic stats
        print("\n📊 Test Results:")
        print_stats(env.runner.stats)
        
        # Save CSV stats
        env.runner.stats.to_csv('reports', 'direct_test')
        
        print("✅ Reports generated")
        
        return True
        
    except ImportError as e:
        print(f"❌ Import error: {e}")
        print("Make sure Locust is installed: pip install locust")
        return False
    except Exception as e:
        print(f"❌ Error running test: {e}")
        return False

def main():
    """Main function"""
    try:
        success = run_direct_test()
        
        if success:
            print("\n🎉 Direct CI test completed successfully!")
            print("🎉 直接CI测试成功完成！")
            return 0
        else:
            print("\n❌ Direct CI test failed")
            print("❌ 直接CI测试失败")
            return 1
    except Exception as e:
        print(f"❌ Unexpected error: {e}")
        return 1

if __name__ == "__main__":
    sys.exit(main())
