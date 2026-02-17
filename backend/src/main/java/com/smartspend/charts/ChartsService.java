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
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.bankAccount.BankAccountRepository;
import com.smartspend.charts.dtos.BarLineChartDto;
import com.smartspend.charts.dtos.LineChartDto;
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


    public LineChartDto getTimeLineChartByMonth(String userEmail, Long accountId, int year, int month){

        User user = userRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount account = bankAccountRepository.findByIdAndUser_UserId(accountId, user.getUserId())
            .orElseThrow(() -> new RuntimeException("Bank account not found"));
    
        LocalDate dateFrom = LocalDate.of(year, month, 1);
        LocalDate dateTo = dateFrom.withDayOfMonth(dateFrom.lengthOfMonth());
        LocalDate today = LocalDate.now();

        List<String> labels = new ArrayList<>();
        List<Float> balanceData = new ArrayList<>();
        List<Float> incomesData = new ArrayList<>();
        List<Float> expensesData = new ArrayList<>();

        LocalDate dayBeforeMonth = dateFrom.minusDays(1);
        BigDecimal initialBalance = transactionRepository.findBalanceUpToDate(accountId, dayBeforeMonth);   
        if (initialBalance == null) initialBalance = BigDecimal.ZERO;
        BigDecimal runningBalance = initialBalance;
        BigDecimal incomesRunningAccumulated = BigDecimal.ZERO;
        BigDecimal expensesRunningAccumulated = BigDecimal.ZERO;


        dateTo =  dateTo.isAfter(today) ? today : dateTo;

        for (LocalDate date = dateFrom; !date.isAfter(dateTo); date = date.plusDays(1)) {
            labels.add(String.valueOf(date.getDayOfMonth()));

            BigDecimal dayIncomes = transactionRepository.findTotalByAccountAndDateRangeAndType(accountId, date, date, TransactionType.INCOME);
            BigDecimal dayExpenses = transactionRepository.findTotalByAccountAndDateRangeAndType(accountId, date, date, TransactionType.EXPENSE);
            if (dayIncomes == null) dayIncomes = BigDecimal.ZERO;
            if (dayExpenses == null) dayExpenses = BigDecimal.ZERO;
            
            runningBalance = runningBalance.add(dayIncomes).subtract(dayExpenses);
            balanceData.add(runningBalance.floatValue());

            incomesRunningAccumulated = incomesRunningAccumulated.add(dayIncomes);
            incomesData.add(incomesRunningAccumulated.floatValue());

            expensesRunningAccumulated = expensesRunningAccumulated.add(dayExpenses);
            expensesData.add(expensesRunningAccumulated.floatValue());
        }

        
        return new LineChartDto(labels, balanceData, incomesData, expensesData);

    }   

    public LineChartDto getTimeLineChartByYear(String userEmail, Long accountId, int year) {
        User user = userRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount account = bankAccountRepository.findByIdAndUser_UserId(accountId, user.getUserId())
            .orElseThrow(() -> new RuntimeException("Bank account not found"));
            
        String[] monthNames = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };
        
        LocalDate yearStart = LocalDate.of(year, 1, 1);
        LocalDate today = LocalDate.now();

        List<String> labels = new ArrayList<>();
        List<Float> balanceData = new ArrayList<>();
        List<Float> incomeData = new ArrayList<>();
        List<Float> expenseData = new ArrayList<>();

        // 1. Obtener el balance de la cuenta justo antes del inicio del año
        LocalDate dayBeforeYear = yearStart.minusDays(1);
        BigDecimal initialBalance = transactionRepository.findBalanceUpToDate(accountId, dayBeforeYear);
        if (initialBalance == null) initialBalance = BigDecimal.ZERO; // Asegurar que no sea null
        
        // Inicializar los acumuladores
        BigDecimal runningBalance = initialBalance;
        BigDecimal runningIncomesAccumulated = BigDecimal.ZERO; // Acumulado de ingresos para el año actual
        BigDecimal runningExpensesAccumulated = BigDecimal.ZERO; // Acumulado de gastos para el año actual
        
        // 2. Iterar mes a mes del año
        for (int month = 1; month <= 12; month++) {
            LocalDate monthStart = LocalDate.of(year, month, 1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
            
            // Si el mes es futuro, paramos
            if (monthStart.isAfter(today)) {
                break;
            }
            
            // Ajustar la fecha fin del mes si es el mes actual para no ir al futuro
            LocalDate currentMonthEndDate = monthEnd.isAfter(today) ? today : monthEnd;

            labels.add(monthNames[month - 1]);
            
            // Obtener transacciones para *ese mes específico*
            BigDecimal monthIncomes = transactionRepository.findTotalByAccountAndDateRangeAndType(
                accountId, monthStart, currentMonthEndDate, TransactionType.INCOME);
            BigDecimal monthExpenses = transactionRepository.findTotalByAccountAndDateRangeAndType(
                accountId, monthStart, currentMonthEndDate, TransactionType.EXPENSE);
                
            if (monthIncomes == null) monthIncomes = BigDecimal.ZERO;
            if (monthExpenses == null) monthExpenses = BigDecimal.ZERO;
            
            // Actualizar el running balance
            runningBalance = runningBalance.add(monthIncomes).subtract(monthExpenses);
            balanceData.add(runningBalance.floatValue());

            // Actualizar ingresos y gastos acumulados *para el período del gráfico* (año)
            runningIncomesAccumulated = runningIncomesAccumulated.add(monthIncomes);
            runningExpensesAccumulated = runningExpensesAccumulated.add(monthExpenses);
            incomeData.add(runningIncomesAccumulated.floatValue());
            expenseData.add(runningExpensesAccumulated.floatValue());
        }
        
        return new LineChartDto(labels, balanceData, incomeData, expenseData);
    }
        



}
