package com.staticdata.platform.config;

import com.staticdata.platform.security.CustomUserDetailsService;
import com.staticdata.platform.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security Configuration Class Configures security filter chain, authentication providers
 * and CORS settings
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configure security filter chain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (not needed when using JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configure session management as stateless
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure authentication provider
                .authenticationProvider(authenticationProvider())

                // Configure authorization rules
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints - no authentication required
                        .requestMatchers("/auth/**").permitAll().requestMatchers("/swagger-ui/**")
                        .permitAll().requestMatchers("/api-docs/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/info").permitAll()

                        // Admin endpoints
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // User management endpoints
                        .requestMatchers(HttpMethod.GET, "/users/profile")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/users/profile")
                        .hasAnyRole("USER", "ADMIN").requestMatchers("/users/**").hasRole("ADMIN")

                        // Organization endpoints
                        .requestMatchers(HttpMethod.GET, "/organizations/**")
                        .hasAnyRole("USER", "ADMIN").requestMatchers("/organizations/**")
                        .hasRole("ADMIN")

                        // Data file endpoints
                        .requestMatchers("/data-files/**").hasAnyRole("USER", "ADMIN")

                        // All other endpoints require authentication
                        .anyRequest().authenticated())

                // Add JWT filter
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configure authentication provider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Configure password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configure CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed origins
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*",
                "https://localhost:*", "http://127.0.0.1:*", "https://127.0.0.1:*"));

        // Allowed HTTP methods
        configuration.setAllowedMethods(
                Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept",
                "Authorization", "Access-Control-Request-Method", "Access-Control-Request-Headers",
                "X-Requested-With"));

        // Allow credentials
        configuration.setAllowCredentials(true);

        // Preflight request cache time
        configuration.setMaxAge(3600L);

        // Exposed headers
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply to all paths
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
