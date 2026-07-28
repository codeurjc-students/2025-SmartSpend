package com.smartspend.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.bankAccount.BankAccountRepository;
import com.smartspend.charts.AnalysisService;
import com.smartspend.charts.dtos.BarLineChartDto;
import com.smartspend.charts.dtos.CategoryTrendDto;
import com.smartspend.charts.dtos.FixedExpensesDto;
import com.smartspend.transaction.Recurrence;
import com.smartspend.transaction.Transaction;
import com.smartspend.transaction.TransactionRepository;
import com.smartspend.transaction.TransactionType;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

class AnalysisServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AnalysisService analysisService;

    private User user;
    private BankAccount account;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User("owner", "owner@test.com", "hashed");
        user.setUserId(1L);

        account = new BankAccount(user, "Main", new BigDecimal("1000.00"));
        account.setId(1L);
    }

    @Test
    void shouldReturnForecastBalanceUsingSafeDefaultsAndProjection() {
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.findTotalByAccountAndDateRangeAndType(eq(1L), any(LocalDate.class), any(LocalDate.class), eq(TransactionType.EXPENSE)))
            .thenReturn(null, new BigDecimal("100.00"), new BigDecimal("900.00"));

        BarLineChartDto result = analysisService.getForecastBalance("owner@test.com", 1L, TransactionType.EXPENSE);

        assertNotNull(result);
        assertEquals(List.of("Mes anterior", "Mes actual", "Previsión"), result.labels());
        assertEquals(0.0f, result.data().get(0));
        assertEquals(100.0f, result.data().get(1));

        int remainingDays = LocalDate.now().lengthOfMonth() - LocalDate.now().getDayOfMonth();
        BigDecimal expected = new BigDecimal("100.00")
            .add(new BigDecimal("900.00").divide(BigDecimal.valueOf(90), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(remainingDays)));
        assertEquals(expected.floatValue(), result.data().get(2), 0.01f);
    }

    @Test
    void shouldThrowWhenForecastAccountDoesNotBelongToUser() {
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> analysisService.getForecastBalance("other@test.com", 1L, TransactionType.INCOME));

        assertEquals("Unauthorized access: Account does not belong to the user", ex.getMessage());
    }

    @Test
    void shouldBuildCategoryTrendsWithUpDownAndStableLikeBehavior() {
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.findCategoryTotalsByAccountAndDateRangeAndType(eq(1L), any(LocalDate.class), any(LocalDate.class), eq(TransactionType.EXPENSE)))
            .thenReturn(List.of(new Object[] { "Food", new BigDecimal("100.00") }, new Object[] { "Rent", new BigDecimal("500.00") }))
            .thenReturn(List.of(new Object[] { "Food", new BigDecimal("150.00") }, new Object[] { "New", new BigDecimal("40.00") }))
            .thenReturn(List.of(new Object[] { "Food", new BigDecimal("300.00") }, new Object[] { "Rent", new BigDecimal("1500.00") }, new Object[] { "New", new BigDecimal("120.00") }));

        CategoryTrendDto result = analysisService.getCategoryTrends("owner@test.com", 1L, TransactionType.EXPENSE);

        Map<String, CategoryTrendDto.CategoryMetricsDto> categories = result.categories();
        assertEquals(3, categories.size());
        assertEquals("UP", categories.get("Food").trend());
        assertEquals(50.0f, categories.get("Food").variationPercentage(), 0.01f);
        assertEquals("DOWN", categories.get("Rent").trend());
        assertEquals(-100.0f, categories.get("Rent").variationPercentage(), 0.01f);
        assertEquals("UP", categories.get("New").trend());
        assertEquals(100.0f, categories.get("New").variationPercentage(), 0.01f);
    }

    @Test
    void shouldThrowWhenCategoryTrendsAccountDoesNotBelongToUser() {
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> analysisService.getCategoryTrends("other@test.com", 1L, TransactionType.EXPENSE));

        assertEquals("Unauthorized access", ex.getMessage());
    }

    @Test
    void shouldReturnFixedExpensesForCurrentMonth() {
        Transaction recurringTx = Transaction.builder()
            .title("Rent")
            .description("Monthly")
            .amount(new BigDecimal("800.00"))
            .date(LocalDate.now())
            .type(TransactionType.EXPENSE)
            .recurrence(Recurrence.MONTHLY)
            .isRecurringSeriesParent(true)
            .account(account)
            .build();

        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.findRecurringOrFixedCurrentMonthByAccount(1L)).thenReturn(List.of(recurringTx));

        FixedExpensesDto result = analysisService.getFixedExpenses("owner@test.com", 1L);

        assertEquals(1, result.fixedExpenses().size());
        assertEquals("Rent", result.fixedExpenses().get(0).getTitle());
    }

    @Test
    void shouldReturnBothPendingAndPaidRecurringExpensesForCurrentMonth() {
        LocalDate now = LocalDate.now();

        // Recurrencia pendiente: nextRecurrenceDate en este mes
        Transaction pendingRecurring = Transaction.builder()
            .title("Netflix - Pendiente")
            .description("Suscripción pendiente")
            .amount(new BigDecimal("15.99"))
            .date(now.minusMonths(2))  // Creado hace 2 meses
            .type(TransactionType.EXPENSE)
            .recurrence(Recurrence.MONTHLY)
            .isRecurringSeriesParent(true)
            .nextRecurrenceDate(now.plusDays(5))  // Pendiente en 5 días
            .account(account)
            .build();

        // Recurrencia ya pagada: nextRecurrenceDate en próximo mes, pero tiene hijo este mes
        Transaction paidRecurring = Transaction.builder()
            .title("Spotify - Pagado")
            .description("Suscripción pagada")
            .amount(new BigDecimal("12.99"))
            .date(now.minusMonths(3))  // Creado hace 3 meses
            .type(TransactionType.EXPENSE)
            .recurrence(Recurrence.MONTHLY)
            .isRecurringSeriesParent(true)
            .nextRecurrenceDate(now.plusMonths(1).withDayOfMonth(15))  // Próxima en agosto
            .account(account)
            .build();

        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.findRecurringOrFixedCurrentMonthByAccount(1L))
            .thenReturn(List.of(pendingRecurring, paidRecurring));

        FixedExpensesDto result = analysisService.getFixedExpenses("owner@test.com", 1L);

        assertEquals(2, result.fixedExpenses().size());
        assertTrue(result.fixedExpenses().stream().anyMatch(t -> "Netflix - Pendiente".equals(t.getTitle())));
        assertTrue(result.fixedExpenses().stream().anyMatch(t -> "Spotify - Pagado".equals(t.getTitle())));
    }

    @Test
    void shouldThrowUnauthorizedAccessExceptionForFixedExpensesWhenWrongUser() {
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(account));

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> analysisService.getFixedExpenses("other@test.com", 1L));

        assertEquals("Unauthorized access", ex.getMessage());
    }
}
