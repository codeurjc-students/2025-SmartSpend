import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

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

  get totalFixedExpenses(): number {
    return this.fixedExpenses.reduce((total, tx) => total + Number(tx.amount ?? 0), 0);
  }

  isPaidThisMonth(transactionDate: string): boolean {
    const txDate = new Date(transactionDate);
    const now = new Date();

    return (
      txDate.getFullYear() === now.getFullYear() &&
      txDate.getMonth() === now.getMonth() &&
      txDate.getDate() <= now.getDate()
    );
  }

  trackByTransactionId(index: number, tx: Transaction): number {
    return tx.id ?? index;
  }
}
