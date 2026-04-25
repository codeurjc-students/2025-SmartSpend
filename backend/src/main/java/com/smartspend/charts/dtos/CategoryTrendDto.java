package com.smartspend.charts.dtos;




public record CategoryTrendDto(
    java.util.Map<String, CategoryMetricsDto> categories

) {
    public record CategoryMetricsDto(
        float lastMonthAmount,
        float currentMonthActual,
        float currentMonthForecast,
        float variationPercentage, // (Actual vs LastMonth)
        String trend // "UP", "DOWN", "STABLE"
    ) {}

}
