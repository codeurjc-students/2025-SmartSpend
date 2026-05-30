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
    recurrence: 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY';
        recurrenceEndDate?: string;
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

export interface TransferDto {
    originAccountId: number;
    destinationAccountId: number;
    amount: number;
    title: string;
    date: string;
    description: string;
    recurrence: 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY';
}

export interface TransferResponseDto {
    originTransactionId: number;
    destinationTransactionId: number;
    amount: number;
    date: number[] | string;
    message: string;
}
