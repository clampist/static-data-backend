#!/usr/bin/env python3
"""
Ultra Simple CI Performance Test
超简单CI性能测试

This script completely avoids Locust's logging configuration issues.
这个脚本完全避免Locust的日志配置问题。
"""

import os
import sys
import subprocess
import time
import tempfile

def check_backend():
    """Check if backend is running"""
    try:
        import requests
        response = requests.get('http://localhost:8080/api/actuator/health', timeout=5)
        return response.status_code == 200
    except:
        return False

def run_ultra_simple_test():
    """Run ultra simple performance test with custom logging setup"""
    
    print("🚀 Ultra Simple CI Performance Test")
    print("🚀 超简单CI性能测试")
    print("=" * 45)
    
    # Create directories
    os.makedirs('reports', exist_ok=True)
    
    # Check backend
    print("🔍 Checking backend...")
    if not check_backend():
        print("❌ Backend is not running")
        return False
    
    print("✅ Backend is running")
    
    # Set environment variables to control Locust logging
    env = os.environ.copy()
    env['LOCUST_LOG_LEVEL'] = 'WARNING'
    env['LOCUST_LOG_FILE'] = ''  # Disable log file
    
    # Use a temporary directory for any potential log files
    with tempfile.TemporaryDirectory() as temp_dir:
        env['TMPDIR'] = temp_dir
        
        # Ultra simple locust command
        cmd = [
            sys.executable, '-m', 'locust',
            '-f', 'locustfile_auth.py',
            '--host=http://localhost:8080',
            '--users=3',  # Even fewer users
            '--spawn-rate=1',
            '--run-time=20s',  # Shorter test
            '--headless',
            '--html=reports/ultra_simple_report.html',
            '--csv=reports/ultra_simple_stats',
            '--loglevel=ERROR'  # Only show errors
        ]
        
        print(f"Running: {' '.join(cmd)}")
        print("-" * 45)
        
        try:
            # Run with custom environment
            result = subprocess.run(
                cmd, 
                timeout=60,  # Shorter timeout
                capture_output=True, 
                text=True,
                env=env
            )
            
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
                print("✅ Test completed successfully")
                
                if os.path.exists('reports/ultra_simple_report.html'):
                    print("✅ HTML report generated")
                
                if os.path.exists('reports/ultra_simple_stats_stats.csv'):
                    print("✅ CSV stats generated")
                    
                    # Show basic stats
                    with open('reports/ultra_simple_stats_stats.csv', 'r') as f:
                        lines = f.readlines()
                        if len(lines) > 1:
                            print("\n📊 Basic Stats:")
                            print(lines[0].strip())
                            print(lines[-1].strip())
                
                return True
            else:
                print("❌ Test failed")
                return False
                
        except subprocess.TimeoutExpired:
            print("❌ Test timed out")
            return False
        except Exception as e:
            print(f"❌ Error: {e}")
            return False

def main():
    """Main function"""
    try:
        success = run_ultra_simple_test()
        
        if success:
            print("\n🎉 Ultra simple CI test completed successfully!")
            print("🎉 超简单CI测试成功完成！")
            return 0
        else:
            print("\n❌ Ultra simple CI test failed")
            print("❌ 超简单CI测试失败")
            return 1
    except Exception as e:
        print(f"❌ Unexpected error: {e}")
        return 1

if __name__ == "__main__":
    sys.exit(main())
