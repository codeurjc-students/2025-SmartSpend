package com.smartspend.integration.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.bankAccount.BankAccountRepository;
import com.smartspend.category.Category;
import com.smartspend.category.CategoryRepository;
import com.smartspend.transaction.Debt;
import com.smartspend.transaction.DebtRepository;
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
class DebtRepositoryIntegrationTest {

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
    void shouldFindPendingDebtsAndMatchDebtByAmount() {
        User user = new User();
        user.setUserName("debts-owner");
        user.setUserEmail("debts-owner@test.com");
        user.setUserHashedPassword("hashed");
        user = userRepository.save(user);

        BankAccount account = new BankAccount(user, "Cuenta", new BigDecimal("200.00"));
        account = bankAccountRepository.save(account);

        Category category = new Category("Comida", "desc", "#f00", TransactionType.EXPENSE, user, "🍔");
        category = categoryRepository.save(category);

        Transaction transaction = Transaction.builder()
            .title("Cena compartida")
            .description("desc")
            .amount(new BigDecimal("40.00"))
            .date(LocalDate.now())
            .type(TransactionType.EXPENSE)
            .recurrence(Recurrence.NONE)
            .isRecurringSeriesParent(false)
            .account(account)
            .category(category)
            .build();
        transaction = transactionRepository.save(transaction);

        Debt debt = Debt.builder()
            .name("Carlos")
            .amount(new BigDecimal("20.00"))
            .isPaid(false)
            .transaction(transaction)
            .build();
        debtRepository.save(debt);

        assertEquals(1, debtRepository.findPendingDebtsByUserId(user.getUserId(), PageRequest.of(0, 5)).size());
        assertTrue(debtRepository.findByTransaction_Account_IdAndNameAndAmount(account.getId(), "Carlos", new BigDecimal("20.00")).isPresent());
    }

    @Test
    void shouldReturnPendingDebtsOrderedAndPaginated() {
        User user = new User();
        user.setUserName("debts-owner-2");
        user.setUserEmail("debts-owner-2@test.com");
        user.setUserHashedPassword("hashed");
        user = userRepository.save(user);

        BankAccount account = new BankAccount(user, "Cuenta 2", new BigDecimal("200.00"));
        account = bankAccountRepository.save(account);

        Category category = new Category("Comida", "desc", "#f00", TransactionType.EXPENSE, user, "🍔");
        category = categoryRepository.save(category);

        Transaction older = Transaction.builder()
            .title("Compra vieja")
            .description("desc")
            .amount(new BigDecimal("40.00"))
            .date(LocalDate.now().minusDays(3))
            .type(TransactionType.EXPENSE)
            .recurrence(Recurrence.NONE)
            .isRecurringSeriesParent(false)
            .account(account)
            .category(category)
            .build();
        older = transactionRepository.save(older);

        Transaction newer = Transaction.builder()
            .title("Compra nueva")
            .description("desc")
            .amount(new BigDecimal("50.00"))
            .date(LocalDate.now())
            .type(TransactionType.EXPENSE)
            .recurrence(Recurrence.NONE)
            .isRecurringSeriesParent(false)
            .account(account)
            .category(category)
            .build();
        newer = transactionRepository.save(newer);

        debtRepository.save(Debt.builder().name("Ana").amount(new BigDecimal("10.00")).isPaid(false).transaction(older).build());
        debtRepository.save(Debt.builder().name("Luis").amount(new BigDecimal("15.00")).isPaid(false).transaction(newer).build());
        debtRepository.save(Debt.builder().name("Pagada").amount(new BigDecimal("20.00")).isPaid(true).transaction(newer).build());

        var page = debtRepository.findPendingDebtsByUserId(user.getUserId(), PageRequest.of(0, 1));

        assertEquals(1, page.size());
        assertEquals("Luis", page.get(0).getName());
    }

    @Test
    void shouldNotMatchDebtWhenAmountDiffers() {
        User user = new User();
        user.setUserName("debts-owner-3");
        user.setUserEmail("debts-owner-3@test.com");
        user.setUserHashedPassword("hashed");
        user = userRepository.save(user);

        BankAccount account = new BankAccount(user, "Cuenta 3", new BigDecimal("200.00"));
        account = bankAccountRepository.save(account);

        Category category = new Category("Comida", "desc", "#f00", TransactionType.EXPENSE, user, "🍔");
        category = categoryRepository.save(category);

        Transaction transaction = Transaction.builder()
            .title("Cena")
            .description("desc")
            .amount(new BigDecimal("40.00"))
            .date(LocalDate.now())
            .type(TransactionType.EXPENSE)
            .recurrence(Recurrence.NONE)
            .isRecurringSeriesParent(false)
            .account(account)
            .category(category)
            .build();
        transaction = transactionRepository.save(transaction);

        debtRepository.save(Debt.builder().name("Carlos").amount(new BigDecimal("20.00")).isPaid(false).transaction(transaction).build());

        assertFalse(
            debtRepository.findByTransaction_Account_IdAndNameAndAmount(account.getId(), "Carlos", new BigDecimal("21.00")).isPresent()
        );
    }
}
