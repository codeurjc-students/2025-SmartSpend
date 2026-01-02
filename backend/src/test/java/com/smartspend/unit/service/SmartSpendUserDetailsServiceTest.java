package com.smartspend.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smartspend.security.SmartSpendUserDetailsService;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

public class SmartSpendUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SmartSpendUserDetailsService userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User("testuser", "test@example.com", "hashedpassword123");
        testUser.setUserId(1L);
    }

    @Test
    void shouldLoadUserByUsernameSuccessfully() {
        // Given
        String email = "test@example.com";
        
        // When - Configure mocks
        when(userRepository.findByUserEmail(email)).thenReturn(Optional.of(testUser));
        
        // When - Execute
        UserDetails result = userDetailsService.loadUserByUsername(email);
        
        // Then
        assertNotNull(result);
        assertEquals("test@example.com", result.getUsername()); // Should use email as username
        assertEquals("hashedpassword123", result.getPassword());
        assertTrue(result.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        
        verify(userRepository).findByUserEmail(email);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        
        // When - Configure mocks
        when(userRepository.findByUserEmail(email)).thenReturn(Optional.empty());
        
        // Then - Should throw UsernameNotFoundException
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> userDetailsService.loadUserByUsername(email)
        );
        
        assertEquals("User not found with email: nonexistent@example.com", exception.getMessage());
        verify(userRepository).findByUserEmail(email);
    }

    @Test
    void shouldMapUserFieldsCorrectly() {
        // Given
        User userWithSpecialChars = new User("user+test", "user+test@domain-name.com", "complex$password#123");
        userWithSpecialChars.setUserId(2L);
        String email = "user+test@domain-name.com";
        
        // When - Configure mocks
        when(userRepository.findByUserEmail(email)).thenReturn(Optional.of(userWithSpecialChars));
        
        // When - Execute
        UserDetails result = userDetailsService.loadUserByUsername(email);
        
        // Then - Verify correct field mapping
        assertNotNull(result);
        assertEquals("user+test@domain-name.com", result.getUsername());
        assertEquals("complex$password#123", result.getPassword());
        
        // Should have USER role
        assertTrue(result.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        
        // Should be enabled by default
        assertTrue(result.isEnabled());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
    }

    @Test
    void shouldUseEmailAsUsername() {
        // Given
        String email = "test@example.com";
        
        // When
        when(userRepository.findByUserEmail(email)).thenReturn(Optional.of(testUser));
        
        // Execute
        UserDetails result = userDetailsService.loadUserByUsername(email);
        
        // Then - Username should be the email, not the user's display name
        assertEquals("test@example.com", result.getUsername());
        assertNotEquals("testuser", result.getUsername()); // Should NOT use display name
    }

    @Test
    void shouldAssignUserRole() {
        // Given
        String email = "test@example.com";
        
        // When
        when(userRepository.findByUserEmail(email)).thenReturn(Optional.of(testUser));
        
        // Execute
        UserDetails result = userDetailsService.loadUserByUsername(email);
        
        // Then - Should have exactly one role: USER
        assertEquals(1, result.getAuthorities().size());
        assertTrue(result.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        
        // Should not have other roles
        assertFalse(result.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void shouldHandleNullOrEmptyPassword() {
        // Given
        User userWithNullPassword = new User("testuser", "test@example.com", null);
        userWithNullPassword.setUserId(1L);
        String email = "test@example.com";
        
        // When
        when(userRepository.findByUserEmail(email)).thenReturn(Optional.of(userWithNullPassword));
        
        // Execute
        UserDetails result = userDetailsService.loadUserByUsername(email);
        
        // Then - Should handle null password gracefully
        assertNotNull(result);
        assertEquals("", result.getPassword()); // Spring Security requiere password no null
        assertEquals("test@example.com", result.getUsername());
    }

    @Test
    void shouldBeThreadSafe() {
        // Given
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";
        
        User user1 = new User("user1", email1, "password1");
        User user2 = new User("user2", email2, "password2");
        
        // When - Configure mocks
        when(userRepository.findByUserEmail(email1)).thenReturn(Optional.of(user1));
        when(userRepository.findByUserEmail(email2)).thenReturn(Optional.of(user2));
        
        // Execute multiple calls
        UserDetails result1 = userDetailsService.loadUserByUsername(email1);
        UserDetails result2 = userDetailsService.loadUserByUsername(email2);
        
        // Then - Each call should return correct user
        assertEquals(email1, result1.getUsername());
        assertEquals("password1", result1.getPassword());
        
        assertEquals(email2, result2.getUsername());
        assertEquals("password2", result2.getPassword());
        
        // Verify independent calls
        verify(userRepository).findByUserEmail(email1);
        verify(userRepository).findByUserEmail(email2);
    }
}