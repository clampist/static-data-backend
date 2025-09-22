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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 认证服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

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
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        // 创建测试用户主体
        testUserPrincipal = UserPrincipal.create(testUser);

        // 创建登录请求
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        // 创建注册请求
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password");
        registerRequest.setConfirmPassword("password");
        registerRequest.setFullName("New User");
    }

    @Test
    void login_WithValidCredentials_ShouldReturnLoginResponse() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUserPrincipal);
        when(jwtUtils.generateJwtToken(anyString())).thenReturn("jwt-token");
        when(jwtUtils.getJwtExpirationMs()).thenReturn(86400000L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getAccessToken());
        assertEquals(86400000L, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals("testuser", response.getUser().getUsername());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtToken("testuser");
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
        assertEquals("testuser", result.getUsername()); // Note: using the saved user's data
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
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> authService.register(registerRequest));
        
        assertEquals("用户名已存在: newuser", exception.getMessage());
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> authService.register(registerRequest));
        
        assertEquals("邮箱已存在: new@example.com", exception.getMessage());
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository, never()).save(any(User.class));
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
        assertEquals(86400000L, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals("testuser", response.getUser().getUsername());

        verify(jwtUtils).validateJwtToken(oldToken);
        verify(jwtUtils).getUsernameFromJwtToken(oldToken);
        verify(jwtUtils).generateJwtToken("testuser");
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldThrowException() {
        // Given
        String invalidToken = "invalid-token";
        when(jwtUtils.validateJwtToken(invalidToken)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> authService.refreshToken(invalidToken));
        
        assertEquals("无效的token", exception.getMessage());
        verify(jwtUtils).validateJwtToken(invalidToken);
        verify(jwtUtils, never()).generateJwtToken(anyString());
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String validToken = "valid-token";
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);

        // When
        boolean result = authService.validateToken(validToken);

        // Then
        assertTrue(result);
        verify(jwtUtils).validateJwtToken(validToken);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid-token";
        when(jwtUtils.validateJwtToken(invalidToken)).thenReturn(false);

        // When
        boolean result = authService.validateToken(invalidToken);

        // Then
        assertFalse(result);
        verify(jwtUtils).validateJwtToken(invalidToken);
    }

    @Test
    void getUserFromToken_WithValidToken_ShouldReturnUserDto() {
        // Given
        String validToken = "valid-token";
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
        when(jwtUtils.getUsernameFromJwtToken(validToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDto result = authService.getUserFromToken(validToken);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(UserRole.USER, result.getRole());

        verify(jwtUtils).validateJwtToken(validToken);
        verify(jwtUtils).getUsernameFromJwtToken(validToken);
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
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void isUsernameAvailable_WithAvailableUsername_ShouldReturnTrue() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        // When
        boolean result = authService.isUsernameAvailable("newuser");

        // Then
        assertTrue(result);
        verify(userRepository).existsByUsername("newuser");
    }

    @Test
    void isUsernameAvailable_WithTakenUsername_ShouldReturnFalse() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When
        boolean result = authService.isUsernameAvailable("testuser");

        // Then
        assertFalse(result);
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    void isEmailAvailable_WithAvailableEmail_ShouldReturnTrue() {
        // Given
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        // When
        boolean result = authService.isEmailAvailable("new@example.com");

        // Then
        assertTrue(result);
        verify(userRepository).existsByEmail("new@example.com");
    }

    @Test
    void isEmailAvailable_WithTakenEmail_ShouldReturnFalse() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When
        boolean result = authService.isEmailAvailable("test@example.com");

        // Then
        assertFalse(result);
        verify(userRepository).existsByEmail("test@example.com");
    }
}