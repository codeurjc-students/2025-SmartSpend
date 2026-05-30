package com.smartspend.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;

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
import com.smartspend.transaction.Debt;
import com.smartspend.transaction.DebtRepository;
import com.smartspend.transaction.Transaction;
import com.smartspend.transaction.TransactionMapper;
import com.smartspend.transaction.TransactionRepository;
import com.smartspend.transaction.TransactionService;
import com.smartspend.transaction.TransactionType;
import com.smartspend.transaction.dtos.CreateTransactionDto;
import com.smartspend.transaction.dtos.TransactionResponseDto;
import com.smartspend.transaction.dtos.TransferRequestDto;
import com.smartspend.transaction.dtos.TransferResponseDto;
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
    private DebtRepository debtRepository;
    
    @Mock
    private ImageUtils imageUtils;
    
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private BankAccount testAccount;
    private Category testCategory;
    private Category transferExpenseCategory;
    private Category transferIncomeCategory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User("testuser", "test@example.com", "hashedpassword");
        testUser.setUserId(1L);
        
        testAccount = new BankAccount(testUser, "Test Account", new BigDecimal("1000.00"));
        testAccount.setId(1L);
        
        testCategory = new Category("Salary", "Income from work", "#27ae60", TransactionType.INCOME, null);
        testCategory.setId(1L);

        transferExpenseCategory = new Category("Traspaso (Salida)", "Transfer out", "#6c757d", TransactionType.EXPENSE, "transfer");
        transferExpenseCategory.setId(100L);

        transferIncomeCategory = new Category("Traspaso (Entrada)", "Transfer in", "#6c757d", TransactionType.INCOME, "transfer");
        transferIncomeCategory.setId(101L);
    }

    private void mockTransferDefaultCategories() {
        when(categoryRepository.findByName("Traspaso (Salida)"))
            .thenReturn(transferExpenseCategory);
        when(categoryRepository.findByName("Traspaso (Entrada)"))
            .thenReturn(transferIncomeCategory);
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
            1L,
            null, null, null
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
            1L, "Salary Payment", "Monthly salary", transactionAmount, null, null,
            LocalDate.now(), TransactionType.INCOME, Recurrence.NONE,
            1L, "Test Account", testCategory, false, null, false, null, null, null
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
            1L,
            null, null, null
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
            1L, "Grocery Shopping", "Weekly groceries", transactionAmount, null, null,
            LocalDate.now(), TransactionType.EXPENSE, Recurrence.NONE,
            1L, "Test Account", testCategory, false, null, false, null, null, null
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
    @DisplayName("TS-2.1 - Should delete adjustments when main transaction is deleted")
    void shouldDeleteAdjustmentsWhenMainTransactionIsDeleted() {
        // Given
        BigDecimal initialBalance = new BigDecimal("1000.00");
        BigDecimal mainAmount = new BigDecimal("100.00");
        BigDecimal debtAmount = new BigDecimal("40.00");
        
        // El balance inicial ya refleja el gasto principal (-100) y el cobro (+40) = 940
        testAccount.setCurrentBalance(new BigDecimal("940.00"));
        
        // Deuda ya pagada
        Debt paidDebt = Debt.builder()
            .id(1L)
            .name("Juan")
            .amount(debtAmount)
            .isPaid(true)
            .build();
            
        Transaction mainTransaction = Transaction.builder()
            .id(100L)
            .title("Dinner")
            .amount(mainAmount)
            .type(TransactionType.EXPENSE)
            .account(testAccount)
            .sharedDebts(List.of(paidDebt))
            .build();
            
        Transaction adjustment = Transaction.builder()
            .id(200L)
            .title("Ajuste deuda: Juan")
            .amount(debtAmount)
            .type(TransactionType.INCOME)
            .account(testAccount)
            .excludeFromStats(true)
            .build();
            
        // When
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findById(100L)).thenReturn(Optional.of(mainTransaction));
        when(transactionRepository.findAdjustments(testAccount.getId(), "Ajuste deuda: Juan", debtAmount))
            .thenReturn(List.of(adjustment));
            
        transactionService.deleteTransaction(100L, "test@example.com");
        
        // Then
        ArgumentCaptor<BankAccount> accountCaptor = ArgumentCaptor.forClass(BankAccount.class);
        verify(bankAccountRepository).save(accountCaptor.capture());
        
        // Balance final: 
        // 940 (inicial) 
        // + 100 (revertir gasto principal) 
        // - 40 (revertir ajuste de deuda) 
        // = 1000
        assertEquals(0, new BigDecimal("1000.00").compareTo(accountCaptor.getValue().getCurrentBalance()),
            "Balance should revert both main transaction and its debt adjustments");
            
        verify(transactionRepository).delete(mainTransaction);
        verify(transactionRepository).delete(adjustment);
    }

    @Test
    @DisplayName("TS-2.2 - Should restore debt status when adjustment is deleted")
    void shouldRestoreDebtStatusWhenAdjustmentIsDeleted() {
        // Given
        BigDecimal initialBalance = new BigDecimal("1000.00");
        BigDecimal adjustmentAmount = new BigDecimal("50.00");
        
        testAccount.setCurrentBalance(initialBalance);
        
        Transaction adjustment = Transaction.builder()
            .id(200L)
            .title("Ajuste deuda: Maria")
            .amount(adjustmentAmount)
            .type(TransactionType.INCOME)
            .account(testAccount)
            .excludeFromStats(true)
            .build();
            
        Debt debt = Debt.builder()
            .id(1L)
            .name("Maria")
            .amount(adjustmentAmount)
            .isPaid(true) // Inicialmente pagada
            .build();
            
        // When
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findById(200L)).thenReturn(Optional.of(adjustment));
        when(debtRepository.findByTransaction_Account_IdAndNameAndAmount(testAccount.getId(), "Maria", adjustmentAmount))
            .thenReturn(Optional.of(debt));
            
        transactionService.deleteAdjustmentAndRestoreDebt(200L, "test@example.com");
        
        // Then
        // 1. Verificar balance: 1000 - 50 = 950
        ArgumentCaptor<BankAccount> accountCaptor = ArgumentCaptor.forClass(BankAccount.class);
        verify(bankAccountRepository).save(accountCaptor.capture());
        assertEquals(0, new BigDecimal("950.00").compareTo(accountCaptor.getValue().getCurrentBalance()));
        
        // 2. Verificar que la deuda vuelve a estar pendiente
        assertEquals(false, debt.getIsPaid(), "Debt should be marked as UNPAID after deleting adjustment");
        verify(debtRepository).save(debt);
        
        // 3. Verificar borrado de la transacción de ajuste
        verify(transactionRepository).delete(adjustment);
    }

    @Test
    @DisplayName("TS-2.3 - Should increase balance when marking debt as paid")
    void shouldIncreaseBalanceWhenMarkingDebtAsPaid() {
        // Given
        BigDecimal initialBalance = new BigDecimal("1000.00");
        BigDecimal debtAmount = new BigDecimal("45.50");
        BigDecimal expectedBalance = initialBalance.add(debtAmount);
        
        testAccount.setCurrentBalance(initialBalance);
        
        Transaction mainTransaction = Transaction.builder()
            .id(100L)
            .title("Dinner")
            .amount(new BigDecimal("150.00"))
            .type(TransactionType.EXPENSE)
            .account(testAccount)
            .category(testCategory)
            .build();
            
        Debt debt = Debt.builder()
            .id(1L)
            .name("User A")
            .amount(debtAmount)
            .isPaid(false)
            .transaction(mainTransaction)
            .build();
            
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findById(100L)).thenReturn(Optional.of(mainTransaction));
        when(debtRepository.findById(1L)).thenReturn(Optional.of(debt));
        when(transactionMapper.toResponseDto(any(Transaction.class))).thenReturn(null); // No nos importa el retorno en este test
        
        // When
        transactionService.markDebtAsPaid(100L, 1L, "test@example.com");
        
        // Then
        // 1. Verificar balance de la cuenta
        ArgumentCaptor<BankAccount> accountCaptor = ArgumentCaptor.forClass(BankAccount.class);
        verify(bankAccountRepository).save(accountCaptor.capture());
        assertEquals(0, expectedBalance.compareTo(accountCaptor.getValue().getCurrentBalance()), 
            "Balance should increase by debt amount when marked as paid");
            
        // 2. Verificar que se crea la transacción de ajuste como INCOME y EXCLUDE_FROM_STATS
        ArgumentCaptor<Transaction> transCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transCaptor.capture());
        Transaction adjustment = transCaptor.getValue();
        
        assertEquals(TransactionType.INCOME, adjustment.getType());
        assertEquals(debtAmount, adjustment.getAmount());
        assertEquals(true, adjustment.getExcludeFromStats());
        assertEquals("Ajuste deuda: User A", adjustment.getTitle());
        
        // 3. Verificar que la deuda se marca como pagada
        assertEquals(true, debt.getIsPaid());
        verify(debtRepository).save(debt);
    }

    @Test
    @DisplayName("TS-2.4 - Should NOT double increase balance if debt is already paid")
    void shouldNotDoubleIncreaseBalanceIfAlreadyPaid() {
        // Given
        BigDecimal initialBalance = new BigDecimal("1000.00");
        testAccount.setCurrentBalance(initialBalance);
        
        Transaction mainTransaction = Transaction.builder()
            .id(100L)
            .title("Dinner")
            .amount(new BigDecimal("150.00"))
            .type(TransactionType.EXPENSE)
            .account(testAccount)
            .build();
            
        Debt debt = Debt.builder()
            .id(1L)
            .name("User A")
            .amount(new BigDecimal("50.00"))
            .isPaid(true) // YA PAGADA
            .transaction(mainTransaction)
            .build();
            
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findById(100L)).thenReturn(Optional.of(mainTransaction));
        when(debtRepository.findById(1L)).thenReturn(Optional.of(debt));
        when(transactionMapper.toResponseDto(any(Transaction.class))).thenReturn(null);
        
        // When
        transactionService.markDebtAsPaid(100L, 1L, "test@example.com");
        
        // Then
        // No debería guardarse la cuenta ni el repositorio de transacciones de nuevo
        verify(bankAccountRepository, org.mockito.Mockito.never()).save(any(BankAccount.class));
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
            1L,
            null, null, null
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
            1L, "Test Transaction", "Test description", new BigDecimal("100.00"), null, null,
            today, TransactionType.EXPENSE, Recurrence.NONE,
            1L, "Test Account", testCategory, false, null, false, null, null, null
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
            1L,
            null, null, null
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
            1L,
            null, null, null
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
            999L,
            null, null, null
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
            TransactionType.INCOME, LocalDate.now().plusDays(1), Recurrence.MONTHLY, 1L, 1L,
            null, null, null
        );

        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(originalTransaction));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(originalTransaction);
        
        TransactionResponseDto responseDto = new TransactionResponseDto(
            1L, "Updated Income", "Updated description", new BigDecimal("80"), null, null,
            LocalDate.now().plusDays(1), TransactionType.INCOME, Recurrence.MONTHLY,
            1L, "Test Account", testCategory, false, null, false, null, null, null
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
            TransactionType.EXPENSE, LocalDate.now(), Recurrence.WEEKLY, 1L, 1L,
            null, null, null
        );

        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.findById(2L)).thenReturn(Optional.of(originalTransaction));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(originalTransaction);
        
        TransactionResponseDto responseDto = new TransactionResponseDto(
            2L, "Updated Expense", "Updated expense description", new BigDecimal("10"), null, null,
            LocalDate.now(), TransactionType.EXPENSE, Recurrence.WEEKLY,
            1L, "Test Account", testCategory, false, null, false, null, null, null
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
            TransactionType.EXPENSE, LocalDate.now(), Recurrence.NONE, 1L, 1L,
            null, null, null
        );

        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.findById(3L)).thenReturn(Optional.of(originalTransaction));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(originalTransaction);
        
        TransactionResponseDto responseDto = new TransactionResponseDto(
            3L, "Now Expense", "Changed to expense", new BigDecimal("30"), null, null,
            LocalDate.now(), TransactionType.EXPENSE, Recurrence.NONE,
            1L, "Test Account", testCategory, false, null, false, null, null, null
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
            TransactionType.INCOME, LocalDate.now(), Recurrence.NONE, 1L, 1L,
            null, null, null
        );

        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.findById(4L)).thenReturn(Optional.of(originalTransaction));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(originalTransaction);
        
        TransactionResponseDto responseDto = new TransactionResponseDto(
            4L, "Now Income", "Changed to income", new BigDecimal("60"), null, null,
            LocalDate.now(), TransactionType.INCOME, Recurrence.NONE,
            1L, "Test Account", testCategory, false, null, false, null, null, null
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

    // ===============================================
    // TESTS PARA FUNCIONALIDAD DE TRANSFERENCIAS
    // ===============================================

    @Test
    @DisplayName("TR-1 - Should create both transfer transactions and update both balances")
    void shouldCreateBothTransferTransactionsAndUpdateBothBalances() {
        // Given
        BankAccount destinationAccount = new BankAccount(testUser, "Savings", new BigDecimal("300.00"));
        destinationAccount.setId(2L);

        TransferRequestDto request = new TransferRequestDto(
            1L,
            2L,
            new BigDecimal("125.50"),
            "Rent reserve",
            LocalDate.of(2026, 5, 28),
            "Monthly reserve transfer",
            Recurrence.MONTHLY
        );

        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(bankAccountRepository.findById(2L)).thenReturn(Optional.of(destinationAccount));
        mockTransferDefaultCategories();
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            if (tx.getId() == null) {
                tx.setId(tx.getType() == TransactionType.EXPENSE ? 101L : 102L);
            }
            return tx;
        });

        // When
        TransferResponseDto response = transactionService.createTransfers(request, "test@example.com");

        // Then
        assertNotNull(response);
        assertEquals(101L, response.originTransactionId());
        assertEquals(102L, response.destinationTransactionId());
        assertEquals(0, new BigDecimal("125.50").compareTo(response.amount()));
        assertEquals(LocalDate.of(2026, 5, 28), response.date());

        assertEquals(0, new BigDecimal("874.50").compareTo(testAccount.getCurrentBalance()));
        assertEquals(0, new BigDecimal("425.50").compareTo(destinationAccount.getCurrentBalance()));

        verify(bankAccountRepository, org.mockito.Mockito.times(2)).save(any(BankAccount.class));

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, org.mockito.Mockito.times(2)).save(transactionCaptor.capture());

        List<Transaction> savedTransactions = transactionCaptor.getAllValues();
        Transaction originTransaction = savedTransactions.get(0);
        Transaction destinationTransaction = savedTransactions.get(1);

        assertEquals(TransactionType.EXPENSE, originTransaction.getType());
        assertEquals("Traspaso salida", originTransaction.getTitle());
        assertEquals(true, originTransaction.getExcludeFromStats());
        assertEquals(true, originTransaction.getIsRecurringSeriesParent());
        assertEquals(LocalDate.of(2026, 6, 28), originTransaction.getNextRecurrenceDate());
        assertEquals(transferExpenseCategory, originTransaction.getCategory());

        assertEquals(TransactionType.INCOME, destinationTransaction.getType());
        assertEquals("Traspaso entrada", destinationTransaction.getTitle());
        assertEquals(true, destinationTransaction.getExcludeFromStats());
        assertEquals(true, destinationTransaction.getIsRecurringSeriesParent());
        assertEquals(LocalDate.of(2026, 6, 28), destinationTransaction.getNextRecurrenceDate());
        assertEquals(transferIncomeCategory, destinationTransaction.getCategory());
    }

    @Test
    @DisplayName("TR-2 - Should default date and recurrence for transfer when omitted")
    void shouldDefaultDateAndRecurrenceForTransferWhenOmitted() {
        // Given
        BankAccount destinationAccount = new BankAccount(testUser, "Savings", new BigDecimal("300.00"));
        destinationAccount.setId(2L);

        TransferRequestDto request = new TransferRequestDto(
            1L,
            2L,
            new BigDecimal("10.00"),
            "Wallet top-up",
            null,
            "",
            null
        );

        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(bankAccountRepository.findById(2L)).thenReturn(Optional.of(destinationAccount));
        mockTransferDefaultCategories();
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDate executionDate = LocalDate.now();

        // When
        TransferResponseDto response = transactionService.createTransfers(request, "test@example.com");

        // Then
        assertEquals(executionDate, response.date());

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, org.mockito.Mockito.times(2)).save(transactionCaptor.capture());

        for (Transaction savedTransaction : transactionCaptor.getAllValues()) {
            assertEquals(Recurrence.NONE, savedTransaction.getRecurrence());
            assertEquals(false, savedTransaction.getIsRecurringSeriesParent());
            assertEquals(null, savedTransaction.getNextRecurrenceDate());
        }
    }

    @Test
    @DisplayName("TR-3 - Should throw exception when transfer user does not exist")
    void shouldThrowExceptionWhenTransferUserDoesNotExist() {
        // Given
        TransferRequestDto request = new TransferRequestDto(
            1L,
            2L,
            new BigDecimal("10.00"),
            "Transfer",
            LocalDate.now(),
            "",
            Recurrence.NONE
        );

        when(userRepository.findByUserEmail("missing@example.com")).thenReturn(Optional.empty());

        // When / Then
        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class,
            () -> transactionService.createTransfers(request, "missing@example.com")
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @DisplayName("TR-4 - Should throw exception when origin account does not exist")
    void shouldThrowExceptionWhenOriginAccountDoesNotExist() {
        // Given
        TransferRequestDto request = new TransferRequestDto(
            999L,
            2L,
            new BigDecimal("10.00"),
            "Transfer",
            LocalDate.now(),
            "",
            Recurrence.NONE
        );

        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(999L)).thenReturn(Optional.empty());

        // When / Then
        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class,
            () -> transactionService.createTransfers(request, "test@example.com")
        );

        assertEquals("Origin account not found", exception.getMessage());
    }

    @Test
    @DisplayName("TR-5 - Should throw exception when destination account does not exist")
    void shouldThrowExceptionWhenDestinationAccountDoesNotExist() {
        // Given
        TransferRequestDto request = new TransferRequestDto(
            1L,
            999L,
            new BigDecimal("10.00"),
            "Transfer",
            LocalDate.now(),
            "",
            Recurrence.NONE
        );

        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(bankAccountRepository.findById(999L)).thenReturn(Optional.empty());
        mockTransferDefaultCategories();

        // When / Then
        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(
            RuntimeException.class,
            () -> transactionService.createTransfers(request, "test@example.com")
        );

        assertEquals("Destination account not found", exception.getMessage());
    }

    @Test
    @DisplayName("TR-6 - Should capture beforeBalance for both transfer transactions")
    void shouldCaptureBeforeBalanceForBothTransferTransactions() {
        // Given
        testAccount.setCurrentBalance(new BigDecimal("1000.00"));

        BankAccount destinationAccount = new BankAccount(testUser, "Savings", new BigDecimal("300.00"));
        destinationAccount.setId(2L);

        TransferRequestDto request = new TransferRequestDto(
            1L,
            2L,
            new BigDecimal("50.00"),
            "Transfer",
            LocalDate.of(2026, 5, 28),
            "",
            Recurrence.NONE
        );

        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(bankAccountRepository.findById(2L)).thenReturn(Optional.of(destinationAccount));
        mockTransferDefaultCategories();
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        transactionService.createTransfers(request, "test@example.com");

        // Then
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, org.mockito.Mockito.times(2)).save(transactionCaptor.capture());

        List<Transaction> savedTransactions = transactionCaptor.getAllValues();
        Transaction originTransaction = savedTransactions.get(0);
        Transaction destinationTransaction = savedTransactions.get(1);

        assertEquals(0, new BigDecimal("1000.00").compareTo(originTransaction.getBeforeBalance()));
        assertEquals(0, new BigDecimal("300.00").compareTo(destinationTransaction.getBeforeBalance()));
    }

    // ===============================================
    // TESTS PARA FUNCIONALIDAD DE RECURRENCIA
    // ===============================================

    @Test
    @DisplayName("R-1: calculateNextRecurrenceDate - Should calculate daily recurrence correctly")
    void shouldCalculateDailyRecurrenceCorrectly() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2025, 2, 15);
        
        // When - Using reflection to access private method
        java.lang.reflect.Method method = TransactionService.class.getDeclaredMethod(
            "calculateNextRecurrenceDate", LocalDate.class, Recurrence.class);
        method.setAccessible(true);
        LocalDate result = (LocalDate) method.invoke(transactionService, startDate, Recurrence.DAILY);
        
        // Then
        assertEquals(LocalDate.of(2025, 2, 16), result);
    }

    @Test
    @DisplayName("R-2: calculateNextRecurrenceDate - Should calculate weekly recurrence correctly") 
    void shouldCalculateWeeklyRecurrenceCorrectly() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2025, 2, 15); // Saturday
        
        // When
        java.lang.reflect.Method method = TransactionService.class.getDeclaredMethod(
            "calculateNextRecurrenceDate", LocalDate.class, Recurrence.class);
        method.setAccessible(true);
        LocalDate result = (LocalDate) method.invoke(transactionService, startDate, Recurrence.WEEKLY);
        
        // Then
        assertEquals(LocalDate.of(2025, 2, 22), result); // Next Saturday
    }

    @Test
    @DisplayName("R-3: calculateNextRecurrenceDate - Should calculate monthly recurrence correctly")
    void shouldCalculateMonthlyRecurrenceCorrectly() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2025, 2, 15);
        
        // When
        java.lang.reflect.Method method = TransactionService.class.getDeclaredMethod(
            "calculateNextRecurrenceDate", LocalDate.class, Recurrence.class);
        method.setAccessible(true);
        LocalDate result = (LocalDate) method.invoke(transactionService, startDate, Recurrence.MONTHLY);
        
        // Then
        assertEquals(LocalDate.of(2025, 3, 15), result);
    }

    @Test
    @DisplayName("R-4: calculateNextRecurrenceDate - Should calculate yearly recurrence correctly")
    void shouldCalculateYearlyRecurrenceCorrectly() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2025, 2, 15);
        
        // When
        java.lang.reflect.Method method = TransactionService.class.getDeclaredMethod(
            "calculateNextRecurrenceDate", LocalDate.class, Recurrence.class);
        method.setAccessible(true);
        LocalDate result = (LocalDate) method.invoke(transactionService, startDate, Recurrence.YEARLY);
        
        // Then
        assertEquals(LocalDate.of(2026, 2, 15), result);
    }

    @Test
    @DisplayName("R-5: calculateNextRecurrenceDate - Should return null for NONE recurrence")
    void shouldReturnNullForNoneRecurrence() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2025, 2, 15);
        
        // When
        java.lang.reflect.Method method = TransactionService.class.getDeclaredMethod(
            "calculateNextRecurrenceDate", LocalDate.class, Recurrence.class);
        method.setAccessible(true);
        LocalDate result = (LocalDate) method.invoke(transactionService, startDate, Recurrence.NONE);
        
        // Then
        assertEquals(null, result);
    }

    @Test
    @DisplayName("R-6: saveTransaction - Should set isRecurringSeriesParent to true for recurring transactions")
    void shouldSetRecurringSeriesParentForRecurringTransactions() {
        // Given
        CreateTransactionDto recurringTransactionDto = new CreateTransactionDto(
            "Monthly Salary",
            "Recurring salary payment", 
            new BigDecimal("2000.00"),
            TransactionType.INCOME,
            LocalDate.now(),
            Recurrence.MONTHLY,
            1L,
            1L,
            null, null, null
        );

        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionMapper.toResponseDto(any(Transaction.class))).thenReturn(
            new TransactionResponseDto(1L, "Monthly Salary", "Recurring salary payment",
            new BigDecimal("2000.00"), null, null, LocalDate.now(), TransactionType.INCOME, Recurrence.MONTHLY,
            1L, "Test Account", testCategory, false, null, true, null, null, null)
        );

        // When  
        TransactionResponseDto response = transactionService.saveTransaction(recurringTransactionDto, "test@example.com");

        // Then
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        
        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals(true, savedTransaction.getIsRecurringSeriesParent());
        assertEquals(Recurrence.MONTHLY, savedTransaction.getRecurrence());
        assertNotNull(savedTransaction.getNextRecurrenceDate());
    }

    @Test
    @DisplayName("R-7: saveTransaction - Should set isRecurringSeriesParent to false for non-recurring transactions")
    void shouldSetRecurringSeriesParentToFalseForNonRecurringTransactions() {
        // Given
        CreateTransactionDto nonRecurringTransactionDto = new CreateTransactionDto(
            "One-time expense",
            "Single payment", 
            new BigDecimal("50.00"),
            TransactionType.EXPENSE,
            LocalDate.now(),
            Recurrence.NONE,
            1L,
            1L,
            null, null, null
        );

        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionMapper.toResponseDto(any(Transaction.class))).thenReturn(
            new TransactionResponseDto(1L, "One-time expense", "Single payment",
            new BigDecimal("50.00"), null, null, LocalDate.now(), TransactionType.EXPENSE, Recurrence.NONE,
            1L, "Test Account", testCategory, false, null, false, null, null, null)
        );

        // When  
        TransactionResponseDto response = transactionService.saveTransaction(nonRecurringTransactionDto, "test@example.com");

        // Then
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        
        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals(false, savedTransaction.getIsRecurringSeriesParent());
        assertEquals(Recurrence.NONE, savedTransaction.getRecurrence());
        assertEquals(null, savedTransaction.getNextRecurrenceDate());
    }

    @Test
    @DisplayName("R-8: saveTransaction - Should calculate next recurrence date for different recurrence types")
    void shouldCalculateNextRecurrenceDateForDifferentTypes() {
        // Test for each recurrence type
        LocalDate testDate = LocalDate.of(2025, 2, 15);
        
        // Test DAILY
        testRecurrenceCalculation(testDate, Recurrence.DAILY, testDate.plusDays(1));
        
        // Test WEEKLY  
        testRecurrenceCalculation(testDate, Recurrence.WEEKLY, testDate.plusWeeks(1));
        
        // Test MONTHLY
        testRecurrenceCalculation(testDate, Recurrence.MONTHLY, testDate.plusMonths(1));
        
        // Test YEARLY
        testRecurrenceCalculation(testDate, Recurrence.YEARLY, testDate.plusYears(1));
    }
    
    private void testRecurrenceCalculation(LocalDate transactionDate, Recurrence recurrence, LocalDate expectedNextDate) {
        CreateTransactionDto recurringTransactionDto = new CreateTransactionDto(
            "Recurring Transaction",
            "Test recurrence", 
            new BigDecimal("100.00"),
            TransactionType.INCOME,
            transactionDate,
            recurrence,
            1L,
            1L,
            null, null, null
        );

        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionMapper.toResponseDto(any(Transaction.class))).thenReturn(
            new TransactionResponseDto(1L, "Recurring Transaction", "Test recurrence",
            new BigDecimal("100.00"), null, null, transactionDate, TransactionType.INCOME, recurrence,
            1L, "Test Account", testCategory, false, null, true, null, null, null)
        );

        // When  
        TransactionResponseDto response = transactionService.saveTransaction(recurringTransactionDto, "test@example.com");

        // Then
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, atLeastOnce()).save(transactionCaptor.capture());
        
        Transaction savedTransaction = transactionCaptor.getValue();
        assertEquals(expectedNextDate, savedTransaction.getNextRecurrenceDate());
    }
}
