# 静态数据平台 测试完整指南
# Static Data Platform Testing Complete Guide

## 📋 目录 / Table of Contents

- [概述 / Overview](#概述--overview)
- [测试环境配置 / Test Environment Configuration](#测试环境配置--test-environment-configuration)
- [单元测试 / Unit Testing](#单元测试--unit-testing)
- [集成测试 / Integration Testing](#集成测试--integration-testing)
- [API测试 / API Testing](#api测试--api-testing)
- [代码覆盖率 / Code Coverage](#代码覆盖率--code-coverage)
- [测试脚本 / Test Scripts](#测试脚本--test-scripts)
- [测试数据管理 / Test Data Management](#测试数据管理--test-data-management)
- [持续集成 / Continuous Integration](#持续集成--continuous-integration)
- [故障排除 / Troubleshooting](#故障排除--troubleshooting)

## 概述 / Overview

本指南提供了静态数据平台完整的测试解决方案，包括单元测试、集成测试、API测试和代码覆盖率分析。项目使用JUnit 5、Testcontainers、JaCoCo等现代测试工具，确保代码质量和系统稳定性。

This guide provides a complete testing solution for the Static Data Platform, including unit tests, integration tests, API tests, and code coverage analysis. The project uses modern testing tools like JUnit 5, Testcontainers, and JaCoCo to ensure code quality and system stability.

### 测试架构 / Testing Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Testing Pyramid                         │
│                                                             │
│  ┌─────────────────────────────────────────────────────────┐│
│  │                E2E Tests                               ││
│  │            (API Integration Tests)                     ││
│  └─────────────────────────────────────────────────────────┘│
│                                                             │
│  ┌─────────────────────────────────────────────────────────┐│
│  │              Integration Tests                         ││
│  │            (Service + Repository)                      ││
│  └─────────────────────────────────────────────────────────┘│
│                                                             │
│  ┌─────────────────────────────────────────────────────────┐│
│  │                Unit Tests                              ││
│  │            (Service + Utility)                         ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

### 测试工具栈 / Testing Tool Stack

- **JUnit 5**: 单元测试框架
- **Testcontainers**: 集成测试容器化
- **JaCoCo**: 代码覆盖率分析
- **Mockito**: Mock框架
- **Spring Boot Test**: Spring测试支持
- **PostgreSQL Testcontainer**: 数据库测试
- **Redis Testcontainer**: 缓存测试

## 测试环境配置 / Test Environment Configuration

### 环境要求 / Environment Requirements

- **Java**: 17+
- **Maven**: 3.9+
- **Docker**: 用于Testcontainers
- **内存**: 至少4GB可用内存

### 配置文件 / Configuration Files

#### application-test.yml
```yaml
spring:
  datasource:
    url: jdbc:tc:postgresql:15:///testdb
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver
  
  redis:
    host: localhost
    port: 6379
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    com.staticdata.platform: DEBUG
    org.springframework.test: DEBUG
    org.testcontainers: INFO
```

#### pom.xml测试依赖
```xml
<dependencies>
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Spring Boot Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Testcontainers -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- JaCoCo -->
    <dependency>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <version>0.8.12</version>
    </dependency>
</dependencies>
```

## 单元测试 / Unit Testing

### 测试结构 / Test Structure

```
src/test/java/com/staticdata/platform/
├── controller/          # Controller层测试
├── service/            # Service层测试
├── repository/         # Repository层测试
├── util/              # 工具类测试
├── security/          # 安全相关测试
└── config/            # 配置类测试
```

### Service层测试示例 / Service Layer Test Example

#### AuthServiceTest.java
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtils jwtUtils;
    
    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("用户登录成功")
    void testLoginSuccess() {
        // Given
        String username = "testuser";
        String password = "password123";
        String encodedPassword = "encoded_password";
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setEnabled(true);
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtUtils.generateToken(user)).thenReturn("jwt_token");

        // When
        LoginResponse response = authService.login(new LoginRequest(username, password));

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt_token");
        assertThat(response.getUser().getUsername()).isEqualTo(username);
        
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).matches(password, encodedPassword);
        verify(jwtUtils).generateToken(user);
    }

    @Test
    @DisplayName("用户登录失败 - 用户名不存在")
    void testLoginFailure_UserNotFound() {
        // Given
        String username = "nonexistent";
        String password = "password123";
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(new LoginRequest(username, password)))
            .isInstanceOf(BusinessException.class)
            .hasMessage("用户名或密码错误");
    }
}
```

### Controller层测试示例 / Controller Layer Test Example

#### AuthControllerTest.java
```java
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AuthService authService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("用户登录API测试")
    void testLogin() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("testuser", "password123");
        LoginResponse response = new LoginResponse();
        response.setAccessToken("jwt_token");
        response.setExpiresIn(86400000L);
        
        when(authService.login(request)).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt_token"))
                .andExpect(jsonPath("$.expiresIn").value(86400000L));
    }

    @Test
    @DisplayName("用户注册API测试")
    void testRegister() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        request.setFullName("New User");
        
        UserDto userDto = new UserDto();
        userDto.setUsername("newuser");
        userDto.setEmail("newuser@example.com");
        
        when(authService.register(request)).thenReturn(userDto);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));
    }
}
```

### 工具类测试示例 / Utility Class Test Example

#### JwtUtilsTest.java
```java
@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    private JwtUtils jwtUtils;
    
    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        jwtUtils.setJwtSecret("test_secret_key_that_is_long_enough_for_hs512_algorithm");
        jwtUtils.setJwtExpiration(86400000L);
    }

    @Test
    @DisplayName("生成JWT Token")
    void testGenerateToken() {
        // Given
        UserPrincipal userPrincipal = new UserPrincipal();
        userPrincipal.setId(1L);
        userPrincipal.setUsername("testuser");

        // When
        String token = jwtUtils.generateToken(userPrincipal);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtUtils.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("验证JWT Token")
    void testValidateToken() {
        // Given
        UserPrincipal userPrincipal = new UserPrincipal();
        userPrincipal.setId(1L);
        userPrincipal.setUsername("testuser");
        
        String token = jwtUtils.generateToken(userPrincipal);

        // When & Then
        assertThat(jwtUtils.validateToken(token)).isTrue();
        assertThat(jwtUtils.validateToken("invalid_token")).isFalse();
    }

    @Test
    @DisplayName("从Token获取用户名")
    void testGetUsernameFromToken() {
        // Given
        UserPrincipal userPrincipal = new UserPrincipal();
        userPrincipal.setId(1L);
        userPrincipal.setUsername("testuser");
        
        String token = jwtUtils.generateToken(userPrincipal);

        // When
        String username = jwtUtils.getUsernameFromToken(token);

        // Then
        assertThat(username).isEqualTo("testuser");
    }
}
```

## 集成测试 / Integration Testing

### Testcontainers配置 / Testcontainers Configuration

#### 基础测试类 / Base Test Class
```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }
}
```

### Repository层集成测试 / Repository Integration Test

#### UserRepositoryTest.java
```java
class UserRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("根据用户名查找用户")
    void testFindByUsername() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encoded_password");
        user.setFullName("Test User");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        
        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByUsername("testuser");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("检查用户名是否存在")
    void testExistsByUsername() {
        // Given
        User user = new User();
        user.setUsername("existinguser");
        user.setEmail("existing@example.com");
        user.setPassword("encoded_password");
        user.setFullName("Existing User");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        
        userRepository.save(user);

        // When & Then
        assertThat(userRepository.existsByUsername("existinguser")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }

    @Test
    @DisplayName("检查邮箱是否存在")
    void testExistsByEmail() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("existing@example.com");
        user.setPassword("encoded_password");
        user.setFullName("Test User");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        
        userRepository.save(user);

        // When & Then
        assertThat(userRepository.existsByEmail("existing@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }
}
```

### Service层集成测试 / Service Integration Test

#### DataFileServiceIntegrationTest.java
```java
class DataFileServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DataFileService dataFileService;
    
    @Autowired
    private DataFileRepository dataFileRepository;
    
    @Autowired
    private OrganizationNodeRepository organizationNodeRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("创建数据文件集成测试")
    void testCreateDataFile() {
        // Given
        User user = createTestUser();
        OrganizationNode module = createTestModule();
        
        CreateDataFileRequest request = new CreateDataFileRequest();
        request.setName("测试数据文件");
        request.setDescription("测试用数据文件");
        request.setOrganizationNodeId(module.getId());
        request.setAccessLevel(DataFile.AccessLevel.PRIVATE);
        
        List<ColumnDefinition> columns = Arrays.asList(
            createColumnDefinition("id", "INTEGER", true, 1),
            createColumnDefinition("name", "STRING", true, 2)
        );
        request.setColumnDefinitions(columns);
        
        List<Map<String, Object>> dataRows = Arrays.asList(
            Map.of("id", 1, "name", "测试数据1"),
            Map.of("id", 2, "name", "测试数据2")
        );
        request.setDataRows(dataRows);

        // When
        DataFileDto result = dataFileService.createDataFile(request, user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("测试数据文件");
        assertThat(result.getRowCount()).isEqualTo(2);
        assertThat(result.getColumnCount()).isEqualTo(2);
        assertThat(result.getOwnerId()).isEqualTo(user.getId());
        
        // 验证数据库中的数据
        Optional<DataFile> saved = dataFileRepository.findById(result.getId());
        assertThat(saved).isPresent();
        assertThat(saved.get().getName()).isEqualTo("测试数据文件");
    }

    private User createTestUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encoded_password");
        user.setFullName("Test User");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private OrganizationNode createTestModule() {
        OrganizationNode module = new OrganizationNode();
        module.setName("测试模块");
        module.setDescription("测试用模块");
        module.setType(OrganizationNodeType.MODULE);
        module.setSortOrder(1);
        return organizationNodeRepository.save(module);
    }

    private ColumnDefinition createColumnDefinition(String name, String dataType, boolean required, int sortOrder) {
        ColumnDefinition column = new ColumnDefinition();
        column.setName(name);
        column.setDataType(ColumnDataType.valueOf(dataType));
        column.setRequired(required);
        column.setSortOrder(sortOrder);
        return column;
    }
}
```

## API测试 / API Testing

### API测试脚本 / API Test Scripts

项目提供了完整的API测试脚本，位于`api-tests/`目录：

#### 认证API测试 / Authentication API Tests
```bash
#!/bin/bash
# test-auth-apis.sh

echo "🔐 测试认证API"
echo "================"

BASE_URL="http://localhost:8080/api"
TOKEN=""

# 1. 用户注册
echo "1. 测试用户注册..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "testuser@example.com",
    "password": "password123",
    "confirmPassword": "password123",
    "fullName": "Test User"
  }')

echo "注册响应: $REGISTER_RESPONSE"

# 2. 用户登录
echo "2. 测试用户登录..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }')

echo "登录响应: $LOGIN_RESPONSE"

# 提取Token
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken')
echo "获取到Token: $TOKEN"

# 3. Token验证
echo "3. 测试Token验证..."
curl -s -X GET "$BASE_URL/auth/validate" \
  -H "Authorization: Bearer $TOKEN"

echo "✅ 认证API测试完成"
```

#### 组织管理API测试 / Organization API Tests
```bash
#!/bin/bash
# test-organization-apis.sh

echo "🏢 测试组织管理API"
echo "=================="

BASE_URL="http://localhost:8080/api"
TOKEN="YOUR_TOKEN_HERE"

# 1. 获取组织树
echo "1. 测试获取组织树..."
curl -s -X GET "$BASE_URL/organization/tree" \
  -H "Authorization: Bearer $TOKEN" | jq '.'

# 2. 创建组织节点
echo "2. 测试创建组织节点..."
CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/organization/nodes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "测试部门",
    "description": "测试用部门",
    "type": "DEPARTMENT",
    "parentId": null,
    "sortOrder": 1
  }')

