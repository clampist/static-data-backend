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
 * Custom user details service implementation
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
                    return new UsernameNotFoundException("UserDoes not exist: " + usernameOrEmail);
                });

        if (!user.getEnabled()) {
            log.warn("User account is disabled: {}", usernameOrEmail);
            throw new UsernameNotFoundException("User account has been disabled: " + usernameOrEmail);
        }

        log.debug("User loaded successfully: {}", user.getUsername());
        return UserPrincipal.create(user);
    }

    /**
     * Find and load user details by user ID
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        log.debug("Loading user by id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new UsernameNotFoundException("User does not exist, ID: " + id);
                });

        if (!user.getEnabled()) {
            log.warn("User account is disabled, id: {}", id);
            throw new UsernameNotFoundException("User account has been disabled, ID: " + id);
        }

        log.debug("User loaded successfully by id: {}", user.getUsername());
        return UserPrincipal.create(user);
    }
}