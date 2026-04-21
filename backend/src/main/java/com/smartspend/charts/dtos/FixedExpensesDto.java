package com.smartspend.charts.dtos;

import java.util.List;

import com.smartspend.transaction.Transaction;

public record FixedExpensesDto(
    List<Transaction> fixedExpenses
) {}
