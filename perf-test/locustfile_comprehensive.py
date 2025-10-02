"""
Comprehensive Locust performance test for all APIs
Combines authentication, organization, and data file operations
"""
import json
import random
from locust import HttpUser, task, between
from config import Config
from utils import TestDataGenerator, ResponseValidator, token_manager

class ComprehensiveUser(HttpUser):
    """Comprehensive user behavior testing all APIs together"""
    
    wait_time = between(2, 6)
    
    def on_start(self):
        """Called when a user starts"""
        self.user_id = f"user_{random.randint(1000, 9999)}"
        self.test_data = TestDataGenerator()
        self.validator = ResponseValidator()
        self.organization_id = None
        self.data_file_id = None
        
        # Login first to get token
        self._login()
        # Get organization ID for data file operations
        self._get_organization_id()
    
    def _login(self):
        """Login to get authentication token"""
        response = self.client.post(
            Config.AUTH_LOGIN,
            json={
                "username": Config.DEFAULT_USERNAME,
                "password": Config.DEFAULT_PASSWORD
            }
        )
        
        if response.status_code == 200:
            try:
                data = response.json()
                token = data.get("accessToken")
                if token:
                    token_manager.set_token(self.user_id, token)
            except json.JSONDecodeError:
                pass
    
    def _get_organization_id(self):
        """Get a valid organization ID for data file operations"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        response = self.client.get(Config.ORG_TREE, headers=headers)
        if response.status_code == 200:
            try:
                tree_data = response.json()
                # Find a MODULE type node
                for node in self._find_module_nodes(tree_data):
                    self.organization_id = node.get("id")
                    break
                
                # If no MODULE found, use any available node
                if not self.organization_id and tree_data:
                    self.organization_id = tree_data[0].get("id")
                    
            except (json.JSONDecodeError, (KeyError, IndexError)):
                self.organization_id = 1  # Fallback
    
    def _find_module_nodes(self, tree_data):
        """Recursively find MODULE type nodes in the organization tree"""
        for node in tree_data:
            if node.get("type") == "MODULE":
                yield node
            if node.get("children"):
                yield from self._find_module_nodes(node["children"])
    
    # Authentication Tasks (20% of traffic)
    @task(2)
    def test_login(self):
        """Test user login"""
        with self.client.post(
            Config.AUTH_LOGIN,
            json={
                "username": Config.DEFAULT_USERNAME,
                "password": Config.DEFAULT_PASSWORD
            },
            catch_response=True,
            name="comprehensive_auth_login"
        ) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    if self.validator.validate_auth_response(response):
                        token = data.get("accessToken")
                        if token:
                            token_manager.set_token(self.user_id, token)
                        response.success()
                    else:
                        response.failure("Invalid response format")
                except json.JSONDecodeError:
                    response.failure("Invalid JSON response")
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(1)
    def test_get_current_user(self):
        """Test get current user info"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        with self.client.get(
            Config.AUTH_ME,
            headers=headers,
            catch_response=True,
            name="comprehensive_auth_me"
        ) as response:
            if response.status_code == 200:
                if self.validator.validate_user_response(response):
                    response.success()
                else:
                    response.failure("Invalid user response format")
            elif response.status_code == 401:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    # Organization Tasks (30% of traffic)
    @task(3)
    def test_get_organization_tree(self):
        """Test getting organization tree"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        with self.client.get(
            Config.ORG_TREE,
            headers=headers,
            catch_response=True,
            name="comprehensive_org_tree"
        ) as response:
            if response.status_code == 200:
                if self.validator.validate_organization_response(response):
                    response.success()
                else:
                    response.failure("Invalid organization response format")
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(2)
    def test_get_children_by_parent_id(self):
        """Test getting children by parent ID"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        parent_id = random.randint(1, Config.MAX_ORGANIZATION_NODES)
        
        with self.client.get(
            f"{Config.ORG_NODES}?parentId={parent_id}",
            headers=headers,
            catch_response=True,
            name="comprehensive_org_children"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(1)
    def test_create_organization_node(self):
        """Test creating organization node"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        node_data = self.test_data.generate_organization_node_data()
        
        with self.client.post(
            Config.ORG_NODES,
            json=node_data,
            headers=headers,
            catch_response=True,
            name="comprehensive_org_create"
        ) as response:
            if response.status_code in [200, 201]:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    # Data File Tasks (50% of traffic)
    @task(5)
    def test_query_data_files(self):
        """Test querying data files - most common operation"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        query_data = self.test_data.generate_query_data()
        
        with self.client.post(
            Config.DATA_FILES_QUERY,
            json=query_data,
            headers=headers,
            catch_response=True,
            name="comprehensive_datafile_query"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(3)
    def test_get_accessible_data_files(self):
        """Test getting accessible data files"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        page = random.randint(0, 5)
        size = random.randint(10, 50)
        
        with self.client.get(
            f"{Config.BACKEND_BASE_URL}/data-files/accessible?page={page}&size={size}",
            headers=headers,
            catch_response=True,
            name="comprehensive_datafile_accessible"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(2)
    def test_search_data_files(self):
        """Test searching data files by keyword"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        keywords = ["test", "data", "file", "sample", "demo"]
        keyword = random.choice(keywords)
        
        with self.client.get(
            f"{Config.BACKEND_BASE_URL}/data-files/search?keyword={keyword}",
            headers=headers,
            catch_response=True,
            name="comprehensive_datafile_search"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(2)
    def test_get_data_file_by_id(self):
        """Test getting data file by ID"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        data_file_id = random.randint(1, Config.MAX_DATA_FILES)
        
        with self.client.get(
            Config.DATA_FILE_BY_ID.format(id=data_file_id),
            headers=headers,
            catch_response=True,
            name="comprehensive_datafile_get"
        ) as response:
            if response.status_code in [200, 404]:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(1)
    def test_create_data_file(self):
        """Test creating data file"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers or not self.organization_id:
            return
        
        data_file_data = self.test_data.generate_data_file_data(self.organization_id)
        
        with self.client.post(
            Config.DATA_FILES,
            json=data_file_data,
            headers=headers,
            catch_response=True,
            name="comprehensive_datafile_create"
        ) as response:
            if response.status_code in [200, 201]:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(1)
    def test_get_statistics(self):
        """Test getting data file statistics"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        with self.client.get(
            Config.DATA_FILES_STATISTICS,
            headers=headers,
            catch_response=True,
            name="comprehensive_datafile_stats"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    # Health Check (occasional)
    @task(1)
    def test_health_check(self):
        """Test health check endpoint"""
        with self.client.get(
            Config.HEALTH_CHECK,
            catch_response=True,
            name="comprehensive_health_check"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
