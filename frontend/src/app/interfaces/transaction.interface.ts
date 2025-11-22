import { Category } from "./category.interface";

export interface Transaction {
  id: number;
  title: string;
  description: string;
  amount: number;
  date: string;
  type: 'INCOME' | 'EXPENSE';
  recurrence: 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY';
  category: Category;
}