package com.demo.tms.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testDoFilterInternal_WithValidToken() throws ServletException, IOException {
        // Given
        String token = "validToken";
        String email = "testUser@test.com";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractTokenFromHeader("Bearer " + token)).thenReturn(token);
        when(jwtService.extractEmail(token)).thenReturn(email);
        when(jwtService.isTokenExpired(token)).thenReturn(false);
        when(jwtService.extractRoles(token)).thenReturn(Collections.singletonList("ROLE_USER"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void testDoFilterInternal_WithExpiredToken() throws ServletException, IOException {
        // Given
        String token = "expiredToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.extractTokenFromHeader("Bearer " + token)).thenReturn(token);
        when(jwtService.extractEmail(token)).thenReturn("testUser@test.com");
        when(jwtService.isTokenExpired(token)).thenReturn(true);

        // Mocking the PrintWriter to be returned by response.getWriter()
        PrintWriter printWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(printWriter);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response, times(2)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(printWriter, times(2)).write("Token has expired or is invalid");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_WithMissingAuthorizationHeader() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // Mocking the PrintWriter to be returned by response.getWriter()
        PrintWriter printWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(printWriter);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(0)).doFilter(request, response);
        verify(jwtService, never()).extractEmail(anyString());
        verify(response,times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(printWriter, times(1)).write(anyString());
    }

    @Test
    void testDoFilterInternal_WithInvalidAuthorizationHeader() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(0)).doFilter(request, response);
        verify(jwtService, never()).extractEmail(anyString());
    }

    @Test
    void testDoFilterInternal_WithExceptionHandling() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidToken");
        when(jwtService.extractTokenFromHeader(anyString())).thenThrow(new RuntimeException("Parsing error"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response, times(1)).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void testShouldNotFilter_WithExcludedEndpoints() {
        // Test for /auth/login
        when(request.getRequestURI()).thenReturn("/auth/login");
        assertTrue(jwtAuthenticationFilter.shouldNotFilter(request));

        // Test for /v3/api-docs/swagger-config
        when(request.getRequestURI()).thenReturn("/v3/api-docs/swagger-config");
        assertTrue(jwtAuthenticationFilter.shouldNotFilter(request));

        // Test for /swagger-ui/index.html
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");
        assertTrue(jwtAuthenticationFilter.shouldNotFilter(request));

        // Test for root path
        when(request.getRequestURI()).thenReturn("/");
        assertTrue(jwtAuthenticationFilter.shouldNotFilter(request));

        // Test for null URI path (expected false)
        when(request.getRequestURI()).thenReturn(null);
        assertFalse(jwtAuthenticationFilter.shouldNotFilter(request));
    }
}