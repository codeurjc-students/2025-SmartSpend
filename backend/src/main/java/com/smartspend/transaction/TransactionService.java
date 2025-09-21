package com.smartspend.transaction;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service 
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    public TransactionService(TransactionRepository transactionRepository){ this.transactionRepository = transactionRepository; }
    public List<Transaction> findAll(){ return transactionRepository.findAll(); }

}