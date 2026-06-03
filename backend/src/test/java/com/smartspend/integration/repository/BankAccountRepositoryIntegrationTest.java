package com.smartspend.integration.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.bankAccount.BankAccountRepository;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

@DataJpaTest(properties = {
    "spring.profiles.active=test",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BankAccountRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Test
    void shouldFindAccountsByUserId() {
        User user = new User();
        user.setUserName("owner");
        user.setUserEmail("owner@test.com");
        user.setUserHashedPassword("hashed");
        user = userRepository.save(user);

        BankAccount account = new BankAccount(user, "Cuenta principal", new BigDecimal("500.00"));
        bankAccountRepository.save(account);

        assertEquals(1, bankAccountRepository.findByUser_UserId(user.getUserId()).size());
        assertTrue(bankAccountRepository.findByIdAndUser_UserId(account.getId(), user.getUserId()).isPresent());
    }

    @Test
    void shouldCountAccountsByUser() {
        User user = new User();
        user.setUserName("owner");
        user.setUserEmail("owner2@test.com");
        user.setUserHashedPassword("hashed");
        user = userRepository.save(user);

        bankAccountRepository.save(new BankAccount(user, "Cuenta 1", new BigDecimal("100.00")));
        bankAccountRepository.save(new BankAccount(user, "Cuenta 2", new BigDecimal("200.00")));

        assertEquals(2, bankAccountRepository.countByUser(user));
    }

    @Test
    void shouldNotFindAccountByIdWhenUserIsNotOwner() {
        User owner = new User();
        owner.setUserName("owner");
        owner.setUserEmail("owner3@test.com");
        owner.setUserHashedPassword("hashed");
        owner = userRepository.save(owner);

        User other = new User();
        other.setUserName("other");
        other.setUserEmail("other3@test.com");
        other.setUserHashedPassword("hashed");
        other = userRepository.save(other);

        BankAccount account = new BankAccount(owner, "Cuenta privada", new BigDecimal("300.00"));
        account = bankAccountRepository.save(account);

        assertFalse(bankAccountRepository.findByIdAndUser_UserId(account.getId(), other.getUserId()).isPresent());
    }
}
