package com.smartspend.integration.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.smartspend.category.Category;
import com.smartspend.category.CategoryRepository;
import com.smartspend.transaction.TransactionType;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

@DataJpaTest(properties = {
    "spring.profiles.active=test",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CategoryRepositoryIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldReturnDefaultAndUserCategories() {
        Category defaultExpense = new Category("Comida", "desc", "#123456", TransactionType.EXPENSE, "🍽️");
        categoryRepository.save(defaultExpense);

        User user = new User();
        user.setUserName("ana");
        user.setUserEmail("ana@test.com");
        user.setUserHashedPassword("hashed");
        user = userRepository.save(user);

        Category customExpense = new Category("Mascota", "desc", "#654321", TransactionType.EXPENSE, user, "🐾");
        categoryRepository.save(customExpense);

        assertEquals(1, categoryRepository.findByIsDefaultTrueAndType(TransactionType.EXPENSE).size());
        assertEquals(1, categoryRepository.findByUserUserIdAndType(user.getUserId(), TransactionType.EXPENSE).size());
    }

    @Test
    void shouldFindCategoryByNameAndCountDefaults() {
        Category defaultExpense = new Category("Comida", "desc", "#123456", TransactionType.EXPENSE, "🍽️");
        categoryRepository.save(defaultExpense);

        Category defaultIncome = new Category("Nomina", "desc", "#abcdef", TransactionType.INCOME, "💼");
        categoryRepository.save(defaultIncome);

        Category result = categoryRepository.findByName("Comida");

        assertNotNull(result);
        assertEquals("Comida", result.getName());
        assertEquals(2, categoryRepository.countByIsDefaultTrue());
    }

    @Test
    void shouldReturnAllUserCategoriesWithoutTypeFilter() {
        User user = new User();
        user.setUserName("ana2");
        user.setUserEmail("ana2@test.com");
        user.setUserHashedPassword("hashed");
        user = userRepository.save(user);

        categoryRepository.save(new Category("Mascota", "desc", "#654321", TransactionType.EXPENSE, user, "🐾"));
        categoryRepository.save(new Category("Sueldo", "desc", "#00ff00", TransactionType.INCOME, user, "💰"));

        assertEquals(2, categoryRepository.findByUserUserId(user.getUserId()).size());
    }
}
