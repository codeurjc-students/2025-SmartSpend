package com.smartspend.bankAccount.dtos;

import java.math.BigDecimal;

public record CreateBankAccountDTO(
String accountName,
BigDecimal initialBalance

) {
    
}
