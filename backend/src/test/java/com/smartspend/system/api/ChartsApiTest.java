package com.smartspend.system.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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

class ChartsApiTest {

    private MockMvc mockMvc;

    @Mock
    private ChartsService chartsService;

    @Mock
    private AnalysisService analysisService;

    @InjectMocks
    private ChartsController chartsController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(chartsController).build();
    }

    @Test
    void shouldGetMonthlyPieChart() throws Exception {
        PieChartDto pie = new PieChartDto(
            List.of("Comida"),
            List.of(120.5),
            List.of("#ffffff"),
            new BigDecimal("120.50")
        );

        when(chartsService.getCategoryStadsByMonth("user@test.com", 1L, 2026, 6, TransactionType.EXPENSE))
            .thenReturn(pie);

        Authentication authentication = new UsernamePasswordAuthenticationToken("user@test.com", null);

        mockMvc.perform(get("/api/v1/charts/pie/monthly")
                .principal(authentication)
                .param("accountId", "1")
                .param("year", "2026")
                .param("month", "6")
                .param("type", "EXPENSE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.labels[0]").value("Comida"));

        verify(chartsService).getCategoryStadsByMonth("user@test.com", 1L, 2026, 6, TransactionType.EXPENSE);
    }

    @Test
    void shouldReturnBadRequestWhenMonthlyPieChartFails() throws Exception {
        when(chartsService.getCategoryStadsByMonth("user@test.com", 1L, 2026, 6, TransactionType.EXPENSE))
            .thenThrow(new RuntimeException("boom"));

        Authentication authentication = new UsernamePasswordAuthenticationToken("user@test.com", null);

        mockMvc.perform(get("/api/v1/charts/pie/monthly")
                .principal(authentication)
                .param("accountId", "1")
                .param("year", "2026")
                .param("month", "6")
                .param("type", "EXPENSE"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetForecastBalanceChart() throws Exception {
        BarLineChartDto incomes = new BarLineChartDto(List.of("Jun"), List.of(500f));
        BarLineChartDto expenses = new BarLineChartDto(List.of("Jun"), List.of(200f));

        when(analysisService.getForecastBalance("user@test.com", 2L, TransactionType.INCOME)).thenReturn(incomes);
        when(analysisService.getForecastBalance("user@test.com", 2L, TransactionType.EXPENSE)).thenReturn(expenses);

        Authentication authentication = new UsernamePasswordAuthenticationToken("user@test.com", null);

        mockMvc.perform(get("/api/v1/charts/forecast-balance")
                .principal(authentication)
                .param("accountId", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.forecastBalanceIncomesChart.labels[0]").value("Jun"))
            .andExpect(jsonPath("$.forecastBalanceExpensesChart.data[0]").value(200.0));

        verify(analysisService).getForecastBalance("user@test.com", 2L, TransactionType.INCOME);
        verify(analysisService).getForecastBalance("user@test.com", 2L, TransactionType.EXPENSE);
    }
}
