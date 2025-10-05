package com.smartspend.backend.unit;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartspend.transaction.Transaction;
import com.smartspend.transaction.TransactionRepository;
import com.smartspend.transaction.TransactionService;

public class TransactionServiceTest {
    
    @Mock 
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void returnTransactions(){
        Transaction t1 = new Transaction("Nómina Septiembre", "Salario mensual", null, null, null, null);


        when(transactionRepository.findAll()).thenReturn(List.of(t1));

        List<Transaction> result = transactionService.findAll();

        assertEquals(1, result.size());
        assertEquals("Nómina Septiembre", result.get(0).getTitle());


    }


}
