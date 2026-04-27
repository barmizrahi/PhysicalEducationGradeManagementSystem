package com.pe.grademanagement.config;

import com.pe.grademanagement.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security configuration for JWT-based authentication.
 * Configures HTTP security rules, JWT filter, and stateless session management.
 * 
 * Requirements:
 * - 13.1: Configure Spring Security with JWT authentication
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    
    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }
    
    /**
     * Configure HTTP security rules.
     * - Disable CSRF (using JWT tokens)
     * - Configure CORS
     * - Set stateless session management
     * - Configure authorization rules
     * - Add JWT filter to security chain
     * - Configure custom authentication entry point to return 401 instead of 403
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF since we're using JWT tokens
            .csrf(csrf -> csrf.disable())
            
            // Configure CORS using the existing CorsConfigurationSource
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Allow authentication endpoints without authentication
                .requestMatchers("/api/auth/**").permitAll()
                
                // Allow health check endpoints without authentication
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // Require authentication for all other API endpoints
                .requestMatchers("/api/**").authenticated()
                
                // Allow all other requests (for static resources, etc.)
                .anyRequest().permitAll()
            )
            
            // Configure stateless session management (no sessions, JWT only)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configure exception handling to return 401 for authentication failures
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" 
                        + authException.getMessage() + "\"}");
                })
            )
            
            // Add JWT authentication filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * Password encoder bean for BCrypt password hashing.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Authentication manager bean for authentication operations.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