echo "创建响应: $CREATE_RESPONSE"

# 3. 获取节点详情
NODE_ID=$(echo $CREATE_RESPONSE | jq -r '.id')
echo "3. 测试获取节点详情 (ID: $NODE_ID)..."
curl -s -X GET "$BASE_URL/organization/nodes/$NODE_ID" \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo "✅ 组织管理API测试完成"
```

#### 数据文件API测试 / Data File API Tests
```bash
#!/bin/bash
# test-datafile-apis.sh

echo "📁 测试数据文件API"
echo "=================="

BASE_URL="http://localhost:8080/api"
TOKEN="YOUR_TOKEN_HERE"

# 1. 创建数据文件
echo "1. 测试创建数据文件..."
CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/data-files" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "测试数据表",
    "description": "测试用数据表",
    "organizationNodeId": 1,
    "accessLevel": "PRIVATE",
    "columnDefinitions": [
      {
        "name": "id",
        "dataType": "INTEGER",
        "required": true,
        "sortOrder": 1
      },
      {
        "name": "name",
        "dataType": "STRING",
        "required": true,
        "maxLength": 50,
        "sortOrder": 2
      }
    ],
    "dataRows": [
      {"id": 1, "name": "测试数据1"},
      {"id": 2, "name": "测试数据2"}
    ]
  }')

