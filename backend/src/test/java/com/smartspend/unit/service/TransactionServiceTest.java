package com.smartspend.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartspend.category.Category;
import com.smartspend.category.CategoryRepository;
import com.smartspend.transaction.Recurrence;
import com.smartspend.transaction.Transaction;
import com.smartspend.transaction.TransactionMapper;
import com.smartspend.transaction.TransactionRepository;
import com.smartspend.transaction.TransactionService;
import com.smartspend.transaction.TransactionType;
import com.smartspend.transaction.dtos.CreateTransactionDto;
import com.smartspend.transaction.dtos.TransactionResponseDto;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;
import com.smartspend.bankAccount.BankAccount;
import com.smartspend.bankAccount.BankAccountRepository;
import com.smartspend.config.ImageUtils;

public class TransactionServiceTest {
    
    @Mock 
    private TransactionRepository transactionRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private BankAccountRepository bankAccountRepository;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private ImageUtils imageUtils;
    
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private BankAccount testAccount;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User("testuser", "test@example.com", "hashedpassword");
        testUser.setUserId(1L);
        
        testAccount = new BankAccount(testUser, "Test Account", new BigDecimal("1000.00"));
        testAccount.setId(1L);
        
        testCategory = new Category("Salary", "Income from work", "#27ae60", TransactionType.INCOME, null);
        testCategory.setId(1L);
    }

    @Test
    @DisplayName("TS-1.1 - Should increase balance for income transaction")
    void shouldIncreaseBalanceForIncomeTransaction(){
        // Given
        BigDecimal initialBalance = new BigDecimal("1000.00");
        BigDecimal transactionAmount = new BigDecimal("500.00");
        BigDecimal expectedBalance = new BigDecimal("1500.00");
        
        testAccount.setCurrentBalance(initialBalance);
        
        CreateTransactionDto transactionDto = new CreateTransactionDto(
            "Salary Payment",
            "Monthly salary",
            transactionAmount,
            TransactionType.INCOME,
            LocalDate.now(),
            Recurrence.NONE,
            1L,
            1L
        );
        
        // When
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        
        Transaction savedTransaction = Transaction.builder()
            .id(1L)
            .title("Salary Payment")
            .description("Monthly salary")
            .amount(transactionAmount)
            .date(LocalDate.now())
            .type(TransactionType.INCOME)
            .recurrence(Recurrence.NONE)
            .category(testCategory)
            .account(testAccount)
            .build();
            
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        
        TransactionResponseDto responseDto = new TransactionResponseDto(
            1L, "Salary Payment", "Monthly salary", transactionAmount,
            LocalDate.now(), TransactionType.INCOME, Recurrence.NONE,
            1L, "Test Account", testCategory, false, null, null, null
        );
        when(transactionMapper.toResponseDto(any(Transaction.class))).thenReturn(responseDto);
        
        TransactionResponseDto result = transactionService.saveTransaction(transactionDto, "test@example.com");
        
        // Then
        assertNotNull(result);
        assertEquals("Salary Payment", result.title());
        assertEquals(TransactionType.INCOME, result.type());
        
        ArgumentCaptor<BankAccount> accountCaptor = ArgumentCaptor.forClass(BankAccount.class);
        verify(bankAccountRepository).save(accountCaptor.capture());
        
        BankAccount savedAccount = accountCaptor.getValue();
        assertEquals(0, expectedBalance.compareTo(savedAccount.getCurrentBalance()),
            "Balance should be increased by transaction amount for INCOME transaction");
    }

    @Test
    @DisplayName("TS-1.2 - Should decrease balance for expense transaction") 
    void shouldDecreaseBalanceForExpenseTransaction(){
        // Given
        BigDecimal initialBalance = new BigDecimal("1000.00");
        BigDecimal transactionAmount = new BigDecimal("200.00");
        BigDecimal expectedBalance = new BigDecimal("800.00");
        
        testAccount.setCurrentBalance(initialBalance);
        
        CreateTransactionDto transactionDto = new CreateTransactionDto(
            "Grocery Shopping",
            "Weekly groceries",
            transactionAmount,
            TransactionType.EXPENSE,
            LocalDate.now(),
            Recurrence.NONE,
            1L,
            1L
        );
        
        // When
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        Transaction savedTransaction = Transaction.builder()
            .id(1L)
            .title("Grocery Shopping")
            .description("Weekly groceries")
            .amount(transactionAmount)
            .date(LocalDate.now())
            .type(TransactionType.EXPENSE)
            .recurrence(Recurrence.NONE)
            .category(testCategory)
            .account(testAccount)
            .build();
            
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        TransactionResponseDto responseDto = new TransactionResponseDto(
            1L, "Grocery Shopping", "Weekly groceries", transactionAmount,
            LocalDate.now(), TransactionType.EXPENSE, Recurrence.NONE,
            1L, "Test Account", testCategory, false, null, null, null
        );
        when(transactionMapper.toResponseDto(any(Transaction.class))).thenReturn(responseDto);

        TransactionResponseDto result = transactionService.saveTransaction(transactionDto, "test@example.com");
        
        // Then
        assertNotNull(result);
        assertEquals("Grocery Shopping", result.title());
        assertEquals(TransactionType.EXPENSE, result.type());
        
        ArgumentCaptor<BankAccount> accountCaptor = ArgumentCaptor.forClass(BankAccount.class);
        verify(bankAccountRepository).save(accountCaptor.capture());
        
        BankAccount savedAccount = accountCaptor.getValue();
        assertEquals(0, expectedBalance.compareTo(savedAccount.getCurrentBalance()),
            "Balance should be decreased by transaction amount for EXPENSE transaction");
    }

    @Test
    @DisplayName("TS-1.3 - Should revert income transaction on delete")
    void shouldRevertIncomeTransactionOnDelete() {
        // Given
        BigDecimal initialBalance = new BigDecimal("1500.00");
        BigDecimal transactionAmount = new BigDecimal("500.00");
        BigDecimal expectedBalance = new BigDecimal("1000.00");
        
        testAccount.setCurrentBalance(initialBalance);
        
        Transaction existingTransaction = Transaction.builder()
            .id(1L)
            .title("Salary Payment")
            .amount(transactionAmount)
            .type(TransactionType.INCOME)
            .account(testAccount)
            .build();
        
        // When
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existingTransaction));
        
        transactionService.deleteTransaction(1L, "test@example.com");
        
        // Then
        ArgumentCaptor<BankAccount> accountCaptor = ArgumentCaptor.forClass(BankAccount.class);
        verify(bankAccountRepository).save(accountCaptor.capture());
        
        BankAccount savedAccount = accountCaptor.getValue();
        assertEquals(0, expectedBalance.compareTo(savedAccount.getCurrentBalance()),
            "Balance should be decreased when deleting INCOME transaction");
    }

    @Test
    @DisplayName("TS-1.4 - Should revert expense transaction on delete")
    void shouldRevertExpenseTransactionOnDelete() {
        // Given
        BigDecimal initialBalance = new BigDecimal("800.00");
        BigDecimal transactionAmount = new BigDecimal("200.00");
        BigDecimal expectedBalance = new BigDecimal("1000.00");
        
        testAccount.setCurrentBalance(initialBalance);
        
        Transaction existingTransaction = Transaction.builder()
            .id(1L)
            .title("Grocery Shopping")
            .amount(transactionAmount)
            .type(TransactionType.EXPENSE)
            .account(testAccount)
            .build();
        
        // When
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existingTransaction));
        
        transactionService.deleteTransaction(1L, "test@example.com");
        
        // Then
        ArgumentCaptor<BankAccount> accountCaptor = ArgumentCaptor.forClass(BankAccount.class);
        verify(bankAccountRepository).save(accountCaptor.capture());
        
        BankAccount savedAccount = accountCaptor.getValue();
        assertEquals(0, expectedBalance.compareTo(savedAccount.getCurrentBalance()),
            "Balance should be increased when deleting EXPENSE transaction");
    }

    @Test
    @DisplayName("TS-1.5 - Should throw exception when user not owner of transaction")
    void shouldThrowExceptionWhenUserNotOwnerOfTransaction() {
        // Given
        User otherUser = new User("other", "other@example.com", "password");
        otherUser.setUserId(2L);
        
        BankAccount otherAccount = new BankAccount(otherUser, "Other Account", BigDecimal.ZERO);
        otherAccount.setId(2L);
        
        Transaction existingTransaction = Transaction.builder()
            .id(1L)
            .title("Some Transaction")
            .amount(new BigDecimal("100.00"))
            .type(TransactionType.EXPENSE)
            .account(otherAccount)
            .build();
        
        // When
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existingTransaction));
        
        // Then
        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class,
            () -> transactionService.deleteTransaction(1L, "test@example.com")
        );
        
        assertEquals("Unauthorized to delete this transaction", exception.getMessage());
    }

    @Test
    @DisplayName("TS-1.6 - Should throw exception when transaction not found")
    void shouldThrowExceptionWhenTransactionNotFound() {
        // Given
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Then
        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class,
            () -> transactionService.deleteTransaction(999L, "test@example.com")
        );
        
        assertEquals("Transaction not found", exception.getMessage());
    }

    @Test
    @DisplayName("TS-1.7 - Should use current date when date is null")
    void shouldUseCurrentDateWhenDateIsNull() {
        // Given
        CreateTransactionDto transactionDto = new CreateTransactionDto(
            "Test Transaction",
            "Test description",
            new BigDecimal("100.00"),
            TransactionType.EXPENSE,
            null,
            Recurrence.NONE,
            1L,
            1L
        );
        
        // When
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        
        LocalDate today = LocalDate.now();
        Transaction savedTransaction = Transaction.builder()
            .id(1L)
            .title("Test Transaction")
            .date(today)
            .amount(new BigDecimal("100.00"))
            .type(TransactionType.EXPENSE)
            .account(testAccount)
            .category(testCategory)
            .build();
            
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
        
        TransactionResponseDto responseDto = new TransactionResponseDto(
            1L, "Test Transaction", "Test description", new BigDecimal("100.00"),
            today, TransactionType.EXPENSE, Recurrence.NONE,
            1L, "Test Account", testCategory, false, null, null, null
        );
        when(transactionMapper.toResponseDto(any(Transaction.class))).thenReturn(responseDto);
        
        TransactionResponseDto result = transactionService.saveTransaction(transactionDto, "test@example.com");
        
        // Then
        assertEquals(today, result.date());
    }

    @Test
    @DisplayName("TS-1.8 - Should throw exception when user not owner of account")
    void shouldThrowExceptionWhenUserNotOwnerOfAccount() {
        // Given
        User otherUser = new User("other", "other@example.com", "password");
        otherUser.setUserId(2L);
        
        BankAccount otherAccount = new BankAccount(otherUser, "Other Account", BigDecimal.ZERO);
        otherAccount.setId(2L);
        
        CreateTransactionDto transactionDto = new CreateTransactionDto(
            "Test Transaction",
            "Test description",
            new BigDecimal("100.00"),
            TransactionType.EXPENSE,
            LocalDate.now(),
            Recurrence.NONE,
            2L,
            1L
        );
        
        // When
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(2L)).thenReturn(Optional.of(otherAccount));
        
        // Then
        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class,
            () -> transactionService.saveTransaction(transactionDto, "test@example.com")
        );
        
        assertEquals("Unauthorized to add transaction to this account", exception.getMessage());
    }

    @Test
    @DisplayName("TS-1.9 - Should throw exception when account not found")
    void shouldThrowExceptionWhenAccountNotFound() {
        // Given
        CreateTransactionDto transactionDto = new CreateTransactionDto(
            "Test Transaction",
            "Test description",
            new BigDecimal("100.00"),
            TransactionType.EXPENSE,
            LocalDate.now(),
            Recurrence.NONE,
            999L,
            1L
        );
        
        // When
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Then
        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class,
            () -> transactionService.saveTransaction(transactionDto, "test@example.com")
        );
        
        assertEquals("Bank account not found", exception.getMessage());
    }

    @Test
    @DisplayName("TS-1.10 - Should throw exception when category not found")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // Given
        CreateTransactionDto transactionDto = new CreateTransactionDto(
            "Test Transaction",
            "Test description",
            new BigDecimal("100.00"),
            TransactionType.EXPENSE,
            LocalDate.now(),
            Recurrence.NONE,
            1L,
            999L
        );
        
        // When
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Then
        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class,
            () -> transactionService.saveTransaction(transactionDto, "test@example.com")
        );
        
        assertEquals("Category not found", exception.getMessage());
    }

    // @Test
    // @DisplayName("TS-1.11 - Should throw exception when user not authorized for account")
    // void shouldThrowExceptionWhenUserNotAuthorizedForAccount() {
    //     // Given
    //     User otherUser = new User("other", "other@example.com", "password");
    //     otherUser.setUserId(2L);
        
    //     BankAccount otherAccount = new BankAccount(otherUser, "Other Account", BigDecimal.ZERO);
    //     otherAccount.setId(2L);
        
    //     // When
    //     when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
    //     when(bankAccountRepository.findById(2L)).thenReturn(Optional.of(otherAccount));
        
    //     // Then
    //     RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(
    //         RuntimeException.class,
    //         () -> transactionService.getTransactionsByAccount(2L, "test@example.com", null)
    //     );
        
    //     assertEquals("Unauthorized to access this account", exception.getMessage());
    // }

    @Test
    @DisplayName("TS-1.12 - Should throw exception for invalid image")
    void shouldThrowExceptionForInvalidImage() {
        // Given
        org.springframework.web.multipart.MultipartFile invalidImageFile = 
            org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class);
        
        com.smartspend.transaction.dtos.CreateTransactionWithImageDto transactionDto = 
            new com.smartspend.transaction.dtos.CreateTransactionWithImageDto();
        transactionDto.setTitle("Test Transaction");
        transactionDto.setAmount(new BigDecimal("100.00"));
        transactionDto.setType(TransactionType.EXPENSE);
        transactionDto.setImageFile(invalidImageFile);
        
        // When
        when(imageUtils.isValidImage(invalidImageFile)).thenReturn(false);
        
        // Then
        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class,
            () -> transactionService.saveTransactionWithImage(transactionDto, "test@example.com")
        );
        
        assertEquals("Invalid image file", exception.getMessage());
    }

    @Test
    @DisplayName("TS-2.1 - Should update balance correctly when updating INCOME transaction")
    void updateTransactionShouldUpdateBalanceForIncome() {
        // Initial balance: 100, original transaction: INCOME 50, new amount: 80
        testAccount.setCurrentBalance(new BigDecimal("100"));
        Transaction originalTransaction = Transaction.builder()
            .id(1L)
            .title("Original Income")
            .amount(new BigDecimal("50"))
            .type(TransactionType.INCOME)
            .account(testAccount)
            .category(testCategory)
            .build();

        CreateTransactionDto dto = new CreateTransactionDto(
            "Updated Income", "Updated description", new BigDecimal("80"),
            TransactionType.INCOME, LocalDate.now().plusDays(1), Recurrence.MONTHLY, 1L, 1L
        );

        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(originalTransaction));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(originalTransaction);
        
        TransactionResponseDto responseDto = new TransactionResponseDto(
            1L, "Updated Income", "Updated description", new BigDecimal("80"),
            LocalDate.now().plusDays(1), TransactionType.INCOME, Recurrence.MONTHLY, 
            1L, "Test Account", testCategory, false, null, null, null
        );
        when(transactionMapper.toResponseDto(any(Transaction.class))).thenReturn(responseDto);

        Optional<TransactionResponseDto> result = transactionService.updateTransaction(1L, dto, "test@example.com");

        // Balance should be: 100 - 50 (remove old) + 80 (add new) = 130
        assertEquals(new BigDecimal("130"), testAccount.getCurrentBalance());
        
        // Verify all fields are updated
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        
        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals("Updated Income", savedTransaction.getTitle());
        assertEquals("Updated description", savedTransaction.getDescription());
        assertEquals(new BigDecimal("80"), savedTransaction.getAmount());
        assertEquals(TransactionType.INCOME, savedTransaction.getType());
        assertEquals(Recurrence.MONTHLY, savedTransaction.getRecurrence());
        assertEquals(testCategory, savedTransaction.getCategory());
        
        // Verify response
        assertNotNull(result);
        assertEquals("Updated Income", result.get().title());
        assertEquals("Updated description", result.get().description());
        assertEquals(new BigDecimal("80"), result.get().amount());
    }

    @Test
    @DisplayName("TS-2.2 - Should update balance correctly when updating EXPENSE transaction")
    void updateTransactionShouldUpdateBalanceForExpense() {
        // Initial balance: 100, original transaction: EXPENSE 30, new amount: 10
        testAccount.setCurrentBalance(new BigDecimal("100"));
        Transaction originalTransaction = Transaction.builder()
            .id(2L)
            .title("Original Expense")
            .description("Original description")
            .amount(new BigDecimal("30"))
            .type(TransactionType.EXPENSE)
            .date(LocalDate.now().minusDays(1))
            .recurrence(Recurrence.NONE)
            .account(testAccount)
            .category(testCategory)
            .build();

        CreateTransactionDto dto = new CreateTransactionDto(
            "Updated Expense", "Updated expense description", new BigDecimal("10"),
            TransactionType.EXPENSE, LocalDate.now(), Recurrence.WEEKLY, 1L, 1L
        );

        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.findById(2L)).thenReturn(Optional.of(originalTransaction));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(originalTransaction);
        
        TransactionResponseDto responseDto = new TransactionResponseDto(
            2L, "Updated Expense", "Updated expense description", new BigDecimal("10"),
            LocalDate.now(), TransactionType.EXPENSE, Recurrence.WEEKLY,
            1L, "Test Account", testCategory, false, null, null, null
        );
        when(transactionMapper.toResponseDto(any(Transaction.class))).thenReturn(responseDto);

        Optional<TransactionResponseDto> result = transactionService.updateTransaction(2L, dto, "test@example.com");

        // Balance should be: 100 + 30 (restore old) - 10 (subtract new) = 120
        assertEquals(new BigDecimal("120"), testAccount.getCurrentBalance());
        
        // Verify all fields are updated
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        
        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals("Updated Expense", savedTransaction.getTitle());
        assertEquals("Updated expense description", savedTransaction.getDescription());
        assertEquals(new BigDecimal("10"), savedTransaction.getAmount());
        assertEquals(TransactionType.EXPENSE, savedTransaction.getType());
        assertEquals(LocalDate.now(), savedTransaction.getDate());
        assertEquals(Recurrence.WEEKLY, savedTransaction.getRecurrence());
        assertEquals(testCategory, savedTransaction.getCategory());
        
        // Verify response
        assertNotNull(result);
        assertEquals("Updated Expense", result.get().title());
        assertEquals("Updated expense description", result.get().description());
        assertEquals(new BigDecimal("10"), result.get().amount());
        assertEquals(Recurrence.WEEKLY, result.get().recurrence());
    }

    @Test
    @DisplayName("TS-2.3 - Should update balance when changing transaction type from INCOME to EXPENSE")
    void updateTransactionShouldUpdateBalanceWhenChangingIncomeToExpense() {
        // Initial balance: 100, original transaction: INCOME 50, new: EXPENSE 30
        testAccount.setCurrentBalance(new BigDecimal("100"));
        Transaction originalTransaction = Transaction.builder()
            .id(3L)
            .title("Income Transaction")
            .amount(new BigDecimal("50"))
            .type(TransactionType.INCOME)
            .account(testAccount)
            .category(testCategory)
            .build();

        CreateTransactionDto dto = new CreateTransactionDto(
            "Now Expense", "Changed to expense", new BigDecimal("30"),
            TransactionType.EXPENSE, LocalDate.now(), Recurrence.NONE, 1L, 1L
        );

        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.findById(3L)).thenReturn(Optional.of(originalTransaction));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(originalTransaction);
        
        TransactionResponseDto responseDto = new TransactionResponseDto(
            3L, "Now Expense", "Changed to expense", new BigDecimal("30"),
            LocalDate.now(), TransactionType.EXPENSE, Recurrence.NONE,
            1L, "Test Account", testCategory, false, null, null, null
        );
        when(transactionMapper.toResponseDto(any(Transaction.class))).thenReturn(responseDto);

        transactionService.updateTransaction(3L, dto, "test@example.com");

        // Balance should be: 100 - 50 (remove old income) - 30 (subtract new expense) = 20
        assertEquals(new BigDecimal("20"), testAccount.getCurrentBalance());
        
        // Verify type change
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        
        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals(TransactionType.EXPENSE, savedTransaction.getType());
        assertEquals("Now Expense", savedTransaction.getTitle());
    }

    @Test
    @DisplayName("TS-2.4 - Should update balance when changing transaction type from EXPENSE to INCOME")
    void updateTransactionShouldUpdateBalanceWhenChangingExpenseToIncome() {
        // Initial balance: 100, original transaction: EXPENSE 40, new: INCOME 60
        testAccount.setCurrentBalance(new BigDecimal("100"));
        Transaction originalTransaction = Transaction.builder()
            .id(4L)
            .title("Expense Transaction")
            .amount(new BigDecimal("40"))
            .type(TransactionType.EXPENSE)
            .account(testAccount)
            .category(testCategory)
            .build();

        CreateTransactionDto dto = new CreateTransactionDto(
            "Now Income", "Changed to income", new BigDecimal("60"),
            TransactionType.INCOME, LocalDate.now(), Recurrence.NONE, 1L, 1L
        );

        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.findById(4L)).thenReturn(Optional.of(originalTransaction));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(originalTransaction);
        
        TransactionResponseDto responseDto = new TransactionResponseDto(
            4L, "Now Income", "Changed to income", new BigDecimal("60"),
            LocalDate.now(), TransactionType.INCOME, Recurrence.NONE,
            1L, "Test Account", testCategory, false, null, null, null
        );
        when(transactionMapper.toResponseDto(any(Transaction.class))).thenReturn(responseDto);

        transactionService.updateTransaction(4L, dto, "test@example.com");

        // Balance should be: 100 + 40 (restore old expense) + 60 (add new income) = 200
        assertEquals(new BigDecimal("200"), testAccount.getCurrentBalance());
        
        // Verify type change
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        
        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals(TransactionType.INCOME, savedTransaction.getType());
        assertEquals("Now Income", savedTransaction.getTitle());
    }
}
