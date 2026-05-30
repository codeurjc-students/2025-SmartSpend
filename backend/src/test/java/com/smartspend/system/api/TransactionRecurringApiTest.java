package com.smartspend.system.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.smartspend.transaction.TransactionController;
import com.smartspend.transaction.TransactionService;

class TransactionRecurringApiTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
    }

    @Test
    @DisplayName("API-R-1 - Should cancel recurrence and return confirmation message")
    void shouldCancelRecurrenceAndReturnConfirmationMessage() throws Exception {
        Long transactionId = 44L;
        String email = "recurring@test.com";
        String successMessage = "Suscripción cancelada exitosamente. No se generarán más cobros.";

        when(transactionService.cancelRecurrence(transactionId, email)).thenReturn(successMessage);

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(email, null);

        mockMvc.perform(patch("/api/v1/transactions/{transactionId}/cancel-recurrence", transactionId)
                .principal(authentication))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value(successMessage));

        verify(transactionService).cancelRecurrence(transactionId, email);
    }
}
