"""
Locust performance test for Organization Management APIs
Based on backend/api-tests/test-organization-apis.sh
"""
import json
import random
from locust import HttpUser, task, between
from config import Config
from utils import TestDataGenerator, ResponseValidator, token_manager

class OrganizationUser(HttpUser):
    """User behavior for organization management API performance testing"""
    
    wait_time = between(2, 5)
    
    def on_start(self):
        """Called when a user starts"""
        self.user_id = f"user_{random.randint(1000, 9999)}"
        self.test_data = TestDataGenerator()
        self.validator = ResponseValidator()
        
        # Login first to get token
        self._login()
    
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
    
    @task(5)
    def test_get_organization_tree(self):
        """Test getting organization tree - most common operation"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        with self.client.get(
            Config.ORG_TREE,
            headers=headers,
            catch_response=True,
            name="org_get_tree"
        ) as response:
            if response.status_code == 200:
                if self.validator.validate_organization_response(response):
                    response.success()
                else:
                    response.failure("Invalid organization response format")
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(2)
    def test_get_node_types(self):
        """Test getting node types"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        with self.client.get(
            f"{Config.BACKEND_BASE_URL}/organization/node-types",
            headers=headers,
            catch_response=True,
            name="org_get_node_types"
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
            name="org_create_node"
        ) as response:
            if response.status_code in [200, 201]:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(3)
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
            name="org_get_children"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(2)
    def test_get_node_details(self):
        """Test getting specific node details"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        node_id = random.randint(1, Config.MAX_ORGANIZATION_NODES)
        
        with self.client.get(
            Config.ORG_NODE_BY_ID.format(id=node_id),
            headers=headers,
            catch_response=True,
            name="org_get_node_details"
        ) as response:
            if response.status_code in [200, 404]:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(1)
    def test_update_organization_node(self):
        """Test updating organization node"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        node_id = random.randint(1, Config.MAX_ORGANIZATION_NODES)
        update_data = {
            "name": f"Updated Node {random.randint(1000, 9999)}",
            "description": "Updated description for performance test"
        }
        
        with self.client.put(
            Config.ORG_NODE_BY_ID.format(id=node_id),
            json=update_data,
            headers=headers,
            catch_response=True,
            name="org_update_node"
        ) as response:
            if response.status_code in [200, 404]:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(1)
    def test_get_node_statistics(self):
        """Test getting node statistics"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        node_id = random.randint(1, Config.MAX_ORGANIZATION_NODES)
        
        with self.client.get(
            f"{Config.BACKEND_BASE_URL}/organization/nodes/{node_id}/stats",
            headers=headers,
            catch_response=True,
            name="org_get_node_stats"
        ) as response:
            if response.status_code in [200, 404]:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(1)
    def test_move_node(self):
        """Test moving node to different parent"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        node_id = random.randint(1, Config.MAX_ORGANIZATION_NODES)
        new_parent_id = random.randint(1, Config.MAX_ORGANIZATION_NODES)
        
        move_data = {"parentId": new_parent_id}
        
        with self.client.put(
            f"{Config.BACKEND_BASE_URL}/organization/nodes/{node_id}/move",
            json=move_data,
            headers=headers,
            catch_response=True,
            name="org_move_node"
        ) as response:
            if response.status_code in [200, 404]:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(2)
    def test_search_nodes(self):
        """Test searching nodes"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        keywords = ["test", "team", "department", "module", "development"]
        keyword = random.choice(keywords)
        
        with self.client.get(
            f"{Config.BACKEND_BASE_URL}/organization/search?keyword={keyword}",
            headers=headers,
            catch_response=True,
            name="org_search_nodes"
        ) as response:
            if response.status_code == 200:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
    
    @task(1)
    def test_delete_node(self):
        """Test deleting organization node"""
        headers = token_manager.get_auth_headers(self.user_id)
        if not headers:
            return
        
        # Use a high ID to avoid deleting important nodes
        node_id = random.randint(100, 999)
        
        with self.client.delete(
            Config.ORG_NODE_BY_ID.format(id=node_id),
            headers=headers,
            catch_response=True,
            name="org_delete_node"
        ) as response:
            if response.status_code in [200, 204, 404]:
                response.success()
            else:
                response.failure(f"HTTP {response.status_code}")
