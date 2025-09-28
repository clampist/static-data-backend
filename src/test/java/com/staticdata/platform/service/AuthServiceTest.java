package com.staticdata.platform.service;

import com.staticdata.platform.dto.LoginRequest;
import com.staticdata.platform.dto.LoginResponse;
import com.staticdata.platform.dto.RegisterRequest;
import com.staticdata.platform.dto.UserDto;
import com.staticdata.platform.entity.User;
import com.staticdata.platform.enums.UserRole;
import com.staticdata.platform.repository.UserRepository;
import com.staticdata.platform.security.UserPrincipal;
import com.staticdata.platform.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 认证服务集成测试 使用@SpringBootTest替代@ExtendWith(MockitoExtension.class)，简化mock配置
 */
@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

  @Autowired
  private AuthService authService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private PasswordEncoder passwordEncoder;

  @MockBean
  private JwtUtils jwtUtils;

  @MockBean
  private AuthenticationManager authenticationManager;

  private User testUser;
  private UserPrincipal testUserPrincipal;
  private LoginRequest loginRequest;
  private RegisterRequest registerRequest;

  @BeforeEach
  void setUp() {
    // 创建测试用户
    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setPassword("encodedPassword");
    testUser.setFullName("Test User");
    testUser.setRole(UserRole.USER);
    testUser.setEnabled(true);

    // 创建测试用户主体
    testUserPrincipal = UserPrincipal.create(testUser);

    // 设置登录请求
    loginRequest = new LoginRequest().setUsername("testuser").setPassword("password");

    // 设置注册请求
    registerRequest =
        new RegisterRequest("newuser", "new@example.com", "password", "password", "New User");
  }

  @Test
  void login_WithValidCredentials_ShouldReturnLoginResponse() {
    // Given
    Authentication authentication = mock(Authentication.class);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(testUserPrincipal);
    when(jwtUtils.generateJwtToken(anyString())).thenReturn("jwt-token");
    when(jwtUtils.getJwtExpirationMs()).thenReturn(86400000L);

    // When
    LoginResponse response = authService.login(loginRequest);

    // Then
    assertNotNull(response);
    assertEquals("jwt-token", response.getAccessToken());
    assertEquals("Bearer", response.getTokenType());
    assertEquals(86400000L, response.getExpiresIn());
    assertNotNull(response.getUser());
    assertEquals("testuser", response.getUser().getUsername());

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(jwtUtils).generateJwtToken("testuser");
  }

  @Test
  void login_WithInvalidCredentials_ShouldThrowException() {
    // Given
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new RuntimeException("Invalid credentials"));

    // When & Then
    assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  void register_WithValidRequest_ShouldReturnUserDto() {
    // Given
    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // When
    UserDto result = authService.register(registerRequest);

    // Then
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("test@example.com", result.getEmail());
    assertEquals(UserRole.USER, result.getRole());
    assertTrue(result.getEnabled());

    verify(userRepository).existsByUsername("newuser");
    verify(userRepository).existsByEmail("new@example.com");
    verify(passwordEncoder).encode("password");
    verify(userRepository).save(any(User.class));
  }

  @Test
  void register_WithExistingUsername_ShouldThrowException() {
    // Given
    when(userRepository.existsByUsername("newuser")).thenReturn(true);

    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.register(registerRequest));

    assertEquals("用户名已存在: newuser", exception.getMessage());
    verify(userRepository).existsByUsername("newuser");
  }

  @Test
  void register_WithExistingEmail_ShouldThrowException() {
    // Given
    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.register(registerRequest));

    assertEquals("邮箱已存在: new@example.com", exception.getMessage());
    verify(userRepository).existsByEmail("new@example.com");
  }

  @Test
  void register_WithMismatchedPasswords_ShouldThrowException() {
    // Given
    registerRequest.setConfirmPassword("different");

    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.register(registerRequest));

    assertEquals("密码和确认密码不匹配", exception.getMessage());
  }

  @Test
  void refreshToken_WithValidToken_ShouldReturnNewLoginResponse() {
    // Given
    String oldToken = "old-jwt-token";
    String newToken = "new-jwt-token";

    when(jwtUtils.validateJwtToken(oldToken)).thenReturn(true);
    when(jwtUtils.getUsernameFromJwtToken(oldToken)).thenReturn("testuser");
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    when(jwtUtils.generateJwtToken("testuser")).thenReturn(newToken);
    when(jwtUtils.getJwtExpirationMs()).thenReturn(86400000L);

    // When
    LoginResponse response = authService.refreshToken(oldToken);

    // Then
    assertNotNull(response);
    assertEquals(newToken, response.getAccessToken());
    assertEquals("Bearer", response.getTokenType());
    assertEquals(86400000L, response.getExpiresIn());
    assertNotNull(response.getUser());

    verify(jwtUtils).validateJwtToken(oldToken);
    verify(jwtUtils).getUsernameFromJwtToken(oldToken);
    verify(userRepository).findByUsername("testuser");
    verify(jwtUtils).generateJwtToken("testuser");
  }

  @Test
  void refreshToken_WithInvalidToken_ShouldThrowException() {
    // Given
    String invalidToken = "invalid-token";
    when(jwtUtils.validateJwtToken(invalidToken)).thenReturn(false);

    // When & Then
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> authService.refreshToken(invalidToken));

    assertEquals("无效的token", exception.getMessage());
    verify(jwtUtils).validateJwtToken(invalidToken);
  }

  @Test
  void validateToken_WithValidToken_ShouldReturnTrue() {
    // Given
    String token = "valid-token";
    when(jwtUtils.validateJwtToken(token)).thenReturn(true);

    // When
    boolean result = authService.validateToken(token);

    // Then
    assertTrue(result);
    verify(jwtUtils).validateJwtToken(token);
  }

  @Test
  void validateToken_WithInvalidToken_ShouldReturnFalse() {
    // Given
    String token = "invalid-token";
    when(jwtUtils.validateJwtToken(token)).thenReturn(false);

    // When
    boolean result = authService.validateToken(token);

    // Then
    assertFalse(result);
    verify(jwtUtils).validateJwtToken(token);
  }

  @Test
  void getUserFromToken_WithValidToken_ShouldReturnUserDto() {
    // Given
    String token = "valid-token";
    when(jwtUtils.validateJwtToken(token)).thenReturn(true);
    when(jwtUtils.getUsernameFromJwtToken(token)).thenReturn("testuser");
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

    // When
    UserDto result = authService.getUserFromToken(token);

    // Then
    assertNotNull(result);
    assertEquals("testuser", result.getUsername());
    assertEquals("test@example.com", result.getEmail());

    verify(jwtUtils).validateJwtToken(token);
    verify(jwtUtils).getUsernameFromJwtToken(token);
    verify(userRepository).findByUsername("testuser");
  }

  @Test
  void getUserFromToken_WithInvalidToken_ShouldThrowException() {
    // Given
    String invalidToken = "invalid-token";
    when(jwtUtils.validateJwtToken(invalidToken)).thenReturn(false);

    // When & Then
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> authService.getUserFromToken(invalidToken));

    assertEquals("无效的token", exception.getMessage());
    verify(jwtUtils).validateJwtToken(invalidToken);
  }

  @Test
  void isUsernameAvailable_WithAvailableUsername_ShouldReturnTrue() {
    // Given
    when(userRepository.existsByUsername("availableuser")).thenReturn(false);

    // When
    boolean result = authService.isUsernameAvailable("availableuser");

    // Then
    assertTrue(result);
    verify(userRepository).existsByUsername("availableuser");
  }

  @Test
  void isUsernameAvailable_WithTakenUsername_ShouldReturnFalse() {
    // Given
    when(userRepository.existsByUsername("takenuser")).thenReturn(true);

    // When
    boolean result = authService.isUsernameAvailable("takenuser");

    // Then
    assertFalse(result);
    verify(userRepository).existsByUsername("takenuser");
  }

  @Test
  void isEmailAvailable_WithAvailableEmail_ShouldReturnTrue() {
    // Given
    when(userRepository.existsByEmail("available@example.com")).thenReturn(false);

    // When
    boolean result = authService.isEmailAvailable("available@example.com");

    // Then
    assertTrue(result);
    verify(userRepository).existsByEmail("available@example.com");
  }

  @Test
  void isEmailAvailable_WithTakenEmail_ShouldReturnFalse() {
    // Given
    when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

    // When
    boolean result = authService.isEmailAvailable("taken@example.com");

    // Then
    assertFalse(result);
    verify(userRepository).existsByEmail("taken@example.com");
  }
}
