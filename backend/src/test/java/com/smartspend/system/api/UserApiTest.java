package com.smartspend.system.api;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.smartspend.user.UserController;
import com.smartspend.user.UserService;

import jakarta.persistence.EntityNotFoundException;

class UserApiTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void shouldAcceptPrivacyPolicyAndReturn200() throws Exception {
        doNothing().when(userService).acceptPrivacyPolicy(any());

        mockMvc.perform(patch("/api/v1/users/me/accept-privacy")
                .with(user("juan@test.com")))
            .andExpect(status().isOk());

        verify(userService).acceptPrivacyPolicy(any());
    }

    @Test
    void shouldDeleteAccountAndReturn204() throws Exception {
        doNothing().when(userService).deleteAccount(any());

        mockMvc.perform(delete("/api/v1/users/me")
                .with(user("juan@test.com")))
            .andExpect(status().isNoContent());

        verify(userService).deleteAccount(any());
    }

    @Test
    void shouldReturn404WhenDeletingUnknownUser() throws Exception {
        doThrow(new EntityNotFoundException("not found"))
            .when(userService).deleteAccount(any());

        mockMvc.perform(delete("/api/v1/users/me")
                .with(user("ghost@test.com")))
            .andExpect(status().isNotFound());
    }
}
