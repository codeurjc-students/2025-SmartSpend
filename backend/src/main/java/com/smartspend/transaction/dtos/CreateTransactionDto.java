package com.smartspend.transaction.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.smartspend.transaction.Recurrence;
import com.smartspend.transaction.TransactionType;

public record CreateTransactionDto(
    String title,
    String description,
    BigDecimal amount,
    TransactionType type,
    LocalDate date,
    Recurrence recurrence,
    Long accountId,
    Long categoryId,
    BigDecimal personalAmount,
    Boolean excludeFromStats,
    List<DebtDto> debts
) {}
