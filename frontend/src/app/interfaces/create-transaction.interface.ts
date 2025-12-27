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

// Nueva interfaz para transacciones con imagen
export interface CreateTransactionWithImageDto extends CreateTransactionDto {
    imageFile?: File;
}