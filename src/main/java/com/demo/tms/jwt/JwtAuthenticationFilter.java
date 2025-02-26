package com.demo.tms.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                 @NonNull FilterChain filterChain) {
        String token = null;
        try {
            String authHeader = request.getHeader("Authorization");
            token = extractToken(authHeader);
            if (token != null) {
                String email = jwtService.extractEmail(token);
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    handleAuthentication(token, email, response);
                }
            }
            // Check if token is null or expired before proceeding with the filter chain
            if (token != null && !jwtService.isTokenExpired(token)) {
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token has expired or is invalid");
            }
        } catch (Exception e) {
            log.error("Error during authentication: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.info("Authorization Header: {}", authHeader);
            return jwtService.extractTokenFromHeader(authHeader);
        }
        return null;
    }

    private void handleAuthentication(String token, String email, HttpServletResponse response) {
        if (jwtService.isTokenExpired(token)) {
            // Token is expired, set status to 401 and send message
            try {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token has expired or is invalid");
            } catch (IOException e) {
                log.error("Error writing response: {}", e.getMessage(), e);
            }
            return; // Exit early without setting authentication
        }

        // If token is valid, extract roles and set authentication
        List<String> roles = jwtService.extractRoles(token);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null, roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Check for null or empty path to avoid NullPointerException
        if (path == null || path.isEmpty()) {
            return false; // or true, depending on your needs
        }

        // Skip filtering for certain paths
        return path.equals("/auth/login") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.equals("/swagger-ui.html") ||
                path.equals("/");
    }
}