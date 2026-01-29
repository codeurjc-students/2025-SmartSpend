import { Component, OnInit, Input, OnChanges, Output, EventEmitter, SimpleChanges } from '@angular/core'; // ✅ Añadir SimpleChanges para ngOnChanges
import { CommonModule } from '@angular/common';
import { TransactionService } from '../../services/transaction/transaction.service';
import { Transaction } from '../../interfaces/transaction.interface';
import { Router } from '@angular/router';
import { ActiveAccountService } from '../../services/active-account/active-account.service';


@Component({
  selector: 'app-transaction-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './transaction-list.component.html',
  styleUrl: './transaction-list.component.css'
})
export class TransactionListComponent implements OnInit, OnChanges { // ✅ Implementar OnChanges
  @Input() accountId: number | null = null;
  @Input() limit: number = 2; // ✅ Añadir un Input para el límite de transacciones a mostrar
  
  // ✅ Nuevos inputs para modo avanzado
  @Input() advancedMode: boolean = false; // Si está en modo avanzado (recibe transacciones externas)
  @Input() externalTransactions: Transaction[] | null = null; // Transacciones desde el padre
  @Input() externalLoading: boolean = false; // Estado de carga externo
  @Input() showMoreButton: boolean = true; // Mostrar botón "Ver más"

  // ✅ Estas propiedades ahora son internas del componente, no @Input
  transactions: Transaction[] = [];
  isLoading: boolean = false;
  hasMoreTransactions: boolean = false;

  @Output() addTransactionClick = new EventEmitter<void>();
  @Output() viewAllTransactionsClick = new EventEmitter<void>(); 
  @Output() editTransactionClick = new EventEmitter<number>();   
  @Output() deleteTransactionClick = new EventEmitter<number>(); 
  @Output() transactionDeletedSuccess = new EventEmitter<void>();

  constructor(
    private transactionService: TransactionService, 
    private router: Router,
    private activeAccountService: ActiveAccountService
  ) {}

  ngOnInit(): void {
    // Si está en modo avanzado, usar transacciones externas
    if (this.advancedMode) {
      this.handleExternalTransactions();
    } else {
      // Modo normal: suscribirse a cambios en la cuenta activa
      this.activeAccountService.activeAccount$.subscribe(activeAccount => {
        if (activeAccount) {
          this.accountId = activeAccount.id;
          this.loadTransactions();
        } else {
          this.accountId = null;
          this.transactions = [];
          this.hasMoreTransactions = false;
        }
      });
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    // En modo avanzado, reaccionar a cambios en transacciones externas
    if (this.advancedMode && changes['externalTransactions']) {
      this.handleExternalTransactions();
    }
    
    // En modo normal, reaccionar a cambios en el accountId
    if (!this.advancedMode && changes['accountId'] && changes['accountId'].currentValue !== changes['accountId'].previousValue) {
      if (this.accountId) {
        this.loadTransactions();
      } else {
        this.transactions = [];
        this.hasMoreTransactions = false;
      }
    }
  }

  // ✅ Método para cargar las transacciones desde el servicio
  loadTransactions(): void {
    if (!this.accountId) {
      this.transactions = [];
      this.isLoading = false;
      this.hasMoreTransactions = true;
      return;
    }

    this.isLoading = true;
    // Se pide 'limit + 1' para saber si hay más transacciones de las que se mostrarán
    this.transactionService.getRecentTransactionsByAccount(this.accountId, this.limit + 1).subscribe({
      next: (data) => {
        if (data.length > this.limit) {
          this.transactions = data.slice(0, this.limit); // Mostrar solo el límite
          this.hasMoreTransactions = true;
        } else {
          this.transactions = data;
          this.hasMoreTransactions = false;
        }
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading transactions:', err);
        this.transactions = [];
        this.isLoading = false;
        this.hasMoreTransactions = false;
      }
    });
  }

  // ✅ Método para manejar transacciones externas en modo avanzado
  private handleExternalTransactions(): void {
    if (this.externalTransactions) {
      this.transactions = this.externalTransactions;
      this.isLoading = this.externalLoading;
      this.hasMoreTransactions = false; // En modo avanzado, la paginación la maneja el padre
    } else {
      this.transactions = [];
      this.isLoading = this.externalLoading;
      this.hasMoreTransactions = false;
    }
  }

  onAddTransaction(): void { 
    this.addTransactionClick.emit();
  }

  onEditTransaction(transactionId: number): void {
    console.log('Edit transaction clicked:', transactionId);
    this.editTransactionClick.emit(transactionId); // Emitir evento para que el padre lo maneje
  }

  onDeleteTransaction(transactionId: number): void {
    console.log(`TransactionListComponent: Attempting to delete transaction ${transactionId}.`); // LOG
    this.transactionService.deleteTransaction(transactionId).subscribe({
      next: () => {
        console.log(`TransactionListComponent: Transaction ${transactionId} deleted successfully.`); // LOG
        this.loadTransactions(); // Recargar la lista de transacciones del componente
        console.log('TransactionListComponent: Emitting transactionDeletedSuccess event.'); // LOG
        this.transactionDeletedSuccess.emit(); // Emitir el evento para que el padre actualice el saldo
      },
      error: (err) => {
        console.error('TransactionListComponent: Error deleting transaction:', err); // LOG
        // Manejar el error, por ejemplo, mostrar un mensaje al usuario
      }
    });
  }
  onViewAllTransactions(): void {
    console.log('View all transactions clicked');
    this.router.navigate(['/all-transactions']); // Navegar a la nueva página de todas las transacciones
  }

  getTransactionIcon(transaction: Transaction): string {
    return transaction.category?.icon || '💳'; // Usar optional chaining por seguridad
  }

  getAmountClass(transaction: Transaction): string {
    return transaction.type === 'INCOME' ? 'amount-positive' : 'amount-negative';
  }

  getAmountPrefix(transaction: Transaction): string {
    return transaction.type === 'INCOME' ? '+' : '-';
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }

  // ✅ Método para recargar las transacciones
  refreshTransactions(): void {
    this.loadTransactions();
  }

  onViewDetails(transactionId:number): void {
    
    console.log('Navigating to transaction details for ID:', transactionId);
    this.router.navigate(['/transaction', transactionId]);

  }

}