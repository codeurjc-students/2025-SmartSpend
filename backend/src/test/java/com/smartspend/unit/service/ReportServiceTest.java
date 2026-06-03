package com.smartspend.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
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
import com.smartspend.report.ReportService;
import com.smartspend.report.dtos.ReportResponseDTO;
import com.smartspend.transaction.Transaction;
import com.smartspend.transaction.TransactionRepository;
import com.smartspend.transaction.TransactionService;
import com.smartspend.transaction.TransactionType;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

class ReportServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChartsService chartsService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private ReportService reportService;

    private User user;
    private BankAccount account;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User("owner", "owner@test.com", "hashed");
        user.setUserId(1L);
        account = new BankAccount(user, "Main", new BigDecimal("1000.00"));
        account.setId(10L);
    }

    @Test
    void shouldBuildMonthlyReportResponse() {
        int year = 2026;
        int month = 6;
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

        Transaction expenseTx = Transaction.builder().title("Expense").type(TransactionType.EXPENSE).account(account).build();
        Transaction incomeTx = Transaction.builder().title("Income").type(TransactionType.INCOME).account(account).build();

        PieChartDto expensePie = new PieChartDto(
            List.of("Food"),
            List.of(100.0),
            List.of("#ff0000"),
            new BigDecimal("100.00")
        );
        PieChartDto incomePie = new PieChartDto(
            List.of("Salary"),
            List.of(1000.0),
            List.of("#00ff00"),
            new BigDecimal("1000.00")
        );
        BarLineChartDto barLine = new BarLineChartDto(List.of("A", "B"), List.of(1f, 2f));
        LineChartDto line = new LineChartDto(List.of("D1"), List.of(20f), List.of(10f), List.of(5f));

        when(userRepository.findByUserEmail("owner@test.com")).thenReturn(Optional.of(user));
        when(bankAccountRepository.findById(10L)).thenReturn(Optional.of(account));
        when(transactionRepository.findByAccountAndDateRangeAndType(10L, from, to, TransactionType.EXPENSE)).thenReturn(List.of(expenseTx));
        when(transactionRepository.findByAccountAndDateRangeAndType(10L, from, to, TransactionType.INCOME)).thenReturn(List.of(incomeTx));
        when(chartsService.getCategoryStadsByMonth("owner@test.com", 10L, year, month, TransactionType.EXPENSE)).thenReturn(expensePie);
        when(chartsService.getCategoryStadsByMonth("owner@test.com", 10L, year, month, TransactionType.INCOME)).thenReturn(incomePie);
        when(chartsService.getBarLineChartByMonth("owner@test.com", 10L, year, month)).thenReturn(barLine);
        when(chartsService.getTimeLineChartByMonth("owner@test.com", 10L, year, month)).thenReturn(line);
        when(transactionRepository.findTotalByAccountAndDateRangeAndType(10L, from, to, TransactionType.INCOME)).thenReturn(new BigDecimal("1000.00"));
        when(transactionRepository.findTotalByAccountAndDateRangeAndType(10L, from, to, TransactionType.EXPENSE)).thenReturn(new BigDecimal("300.00"));

        ReportResponseDTO result = reportService.getResponseData(10L, "owner@test.com", year, month);

        assertNotNull(result);
        assertEquals(10L, result.bankAccount().getId());
        assertEquals(1, result.incomesList().size());
        assertEquals(1, result.expensesList().size());
        assertEquals(1000.0f, result.stadistics().totalIncomes());
        assertEquals(300.0f, result.stadistics().totalExpenses());
        assertEquals(700.0f, result.stadistics().balance());
    }

    @Test
    void shouldThrowWhenUserIsMissing() {
        when(userRepository.findByUserEmail("missing@test.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> reportService.getResponseData(10L, "missing@test.com", 2026, 6));

        assertEquals("User not found", ex.getMessage());
    }
}
