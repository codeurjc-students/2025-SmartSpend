package com.smartspend.charts.dtos;

import java.math.BigDecimal;
import java.util.List;

public record PieChartDto(
    List<String> labels,
    List<Double> data,
    List<String> backgroundColors,
    BigDecimal totalAmount
) {
}   