import { Observable } from 'rxjs';
import { CreateTransactionDto } from '../../interfaces/create-transaction.interface';
import { Transaction } from '../../interfaces/transaction.interface'; // Asegúrate de que Transaction esté importado desde interfaces
import { HttpClient } from '@angular/common/http';
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
  
}