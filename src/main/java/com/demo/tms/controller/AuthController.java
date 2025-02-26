package com.demo.tms.controller;

import com.demo.tms.jwt.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * The {@code AuthController} class handles authentication-related requests.
 * It provides an endpoint for user login, where users can authenticate using their email and password.
 * Upon successful authentication, a JSON Web Token (JWT) is generated and returned to the client.
 * <p>
 * This controller uses Spring Security's {@link AuthenticationManager} to authenticate users
 * and the {@link JwtService} to generate JWT tokens.
 * </p>
 * <p>
 * If authentication fails due to invalid credentials, a 401 Unauthorized response is returned
 * with an error message. Other exceptions during authentication also result in a 401 response.
 * </p>
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Constructs a new {@code AuthController} with the specified dependencies.
     *
     * @param authenticationManager The {@link AuthenticationManager} used to authenticate users.
     * @param jwtService            The {@link JwtService} used to generate JWT tokens.
     */
    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Handles user login requests.
     * <p>
     * This method authenticates the user using the provided email and password. If authentication is successful,
     * a JWT token is generated and returned in the response.
     * The token includes the user's email and authorities (roles).
     * </p>
     * <p>
     * If the provided credentials are invalid, a 401 Unauthorized response is returned with an error message.
     * Other exceptions during authentication also result in a 401 response.
     * </p>
     *
     * @param email    The email address of the user attempting to log in.
     * @param password The password of the user attempting to log in.
     * @return A {@link ResponseEntity} containing the JWT token in JSON format if authentication is successful,
     *         or an error message if authentication fails.
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password) {
        try {
            // Authenticate the user with the provided credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // Set authentication context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate and return JWT token
            String token = jwtService.generateToken(email, authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));

            return ResponseEntity.ok("{\"token\":\"" + token + "\"}");
        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for email: {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\":\"Invalid email or password\"}");
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", email, e);
            // Return 401 Unauthorized if authentication fails
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}