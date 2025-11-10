package com.smartspend.bankAccount;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartspend.bankAccount.dtos.CreateBankAccountDTO;

import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

@Service
public class BankAccountService {
    
    @Autowired 
    BankAccountRepository bankAccountRepository;

    @Autowired 
    UserRepository userRepository;


    public BankAccount createBankAccount(CreateBankAccountDTO bankAccountDto, String email) {
        
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal initialBalance = bankAccountDto.initialBalance() != null ? bankAccountDto.initialBalance() : BigDecimal.ZERO;

        BankAccount newAcount = new BankAccount(user, bankAccountDto.accountName(), initialBalance);
        
        return bankAccountRepository.save(newAcount);
    }


    public List<BankAccount> getUserBankAccountsByEmail(String email){
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        return bankAccountRepository.findByUser_UserId(user.getUserId());

    }

    public BankAccount getBankAccountByIdAndEmail(Long accountId, String email) {
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        return bankAccountRepository.findByIdAndUser_UserId(accountId, user.getUserId())
            .orElseThrow(() -> new RuntimeException("Bank account not found"));
    }


    public void deleteBankAccount(BankAccount account) {
        bankAccountRepository.delete(account);
    }    
}