echo "创建响应: $CREATE_RESPONSE"

# 2. 查询数据文件
echo "2. 测试查询数据文件..."
curl -s -X POST "$BASE_URL/data-files/query" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "page": 1,
    "size": 10,
    "sortBy": "createdAt",
    "sortDirection": "desc"
  }' | jq '.'

# 3. 获取统计信息
echo "3. 测试获取统计信息..."
curl -s -X GET "$BASE_URL/data-files/statistics" \
  -H "Authorization: Bearer $TOKEN" | jq '.'

echo "✅ 数据文件API测试完成"
```

### Postman测试集合 / Postman Test Collection

项目包含完整的Postman测试集合：

#### 导入测试集合
1. 导入 `postman-collection.json`
2. 导入 `postman-environment.json`
3. 选择 "Static Data Platform - Local Environment" 环境

#### 测试场景覆盖
- ✅ 用户注册和登录
- ✅ Token验证和刷新
- ✅ 组织节点CRUD操作
- ✅ 数据文件CRUD操作
- ✅ 错误处理和边界情况
- ✅ 权限验证

## 代码覆盖率 / Code Coverage

### JaCoCo配置 / JaCoCo Configuration

#### Maven插件配置
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <!-- 为单元测试准备代理 -->
        <execution>
            <id>prepare-agent</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        
        <!-- 为集成测试准备代理 -->
        <execution>
            <id>prepare-agent-integration</id>
            <phase>pre-integration-test</phase>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
            <configuration>
                <destFile>${project.build.directory}/jacoco-it.exec</destFile>
                <propertyName>jacoco.agent.it</propertyName>
            </configuration>
        </execution>
        
        <!-- 生成单元测试报告 -->
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        
        <!-- 生成集成测试报告 -->
        <execution>
            <id>report-integration</id>
            <phase>post-integration-test</phase>
            <goals>
                <goal>report</goal>
            </goals>
            <configuration>
                <dataFile>${project.build.directory}/jacoco-it.exec</dataFile>
                <outputDirectory>${project.build.directory}/site/jacoco-it</outputDirectory>
            </configuration>
        </execution>
        
        <!-- 验证覆盖率阈值 -->
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>INSTRUCTION</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.60</minimum>
                            </limit>
                            <limit>
                                <counter>BRANCH</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.50</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 生成覆盖率报告 / Generate Coverage Reports

#### 方法1: 使用Maven命令
```bash
# 运行测试并生成覆盖率报告
mvn clean test jacoco:report

