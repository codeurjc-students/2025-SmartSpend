package com.smartspend.transaction.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.smartspend.transaction.Recurrence;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransferRequestDto(
    @NotNull(message = "Origin account is required")
    Long originAccountId,

    @NotNull(message = "Destination account is required")
    Long destinationAccountId,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    BigDecimal amount,

    @NotBlank(message = "Transfer title is required")
    @Size(max = 100, message = "Transfer title must be at most 100 characters")
    String title,

    LocalDate date,

    @Size(max = 100, message = "Transfer description must be at most 100 characters")
    String description,

    Recurrence recurrence
) {}
