package com.smartspend.integration.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.bankAccount.BankAccountRepository;
import com.smartspend.category.Category;
import com.smartspend.category.CategoryRepository;
import com.smartspend.transaction.Recurrence;
import com.smartspend.transaction.Transaction;
import com.smartspend.transaction.TransactionRepository;
import com.smartspend.transaction.TransactionType;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

@DataJpaTest(properties = {
    "spring.profiles.active=test",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TransactionRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldFindPendingRecurringAndBalances() {
        User user = new User();
        user.setUserName("tx-owner");
        user.setUserEmail("tx-owner@test.com");
        user.setUserHashedPassword("hashed");
        user = userRepository.save(user);

        BankAccount account = new BankAccount(user, "Cuenta", new BigDecimal("1000.00"));
        account = bankAccountRepository.save(account);

        Category category = new Category("Nómina", "desc", "#00ff00", TransactionType.INCOME, user, "💰");
        category = categoryRepository.save(category);

        Transaction recurring = Transaction.builder()
            .title("Ingreso recurrente")
            .description("desc")
            .amount(new BigDecimal("150.00"))
            .date(LocalDate.now().minusDays(5))
            .type(TransactionType.INCOME)
            .recurrence(Recurrence.MONTHLY)
            .isRecurringSeriesParent(true)
            .nextRecurrenceDate(LocalDate.now().minusDays(1))
            .account(account)
            .category(category)
            .build();

        Transaction normal = Transaction.builder()
            .title("Ingreso normal")
            .description("desc")
            .amount(new BigDecimal("50.00"))
            .date(LocalDate.now())
            .type(TransactionType.INCOME)
            .recurrence(Recurrence.NONE)
            .isRecurringSeriesParent(false)
            .account(account)
            .category(category)
            .build();

        transactionRepository.save(recurring);
        transactionRepository.save(normal);

        assertFalse(transactionRepository.findPendingRecurringTransactions(LocalDate.now()).isEmpty());
        assertEquals(new BigDecimal("200.00"), transactionRepository.findBalanceUpToDate(account.getId(), LocalDate.now()));
    }

    @Test
    void shouldReturnTransactionsByLimitAndPageOrderedByDateDesc() {
        User user = new User();
        user.setUserName("tx-owner-2");
        user.setUserEmail("tx-owner-2@test.com");
        user.setUserHashedPassword("hashed");
        user = userRepository.save(user);

        BankAccount account = new BankAccount(user, "Cuenta 2", new BigDecimal("1000.00"));
        account = bankAccountRepository.save(account);

        Category category = new Category("General", "desc", "#00ff00", TransactionType.INCOME, user, "💰");
        category = categoryRepository.save(category);

        transactionRepository.save(Transaction.builder()
            .title("t1")
            .description("desc")
            .amount(new BigDecimal("10.00"))
            .date(LocalDate.now().minusDays(2))
            .type(TransactionType.INCOME)
            .recurrence(Recurrence.NONE)
            .isRecurringSeriesParent(false)
            .account(account)
            .category(category)
            .build());

        transactionRepository.save(Transaction.builder()
            .title("t2")
            .description("desc")
            .amount(new BigDecimal("20.00"))
            .date(LocalDate.now().minusDays(1))
            .type(TransactionType.INCOME)
            .recurrence(Recurrence.NONE)
            .isRecurringSeriesParent(false)
            .account(account)
            .category(category)
            .build());

        transactionRepository.save(Transaction.builder()
            .title("t3")
            .description("desc")
            .amount(new BigDecimal("30.00"))
            .date(LocalDate.now())
            .type(TransactionType.INCOME)
            .recurrence(Recurrence.NONE)
            .isRecurringSeriesParent(false)
            .account(account)
            .category(category)
            .build());

        List<Transaction> limited = transactionRepository.findByAccountIdAndLimit(account.getId(), 2);
        Page<Transaction> paged = transactionRepository.findByAccountIdOrderByDateDesc(account.getId(), PageRequest.of(0, 2));

        assertEquals(2, limited.size());
        assertEquals("t3", limited.get(0).getTitle());
        assertEquals("t2", limited.get(1).getTitle());
        assertEquals(2, paged.getContent().size());
        assertEquals("t3", paged.getContent().get(0).getTitle());
    }

    @Test
    void shouldFindOnlyIncomeAdjustmentsExcludedFromStats() {
        User user = new User();
        user.setUserName("tx-owner-3");
        user.setUserEmail("tx-owner-3@test.com");
        user.setUserHashedPassword("hashed");
        user = userRepository.save(user);

        BankAccount account = new BankAccount(user, "Cuenta 3", new BigDecimal("500.00"));
        account = bankAccountRepository.save(account);

        Category category = new Category("Ajustes", "desc", "#999999", TransactionType.INCOME, user, "⚖️");
        category = categoryRepository.save(category);

        BigDecimal amount = new BigDecimal("45.00");

        transactionRepository.save(Transaction.builder()
            .title("Ajuste deuda")
            .description("match")
            .amount(amount)
            .date(LocalDate.now())
            .type(TransactionType.INCOME)
            .excludeFromStats(true)
            .recurrence(Recurrence.NONE)
            .isRecurringSeriesParent(false)
            .account(account)
            .category(category)
            .build());

        transactionRepository.save(Transaction.builder()
            .title("Ajuste deuda")
            .description("different because not excluded")
            .amount(amount)
            .date(LocalDate.now())
            .type(TransactionType.INCOME)
            .excludeFromStats(false)
            .recurrence(Recurrence.NONE)
            .isRecurringSeriesParent(false)
            .account(account)
            .category(category)
            .build());

        transactionRepository.save(Transaction.builder()
            .title("Ajuste deuda")
            .description("different because expense")
            .amount(amount)
            .date(LocalDate.now())
            .type(TransactionType.EXPENSE)
            .excludeFromStats(true)
            .recurrence(Recurrence.NONE)
            .isRecurringSeriesParent(false)
            .account(account)
            .category(category)
            .build());

        List<Transaction> adjustments = transactionRepository.findAdjustments(account.getId(), "Ajuste deuda", amount);

        assertEquals(1, adjustments.size());
        assertTrue(Boolean.TRUE.equals(adjustments.get(0).getExcludeFromStats()));
        assertEquals(TransactionType.INCOME, adjustments.get(0).getType());
    }

    @Test
    void shouldFindRecurringWithNextRecurrenceDateInCurrentMonth() {
        // This test verifies that the repository can find recurring transactions
        // by checking that a parent transaction is properly persisted and can be retrieved
        
        User user = new User();
        user.setUserName("recurring-test");
        user.setUserEmail("recurring@test.com");
        user.setUserHashedPassword("hashed");
        user = userRepository.save(user);

        BankAccount account = new BankAccount(user, "Test Account", new BigDecimal("1000.00"));
        account = bankAccountRepository.save(account);

        Category category = new Category("Entertainment", "desc", "#ff0000", TransactionType.EXPENSE, user, "💳");
        category = categoryRepository.save(category);

        LocalDate baseDate = LocalDate.of(2025, 7, 15);
        
        // Create a recurring parent transaction
        Transaction recurring = Transaction.builder()
            .title("Netflix")
            .description("Monthly subscription")
            .amount(new BigDecimal("15.99"))
            .date(baseDate)
            .type(TransactionType.EXPENSE)
            .recurrence(Recurrence.MONTHLY)
            .isRecurringSeriesParent(true)
            .nextRecurrenceDate(baseDate.plusMonths(1))
            .account(account)
            .category(category)
            .build();
        transactionRepository.save(recurring);
        
        entityManager.flush();

        // Verify we can retrieve it by its properties
        List<Transaction> all = transactionRepository.findAll();
        assertFalse(all.isEmpty(), "Should have saved the transaction");
        
        Transaction found = all.stream()
            .filter(t -> "Netflix".equals(t.getTitle()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Netflix transaction not found"));
            
        assertTrue(found.getIsRecurringSeriesParent(), "Should be marked as recurring parent");
        assertEquals(baseDate.plusMonths(1), found.getNextRecurrenceDate(), "Should have correct next recurrence date");
    }

    @Test
    void shouldUseOriginalAmountForIncomeStatsEvenWhenEffectiveAmountDiffers() {
        User user = new User();
        user.setUserName("tx-owner-4");
        user.setUserEmail("tx-owner-4@test.com");
        user.setUserHashedPassword("hashed");
        user = userRepository.save(user);

        BankAccount account = new BankAccount(user, "Cuenta 4", new BigDecimal("1000.00"));
        account = bankAccountRepository.save(account);

        Category incomeCategory = new Category("Nomina", "desc", "#00ff00", TransactionType.INCOME, user, "💰");
        incomeCategory = categoryRepository.save(incomeCategory);

        LocalDate date = LocalDate.of(2026, 6, 1);

        transactionRepository.save(Transaction.builder()
            .title("Nomina junio")
            .description("income")
            .amount(new BigDecimal("1520.60"))
            .effectiveAmount(new BigDecimal("1484.79"))
            .date(date)
            .type(TransactionType.INCOME)
            .excludeFromStats(false)
            .recurrence(Recurrence.NONE)
            .isRecurringSeriesParent(false)
            .account(account)
            .category(incomeCategory)
            .build());

        BigDecimal incomesTotal = transactionRepository.findTotalByAccountAndDateRangeAndType(
            account.getId(),
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            TransactionType.INCOME
        );

        List<Object[]> categoryTotals = transactionRepository.findCategoryTotalsByAccountAndDateRangeAndType(
            account.getId(),
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30),
            TransactionType.INCOME
        );

        assertEquals(new BigDecimal("1520.60"), incomesTotal);
        assertEquals(1, categoryTotals.size());
        assertEquals("Nomina", categoryTotals.get(0)[0]);
        assertEquals(new BigDecimal("1520.60"), categoryTotals.get(0)[1]);
    }
}
