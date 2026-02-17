package com.smartspend.charts.dtos;

import java.util.List;

public record LineChartDto(

    List<String> labels,
    List<Float> balanceData,
    List<Float> incomesData,
    List<Float> expensesData


) {
    
}