# 运行集成测试并生成报告
mvn clean verify

# 验证覆盖率阈值
mvn jacoco:check
```

#### 方法2: 使用测试脚本
```bash
# 运行所有测试并生成覆盖率报告
./ut-scripts/run-all-tests.sh

# 生成详细的覆盖率报告
./ut-scripts/generate-coverage-report.sh

# 快速测试JaCoCo配置
./ut-scripts/test-jacoco.sh
```

#### 方法3: 使用API覆盖率测试
```bash
# 运行API覆盖率测试
./api-tests/api-coverage-test.sh

# 快速API覆盖率测试
./api-tests/quick-api-coverage.sh
```

### 查看覆盖率报告 / View Coverage Reports

#### 报告位置
- **HTML报告**: `target/site/jacoco/index.html`
- **CSV报告**: `target/site/jacoco/jacoco.csv`
- **XML报告**: `target/site/jacoco/jacoco.xml`
- **ut-scripts报告**: `ut-scripts/coverage-reports/jacoco/index.html`

#### 在浏览器中查看
```bash
# macOS
open target/site/jacoco/index.html

# Linux
xdg-open target/site/jacoco/index.html

# Windows
start target/site/jacoco/index.html
```

### 当前覆盖率状态 / Current Coverage Status

根据最新测试结果：

- **指令覆盖率**: 69.0%
- **分支覆盖率**: 83.6%
- **行覆盖率**: 68.5%
- **方法覆盖率**: 85.2%
- **类覆盖率**: 90.0%

### 覆盖率改进建议 / Coverage Improvement Suggestions

#### 低覆盖率包
1. **com.staticdata.platform.enums**: 0.0%
   - 建议：为枚举类添加测试用例

2. **com.staticdata.platform.dto**: 0.0%
   - 建议：为DTO类添加序列化/反序列化测试

3. **com.staticdata.platform.config**: 0.0%
   - 建议：为配置类添加配置验证测试

4. **com.staticdata.platform.entity**: 0.0%
   - 建议：为实体类添加JPA映射测试

#### 改进策略
1. **增加单元测试**: 为未覆盖的类添加测试用例
2. **边界测试**: 添加边界条件和异常情况测试
3. **集成测试**: 增加更多集成测试场景
4. **API测试**: 完善API端到端测试

## 测试脚本 / Test Scripts

### 主要测试脚本 / Main Test Scripts

#### 1. run-all-tests.sh - 运行所有测试
```bash
#!/bin/bash
echo "🧪 运行所有测试"
echo "==============="

