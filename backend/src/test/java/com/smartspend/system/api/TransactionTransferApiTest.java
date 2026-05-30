package com.smartspend.system.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.smartspend.transaction.Recurrence;
import com.smartspend.transaction.TransactionController;
import com.smartspend.transaction.TransactionService;
import com.smartspend.transaction.dtos.TransferRequestDto;
import com.smartspend.transaction.dtos.TransferResponseDto;

class TransactionTransferApiTest {

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
    @DisplayName("API-TR-1 - Should create transfer and return transfer response")
    void shouldCreateTransferAndReturnTransferResponse() throws Exception {
        TransferRequestDto request = new TransferRequestDto(
            1L,
            2L,
            new BigDecimal("125.50"),
            "Rent reserve",
            LocalDate.of(2026, 5, 28),
            "Monthly reserve transfer",
            Recurrence.MONTHLY
        );

        TransferResponseDto expectedResponse = new TransferResponseDto(
            101L,
            102L,
            new BigDecimal("125.50"),
            LocalDate.of(2026, 5, 28),
            "Transfer created"
        );

        when(transactionService.createTransfers(any(TransferRequestDto.class), eq("transfer@test.com")))
            .thenReturn(expectedResponse);

        Authentication authentication =
            new UsernamePasswordAuthenticationToken("transfer@test.com", null);

        String requestBody = """
            {
              \"originAccountId\": 1,
              \"destinationAccountId\": 2,
              \"amount\": 125.50,
              \"title\": \"Rent reserve\",
              \"date\": \"2026-05-28\",
              \"description\": \"Monthly reserve transfer\",
              \"recurrence\": \"MONTHLY\"
            }
            """;

        mockMvc.perform(post("/api/v1/transactions/transfer")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.originTransactionId").value(101))
            .andExpect(jsonPath("$.destinationTransactionId").value(102))
            .andExpect(jsonPath("$.amount").value(125.50))
            .andExpect(jsonPath("$.date[0]").value(2026))
            .andExpect(jsonPath("$.date[1]").value(5))
            .andExpect(jsonPath("$.date[2]").value(28))
            .andExpect(jsonPath("$.message").value("Transfer created"));

        verify(transactionService).createTransfers(any(TransferRequestDto.class), eq("transfer@test.com"));
    }

    @Test
    @DisplayName("API-TR-2 - Should return bad request for malformed transfer payload")
    void shouldReturnBadRequestForMalformedTransferPayload() throws Exception {
        Authentication authentication =
            new UsernamePasswordAuthenticationToken("transfer@test.com", null);

        String malformedRequestBody = """
            {
              "originAccountId": 1,
              "destinationAccountId":
            }
            """;

        mockMvc.perform(post("/api/v1/transactions/transfer")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedRequestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("API-TR-3 - Should map transfer payload fields into service request")
    void shouldMapTransferPayloadFieldsIntoServiceRequest() throws Exception {
        TransferResponseDto expectedResponse = new TransferResponseDto(
            301L,
            302L,
            new BigDecimal("25.75"),
            LocalDate.of(2026, 5, 29),
            "Transfer created"
        );

        when(transactionService.createTransfers(any(TransferRequestDto.class), eq("mapping@test.com")))
            .thenReturn(expectedResponse);

        Authentication authentication =
            new UsernamePasswordAuthenticationToken("mapping@test.com", null);

        String requestBody = """
            {
              "originAccountId": 10,
              "destinationAccountId": 20,
              "amount": 25.75,
              "title": "Savings move",
              "date": "2026-05-29",
              "description": "Weekly transfer",
              "recurrence": "WEEKLY"
            }
            """;

        mockMvc.perform(post("/api/v1/transactions/transfer")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.originTransactionId").value(301))
            .andExpect(jsonPath("$.destinationTransactionId").value(302));

        ArgumentCaptor<TransferRequestDto> requestCaptor = ArgumentCaptor.forClass(TransferRequestDto.class);
        verify(transactionService).createTransfers(requestCaptor.capture(), eq("mapping@test.com"));

        TransferRequestDto captured = requestCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(10L, captured.originAccountId());
        org.junit.jupiter.api.Assertions.assertEquals(20L, captured.destinationAccountId());
        org.junit.jupiter.api.Assertions.assertEquals(0, new BigDecimal("25.75").compareTo(captured.amount()));
        org.junit.jupiter.api.Assertions.assertEquals("Savings move", captured.title());
        org.junit.jupiter.api.Assertions.assertEquals(LocalDate.of(2026, 5, 29), captured.date());
        org.junit.jupiter.api.Assertions.assertEquals("Weekly transfer", captured.description());
        org.junit.jupiter.api.Assertions.assertEquals(Recurrence.WEEKLY, captured.recurrence());
    }
}
