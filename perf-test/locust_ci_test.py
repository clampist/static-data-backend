#!/usr/bin/env python3
"""
Locust CI Test with Logging Fix
带日志修复的Locust CI测试

This script runs Locust tests while fixing the logging issues.
这个脚本运行Locust测试，同时修复日志问题。
"""

import os
import sys
import subprocess
import tempfile
import logging

def setup_logging_fix():
    """Setup logging to prevent file creation issues"""
    
    # Create logs directory
    os.makedirs('logs', exist_ok=True)
    
    # Create dummy log files to prevent Locust from trying to create them
    dummy_files = ['locust.log', 'locust_ci.log']
    for log_file in dummy_files:
        with open(f'logs/{log_file}', 'w') as f:
            f.write('# Locust log file\n')
    
    # Set up console logging with minimal output
    logging.basicConfig(
        level=logging.ERROR,  # Only show errors
        format='%(levelname)s: %(message)s',
        handlers=[logging.StreamHandler()]
    )
    
    # Disable urllib3 warnings
    import urllib3
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

def check_backend():
    """Check if backend is running"""
    try:
        import requests
        response = requests.get('http://localhost:8080/api/actuator/health', timeout=5)
        return response.status_code == 200
    except:
        return False

def register_test_user():
    """Register test user if not exists"""
    try:
        import requests
        response = requests.post(
            'http://localhost:8080/api/auth/register',
            json={
                "username": "testuser",
                "email": "testuser@test.com",
                "password": "password123",
                "confirmPassword": "password123",
                "fullName": "Test User"
            },
            timeout=10
        )
        return response.status_code in [200, 201, 400]
    except:
        return False

def run_locust_test():
    """Run Locust test with proper configuration"""
    
    print("🚀 Locust CI Performance Test")
    print("🚀 Locust CI性能测试")
    print("=" * 45)
    
    # Setup logging fix
    setup_logging_fix()
    
    # Create directories
    os.makedirs('reports', exist_ok=True)
    
    # Check backend
    print("🔍 Checking backend...")
    if not check_backend():
        print("❌ Backend is not running")
        return False
    
    print("✅ Backend is running")
    
    # Register test user
    print("📝 Registering test user...")
    if register_test_user():
        print("✅ Test user ready")
    else:
        print("⚠️  Test user registration failed, continuing...")
    
    # Locust command with explicit log file
    cmd = [
        sys.executable, '-m', 'locust',
        '-f', 'locustfile_auth.py',
        '--host=http://localhost:8080',
        '--users=10',
        '--spawn-rate=2',
        '--run-time=60s',
        '--headless',
        '--html=reports/locust_ci_report.html',
        '--csv=reports/locust_ci_stats',
        '--csv-full-history',
        '--logfile=logs/locust_ci.log',  # Explicit log file
        '--loglevel=WARNING'
    ]
    
    print(f"Running: {' '.join(cmd)}")
    print("-" * 45)
    
    try:
        # Run Locust
        result = subprocess.run(cmd, timeout=180, capture_output=True, text=True)
        
        print("Test completed!")
        print(f"Return code: {result.returncode}")
        
        if result.stdout:
            print("Output:")
            print(result.stdout)
        
        if result.stderr:
            print("Errors:")
            print(result.stderr)
        
        # Check results
        if result.returncode == 0:
            print("✅ Locust test completed successfully")
            
            if os.path.exists('reports/locust_ci_report.html'):
                print("✅ HTML report generated")
            
            if os.path.exists('reports/locust_ci_stats_stats.csv'):
                print("✅ CSV stats generated")
                
                # Show basic stats
                with open('reports/locust_ci_stats_stats.csv', 'r') as f:
                    lines = f.readlines()
                    if len(lines) > 1:
                        print("\n📊 Locust Test Results:")
                        print(lines[0].strip())
                        print(lines[-1].strip())
            
            return True
        else:
            print("❌ Locust test failed")
            return False
            
    except subprocess.TimeoutExpired:
        print("❌ Locust test timed out")
        return False
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

def main():
    """Main function"""
    try:
        success = run_locust_test()
        
        if success:
            print("\n🎉 Locust CI test completed successfully!")
            print("🎉 Locust CI测试成功完成！")
            return 0
        else:
            print("\n❌ Locust CI test failed")
            print("❌ Locust CI测试失败")
            return 1
    except Exception as e:
        print(f"❌ Unexpected error: {e}")
        return 1

if __name__ == "__main__":
    sys.exit(main())
