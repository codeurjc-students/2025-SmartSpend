import { Category } from "./category.interface";

export interface DebtResponse {
  id: number;
  name: string;
  amount: number;
  isPaid: boolean;
}

export interface Transaction {
  id: number;
  title: string;
  description?: string;
  amount: number;
  beforeBalance?: number;
  effectiveAmount?: number;
  date: string;
  type: 'EXPENSE' | 'INCOME';
  recurrence: 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY';
  isRecurringSeriesParent?: boolean;
  nextRecurrenceDate?: string | null;
  accountId: number;
  accountName: string;
  category: Category;
  excludeFromStats?: boolean;
  debts?: DebtResponse[];
  hasImage: boolean;
  imageBase64?: string | null;
  imageName?: string | null;
  imageType?: string | null;
}
