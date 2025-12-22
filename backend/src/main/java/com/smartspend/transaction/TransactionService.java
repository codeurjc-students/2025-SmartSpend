package com.smartspend.transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.bankAccount.BankAccountRepository;
import com.smartspend.category.Category;
import com.smartspend.category.CategoryRepository;
import com.smartspend.transaction.dtos.CreateTransactionDto;
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


    public Optional<Transaction> getTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId);
    }

    public List<Transaction> findAll(String email) {

        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        
        return transactionRepository.findByAccount_User_UserIdOrderByDateDesc(user.getUserId());
    }

    public List<Transaction> getRecentTransactionsByAccount(Long accountId, int limit, String email) {
        User user = userRepository.findByUserEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount account = bankAccountRepository.findByIdAndUser_UserId(accountId, user.getUserId())
            .orElseThrow(() -> new RuntimeException("Bank account not found"));

        return transactionRepository.findByAccountIdAndLimit(accountId, limit);
    }

    public Page<Transaction> getTransactionsByAccount(Long accountId, String userEmail, Pageable pageable) {
        User user = userRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        BankAccount account = bankAccountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Bank account not found"));

        if (!account.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Unauthorized to access this account");
        }

        return transactionRepository.findByAccountIdOrderByDateDesc(accountId, pageable);
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

    public Transaction saveTransaction(CreateTransactionDto transactionDto, String userEmail) {

        User user = userRepository.findByUserEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        
        BankAccount account = bankAccountRepository.findById(transactionDto.accountId())
            .orElseThrow(() -> new RuntimeException("Bank account not found"));

        if (!account.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Unauthorized to add transaction to this account");
        }


        LocalDate transactionDate = transactionDto.date() != null ? transactionDto.date() : LocalDate.now();

        Category category = categoryRepository.findById(transactionDto.categoryId())
            .orElseThrow(() -> new RuntimeException("Category not found"));
        

        Transaction transaction = new Transaction();
        transaction.setTitle(transactionDto.title());
        transaction.setDescription(transactionDto.description());
        transaction.setAmount(transactionDto.amount());
        transaction.setDate(transactionDate);
        transaction.setType(transactionDto.type());
        transaction.setRecurrence(transactionDto.recurrence());
        transaction.setCategory(category);
        transaction.setAccount(account);

        upadateAccountBalance(transaction, account);

        bankAccountRepository.save(account);

        return transactionRepository.save(transaction);

    }
    

    private void upadateAccountBalance(Transaction transaction, BankAccount account) {
        if (transaction.getType() == TransactionType.INCOME){
            account.setCurrentBalance(account.getCurrentBalance().add(transaction.getAmount()));
        } else {
            account.setCurrentBalance(account.getCurrentBalance().subtract(transaction.getAmount()));
        }
    }


    

    



}