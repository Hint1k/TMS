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

/**
 * {@code JwtAuthenticationFilter} is a Spring Security filter that processes incoming HTTP requests and
 * authenticates the user based on a JWT token in the Authorization header.
 * <p>
 * This filter is responsible for:
 * <ul>
 *     <li>Extracting the JWT token from the Authorization header.</li>
 *     <li>Verifying the validity and expiration of the token.</li>
 *     <li>Extracting the user's email and roles from the token.</li>
 *     <li>Setting the authentication context for the user if the token is valid.</li>
 * </ul>
 * If the token is invalid or expired, the filter responds with a {@code 401 Unauthorized} status and an error message.
 * </p>
 * <p>
 * The filter applies only to requests that are not excluded from filtering, such as the login endpoint or
 * Swagger documentation endpoints.
 * </p>
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    /**
     * Constructs a {@code JwtAuthenticationFilter} with the specified {@code JwtService} to handle JWT processing.
     *
     * @param jwtService The service responsible for handling JWT token extraction and validation.
     */
    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * The main method that filters incoming requests for JWT authentication.
     * <p>
     * It extracts the JWT token from the Authorization header, validates the token, and sets the authentication
     * context if the token is valid. If the token is expired or invalid, it returns a {@code 401 Unauthorized}
     * response.
     * </p>
     *
     * @param request The incoming HTTP request.
     * @param response The HTTP response.
     * @param filterChain The filter chain to pass the request and response along to the next filter.
     */
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

    /**
     * Extracts the JWT token from the Authorization header.
     *
     * @param authHeader The Authorization header value containing the token.
     * @return The JWT token or {@code null} if the header is invalid.
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.info("Authorization Header: {}", authHeader);
            return jwtService.extractTokenFromHeader(authHeader);
        }
        return null;
    }

    /**
     * Handles the authentication process by validating the JWT token and setting the user's authentication context.
     * If the token is expired, it responds with a {@code 401 Unauthorized} status.
     *
     * @param token The JWT token.
     * @param email The email extracted from the token.
     * @param response The HTTP response to write error messages if needed.
     */
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

    /**
     * Determines whether the request should be filtered.
     * <p>
     * Excludes paths like login and Swagger documentation from filtering.
     * </p>
     *
     * @param request The HTTP request.
     * @return {@code true} if the request should not be filtered; {@code false} otherwise.
     */
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