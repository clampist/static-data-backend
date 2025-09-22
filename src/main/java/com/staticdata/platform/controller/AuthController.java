package com.staticdata.platform.controller;

import com.staticdata.platform.dto.LoginRequest;
import com.staticdata.platform.dto.LoginResponse;
import com.staticdata.platform.dto.RegisterRequest;
import com.staticdata.platform.dto.UserDto;
import com.staticdata.platform.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、注册、token管理等认证相关请求
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "认证管理", description = "用户认证相关API")
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户使用用户名/邮箱和密码进行登录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "登录成功", 
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "认证失败"),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public ResponseEntity<LoginResponse> login(
            @Parameter(description = "登录请求信息", required = true)
            @Valid @RequestBody LoginRequest loginRequest) {
        
        log.info("Login attempt for user: {}", loginRequest.getUsername());
        
        try {
            LoginResponse loginResponse = authService.login(loginRequest);
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            log.error("Login failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            throw e;
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册账号")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "注册成功", 
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误或用户名/邮箱已存在"),
        @ApiResponse(responseCode = "409", description = "用户名或邮箱冲突")
    })
    public ResponseEntity<UserDto> register(
            @Parameter(description = "注册请求信息", required = true)
            @Valid @RequestBody RegisterRequest registerRequest) {
        
        log.info("Registration attempt for user: {}", registerRequest.getUsername());
        
        // 验证密码确认
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("密码和确认密码不匹配");
        }
        
        try {
            UserDto userDto = authService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
        } catch (Exception e) {
            log.error("Registration failed for user {}: {}", registerRequest.getUsername(), e.getMessage());
            throw e;
        }
    }

    /**
     * 刷新token
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新token", description = "使用当前token获取新的token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "刷新成功", 
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "token无效或已过期")
    })
    public ResponseEntity<LoginResponse> refreshToken(HttpServletRequest request) {
        String token = parseJwtFromRequest(request);
        
        if (token == null) {
            throw new IllegalArgumentException("请求头中缺少Authorization token");
        }
        
        try {
            LoginResponse loginResponse = authService.refreshToken(token);
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 验证token
     */
    @GetMapping("/validate")
    @Operation(summary = "验证token", description = "验证当前token是否有效")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "验证结果"),
        @ApiResponse(responseCode = "401", description = "token无效")
    })
    public ResponseEntity<Map<String, Object>> validateToken(HttpServletRequest request) {
        String token = parseJwtFromRequest(request);
        
        Map<String, Object> response = new HashMap<>();
        
        if (token == null) {
            response.put("valid", false);
            response.put("message", "请求头中缺少Authorization token");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            boolean isValid = authService.validateToken(token);
            response.put("valid", isValid);
            
            if (isValid) {
                UserDto user = authService.getUserFromToken(token);
                response.put("user", user);
                response.put("message", "token有效");
            } else {
                response.put("message", "token无效或已过期");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            response.put("valid", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "根据token获取当前登录用户的详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功", 
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "401", description = "未认证或token无效")
    })
    public ResponseEntity<UserDto> getCurrentUser(HttpServletRequest request) {
        String token = parseJwtFromRequest(request);
        
        if (token == null) {
            throw new IllegalArgumentException("请求头中缺少Authorization token");
        }
        
        try {
            UserDto user = authService.getUserFromToken(token);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Get current user failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 检查用户名可用性
     */
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名可用性", description = "检查指定用户名是否可用")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "检查结果"),
        @ApiResponse(responseCode = "400", description = "参数错误")
    })
    public ResponseEntity<Map<String, Object>> checkUsername(
            @Parameter(description = "要检查的用户名", required = true)
            @RequestParam String username) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (!StringUtils.hasText(username)) {
            response.put("available", false);
            response.put("message", "用户名不能为空");
            return ResponseEntity.badRequest().body(response);
        }
        
        boolean available = authService.isUsernameAvailable(username);
        response.put("available", available);
        response.put("message", available ? "用户名可用" : "用户名已存在");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 检查邮箱可用性
     */
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱可用性", description = "检查指定邮箱是否可用")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "检查结果"),
        @ApiResponse(responseCode = "400", description = "参数错误")
    })
    public ResponseEntity<Map<String, Object>> checkEmail(
            @Parameter(description = "要检查的邮箱", required = true)
            @RequestParam String email) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (!StringUtils.hasText(email)) {
            response.put("available", false);
            response.put("message", "邮箱不能为空");
            return ResponseEntity.badRequest().body(response);
        }
        
        boolean available = authService.isEmailAvailable(email);
        response.put("available", available);
        response.put("message", available ? "邮箱可用" : "邮箱已存在");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 从HTTP请求中解析JWT token
     */
    private String parseJwtFromRequest(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        
        return null;
    }
}