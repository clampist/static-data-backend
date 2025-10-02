#!/usr/bin/env python3
"""
Performance Baseline Check Script
性能基准检查脚本

This script checks if performance metrics meet the baseline requirements.
这个脚本检查性能指标是否满足基准要求。
"""

import csv
import json
import os
import sys
from typing import Dict, List, Optional

class PerformanceBaseline:
    """Performance baseline configuration"""
    
    # 性能基准配置
    BASELINE_METRICS = {
        "auth": {
            "max_avg_response_time": 200,  # ms
            "min_success_rate": 0.95,      # 95%
            "min_rps": 10,                 # requests per second
        },
        "organization": {
            "max_avg_response_time": 300,  # ms
            "min_success_rate": 0.95,      # 95%
            "min_rps": 5,                  # requests per second
        },
        "datafile": {
            "max_avg_response_time": 500,  # ms
            "min_success_rate": 0.90,      # 90%
            "min_rps": 3,                  # requests per second
        },
        "comprehensive": {
            "max_avg_response_time": 400,  # ms
            "min_success_rate": 0.90,      # 90%
            "min_rps": 5,                  # requests per second
        }
    }
    
    def __init__(self, reports_dir: str = "reports"):
        self.reports_dir = reports_dir
    
    def load_test_results(self, test_type: str) -> Optional[Dict]:
        """Load test results from CSV files"""
        stats_file = os.path.join(self.reports_dir, f"{test_type}_performance_stats_stats.csv")
        
        if not os.path.exists(stats_file):
            print(f"❌ Stats file not found: {stats_file}")
            return None
        
        try:
            with open(stats_file, 'r') as f:
                reader = csv.DictReader(f)
                rows = list(reader)
                
                # 找到 Aggregated 行
                aggregated_row = None
                for row in rows:
                    if row.get('Type') == 'Aggregated':
                        aggregated_row = row
                        break
                
                if not aggregated_row:
                    print(f"❌ No aggregated data found in {stats_file}")
                    return None
                
                return {
                    'request_count': int(aggregated_row.get('Request Count', 0)),
                    'failure_count': int(aggregated_row.get('Failure Count', 0)),
                    'avg_response_time': float(aggregated_row.get('Average Response Time', 0)),
                    'requests_per_second': float(aggregated_row.get('Requests/s', 0)),
                    'success_rate': (int(aggregated_row.get('Request Count', 0)) - int(aggregated_row.get('Failure Count', 0))) / max(int(aggregated_row.get('Request Count', 1)), 1)
                }
                
        except Exception as e:
            print(f"❌ Error loading {stats_file}: {e}")
            return None
    
    def check_baseline(self, test_type: str, results: Dict) -> bool:
        """Check if results meet baseline requirements"""
        if test_type not in self.BASELINE_METRICS:
            print(f"❌ Unknown test type: {test_type}")
            return False
        
        baseline = self.BASELINE_METRICS[test_type]
        passed = True
        
        print(f"🔍 Checking {test_type} performance baseline...")
        print(f"   📊 Results: {results['request_count']} requests, {results['success_rate']:.2%} success rate")
        print(f"   📈 Avg Response Time: {results['avg_response_time']:.1f}ms (baseline: <{baseline['max_avg_response_time']}ms)")
        print(f"   🚀 RPS: {results['requests_per_second']:.1f} (baseline: >{baseline['min_rps']})")
        
        # 检查响应时间
        if results['avg_response_time'] > baseline['max_avg_response_time']:
            print(f"   ❌ Response time too high: {results['avg_response_time']:.1f}ms > {baseline['max_avg_response_time']}ms")
            passed = False
        else:
            print(f"   ✅ Response time OK: {results['avg_response_time']:.1f}ms <= {baseline['max_avg_response_time']}ms")
        
        # 检查成功率
        if results['success_rate'] < baseline['min_success_rate']:
            print(f"   ❌ Success rate too low: {results['success_rate']:.2%} < {baseline['min_success_rate']:.2%}")
            passed = False
        else:
            print(f"   ✅ Success rate OK: {results['success_rate']:.2%} >= {baseline['min_success_rate']:.2%}")
        
        # 检查RPS
        if results['requests_per_second'] < baseline['min_rps']:
            print(f"   ❌ RPS too low: {results['requests_per_second']:.1f} < {baseline['min_rps']}")
            passed = False
        else:
            print(f"   ✅ RPS OK: {results['requests_per_second']:.1f} >= {baseline['min_rps']}")
        
        return passed
    
    def run_all_checks(self) -> bool:
        """Run baseline checks for all test types"""
        print("🚀 Starting Performance Baseline Check")
        print("=" * 50)
        
        all_passed = True
        
        for test_type in self.BASELINE_METRICS.keys():
            print(f"\n📋 Testing {test_type.upper()} API...")
            
            results = self.load_test_results(test_type)
            if results is None:
                print(f"❌ Failed to load results for {test_type}")
                all_passed = False
                continue
            
            if not self.check_baseline(test_type, results):
                print(f"❌ {test_type} performance baseline check FAILED")
                all_passed = False
            else:
                print(f"✅ {test_type} performance baseline check PASSED")
        
        print("\n" + "=" * 50)
        if all_passed:
            print("🎉 All performance baseline checks PASSED!")
            return True
        else:
            print("❌ Some performance baseline checks FAILED!")
            return False

def main():
    """Main function"""
    import argparse
    
    parser = argparse.ArgumentParser(description='Check performance baseline')
    parser.add_argument('--reports-dir', default='reports', 
                       help='Directory containing performance test reports')
    parser.add_argument('--test-type', choices=['auth', 'organization', 'datafile', 'comprehensive'],
                       help='Check specific test type only')
    
    args = parser.parse_args()
    
    baseline_checker = PerformanceBaseline(args.reports_dir)
    
    if args.test_type:
        # Check specific test type
        results = baseline_checker.load_test_results(args.test_type)
        if results is None:
            sys.exit(1)
        
        if baseline_checker.check_baseline(args.test_type, results):
            print(f"✅ {args.test_type} performance baseline check PASSED")
            sys.exit(0)
        else:
            print(f"❌ {args.test_type} performance baseline check FAILED")
            sys.exit(1)
    else:
        # Check all test types
        if baseline_checker.run_all_checks():
            sys.exit(0)
        else:
            sys.exit(1)

if __name__ == "__main__":
    main()
