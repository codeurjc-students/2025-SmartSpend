package com.smartspend.transaction.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PendingDebtSummaryDto(
    Long debtId,
    Long transactionId,
    String transactionTitle,
    String debtorName,
    BigDecimal amount,
    LocalDate transactionDate,
    Long accountId,
    String accountName
) {}
