import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';

import type { Transaction } from '../../../interfaces/transaction.interface';

@Component({
  selector: 'app-fixed-expenses-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './fixed-expenses-card.component.html',
  styleUrl: './fixed-expenses-card.component.scss'
})
export class FixedExpensesCardComponent {
  @Input({ required: true }) fixedExpenses: Transaction[] = [];

  constructor(private readonly router: Router) {}

  get totalFixedExpenses(): number {
    return this.fixedExpenses.reduce((total, tx) => total + Number(tx.amount ?? 0), 0);
  }

  isPaidThisMonth(tx: Transaction): boolean {
    if (!tx.nextRecurrenceDate) return false;
    const next = new Date(tx.nextRecurrenceDate + 'T00:00:00');
    const now = new Date();
    // Si nextRecurrenceDate ya pasó al mes siguiente, el scheduler ya generó el hijo este mes
    return next.getFullYear() > now.getFullYear() ||
           (next.getFullYear() === now.getFullYear() && next.getMonth() > now.getMonth());
  }

  /**
   * Devuelve la fecha de la ocurrencia de este mes:
   * - Si ya está pagado, calcula la fecha en el mes actual con el mismo día que nextRecurrenceDate
   * - Si está pendiente, devuelve nextRecurrenceDate directamente
   */
  getOccurrenceDateThisMonth(tx: Transaction): Date {
    if (!tx.nextRecurrenceDate) return new Date(tx.date);
    const next = new Date(tx.nextRecurrenceDate + 'T00:00:00');
    if (this.isPaidThisMonth(tx)) {
      const now = new Date();
      return new Date(now.getFullYear(), now.getMonth(), next.getDate());
    }
    return next;
  }

  trackByTransactionId(index: number, tx: Transaction): number {
    return tx.id ?? index;
  }

  openTransactionDetail(transactionId: number): void {
    this.router.navigate(['/transaction', transactionId]);
  }
}
