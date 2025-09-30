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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Authentication Service Class handles user login, registration, token management and other
 * authentication-related business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    /**
     * UserLogin
     * 
     * @param loginRequest LoginRequest
     * @return Login response containing token and user information
     */
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("User login attempt: {}", loginRequest.getUsername());

        try {
            // Validate user credentials
            Authentication authentication =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(), loginRequest.getPassword()));

            // Set security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user principal
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            // GenerateJWT token
            String jwt = jwtUtils.generateJwtToken(userPrincipal.getUsername());

            // Update user last login time
            updateLastLoginTime(userPrincipal.getId());

            // BuildUserDTO
            UserDto userDto = UserDto.builder().id(userPrincipal.getId())
                    .username(userPrincipal.getUsername()).email(userPrincipal.getEmail())
                    .fullName(userPrincipal.getFullName()).role(userPrincipal.getRole())
                    .enabled(userPrincipal.isEnabled()).build();

            log.info("User login successful: {}", userPrincipal.getUsername());

            return new LoginResponse(jwt, jwtUtils.getJwtExpirationMs(), userDto);
        } catch (Exception e) {
            log.error("Login failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            throw new RuntimeException("Invalid credentials");
        }
    }

    /**
     * UserRegister
     * 
     * @param registerRequest RegisterRequest
     * @return UserDTO
     */
    @Transactional
    public UserDto register(RegisterRequest registerRequest) {
        log.info("User registration attempt: {}", registerRequest.getUsername());

        // Validate password and confirm password match
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match");
        }

        // CheckUsernameYesNoAlready exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException(
                    "UsernameAlready exists: " + registerRequest.getUsername());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException(
                    "Email already exists: " + registerRequest.getEmail());
        }

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setRole(UserRole.USER); // Default role is USER
        user.setEnabled(true);
        user.setCreatedBy("SYSTEM");
        user.setUpdatedBy("SYSTEM");

        // SaveUser
        User savedUser = userRepository.save(user);

        log.info("User registration successful: {}", savedUser.getUsername());

        // Convert to DTO and return
        return UserDto.builder().id(savedUser.getId()).username(savedUser.getUsername())
                .email(savedUser.getEmail()).fullName(savedUser.getFullName())
                .role(savedUser.getRole()).enabled(savedUser.getEnabled())
                .createdAt(savedUser.getCreatedAt()).updatedAt(savedUser.getUpdatedAt()).build();
    }

    /**
     * RefreshJWT token
     * 
     * @param token Currenttoken
     * @return NewLoginResponse
     */
    public LoginResponse refreshToken(String token) {
        log.info("Token refresh attempt");

        // ValidateCurrenttoken
        if (!jwtUtils.validateJwtToken(token)) {
            throw new IllegalArgumentException("Invalid token");
        }

        // Get username from token
        String username = jwtUtils.getUsernameFromJwtToken(token);

        // FindUser
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("UserDoes not exist: " + username));

        // Generate new token
        String newJwt = jwtUtils.generateJwtToken(username);

        // BuildUserDTO
        UserDto userDto = UserDto.builder().id(user.getId()).username(user.getUsername())
                .email(user.getEmail()).fullName(user.getFullName()).role(user.getRole())
                .enabled(user.getEnabled()).build();

        log.info("Token refresh successful for user: {}", username);

        return new LoginResponse(newJwt, jwtUtils.getJwtExpirationMs(), userDto);
    }

    /**
     * Validate token validity
     * 
     * @param token JWT token
     * @return YesNoValid
     */
    public boolean validateToken(String token) {
        return jwtUtils.validateJwtToken(token);
    }

    /**
     * Get user information from token
     * 
     * @param token JWT token
     * @return UserDTO
     */
    public UserDto getUserFromToken(String token) {
        if (!jwtUtils.validateJwtToken(token)) {
            throw new IllegalArgumentException("Invalid token");
        }

        String username = jwtUtils.getUsernameFromJwtToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("UserDoes not exist: " + username));

        return UserDto.builder().id(user.getId()).username(user.getUsername())
                .email(user.getEmail()).fullName(user.getFullName()).role(user.getRole())
                .enabled(user.getEnabled()).createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt()).build();
    }

    /**
     * Update user last login time
     * 
     * @param userId UserID
     */
    private void updateLastLoginTime(Long userId) {
        try {
            userRepository.findById(userId).ifPresent(user -> {
                user.setLastLoginAt(LocalDateTime.now());
                user.setUpdatedBy(user.getUsername());
                userRepository.save(user);
            });
        } catch (Exception e) {
            log.warn("Failed to update last login time for user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * CheckUsernameYesNoAvailable
     * 
     * @param username Username
     * @return YesNoAvailable
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * Check if email is available
     * 
     * @param email Email
     * @return YesNoAvailable
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}
