package com.smartspend.system.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.smartspend.auth.AuthController;
import com.smartspend.auth.AuthService;
import com.smartspend.auth.dtos.AuthResponseDto;
import com.smartspend.auth.dtos.GoogleTokenDto;
import com.smartspend.auth.dtos.LoginRequestDto;
import com.smartspend.auth.dtos.RegisterRequestDto;

class AuthApiTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        AuthResponseDto response = new AuthResponseDto(1L, "jwt-token", "juan", "juan@test.com", false);
        when(authService.login(new LoginRequestDto("juan@test.com", "123456"))).thenReturn(response);

        String body = """
            {
              \"email\": \"juan@test.com\",
              \"password\": \"123456\"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(authService).login(new LoginRequestDto("juan@test.com", "123456"));
    }

    @Test
    void shouldRegisterSuccessfully() throws Exception {
        AuthResponseDto response = new AuthResponseDto(2L, "jwt-register", "ana", "ana@test.com", false);
        when(authService.register(new RegisterRequestDto("ana@test.com", "abcdef"))).thenReturn(response);

        String body = """
            {
              \"email\": \"ana@test.com\",
              \"password\": \"abcdef\"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.token").value("jwt-register"));

        verify(authService).register(new RegisterRequestDto("ana@test.com", "abcdef"));
    }

    @Test
    void shouldLoginWithGoogleSuccessfully() throws Exception {
        AuthResponseDto response = new AuthResponseDto(3L, "jwt-google", "maria", "maria@test.com", false);
        when(authService.googleLogin(new GoogleTokenDto("google-id-token"))).thenReturn(response);

        String body = """
            {
              \"token\": \"google-id-token\"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/google-login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(3))
            .andExpect(jsonPath("$.token").value("jwt-google"));

        verify(authService).googleLogin(new GoogleTokenDto("google-id-token"));
    }
}
