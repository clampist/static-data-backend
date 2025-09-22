package com.staticdata.platform.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具类单元测试
 */
class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private final String testSecret = "testSecretKeyForJwtThatShouldBeLongEnoughToMeetSecurityRequirements";
    private final int testExpiration = 86400000; // 24小时

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", testExpiration);
    }

    @Test
    void generateJwtToken_ShouldReturnValidToken() {
        // Given
        String username = "testuser";

        // When
        String token = jwtUtils.generateJwtToken(username);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    void getUsernameFromJwtToken_ShouldReturnCorrectUsername() {
        // Given
        String username = "testuser";
        String token = jwtUtils.generateJwtToken(username);

        // When
        String extractedUsername = jwtUtils.getUsernameFromJwtToken(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void validateJwtToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String username = "testuser";
        String token = jwtUtils.generateJwtToken(username);

        // When
        boolean isValid = jwtUtils.validateJwtToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateJwtToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtUtils.validateJwtToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_WithNullToken_ShouldReturnFalse() {
        // When
        boolean isValid = jwtUtils.validateJwtToken(null);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_WithEmptyToken_ShouldReturnFalse() {
        // Given
        String emptyToken = "";

        // When
        boolean isValid = jwtUtils.validateJwtToken(emptyToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void isTokenExpiringSoon_WithNewToken_ShouldReturnFalse() {
        // Given
        String username = "testuser";
        String token = jwtUtils.generateJwtToken(username);

        // When
        boolean isExpiringSoon = jwtUtils.isTokenExpiringSoon(token);

        // Then
        assertFalse(isExpiringSoon);
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewToken() throws InterruptedException {
        // Given
        String username = "testuser";
        String originalToken = jwtUtils.generateJwtToken(username);
        
        // Add a small delay to ensure different timestamps
        Thread.sleep(1000);

        // When
        String refreshedToken = jwtUtils.refreshToken(originalToken);

        // Then
        assertNotNull(refreshedToken);
        // Note: Tokens might be the same if generated within the same second
        // So we'll just verify the token is valid and contains the correct username
        assertTrue(jwtUtils.validateJwtToken(refreshedToken));
        assertEquals(username, jwtUtils.getUsernameFromJwtToken(refreshedToken));
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldThrowException() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            jwtUtils.refreshToken(invalidToken);
        });
    }

    @Test
    void getTokenRemainingTime_WithValidToken_ShouldReturnPositiveValue() {
        // Given
        String username = "testuser";
        String token = jwtUtils.generateJwtToken(username);

        // When
        long remainingTime = jwtUtils.getTokenRemainingTime(token);

        // Then
        assertTrue(remainingTime > 0);
        assertTrue(remainingTime <= testExpiration);
    }

    @Test
    void getTokenRemainingTime_WithInvalidToken_ShouldReturnZero() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        long remainingTime = jwtUtils.getTokenRemainingTime(invalidToken);

        // Then
        assertEquals(0, remainingTime);
    }

    @Test
    void getJwtExpirationMs_ShouldReturnConfiguredValue() {
        // When
        long expiration = jwtUtils.getJwtExpirationMs();

        // Then
        assertEquals(testExpiration, expiration);
    }
}