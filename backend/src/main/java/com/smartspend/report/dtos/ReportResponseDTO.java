package com.smartspend.report.dtos;

import java.util.List;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.charts.dtos.BarLineChartDto;
import com.smartspend.charts.dtos.LineChartDto;
import com.smartspend.charts.dtos.PieChartDto;
import com.smartspend.transaction.Transaction;

public record ReportResponseDTO(

    BankAccount bankAccount,
    List<Transaction> incomesList,
    List<Transaction> expensesList,
    StadisticsDto stadistics,
    BarLineChartDto barLineChart,
    PieChartDto expensePieChart,
    PieChartDto incomePieChart,
    LineChartDto lineChart


) {
    
}
