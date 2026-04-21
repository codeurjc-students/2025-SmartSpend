package com.smartspend.charts.dtos;

public record ForecastBalanceDto(
    BarLineChartDto forecastBalanceIncomesChart,
    BarLineChartDto forecastBalanceExpensesChart
) {
    
}
