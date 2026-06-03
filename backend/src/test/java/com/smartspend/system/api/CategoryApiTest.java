package com.smartspend.system.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.smartspend.category.Category;
import com.smartspend.category.CategoryController;
import com.smartspend.category.CategoryService;
import com.smartspend.transaction.TransactionType;

class CategoryApiTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
    }

    @Test
    void shouldGetCategoriesByTypeAndUser() throws Exception {
        Category category = new Category();
        category.setId(15L);
        category.setName("Comida");

        when(categoryService.getCategoriesForDropdown("user@test.com", TransactionType.EXPENSE))
            .thenReturn(List.of(category));

        Authentication authentication = new UsernamePasswordAuthenticationToken("user@test.com", null);

        mockMvc.perform(get("/api/v1/categories")
                .principal(authentication)
                .param("type", "EXPENSE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(15))
            .andExpect(jsonPath("$[0].name").value("Comida"));

        verify(categoryService).getCategoriesForDropdown("user@test.com", TransactionType.EXPENSE);
    }
}
