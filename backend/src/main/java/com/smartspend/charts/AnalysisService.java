package com.smartspend.charts;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebProperties.Resources.Chain.Strategy.Fixed;
import org.springframework.stereotype.Service;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.bankAccount.BankAccountRepository;
import com.smartspend.charts.dtos.BarLineChartDto;
import com.smartspend.charts.dtos.CategoryTrendDto;
import com.smartspend.charts.dtos.FixedExpensesDto;
import com.smartspend.charts.dtos.CategoryTrendDto.CategoryMetricsDto;
import com.smartspend.charts.dtos.ForecastBalanceDto;
import com.smartspend.transaction.Transaction;
import com.smartspend.transaction.TransactionRepository;
import com.smartspend.transaction.TransactionType;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

@Service
public class AnalysisService {
    @Autowired
    UserRepository userRepository;

    @Autowired 
    BankAccountRepository bankAccountRepository;

    @Autowired
    TransactionRepository transactionRepository;


    public BarLineChartDto getForecastBalance(String userEmail, Long accountId, TransactionType type) {
        
        BankAccount account = bankAccountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Bank account not found"));
            
        if (!account.getUser().getUserEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized access: Account does not belong to the user");
        }

        // 2. Dates
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfLastMonth = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(firstDayOfLastMonth.lengthOfMonth());
        LocalDate firstDayOfCurrentMonth = now.withDayOfMonth(1);
        LocalDate firstDayOfMonthsAgo = now.minusMonths(3).withDayOfMonth(1);

        // 3. Database calls with ternary operator to avoid NullPointerExceptions
        BigDecimal lastMonthTotal = transactionRepository.findTotalByAccountAndDateRangeAndType(
            accountId, firstDayOfLastMonth, lastDayOfLastMonth, type);
        lastMonthTotal = lastMonthTotal != null ? lastMonthTotal : BigDecimal.ZERO;

        BigDecimal currentMonthActual = transactionRepository.findTotalByAccountAndDateRangeAndType(
            accountId, firstDayOfCurrentMonth, now, type);
        currentMonthActual = currentMonthActual != null ? currentMonthActual : BigDecimal.ZERO;

        BigDecimal last3MonthsTotal = transactionRepository.findTotalByAccountAndDateRangeAndType(
            accountId, firstDayOfMonthsAgo, lastDayOfLastMonth, type);
        last3MonthsTotal = last3MonthsTotal != null ? last3MonthsTotal : BigDecimal.ZERO;
        
        // 4. Safe calculations with rounding to 2 decimal places
        BigDecimal averageLast3Months = BigDecimal.ZERO;
        if (last3MonthsTotal.compareTo(BigDecimal.ZERO) > 0) {
            averageLast3Months = last3MonthsTotal.divide(BigDecimal.valueOf(90), 2, RoundingMode.HALF_UP);
        }

        BigDecimal daysUntilFinalDayOfMonth = BigDecimal.valueOf(now.lengthOfMonth() - now.getDayOfMonth());
        BigDecimal prevision = currentMonthActual.add(averageLast3Months.multiply(daysUntilFinalDayOfMonth));
        
        
        return new BarLineChartDto(
            List.of("Mes anterior", "Mes actual", "Previsión"),
            List.of(lastMonthTotal.floatValue(), currentMonthActual.floatValue(), prevision.floatValue())
        );
    }


