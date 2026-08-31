package com.smartspend.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartspend.bankAccount.BankAccountRepository;
import com.smartspend.category.CategoryRepository;
import com.smartspend.transaction.DebtRepository;
import com.smartspend.transaction.TransactionRepository;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;
import com.smartspend.user.UserService;

import jakarta.persistence.EntityNotFoundException;

class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private DebtRepository debtRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private BankAccountRepository bankAccountRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User("juan", "juan@test.com", "hashed");
        testUser.setUserId(1L);
    }

    // --- acceptPrivacyPolicy ---

    @Test
    void shouldAcceptPrivacyPolicy() {
        when(userRepository.findByUserEmail("juan@test.com")).thenReturn(Optional.of(testUser));

        userService.acceptPrivacyPolicy("juan@test.com");

        assertTrue(testUser.getPrivacyPolicyAccepted());
        verify(userRepository).save(testUser);
    }

    @Test
    void shouldThrowWhenAcceptingPolicyForUnknownEmail() {
        when(userRepository.findByUserEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> userService.acceptPrivacyPolicy("ghost@test.com"));
    }

    // --- deleteAccount ---

    @Test
    void shouldDeleteAccountInCascadeOrder() {
        when(userRepository.findByUserEmail("juan@test.com")).thenReturn(Optional.of(testUser));

        userService.deleteAccount("juan@test.com");

        // Verify cascade order: debts → transactions → accounts → categories → user
        var order = inOrder(debtRepository, transactionRepository, bankAccountRepository, categoryRepository, userRepository);
        order.verify(debtRepository).deleteByTransaction_Account_User_UserId(1L);
        order.verify(transactionRepository).deleteByAccount_User_UserId(1L);
        order.verify(bankAccountRepository).deleteByUser_UserId(1L);
        order.verify(categoryRepository).deleteByUser_UserId(1L);
        order.verify(userRepository).delete(testUser);
    }

    @Test
    void shouldThrowWhenDeletingUnknownAccount() {
        when(userRepository.findByUserEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> userService.deleteAccount("ghost@test.com"));
    }
}
