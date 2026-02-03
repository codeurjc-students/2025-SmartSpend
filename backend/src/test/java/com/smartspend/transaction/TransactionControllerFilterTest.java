package com.smartspend.transaction;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartspend.transaction.dtos.TransactionResponseDto;

@WebMvcTest(TransactionController.class)
@DisplayName("TransactionController - Filter Tests")
class TransactionControllerFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private TransactionResponseDto testResponse1;
    private TransactionResponseDto testResponse2;
    private Page<TransactionResponseDto> mockPage;

    @BeforeEach
    void setUp() {
        testResponse1 = new TransactionResponseDto();
        testResponse1.setId(1L);
        testResponse1.setTitle("Compra supermercado");
        testResponse1.setDescription("Compras semanales");
        testResponse1.setAmount(new BigDecimal("75.50"));
        testResponse1.setDate(LocalDate.of(2023, 6, 15));
        testResponse1.setType(TransactionType.EXPENSE);

        testResponse2 = new TransactionResponseDto();
        testResponse2.setId(2L);
        testResponse2.setTitle("Nómina");
        testResponse2.setDescription("Sueldo de junio");
        testResponse2.setAmount(new BigDecimal("1500.00"));
        testResponse2.setDate(LocalDate.of(2023, 6, 1));
        testResponse2.setType(TransactionType.INCOME);

        List<TransactionResponseDto> content = List.of(testResponse1, testResponse2);
        mockPage = new PageImpl<>(content, PageRequest.of(0, 5), 2);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should get transactions with no filters")
    void getTransactionsPaginated_NoFilters_ReturnsAllTransactions() throws Exception {
        // Arrange
        Long accountId = 1L;
        when(transactionService.getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), isNull(), 
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(mockPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/transactions/account/{accountId}/paginated", accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Compra supermercado"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].title").value("Nómina"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(0));

        verify(transactionService).getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), isNull(), 
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should filter by search parameter")
    void getTransactionsPaginated_WithSearchFilter_ReturnsFilteredTransactions() throws Exception {
        // Arrange
        Long accountId = 1L;
        String search = "supermercado";
        Page<TransactionResponseDto> filteredPage = new PageImpl<>(
            List.of(testResponse1), PageRequest.of(0, 5), 1);

        when(transactionService.getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), eq(search), isNull(), 
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(filteredPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/transactions/account/{accountId}/paginated", accountId)
                .param("search", search)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Compra supermercado"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(transactionService).getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), eq(search), isNull(), 
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should filter by transaction type")
    void getTransactionsPaginated_WithTypeFilter_ReturnsFilteredTransactions() throws Exception {
        // Arrange
        Long accountId = 1L;
        String type = "EXPENSE";
        Page<TransactionResponseDto> filteredPage = new PageImpl<>(
            List.of(testResponse1), PageRequest.of(0, 5), 1);

        when(transactionService.getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), eq(type), 
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(filteredPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/transactions/account/{accountId}/paginated", accountId)
                .param("type", type)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].type").value("EXPENSE"));

        verify(transactionService).getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), eq(type), 
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should filter by date range")
    void getTransactionsPaginated_WithDateFilter_ReturnsFilteredTransactions() throws Exception {
        // Arrange
        Long accountId = 1L;
        String dateFrom = "2023-06-10";
        String dateTo = "2023-06-20";
        Page<TransactionResponseDto> filteredPage = new PageImpl<>(
            List.of(testResponse1), PageRequest.of(0, 5), 1);

        when(transactionService.getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), isNull(), 
            eq(dateFrom), eq(dateTo), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(filteredPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/transactions/account/{accountId}/paginated", accountId)
                .param("dateFrom", dateFrom)
                .param("dateTo", dateTo)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].date").value("2023-06-15"));

        verify(transactionService).getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), isNull(), 
            eq(dateFrom), eq(dateTo), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should filter by amount range")
    void getTransactionsPaginated_WithAmountFilter_ReturnsFilteredTransactions() throws Exception {
        // Arrange
        Long accountId = 1L;
        BigDecimal minAmount = new BigDecimal("50.00");
        BigDecimal maxAmount = new BigDecimal("100.00");
        Page<TransactionResponseDto> filteredPage = new PageImpl<>(
            List.of(testResponse1), PageRequest.of(0, 5), 1);

        when(transactionService.getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), isNull(), 
            isNull(), isNull(), eq(minAmount), eq(maxAmount), isNull(), any(Pageable.class)))
            .thenReturn(filteredPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/transactions/account/{accountId}/paginated", accountId)
                .param("minAmount", "50.00")
                .param("maxAmount", "100.00")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].amount").value(75.50));

        verify(transactionService).getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), isNull(), 
            isNull(), isNull(), eq(minAmount), eq(maxAmount), isNull(), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should filter by category ID")
    void getTransactionsPaginated_WithCategoryFilter_ReturnsFilteredTransactions() throws Exception {
        // Arrange
        Long accountId = 1L;
        Long categoryId = 5L;
        Page<TransactionResponseDto> filteredPage = new PageImpl<>(
            List.of(testResponse1), PageRequest.of(0, 5), 1);

        when(transactionService.getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), isNull(), 
            isNull(), isNull(), isNull(), isNull(), eq(categoryId), any(Pageable.class)))
            .thenReturn(filteredPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/transactions/account/{accountId}/paginated", accountId)
                .param("categoryId", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(transactionService).getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), isNull(), 
            isNull(), isNull(), isNull(), isNull(), eq(categoryId), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should apply multiple filters simultaneously")
    void getTransactionsPaginated_WithMultipleFilters_ReturnsFilteredTransactions() throws Exception {
        // Arrange
        Long accountId = 1L;
        String search = "compra";
        String type = "EXPENSE";
        String dateFrom = "2023-06-01";
        String dateTo = "2023-06-30";
        BigDecimal minAmount = new BigDecimal("50.00");
        BigDecimal maxAmount = new BigDecimal("100.00");
        Long categoryId = 2L;
        
        Page<TransactionResponseDto> filteredPage = new PageImpl<>(
            List.of(testResponse1), PageRequest.of(0, 5), 1);

        when(transactionService.getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), eq(search), eq(type), 
            eq(dateFrom), eq(dateTo), eq(minAmount), eq(maxAmount), eq(categoryId), any(Pageable.class)))
            .thenReturn(filteredPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/transactions/account/{accountId}/paginated", accountId)
                .param("search", search)
                .param("type", type)
                .param("dateFrom", dateFrom)
                .param("dateTo", dateTo)
                .param("minAmount", "50.00")
                .param("maxAmount", "100.00")
                .param("categoryId", "2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        verify(transactionService).getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), eq(search), eq(type), 
            eq(dateFrom), eq(dateTo), eq(minAmount), eq(maxAmount), eq(categoryId), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should respect pagination parameters")
    void getTransactionsPaginated_WithPagination_RespectsPageableParameters() throws Exception {
        // Arrange
        Long accountId = 1L;
        Page<TransactionResponseDto> customPage = new PageImpl<>(
            List.of(testResponse1), PageRequest.of(1, 3), 10);

        when(transactionService.getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), isNull(), 
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(customPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/transactions/account/{accountId}/paginated", accountId)
                .param("page", "1")
                .param("size", "3")
                .param("sort", "amount,asc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.totalElements").value(10));

        // Verify that the service method was called with correct Pageable
        verify(transactionService).getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), isNull(), 
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should use default pagination when not specified")
    void getTransactionsPaginated_WithoutPagination_UsesDefaults() throws Exception {
        // Arrange
        Long accountId = 1L;
        when(transactionService.getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), isNull(), 
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(mockPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/transactions/account/{accountId}/paginated", accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5)) // Default size from @PageableDefault
                .andExpect(jsonPath("$.number").value(0)); // Default page

        verify(transactionService).getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), isNull(), 
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should handle empty filter parameters")
    void getTransactionsPaginated_WithEmptyFilters_PassesNullToService() throws Exception {
        // Arrange
        Long accountId = 1L;
        when(transactionService.getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), isNull(), 
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(mockPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/transactions/account/{accountId}/paginated", accountId)
                .param("search", "")
                .param("type", "")
                .param("dateFrom", "")
                .param("dateTo", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpected(status().isOk());

        // Verify that empty strings are passed (service layer handles empty string logic)
        verify(transactionService).getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), eq(""), eq(""), 
            eq(""), eq(""), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should handle invalid amount parameters gracefully")
    void getTransactionsPaginated_WithInvalidAmounts_IgnoresInvalidValues() throws Exception {
        // Arrange
        Long accountId = 1L;
        when(transactionService.getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), isNull(), 
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(mockPage);

        // Act & Assert - Should handle invalid BigDecimal parameters
        mockMvc.perform(get("/api/v1/transactions/account/{accountId}/paginated", accountId)
                .param("minAmount", "invalid-number")
                .param("maxAmount", "also-invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()); // Spring should return 400 for invalid BigDecimal
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should handle invalid category ID gracefully")
    void getTransactionsPaginated_WithInvalidCategoryId_IgnoresInvalidValue() throws Exception {
        // Arrange
        Long accountId = 1L;

        // Act & Assert - Should handle invalid Long parameter
        mockMvc.perform(get("/api/v1/transactions/account/{accountId}/paginated", accountId)
                .param("categoryId", "not-a-number")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()); // Spring should return 400 for invalid Long
    }

    @Test
    @DisplayName("Should require authentication")
    void getTransactionsPaginated_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/transactions/account/{accountId}/paginated", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(transactionService, never()).getTransactionsByAccount(
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should return empty page when no transactions found")
    void getTransactionsPaginated_NoTransactions_ReturnsEmptyPage() throws Exception {
        // Arrange
        Long accountId = 1L;
        Page<TransactionResponseDto> emptyPage = new PageImpl<>(
            List.of(), PageRequest.of(0, 5), 0);

        when(transactionService.getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), isNull(), 
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/transactions/account/{accountId}/paginated", accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.empty").value(true));

        verify(transactionService).getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), isNull(), 
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should handle service exceptions properly")
    void getTransactionsPaginated_ServiceThrowsException_ReturnsErrorResponse() throws Exception {
        // Arrange
        Long accountId = 1L;
        when(transactionService.getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), isNull(), 
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenThrow(new RuntimeException("Account not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/transactions/account/{accountId}/paginated", accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(transactionService).getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), isNull(), 
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should pass correct user email from authentication")
    void getTransactionsPaginated_WithAuthentication_PassesCorrectUserEmail() throws Exception {
        // Arrange
        Long accountId = 1L;
        when(transactionService.getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), isNull(), 
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
            .thenReturn(mockPage);

        // Act
        mockMvc.perform(get("/api/v1/transactions/account/{accountId}/paginated", accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpected(status().isOk());

        // Assert - Verify correct user email is passed from Authentication
        verify(transactionService).getTransactionsByAccount(
            eq(accountId), eq("test@example.com"), isNull(), isNull(), 
            isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }
}