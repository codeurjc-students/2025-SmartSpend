package com.smartspend.integration.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.bankAccount.BankAccountRepository;
import com.smartspend.category.Category;
import com.smartspend.category.CategoryRepository;
import com.smartspend.transaction.Debt;
import com.smartspend.transaction.DebtRepository;
import com.smartspend.transaction.Recurrence;
import com.smartspend.transaction.Transaction;
import com.smartspend.transaction.TransactionRepository;
import com.smartspend.transaction.TransactionSpecification;
import com.smartspend.transaction.TransactionType;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

@DataJpaTest(properties = {
    "spring.profiles.active=test",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TransactionSpecificationIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private DebtRepository debtRepository;

    @Test
    void shouldFilterTransactionsWithAllMainCriteriaIncludingPendingDebts() {
        User user = new User();
        user.setUserName("spec-owner");
        user.setUserEmail("spec-owner@test.com");
        user.setUserHashedPassword("hashed");
        user = userRepository.save(user);

        BankAccount account = new BankAccount(user, "Spec account", new BigDecimal("1000.00"));
        account = bankAccountRepository.save(account);

        Category expenseCategory = categoryRepository.save(new Category("Food", "desc", "#f00", TransactionType.EXPENSE, user, "🍔"));
        Category incomeCategory = categoryRepository.save(new Category("Salary", "desc", "#0f0", TransactionType.INCOME, user, "💰"));

        LocalDate now = LocalDate.now();

        Transaction tx1 = transactionRepository.save(Transaction.builder()
            .title("Food Market")
            .description("Weekly purchase")
            .amount(new BigDecimal("50.00"))
            .date(now.minusDays(1))
            .type(TransactionType.EXPENSE)
            .recurrence(Recurrence.NONE)
            .isRecurringSeriesParent(false)
            .account(account)
            .category(expenseCategory)
            .build());

        transactionRepository.save(Transaction.builder()
            .title("Another expense")
            .description("Not matching amount")
            .amount(new BigDecimal("200.00"))
            .date(now)
            .type(TransactionType.EXPENSE)
            .recurrence(Recurrence.NONE)
            .isRecurringSeriesParent(false)
            .account(account)
            .category(expenseCategory)
            .build());

        transactionRepository.save(Transaction.builder()
            .title("Salary")
            .description("Income")
            .amount(new BigDecimal("1000.00"))
            .date(now)
            .type(TransactionType.INCOME)
            .recurrence(Recurrence.NONE)
            .isRecurringSeriesParent(false)
            .account(account)
            .category(incomeCategory)
            .build());

        debtRepository.save(Debt.builder()
            .name("Carlos")
            .amount(new BigDecimal("25.00"))
            .isPaid(false)
            .transaction(tx1)
            .build());

        Specification<Transaction> spec = TransactionSpecification.filterTransactions(
            account.getId(),
            "food",
            "expense",
            now.minusDays(2),
            now,
            new BigDecimal("40.00"),
            new BigDecimal("60.00"),
            expenseCategory.getId(),
            true
        );

        List<Transaction> result = transactionRepository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Food Market", result.get(0).getTitle());
    }

    @Test
    void shouldIgnoreInvalidTypeAndReturnAccountTransactions() {
        User user = new User();
        user.setUserName("spec-owner-2");
        user.setUserEmail("spec-owner-2@test.com");
        user.setUserHashedPassword("hashed");
        user = userRepository.save(user);

        BankAccount account = new BankAccount(user, "Spec account 2", new BigDecimal("1000.00"));
        account = bankAccountRepository.save(account);

        Category category = categoryRepository.save(new Category("General", "desc", "#aaa", TransactionType.EXPENSE, user, "📦"));

        LocalDate now = LocalDate.now();
        transactionRepository.save(Transaction.builder()
            .title("A")
            .description("A")
            .amount(new BigDecimal("10.00"))
            .date(now.minusDays(1))
            .type(TransactionType.EXPENSE)
            .recurrence(Recurrence.NONE)
            .isRecurringSeriesParent(false)
            .account(account)
            .category(category)
            .build());

        transactionRepository.save(Transaction.builder()
            .title("B")
            .description("B")
            .amount(new BigDecimal("20.00"))
            .date(now)
            .type(TransactionType.INCOME)
            .recurrence(Recurrence.NONE)
            .isRecurringSeriesParent(false)
            .account(account)
            .category(category)
            .build());

        Specification<Transaction> spec = TransactionSpecification.filterTransactions(
            account.getId(),
            null,
            "NOT_A_TYPE",
            null,
            null,
            null,
            null,
            null,
            false
        );

        List<Transaction> result = transactionRepository.findAll(spec);

        assertEquals(2, result.size());
    }

    @Test
    void shouldFilterTransactionsForChartsAndOrderAscendingByDate() {
        User user = new User();
        user.setUserName("spec-owner-3");
        user.setUserEmail("spec-owner-3@test.com");
        user.setUserHashedPassword("hashed");
        user = userRepository.save(user);

        BankAccount account = new BankAccount(user, "Spec account 3", new BigDecimal("1000.00"));
        account = bankAccountRepository.save(account);

        Category category = categoryRepository.save(new Category("Food", "desc", "#bbb", TransactionType.EXPENSE, user, "🍔"));

        LocalDate now = LocalDate.now();
        transactionRepository.save(Transaction.builder()
            .title("Later")
            .description("later")
            .amount(new BigDecimal("70.00"))
            .date(now)
            .type(TransactionType.EXPENSE)
            .recurrence(Recurrence.NONE)
            .isRecurringSeriesParent(false)
            .account(account)
            .category(category)
            .build());

        transactionRepository.save(Transaction.builder()
            .title("Earlier")
            .description("earlier")
            .amount(new BigDecimal("30.00"))
            .date(now.minusDays(2))
            .type(TransactionType.EXPENSE)
            .recurrence(Recurrence.NONE)
            .isRecurringSeriesParent(false)
            .account(account)
            .category(category)
            .build());

        transactionRepository.save(Transaction.builder()
            .title("Income ignored")
            .description("income")
            .amount(new BigDecimal("100.00"))
            .date(now.minusDays(1))
            .type(TransactionType.INCOME)
            .recurrence(Recurrence.NONE)
            .isRecurringSeriesParent(false)
            .account(account)
            .category(category)
            .build());

        Specification<Transaction> spec = TransactionSpecification.filterTransactionsForCharts(
            account.getId(),
            now.minusDays(3),
            now,
            TransactionType.EXPENSE
        );

        List<Transaction> result = transactionRepository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Earlier", result.get(0).getTitle());
        assertEquals("Later", result.get(1).getTitle());
    }
}
