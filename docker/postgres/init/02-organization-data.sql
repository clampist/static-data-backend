-- Default Organization Structure Data (English Version)
-- This script creates a default organizational hierarchy

-- Insert root organization (Headquarters)
INSERT INTO organization_nodes (id, name, description, type, parent_id, sort_order, created_at, updated_at, created_by, updated_by) 
VALUES (1, 'Headquarters', 'Main company headquarters and executive leadership', 'DEPARTMENT', NULL, 1, NOW(), NOW(), 'system', 'system');

-- Insert top-level departments
INSERT INTO organization_nodes (id, name, description, type, parent_id, sort_order, created_at, updated_at, created_by, updated_by) 
VALUES 
(2, 'Product Department', 'Product development, design, and innovation', 'DEPARTMENT', 1, 1, NOW(), NOW(), 'system', 'system'),
(3, 'Engineering Department', 'Software development and technical operations', 'DEPARTMENT', 1, 2, NOW(), NOW(), 'system', 'system'),
(4, 'Sales & Marketing', 'Sales, marketing, and customer acquisition', 'DEPARTMENT', 1, 3, NOW(), NOW(), 'system', 'system'),
(5, 'Operations', 'Business operations, HR, and administration', 'DEPARTMENT', 1, 4, NOW(), NOW(), 'system', 'system');

-- Insert teams under Product Department
INSERT INTO organization_nodes (id, name, description, type, parent_id, sort_order, created_at, updated_at, created_by, updated_by) 
VALUES 
(6, 'Product Management', 'Product strategy, roadmap, and requirements', 'TEAM', 2, 1, NOW(), NOW(), 'system', 'system'),
(7, 'UX/UI Design', 'User experience and interface design', 'TEAM', 2, 2, NOW(), NOW(), 'system', 'system'),
(8, 'Product Analytics', 'Data analysis and product insights', 'TEAM', 2, 3, NOW(), NOW(), 'system', 'system');

-- Insert teams under Engineering Department
INSERT INTO organization_nodes (id, name, description, type, parent_id, sort_order, created_at, updated_at, created_by, updated_by) 
VALUES 
(9, 'Frontend Team', 'Frontend development and user interface', 'TEAM', 3, 1, NOW(), NOW(), 'system', 'system'),
(10, 'Backend Team', 'Backend services and API development', 'TEAM', 3, 2, NOW(), NOW(), 'system', 'system'),
(11, 'DevOps Team', 'Infrastructure, deployment, and operations', 'TEAM', 3, 3, NOW(), NOW(), 'system', 'system'),
(12, 'QA Team', 'Quality assurance and testing', 'TEAM', 3, 4, NOW(), NOW(), 'system', 'system'),
(13, 'Data Engineering', 'Data pipeline and analytics infrastructure', 'TEAM', 3, 5, NOW(), NOW(), 'system', 'system');

-- Insert business directions under Frontend Team
INSERT INTO organization_nodes (id, name, description, type, parent_id, sort_order, created_at, updated_at, created_by, updated_by) 
VALUES 
(14, 'Web Development', 'Web application development', 'BUSINESS_DIRECTION', 9, 1, NOW(), NOW(), 'system', 'system'),
(15, 'Mobile Development', 'Mobile application development', 'BUSINESS_DIRECTION', 9, 2, NOW(), NOW(), 'system', 'system'),
(16, 'Design System', 'Component library and design standards', 'BUSINESS_DIRECTION', 9, 3, NOW(), NOW(), 'system', 'system');

-- Insert business directions under Backend Team
INSERT INTO organization_nodes (id, name, description, type, parent_id, sort_order, created_at, updated_at, created_by, updated_by) 
VALUES 
(17, 'API Development', 'RESTful APIs and microservices', 'BUSINESS_DIRECTION', 10, 1, NOW(), NOW(), 'system', 'system'),
(18, 'Database Management', 'Database design and optimization', 'BUSINESS_DIRECTION', 10, 2, NOW(), NOW(), 'system', 'system'),
(19, 'Integration Services', 'Third-party integrations and middleware', 'BUSINESS_DIRECTION', 10, 3, NOW(), NOW(), 'system', 'system');

-- Insert modules under Web Development
INSERT INTO organization_nodes (id, name, description, type, parent_id, sort_order, created_at, updated_at, created_by, updated_by) 
VALUES 
(20, 'User Dashboard', 'Main user interface and dashboard', 'MODULE', 14, 1, NOW(), NOW(), 'system', 'system'),
(21, 'Admin Panel', 'Administrative interface and controls', 'MODULE', 14, 2, NOW(), NOW(), 'system', 'system'),
(22, 'Authentication', 'Login, registration, and security', 'MODULE', 14, 3, NOW(), NOW(), 'system', 'system');

-- Insert modules under Mobile Development
INSERT INTO organization_nodes (id, name, description, type, parent_id, sort_order, created_at, updated_at, created_by, updated_by) 
VALUES 
(23, 'iOS App', 'Native iOS application', 'MODULE', 15, 1, NOW(), NOW(), 'system', 'system'),
(24, 'Android App', 'Native Android application', 'MODULE', 15, 2, NOW(), NOW(), 'system', 'system'),
(25, 'React Native', 'Cross-platform mobile development', 'MODULE', 15, 3, NOW(), NOW(), 'system', 'system');

-- Insert modules under API Development
INSERT INTO organization_nodes (id, name, description, type, parent_id, sort_order, created_at, updated_at, created_by, updated_by) 
VALUES 
(26, 'User Management API', 'User authentication and profile management', 'MODULE', 17, 1, NOW(), NOW(), 'system', 'system'),
(27, 'Data Management API', 'Data file and content management', 'MODULE', 17, 2, NOW(), NOW(), 'system', 'system'),
(28, 'Organization API', 'Organizational structure management', 'MODULE', 17, 3, NOW(), NOW(), 'system', 'system');

-- Insert teams under Sales & Marketing
INSERT INTO organization_nodes (id, name, description, type, parent_id, sort_order, created_at, updated_at, created_by, updated_by) 
VALUES 
(29, 'Sales Team', 'Direct sales and customer acquisition', 'TEAM', 4, 1, NOW(), NOW(), 'system', 'system'),
(30, 'Marketing Team', 'Brand marketing and lead generation', 'TEAM', 4, 2, NOW(), NOW(), 'system', 'system'),
(31, 'Customer Success', 'Customer onboarding and support', 'TEAM', 4, 3, NOW(), NOW(), 'system', 'system');

-- Insert teams under Operations
INSERT INTO organization_nodes (id, name, description, type, parent_id, sort_order, created_at, updated_at, created_by, updated_by) 
VALUES 
(32, 'Human Resources', 'Recruitment, benefits, and employee relations', 'TEAM', 5, 1, NOW(), NOW(), 'system', 'system'),
(33, 'Finance', 'Accounting, budgeting, and financial planning', 'TEAM', 5, 2, NOW(), NOW(), 'system', 'system'),
(34, 'Legal & Compliance', 'Legal affairs and regulatory compliance', 'TEAM', 5, 3, NOW(), NOW(), 'system', 'system');

-- Update sequence to avoid conflicts
SELECT setval('organization_nodes_id_seq', 34, true);
