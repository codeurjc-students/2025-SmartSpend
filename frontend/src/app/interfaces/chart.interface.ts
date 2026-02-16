export interface PieChartDto {
  labels: string[];
  data: number[];
  backgroundColors: string[] | null;
  totalAmount: number;
}

export interface BarLineChartDto {
  labels: string[];
  data: number[];
}

// Nueva interfaz para gráficos comparativos
export interface ComparisonChartDto {
  labels: string[];
  incomes: number[];
  expenses: number[];
}

export enum TransactionType {
  INCOME = 'INCOME',
  EXPENSE = 'EXPENSE'
}