# 清理并编译
mvn clean compile

# 运行单元测试
echo "运行单元测试..."
mvn test

# 运行集成测试
echo "运行集成测试..."
mvn verify

# 生成覆盖率报告
echo "生成覆盖率报告..."
mvn jacoco:report

echo "✅ 所有测试完成"
echo "覆盖率报告: target/site/jacoco/index.html"
```

#### 2. generate-coverage-report.sh - 生成覆盖率报告
```bash
#!/bin/bash
echo "📊 生成覆盖率报告"
echo "================="

# 创建报告目录
mkdir -p ut-scripts/coverage-reports

# 运行测试
mvn clean test jacoco:report

# 复制报告到ut-scripts目录
cp -r target/site/jacoco ut-scripts/coverage-reports/

# 创建时间戳备份
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
cp -r ut-scripts/coverage-reports/jacoco ut-scripts/coverage-reports/jacoco_$TIMESTAMP

echo "✅ 覆盖率报告生成完成"
echo "报告位置: ut-scripts/coverage-reports/jacoco/index.html"
```

#### 3. test-jacoco.sh - 测试JaCoCo配置
```bash
#!/bin/bash
echo "🔍 测试JaCoCo配置"
echo "================="

# 运行简单测试
mvn clean test

# 检查报告是否生成
if [ -f "target/site/jacoco/index.html" ]; then
    echo "✅ JaCoCo报告生成成功"
    echo "报告位置: target/site/jacoco/index.html"
else
    echo "❌ JaCoCo报告生成失败"
    exit 1
fi

# 检查覆盖率阈值
mvn jacoco:check
if [ $? -eq 0 ]; then
    echo "✅ 覆盖率阈值检查通过"
else
    echo "⚠️ 覆盖率阈值检查失败"
fi
```

### API测试脚本 / API Test Scripts

#### 1. api-coverage-test.sh - API覆盖率测试
```bash
#!/bin/bash
echo "🚀 API接口测试覆盖率分析"
echo "=========================="

