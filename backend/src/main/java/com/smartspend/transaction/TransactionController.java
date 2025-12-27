package com.smartspend.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.smartspend.transaction.dtos.CreateTransactionDto;
import com.smartspend.transaction.dtos.CreateTransactionWithImageDto;
import com.smartspend.transaction.dtos.TransactionResponseDto;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<TransactionResponseDto> getAllTransactions(Authentication authentication) {

        String userEmail = authentication.getName();

        return transactionService.findAll(userEmail);
    }

    @GetMapping("/account/{accountId}")
    public List<TransactionResponseDto> getTransactionsByAccount(@PathVariable Long accountId, @RequestParam int limit, Authentication authentication) {
        String userEmail = authentication.getName();

        return transactionService.getRecentTransactionsByAccount(accountId, limit, userEmail);
    }


    @GetMapping("/account/{accountId}/paginated")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionsByAccountPaginated(
            @PathVariable Long accountId,
            @PageableDefault(size = 5, sort = "date", direction = Sort.Direction.DESC) Pageable pageable, // ✅ Valores por defecto
            Authentication authentication) {
        String userEmail = authentication.getName();
        Page<TransactionResponseDto> transactionsPage = transactionService.getTransactionsByAccount(accountId, userEmail, pageable);
        return ResponseEntity.ok(transactionsPage);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDto> getTransactionById(@PathVariable Long transactionId, Authentication authentication){
        String userEmail = authentication.getName();

        Optional<TransactionResponseDto> transaction = transactionService.getTransactionById(transactionId);
        if (transaction.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transaction.get());
    }

    @DeleteMapping("/{transactionId}")
    public void deleteTransaction(@PathVariable Long transactionId, Authentication authentication) {
        String userEmail = authentication.getName();

        System.out.println("Deleting transaction with ID: " + transactionId + " for user: " + userEmail);
        
        transactionService.deleteTransaction(transactionId, userEmail);
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDto> createTransaction(@RequestBody CreateTransactionDto transactionDto, Authentication authentication){

        String userEmail = authentication.getName();

        TransactionResponseDto transaction = transactionService.saveTransaction(transactionDto, userEmail);
        
        return ResponseEntity.ok(transaction);

    }

    @PostMapping("/with-image")
    public ResponseEntity<TransactionResponseDto> createTransactionWithImage(
            @ModelAttribute CreateTransactionWithImageDto transactionWithImageDto,
            Authentication authentication) {
        
        try {
            System.out.println("🎯 Endpoint /with-image alcanzado");
            String userEmail = authentication.getName();
            System.out.println("🎯 User email: " + userEmail);
            System.out.println("🎯 DTO Title: " + transactionWithImageDto.getTitle());
            System.out.println("🎯 DTO Amount: " + transactionWithImageDto.getAmount());
            System.out.println("🎯 Has image: " + (transactionWithImageDto.getImageFile() != null));
            
            TransactionResponseDto transaction = transactionService.saveTransactionWithImage(
                transactionWithImageDto, 
                userEmail
            );
            
            System.out.println("🎯 Transaction created successfully");
            return ResponseEntity.ok(transaction);
            
        } catch (IllegalArgumentException e) {
            System.err.println("❌ IllegalArgumentException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("❌ Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}