export interface RecurringTreeChild {
  id: number;
  date: string;
  amount: number;
}

export interface RecurringTreeParent {
  id: number;
  title: string;
  amount: number;
  recurrence: 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY';
  nextRecurrenceDate: string | null;
  childTransactions: RecurringTreeChild[];
}