# 启动应用并收集覆盖率
echo "1. 启动应用并收集覆盖率数据..."
java -javaagent:jacoco-agent.jar \
     -Dspring.profiles.active=test \
     -jar target/platform-0.0.1-SNAPSHOT.jar &
APP_PID=$!

# 等待应用启动
sleep 30

# 运行API测试
echo "2. 运行API接口测试..."
cd api-tests
chmod +x *.sh

# 按顺序运行测试
./test-auth-apis.sh
./test-organization-apis.sh  
./test-final-datafile.sh

# 停止应用
echo "3. 停止应用..."
kill $APP_PID

# 生成覆盖率报告
echo "4. 生成覆盖率报告..."
cd ..
mvn jacoco:report

echo "✅ API覆盖率测试完成！"
echo "报告位置: target/site/jacoco/index.html"
```

#### 2. quick-api-coverage.sh - 快速API覆盖率测试
```bash
#!/bin/bash
echo "⚡ 快速API覆盖率测试"
echo "==================="

# 运行基础API测试
./api-tests/test-auth-apis.sh
./api-tests/test-organization-apis.sh

# 生成简单报告
mvn jacoco:report

echo "✅ 快速API覆盖率测试完成！"
```

## 测试数据管理 / Test Data Management

### 测试数据准备 / Test Data Preparation

#### 基础测试数据 / Base Test Data
```java
@Component
public class TestDataFactory {

    public User createTestUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encoded_password");
        user.setFullName("Test User");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        return user;
    }

    public OrganizationNode createTestDepartment() {
        OrganizationNode department = new OrganizationNode();
        department.setName("测试部门");
        department.setDescription("测试用部门");
        department.setType(OrganizationNodeType.DEPARTMENT);
        department.setSortOrder(1);
        return department;
    }

    public DataFile createTestDataFile() {
        DataFile dataFile = new DataFile();
        dataFile.setName("测试数据文件");
        dataFile.setDescription("测试用数据文件");
        dataFile.setAccessLevel(DataFile.AccessLevel.PRIVATE);
        dataFile.setRowCount(2);
        dataFile.setColumnCount(2);
        return dataFile;
    }
}
```

#### 测试数据清理 / Test Data Cleanup
```java
@Transactional
@Rollback
public class BaseTestWithCleanup {

    @Autowired
    protected UserRepository userRepository;
    
    @Autowired
    protected OrganizationNodeRepository organizationNodeRepository;
    
    @Autowired
    protected DataFileRepository dataFileRepository;

