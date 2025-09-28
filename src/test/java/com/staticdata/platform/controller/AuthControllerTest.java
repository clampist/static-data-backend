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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 认证控制器集成测试 使用@SpringBootTest替代@WebMvcTest，避免Spring Security配置问题
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class AuthControllerTest {

  @Autowired
  private WebApplicationContext webApplicationContext;

  @MockBean
  private AuthService authService;

  @Autowired
  private ObjectMapper objectMapper;

  private MockMvc mockMvc;

  private LoginRequest loginRequest;
  private RegisterRequest registerRequest;
  private LoginResponse loginResponse;
  private UserDto userDto;

  @BeforeEach
  void setUp() {
    // 设置MockMvc
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

    // 设置登录请求
    loginRequest = new LoginRequest().setUsername("testuser").setPassword("password");

    // 设置注册请求
    registerRequest =
        new RegisterRequest("newuser", "new@example.com", "password", "password", "New User");

    // 设置用户DTO
    userDto = UserDto.builder().id(1L).username("testuser").email("test@example.com")
        .fullName("Test User").role(UserRole.USER).enabled(true).createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now()).build();

    // 设置登录响应
    loginResponse = new LoginResponse("jwt-token", 86400000L, userDto);
  }

  @Test
  void login_WithValidCredentials_ShouldReturnLoginResponse() throws Exception {
    // Given
    when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

    // When & Then
    mockMvc
        .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").value("jwt-token"))
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.expiresIn").value(86400000L))
        .andExpect(jsonPath("$.user.username").value("testuser"));

    verify(authService).login(any(LoginRequest.class));
  }

  @Test
  void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
    // Given
    when(authService.login(any(LoginRequest.class)))
        .thenThrow(new RuntimeException("Invalid credentials"));

    // When & Then
    mockMvc
        .perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized());

    verify(authService).login(any(LoginRequest.class));
  }

  @Test
  void register_WithValidRequest_ShouldReturnUserDto() throws Exception {
    // Given
    when(authService.register(any(RegisterRequest.class))).thenReturn(userDto);

    // When & Then
    mockMvc
        .perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.role").value("USER")).andExpect(jsonPath("$.enabled").value(true));

    verify(authService).register(any(RegisterRequest.class));
  }

  @Test
  void register_WithExistingUsername_ShouldReturnBadRequest() throws Exception {
    // Given
    when(authService.register(any(RegisterRequest.class)))
        .thenThrow(new IllegalArgumentException("用户名已存在"));

    // When & Then
    mockMvc
        .perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isBadRequest());

    verify(authService).register(any(RegisterRequest.class));
  }

  @Test
  void register_WithMismatchedPasswords_ShouldReturnBadRequest() throws Exception {
    // Given
    registerRequest.setConfirmPassword("different");

    // When & Then
    mockMvc
        .perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void refreshToken_WithValidToken_ShouldReturnNewLoginResponse() throws Exception {
    // Given
    String token = "valid-token";
    when(authService.refreshToken(token)).thenReturn(loginResponse);

    // When & Then
    mockMvc.perform(post("/auth/refresh").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").value("jwt-token"))
        .andExpect(jsonPath("$.tokenType").value("Bearer"));

    verify(authService).refreshToken(token);
  }

  @Test
  void refreshToken_WithoutAuthorizationHeader_ShouldReturnBadRequest() throws Exception {
    // When & Then
    mockMvc.perform(post("/auth/refresh")).andExpect(status().isBadRequest());
  }

  @Test
  void validateToken_WithValidToken_ShouldReturnValidationResult() throws Exception {
    // Given
    String token = "valid-token";
    Map<String, Object> validationResult = new HashMap<>();
    validationResult.put("valid", true);
    validationResult.put("username", "testuser");

    when(authService.validateToken(token)).thenReturn(true);

    // When & Then
    mockMvc.perform(get("/auth/validate").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk()).andExpect(jsonPath("$.valid").value(true));

    verify(authService).validateToken(token);
  }

  @Test
  void checkUsername_WithAvailableUsername_ShouldReturnAvailable() throws Exception {
    // Given
    when(authService.isUsernameAvailable("availableuser")).thenReturn(true);

    // When & Then
    mockMvc.perform(get("/auth/check-username").param("username", "availableuser"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(true));

    verify(authService).isUsernameAvailable("availableuser");
  }

  @Test
  void checkUsername_WithTakenUsername_ShouldReturnNotAvailable() throws Exception {
    // Given
    when(authService.isUsernameAvailable("takenuser")).thenReturn(false);

    // When & Then
    mockMvc.perform(get("/auth/check-username").param("username", "takenuser"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false));

    verify(authService).isUsernameAvailable("takenuser");
  }

  @Test
  void checkEmail_WithAvailableEmail_ShouldReturnAvailable() throws Exception {
    // Given
    when(authService.isEmailAvailable("available@example.com")).thenReturn(true);

    // When & Then
    mockMvc.perform(get("/auth/check-email").param("email", "available@example.com"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(true));

    verify(authService).isEmailAvailable("available@example.com");
  }

  @Test
  void checkEmail_WithTakenEmail_ShouldReturnNotAvailable() throws Exception {
    // Given
    when(authService.isEmailAvailable("taken@example.com")).thenReturn(false);

    // When & Then
    mockMvc.perform(get("/auth/check-email").param("email", "taken@example.com"))
        .andExpect(status().isOk()).andExpect(jsonPath("$.available").value(false));

    verify(authService).isEmailAvailable("taken@example.com");
  }
}
