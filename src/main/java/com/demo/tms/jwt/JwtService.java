package com.demo.tms.jwt;

import java.util.List;

public interface JwtService {

    String generateToken(String email, List<String> roles);

    String extractEmail(String token);

    boolean isTokenExpired(String token);

    List<String> extractRoles(String token);

    String extractTokenFromHeader(String authHeader);

    Long extractUserId(String token);

    boolean isTaskAuthor(Long userId, Long taskId);

    boolean isTaskAssignee(Long userId, Long taskId);
}