    @AfterEach
    void cleanup() {
        // 清理测试数据
        dataFileRepository.deleteAll();
        organizationNodeRepository.deleteAll();
        userRepository.deleteAll();
    }
}
```

### 测试数据库配置 / Test Database Configuration

#### Testcontainers配置
```java
@Testcontainers
public class DatabaseTestBase {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("test-data.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

## 持续集成 / Continuous Integration

### GitHub Actions配置 / GitHub Actions Configuration

#### .github/workflows/test.yml
```yaml
name: Test and Coverage

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
    
    - name: Run unit tests
      run: mvn clean test
    
    - name: Run integration tests
      run: mvn verify
    
    - name: Generate coverage report
      run: mvn jacoco:report
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: target/site/jacoco/jacoco.xml
        flags: unittests
        name: codecov-umbrella
    
    - name: Upload coverage reports
      uses: actions/upload-artifact@v3
      with:
        name: coverage-reports
        path: target/site/jacoco/
```

### Jenkins配置 / Jenkins Configuration

#### Jenkinsfile
```groovy
pipeline {
    agent any
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Unit Tests') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Integration Tests') {
            steps {
                sh 'mvn verify'
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/failsafe-reports/*.xml'
                }
            }
        }
        
        stage('Coverage Report') {
            steps {
                sh 'mvn jacoco:report'
            }
            post {
                always {
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: 'JaCoCo Coverage Report'
                    ])
                }
            }
        }
    }
}
```

## 故障排除 / Troubleshooting

### 常见问题 / Common Issues

#### 1. 测试数据库连接失败 / Test Database Connection Failed
**症状**: `Connection refused` 或 `Database not found`

**解决方案**:
```bash
# 检查Docker是否运行
docker ps

# 检查Testcontainers配置
mvn test -Dtest.containers.enabled=true

# 检查数据库初始化脚本
ls -la src/test/resources/
```

#### 2. 覆盖率报告未生成 / Coverage Report Not Generated
**症状**: `target/site/jacoco/index.html` 不存在

**解决方案**:
```bash
# 确保测试成功运行
mvn clean test

# 检查JaCoCo插件配置
mvn help:effective-pom | grep jacoco

# 手动生成报告
mvn jacoco:report
```

#### 3. 内存不足错误 / Out of Memory Error
**症状**: `OutOfMemoryError: Java heap space`

**解决方案**:
```bash
# 增加Maven内存
export MAVEN_OPTS="-Xmx2g -XX:MaxPermSize=512m"

# 或者修改pom.xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>-Xmx2g</argLine>
    </configuration>
</plugin>
```

#### 4. 测试超时 / Test Timeout
**症状**: 测试运行时间过长或超时

**解决方案**:
```java
// 增加测试超时时间
@Test
@Timeout(value = 30, unit = TimeUnit.SECONDS)
void testLongRunningOperation() {
    // 测试代码
}

// 或者配置全局超时
@TestPropertySource(properties = {
    "spring.test.database.replace=none",
    "spring.datasource.url=jdbc:h2:mem:testdb"
})
```

### 调试技巧 / Debugging Tips

#### 1. 启用详细日志 / Enable Verbose Logging
```yaml
# application-test.yml
logging:
  level:
    com.staticdata.platform: DEBUG
    org.springframework.test: DEBUG
    org.testcontainers: INFO
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

#### 2. 使用测试监听器 / Use Test Listeners
```java
@ExtendWith(TestExecutionListener.class)
class DebugTest {
    
    @BeforeEach
    void setUp() {
        System.out.println("开始测试: " + getClass().getSimpleName());
    }
    
    @AfterEach
    void tearDown() {
        System.out.println("测试完成: " + getClass().getSimpleName());
    }
}
```

#### 3. 检查测试环境 / Check Test Environment
```java
@Test
void testEnvironment() {
    // 检查Spring上下文
    assertThat(applicationContext).isNotNull();
    
    // 检查数据库连接
    assertThat(dataSource).isNotNull();
    
    // 检查测试配置
    assertThat(environment.getActiveProfiles()).contains("test");
}
```

## 总结 / Summary

本指南提供了静态数据平台完整的测试解决方案，包括：

### ✅ 测试覆盖
- **单元测试**: 使用JUnit 5和Mockito进行Service和Controller层测试
- **集成测试**: 使用Testcontainers进行Repository和Service集成测试
- **API测试**: 完整的API端到端测试脚本和Postman集合
- **代码覆盖率**: 使用JaCoCo进行全面的代码覆盖率分析

### ✅ 测试工具
- **JUnit 5**: 现代单元测试框架
- **Testcontainers**: 容器化集成测试
- **JaCoCo**: 代码覆盖率分析
- **Mockito**: Mock框架
- **Spring Boot Test**: Spring测试支持

### ✅ 自动化支持
- **测试脚本**: 完整的测试自动化脚本
- **CI/CD集成**: GitHub Actions和Jenkins配置
- **覆盖率报告**: 自动生成和发布覆盖率报告
- **测试数据管理**: 测试数据准备和清理

### ✅ 质量保证
- **覆盖率目标**: 指令覆盖率≥60%，分支覆盖率≥50%
- **测试策略**: 测试金字塔策略，确保全面的测试覆盖
- **持续集成**: 自动化测试和覆盖率检查
- **故障排除**: 详细的调试和问题解决指南

通过本指南，您可以：
1. 快速设置完整的测试环境
2. 编写高质量的单元测试和集成测试
3. 进行全面的API测试
4. 生成和分析代码覆盖率报告
5. 集成到CI/CD流程中
6. 解决常见的测试问题

---

**最后更新**: 2024-01-01  
**版本**: 1.0.0  
**状态**: 生产就绪 🚀
