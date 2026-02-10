package com.smartspend.charts;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.bankAccount.BankAccountRepository;
import com.smartspend.charts.dtos.PieChartDto;
import com.smartspend.transaction.Transaction;
import com.smartspend.transaction.TransactionRepository;
import com.smartspend.transaction.TransactionSpecification;
import com.smartspend.transaction.TransactionType;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;


@Service
public class ChartsService {
    
    @Autowired
    UserRepository userRepository;

    @Autowired 
    BankAccountRepository bankAccountRepository;

    @Autowired
    TransactionRepository transactionRepository;
    
    public PieChartDto   getCategoryStadsByMonth(String userEmail, Long accountId, int year, int month, TransactionType transactionType){

        User user = userRepository.findByUserEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount account = bankAccountRepository.findByIdAndUser_UserId(accountId, user.getUserId())
            .orElseThrow(() -> new RuntimeException("Bank account not found"));
    

        // controller gives year and month correctly
        LocalDate dateFrom = LocalDate.of(year, month, 1);
        LocalDate dateTo = dateFrom.withDayOfMonth(dateFrom.lengthOfMonth());

        Specification<Transaction> spec = TransactionSpecification.filterTransactionsForCharts(account.getId(), dateFrom, dateTo, transactionType);

        List<Transaction> transactions = transactionRepository.findAll(spec);

        Map<String, BigDecimal> categoryTotals = transactions.stream()
            .collect(Collectors.groupingBy(
                transaction -> transaction.getCategory().getName(),
                LinkedHashMap::new, 
                Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
            ));
        
        BigDecimal totalAmount = categoryTotals.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);



        return buildPieChartDto(categoryTotals, totalAmount);


    }


    public PieChartDto getCategoryStadsByYear(String userEmail, Long accountId, int year, TransactionType transactionType){

        User user = userRepository.findByUserEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount account = bankAccountRepository.findByIdAndUser_UserId(accountId, user.getUserId())
            .orElseThrow(() -> new RuntimeException("Bank account not found"));
    

        // controller gives year and month correctly
        LocalDate dateFrom = LocalDate.of(year, 1, 1);
        LocalDate dateTo = LocalDate.of(year, 12, 31);

        Specification<Transaction> spec = TransactionSpecification.filterTransactionsForCharts(account.getId(), dateFrom, dateTo, transactionType);

        List<Transaction> transactions = transactionRepository.findAll(spec);

        Map<String, BigDecimal> categoryTotals = transactions.stream()
            .collect(Collectors.groupingBy(
                transaction -> transaction.getCategory().getName(),
                LinkedHashMap::new, 
                Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
            ));
        
        BigDecimal totalAmount = categoryTotals.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);



        return buildPieChartDto(categoryTotals, totalAmount);


    }

    public PieChartDto buildPieChartDto(Map<String, BigDecimal> categoryTotals, BigDecimal totalAmount){


        List<String> labels = new ArrayList<>(categoryTotals.keySet());
        List<Double> data = categoryTotals.values().stream()
                    .map(BigDecimal::doubleValue)
                    .collect(Collectors.toList());


        return new PieChartDto(labels, data, null, totalAmount);

    }



}
