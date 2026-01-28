package com.smartspend.transaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.bankAccount.BankAccountRepository;
import com.smartspend.category.Category;
import com.smartspend.category.CategoryRepository;
import com.smartspend.config.ImageUtils;
import com.smartspend.transaction.dtos.CreateTransactionDto;
import com.smartspend.transaction.dtos.CreateTransactionWithImageDto;
import com.smartspend.transaction.dtos.TransactionResponseDto;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

@Service 
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired 
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired 
    private ImageUtils imageUtils;

    @Autowired
    private TransactionMapper transactionMapper;


    public Optional<TransactionResponseDto> getTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .map(transactionMapper::toResponseDto);
    }

    public List<TransactionResponseDto> findAll(String email) {

        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Transaction> transactions = transactionRepository.findByAccount_User_UserIdOrderByDateDesc(user.getUserId());
        
        return transactions.stream()
                .map(transactionMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public List<TransactionResponseDto> getRecentTransactionsByAccount(Long accountId, int limit, String email) {
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount account = bankAccountRepository.findByIdAndUser_UserId(accountId, user.getUserId())
            .orElseThrow(() -> new RuntimeException("Bank account not found"));

        List<Transaction> transactions = transactionRepository.findByAccountIdAndLimit(accountId, limit);
        
        return transactions.stream()
                .map(transactionMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public Page<TransactionResponseDto> getTransactionsByAccount(Long accountId, String userEmail, Pageable pageable) {
        User user = userRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        BankAccount account = bankAccountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Bank account not found"));

        if (!account.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Unauthorized to access this account");
        }

        Page<Transaction> transactions = transactionRepository.findByAccountIdOrderByDateDesc(accountId, pageable);
        
        return transactions.map(transactionMapper::toResponseDto);
    }

    public void deleteTransaction(Long transactionId, String email) {

        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));  

        BankAccount account = transaction.getAccount();
        if (!account.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Unauthorized to delete this transaction");
        }

        if (transaction.getType() == TransactionType.INCOME){
            account.setCurrentBalance(account.getCurrentBalance().subtract(transaction.getAmount()));
        } else {
            account.setCurrentBalance(account.getCurrentBalance().add(transaction.getAmount()));
        }
        

        bankAccountRepository.save(account);
        transactionRepository.delete(transaction);
    }

    public TransactionResponseDto saveTransaction(CreateTransactionDto transactionDto, String userEmail) {

        User user = userRepository.findByUserEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        
        BankAccount account = bankAccountRepository.findById(transactionDto.accountId())
            .orElseThrow(() -> new RuntimeException("Bank account not found"));

        if (!account.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Unauthorized to add transaction to this account");
        }


        LocalDate transactionDate = transactionDto.date() != null ? transactionDto.date() : LocalDate.now();

        Category category = categoryRepository.findById(transactionDto.categoryId())
            .orElseThrow(() -> new RuntimeException("Category not found"));
        

        Transaction transaction = Transaction.builder()
            .title(transactionDto.title())
            .description(transactionDto.description())
            .amount(transactionDto.amount())
            .date(transactionDate)
            .type(transactionDto.type())
            .recurrence(transactionDto.recurrence())
            .category(category)
            .account(account)
            .build();

        upadateAccountBalance(transaction, account);

        bankAccountRepository.save(account);

        Transaction savedTransaction = transactionRepository.save(transaction);
        
        return transactionMapper.toResponseDto(savedTransaction);

    }

    public Optional<TransactionResponseDto> updateTransaction(Long transactionId, CreateTransactionDto transactionDto, String userEmail) {
        User user = userRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount account = bankAccountRepository.findById(transactionDto.accountId())
            .orElseThrow(() -> new RuntimeException("Bank account not found"));

        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // Actualiza los campos (sin tocar el id ni la cuenta)
        transaction.setTitle(transactionDto.title());
        transaction.setDescription(transactionDto.description());
        BigDecimal oldAmount = transaction.getAmount();
        transaction.setAmount(transactionDto.amount());
        transaction.setDate(transactionDto.date() != null ? transactionDto.date() : LocalDate.now());
        TransactionType oldType = transaction.getType();
        transaction.setType(transactionDto.type());
        transaction.setRecurrence(transactionDto.recurrence());

        Category category = categoryRepository.findById(transactionDto.categoryId())
            .orElseThrow(() -> new RuntimeException("Category not found"));
        transaction.setCategory(category);

        updateBalanceOfEditTransaction(transaction, oldAmount, oldType, account);
        Transaction updated = transactionRepository.save(transaction);
        bankAccountRepository.save(account);
        return Optional.of(transactionMapper.toResponseDto(updated));
    }


    public TransactionResponseDto saveTransactionWithImage(CreateTransactionWithImageDto transactionDto, String userEmail) {

    
        if (!imageUtils.isValidImage(transactionDto.getImageFile())) {
            throw new RuntimeException("Invalid image file");
        }

        User user = userRepository.findByUserEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount account = bankAccountRepository.findById(transactionDto.getAccountId())
            .orElseThrow(() -> new RuntimeException("Bank account not found"));
        
        Category category = categoryRepository.findById(transactionDto.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found"));

        Transaction transaction = Transaction.builder()
            .title(transactionDto.getTitle())
            .description(transactionDto.getDescription())
            .amount(transactionDto.getAmount())
            .date(transactionDto.getDate() != null ? transactionDto.getDate() : LocalDate.now())
            .type(transactionDto.getType())
            .recurrence(transactionDto.getRecurrence())
            .category(category)
            .account(account)
            .build();
        
        try {
            byte[] imageData = imageUtils.processImage(transactionDto.getImageFile());
            transaction.setImageData(imageData);
            transaction.setImageType(transactionDto.getImageFile().getContentType());
            transaction.setImageName(transactionDto.getImageFile().getOriginalFilename()); 

        } catch (IOException e) {
            throw new RuntimeException("Failed to process image file", e);
        }
        

        upadateAccountBalance(transaction, account);

        bankAccountRepository.save(account);

        Transaction savedTransaction = transactionRepository.save(transaction);
        
        return transactionMapper.toResponseDto(savedTransaction);

    }

    

    private void upadateAccountBalance(Transaction transaction, BankAccount account) {
        if (transaction.getType() == TransactionType.INCOME){
            account.setCurrentBalance(account.getCurrentBalance().add(transaction.getAmount()));
        } else {
            account.setCurrentBalance(account.getCurrentBalance().subtract(transaction.getAmount()));
        }
    }

    private void updateBalanceOfEditTransaction(Transaction transaction, BigDecimal oldAmount, TransactionType oldType, BankAccount account){

        if (oldType == TransactionType.INCOME){
            account.setCurrentBalance(account.getCurrentBalance().subtract(oldAmount)); // substract old income amount to restore balance
        } else { 
            account.setCurrentBalance(account.getCurrentBalance().add(oldAmount)); // add back old expense amount to restore balance
        }
        upadateAccountBalance(transaction, account);
        
    }


    
    

    



}