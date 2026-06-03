package com.smartspend.unit.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.bankAccount.BankAccountRepository;
import com.smartspend.category.Category;
import com.smartspend.category.CategoryRepository;
import com.smartspend.config.DataLoader;
import com.smartspend.transaction.Transaction;
import com.smartspend.transaction.TransactionRepository;
import com.smartspend.transaction.TransactionType;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

class DataLoaderTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    private DataLoader dataLoader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dataLoader = new DataLoader(categoryRepository, userRepository, passwordEncoder, transactionRepository, bankAccountRepository);
    }

    @Test
    void shouldLoadInitialDataWhenNoBankAccountsExist() throws Exception {
        when(bankAccountRepository.count()).thenReturn(0L);
        when(categoryRepository.count()).thenReturn(100L);
        when(transactionRepository.count()).thenReturn(200L);
        when(userRepository.count()).thenReturn(1L);
        when(passwordEncoder.encode("administrator")).thenReturn("encoded-password");

        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(categoryRepository.findByName("Deportes")).thenReturn(null);

        dataLoader.run();

        verify(passwordEncoder).encode("administrator");
        verify(userRepository).save(any(User.class));
        verify(bankAccountRepository).save(any(BankAccount.class));
        verify(categoryRepository, atLeast(10)).save(any(Category.class));
        verify(transactionRepository, atLeast(20)).save(any(Transaction.class));
        verify(categoryRepository).findByName("Deportes");
    }

    @Test
    void shouldSkipInitialDataWhenBankAccountsAlreadyExistAndSportsCategoryExists() throws Exception {
        when(bankAccountRepository.count()).thenReturn(2L);
        when(categoryRepository.findByName("Deportes"))
            .thenReturn(new Category("Deportes", "desc", "#0ea5e9", TransactionType.EXPENSE, "🏃"));

        dataLoader.run();

        verify(categoryRepository, never()).save(any(Category.class));
        verify(userRepository, never()).save(any(User.class));
        verify(bankAccountRepository, never()).save(any(BankAccount.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}
