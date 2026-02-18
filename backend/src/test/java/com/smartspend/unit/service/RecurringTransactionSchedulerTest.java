package com.smartspend.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.bankAccount.BankAccountRepository;
import com.smartspend.category.Category;
import com.smartspend.transaction.Recurrence;
import com.smartspend.transaction.RecurringTransactionScheduler;
import com.smartspend.transaction.Transaction;
import com.smartspend.transaction.TransactionRepository;
import com.smartspend.transaction.TransactionService;
import com.smartspend.transaction.TransactionType;
import com.smartspend.user.User;

public class RecurringTransactionSchedulerTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock 
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private RecurringTransactionScheduler recurringTransactionScheduler;

    private User testUser;
    private BankAccount testAccount;
    private Category testCategory;
    private Transaction parentTransaction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User("testuser", "test@example.com", "hashedpassword");
        testUser.setUserId(1L);
        
        testAccount = new BankAccount(testUser, "Test Account", new BigDecimal("1000.00"));
        testAccount.setId(1L);
        
        testCategory = new Category("Salary", "Income from work", "#27ae60", TransactionType.INCOME, null);
        testCategory.setId(1L);
        
        parentTransaction = Transaction.builder()
            .id(1L)
            .title("Monthly Salary")
            .description("Recurring salary payment")
            .amount(new BigDecimal("2000.00"))
            .date(LocalDate.now().minusMonths(1))
            .type(TransactionType.INCOME)
            .recurrence(Recurrence.MONTHLY)
            .category(testCategory)
            .account(testAccount)
            .beforeBalance(new BigDecimal("1000.00"))
            .isRecurringSeriesParent(true)
            .nextRecurrenceDate(LocalDate.now())
            .build();
    }

    // ===============================================
    // TESTS PARA SCHEDULER DE TRANSACCIONES RECURRENTES
    // ===============================================

    @Test
    @DisplayName("S-1: generateRecurringTransactions - Should process pending recurring transactions")
    void shouldProcessPendingRecurringTransactions() {
        // Given
        List<Transaction> pendingTransactions = Arrays.asList(parentTransaction);
        
        when(transactionRepository.findPendingRecurringTransactions(any(LocalDate.class)))
            .thenReturn(pendingTransactions);
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        recurringTransactionScheduler.generateRecurringTransactions();

        // Then
        verify(transactionRepository).findPendingRecurringTransactions(LocalDate.now());
        verify(transactionService).upadateAccountBalance(any(Transaction.class), any(BankAccount.class));
        verify(bankAccountRepository).save(testAccount);
        verify(transactionRepository, times(2)).save(any(Transaction.class)); // Child + parent update
    }

    @Test
    @DisplayName("S-2: generateRecurringTransactions - Should handle empty pending transactions list")
    void shouldHandleEmptyPendingTransactionsList() {
        // Given
        when(transactionRepository.findPendingRecurringTransactions(any(LocalDate.class)))
            .thenReturn(Collections.emptyList());

        // When
        recurringTransactionScheduler.generateRecurringTransactions();

        // Then
        verify(transactionRepository).findPendingRecurringTransactions(LocalDate.now());
        verify(transactionService, never()).upadateAccountBalance(any(), any());
        verify(bankAccountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("S-3: generateRecurringTransactions - Should create child transaction with correct properties")
    void shouldCreateChildTransactionWithCorrectProperties() {
        // Given
        List<Transaction> pendingTransactions = Arrays.asList(parentTransaction);
        
        when(transactionRepository.findPendingRecurringTransactions(any(LocalDate.class)))
            .thenReturn(pendingTransactions);
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        recurringTransactionScheduler.generateRecurringTransactions();

        // Then
        ArgumentCaptor<Transaction> savedTransactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(savedTransactionCaptor.capture());
        
        List<Transaction> savedTransactions = savedTransactionCaptor.getAllValues();
        Transaction childTransaction = savedTransactions.stream()
            .filter(t -> t.getIsRecurringSeriesParent() == false)
            .findFirst()
            .orElseThrow();

        // Verify child transaction properties
        assertEquals(parentTransaction.getTitle(), childTransaction.getTitle());
        assertEquals(parentTransaction.getDescription(), childTransaction.getDescription());
        assertEquals(parentTransaction.getAmount(), childTransaction.getAmount());
        assertEquals(parentTransaction.getType(), childTransaction.getType());
        assertEquals(parentTransaction.getCategory(), childTransaction.getCategory());
        assertEquals(parentTransaction.getAccount(), childTransaction.getAccount());
        assertEquals(Recurrence.NONE, childTransaction.getRecurrence());
        assertEquals(false, childTransaction.getIsRecurringSeriesParent());
        assertNull(childTransaction.getNextRecurrenceDate());
        assertEquals(LocalDate.now(), childTransaction.getDate());
    }

    @Test
    @DisplayName("S-4: generateRecurringTransactions - Should update parent next recurrence date for DAILY")
    void shouldUpdateParentNextRecurrenceDateForDaily() {
        // Given
        parentTransaction.setRecurrence(Recurrence.DAILY);
        List<Transaction> pendingTransactions = Arrays.asList(parentTransaction);
        
        when(transactionRepository.findPendingRecurringTransactions(any(LocalDate.class)))
            .thenReturn(pendingTransactions);
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        recurringTransactionScheduler.generateRecurringTransactions();

        // Then
        ArgumentCaptor<Transaction> savedParentCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(savedParentCaptor.capture());
        
        List<Transaction> savedTransactions = savedParentCaptor.getAllValues();
        Transaction updatedParent = savedTransactions.stream()
            .filter(t -> t.getIsRecurringSeriesParent() == true)
            .findFirst()
            .orElseThrow();

        LocalDate expectedNextDate = LocalDate.now().plusDays(1);
        assertEquals(expectedNextDate, updatedParent.getNextRecurrenceDate());
    }

    @Test
    @DisplayName("S-5: generateRecurringTransactions - Should update parent next recurrence date for WEEKLY")
    void shouldUpdateParentNextRecurrenceDateForWeekly() {
        // Given
        parentTransaction.setRecurrence(Recurrence.WEEKLY);
        List<Transaction> pendingTransactions = Arrays.asList(parentTransaction);
        
        when(transactionRepository.findPendingRecurringTransactions(any(LocalDate.class)))
            .thenReturn(pendingTransactions);
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        recurringTransactionScheduler.generateRecurringTransactions();

        // Then
        ArgumentCaptor<Transaction> savedParentCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(savedParentCaptor.capture());
        
        List<Transaction> savedTransactions = savedParentCaptor.getAllValues();
        Transaction updatedParent = savedTransactions.stream()
            .filter(t -> t.getIsRecurringSeriesParent() == true)
            .findFirst()
            .orElseThrow();

        LocalDate expectedNextDate = LocalDate.now().plusWeeks(1);
        assertEquals(expectedNextDate, updatedParent.getNextRecurrenceDate());
    }

    @Test
    @DisplayName("S-6: generateRecurringTransactions - Should update parent next recurrence date for MONTHLY")
    void shouldUpdateParentNextRecurrenceDateForMonthly() {
        // Given
        parentTransaction.setRecurrence(Recurrence.MONTHLY);
        List<Transaction> pendingTransactions = Arrays.asList(parentTransaction);
        
        when(transactionRepository.findPendingRecurringTransactions(any(LocalDate.class)))
            .thenReturn(pendingTransactions);
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        recurringTransactionScheduler.generateRecurringTransactions();

        // Then
        ArgumentCaptor<Transaction> savedParentCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(savedParentCaptor.capture());
        
        List<Transaction> savedTransactions = savedParentCaptor.getAllValues();
        Transaction updatedParent = savedTransactions.stream()
            .filter(t -> t.getIsRecurringSeriesParent() == true)
            .findFirst()
            .orElseThrow();

        LocalDate expectedNextDate = LocalDate.now().plusMonths(1);
        assertEquals(expectedNextDate, updatedParent.getNextRecurrenceDate());
    }

    @Test
    @DisplayName("S-7: generateRecurringTransactions - Should update parent next recurrence date for YEARLY")
    void shouldUpdateParentNextRecurrenceDateForYearly() {
        // Given
        parentTransaction.setRecurrence(Recurrence.YEARLY);
        List<Transaction> pendingTransactions = Arrays.asList(parentTransaction);
        
        when(transactionRepository.findPendingRecurringTransactions(any(LocalDate.class)))
            .thenReturn(pendingTransactions);
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        recurringTransactionScheduler.generateRecurringTransactions();

        // Then
        ArgumentCaptor<Transaction> savedParentCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(savedParentCaptor.capture());
        
        List<Transaction> savedTransactions = savedParentCaptor.getAllValues();
        Transaction updatedParent = savedTransactions.stream()
            .filter(t -> t.getIsRecurringSeriesParent() == true)
            .findFirst()
            .orElseThrow();

        LocalDate expectedNextDate = LocalDate.now().plusYears(1);
        assertEquals(expectedNextDate, updatedParent.getNextRecurrenceDate());
    }

    @Test
    @DisplayName("S-8: generateRecurringTransactions - Should process multiple pending transactions")
    void shouldProcessMultiplePendingTransactions() {
        // Given
        Transaction secondParentTransaction = Transaction.builder()
            .id(2L)
            .title("Weekly Expense")
            .description("Recurring weekly expense")
            .amount(new BigDecimal("100.00"))
            .date(LocalDate.now().minusWeeks(1))
            .type(TransactionType.EXPENSE)
            .recurrence(Recurrence.WEEKLY)
            .category(testCategory)
            .account(testAccount)
            .beforeBalance(new BigDecimal("1000.00"))
            .isRecurringSeriesParent(true)
            .nextRecurrenceDate(LocalDate.now())
            .build();

        List<Transaction> pendingTransactions = Arrays.asList(parentTransaction, secondParentTransaction);
        
        when(transactionRepository.findPendingRecurringTransactions(any(LocalDate.class)))
            .thenReturn(pendingTransactions);
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        recurringTransactionScheduler.generateRecurringTransactions();

        // Then
        verify(transactionRepository).findPendingRecurringTransactions(LocalDate.now());
        verify(transactionService, times(2)).upadateAccountBalance(any(Transaction.class), any(BankAccount.class));
        verify(bankAccountRepository, times(2)).save(testAccount);
        verify(transactionRepository, times(4)).save(any(Transaction.class)); // 2 children + 2 parent updates
    }

    @Test
    @DisplayName("S-9: generateRecurringTransactions - Should handle exceptions gracefully")
    void shouldHandleExceptionsGracefully() {
        // Given
        List<Transaction> pendingTransactions = Arrays.asList(parentTransaction);
        
        when(transactionRepository.findPendingRecurringTransactions(any(LocalDate.class)))
            .thenReturn(pendingTransactions);
        doThrow(new RuntimeException("Database error"))
            .when(transactionService).upadateAccountBalance(any(Transaction.class), any(BankAccount.class));

        // When & Then - Should not throw exception
        recurringTransactionScheduler.generateRecurringTransactions();

        // Verify it tried to process but handled the exception
        verify(transactionRepository).findPendingRecurringTransactions(LocalDate.now());
        verify(transactionService).upadateAccountBalance(any(Transaction.class), any(BankAccount.class));
    }

    @Test 
    @DisplayName("S-10: calculateNextRecurrenceDate - Should return null for NONE recurrence")
    void shouldReturnNullForNoneRecurrence() throws Exception {
        // Given
        LocalDate currentDate = LocalDate.now();
        
        // When - Using reflection to access private method
        java.lang.reflect.Method method = RecurringTransactionScheduler.class.getDeclaredMethod(
            "calculateNextRecurrenceDate", LocalDate.class, Recurrence.class);
        method.setAccessible(true);
        LocalDate result = (LocalDate) method.invoke(recurringTransactionScheduler, currentDate, Recurrence.NONE);
        
        // Then
        assertNull(result);
    }
}