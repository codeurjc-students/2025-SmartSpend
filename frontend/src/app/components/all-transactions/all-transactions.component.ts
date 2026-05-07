import { Component, OnInit, OnDestroy } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
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
  imports: [FormsModule, TransactionListComponent, TransactionFiltersComponent],
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
  totalElements = 0;
  totalPages = 0;
  filtersExpanded = false;

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
    categoryId: '',
    isPending: false
  };

  constructor(
    private transactionService: TransactionService,
    private activeAccountService: ActiveAccountService,
    private route: ActivatedRoute,
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

    this.route.queryParamMap
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        const filter = params.get('filter');
        const shouldApplyPending = filter === 'pending-debts';

        this.filters = {
          ...this.filters,
          isPending: shouldApplyPending
        };

        this.filtersExpanded = shouldApplyPending;

        this.loadTransactions(0);
      });
  }

  loadTransactions(page: number = this.currentPage) {
    const accountId = this.activeAccountService.getActiveAccountValue()?.id;
    if (!accountId) {
      console.warn('No active account found');
      return;
    }

    this.isLoading = true;

    // Llamada al backend con filtros y paginación
    this.transactionService.getTransactionsPaginated(
      accountId,
      page,
      this.pageSize,
      this.filters
    ).subscribe({
      next: (response: PaginatedResponse<Transaction>) => {
        this.transactions = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.currentPage = response.number;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading transactions:', err);
        this.isLoading = false;
      }
    });
  }

  loadMore() {
    this.nextPage();
  }

  applyFilters() {
    this.loadTransactions(0);
  }

  clearFilters() {
    this.filters = {
      type: null,
      search: '',
      dateFrom: '',
      dateTo: '',
      minAmount: undefined,
      maxAmount: undefined,
      categoryId: '',
      isPending: false
    };
    this.loadTransactions(0);
  }

  // Método para manejar cambios en los filtros desde el componente hijo
  onFiltersChange(newFilters: TransactionFilters) {
    this.filters = { ...newFilters };
    this.loadTransactions(0);
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
      categoryId: '',
      isPending: false
    };
    this.loadTransactions(0);
  }

  // Método para aplicar filtros
  onApplyFilters(filters: TransactionFilters) {
    this.filters = { ...filters };
    this.loadTransactions(0);
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
      this.loadTransactions(this.currentPage - 1);
    }
  }

  nextPage() {
    if (this.currentPage + 1 < this.totalPages) {
      this.loadTransactions(this.currentPage + 1);
    }
  }

  toggleFiltersPanel() {
    this.filtersExpanded = !this.filtersExpanded;
  }

  hasActiveFilters(): boolean {
    return !!(
      this.filters.search ||
      this.filters.type ||
      this.filters.dateFrom ||
      this.filters.dateTo ||
      this.filters.minAmount !== undefined ||
      this.filters.maxAmount !== undefined ||
      this.filters.categoryId ||
      this.filters.isPending
    );
  }

  getCurrentRangeStart(): number {
    if (this.totalElements === 0) return 0;
    return this.currentPage * this.pageSize + 1;
  }

  getCurrentRangeEnd(): number {
    return Math.min((this.currentPage + 1) * this.pageSize, this.totalElements);
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
