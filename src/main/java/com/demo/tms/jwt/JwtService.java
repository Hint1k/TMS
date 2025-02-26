package com.demo.tms.jwt;

import java.util.List;

/**
 * {@code JwtService} is an interface that defines the contract for services handling JWT (JSON Web Token)
 * operations such as generating tokens, extracting claims, and validating token-related operations.
 * <p>
 * This interface provides methods for:
 * <ul>
 *     <li>Generating a JWT token for a user with roles.</li>
 *     <li>Extracting the email associated with the token.</li>
 *     <li>Checking if the token is expired.</li>
 *     <li>Extracting the roles embedded within the token.</li>
 *     <li>Extracting a token from the Authorization header in an HTTP request.</li>
 *     <li>Extracting the user ID from the token.</li>
 *     <li>Checking if a user is the author or assignee of a specific task.</li>
 * </ul>
 * </p>
 */
public interface JwtService {

    /**
     * Generates a JWT token for the given email and roles.
     *
     * @param email The user's email.
     * @param roles The list of roles assigned to the user.
     * @return The generated JWT token.
     */
    String generateToken(String email, List<String> roles);

    /**
     * Extracts the email address from the provided JWT token.
     *
     * @param token The JWT token.
     * @return The email associated with the token.
     */
    String extractEmail(String token);

    /**
     * Checks if the provided JWT token has expired.
     *
     * @param token The JWT token.
     * @return {@code true} if the token has expired; {@code false} otherwise.
     */
    boolean isTokenExpired(String token);

    /**
     * Extracts the roles from the provided JWT token.
     *
     * @param token The JWT token.
     * @return A list of roles extracted from the token.
     */
    List<String> extractRoles(String token);

    /**
     * Extracts the JWT token from the Authorization header.
     *
     * @param authHeader The Authorization header.
     * @return The extracted JWT token.
     */
    String extractTokenFromHeader(String authHeader);

    /**
     * Extracts the user ID from the provided JWT token.
     *
     * @param token The JWT token.
     * @return The user ID associated with the email in the token.
     */
    Long extractUserId(String token);

    /**
     * Checks if the user is the author of the specified task.
     *
     * @param userId The ID of the user.
     * @param taskId The ID of the task.
     * @return {@code true} if the user is the author of the task; {@code false} otherwise.
     */
    boolean isTaskAuthor(Long userId, Long taskId);

    /**
     * Checks if the user is the assignee of the specified task.
     *
     * @param userId The ID of the user.
     * @param taskId The ID of the task.
     * @return {@code true} if the user is the assignee of the task; {@code false} otherwise.
     */
    boolean isTaskAssignee(Long userId, Long taskId);
}