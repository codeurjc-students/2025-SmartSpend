package com.smartspend.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartspend.category.Category;
import com.smartspend.category.CategoryRepository;
import com.smartspend.category.CategoryService;
import com.smartspend.transaction.TransactionType;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

public class CategoryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;
    private Category systemIncomeCategory;
    private Category systemExpenseCategory;
    private Category userCustomCategory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User("testuser", "test@example.com", "hashedpassword");
        testUser.setUserId(1L);
        
        // System categories (default=true, user=null)
        systemIncomeCategory = new Category("Salary", "System salary category", "#27ae60", TransactionType.INCOME, "💰");
        systemIncomeCategory.setId(1L);
        
        systemExpenseCategory = new Category("Food", "System food category", "#e74c3c", TransactionType.EXPENSE, "🛒");
        systemExpenseCategory.setId(2L);
        
        // User custom category
        userCustomCategory = new Category("Custom Income", "User custom category", "#3498db", TransactionType.INCOME, testUser, "⭐");
        userCustomCategory.setId(3L);
    }

    @Test
    void shouldCombineSystemAndUserCategories() {
        // Given
        List<Category> systemCategories = List.of(systemIncomeCategory);
        List<Category> userCategories = List.of(userCustomCategory);
        
        // When - Configure mocks
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByIsDefaultTrueAndType(TransactionType.INCOME)).thenReturn(systemCategories);
        when(categoryRepository.findByUserUserIdAndType(1L, TransactionType.INCOME)).thenReturn(userCategories);
        
        // When - Execute
        List<Category> result = categoryService.getCategoriesForDropdown("test@example.com", TransactionType.INCOME);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Should contain both system and user categories
        assertTrue(result.contains(systemIncomeCategory));
        assertTrue(result.contains(userCustomCategory));
        
        // Verify correct repository calls
        verify(categoryRepository).findByIsDefaultTrueAndType(TransactionType.INCOME);
        verify(categoryRepository).findByUserUserIdAndType(1L, TransactionType.INCOME);
    }

    @Test
    void shouldFilterCategoriesByType() {
        // Given
        List<Category> systemExpenseCategories = List.of(systemExpenseCategory);
        List<Category> userExpenseCategories = new ArrayList<>(); // No user custom expense categories
        
        // When - Configure mocks for EXPENSE type
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByIsDefaultTrueAndType(TransactionType.EXPENSE)).thenReturn(systemExpenseCategories);
        when(categoryRepository.findByUserUserIdAndType(1L, TransactionType.EXPENSE)).thenReturn(userExpenseCategories);
        
        // When - Execute
        List<Category> result = categoryService.getCategoriesForDropdown("test@example.com", TransactionType.EXPENSE);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(systemExpenseCategory, result.get(0));
        assertEquals(TransactionType.EXPENSE, result.get(0).getType());
        
        // Verify it only requested EXPENSE categories
        verify(categoryRepository).findByIsDefaultTrueAndType(TransactionType.EXPENSE);
        verify(categoryRepository).findByUserUserIdAndType(1L, TransactionType.EXPENSE);
        verify(categoryRepository, never()).findByIsDefaultTrueAndType(TransactionType.INCOME);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given - User doesn't exist
        when(userRepository.findByUserEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        // Then - Should throw exception
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> categoryService.getCategoriesForDropdown("nonexistent@example.com", TransactionType.INCOME)
        );
        
        assertEquals("User not found", exception.getMessage());
        
        // Verify no category repository calls were made
        verify(categoryRepository, never()).findByIsDefaultTrueAndType(any());
        verify(categoryRepository, never()).findByUserUserIdAndType(anyLong(), any());
    }

    @Test
    void shouldReturnOnlySystemCategoriesWhenUserHasNoCustomCategories() {
        // Given
        List<Category> systemCategories = List.of(systemIncomeCategory);
        List<Category> emptyUserCategories = new ArrayList<>();
        
        // When - Configure mocks
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByIsDefaultTrueAndType(TransactionType.INCOME)).thenReturn(systemCategories);
        when(categoryRepository.findByUserUserIdAndType(1L, TransactionType.INCOME)).thenReturn(emptyUserCategories);
        
        // When - Execute
        List<Category> result = categoryService.getCategoriesForDropdown("test@example.com", TransactionType.INCOME);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(systemIncomeCategory, result.get(0));
        assertTrue(result.get(0).getIsDefault());
    }

    @Test
    void shouldReturnOnlyUserCategoriesWhenNoSystemCategoriesExist() {
        // Given
        List<Category> emptySystemCategories = new ArrayList<>();
        List<Category> userCategories = List.of(userCustomCategory);
        
        // When - Configure mocks
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByIsDefaultTrueAndType(TransactionType.INCOME)).thenReturn(emptySystemCategories);
        when(categoryRepository.findByUserUserIdAndType(1L, TransactionType.INCOME)).thenReturn(userCategories);
        
        // When - Execute
        List<Category> result = categoryService.getCategoriesForDropdown("test@example.com", TransactionType.INCOME);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userCustomCategory, result.get(0));
        assertFalse(result.get(0).getIsDefault());
        assertEquals(testUser, result.get(0).getUser());
    }

    @Test
    void shouldReturnEmptyListWhenNoCategoriesExist() {
        // Given
        List<Category> emptySystemCategories = new ArrayList<>();
        List<Category> emptyUserCategories = new ArrayList<>();
        
        // When - Configure mocks
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByIsDefaultTrueAndType(TransactionType.INCOME)).thenReturn(emptySystemCategories);
        when(categoryRepository.findByUserUserIdAndType(1L, TransactionType.INCOME)).thenReturn(emptyUserCategories);
        
        // When - Execute
        List<Category> result = categoryService.getCategoriesForDropdown("test@example.com", TransactionType.INCOME);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldMaintainOrderWithSystemCategoriesFirst() {
        // Given
        Category systemCategory1 = new Category("System 1", "First system category", "#color1", TransactionType.INCOME, "📈");
        Category systemCategory2 = new Category("System 2", "Second system category", "#color2", TransactionType.INCOME, "💼");
        Category userCategory1 = new Category("User 1", "First user category", "#color3", TransactionType.INCOME, testUser, "⭐");
        Category userCategory2 = new Category("User 2", "Second user category", "#color4", TransactionType.INCOME, testUser, "🔥");
        
        List<Category> systemCategories = List.of(systemCategory1, systemCategory2);
        List<Category> userCategories = List.of(userCategory1, userCategory2);
        
        // When - Configure mocks
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByIsDefaultTrueAndType(TransactionType.INCOME)).thenReturn(systemCategories);
        when(categoryRepository.findByUserUserIdAndType(1L, TransactionType.INCOME)).thenReturn(userCategories);
        
        // When - Execute
        List<Category> result = categoryService.getCategoriesForDropdown("test@example.com", TransactionType.INCOME);
        
        // Then - System categories should appear first
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals(systemCategory1, result.get(0));
        assertEquals(systemCategory2, result.get(1));
        assertEquals(userCategory1, result.get(2));
        assertEquals(userCategory2, result.get(3));
    }
}