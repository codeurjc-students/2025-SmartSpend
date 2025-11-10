package com.smartspend.bankAccount;

import org.springframework.security.core.Authentication;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartspend.bankAccount.dtos.CreateBankAccountDTO;


@RestController
@RequestMapping("/api/v1/accounts")
public class BankAccountController {

    @Autowired
    BankAccountService bankAccountService;

    @GetMapping
    public ResponseEntity<List<BankAccount>> getBankAccounts(Authentication authentication) {

        String email = authentication.getName();

        List<BankAccount> accounts = bankAccountService.getUserBankAccountsByEmail(email);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<BankAccount> getBankAccountById(@PathVariable Long accountId, Authentication authentication) {

        String email = authentication.getName();

        BankAccount accountDetails = bankAccountService.getBankAccountByIdAndEmail(accountId, email);
        

        return ResponseEntity.ok(accountDetails);
    }



    @PostMapping
    public ResponseEntity<BankAccount> createBankAccount(@RequestBody CreateBankAccountDTO bankAccountDto, Authentication authentication) {

        String email = authentication.getName();

        BankAccount newAccount = bankAccountService.createBankAccount(bankAccountDto, email);

        return ResponseEntity.ok(newAccount); 
    }


    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteBankAccount(@PathVariable Long accountId, Authentication authentication)
    {
        String email = authentication.getName();

        BankAccount accountToDelete = bankAccountService.getBankAccountByIdAndEmail(accountId, email);

        bankAccountService.deleteBankAccount(accountToDelete);

        return ResponseEntity.noContent().build();
    }


}
