package com.smartspend.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.smartspend.auth.AuthService;
import com.smartspend.auth.JwtService;
import com.smartspend.auth.dtos.AuthResponseDto;
import com.smartspend.auth.dtos.LoginRequestDto;
import com.smartspend.auth.dtos.RegisterRequestDto;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User("testuser", "test@example.com", "hashedpassword");
        testUser.setUserId(1L);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // Given
        RegisterRequestDto registerRequest = new RegisterRequestDto(
            "newuser", "new@example.com", "password123"
        );
        
        String hashedPassword = "hashedpassword123";
        String token = "jwt.token.here";
        
        // When - Configure mocks
        when(userRepository.findByUserEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(1L);
            return user;
        });
        when(jwtService.generateToken(1L, "new@example.com")).thenReturn(token);
        
        // When - Execute
        AuthResponseDto result = authService.register(registerRequest);
        
        // Then - Verify
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(token, result.token());
        assertEquals("newuser", result.username());
        assertEquals("new@example.com", result.email());
        
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(1L, "new@example.com");
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        RegisterRequestDto registerRequest = new RegisterRequestDto(
            "testuser", "test@example.com", "password123"
        );
        
        // When - Email already exists
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        // Then - Should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.register(registerRequest)
        );
        
        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionForInvalidRegistrationData() {
        // Given - Invalid data (empty username)
        RegisterRequestDto registerRequest = new RegisterRequestDto(
            "", "test@example.com", "password123"
        );
        
        // Then - Should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.register(registerRequest)
        );
        
        assertEquals("Invalid registration request", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldHashPasswordCorrectly() {
        // Given
        RegisterRequestDto registerRequest = new RegisterRequestDto(
            "testuser", "new@example.com", "password123"
        );
        String hashedPassword = "hashedpassword123";
        
        // When
        when(userRepository.findByUserEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(1L);
            return user;
        });
        when(jwtService.generateToken(anyLong(), anyString())).thenReturn("token");
        
        // Execute
        authService.register(registerRequest);
        
        // Then - Verify password was hashed
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void shouldGenerateJwtTokenOnRegistration() {
        // Given
        RegisterRequestDto registerRequest = new RegisterRequestDto(
            "testuser", "new@example.com", "password123"
        );
        String expectedToken = "jwt.token.generated";
        
        // When
        when(userRepository.findByUserEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(1L);
            return user;
        });
        when(jwtService.generateToken(1L, "new@example.com")).thenReturn(expectedToken);
        
        // Execute
        AuthResponseDto result = authService.register(registerRequest);
        
        // Then - Verify JWT token was generated
        assertEquals(expectedToken, result.token());
        verify(jwtService).generateToken(1L, "new@example.com");
    }

    @Test
    void shouldLoginSuccessfully() {
        // Given
        LoginRequestDto loginRequest = new LoginRequestDto("test@example.com", "password123");
        String token = "jwt.login.token";
        
        // When - Configure mocks
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(1L, "test@example.com")).thenReturn(token);
        
        // When - Execute
        AuthResponseDto result = authService.login(loginRequest);
        
        // Then - Verify
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(token, result.token());
        assertEquals("testuser", result.username());
        assertEquals("test@example.com", result.email());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(1L, "test@example.com");
    }

    @Test
    void shouldThrowExceptionForInvalidCredentials() {
        // Given
        LoginRequestDto loginRequest = new LoginRequestDto("test@example.com", "wrongpassword");
        
        // When - Authentication fails
        doThrow(new BadCredentialsException("Bad credentials"))
            .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        
        // Then - Should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.login(loginRequest)
        );
        
        assertEquals("Invalid credentials", exception.getMessage());
        verify(jwtService, never()).generateToken(anyLong(), anyString());
    }

    @Test
    void shouldGenerateJwtTokenOnLogin() {
        // Given
        LoginRequestDto loginRequest = new LoginRequestDto("test@example.com", "password123");
        String expectedToken = "jwt.login.token";
        
        // When
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(1L, "test@example.com")).thenReturn(expectedToken);
        
        // Execute
        AuthResponseDto result = authService.login(loginRequest);
        
        // Then - Verify JWT token was generated
        assertEquals(expectedToken, result.token());
        verify(jwtService).generateToken(1L, "test@example.com");
    }

    @Test
    void shouldRejectEmptyUsername() {
        // Given - Empty username
        RegisterRequestDto registerRequest = new RegisterRequestDto(
            "", "test@example.com", "password123"
        );
        
        // Then - Should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.register(registerRequest)
        );
        
        assertEquals("Invalid registration request", exception.getMessage());
    }

    @Test
    void shouldRejectEmptyEmail() {
        // Given - Empty email
        RegisterRequestDto registerRequest = new RegisterRequestDto(
            "testuser", "", "password123"
        );
        
        // Then - Should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.register(registerRequest)
        );
        
        assertEquals("Invalid registration request", exception.getMessage());
    }

    @Test
    void shouldRejectEmptyPassword() {
        // Given - Empty password
        RegisterRequestDto registerRequest = new RegisterRequestDto(
            "testuser", "test@example.com", ""
        );
        
        // Then - Should throw exception
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authService.register(registerRequest)
        );
        
        assertEquals("Invalid registration request", exception.getMessage());
    }
}