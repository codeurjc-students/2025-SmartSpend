import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { debounceTime, takeUntil } from 'rxjs/operators';
import { TransactionService } from '../../services/transaction/transaction.service';
import { PaginatedResponse, TransactionFilters } from '../../interfaces/pagination.interface';
import { ActiveAccountService } from '../../services/active-account/active-account.service';
import { Transaction } from '../../interfaces/transaction.interface';
import { TransactionListComponent } from '../transaction-list/transaction-list.component';
import { TransactionFiltersComponent } from '../transaction-filters/transaction-filters.component';

@Component({
  selector: 'app-all-transactions',
  standalone: true,
  imports: [CommonModule, FormsModule, TransactionListComponent, TransactionFiltersComponent],
  templateUrl: './all-transactions.component.html',
  styleUrl: './all-transactions.component.css'
})
export class AllTransactionsComponent implements OnInit, OnDestroy {
  
  // Exponer Math para el template
  Math = Math;
  
  transactions: Transaction[] = [];
  currentPage = 0;
  pageSize = 10;
  isLoading = false;
  hasMorePages = true;
  totalElements = 0;
  
  // Subject para el debounce de búsqueda
  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();
  
  // Filtros
  filters: TransactionFilters = {
    type: null,
    search: '',
    dateFrom: '',
    dateTo: '',
    minAmount: undefined,
    maxAmount: undefined,
    categoryId: ''
  };

  constructor(
    private transactionService: TransactionService,
    private activeAccountService: ActiveAccountService,
    private router: Router
  ) {}

  ngOnInit() {
    // Configurar debounce para búsqueda de texto
    this.searchSubject
      .pipe(
        debounceTime(500), // Esperar 500ms de pausa
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.applyFilters();
      });

    this.loadTransactions();
  }

  loadTransactions(reset = false) {
    if (reset) {
      this.transactions = [];
      this.currentPage = 0;
      this.hasMorePages = true;
    }

    if (!this.hasMorePages && !reset) return;

    const accountId = this.activeAccountService.getActiveAccountValue()?.id;
    if (!accountId) {
      console.warn('No active account found');
      return;
    }

    this.isLoading = true;
    
    // Llamada al backend con filtros y paginación
    this.transactionService.getTransactionsPaginated(
      accountId,
      this.currentPage,
      this.pageSize,
      this.filters
    ).subscribe({
      next: (response: PaginatedResponse<Transaction>) => {
        if (this.currentPage === 0) {
          // Nueva búsqueda o filtros aplicados
          this.transactions = response.content;
        } else {
          // "Mostrar más" - agregar a la lista existente
          this.transactions.push(...response.content);
        }
        
        this.hasMorePages = !response.last;
        this.totalElements = response.totalElements;
        this.currentPage++;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading transactions:', err);
        this.isLoading = false;
      }
    });
  }

  loadMore() {
    this.loadTransactions();
  }

  applyFilters() {
    // Resetear paginación y cargar con nuevos filtros
    this.loadTransactions(true);
  }

  clearFilters() {
    this.filters = {
      type: null,
      search: '',
      dateFrom: '',
      dateTo: '',
      minAmount: undefined,
      maxAmount: undefined,
      categoryId: ''
    };
    this.loadTransactions(true);
  }

  // Método para manejar cambios en los filtros desde el componente hijo
  onFiltersChange(newFilters: TransactionFilters) {
    this.filters = { ...newFilters };
    this.currentPage = 0; // Reset a primera página
    this.loadTransactions(true);
  }

  // Método para manejar cambios en búsqueda con debounce
  onSearchChange(searchTerm: string) {
    this.searchSubject.next(searchTerm);
  }

  // Método para limpiar filtros
  onClearFilters() {
    this.filters = {
      type: null,
      search: '',
      dateFrom: '',
      dateTo: '',
      minAmount: undefined,
      maxAmount: undefined,
      categoryId: ''
    };
    this.currentPage = 0;
    this.loadTransactions(true);
  }

  // Método para aplicar filtros
  onApplyFilters(filters: TransactionFilters) {
    this.filters = { ...filters };
    this.currentPage = 0;
    this.loadTransactions(true);
  }

  onViewDetails(transactionId: number) {
    this.router.navigate(['/transaction', transactionId]);
  }

  viewTransactionDetails(transactionId: number) {
    this.router.navigate(['/transaction', transactionId]);
  }

  onEditTransaction(transactionId: number) {
    // Implementar edición
    console.log('Edit transaction:', transactionId);
  }

  onDeleteTransaction(transactionId: number) {
    if (confirm('¿Estás seguro de que quieres eliminar esta transacción?')) {
      this.transactionService.deleteTransaction(transactionId).subscribe({
        next: () => {
          this.transactions = this.transactions.filter(t => t.id !== transactionId);
          this.totalElements--;
        },
        error: (err) => {
          console.error('Error deleting transaction:', err);
        }
      });
    }
  }

  goBack() {
    this.router.navigate(['/dashboard']);
  }

  previousPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadTransactions(false);
    }
  }

  nextPage() {
    if ((this.currentPage + 1) * this.pageSize < this.totalElements) {
      this.currentPage++;
      this.loadTransactions(false);
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}