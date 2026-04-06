package com.smartspend.transaction.dtos;

import java.math.BigDecimal;

public record DebtResponseDto(
    Long id,
    String name,
    BigDecimal amount,
    Boolean isPaid
) {}
