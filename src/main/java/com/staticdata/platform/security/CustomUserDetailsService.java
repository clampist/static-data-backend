package com.staticdata.platform.security;

import com.staticdata.platform.entity.User;
import com.staticdata.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 自定义用户详情服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("Loading user by username or email: {}", usernameOrEmail);
        
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> {
                    log.warn("User not found with username or email: {}", usernameOrEmail);
                    return new UsernameNotFoundException("用户不存在: " + usernameOrEmail);
                });

        if (!user.getEnabled()) {
            log.warn("User account is disabled: {}", usernameOrEmail);
            throw new UsernameNotFoundException("用户账户已被禁用: " + usernameOrEmail);
        }

        log.debug("User loaded successfully: {}", user.getUsername());
        return UserPrincipal.create(user);
    }

    /**
     * 根据用户ID加载用户详情
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        log.debug("Loading user by id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new UsernameNotFoundException("用户不存在，ID: " + id);
                });

        if (!user.getEnabled()) {
            log.warn("User account is disabled, id: {}", id);
            throw new UsernameNotFoundException("用户账户已被禁用，ID: " + id);
        }

        log.debug("User loaded successfully by id: {}", user.getUsername());
        return UserPrincipal.create(user);
    }
}