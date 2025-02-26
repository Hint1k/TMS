package com.demo.tms.config;

import com.demo.tms.jwt.JwtAuthenticationFilter;
import com.demo.tms.jwt.JwtService;
import com.demo.tms.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Autowired
    public SecurityConfig(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService(userRepository);
    }

    @Bean // forcing Spring Security to accept my custom UserDetailsService class
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService,
                                                               PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, DaoAuthenticationProvider authProvider) {
        try {
            log.debug("Configuring AuthenticationManager with Custom DaoAuthenticationProvider");

            AuthenticationManagerBuilder authenticationManagerBuilder =
                    http.getSharedObject(AuthenticationManagerBuilder.class);

            authenticationManagerBuilder.authenticationProvider(authProvider);

            log.debug("AuthenticationManager configured successfully");
            return authenticationManagerBuilder.build();
        } catch (Exception e) {
            log.error("Error in AuthenticationManager", e);
            throw new RuntimeException(e);
        }
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        try {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests((authorize) -> authorize
                            .requestMatchers("/auth/login","/v3/api-docs/**", "/swagger-ui/**",
                                    "/swagger-ui.html", "/").permitAll()
                            .requestMatchers("/api/users/**", "/api/roles/**", "/api/roles", "/api/users")
                                    .hasRole("ADMIN")
                            .requestMatchers(HttpMethod.GET, "/api/tasks", "/api/comments").hasRole("ADMIN")
                            .requestMatchers((request) ->
                                    isTaskOrCommentRelated(request.getRequestURI()))
                            .access((authentication, context) -> {
                                // Extract the path from the request
                                String path = context.getRequest().getRequestURI();
                                String method = context.getRequest().getMethod();

                                // Extract user ID from authentication
                                Long userId = jwtService.extractUserId(authentication.get().getName());

                                // Delegate the decision to a helper method
                                return authorizeTaskOrCommentAccess(userId, path, method);
                            })
                            .anyRequest().authenticated()
                    )
                    .addFilterBefore(new JwtAuthenticationFilter(jwtService),
                            UsernamePasswordAuthenticationFilter.class);

            return http.build();
        } catch (Exception e) {
            log.error("Error configuring security filter chain", e);
            throw new RuntimeException(e);
        }
    }

    private boolean isTaskOrCommentRelated(String path) {
        return path.matches("/tasks/\\d+/.*") || path.matches("/comments/task/\\d+/.*");
    }


    // Helper method to handle authorization decision
    private AuthorizationDecision authorizeTaskOrCommentAccess(Long userId, String path, String method) {
        Long taskId = extractTaskIdFromPath(path);

        if (taskId == null) {
            return new AuthorizationDecision(false); // No taskId found, deny access
        }

        // Check if the user is the author of the task
        if (jwtService.isTaskAuthor(userId, taskId)) {
            return new AuthorizationDecision(true); // Grant full access if the user is the author
        }

        // Check if the user is the assignee and apply method-based restrictions
        if (jwtService.isTaskAssignee(userId, taskId)) {
            if ("PATCH".equals(method) && path.matches("/tasks/" + taskId + "/status")) {
                return new AuthorizationDecision(true); // Allow updating task status
            }
            if ("POST".equals(method) && path.startsWith("/comments/task/" + taskId)) {
                return new AuthorizationDecision(true); // Allow creating comments on the task
            }
            return new AuthorizationDecision(false); // Deny other PATCH/PUT requests for assignees
        }

        // Deny delete access for tasks and comments
        if ("DELETE".equals(method) && (path.matches("/tasks/" + taskId) || path.startsWith("/comments/"))) {
            return new AuthorizationDecision(false);
        }

        // Default deny access
        return new AuthorizationDecision(false);
    }

    // helper method to extract from Path
    private Long extractTaskIdFromPath(String path) {
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ((parts[i].equals("tasks") || parts[i].equals("task")) && i + 1 < parts.length) {
                try {
                    return Long.valueOf(parts[i + 1]);
                } catch (NumberFormatException e) {
                    return null; // Invalid taskId, return null
                }
            }
        }
        return null;
    }
}