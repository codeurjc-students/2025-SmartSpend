package com.smartspend.transaction.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransferResponseDto(
    Long originTransactionId,
    Long destinationTransactionId,
    BigDecimal amount,
    LocalDate date,
    String message
) {}


