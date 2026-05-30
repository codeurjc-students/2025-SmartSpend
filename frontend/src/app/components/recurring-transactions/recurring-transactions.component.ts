import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';

import { RecurringTreeParent } from '../../interfaces/recurring-tree.interface';
import { ActiveAccountService } from '../../services/active-account/active-account.service';
import { TransactionService } from '../../services/transaction/transaction.service';

@Component({
  selector: 'app-recurring-transactions',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './recurring-transactions.component.html',
  styleUrl: './recurring-transactions.component.css'
})
export class RecurringTransactionsComponent implements OnInit, OnDestroy {
  recurringParents: RecurringTreeParent[] = [];
  loading = true;
  error: string | null = null;
  cancellingId: number | null = null;
  cancelError: string | null = null;

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly transactionService: TransactionService,
    private readonly activeAccountService: ActiveAccountService
  ) {}

  ngOnInit(): void {
    this.activeAccountService.activeAccount$
      .pipe(takeUntil(this.destroy$))
      .subscribe((activeAccount) => {
        const accountId = activeAccount?.id ?? this.activeAccountService.getSavedActiveAccountId();

        if (!accountId) {
          this.loading = false;
          this.recurringParents = [];
          this.error = 'Selecciona una cuenta para ver los gastos recurrentes.';
          return;
        }

        this.loadRecurringTree(accountId);
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  getRecurrenceLabel(recurrence: RecurringTreeParent['recurrence']): string {
    const labels: Record<RecurringTreeParent['recurrence'], string> = {
      NONE: 'Sin recurrencia',
      DAILY: 'Diaria',
      WEEKLY: 'Semanal',
      MONTHLY: 'Mensual',
      YEARLY: 'Anual'
    };

    return labels[recurrence] ?? recurrence;
  }

  private loadRecurringTree(accountId: number): void {
    this.loading = true;
    this.error = null;

    this.transactionService.getRecurringTree(accountId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.recurringParents = data;
          this.loading = false;
        },
        error: (err) => {
          this.error = err?.error?.message || 'No se pudo cargar el árbol de recurrentes.';
          this.recurringParents = [];
          this.loading = false;
        }
      });
  }

  cancelRecurrence(parentId: number): void {
    if (!confirm('¿Estás seguro de que deseas cancelar esta suscripción? No se generarán más cobros.')) {
      return;
    }

    this.cancellingId = parentId;
    this.cancelError = null;

    this.transactionService.cancelRecurrence(parentId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.recurringParents = this.recurringParents.filter(p => p.id !== parentId);
          this.cancellingId = null;
        },
        error: (err) => {
          this.cancelError = err?.error?.message || 'Error al cancelar la suscripción.';
          this.cancellingId = null;
        }
      });
  }
}
