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
 * Authentication Controller Handles user login, registration, token management and other
 * authentication-related requests
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication Management", description = "User authentication related APIs")
public class AuthController {

    private final AuthService authService;

    /**
     * User login
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "User login with username/email and password")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Login successful",
                            content = @Content(
                                    schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Authentication failed"),
                    @ApiResponse(responseCode = "400", description = "Invalid request parameters")})
    public ResponseEntity<LoginResponse> login(@Parameter(description = "Login request information",
            required = true) @Valid @RequestBody LoginRequest loginRequest) {

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
     * User registration
     */
    @PostMapping("/register")
    @Operation(summary = "User registration", description = "New user account registration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registration successful",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request parameters or username/email already exists"),
            @ApiResponse(responseCode = "409", description = "Username or email conflict")})
    public ResponseEntity<UserDto> register(
            @Parameter(description = "Registration request information",
                    required = true) @Valid @RequestBody RegisterRequest registerRequest) {

        log.info("Registration attempt for user: {}", registerRequest.getUsername());

        // Validate password confirmation
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match");
        }

        try {
            UserDto userDto = authService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
        } catch (Exception e) {
            log.error("Registration failed for user {}: {}", registerRequest.getUsername(),
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Refresh token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new token using current token")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Refresh successful",
                            content = @Content(
                                    schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Token invalid or expired")})
    public ResponseEntity<LoginResponse> refreshToken(HttpServletRequest request) {
        String token = parseJwtFromRequest(request);

        if (token == null) {
            throw new IllegalArgumentException("Authorization token missing in request header");
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
     * Validate token
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate token", description = "Validate if current token is valid")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "ValidateResult"),
            @ApiResponse(responseCode = "401", description = "Token invalid")})
    public ResponseEntity<Map<String, Object>> validateToken(HttpServletRequest request) {
        String token = parseJwtFromRequest(request);

        Map<String, Object> response = new HashMap<>();

        if (token == null) {
            response.put("valid", false);
            response.put("message", "Authorization token missing in request header");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            boolean isValid = authService.validateToken(token);
            response.put("valid", isValid);

            if (isValid) {
                UserDto user = authService.getUserFromToken(token);
                response.put("user", user);
                response.put("message", "Token is valid");
            } else {
                response.put("message", "Token invalid or expired");
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
     * Get current user information
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user information",
            description = "Get detailed information of current logged-in user based on token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthenticated or token invalid")})
    public ResponseEntity<UserDto> getCurrentUser(HttpServletRequest request) {
        String token = parseJwtFromRequest(request);

        if (token == null) {
            throw new IllegalArgumentException("Authorization token missing in request header");
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
     * Check username availability
     */
    @GetMapping("/check-username")
    @Operation(summary = "Check username availability",
            description = "Check if specified username is available")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Check result"),
            @ApiResponse(responseCode = "400", description = "Parameter error")})
    public ResponseEntity<Map<String, Object>> checkUsername(@Parameter(
            description = "Username to check", required = true) @RequestParam String username) {

        Map<String, Object> response = new HashMap<>();

        if (!StringUtils.hasText(username)) {
            response.put("available", false);
            response.put("message", "Username cannot be empty");
            return ResponseEntity.badRequest().body(response);
        }

        boolean available = authService.isUsernameAvailable(username);
        response.put("available", available);
        response.put("message", available ? "Username is available" : "Username already exists");

        return ResponseEntity.ok(response);
    }

    /**
     * Check email availability
     */
    @GetMapping("/check-email")
    @Operation(summary = "Check email availability",
            description = "Check if specified email is available")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Check result"),
            @ApiResponse(responseCode = "400", description = "Parameter error")})
    public ResponseEntity<Map<String, Object>> checkEmail(@Parameter(description = "Email to check",
            required = true) @RequestParam String email) {

        Map<String, Object> response = new HashMap<>();

        if (!StringUtils.hasText(email)) {
            response.put("available", false);
            response.put("message", "Email cannot be empty");
            return ResponseEntity.badRequest().body(response);
        }

        boolean available = authService.isEmailAvailable(email);
        response.put("available", available);
        response.put("message", available ? "Email is available" : "Email already exists");

        return ResponseEntity.ok(response);
    }

    /**
     * Parse JWT token from HTTP request
     */
    private String parseJwtFromRequest(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
