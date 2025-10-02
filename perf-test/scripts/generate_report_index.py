#!/usr/bin/env python3
"""
Generate an index page for performance test reports
"""

import os
import json
from datetime import datetime

def generate_report_index(reports_dir="reports"):
    """Generate an index.html file for the reports directory"""
    
    # Find all HTML and CSV files
    html_files = []
    csv_files = []
    log_files = []
    
    if os.path.exists(reports_dir):
        for file in os.listdir(reports_dir):
            if file.endswith('.html'):
                html_files.append(file)
            elif file.endswith('.csv'):
                csv_files.append(file)
            elif file.endswith('.log'):
                log_files.append(file)
    
    # Generate HTML content
    html_content = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Performance Test Reports</title>
    <style>
        body {{ 
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; 
            margin: 0; 
            padding: 20px; 
            background: #f8f9fa;
        }}
        .container {{ 
            max-width: 900px; 
            margin: 0 auto; 
            background: white; 
            border-radius: 8px; 
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            overflow: hidden;
        }}
        .header {{
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            text-align: center;
        }}
        .content {{ padding: 30px; }}
        .report-section {{ margin-bottom: 30px; }}
        .report-section h2 {{ 
            color: #333; 
            border-bottom: 2px solid #e9ecef; 
            padding-bottom: 10px;
        }}
        .report-link {{ 
            display: block; 
            padding: 15px; 
            margin: 10px 0; 
            background: #f8f9fa; 
            border: 1px solid #e9ecef;
            border-radius: 6px; 
            text-decoration: none; 
            color: #495057;
            transition: all 0.2s ease;
        }}
        .report-link:hover {{ 
            background: #e9ecef; 
            border-color: #dee2e6;
            transform: translateY(-1px);
        }}
        .file-info {{
            font-size: 0.9em;
            color: #6c757d;
            margin-top: 5px;
        }}
        .timestamp {{ 
            text-align: center; 
            color: #6c757d; 
            font-size: 0.9em; 
            margin-top: 20px;
            padding-top: 20px;
            border-top: 1px solid #e9ecef;
        }}
        .icon {{ margin-right: 10px; }}
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🚀 Performance Test Reports</h1>
            <p>Locust Performance Testing Results</p>
        </div>
        
        <div class="content">
            {generate_html_section(html_files)}
            {generate_csv_section(csv_files)}
            {generate_log_section(log_files)}
        </div>
        
        <div class="timestamp">
            Generated on {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
        </div>
    </div>
</body>
</html>"""
    
    # Write the file
    index_path = os.path.join(reports_dir, 'index.html')
    with open(index_path, 'w', encoding='utf-8') as f:
        f.write(html_content)
    
    print(f"✅ Generated report index: {index_path}")
    return index_path

def generate_html_section(html_files):
    """Generate HTML reports section"""
    if not html_files:
        return '<div class="report-section"><h2>📈 HTML Reports</h2><p>No HTML reports found.</p></div>'
    
    html = '<div class="report-section"><h2>📈 HTML Reports</h2>'
    for file in html_files:
        file_size = get_file_size(file)
        html += f'''
        <a href="{file}" class="report-link" target="_blank">
            <span class="icon">📊</span> {file.replace('_', ' ').replace('.html', '').title()}
            <div class="file-info">Size: {file_size}</div>
        </a>'''
    html += '</div>'
    return html

def generate_csv_section(csv_files):
    """Generate CSV reports section"""
    if not csv_files:
        return '<div class="report-section"><h2>📄 Data Files</h2><p>No CSV files found.</p></div>'
    
    html = '<div class="report-section"><h2>📄 Data Files</h2>'
    for file in csv_files:
        file_size = get_file_size(file)
        html += f'''
        <a href="{file}" class="report-link" target="_blank">
            <span class="icon">📊</span> {file.replace('_', ' ').replace('.csv', '').title()}
            <div class="file-info">Size: {file_size}</div>
        </a>'''
    html += '</div>'
    return html

def generate_log_section(log_files):
    """Generate log files section"""
    if not log_files:
        return '<div class="report-section"><h2>📝 Log Files</h2><p>No log files found.</p></div>'
    
    html = '<div class="report-section"><h2>📝 Log Files</h2>'
    for file in log_files:
        file_size = get_file_size(file)
        html += f'''
        <a href="{file}" class="report-link" target="_blank">
            <span class="icon">📋</span> {file.replace('_', ' ').replace('.log', '').title()}
            <div class="file-info">Size: {file_size}</div>
        </a>'''
    html += '</div>'
    return html

def get_file_size(filename):
    """Get human-readable file size"""
    try:
        size = os.path.getsize(filename)
        for unit in ['B', 'KB', 'MB', 'GB']:
            if size < 1024.0:
                return f"{size:.1f} {unit}"
            size /= 1024.0
        return f"{size:.1f} TB"
    except OSError:
        return "Unknown"

if __name__ == "__main__":
    generate_report_index()
