package com.smartspend.transaction;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.smartspend.transaction.dtos.DebtResponseDto;
import com.smartspend.transaction.dtos.TransactionResponseDto;

@Component
public class TransactionMapper {
    
    public TransactionResponseDto toResponseDto(Transaction transaction) {
        List<DebtResponseDto> debts = transaction.getSharedDebts() == null
            ? Collections.emptyList()
            : transaction.getSharedDebts().stream()
                .map(d -> new DebtResponseDto(d.getId(), d.getName(), d.getAmount(), d.getIsPaid()))
                .collect(Collectors.toList());

        return new TransactionResponseDto(
            transaction.getId(),
            transaction.getTitle(),
            transaction.getDescription(),
            transaction.getAmount(),
            transaction.getEffectiveAmount(),
            transaction.getDate(),
            transaction.getType(),
            transaction.getRecurrence(),
            transaction.getAccount().getId(),
            transaction.getAccount().getAccountName(),
            transaction.getCategory(),
            transaction.getExcludeFromStats(),
            debts,
            transaction.hasImage(),
            transaction.getImageBase64(),
            transaction.getImageName(),
            transaction.getImageType()
        );
    }
}
