package com.smartspend.transaction;

import org.springframework.stereotype.Component;

import com.smartspend.transaction.dtos.TransactionResponseDto;

@Component
public class TransactionMapper {
    
    /**
     * Convierte una entidad Transaction a TransactionResponseDto
     */
    public TransactionResponseDto toResponseDto(Transaction transaction) {
        return new TransactionResponseDto(
            transaction.getId(),
            transaction.getTitle(),
            transaction.getDescription(),
            transaction.getAmount(),
            transaction.getDate(),
            transaction.getType(),
            transaction.getRecurrence(),
            transaction.getAccount().getId(),
            transaction.getAccount().getAccountName(),
            transaction.getCategory(), // Objeto Category completo
            transaction.hasImage(),
            transaction.getImageBase64(), // null si no tiene imagen
            transaction.getImageName(),   // null si no tiene imagen
            transaction.getImageType()    // null si no tiene imagen
        );
    }
}