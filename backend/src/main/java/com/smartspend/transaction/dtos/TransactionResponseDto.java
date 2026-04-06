package com.smartspend.transaction.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.smartspend.transaction.TransactionType;
import com.smartspend.transaction.Recurrence;
import com.smartspend.category.Category;

public record TransactionResponseDto(
    Long id,
    String title,
    String description,
    BigDecimal amount,
    BigDecimal effectiveAmount,
    LocalDate date,
    TransactionType type,
    Recurrence recurrence,
    Long accountId,
    String accountName,
    Category category,
    Boolean excludeFromStats,
    List<DebtResponseDto> debts,
    // Campos de imagen - null si no tiene imagen
    Boolean hasImage,
    String imageBase64,
    String imageName,
    String imageType
) {}
