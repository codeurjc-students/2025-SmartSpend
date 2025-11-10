import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


export interface BankAccount {
  id: number;
  accountName: string; // Cambiar de 'name' a 'accountName'
  currentBalance: number; // Cambiar de 'balance' a 'currentBalance'
  createdAt?: string; // Añadir campo de fecha
  accountTransactions?: any[]; // Usar any[] para evitar conflictos
}

export interface CreateBankAccount {
  accountName: string; // Cambiar String a string
  initialBalance: number;
}


@Injectable({
  providedIn: 'root'
})


export class BankAccountServiceService {

  private apiUrl = environment.apiUrl 

  constructor(private http: HttpClient) { }

  getBankAccounts(): Observable<BankAccount[]>{
    return this.http.get<BankAccount[]>(`${this.apiUrl}/accounts`);
  }

  getBankAccount(accountId: number): Observable<BankAccount>{
    return this.http.get<BankAccount>(`${this.apiUrl}/accounts/${accountId}`);
  }

  createBankAccount(create: CreateBankAccount): Observable<BankAccount>{
    
    return this.http.post<BankAccount>(`${this.apiUrl}/accounts`, create);
  }

  deleteBankAccount(accountId: number): Observable<void>{
    return this.http.delete<void>(`${this.apiUrl}/accounts/${accountId}`);
  }

















}
