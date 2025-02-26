package com.demo.tms.jwt;

import com.demo.tms.repository.TaskRepository;
import com.demo.tms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class JwtServiceImplTest {

    private static final String SECRET_KEY = "jwtSecretSuperSecureKeyThatIsAtLeast64CharactersLongForHS512Algorithm";
    private static final long JWT_EXPIRATION_IN_MS = 60000; // 1 minute expiration for testing

    private JwtServiceImpl jwtService;

    @MockitoBean
    private TaskRepository taskRepository;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl(SECRET_KEY, userRepository, taskRepository);

        // Manually set the expiration time since @Value is not injected in tests
        setJwtExpiration(jwtService, JWT_EXPIRATION_IN_MS);
    }

    // Use reflection to set the jwtExpirationInMs field manually
    private void setJwtExpiration(JwtServiceImpl jwtService, long expiration) {
        try {
            Field field = JwtServiceImpl.class.getDeclaredField("jwtExpirationInMs");
            field.setAccessible(true);
            field.set(jwtService, expiration);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set jwtExpirationInMs in test", e);
        }
    }

    @Test
    void testGenerateToken() {
        String token = jwtService.generateToken("testUser", Collections.singletonList("ROLE_TEST"));

        assertNotNull(token);
        assertEquals("testUser", jwtService.extractEmail(token));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void testExtractUsername() {
        String token = jwtService.generateToken("testUser", Collections.singletonList("ROLE_TEST"));
        assertEquals("testUser", jwtService.extractEmail(token));
    }

    @Test
    void testIsTokenExpired_ShouldReturnFalseForValidToken() {
        String token = jwtService.generateToken("testUser", Collections.singletonList("ROLE_TEST"));
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void testIsTokenExpired_ShouldReturnTrueForExpiredToken() throws InterruptedException {
        setJwtExpiration(jwtService, 1); // Set expiration to 1 ms
        String token = jwtService.generateToken("testUser", Collections.singletonList("ROLE_TEST"));

        Thread.sleep(10); // Wait to ensure expiration
        assertTrue(jwtService.isTokenExpired(token));
    }

    @Test
    void testExtractRoles() {
        String token = jwtService.generateToken("testUser", List.of("ROLE_USER", "ROLE_ADMIN"));

        List<String> roles = jwtService.extractRoles(token);
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ROLE_USER"));
        assertTrue(roles.contains("ROLE_ADMIN"));
    }

    @Test
    void testExtractTokenFromHeader_ValidToken() {
        String token = jwtService.generateToken("testUser", Collections.singletonList("ROLE_TEST"));
        String header = "Bearer " + token;

        assertEquals(token, jwtService.extractTokenFromHeader(header));
    }

    @Test
    void testExtractTokenFromHeader_JsonFormat() {
        String token = jwtService.generateToken("testUser", Collections.singletonList("ROLE_TEST"));
        String header = "Bearer {\"token\":\"" + token + "\"}";

        assertEquals(token, jwtService.extractTokenFromHeader(header));
    }
}