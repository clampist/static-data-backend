"""
Locust performance test for Data File APIs
Based on backend/api-tests/test-final-datafile.sh
"""
import json
import random
from locust import HttpUser, task, between
from config import Config
from utils import TestDataGenerator, ResponseValidator, token_manager

class DataFileUser(HttpUser):
    """User behavior for data file API performance testing"""
    
    wait_time = between(3, 7)
    
    def on_start(self):
        """Called when a user starts"""
        self.user_id = f"user_{random.randint(1000, 9999)}"
        self.test_data = TestDataGenerator()
        self.validator = ResponseValidator()
        self.organization_id = None
        
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
    
    @task(4)
    def test_get_data_file_types(self):
        """Test getting supported data types"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        with self.client.get(
            f"{Config.BACKEND_BASE_URL}/data-files/data-types",
            headers=headers,
            catch_response=True,
            name="datafile_get_types"
        ) as response:
            if response.status_code == 200:
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
            name="datafile_create"
        ) as response:
            if response.status_code in [200, 201]:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(3)
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
            name="datafile_get_by_id"
        ) as response:
            if response.status_code in [200, 404]:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(2)
    def test_update_data_file(self):
        """Test updating data file"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        data_file_id = random.randint(1, Config.MAX_DATA_FILES)
        update_data = {
            "name": f"Updated Data File {random.randint(1000, 9999)}",
            "description": "Updated description for performance test"
        }
        
        with self.client.put(
            Config.DATA_FILE_BY_ID.format(id=data_file_id),
            json=update_data,
            headers=headers,
            catch_response=True,
            name="datafile_update"
        ) as response:
            if response.status_code in [200, 404]:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
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
            name="datafile_query"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(3)
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
            name="datafile_search"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(2)
    def test_get_data_files_by_organization(self):
        """Test getting data files by organization"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        org_id = random.randint(1, Config.MAX_ORGANIZATION_NODES)
        
        with self.client.get(
            f"{Config.BACKEND_BASE_URL}/data-files/organization/{org_id}",
            headers=headers,
            catch_response=True,
            name="datafile_get_by_org"
        ) as response:
            if response.status_code in [200, 404]:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(2)
    def test_get_recent_data_files(self):
        """Test getting recent data files"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        limit = random.randint(5, 20)
        
        with self.client.get(
            f"{Config.BACKEND_BASE_URL}/data-files/recent?limit={limit}",
            headers=headers,
            catch_response=True,
            name="datafile_get_recent"
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
            name="datafile_get_accessible"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(2)
    def test_get_statistics(self):
        """Test getting data file statistics"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        with self.client.get(
            Config.DATA_FILES_STATISTICS,
            headers=headers,
            catch_response=True,
            name="datafile_get_statistics"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(1)
    def test_query_by_data_type(self):
        """Test querying by specific data type"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        data_types = ["STRING", "INTEGER", "BOOLEAN", "DECIMAL", "DATE", "DATETIME", "JSON"]
        data_type = random.choice(data_types)
        
        with self.client.get(
            f"{Config.BACKEND_BASE_URL}/data-files/data-type/{data_type}",
            headers=headers,
            catch_response=True,
            name="datafile_query_by_type"
        ) as response:
            if response.status_code in [200, 404]:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(1)
    def test_delete_data_file(self):
        """Test deleting data file"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        # Use a high ID to avoid deleting important files
        data_file_id = random.randint(100, 999)
        
        with self.client.delete(
            Config.DATA_FILE_BY_ID.format(id=data_file_id),
            headers=headers,
            catch_response=True,
            name="datafile_delete"
        ) as response:
            if response.status_code in [200, 204, 404]:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
