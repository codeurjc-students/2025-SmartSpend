package com.smartspend.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.category.Category;
import com.smartspend.transaction.Debt;
import com.smartspend.transaction.Recurrence;
import com.smartspend.transaction.Transaction;
import com.smartspend.transaction.TransactionMapper;
import com.smartspend.transaction.TransactionType;
import com.smartspend.transaction.dtos.TransactionResponseDto;
import com.smartspend.user.User;

class TransactionMapperTest {

    private TransactionMapper mapper;
    private BankAccount account;
    private Category category;

    @BeforeEach
    void setUp() {
        mapper = new TransactionMapper();

        User user = new User("owner", "owner@test.com", "hashed");
        user.setUserId(1L);

        account = new BankAccount(user, "Main", new BigDecimal("1000.00"));
        account.setId(10L);

        category = new Category("Food", "desc", "#f00", TransactionType.EXPENSE, "🍔");
        category.setId(50L);
    }

    @Test
    void shouldMapTransactionWithoutDebtsAndImage() {
        Transaction tx = Transaction.builder()
            .id(100L)
            .title("Dinner")
            .description("Friday dinner")
            .amount(new BigDecimal("25.00"))
            .beforeBalance(new BigDecimal("200.00"))
            .effectiveAmount(new BigDecimal("25.00"))
            .date(LocalDate.now())
            .type(TransactionType.EXPENSE)
            .recurrence(Recurrence.NONE)
            .account(account)
            .category(category)
            .excludeFromStats(false)
            .sharedDebts(null)
            .build();

        TransactionResponseDto dto = mapper.toResponseDto(tx);

        assertNotNull(dto);
        assertEquals(100L, dto.id());
        assertEquals("Dinner", dto.title());
        assertEquals(10L, dto.accountId());
        assertEquals("Main", dto.accountName());
        assertEquals(0, dto.debts().size());
        assertFalse(dto.hasImage());
    }

    @Test
    void shouldMapTransactionWithDebtsAndImage() {
        byte[] image = "hello-image".getBytes();

        Transaction tx = Transaction.builder()
            .id(101L)
            .title("Shared dinner")
            .description("With debts")
            .amount(new BigDecimal("60.00"))
            .beforeBalance(new BigDecimal("500.00"))
            .effectiveAmount(new BigDecimal("60.00"))
            .date(LocalDate.now())
            .type(TransactionType.EXPENSE)
            .recurrence(Recurrence.NONE)
            .account(account)
            .category(category)
            .excludeFromStats(true)
            .imageData(image)
            .imageName("ticket.png")
            .imageType("image/png")
            .build();

        tx.setSharedDebts(List.of(
            Debt.builder().id(1L).name("Ana").amount(new BigDecimal("20.00")).isPaid(false).transaction(tx).build(),
            Debt.builder().id(2L).name("Luis").amount(new BigDecimal("15.00")).isPaid(true).transaction(tx).build()
        ));

        TransactionResponseDto dto = mapper.toResponseDto(tx);

        assertEquals(2, dto.debts().size());
        assertTrue(dto.hasImage());
        assertEquals("ticket.png", dto.imageName());
        assertEquals("image/png", dto.imageType());
        assertEquals(Base64.getEncoder().encodeToString(image), dto.imageBase64());
    }
}
