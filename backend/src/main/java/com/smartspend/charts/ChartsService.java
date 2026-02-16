package com.smartspend.charts;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.rsocket.RSocketProperties.Server.Spec;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.bankAccount.BankAccountRepository;
import com.smartspend.charts.dtos.BarLineChartDto;
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

        // ✅ USAR QUERY OPTIMIZADA DIRECTA
        List<Object[]> categoryTotalsResult = transactionRepository.findCategoryTotalsByAccountAndDateRangeAndType(
            account.getId(), dateFrom, dateTo, transactionType);
        
        // Construir el mapa de resultados
        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (Object[] result : categoryTotalsResult) {
            String categoryName = (String) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            categoryTotals.put(categoryName, amount);
            totalAmount = totalAmount.add(amount);
        }

        return buildPieChartDto(categoryTotals, totalAmount);


    }


    public PieChartDto getCategoryStadsByYear(String userEmail, Long accountId, int year, TransactionType transactionType){

        User user = userRepository.findByUserEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount account = bankAccountRepository.findByIdAndUser_UserId(accountId, user.getUserId())
            .orElseThrow(() -> new RuntimeException("Bank account not found"));
    

        // controller gives year and month correctly
        LocalDate dateFrom = LocalDate.of(year, 1, 1);
        LocalDate dateTo;

        if (year == LocalDate.now().getYear()) {
            dateTo = LocalDate.now(); 
        } else {
            dateTo = LocalDate.of(year, 12, 31);
        }

        
        List<Object[]> categoryTotalsResult = transactionRepository.findCategoryTotalsByAccountAndDateRangeAndType(
            account.getId(), dateFrom, dateTo, transactionType);
        
        // Construir el mapa de resultados
        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (Object[] result : categoryTotalsResult) {
            String categoryName = (String) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            categoryTotals.put(categoryName, amount);
            totalAmount = totalAmount.add(amount);
        }

        return buildPieChartDto(categoryTotals, totalAmount);


    }

    public PieChartDto buildPieChartDto(Map<String, BigDecimal> categoryTotals, BigDecimal totalAmount){


        List<String> labels = new ArrayList<>(categoryTotals.keySet());
        List<Double> data = categoryTotals.values().stream()
                    .map(BigDecimal::doubleValue)
                    .collect(Collectors.toList());


        return new PieChartDto(labels, data, null, totalAmount);

    }


    public BarLineChartDto getBarLineChartByMonth(String userEmail, Long accountId, int year, int month){

        User user = userRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount account = bankAccountRepository.findByIdAndUser_UserId(accountId, user.getUserId())
            .orElseThrow(() -> new RuntimeException("Bank account not found"));


        LocalDate dateFrom = LocalDate.of(year, month, 1);
        LocalDate dateTo = dateFrom.withDayOfMonth(dateFrom.lengthOfMonth());

        
        BigDecimal incomesTotal = transactionRepository.findTotalByAccountAndDateRangeAndType(
            accountId, dateFrom, dateTo, TransactionType.INCOME);
        BigDecimal expensesTotal = transactionRepository.findTotalByAccountAndDateRangeAndType(
            accountId, dateFrom, dateTo, TransactionType.EXPENSE);
        
        // Manejar valores null
        if (incomesTotal == null) incomesTotal = BigDecimal.ZERO;
        if (expensesTotal == null) expensesTotal = BigDecimal.ZERO;

        List<String> labels = List.of("Incomes", "Expenses");
        List<Float> data = List.of(incomesTotal.floatValue(), expensesTotal.floatValue());



        return new BarLineChartDto(labels, data);

    }

    public BarLineChartDto getBarLineChartByYear(String userEmail, Long accountId, int year){

        User user = userRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount account = bankAccountRepository.findByIdAndUser_UserId(accountId, user.getUserId())
            .orElseThrow(() -> new RuntimeException("Bank account not found"));


        LocalDate dateFrom = LocalDate.of(year, 1, 1);
        LocalDate dateTo;
        
        if (year == LocalDate.now().getYear()) {
            dateTo = LocalDate.now(); 
        } else {
            dateTo = LocalDate.of(year, 12, 31);
        }

        
        BigDecimal incomesTotal = transactionRepository.findTotalByAccountAndDateRangeAndType(
            accountId, dateFrom, dateTo, TransactionType.INCOME);
        BigDecimal expensesTotal = transactionRepository.findTotalByAccountAndDateRangeAndType(
            accountId, dateFrom, dateTo, TransactionType.EXPENSE);
        
        
        if (incomesTotal == null) incomesTotal = BigDecimal.ZERO;
        if (expensesTotal == null) expensesTotal = BigDecimal.ZERO;

        List<String> labels = List.of("Incomes", "Expenses");
        List<Float> data = List.of(incomesTotal.floatValue(), expensesTotal.floatValue());



        return new BarLineChartDto(labels, data);

    }





}
