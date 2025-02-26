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

@Component
@Slf4j
public class JwtServiceImpl implements JwtService {

    private final SecretKey secretKey;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    public JwtServiceImpl(@Value("${jwt.secret}") String secretKey, UserRepository userRepository,
                          TaskRepository taskRepository) {
        // Generate a SecretKey instance from the provided string
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public String generateToken(String email, List<String> roles) {
        return Jwts.builder().subject(email).claim("roles", roles).issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .signWith(secretKey, Jwts.SIG.HS512).compact(); // HS512 is an algorithm for signing tokens
    }

    @Override
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            return extractClaim(token, Claims::getExpiration).before(new Date());
        } catch (ExpiredJwtException e) {
            return true; // Token is expired
        }
    }

    @Override
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        List<?> roles = claims.get("roles", List.class);
        return roles.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    @Override
    public String extractTokenFromHeader(String authHeader) {
        String token = authHeader.substring(7);
        if (token.startsWith("{\"token\":\"") && token.endsWith("\"}")) {
            return token.substring(10, token.length() - 2);
        }
        return token;
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    @Override
    public Long extractUserId(String token) {
        String email = extractEmail(token);
        return userRepository.findByEmail(email).map(User::getUserId).orElseThrow(() ->
                new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    public boolean isTaskAuthor(Long userId, Long taskId) {
        return taskRepository.existsByTaskIdAndAuthorId(taskId, userId);
    }

    @Override
    public boolean isTaskAssignee(Long userId, Long taskId) {
        return taskRepository.existsByTaskIdAndAssigneeId(taskId, userId);
    }
}