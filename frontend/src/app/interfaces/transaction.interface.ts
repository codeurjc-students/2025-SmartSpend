import { Category } from "./category.interface";

export interface Transaction {
  id: number;
  title: string;
  description?: string;
  amount: number;
  date: string;
  type: 'EXPENSE' | 'INCOME';
  recurrence: 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY';
  accountId: number;
  accountName: string;
  category: Category; // Objeto Category completo
  // Nuevos campos para imagen
  hasImage: boolean;
  imageBase64?: string | null;
  imageName?: string | null;
  imageType?: string | null;
}