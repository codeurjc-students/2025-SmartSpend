import { Observable } from 'rxjs';
import { CreateTransactionDto } from '../../interfaces/create-transaction.interface';
import { Transaction } from '../../interfaces/transaction.interface'; // Asegúrate de que Transaction esté importado desde interfaces
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Injectable } from '@angular/core';

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


  deleteTransaction(transactionId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/transactions/${transactionId}`);
  }
  
}