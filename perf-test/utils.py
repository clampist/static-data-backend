"""
Utility functions for Locust performance tests
"""
import json
import random
import string
from typing import Dict, Any, Optional
from faker import Faker
from config import Config

fake = Faker()

class TestDataGenerator:
    """Generate test data for performance tests"""
    
    @staticmethod
    def generate_user_data() -> Dict[str, str]:
        """Generate random user data for registration"""
        username = fake.user_name() + str(random.randint(1000, 9999))
        email = fake.email()
        password = ''.join(random.choices(string.ascii_letters + string.digits, k=12))
        
        return {
            "username": username,
            "email": email,
            "password": password,
            "confirmPassword": password,
            "firstName": fake.first_name(),
            "lastName": fake.last_name()
        }
    
    @staticmethod
    def generate_organization_node_data(parent_id: Optional[int] = None) -> Dict[str, Any]:
        """Generate random organization node data"""
        node_types = ["DEPARTMENT", "TEAM", "BUSINESS_DIRECTION", "MODULE"]
        node_type = random.choice(node_types)
        
        return {
            "name": fake.company() + " " + fake.word().title(),
            "description": fake.sentence(),
            "type": node_type,
            "parentId": parent_id,
            "sortOrder": random.randint(1, 100)
        }
    
    @staticmethod
    def generate_data_file_data(organization_id: int) -> Dict[str, Any]:
        """Generate random data file data"""
        return {
            "name": fake.file_name(),
            "description": fake.sentence(),
            "organizationId": organization_id,
            "dataStructure": {
                "fields": [
                    {
                        "name": fake.word(),
                        "type": random.choice(["STRING", "NUMBER", "BOOLEAN", "DATE"]),
                        "required": random.choice([True, False])
                    }
                    for _ in range(random.randint(3, 8))
                ]
            },
            "content": {
                fake.word(): fake.word() 
                for _ in range(random.randint(5, 15))
            }
        }
    
    @staticmethod
    def generate_query_data() -> Dict[str, Any]:
        """Generate query data for data file search"""
        return {
            "organizationId": random.randint(1, Config.MAX_ORGANIZATION_NODES),
            "filters": {
                "name": fake.word(),
                "description": fake.word()
            },
            "page": 0,
            "size": random.randint(10, 50)
        }

class ResponseValidator:
    """Validate API responses"""
    
    @staticmethod
    def validate_auth_response(response) -> bool:
        """Validate authentication response"""
        try:
            data = response.json()
            return "token" in data and "user" in data
        except:
            return False
    
    @staticmethod
    def validate_user_response(response) -> bool:
        """Validate user response"""
        try:
            data = response.json()
            required_fields = ["id", "username", "email"]
            return all(field in data for field in required_fields)
        except:
            return False
    
    @staticmethod
    def validate_organization_response(response) -> bool:
        """Validate organization response"""
        try:
            data = response.json()
            return isinstance(data, (list, dict))
        except:
            return False
    
    @staticmethod
    def validate_data_file_response(response) -> bool:
        """Validate data file response"""
        try:
            data = response.json()
            required_fields = ["id", "name", "organizationId"]
            return all(field in data for field in required_fields)
        except:
            return False

class TokenManager:
    """Manage authentication tokens"""
    
    def __init__(self):
        self.tokens = {}
    
    def set_token(self, user_id: str, token: str):
        """Set token for a user"""
        self.tokens[user_id] = token
    
    def get_token(self, user_id: str) -> Optional[str]:
        """Get token for a user"""
        return self.tokens.get(user_id)
    
    def get_auth_headers(self, user_id: str) -> Dict[str, str]:
        """Get authorization headers for a user"""
        token = self.get_token(user_id)
        if token:
            return {"Authorization": f"Bearer {token}"}
        return {}

# Global token manager instance
token_manager = TokenManager()
