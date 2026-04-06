package com.smartspend.transaction.dtos;

import java.math.BigDecimal;

public record DebtDto(
    String name,
    BigDecimal amount,
    Boolean isPaid
) {}
