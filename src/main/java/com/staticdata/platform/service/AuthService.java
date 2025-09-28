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
 * 认证服务类 处理用户登录、注册、token管理等认证相关业务逻辑
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
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应包含token和用户信息
     */
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("User login attempt: {}", loginRequest.getUsername());

        try {
            // 验证用户凭据
            Authentication authentication =
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(), loginRequest.getPassword()));

            // 设置安全上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 获取用户主体
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            // 生成JWT token
            String jwt = jwtUtils.generateJwtToken(userPrincipal.getUsername());

            // 更新用户最后登录时间
            updateLastLoginTime(userPrincipal.getId());

            // 构建用户DTO
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
     * 用户注册
     * 
     * @param registerRequest 注册请求
     * @return 用户DTO
     */
    @Transactional
    public UserDto register(RegisterRequest registerRequest) {
        log.info("User registration attempt: {}", registerRequest.getUsername());

        // 验证密码和确认密码是否匹配
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("密码和确认密码不匹配");
        }

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("用户名已存在: " + registerRequest.getUsername());
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("邮箱已存在: " + registerRequest.getEmail());
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setRole(UserRole.USER); // 默认角色为USER
        user.setEnabled(true);
        user.setCreatedBy("SYSTEM");
        user.setUpdatedBy("SYSTEM");

        // 保存用户
        User savedUser = userRepository.save(user);

        log.info("User registration successful: {}", savedUser.getUsername());

        // 转换为DTO并返回
        return UserDto.builder().id(savedUser.getId()).username(savedUser.getUsername())
                .email(savedUser.getEmail()).fullName(savedUser.getFullName())
                .role(savedUser.getRole()).enabled(savedUser.getEnabled())
                .createdAt(savedUser.getCreatedAt()).updatedAt(savedUser.getUpdatedAt()).build();
    }

    /**
     * 刷新JWT token
     * 
     * @param token 当前token
     * @return 新的LoginResponse
     */
    public LoginResponse refreshToken(String token) {
        log.info("Token refresh attempt");

        // 验证当前token
        if (!jwtUtils.validateJwtToken(token)) {
            throw new IllegalArgumentException("无效的token");
        }

        // 从token获取用户名
        String username = jwtUtils.getUsernameFromJwtToken(token);

        // 查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + username));

        // 生成新token
        String newJwt = jwtUtils.generateJwtToken(username);

        // 构建用户DTO
        UserDto userDto = UserDto.builder().id(user.getId()).username(user.getUsername())
                .email(user.getEmail()).fullName(user.getFullName()).role(user.getRole())
                .enabled(user.getEnabled()).build();

        log.info("Token refresh successful for user: {}", username);

        return new LoginResponse(newJwt, jwtUtils.getJwtExpirationMs(), userDto);
    }

    /**
     * 验证token有效性
     * 
     * @param token JWT token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        return jwtUtils.validateJwtToken(token);
    }

    /**
     * 从token获取用户信息
     * 
     * @param token JWT token
     * @return 用户DTO
     */
    public UserDto getUserFromToken(String token) {
        if (!jwtUtils.validateJwtToken(token)) {
            throw new IllegalArgumentException("无效的token");
        }

        String username = jwtUtils.getUsernameFromJwtToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在: " + username));

        return UserDto.builder().id(user.getId()).username(user.getUsername())
                .email(user.getEmail()).fullName(user.getFullName()).role(user.getRole())
                .enabled(user.getEnabled()).createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt()).build();
    }

    /**
     * 更新用户最后登录时间
     * 
     * @param userId 用户ID
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
     * 检查用户名是否可用
     * 
     * @param username 用户名
     * @return 是否可用
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * 检查邮箱是否可用
     * 
     * @param email 邮箱
     * @return 是否可用
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
}
