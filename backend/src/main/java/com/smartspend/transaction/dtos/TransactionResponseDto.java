package com.smartspend.transaction.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.smartspend.transaction.TransactionType;
import com.smartspend.transaction.Recurrence;
import com.smartspend.category.Category;

public record TransactionResponseDto(
    Long id,
    String title,
    String description,
    BigDecimal amount,
    LocalDate date,
    TransactionType type,
    Recurrence recurrence,
    Long accountId,
    String accountName,
    Category category, // Objeto Category completo
    // Campos de imagen - null si no tiene imagen
    Boolean hasImage,
    String imageBase64,  // null si no tiene imagen
    String imageName,    // null si no tiene imagen
    String imageType     // null si no tiene imagen
) {}