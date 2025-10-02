#!/usr/bin/env python3
"""
Test script for report index generation
"""

import os
import sys
sys.path.append('scripts')

from generate_report_index import generate_report_index, get_file_size

def test_file_size():
    """Test file size function"""
    print("🧪 Testing file size function...")
    
    # Test with existing file
    if os.path.exists('reports/test_stats_stats.csv'):
        size = get_file_size('reports/test_stats_stats.csv')
        print(f"✅ CSV file size: {size}")
    else:
        print("⚠️  No CSV file found for testing")
    
    # Test with non-existing file
    size = get_file_size('non_existing_file.txt')
    print(f"✅ Non-existing file size: {size}")

def test_report_generation():
    """Test report index generation"""
    print("\n🧪 Testing report index generation...")
    
    try:
        index_path = generate_report_index()
        print(f"✅ Generated index: {index_path}")
        
        if os.path.exists(index_path):
            with open(index_path, 'r', encoding='utf-8') as f:
                content = f.read()
                print(f"✅ Index file size: {len(content)} characters")
                
                # Check if logs are mentioned
                if 'Log Files' in content:
                    print("✅ Log Files section found")
                else:
                    print("⚠️  Log Files section not found")
                
                # Check if file sizes are shown
                if 'Size:' in content and 'Unknown' not in content:
                    print("✅ File sizes are displayed")
                else:
                    print("⚠️  File sizes not properly displayed")
        else:
            print("❌ Index file not created")
            
    except Exception as e:
        print(f"❌ Error generating report: {e}")

def main():
    """Main test function"""
    print("🚀 Testing Report Index Generation")
    print("=" * 50)
    
    test_file_size()
    test_report_generation()
    
    print("\n🎉 Test completed!")

if __name__ == "__main__":
    main()
