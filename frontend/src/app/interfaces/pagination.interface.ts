// Interfaces compartidas para filtros de transacciones
export interface TransactionFilters {
  dateFrom?: string;
  dateTo?: string;
  minAmount?: number;
  maxAmount?: number;
  type?: 'INCOME' | 'EXPENSE' | null;
  categoryId?: string;
  search?: string;
}

// Interfaz para respuesta paginada del backend
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  last: boolean;
  size: number;
  number: number;
  first: boolean;
}