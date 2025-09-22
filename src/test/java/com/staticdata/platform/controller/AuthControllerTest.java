package com.staticdata.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.staticdata.platform.dto.LoginRequest;
import com.staticdata.platform.dto.LoginResponse;
import com.staticdata.platform.dto.RegisterRequest;
import com.staticdata.platform.dto.UserDto;
import com.staticdata.platform.enums.UserRole;
import com.staticdata.platform.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证控制器集成测试
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private LoginResponse loginResponse;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        // 设置登录请求
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        // 设置注册请求
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password");
        registerRequest.setConfirmPassword("password");
        registerRequest.setFullName("New User");

        // 设置用户DTO
        userDto = UserDto.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .fullName("Test User")
            .role(UserRole.USER)
            .enabled(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // 设置登录响应
        loginResponse = new LoginResponse("jwt-token", 86400000L, userDto);
    }

    @Test
    void login_WithValidCredentials_ShouldReturnLoginResponse() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accessToken").value("jwt-token"))
            .andExpect(jsonPath("$.expiresIn").value(86400000L))
            .andExpect(jsonPath("$.user.username").value("testuser"))
            .andExpect(jsonPath("$.user.email").value("test@example.com"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isInternalServerError());

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_WithInvalidInput_ShouldReturnBadRequest() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setUsername(""); // Invalid: empty username
        invalidRequest.setPassword("123"); // Invalid: too short password

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    void register_WithValidRequest_ShouldReturnUserDto() throws Exception {
        // Given
        when(authService.register(any(RegisterRequest.class))).thenReturn(userDto);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.role").value("USER"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void register_WithMismatchedPasswords_ShouldReturnBadRequest() throws Exception {
        // Given
        registerRequest.setConfirmPassword("differentPassword");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void register_WithExistingUsername_ShouldReturnBadRequest() throws Exception {
        // Given
        when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new IllegalArgumentException("用户名已存在: newuser"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isBadRequest());

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    @WithMockUser
    void refreshToken_WithValidToken_ShouldReturnNewLoginResponse() throws Exception {
        // Given
        when(authService.refreshToken(anyString())).thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf())
                .header("Authorization", "Bearer valid-token"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accessToken").value("jwt-token"));

        verify(authService).refreshToken("valid-token");
    }

    @Test
    @WithMockUser
    void refreshToken_WithoutAuthorizationHeader_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                .with(csrf()))
            .andExpect(status().isBadRequest());

        verify(authService, never()).refreshToken(anyString());
    }

    @Test
    @WithMockUser
    void validateToken_WithValidToken_ShouldReturnValidationResult() throws Exception {
        // Given
        when(authService.validateToken(anyString())).thenReturn(true);
        when(authService.getUserFromToken(anyString())).thenReturn(userDto);

        // When & Then
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer valid-token"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.valid").value(true))
            .andExpect(jsonPath("$.user.username").value("testuser"));

        verify(authService).validateToken("valid-token");
        verify(authService).getUserFromToken("valid-token");
    }

    @Test
    @WithMockUser
    void getCurrentUser_WithValidToken_ShouldReturnUserDto() throws Exception {
        // Given
        when(authService.getUserFromToken(anyString())).thenReturn(userDto);

        // When & Then
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer valid-token"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(authService).getUserFromToken("valid-token");
    }

    @Test
    void checkUsername_WithAvailableUsername_ShouldReturnAvailable() throws Exception {
        // Given
        when(authService.isUsernameAvailable("newuser")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/auth/check-username")
                .param("username", "newuser"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.available").value(true))
            .andExpect(jsonPath("$.message").value("用户名可用"));

        verify(authService).isUsernameAvailable("newuser");
    }

    @Test
    void checkUsername_WithTakenUsername_ShouldReturnNotAvailable() throws Exception {
        // Given
        when(authService.isUsernameAvailable("testuser")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/auth/check-username")
                .param("username", "testuser"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.available").value(false))
            .andExpect(jsonPath("$.message").value("用户名已存在"));

        verify(authService).isUsernameAvailable("testuser");
    }

    @Test
    void checkEmail_WithAvailableEmail_ShouldReturnAvailable() throws Exception {
        // Given
        when(authService.isEmailAvailable("new@example.com")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/auth/check-email")
                .param("email", "new@example.com"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.available").value(true))
            .andExpect(jsonPath("$.message").value("邮箱可用"));

        verify(authService).isEmailAvailable("new@example.com");
    }

    @Test
    void checkEmail_WithTakenEmail_ShouldReturnNotAvailable() throws Exception {
        // Given
        when(authService.isEmailAvailable("test@example.com")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/auth/check-email")
                .param("email", "test@example.com"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.available").value(false))
            .andExpect(jsonPath("$.message").value("邮箱已存在"));

        verify(authService).isEmailAvailable("test@example.com");
    }
}