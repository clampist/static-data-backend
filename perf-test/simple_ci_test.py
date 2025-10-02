#!/usr/bin/env python3
"""
Simple CI Performance Test
简单CI性能测试

This script runs a basic performance test without complex logging configuration.
这个脚本运行基本的性能测试，不涉及复杂的日志配置。
"""

import os
import sys
import subprocess
import time

def check_backend():
    """Check if backend is running"""
    try:
        import requests
        response = requests.get('http://localhost:8080/api/actuator/health', timeout=5)
        return response.status_code == 200
    except:
        return False

def run_simple_test():
    """Run a simple performance test"""
    
    print("🚀 Simple CI Performance Test")
    print("🚀 简单CI性能测试")
    print("=" * 40)
    
    # Create directories
    os.makedirs('reports', exist_ok=True)
    
    # Check backend
    print("🔍 Checking backend...")
    if not check_backend():
        print("❌ Backend is not running")
        return False
    
    print("✅ Backend is running")
    
    # Simple locust command without log file
    cmd = [
        sys.executable, '-m', 'locust',
        '-f', 'locustfile_auth.py',
        '--host=http://localhost:8080',
        '--users=5',
        '--spawn-rate=1',
        '--run-time=30s',
        '--headless',
        '--html=reports/simple_test_report.html',
        '--csv=reports/simple_test_stats',
        '--loglevel=WARNING'  # Reduce log output
    ]
    
    print(f"Running: {' '.join(cmd)}")
    print("-" * 40)
    
    try:
        # Run with timeout
        result = subprocess.run(cmd, timeout=120, capture_output=True, text=True)
        
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
            
            if os.path.exists('reports/simple_test_report.html'):
                print("✅ HTML report generated")
            
            if os.path.exists('reports/simple_test_stats_stats.csv'):
                print("✅ CSV stats generated")
                
                # Show basic stats
                with open('reports/simple_test_stats_stats.csv', 'r') as f:
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
        success = run_simple_test()
        
        if success:
            print("\n🎉 Simple CI test completed successfully!")
            print("🎉 简单CI测试成功完成！")
            return 0
        else:
            print("\n❌ Simple CI test failed")
            print("❌ 简单CI测试失败")
            return 1
    except Exception as e:
        print(f"❌ Unexpected error: {e}")
        return 1

if __name__ == "__main__":
    sys.exit(main())
