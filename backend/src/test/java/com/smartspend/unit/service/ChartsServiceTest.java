package com.smartspend.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.bankAccount.BankAccountRepository;
import com.smartspend.charts.ChartsService;
import com.smartspend.charts.dtos.BarLineChartDto;
import com.smartspend.charts.dtos.LineChartDto;
import com.smartspend.charts.dtos.PieChartDto;
import com.smartspend.transaction.TransactionRepository;
import com.smartspend.transaction.TransactionType;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

public class ChartsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ChartsService chartsService;

    private User testUser;
    private BankAccount testAccount;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUser = new User("testuser", "test@example.com", "hashedpassword");
        testUser.setUserId(1L);
        
        testAccount = new BankAccount(testUser, "Test Account", new BigDecimal("2000.00"));
        testAccount.setId(1L);
    }

    // ===============================================
    // TESTS PARA PIE CHART (Categorías)
    // ===============================================

    @Test
    @DisplayName("C-1: getCategoryStadsByMonth - Should return pie chart data for income categories")
    void shouldReturnPieChartDataForIncomeCategories() {
        // Given
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findByIdAndUser_UserId(1L, 1L)).thenReturn(Optional.of(testAccount));
        
        List<Object[]> categoryResults = List.of(
            new Object[]{"Salary", new BigDecimal("2000.00")},
            new Object[]{"Freelance", new BigDecimal("500.00")}
        );
        
        when(transactionRepository.findCategoryTotalsByAccountAndDateRangeAndType(
            eq(1L), any(LocalDate.class), any(LocalDate.class), eq(TransactionType.INCOME)))
            .thenReturn(categoryResults);

        // When
        PieChartDto result = chartsService.getCategoryStadsByMonth(
            "test@example.com", 1L, 2025, 2, TransactionType.INCOME);

        // Then
        assertNotNull(result);
        assertEquals(2, result.labels().size());
        assertEquals(2, result.data().size());
        assertTrue(result.labels().contains("Salary"));
        assertTrue(result.labels().contains("Freelance"));
        assertEquals(new BigDecimal("2500.00"), result.totalAmount());
    }

    @Test
    @DisplayName("C-2: getCategoryStadsByYear - Should return yearly pie chart data")
    void shouldReturnYearlyPieChartData() {
        // Given
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findByIdAndUser_UserId(1L, 1L)).thenReturn(Optional.of(testAccount));
        
        List<Object[]> categoryResults = List.of(
            new Object[]{"Food", new BigDecimal("3000.00")},
            new Object[]{"Transport", new BigDecimal("1200.00")}
        );
        
        when(transactionRepository.findCategoryTotalsByAccountAndDateRangeAndType(
            eq(1L), any(LocalDate.class), any(LocalDate.class), eq(TransactionType.EXPENSE)))
            .thenReturn(categoryResults);

        // When
        PieChartDto result = chartsService.getCategoryStadsByYear(
            "test@example.com", 1L, 2025, TransactionType.EXPENSE);

        // Then
        assertNotNull(result);
        assertEquals(2, result.labels().size());
        assertEquals(2, result.data().size());
        assertEquals(new BigDecimal("4200.00"), result.totalAmount());
    }

    @Test
    @DisplayName("C-3: buildPieChartDto - Should build pie chart DTO correctly")
    void shouldBuildPieChartDtoCorrectly() {
        // Given
        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
        categoryTotals.put("Category A", new BigDecimal("100.00"));
        categoryTotals.put("Category B", new BigDecimal("200.00"));
        BigDecimal totalAmount = new BigDecimal("300.00");

        // When
        PieChartDto result = chartsService.buildPieChartDto(categoryTotals, totalAmount);

        // Then
        assertNotNull(result);
        assertEquals(List.of("Category A", "Category B"), result.labels());
        assertEquals(List.of(100.0, 200.0), result.data());
        assertEquals(totalAmount, result.totalAmount());
        assertNull(result.backgroundColors());
    }

    // ===============================================
    // TESTS PARA BAR CHART (Ingresos vs Gastos)
    // ===============================================

    @Test
    @DisplayName("C-4: getBarLineChartByMonth - Should return bar chart data for monthly income vs expense")
    void shouldReturnBarChartDataForMonthly() {
        // Given
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findByIdAndUser_UserId(1L, 1L)).thenReturn(Optional.of(testAccount));
        
        when(transactionRepository.findTotalByAccountAndDateRangeAndType(
            eq(1L), any(LocalDate.class), any(LocalDate.class), eq(TransactionType.INCOME)))
            .thenReturn(new BigDecimal("2500.00"));
        when(transactionRepository.findTotalByAccountAndDateRangeAndType(
            eq(1L), any(LocalDate.class), any(LocalDate.class), eq(TransactionType.EXPENSE)))
            .thenReturn(new BigDecimal("1200.00"));

        // When
        BarLineChartDto result = chartsService.getBarLineChartByMonth("test@example.com", 1L, 2025, 2);

        // Then
        assertNotNull(result);
        assertEquals(List.of("Incomes", "Expenses"), result.labels());
        assertEquals(List.of(2500.0f, 1200.0f), result.data());
    }

    @Test
    @DisplayName("C-5: getBarLineChartByYear - Should return bar chart data for yearly income vs expense")
    void shouldReturnBarChartDataForYearly() {
        // Given
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findByIdAndUser_UserId(1L, 1L)).thenReturn(Optional.of(testAccount));
        
        when(transactionRepository.findTotalByAccountAndDateRangeAndType(
            eq(1L), any(LocalDate.class), any(LocalDate.class), eq(TransactionType.INCOME)))
            .thenReturn(new BigDecimal("30000.00"));
        when(transactionRepository.findTotalByAccountAndDateRangeAndType(
            eq(1L), any(LocalDate.class), any(LocalDate.class), eq(TransactionType.EXPENSE)))
            .thenReturn(new BigDecimal("18000.00"));

        // When
        BarLineChartDto result = chartsService.getBarLineChartByYear("test@example.com", 1L, 2025);

        // Then
        assertNotNull(result);
        assertEquals(List.of("Incomes", "Expenses"), result.labels());
        assertEquals(List.of(30000.0f, 18000.0f), result.data());
    }

    @Test
    @DisplayName("C-6: getBarLineChartByMonth - Should handle null values correctly")
    void shouldHandleNullValuesInBarChart() {
        // Given
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findByIdAndUser_UserId(1L, 1L)).thenReturn(Optional.of(testAccount));
        
        // Return null to simulate no data
        when(transactionRepository.findTotalByAccountAndDateRangeAndType(
            eq(1L), any(LocalDate.class), any(LocalDate.class), any(TransactionType.class)))
            .thenReturn(null);

        // When
        BarLineChartDto result = chartsService.getBarLineChartByMonth("test@example.com", 1L, 2025, 2);

        // Then
        assertNotNull(result);
        assertEquals(List.of("Incomes", "Expenses"), result.labels());
        assertEquals(List.of(0.0f, 0.0f), result.data());
    }

    // ===============================================
    // TESTS PARA TIMELINE CHART (Evolución del balance)
    // ===============================================

    @Test
    @DisplayName("C-7: getTimeLineChartByMonth - Should return timeline data for monthly balance evolution")
    void shouldReturnTimelineDataForMonthly() {
        // Given
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findByIdAndUser_UserId(1L, 1L)).thenReturn(Optional.of(testAccount));
        
        // Mock balance calculation
        when(transactionRepository.findBalanceUpToDate(eq(1L), any(LocalDate.class)))
            .thenReturn(new BigDecimal("2000.00"));
        when(transactionRepository.findTotalByAccountAndDateRangeAndType(
            eq(1L), any(LocalDate.class), any(LocalDate.class), any(TransactionType.class)))
            .thenReturn(new BigDecimal("100.00"));

        // When
        LineChartDto result = chartsService.getTimeLineChartByMonth("test@example.com", 1L, 2025, 2);

        // Then
        assertNotNull(result);
        assertNotNull(result.labels());
        assertNotNull(result.balanceData());
        assertNotNull(result.incomesData());
        assertNotNull(result.expensesData());
        assertTrue(result.labels().size() > 0);
    }

    @Test
    @DisplayName("C-8: getTimeLineChartByYear - Should return timeline data for yearly balance evolution")
    void shouldReturnTimelineDataForYearly() {
        // Given  
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findByIdAndUser_UserId(1L, 1L)).thenReturn(Optional.of(testAccount));
        
        // Mock balance calculation
        when(transactionRepository.findBalanceUpToDate(eq(1L), any(LocalDate.class)))
            .thenReturn(new BigDecimal("2000.00"));
        when(transactionRepository.findTotalByAccountAndDateRangeAndType(
            eq(1L), any(LocalDate.class), any(LocalDate.class), any(TransactionType.class)))
            .thenReturn(new BigDecimal("500.00"));

        // When
        LineChartDto result = chartsService.getTimeLineChartByYear("test@example.com", 1L, 2025);

        // Then
        assertNotNull(result);
        assertNotNull(result.labels());
        assertNotNull(result.balanceData());
        assertNotNull(result.incomesData());
        assertNotNull(result.expensesData());
        assertTrue(result.labels().size() > 0);
    }

    // ===============================================
    // TESTS PARA CASOS DE ERROR
    // ===============================================

    @Test
    @DisplayName("C-9: getCategoryStadsByMonth - Should throw exception for non-existent user")
    void shouldThrowExceptionForNonExistentUser() {
        // Given
        when(userRepository.findByUserEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            chartsService.getCategoryStadsByMonth("nonexistent@example.com", 1L, 2025, 2, TransactionType.INCOME);
        });
        
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @DisplayName("C-10: getBarLineChartByMonth - Should throw exception for non-existent account")
    void shouldThrowExceptionForNonExistentAccount() {
        // Given
        when(userRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(bankAccountRepository.findByIdAndUser_UserId(999L, 1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            chartsService.getBarLineChartByMonth("test@example.com", 999L, 2025, 2);
        });
        
        assertEquals("Bank account not found", exception.getMessage());
    }

    @Test
    @DisplayName("C-11: buildPieChartDto - Should handle empty category totals")
    void shouldHandleEmptyCategoryTotals() {
        // Given
        Map<String, BigDecimal> emptyCategoryTotals = new LinkedHashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // When
        PieChartDto result = chartsService.buildPieChartDto(emptyCategoryTotals, totalAmount);

        // Then
        assertNotNull(result);
        assertTrue(result.labels().isEmpty());
        assertTrue(result.data().isEmpty());
        assertEquals(BigDecimal.ZERO, result.totalAmount());
    }
}