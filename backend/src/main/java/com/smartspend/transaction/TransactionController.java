package com.smartspend.transaction;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartspend.transaction.dtos.CreateTransactionDto;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<Transaction> getAllTransactions(Authentication authentication) {

        String userEmail = authentication.getName();

        return transactionService.findAll(userEmail);
    }

    @GetMapping("/account/{accountId}")
    public List<Transaction> getTransactionsByAccount(@PathVariable Long accountId, @RequestParam int limit, Authentication authentication) {
        String userEmail = authentication.getName();

        return transactionService.getRecentTransactionsByAccount(accountId, limit, userEmail);
    }


    @GetMapping("/account/{accountId}/paginated")
    public ResponseEntity<Page<Transaction>> getTransactionsByAccountPaginated(
            @PathVariable Long accountId,
            @PageableDefault(size = 5, sort = "date", direction = Sort.Direction.DESC) Pageable pageable, // ✅ Valores por defecto
            Authentication authentication) {
        String userEmail = authentication.getName();
        Page<Transaction> transactionsPage = transactionService.getTransactionsByAccount(accountId, userEmail, pageable);
        return ResponseEntity.ok(transactionsPage);
    }

    @DeleteMapping("/{transactionId}")
    public void deleteTransaction(@PathVariable Long transactionId, Authentication authentication) {
        String userEmail = authentication.getName();

        System.out.println("Deleting transaction with ID: " + transactionId + " for user: " + userEmail);
        
        transactionService.deleteTransaction(transactionId, userEmail);
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody CreateTransactionDto transactionDto, Authentication authentication){

        String userEmail = authentication.getName();

        Transaction transaction = transactionService.saveTransaction(transactionDto, userEmail);
        
        return ResponseEntity.ok(transaction);

    }

}
