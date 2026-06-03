package com.smartspend.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.category.Category;
import com.smartspend.config.PasswordConfig;
import com.smartspend.transaction.Recurrence;
import com.smartspend.transaction.Transaction;
import com.smartspend.transaction.TransactionType;
import com.smartspend.user.User;

class PasswordAndTransactionEntityTest {

    @Test
    void shouldCreateBcryptPasswordEncoder() {
        PasswordConfig config = new PasswordConfig();

        PasswordEncoder encoder = config.passwordEncoder();

        assertNotNull(encoder);
        assertTrue(encoder.matches("secret", encoder.encode("secret")));
    }

    @Test
    void shouldUseConvenienceConstructorAndImageHelpers() {
        User user = new User("owner", "owner@test.com", "hashed");
        user.setUserId(1L);
        BankAccount account = new BankAccount(user, "Main", new BigDecimal("100.00"));
        account.setId(10L);
        Category category = new Category("Food", "desc", "#f00", TransactionType.EXPENSE, "🍔");
        category.setId(20L);

        Transaction tx = new Transaction(
            "Dinner",
            "Friday",
            new BigDecimal("15.00"),
            LocalDate.now(),
            TransactionType.EXPENSE,
            category,
            Recurrence.NONE,
            account
        );

        assertEquals("Dinner", tx.getTitle());
        assertEquals(TransactionType.EXPENSE, tx.getType());
        assertEquals(Recurrence.NONE, tx.getRecurrence());
        assertFalse(tx.hasImage());
        assertNull(tx.getImageBase64());

        byte[] image = "ticket".getBytes();
        tx.setImageData(image);

        assertTrue(tx.hasImage());
        assertEquals(Base64.getEncoder().encodeToString(image), tx.getImageBase64());
    }
}
