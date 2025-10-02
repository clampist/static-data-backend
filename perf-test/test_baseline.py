#!/usr/bin/env python3
"""
Test script for baseline checking
"""

import os
import csv
import sys

def test_csv_parsing():
    """Test CSV parsing logic"""
    stats_file = "reports/test_stats_stats.csv"
    
    if not os.path.exists(stats_file):
        print(f"❌ Stats file not found: {stats_file}")
        return False
    
    try:
        with open(stats_file, 'r') as f:
            reader = csv.DictReader(f)
            rows = list(reader)
            
            print(f"📊 CSV file has {len(rows)} rows")
            print(f"📋 Columns: {reader.fieldnames}")
            
            # 找到 Aggregated 行
            aggregated_row = None
            for i, row in enumerate(rows):
                print(f"Row {i}: Type='{row.get('Type')}', Name='{row.get('Name')}'")
                # 检查 Type 列为空且 Name 列为 'Aggregated'
                if (row.get('Type') == '' or row.get('Type') is None) and row.get('Name') == 'Aggregated':
                    aggregated_row = row
                    break
            
            if not aggregated_row:
                print(f"❌ No aggregated data found in {stats_file}")
                return False
            
            print(f"✅ Found aggregated row: {aggregated_row}")
            
            # 处理可能的列名变体
            request_count = int(aggregated_row.get('Request Count', aggregated_row.get('request_count', 0)))
            failure_count = int(aggregated_row.get('Failure Count', aggregated_row.get('failure_count', 0)))
            avg_response_time = float(aggregated_row.get('Average Response Time', aggregated_row.get('avg_response_time', 0)))
            requests_per_second = float(aggregated_row.get('Requests/s', aggregated_row.get('requests_per_second', 0)))
            
            success_rate = (request_count - failure_count) / max(request_count, 1)
            
            print(f"📈 Test Results:")
            print(f"  Request Count: {request_count}")
            print(f"  Failure Count: {failure_count}")
            print(f"  Success Rate: {success_rate:.2%}")
            print(f"  Avg Response Time: {avg_response_time:.2f}ms")
            print(f"  Requests/s: {requests_per_second:.2f}")
            
            # 检查基准
            baseline = {
                "max_avg_response_time": 200,  # ms
                "min_success_rate": 0.85,      # 85%
                "min_rps": 3,                  # requests per second
            }
            
            passed = True
            if avg_response_time > baseline["max_avg_response_time"]:
                print(f"❌ Average response time {avg_response_time:.2f}ms exceeds limit {baseline['max_avg_response_time']}ms")
                passed = False
            else:
                print(f"✅ Average response time {avg_response_time:.2f}ms is within limit {baseline['max_avg_response_time']}ms")
            
            if success_rate < baseline["min_success_rate"]:
                print(f"❌ Success rate {success_rate:.2%} is below limit {baseline['min_success_rate']:.2%}")
                passed = False
            else:
                print(f"✅ Success rate {success_rate:.2%} is above limit {baseline['min_success_rate']:.2%}")
            
            if requests_per_second < baseline["min_rps"]:
                print(f"❌ Requests per second {requests_per_second:.2f} is below limit {baseline['min_rps']}")
                passed = False
            else:
                print(f"✅ Requests per second {requests_per_second:.2f} is above limit {baseline['min_rps']}")
            
            if passed:
                print("🎉 All baseline checks passed!")
                return True
            else:
                print("❌ Some baseline checks failed")
                return False
                
    except Exception as e:
        print(f"❌ Error loading {stats_file}: {e}")
        return False

if __name__ == "__main__":
    success = test_csv_parsing()
    sys.exit(0 if success else 1)
