package com.smartspend.report;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.bankAccount.BankAccountRepository;
import com.smartspend.charts.ChartsService;
import com.smartspend.charts.dtos.BarLineChartDto;
import com.smartspend.charts.dtos.LineChartDto;
import com.smartspend.charts.dtos.PieChartDto;
import com.smartspend.report.dtos.ReportResponseDTO;
import com.smartspend.report.dtos.StadisticsDto;
import com.smartspend.transaction.Transaction;
import com.smartspend.transaction.TransactionRepository;
import com.smartspend.transaction.TransactionService;
import com.smartspend.transaction.TransactionType;
import com.smartspend.user.User;
import com.smartspend.user.UserRepository;

@Service
public class ReportService {

    @Autowired
    BankAccountRepository bankAccountRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired 
    ChartsService chartsService;

    @Autowired 
    TransactionService transactionService;


    public ReportResponseDTO getResponseData(Long bankAccountId, String userEmail, int year, int month){
        
        
        User user = userRepository.findByUserEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));

        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId).orElseThrow(() -> new RuntimeException("Bank account not found"));

        LocalDate dateFrom = LocalDate.of(year, month, 1);
        LocalDate dateTo = dateFrom.withDayOfMonth(dateFrom.lengthOfMonth());

        List<Transaction> expenseTransactions = transactionRepository.findByAccountAndDateRangeAndType(bankAccountId, dateFrom, dateTo, TransactionType.EXPENSE);
        List<Transaction> incomeTransactions = transactionRepository.findByAccountAndDateRangeAndType(bankAccountId, dateFrom, dateTo, TransactionType.INCOME);

        PieChartDto expensePieChart = chartsService.getCategoryStadsByMonth(user.getUserEmail(), bankAccount.getId(),year, month, TransactionType.EXPENSE);
        PieChartDto incomePieChart = chartsService.getCategoryStadsByMonth(user.getUserEmail(), bankAccount.getId(),year, month, TransactionType.INCOME);
        
        BarLineChartDto barLineChart = chartsService.getBarLineChartByMonth(user.getUserEmail(), bankAccount.getId(), year, month);

        LineChartDto lineChart = chartsService.getTimeLineChartByMonth(user.getUserEmail(), bankAccount.getId(), year, month);

        Float incomesTotal = transactionRepository.findTotalByAccountAndDateRangeAndType(bankAccountId, dateFrom, dateTo, TransactionType.INCOME).floatValue();
        Float expensesTotal = transactionRepository.findTotalByAccountAndDateRangeAndType(bankAccountId, dateFrom, dateTo, TransactionType.EXPENSE).floatValue();
        Float balance = incomesTotal - expensesTotal;

        StadisticsDto stadistics = new StadisticsDto(incomesTotal, expensesTotal, balance);

        ReportResponseDTO response = new ReportResponseDTO(bankAccount, incomeTransactions, expenseTransactions, stadistics, barLineChart, expensePieChart, incomePieChart, lineChart);
        
        return response;
    }
}
