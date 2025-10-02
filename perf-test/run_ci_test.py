#!/usr/bin/env python3
"""
CI Performance Test Runner
CI性能测试运行器

This script runs performance tests in CI environment without logging issues.
这个脚本在CI环境中运行性能测试，避免日志问题。
"""

import os
import sys
import subprocess
import logging

# Disable Locust's default logging configuration
os.environ['LOCUST_LOG_LEVEL'] = 'INFO'
os.environ['LOCUST_LOG_FILE'] = ''

def setup_logging():
    """Setup basic logging to console"""
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(levelname)s - %(message)s',
        handlers=[logging.StreamHandler()]
    )

def run_performance_test():
    """Run performance test with proper configuration"""
    
    # Create necessary directories
    os.makedirs('reports', exist_ok=True)
    os.makedirs('logs', exist_ok=True)
    
    # Locust command
    cmd = [
        sys.executable, '-m', 'locust',
        '-f', 'locustfile_auth.py',
        '--host=http://localhost:8080',
        '--users=10',
        '--spawn-rate=2',
        '--run-time=60s',
        '--headless',
        '--html=reports/ci_performance_report.html',
        '--csv=reports/ci_performance_stats',
        '--csv-full-history',
        '--loglevel=INFO'
    ]
    
    print("🚀 Running CI Performance Test")
    print(f"Command: {' '.join(cmd)}")
    print("=" * 50)
    
    try:
        # Run the test
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=300)
        
        print("STDOUT:")
        print(result.stdout)
        
        if result.stderr:
            print("STDERR:")
            print(result.stderr)
        
        if result.returncode == 0:
            print("✅ Performance test completed successfully")
            
            # Check output files
            if os.path.exists('reports/ci_performance_report.html'):
                print("✅ HTML report generated")
            else:
                print("❌ HTML report not found")
                
            if os.path.exists('reports/ci_performance_stats_stats.csv'):
                print("✅ CSV stats generated")
                
                # Show summary
                with open('reports/ci_performance_stats_stats.csv', 'r') as f:
                    lines = f.readlines()
                    if len(lines) > 1:
                        print("\n📊 Test Summary:")
                        print(lines[0].strip())  # Header
                        print(lines[-1].strip())  # Last line (Aggregated)
            else:
                print("❌ CSV stats not found")
                
            return True
        else:
            print(f"❌ Performance test failed with return code {result.returncode}")
            return False
            
    except subprocess.TimeoutExpired:
        print("❌ Performance test timed out")
        return False
    except Exception as e:
        print(f"❌ Error running performance test: {e}")
        return False

def main():
    """Main function"""
    setup_logging()
    
    print("🧪 CI Performance Test Runner")
    print("🧪 CI性能测试运行器")
    print("=" * 50)
    
    # Check if we're in the right directory
    if not os.path.exists('locustfile_auth.py'):
        print("❌ locustfile_auth.py not found. Please run from perf-test directory.")
        return 1
    
    # Run the test
    success = run_performance_test()
    
    if success:
        print("\n🎉 CI performance test completed successfully!")
        print("🎉 CI性能测试成功完成！")
        return 0
    else:
        print("\n❌ CI performance test failed")
        print("❌ CI性能测试失败")
        return 1

if __name__ == "__main__":
    sys.exit(main())
