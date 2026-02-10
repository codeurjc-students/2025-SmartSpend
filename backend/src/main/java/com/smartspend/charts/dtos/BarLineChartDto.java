package com.smartspend.charts.dtos;

import java.util.List;

public record BarLineChartDto(
    List <String> labels, // ["Comida", "Transporte", "Ocio"]
    List<DatasetDto> datasets // [{label: "Gastos", data: [450, 230, 180]}]
) {
    
}
