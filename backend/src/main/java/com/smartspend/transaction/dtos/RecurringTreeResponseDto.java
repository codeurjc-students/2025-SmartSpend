package com.smartspend.transaction.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.smartspend.transaction.Recurrence;

public record RecurringTreeResponseDto(
    Long id,
    String title,
    BigDecimal amount,
    Recurrence recurrence,
    LocalDate nextRecurrenceDate,
    List<ChildTransactionDto> childTransactions
) {
    public record ChildTransactionDto(
        Long id,
        LocalDate date,
        BigDecimal amount
    ) {}
}
