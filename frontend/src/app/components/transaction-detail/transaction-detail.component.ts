import { Component, OnInit } from '@angular/core';
import { Transaction } from '../../interfaces/transaction.interface';
import { ActivatedRoute, Router } from '@angular/router';
import { TransactionService } from '../../services/transaction/transaction.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-transaction-detail',
  standalone: true,
  imports: [CommonModule], 
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

  
}
