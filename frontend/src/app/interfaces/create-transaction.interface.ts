export interface DebtDto {
    name: string;
    amount: number;
    isPaid: boolean;
}

export interface CreateTransactionDto {
    title: string;
    description?: string;
    amount: number;
    type: 'EXPENSE' | 'INCOME';
    recurrence: 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY';
    categoryId?: string;
    date: string;
    accountId: number;
    personalAmount?: number;
    excludeFromStats?: boolean;
    debts?: DebtDto[];
}

export interface CreateTransactionWithImageDto extends CreateTransactionDto {
    imageFile?: File;
}
