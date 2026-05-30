package com.smartspend.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.io.IOException;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.Valid;

import com.smartspend.transaction.dtos.CreateTransactionDto;
import com.smartspend.transaction.dtos.CreateTransactionWithImageDto;
import com.smartspend.transaction.dtos.PendingDebtSummaryDto;
import com.smartspend.transaction.dtos.RecurringTreeResponseDto;
import com.smartspend.transaction.dtos.TransactionResponseDto;
import com.smartspend.transaction.dtos.TransferRequestDto;
import com.smartspend.transaction.dtos.TransferResponseDto;

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
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) String dateFrom,
        @RequestParam(required = false) String dateTo,
        @RequestParam(required = false) BigDecimal minAmount,
        @RequestParam(required = false) BigDecimal maxAmount,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Boolean isPending,
        @PageableDefault(size = 5, sort = "date", direction = Sort.Direction.DESC) Pageable pageable, // ✅ Valores por defecto
        Authentication authentication) {
            String userEmail = authentication.getName();
            Page<TransactionResponseDto> transactionsPage = transactionService.getTransactionsByAccount(accountId, userEmail, search, type, dateFrom, dateTo, minAmount, maxAmount, categoryId, isPending, pageable);
            return ResponseEntity.ok(transactionsPage);
    }

    @GetMapping("/pending-summary")
    public List<PendingDebtSummaryDto> getPendingDebtsSummary(Authentication authentication) {
        String userEmail = authentication.getName();
        return transactionService.getPendingDebtsSummary(userEmail, 5);
    }

    @GetMapping("/recurring-tree/{accountId}")
    public ResponseEntity<List<RecurringTreeResponseDto>> getRecurringTreeByAccount(
            @PathVariable Long accountId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        List<RecurringTreeResponseDto> recurringTree = transactionService.getRecurringTreeByAccount(accountId, userEmail);
        return ResponseEntity.ok(recurringTree);
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

        // Verificar si es un ajuste de deuda para usar la lógica especial
        TransactionResponseDto transaction = transactionService.getTransactionById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (Boolean.TRUE.equals(transaction.excludeFromStats()) && 
            transaction.title().startsWith("Ajuste deuda: ")) {
            transactionService.deleteAdjustmentAndRestoreDebt(transactionId, userEmail);
        } else {
            transactionService.deleteTransaction(transactionId, userEmail);
        }
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDto> createTransaction(@RequestBody CreateTransactionDto transactionDto, Authentication authentication){

        String userEmail = authentication.getName();

        TransactionResponseDto transaction = transactionService.saveTransaction(transactionDto, userEmail);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);

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
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
            
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

    @PutMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDto> updateTransaction(
            @PathVariable Long transactionId,
            @ModelAttribute CreateTransactionDto transactionDto,
            Authentication authentication) {

        String userEmail = authentication.getName();

        Optional<TransactionResponseDto> updatedTransaction = transactionService.updateTransaction(transactionId, transactionDto, userEmail);

        if (updatedTransaction.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedTransaction.get());
    }

    @PatchMapping("/{transactionId}/debts/{debtId}/pay")
    public ResponseEntity<TransactionResponseDto> markDebtAsPaid(
            @PathVariable Long transactionId,
            @PathVariable Long debtId,
            Authentication authentication) {

        String userEmail = authentication.getName();
        TransactionResponseDto updatedTransaction = transactionService.markDebtAsPaid(transactionId, debtId, userEmail);
        return ResponseEntity.ok(updatedTransaction);
    }

    @PatchMapping("/{transactionId}/cancel-recurrence")
    public ResponseEntity<Map<String, String>> cancelRecurrence(
            @PathVariable Long transactionId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        String message = transactionService.cancelRecurrence(transactionId, userEmail);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDto> resolveTransferBetweenAccounts(
        @Valid @RequestBody TransferRequestDto request, Authentication authentication
    ) {
        String userEmail = authentication.getName();
        TransferResponseDto response = transactionService.createTransfers(request, userEmail);
        return ResponseEntity.ok(response);







    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getDefaultMessage())
            .orElse("Invalid request");
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }
    
}
