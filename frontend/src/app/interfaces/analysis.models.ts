import type { Transaction } from './transaction.interface';

export interface ForecastBarLineChartDto {
  labels: string[];
  data: number[];
}

export interface ForecastBalanceDto {
  forecastBalanceIncomesChart: ForecastBarLineChartDto;
  forecastBalanceExpensesChart: ForecastBarLineChartDto;
}

export interface CategoryMetricsDto {
  lastMonthAmount: number;
  currentMonthActual: number;
  currentMonthForecast: number;
  variationPercentage: number;
  trend: 'UP' | 'DOWN' | 'STABLE';
}

export interface CategoryTrendDto {
  categories: Record<string, CategoryMetricsDto>;
}

export interface FixedExpensesDto {
  fixedExpenses: Transaction[];
}