    public CategoryTrendDto getCategoryTrends(String userEmail, Long accountId, TransactionType type) {
    
    
        BankAccount account = bankAccountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Bank account not found"));
        if (!account.getUser().getUserEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized access");
        }

        // 2. Dates
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfLastMonth = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfLastMonth = firstDayOfLastMonth.withDayOfMonth(firstDayOfLastMonth.lengthOfMonth());
        LocalDate firstDayOfCurrentMonth = now.withDayOfMonth(1);
        LocalDate firstDayOfMonthsAgo = now.minusMonths(3).withDayOfMonth(1);
        BigDecimal daysUntilFinalDayOfMonth = BigDecimal.valueOf(now.lengthOfMonth() - now.getDayOfMonth());

        // 3. Database calls and transformation to Maps (Clean magic)
        Map<String, BigDecimal> lastMonthMap = parseCategoryTotals(
            transactionRepository.findCategoryTotalsByAccountAndDateRangeAndType(accountId, firstDayOfLastMonth, lastDayOfLastMonth, type));
            
        Map<String, BigDecimal> currentMonthMap = parseCategoryTotals(
            transactionRepository.findCategoryTotalsByAccountAndDateRangeAndType(accountId, firstDayOfCurrentMonth, now, type));
            
        Map<String, BigDecimal> last3MonthsMap = parseCategoryTotals(
            transactionRepository.findCategoryTotalsByAccountAndDateRangeAndType(accountId, firstDayOfMonthsAgo, lastDayOfLastMonth, type));

        // 4. Recopilar todas las categorías únicas que han tenido movimiento en estos meses
        Set<String> allCategories = new HashSet<>();
        allCategories.addAll(lastMonthMap.keySet());
        allCategories.addAll(currentMonthMap.keySet());
        allCategories.addAll(last3MonthsMap.keySet());

        // 5. Calcular las métricas para cada categoría
        Map<String, CategoryMetricsDto> resultCategories = new HashMap<>();

        for (String category : allCategories) {
            BigDecimal lastMonthTotal = lastMonthMap.getOrDefault(category, BigDecimal.ZERO);
            BigDecimal currentMonthActual = currentMonthMap.getOrDefault(category, BigDecimal.ZERO);
            BigDecimal last3MonthsTotal = last3MonthsMap.getOrDefault(category, BigDecimal.ZERO);

            
            BigDecimal dailyAverage = BigDecimal.ZERO;
            if (last3MonthsTotal.compareTo(BigDecimal.ZERO) > 0) {
                dailyAverage = last3MonthsTotal.divide(BigDecimal.valueOf(90), 2, RoundingMode.HALF_UP);
            }
            BigDecimal forecast = currentMonthActual.add(dailyAverage.multiply(daysUntilFinalDayOfMonth));

            
            float variation = 0.0f;
            if (lastMonthTotal.compareTo(BigDecimal.ZERO) > 0) {
                // (Actual - Pasado) / Pasado * 100
                variation = currentMonthActual.subtract(lastMonthTotal)
                    .divide(lastMonthTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).floatValue();
            } else if (currentMonthActual.compareTo(BigDecimal.ZERO) > 0) {
                variation = 100.0f; // Si el mes pasado fue 0 y este mes hay gasto, sube un 100%
            }

            // -- Tendencia visual --
            String trend = variation > 0 ? "UP" : (variation < 0 ? "DOWN" : "STABLE");

            resultCategories.put(category, new CategoryMetricsDto(
                lastMonthTotal.floatValue(),
                currentMonthActual.floatValue(),
                forecast.floatValue(),
                variation,
                trend
            ));
        }

        return new CategoryTrendDto(resultCategories);
    }

    private Map<String, BigDecimal> parseCategoryTotals(List<Object[]> queryResult) {
        Map<String, BigDecimal> map = new HashMap<>();
        for (Object[] row : queryResult) {
            String categoryName = (String) row[0];
            Number sum = (Number) row[1];
            BigDecimal amount = sum != null ? new BigDecimal(sum.toString()) : BigDecimal.ZERO;
            map.put(categoryName, amount);
        }
        return map;
    }

    public FixedExpensesDto getFixedExpenses(String userEmail, Long accountId) {
        BankAccount account = bankAccountRepository.findById(accountId)
            .orElseThrow(() -> new RuntimeException("Bank account not found"));
        if (!account.getUser().getUserEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized access");
        }

        List<Transaction> fixedExpenses = transactionRepository.findRecurringOrFixedCurrentMonthByAccount(accountId);
        

        return new FixedExpensesDto(fixedExpenses);
    }


    

}
