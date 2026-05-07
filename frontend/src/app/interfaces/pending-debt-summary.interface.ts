export interface PendingDebtSummary {
  debtId: number;
  transactionId: number;
  transactionTitle: string;
  debtorName: string;
  amount: number;
  transactionDate: string;
  accountId: number;
  accountName: string;
}
