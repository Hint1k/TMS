package com.demo.tms.jwt;

import com.demo.tms.entity.User;
import com.demo.tms.repository.TaskRepository;
import com.demo.tms.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@code JwtServiceImpl} is an implementation of the {@code JwtService} interface that provides methods for
 * generating and validating JWT tokens. This service interacts with user and task repositories to extract
 * user-specific information and validates token-related operations.
 * <p>
 * This service includes the following functionality:
 * <ul>
 *     <li>Generating a JWT token for a given user email and list of roles.</li>
 *     <li>Extracting the user email from the JWT token.</li>
 *     <li>Checking whether a token has expired.</li>
 *     <li>Extracting roles from the JWT token.</li>
 *     <li>Extracting a token from the Authorization header of an HTTP request.</li>
 *     <li>Validating if a user is the author or assignee of a specific task.</li>
 * </ul>
 * </p>
 * <p>
 * The class uses the HS512 signing algorithm to generate tokens and requires a {@link UserRepository} and
 * {@link TaskRepository} for user and task-related operations.
 * </p>
 */
@Component
@Slf4j
public class JwtServiceImpl implements JwtService {

    private final SecretKey secretKey;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    /**
     * Constructs a {@code JwtServiceImpl} instance using the provided secret key, user repository,
     * and task repository.
     *
     * @param secretKey The secret key used for signing JWT tokens.
     * @param userRepository The repository to fetch user data.
     * @param taskRepository The repository to fetch task data.
     */
    public JwtServiceImpl(@Value("${jwt.secret}") String secretKey, UserRepository userRepository,
                          TaskRepository taskRepository) {
        // Generate a SecretKey instance from the provided string
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    /**
     * Generates a JWT token for the given email and roles.
     * <p>
     * The generated token includes the user email, roles, and an expiration time.
     * </p>
     *
     * @param email The user's email.
     * @param roles The roles associated with the user.
     * @return The generated JWT token.
     */
    @Override
    public String generateToken(String email, List<String> roles) {
        return Jwts.builder().subject(email).claim("roles", roles).issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .signWith(secretKey, Jwts.SIG.HS512).compact(); // HS512 is an algorithm for signing tokens
    }

    /**
     * Extracts the email from the JWT token.
     *
     * @param token The JWT token.
     * @return The email extracted from the token.
     */
    @Override
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Checks if the JWT token has expired.
     *
     * @param token The JWT token.
     * @return {@code true} if the token is expired; {@code false} otherwise.
     */
    @Override
    public boolean isTokenExpired(String token) {
        try {
            return extractClaim(token, Claims::getExpiration).before(new Date());
        } catch (ExpiredJwtException e) {
            return true; // Token is expired
        }
    }

    /**
     * Extracts the roles from the JWT token.
     *
     * @param token The JWT token.
     * @return A list of roles extracted from the token.
     */
    @Override
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        List<?> roles = claims.get("roles", List.class);
        return roles.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    /**
     * Extracts the token from the Authorization header.
     *
     * @param authHeader The Authorization header.
     * @return The extracted JWT token.
     */
    @Override
    public String extractTokenFromHeader(String authHeader) {
        String token = authHeader.substring(7);
        if (token.startsWith("{\"token\":\"") && token.endsWith("\"}")) {
            return token.substring(10, token.length() - 2);
        }
        return token;
    }

    /**
     * Extracts a specific claim from the JWT token.
     *
     * @param token The JWT token.
     * @param claimsResolver A function to extract the desired claim.
     * @param <T> The type of the claim.
     * @return The value of the extracted claim.
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from the JWT token.
     *
     * @param token The JWT token.
     * @return The claims extracted from the token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    /**
     * Extracts the user ID from the JWT token.
     *
     * @param token The JWT token.
     * @return The user ID associated with the email in the token.
     * @throws UsernameNotFoundException If the user with the specified email is not found.
     */
    @Override
    public Long extractUserId(String token) {
        String email = extractEmail(token);
        return userRepository.findByEmail(email).map(User::getUserId).orElseThrow(() ->
                new UsernameNotFoundException("User not found with email: " + email));
    }

    /**
     * Checks if the user is the author of a given task.
     *
     * @param userId The ID of the user.
     * @param taskId The ID of the task.
     * @return {@code true} if the user is the author of the task; {@code false} otherwise.
     */
    @Override
    public boolean isTaskAuthor(Long userId, Long taskId) {
        return taskRepository.existsByTaskIdAndAuthorId(taskId, userId);
    }

    /**
     * Checks if the user is the assignee of a given task.
     *
     * @param userId The ID of the user.
     * @param taskId The ID of the task.
     * @return {@code true} if the user is the assignee of the task; {@code false} otherwise.
     */
    @Override
    public boolean isTaskAssignee(Long userId, Long taskId) {
        return taskRepository.existsByTaskIdAndAssigneeId(taskId, userId);
    }
}