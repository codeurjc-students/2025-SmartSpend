package com.smartspend.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import com.smartspend.charts.AnalysisService;
import com.smartspend.charts.ChartsController;
import com.smartspend.charts.ChartsService;
import com.smartspend.charts.dtos.BarLineChartDto;
import com.smartspend.charts.dtos.CategoryTrendDto;
import com.smartspend.charts.dtos.FixedExpensesDto;
import com.smartspend.charts.dtos.ForecastBalanceDto;
import com.smartspend.charts.dtos.LineChartDto;
import com.smartspend.charts.dtos.PieChartDto;
import com.smartspend.transaction.TransactionType;

class ChartsControllerTest {

    @Mock
    private ChartsService chartsService;

    @Mock
    private AnalysisService analysisService;

    private ChartsController controller;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ChartsController();
        ReflectionTestUtils.setField(controller, "chartsService", chartsService);
        ReflectionTestUtils.setField(controller, "analysisService", analysisService);
        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("owner@test.com");
    }

    @Test
    void shouldReturnMonthlyPieChart() {
        PieChartDto dto = new PieChartDto(List.of("Food"), List.of(100.0), List.of("#f00"), new BigDecimal("100.00"));
        when(chartsService.getCategoryStadsByMonth("owner@test.com", 1L, 2026, 6, TransactionType.EXPENSE)).thenReturn(dto);

        ResponseEntity<PieChartDto> response = controller.getPieChartByMonth(1L, 2026, 6, TransactionType.EXPENSE, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void shouldReturnBadRequestForMonthlyPieChartErrors() {
        when(chartsService.getCategoryStadsByMonth("owner@test.com", 1L, 2026, 6, TransactionType.EXPENSE))
            .thenThrow(new RuntimeException("boom"));

        ResponseEntity<PieChartDto> response = controller.getPieChartByMonth(1L, 2026, 6, TransactionType.EXPENSE, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldReturnYearlyPieChart() {
        PieChartDto dto = new PieChartDto(List.of("Salary"), List.of(3000.0), List.of("#0f0"), new BigDecimal("3000.00"));
        when(chartsService.getCategoryStadsByYear("owner@test.com", 1L, 2026, TransactionType.INCOME)).thenReturn(dto);

        ResponseEntity<PieChartDto> response = controller.getPieChartByYear(1L, 2026, TransactionType.INCOME, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void shouldReturnBarLineMonthlyAndYearly() {
        BarLineChartDto dto = new BarLineChartDto(List.of("A"), List.of(10f));
        when(chartsService.getBarLineChartByMonth("owner@test.com", 1L, 2026, 6)).thenReturn(dto);
        when(chartsService.getBarLineChartByYear("owner@test.com", 1L, 2026)).thenReturn(dto);

        ResponseEntity<BarLineChartDto> monthly = controller.getBarLineChartMonthly(1L, 2026, 6, authentication);
        ResponseEntity<BarLineChartDto> yearly = controller.getBarLineChartYearly(1L, 2026, authentication);

        assertEquals(HttpStatus.OK, monthly.getStatusCode());
        assertEquals(HttpStatus.OK, yearly.getStatusCode());
    }

    @Test
    void shouldReturnTimelineMonthlyAndYearly() {
        LineChartDto dto = new LineChartDto(List.of("d1"), List.of(10f), List.of(20f), List.of(5f));
        when(chartsService.getTimeLineChartByMonth("owner@test.com", 1L, 2026, 6)).thenReturn(dto);
        when(chartsService.getTimeLineChartByYear("owner@test.com", 1L, 2026)).thenReturn(dto);

        ResponseEntity<LineChartDto> monthly = controller.getTimelineChartMonthly(1L, 2026, 6, authentication);
        ResponseEntity<LineChartDto> yearly = controller.getTimelineChartYearly(1L, 2026, authentication);

        assertEquals(HttpStatus.OK, monthly.getStatusCode());
        assertEquals(HttpStatus.OK, yearly.getStatusCode());
    }

    @Test
    void shouldReturnCategoryTrendsAndFixedExpensesAndForecast() {
        CategoryTrendDto trend = new CategoryTrendDto(java.util.Map.of(
            "Food",
            new CategoryTrendDto.CategoryMetricsDto(10f, 15f, 20f, 50f, "UP")
        ));
        FixedExpensesDto fixed = new FixedExpensesDto(List.of());
        BarLineChartDto income = new BarLineChartDto(List.of("A"), List.of(100f));
        BarLineChartDto expense = new BarLineChartDto(List.of("A"), List.of(60f));

        when(analysisService.getCategoryTrends("owner@test.com", 1L, TransactionType.EXPENSE)).thenReturn(trend);
        when(analysisService.getFixedExpenses("owner@test.com", 1L)).thenReturn(fixed);
        when(analysisService.getForecastBalance("owner@test.com", 1L, TransactionType.INCOME)).thenReturn(income);
        when(analysisService.getForecastBalance("owner@test.com", 1L, TransactionType.EXPENSE)).thenReturn(expense);

        ResponseEntity<CategoryTrendDto> trendResponse = controller.getCategoryTrends(1L, authentication);
        ResponseEntity<FixedExpensesDto> fixedResponse = controller.getFixedExpenses(1L, authentication);
        ResponseEntity<ForecastBalanceDto> forecastResponse = controller.getForecast(1L, authentication);

        assertEquals(HttpStatus.OK, trendResponse.getStatusCode());
        assertEquals(HttpStatus.OK, fixedResponse.getStatusCode());
        assertEquals(HttpStatus.OK, forecastResponse.getStatusCode());
        assertEquals(income, forecastResponse.getBody().forecastBalanceIncomesChart());
        assertEquals(expense, forecastResponse.getBody().forecastBalanceExpensesChart());
    }

    @Test
    void shouldReturnBadRequestWhenControllerMethodsThrowRuntimeExceptions() {
        when(chartsService.getBarLineChartByMonth("owner@test.com", 1L, 2026, 6)).thenThrow(new RuntimeException("x"));
        when(chartsService.getBarLineChartByYear("owner@test.com", 1L, 2026)).thenThrow(new RuntimeException("x"));
        when(chartsService.getTimeLineChartByMonth("owner@test.com", 1L, 2026, 6)).thenThrow(new RuntimeException("x"));
        when(chartsService.getTimeLineChartByYear("owner@test.com", 1L, 2026)).thenThrow(new RuntimeException("x"));
        when(analysisService.getCategoryTrends("owner@test.com", 1L, TransactionType.EXPENSE)).thenThrow(new RuntimeException("x"));
        when(analysisService.getFixedExpenses("owner@test.com", 1L)).thenThrow(new RuntimeException("x"));
        when(analysisService.getForecastBalance("owner@test.com", 1L, TransactionType.INCOME)).thenThrow(new RuntimeException("x"));

        assertEquals(HttpStatus.BAD_REQUEST, controller.getBarLineChartMonthly(1L, 2026, 6, authentication).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.getBarLineChartYearly(1L, 2026, authentication).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.getTimelineChartMonthly(1L, 2026, 6, authentication).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.getTimelineChartYearly(1L, 2026, authentication).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.getCategoryTrends(1L, authentication).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.getFixedExpenses(1L, authentication).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.getForecast(1L, authentication).getStatusCode());
    }
}
