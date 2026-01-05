package com.smartspend.unit.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.smartspend.auth.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtServiceTest {

    private JwtService jwtService;
    private final String testSecret = "dGhpcyBpcyBhIHRlc3Qgc2VjcmV0IGtleSBmb3IgSldUIHRva2VuIGdlbmVyYXRpb24gYW5kIHZhbGlkYXRpb24="; // Base64 encoded test secret

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(testSecret);
    }

    @Test
    void shouldGenerateValidJwtToken() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        
        // When
        String token = jwtService.generateToken(userId, email);
        
        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains(".")); // JWT structure: header.payload.signature
        assertEquals(3, token.split("\\.").length); // Should have 3 parts
    }

    @Test
    void shouldExtractEmailFromValidToken() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String token = jwtService.generateToken(userId, email);
        
        // When
        String extractedEmail = jwtService.extractEmail(token);
        
        // Then
        assertEquals(email, extractedEmail);
    }

    @Test
    void shouldExtractUserIdFromToken() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String token = jwtService.generateToken(userId, email);
        
        // When - Parse token manually to verify userId
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(testSecret)))
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        Long extractedUserId = claims.get("userId", Long.class);
        
        // Then
        assertEquals(userId, extractedUserId);
    }

    @Test
    void shouldIncludeExpirationDateInToken() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        long beforeGeneration = System.currentTimeMillis();
        
        // When
        String token = jwtService.generateToken(userId, email);
        long afterGeneration = System.currentTimeMillis();
        
        // Then - Parse token to verify expiration
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(testSecret)))
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        Date expiration = claims.getExpiration();
        Date issuedAt = claims.getIssuedAt();
        
        assertNotNull(expiration);
        assertNotNull(issuedAt);
        assertTrue(expiration.after(issuedAt));
        
        // Should expire in approximately 15 minutes (900000ms)
        long expectedExpirationTime = beforeGeneration + 900000; // 15 minutes
        long actualExpirationTime = expiration.getTime();
        
        // Allow 1 second tolerance
        assertTrue(Math.abs(actualExpirationTime - expectedExpirationTime) < 1000);
    }

    @Test
    void shouldSignTokenWithCorrectAlgorithm() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String token = jwtService.generateToken(userId, email);
        
        // When - Parse token header to verify algorithm
        String[] tokenParts = token.split("\\.");
        String header = new String(java.util.Base64.getUrlDecoder().decode(tokenParts[0]));
        
        // Then - Should use HS256 algorithm
        assertTrue(header.contains("\"alg\":\"HS256\""), 
            "Expected HS256 algorithm in header: " + header);
    }

    @Test
    void shouldThrowExceptionForInvalidToken() {
        // Given - Invalid token
        String invalidToken = "invalid.jwt.token";
        
        // Then - Should throw exception
        assertThrows(Exception.class, () -> {
            jwtService.extractEmail(invalidToken);
        });
    }

    @Test
    void shouldThrowExceptionForExpiredToken() {
        // Given - Create expired token by manipulating expiration time
        Long userId = 1L;
        String email = "test@example.com";
        
        // Create token with past expiration
        long pastTime = System.currentTimeMillis() - 1000000; // 1000 seconds ago
        String expiredToken = Jwts.builder()
            .setSubject(email)
            .setIssuedAt(new Date(pastTime - 900000)) // Issued 15 minutes before pastTime
            .setExpiration(new Date(pastTime)) // Expired
            .addClaims(java.util.Map.of("userId", userId))
            .signWith(Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(testSecret)), 
                     io.jsonwebtoken.SignatureAlgorithm.HS256)
            .compact();
        
        // Then - Should throw exception for expired token
        assertThrows(Exception.class, () -> {
            jwtService.extractEmail(expiredToken);
        });
    }

    @Test
    void shouldThrowExceptionForTamperedToken() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String validToken = jwtService.generateToken(userId, email);
        
        // When - Tamper with token by modifying signature
        String[] parts = validToken.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".tampered_signature";
        
        // Then - Should throw exception for tampered token
        assertThrows(Exception.class, () -> {
            jwtService.extractEmail(tamperedToken);
        });
    }

    @Test
    void shouldGenerateUniqueTokensForDifferentUsers() {
        // Given
        Long userId1 = 1L;
        Long userId2 = 2L;
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";
        
        // When
        String token1 = jwtService.generateToken(userId1, email1);
        String token2 = jwtService.generateToken(userId2, email2);
        
        // Then
        assertNotEquals(token1, token2);
        
        String extractedEmail1 = jwtService.extractEmail(token1);
        String extractedEmail2 = jwtService.extractEmail(token2);
        
        assertEquals(email1, extractedEmail1);
        assertEquals(email2, extractedEmail2);
    }

    @Test
    void shouldHandleSpecialCharactersInEmail() {
        // Given
        Long userId = 1L;
        String emailWithSpecialChars = "test+user@example-domain.com";
        
        // When
        String token = jwtService.generateToken(userId, emailWithSpecialChars);
        String extractedEmail = jwtService.extractEmail(token);
        
        // Then
        assertEquals(emailWithSpecialChars, extractedEmail);
    }
}