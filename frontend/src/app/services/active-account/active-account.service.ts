import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { BankAccount } from '../bankAccount/bank-account-service.service';

@Injectable({
  providedIn: 'root'
})
export class ActiveAccountService {

  private activeAccountSubject = new BehaviorSubject<BankAccount | null>(null);
  public activeAccount$ = this.activeAccountSubject.asObservable();

  constructor() { }

  /**
   * Establece la cuenta activa
   */
  setActiveAccount(account: BankAccount | null): void {
    this.activeAccountSubject.next(account);
    
    // Guardar en localStorage para persistencia
    if (account) {
      localStorage.setItem('activeAccountId', account.id.toString());
    } else {
      localStorage.removeItem('activeAccountId');
    }
  }

  /**
   * Obtiene la cuenta activa actual
   */
  getActiveAccount(): BankAccount | null {
    return this.activeAccountSubject.value;
  }

  /**
   * Obtiene la cuenta activa actual (alias para compatibilidad)
   */
  getActiveAccountValue(): BankAccount | null {
    return this.activeAccountSubject.value;
  }

  /**
   * Obtiene el ID de la cuenta activa guardada en localStorage
   */
  getSavedActiveAccountId(): number | null {
    const savedId = localStorage.getItem('activeAccountId');
    return savedId ? parseInt(savedId, 10) : null;
  }

  /**
   * Limpia la cuenta activa
   */
  clearActiveAccount(): void {
    this.activeAccountSubject.next(null);
    localStorage.removeItem('activeAccountId');
  }
}