package com.smartspend.charts.dtos;

import java.util.List;

public record BarLineChartDto(
    List <String> labels, // ["Incomes", "Expenses"]
    List<Float> data // [450, 230]
) {
    
}
