package com.smartspend.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.bankAccount.BankAccountRepository;
import com.smartspend.bankAccount.BankAccountService;
import com.smartspend.bankAccount.dtos.CreateBankAccountDTO;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

public class BankAccountServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BankAccountService bankAccountService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User("testuser", "test@example.com", "hashedpassword");
        testUser.setUserId(1L);
    }

    @Test
    void shouldCreateAccountWithInitialBalance() {
        // Given
        BigDecimal initialBalance = new BigDecimal("1000.00");
        CreateBankAccountDTO accountDto = new CreateBankAccountDTO("Savings Account", initialBalance);
        
        BankAccount savedAccount = new BankAccount(testUser, "Savings Account", initialBalance);
        savedAccount.setId(1L);
        
        // When - Configure mocks
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(savedAccount);
        
        // When - Execute
        BankAccount result = bankAccountService.createBankAccount(accountDto, "test@example.com");
        
        // Then
        assertNotNull(result);
        assertEquals("Savings Account", result.getAccountName());
        assertEquals(0, initialBalance.compareTo(result.getCurrentBalance()));
        assertEquals(testUser, result.getUser());
        
        // Verify the account was saved with correct initial balance
        ArgumentCaptor<BankAccount> accountCaptor = ArgumentCaptor.forClass(BankAccount.class);
        verify(bankAccountRepository).save(accountCaptor.capture());
        
        BankAccount capturedAccount = accountCaptor.getValue();
        assertEquals(0, initialBalance.compareTo(capturedAccount.getCurrentBalance()));
    }

    @Test
    void shouldUseZeroBalanceWhenInitialBalanceIsNull() {
        // Given
        CreateBankAccountDTO accountDto = new CreateBankAccountDTO("Checking Account", null);
        
        BankAccount savedAccount = new BankAccount(testUser, "Checking Account", BigDecimal.ZERO);
        savedAccount.setId(1L);
        
        // When - Configure mocks
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(savedAccount);
        
        // When - Execute
        BankAccount result = bankAccountService.createBankAccount(accountDto, "test@example.com");
        
        // Then
        assertNotNull(result);
        assertEquals("Checking Account", result.getAccountName());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getCurrentBalance()));
        
        // Verify the account was saved with ZERO balance
        ArgumentCaptor<BankAccount> accountCaptor = ArgumentCaptor.forClass(BankAccount.class);
        verify(bankAccountRepository).save(accountCaptor.capture());
        
        BankAccount capturedAccount = accountCaptor.getValue();
        assertEquals(0, BigDecimal.ZERO.compareTo(capturedAccount.getCurrentBalance()));
    }

    @Test
    void shouldOnlyReturnUserOwnAccounts() {
        // Given
        BankAccount account1 = new BankAccount(testUser, "Account 1", new BigDecimal("100.00"));
        BankAccount account2 = new BankAccount(testUser, "Account 2", new BigDecimal("200.00"));
        List<BankAccount> userAccounts = List.of(account1, account2);
        
        // When - Configure mocks
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findByUser_UserId(1L)).thenReturn(userAccounts);
        
        // When - Execute
        List<BankAccount> result = bankAccountService.getUserBankAccountsByEmail("test@example.com");
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Account 1", result.get(0).getAccountName());
        assertEquals("Account 2", result.get(1).getAccountName());
        
        // Verify it called repository with correct user ID
        verify(bankAccountRepository).findByUser_UserId(1L);
    }

    @Test
    void shouldThrowExceptionWhenAccountNotBelongsToUser() {
        // Given
        User otherUser = new User("other", "other@example.com", "password");
        otherUser.setUserId(2L);
        
        BankAccount otherUserAccount = new BankAccount(otherUser, "Other Account", BigDecimal.ZERO);
        
        // When - Configure mocks
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findByIdAndUser_UserId(1L, 1L)).thenReturn(Optional.empty());
        
        // Then - Should throw exception
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> bankAccountService.getBankAccountByIdAndEmail(1L, "test@example.com")
        );
        
        assertEquals("Bank account not found", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given - User doesn't exist
        when(userRepository.findByUserEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        // Then - Should throw exception for createBankAccount
        RuntimeException exception1 = assertThrows(
            RuntimeException.class,
            () -> bankAccountService.createBankAccount(
                new CreateBankAccountDTO("Test Account", BigDecimal.ZERO), 
                "nonexistent@example.com"
            )
        );
        assertEquals("User not found", exception1.getMessage());
        
        // Then - Should throw exception for getUserBankAccountsByEmail
        RuntimeException exception2 = assertThrows(
            RuntimeException.class,
            () -> bankAccountService.getUserBankAccountsByEmail("nonexistent@example.com")
        );
        assertEquals("User not found", exception2.getMessage());
        
        // Then - Should throw exception for getBankAccountByIdAndEmail
        RuntimeException exception3 = assertThrows(
            RuntimeException.class,
            () -> bankAccountService.getBankAccountByIdAndEmail(1L, "nonexistent@example.com")
        );
        assertEquals("User not found", exception3.getMessage());
    }

    @Test
    void shouldDeleteBankAccountSuccessfully() {
        // Given
        BankAccount account = new BankAccount(testUser, "Test Account", BigDecimal.ZERO);
        account.setId(1L);
        
        // When - Execute
        bankAccountService.deleteBankAccount(account);
        
        // Then - Verify account was deleted
        verify(bankAccountRepository).delete(account);
    }

    @Test
    void shouldGetBankAccountByIdAndEmailSuccessfully() {
        // Given
        BankAccount account = new BankAccount(testUser, "Test Account", new BigDecimal("500.00"));
        account.setId(1L);
        
        // When - Configure mocks
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findByIdAndUser_UserId(1L, 1L)).thenReturn(Optional.of(account));
        
        // When - Execute
        BankAccount result = bankAccountService.getBankAccountByIdAndEmail(1L, "test@example.com");
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Account", result.getAccountName());
        assertEquals(0, new BigDecimal("500.00").compareTo(result.getCurrentBalance()));
        assertEquals(testUser, result.getUser());
    }

    @Test
    void shouldCreateAccountWithNegativeInitialBalance() {
        // Given - Account with negative balance (credit card or overdraft)
        BigDecimal negativeBalance = new BigDecimal("-100.00");
        CreateBankAccountDTO accountDto = new CreateBankAccountDTO("Credit Card", negativeBalance);
        
        BankAccount savedAccount = new BankAccount(testUser, "Credit Card", negativeBalance);
        savedAccount.setId(1L);
        
        // When - Configure mocks
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(savedAccount);
        
        // When - Execute
        BankAccount result = bankAccountService.createBankAccount(accountDto, "test@example.com");
        
        // Then
        assertNotNull(result);
        assertEquals("Credit Card", result.getAccountName());
        assertEquals(0, negativeBalance.compareTo(result.getCurrentBalance()));
        
        // Verify negative balance is preserved
        ArgumentCaptor<BankAccount> accountCaptor = ArgumentCaptor.forClass(BankAccount.class);
        verify(bankAccountRepository).save(accountCaptor.capture());
        
        BankAccount capturedAccount = accountCaptor.getValue();
        assertTrue(capturedAccount.getCurrentBalance().compareTo(BigDecimal.ZERO) < 0);
    }
}