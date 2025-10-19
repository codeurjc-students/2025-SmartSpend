import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Transaction {

  id: number;
  title: string;
  description: string;
  amount: number;
  date: string;
  category: string;
  recurrence: string;
}


@Injectable({
  providedIn: 'root'
})
export class TransactionService {

  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  getTransactions(): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${this.apiUrl}/transactions`);   // get Transactions list
  }
}
