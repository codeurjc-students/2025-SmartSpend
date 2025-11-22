export interface CreateTransactionDto {
    title: string;
    description?: string;
    amount: number;
    type: 'EXPENSE' | 'INCOME';
    recurrence: 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY';
    categoryId?: string;
    date: string; // Fecha en formato YYYY-MM-DD (YYYY-MM-DD),
    accountId: number;
}