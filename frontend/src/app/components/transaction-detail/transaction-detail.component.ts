import { Component, OnInit } from '@angular/core';
import { Transaction } from '../../interfaces/transaction.interface';
import { ActivatedRoute, Router } from '@angular/router';
import { TransactionService } from '../../services/transaction/transaction.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CreateTransactionModalComponent } from '../create-transaction-modal/create-transaction-modal.component';

@Component({
  selector: 'app-transaction-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, CreateTransactionModalComponent],
  templateUrl: './transaction-detail.component.html',
  styleUrl: './transaction-detail.component.css'
})
export class TransactionDetailComponent implements OnInit {

  transactionId: number | null = null;
  transaction: Transaction | null = null;
  error: string | null = null;

  // Variables para modales
  showConfirmModal: boolean = false;
  showSuccessModal: boolean = false;
  isDeleting: boolean = false;

  // Variables para modal de edición
  showEditModal: boolean = false;
  updatingDebtId: number | null = null;

  constructor(private route: ActivatedRoute, private transactionService: TransactionService, private router: Router) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      this.transactionId = idParam ? +idParam : null;

      if (this.transactionId !== null) {
        this.transactionService.getTransactionById(this.transactionId).subscribe({
          next: (data: Transaction) => {
            this.transaction = data;
            this.error = null;
          },
          error: (err) => {
            this.error = 'Error fetching transaction details: ' + err.message;
            this.transaction = null;
          }
        });
      } else {
        this.error = 'Invalid transaction ID';
      }

    });
  }

  getRecurrenceText(recurrence: string): string {
    const recurrenceMap: { [key: string]: string } = {
      'NONE': 'Sin recurrencia',
      'DAILY': 'Diaria',
      'WEEKLY': 'Semanal',
      'MONTHLY': 'Mensual',
      'YEARLY': 'Anual'
    };
    return recurrenceMap[recurrence] || recurrence;
  }

  getTransactionTypeText(): string {
    return this.transaction?.type === 'EXPENSE' ? 'Gasto' : 'Ingreso';
  }

  getAccountImpactAmount(): number {
    if (!this.transaction) return 0;
    const effectiveAmount = this.getEffectiveAmount();
    return this.transaction.type === 'EXPENSE' ? -effectiveAmount : effectiveAmount;
  }

  getAfterBalance(): number | null {
    if (this.transaction?.beforeBalance === undefined || this.transaction?.beforeBalance === null) {
      return null;
    }

    return this.transaction.beforeBalance + this.getAccountImpactAmount();
  }

  goBack(): void {
    if (window.history.length > 1) {
      window.history.back();
      return;
    }

    this.router.navigate(['/dashboard']);
  }

  onDeleteTransaction(): void {
    this.showConfirmModal = true;
  }

  confirmDelete(): void {
    if (!this.transactionId) return;

    this.isDeleting = true;
    this.transactionService.deleteTransaction(this.transactionId).subscribe({
      next: () => {
        this.showConfirmModal = false;
        this.isDeleting = false;
        this.showSuccessModal = true;

        // Redirigir al dashboard después de 2 segundos
        setTimeout(() => {
          this.router.navigate(['/dashboard']);
        }, 2000);
      },
      error: (err) => {
        console.error('Error deleting transaction:', err);
        this.isDeleting = false;
        this.showConfirmModal = false;
        this.error = 'Error al eliminar la transacción: ' + err.message;
      }
    });
  }

  cancelDelete(): void {
    this.showConfirmModal = false;
  }

  closeSuccessModal(): void {
    this.showSuccessModal = false;
    this.router.navigate(['/dashboard']);
  }

  // Métodos para modal de edición
  onEditTransaction(): void {
    this.showEditModal = true;
  }

  onCloseEditModal(): void {
    this.showEditModal = false;
  }

  onTransactionUpdated(updatedTransaction: Transaction): void {
    this.transaction = updatedTransaction; // Actualizar los datos locales
    this.showEditModal = false;
    console.log('Transacción actualizada:', updatedTransaction);
  }

  hasSharedDebts(): boolean {
    return !!(this.transaction?.debts && this.transaction.debts.length > 0);
  }

  getPaidDebtsCount(): number {
    if (!this.transaction?.debts) return 0;
    return this.transaction.debts.filter(d => d.isPaid).length;
  }

  getPendingDebtsCount(): number {
    if (!this.transaction?.debts) return 0;
    return this.transaction.debts.filter(d => !d.isPaid).length;
  }

  getEffectiveAmount(): number {
    if (!this.transaction) return 0;
    return this.transaction.effectiveAmount ?? this.transaction.amount;
  }

  getSharedAmount(): number {
    if (!this.transaction) return 0;
    return Math.max(0, this.transaction.amount - this.getEffectiveAmount());
  }

  getPaidDebtsAmount(): number {
    if (!this.transaction?.debts) return 0;
    return this.transaction.debts
      .filter(d => d.isPaid)
      .reduce((sum, d) => sum + d.amount, 0);
  }

  getPendingDebtsAmount(): number {
    if (!this.transaction?.debts) return 0;
    return this.transaction.debts
      .filter(d => !d.isPaid)
      .reduce((sum, d) => sum + d.amount, 0);
  }

  getDebtsProgress(): number {
    const total = this.transaction?.debts?.length ?? 0;
    if (total === 0) return 0;
    return Math.round((this.getPaidDebtsCount() / total) * 100);
  }

  onDebtPaidChange(debtId: number, checked: boolean): void {
    if (!checked || !this.transactionId) return;

    this.updatingDebtId = debtId;
    this.transactionService.markDebtAsPaid(this.transactionId, debtId).subscribe({
      next: (updatedTransaction) => {
        this.transaction = updatedTransaction;
        this.error = null;
        this.updatingDebtId = null;
      },
      error: (err) => {
        if (this.transaction?.debts) {
          const debt = this.transaction.debts.find(d => d.id === debtId);
          if (debt) debt.isPaid = false;
        }
        this.error = 'Error al marcar deuda como pagada: ' + (err.error?.message || err.message);
        this.updatingDebtId = null;
      }
    });
  }


}
