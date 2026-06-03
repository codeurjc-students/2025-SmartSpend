package com.smartspend.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.smartspend.transaction.Recurrence;
import com.smartspend.transaction.TransactionController;
import com.smartspend.transaction.TransactionService;
import com.smartspend.transaction.TransactionType;
import com.smartspend.transaction.dtos.CreateTransactionDto;
import com.smartspend.transaction.dtos.CreateTransactionWithImageDto;
import com.smartspend.transaction.dtos.PendingDebtSummaryDto;
import com.smartspend.transaction.dtos.RecurringTreeResponseDto;
import com.smartspend.transaction.dtos.TransactionResponseDto;
import com.smartspend.transaction.dtos.TransferRequestDto;
import com.smartspend.transaction.dtos.TransferResponseDto;

class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    private TransactionController controller;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new TransactionController(transactionService);
        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("owner@test.com");
    }

    @Test
    void shouldReturnAllTransactionsForAuthenticatedUser() {
        when(transactionService.findAll("owner@test.com")).thenReturn(List.of(sampleResponse(1L, "Tx")));

        List<TransactionResponseDto> result = controller.getAllTransactions(authentication);

        assertEquals(1, result.size());
        assertEquals("Tx", result.get(0).title());
    }

    @Test
    void shouldReturnRecentTransactionsByAccount() {
        when(transactionService.getRecentTransactionsByAccount(10L, 3, "owner@test.com")).thenReturn(List.of(sampleResponse(2L, "Recent")));

        List<TransactionResponseDto> result = controller.getTransactionsByAccount(10L, 3, authentication);

        assertEquals(1, result.size());
        assertEquals("Recent", result.get(0).title());
    }

    @Test
    void shouldReturnPaginatedTransactions() {
        Page<TransactionResponseDto> page = new PageImpl<>(List.of(sampleResponse(3L, "Paged")), PageRequest.of(0, 5), 1);
        when(transactionService.getTransactionsByAccount(
            10L,
            "owner@test.com",
            "a",
            "EXPENSE",
            "2026-01-01",
            "2026-01-31",
            new BigDecimal("1.00"),
            new BigDecimal("100.00"),
            20L,
            true,
            PageRequest.of(0, 5)
        )).thenReturn(page);

        ResponseEntity<Page<TransactionResponseDto>> response = controller.getTransactionsByAccountPaginated(
            10L,
            "a",
            "EXPENSE",
            "2026-01-01",
            "2026-01-31",
            new BigDecimal("1.00"),
            new BigDecimal("100.00"),
            20L,
            true,
            PageRequest.of(0, 5),
            authentication
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getContent().size());
    }

    @Test
    void shouldReturnPendingDebtsSummary() {
        PendingDebtSummaryDto debt = new PendingDebtSummaryDto(1L, 2L, "Tx", "Ana", new BigDecimal("20.00"), LocalDate.now(), 10L, "Main");
        when(transactionService.getPendingDebtsSummary("owner@test.com", 5)).thenReturn(List.of(debt));

        List<PendingDebtSummaryDto> result = controller.getPendingDebtsSummary(authentication);

        assertEquals(1, result.size());
        assertEquals("Ana", result.get(0).debtorName());
    }

    @Test
    void shouldReturnRecurringTree() {
        RecurringTreeResponseDto tree = new RecurringTreeResponseDto(1L, "Rent", new BigDecimal("800.00"), Recurrence.MONTHLY, LocalDate.now(), List.of());
        when(transactionService.getRecurringTreeByAccount(10L, "owner@test.com")).thenReturn(List.of(tree));

        ResponseEntity<List<RecurringTreeResponseDto>> response = controller.getRecurringTreeByAccount(10L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void shouldReturnTransactionByIdWhenExists() {
        when(transactionService.getTransactionById(5L)).thenReturn(Optional.of(sampleResponse(5L, "Found")));

        ResponseEntity<TransactionResponseDto> response = controller.getTransactionById(5L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Found", response.getBody().title());
    }

    @Test
    void shouldReturnNotFoundWhenTransactionByIdMissing() {
        when(transactionService.getTransactionById(5L)).thenReturn(Optional.empty());

        ResponseEntity<TransactionResponseDto> response = controller.getTransactionById(5L, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldDeleteAdjustmentWhenTransactionIsDebtAdjustment() {
        TransactionResponseDto adjustment = new TransactionResponseDto(
            7L,
            "Ajuste deuda: Ana",
            "desc",
            new BigDecimal("10.00"),
            null,
            null,
            LocalDate.now(),
            TransactionType.INCOME,
            Recurrence.NONE,
            10L,
            "Main",
            null,
            true,
            List.of(),
            false,
            null,
            null,
            null
        );
        when(transactionService.getTransactionById(7L)).thenReturn(Optional.of(adjustment));

        controller.deleteTransaction(7L, authentication);

        verify(transactionService).deleteAdjustmentAndRestoreDebt(7L, "owner@test.com");
    }

    @Test
    void shouldDeleteRegularTransactionWhenNotAdjustment() {
        when(transactionService.getTransactionById(8L)).thenReturn(Optional.of(sampleResponse(8L, "Regular")));

        controller.deleteTransaction(8L, authentication);

        verify(transactionService).deleteTransaction(8L, "owner@test.com");
    }

    @Test
    void shouldCreateTransaction() {
        CreateTransactionDto request = new CreateTransactionDto("Salary", "desc", new BigDecimal("1000.00"), TransactionType.INCOME,
            LocalDate.now(), Recurrence.NONE, 10L, 20L, null, false, List.of());
        when(transactionService.saveTransaction(request, "owner@test.com")).thenReturn(sampleResponse(9L, "Salary"));

        ResponseEntity<TransactionResponseDto> response = controller.createTransaction(request, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Salary", response.getBody().title());
    }

    @Test
    void shouldCreateTransactionWithImage() {
        CreateTransactionWithImageDto request = new CreateTransactionWithImageDto();
        request.setTitle("With image");
        request.setAmount(new BigDecimal("10.00"));
        when(transactionService.saveTransactionWithImage(request, "owner@test.com")).thenReturn(sampleResponse(10L, "With image"));

        ResponseEntity<TransactionResponseDto> response = controller.createTransactionWithImage(request, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void shouldReturnBadRequestWhenCreateTransactionWithImageHasIllegalArgument() {
        CreateTransactionWithImageDto request = new CreateTransactionWithImageDto();
        when(transactionService.saveTransactionWithImage(request, "owner@test.com")).thenThrow(new IllegalArgumentException("bad"));

        ResponseEntity<TransactionResponseDto> response = controller.createTransactionWithImage(request, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldReturnServerErrorWhenCreateTransactionWithImageFailsUnexpectedly() {
        CreateTransactionWithImageDto request = new CreateTransactionWithImageDto();
        when(transactionService.saveTransactionWithImage(request, "owner@test.com")).thenThrow(new RuntimeException("boom"));

        ResponseEntity<TransactionResponseDto> response = controller.createTransactionWithImage(request, authentication);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void shouldUpdateTransactionWhenExists() {
        CreateTransactionDto request = new CreateTransactionDto("Update", "desc", new BigDecimal("20.00"), TransactionType.EXPENSE,
            LocalDate.now(), Recurrence.NONE, 10L, 20L, null, false, List.of());
        when(transactionService.updateTransaction(11L, request, "owner@test.com")).thenReturn(Optional.of(sampleResponse(11L, "Updated")));

        ResponseEntity<TransactionResponseDto> response = controller.updateTransaction(11L, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody().title());
    }

    @Test
    void shouldReturnNotFoundWhenUpdateTransactionMissing() {
        CreateTransactionDto request = new CreateTransactionDto("Update", "desc", new BigDecimal("20.00"), TransactionType.EXPENSE,
            LocalDate.now(), Recurrence.NONE, 10L, 20L, null, false, List.of());
        when(transactionService.updateTransaction(11L, request, "owner@test.com")).thenReturn(Optional.empty());

        ResponseEntity<TransactionResponseDto> response = controller.updateTransaction(11L, request, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldMarkDebtAsPaid() {
        when(transactionService.markDebtAsPaid(12L, 3L, "owner@test.com")).thenReturn(sampleResponse(12L, "Debt paid"));

        ResponseEntity<TransactionResponseDto> response = controller.markDebtAsPaid(12L, 3L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Debt paid", response.getBody().title());
    }

    @Test
    void shouldCancelRecurrence() {
        when(transactionService.cancelRecurrence(13L, "owner@test.com")).thenReturn("Recurring transaction canceled");

        ResponseEntity<Map<String, String>> response = controller.cancelRecurrence(13L, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Recurring transaction canceled", response.getBody().get("message"));
    }

    @Test
    void shouldResolveTransferBetweenAccounts() {
        TransferRequestDto request = new TransferRequestDto(1L, 2L, new BigDecimal("50.00"), "Transfer", LocalDate.now(), "desc", Recurrence.NONE);
        TransferResponseDto transferResponse = new TransferResponseDto(100L, 101L, new BigDecimal("50.00"), LocalDate.now(), "ok");
        when(transactionService.createTransfers(request, "owner@test.com")).thenReturn(transferResponse);

        ResponseEntity<TransferResponseDto> response = controller.resolveTransferBetweenAccounts(request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ok", response.getBody().message());
    }

    @Test
    void shouldHandleValidationException() throws Exception {
        TransferRequestDto target = new TransferRequestDto(1L, 2L, new BigDecimal("10.00"), "t", LocalDate.now(), null, Recurrence.NONE);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "request");
        bindingResult.addError(new FieldError("request", "amount", "Amount is required"));

        Method method = this.getClass().getDeclaredMethod("dummyMethod", TransferRequestDto.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(new org.springframework.core.MethodParameter(method, 0), bindingResult);

        ResponseEntity<Map<String, String>> response = controller.handleValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Amount is required", response.getBody().get("message"));
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        ResponseEntity<Map<String, String>> response = controller.handleIllegalArgumentException(new IllegalArgumentException("Invalid input"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid input", response.getBody().get("message"));
    }

    @SuppressWarnings("unused")
    private void dummyMethod(TransferRequestDto request) {
        // Method only used to build a MethodParameter for MethodArgumentNotValidException.
    }

    private TransactionResponseDto sampleResponse(Long id, String title) {
        return new TransactionResponseDto(
            id,
            title,
            "desc",
            new BigDecimal("10.00"),
            null,
            null,
            LocalDate.now(),
            TransactionType.EXPENSE,
            Recurrence.NONE,
            10L,
            "Main",
            null,
            false,
            List.of(),
            false,
            null,
            null,
            null
        );
    }
}
