import { Observable } from 'rxjs';
import { CreateTransactionDto } from '../../interfaces/create-transaction.interface';
import { Transaction } from '../../interfaces/transaction.interface';
import { PaginatedResponse, TransactionFilters } from '../../interfaces/pagination.interface';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Injectable } from '@angular/core';
import { CreateTransactionWithImageDto } from '../../interfaces/create-transaction-with-image.interface';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  getRecentTransactionsByAccount(accountId: number, limit: number = 5): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${this.apiUrl}/transactions/account/${accountId}?limit=${limit}`);
  }

  getTransactions(): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${this.apiUrl}/transactions`);   // get Transactions list
  }

  getTransactionById(transactionId: number): Observable<Transaction> {
    return this.http.get<Transaction>(`${this.apiUrl}/transactions/${transactionId}`);
  }

  createTransaction(transaction: CreateTransactionDto): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.apiUrl}/transactions`, transaction);
  }

  createTransactionWithImage(transactionData: CreateTransactionWithImageDto): Observable<Transaction> {
    const formData = new FormData();
    
    // Agregar todos los campos de la transacción directamente (no anidados)
    formData.append('title', transactionData.title);
    formData.append('amount', transactionData.amount.toString());
    formData.append('date', transactionData.date);
    formData.append('type', transactionData.type);
    formData.append('recurrence', transactionData.recurrence);
    formData.append('accountId', transactionData.accountId.toString());
    
    if (transactionData.categoryId) {
      formData.append('categoryId', transactionData.categoryId);
    }
    
    if (transactionData.description) {
      formData.append('description', transactionData.description);
    }
    
    // Agregar la imagen si existe
    if (transactionData.imageFile) {
      formData.append('imageFile', transactionData.imageFile); // Nota: 'imageFile' no 'image'
    }

    return this.http.post<Transaction>(`${this.apiUrl}/transactions/with-image`, formData);
  }

  // Actualizar transacción
  updateTransaction(transactionData: any): Observable<Transaction> {
    const formData = new FormData();
    
    // Agregar campos básicos
    formData.append('title', transactionData.title);
    formData.append('amount', transactionData.amount.toString());
    formData.append('date', transactionData.date);
    formData.append('type', transactionData.type);
    formData.append('recurrence', transactionData.recurrence);
    formData.append('accountId', transactionData.accountId.toString());
    
    if (transactionData.categoryId) {
      formData.append('categoryId', transactionData.categoryId);
    }
    
    if (transactionData.description) {
      formData.append('description', transactionData.description);
    }
    
    // Agregar imagen si existe
    if (transactionData.imageFile) {
      formData.append('imageFile', transactionData.imageFile);
    }

    return this.http.put<Transaction>(`${this.apiUrl}/transactions/${transactionData.id}`, formData);
  }


  deleteTransaction(transactionId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/transactions/${transactionId}`);
  }

  // Método para obtener transacciones paginadas con filtros
  getTransactionsPaginated(
    accountId: number,
    page: number = 0,
    size: number = 10,
    filters: TransactionFilters
  ): Observable<PaginatedResponse<Transaction>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    // Aplicar filtros si existen
    if (filters.search) params = params.set('search', filters.search);
    if (filters.type) params = params.set('type', filters.type);
    if (filters.dateFrom) params = params.set('dateFrom', filters.dateFrom);
    if (filters.dateTo) params = params.set('dateTo', filters.dateTo);
    if (filters.minAmount) params = params.set('minAmount', filters.minAmount.toString());
    if (filters.maxAmount) params = params.set('maxAmount', filters.maxAmount.toString());

    return this.http.get<PaginatedResponse<Transaction>>(
      `${this.apiUrl}/transactions/account/${accountId}/paginated`,
      { params }
    );
  }
  
}