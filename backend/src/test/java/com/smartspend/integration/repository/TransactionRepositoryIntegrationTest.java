package com.smartspend.integration.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
}
