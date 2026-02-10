package com.smartspend.charts.dtos;

import java.util.List;

public record DatasetDto(

    String label,
    List<Double> data,
    String backgroundColor,
    String borderColor
) {
    
}
