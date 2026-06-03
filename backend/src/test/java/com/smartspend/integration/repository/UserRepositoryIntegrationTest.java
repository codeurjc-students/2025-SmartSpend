package com.smartspend.integration.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

@DataJpaTest(properties = {
    "spring.profiles.active=test",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserByEmail() {
        User user = new User();
        user.setUserName("integration-user");
        user.setUserEmail("integration@test.com");
        user.setUserHashedPassword("hashed");
        userRepository.save(user);

        assertTrue(userRepository.findByUserEmail("integration@test.com").isPresent());
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        User user = new User();
        user.setUserName("integration-user");
        user.setUserEmail("integration@test.com");
        user.setUserHashedPassword("hashed");
        userRepository.save(user);

        assertFalse(userRepository.findByUserEmail("missing@test.com").isPresent());
    }
}
