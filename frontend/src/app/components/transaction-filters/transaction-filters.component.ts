import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';

import { FormsModule } from '@angular/forms';
import { TransactionFilters } from '../../interfaces/pagination.interface';
import { CategoryService } from '../../services/category/category.service';
import { Category } from '../../interfaces/category.interface';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-transaction-filters',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './transaction-filters.component.html',
  styleUrl: './transaction-filters.component.css'
})
export class TransactionFiltersComponent implements OnInit, OnDestroy {
  
  private destroy$ = new Subject<void>();
  
  // Propiedades para categorías
  categories: Category[] = [];
  loadingCategories = false;
  
  // Inputs para configurar el comportamiento del componente
  @Input() title: string = 'Filtros de Búsqueda';
  @Input() showTitle: boolean = true;
  @Input() showClearButton: boolean = true;
  @Input() initialFilters: TransactionFilters | null = null;
  @Input() debounceTime: number = 300; // ms para debounce de búsqueda
  
  // Configuración de campos visibles
  @Input() showSearch: boolean = true;
  @Input() showType: boolean = true;
  @Input() showDateRange: boolean = true;
  @Input() showAmountRange: boolean = true;
  @Input() showCategory: boolean = true;
  @Input() showApplyButton: boolean = true;
  
  // Labels personalizables
  @Input() searchLabel: string = '🔎 Buscar';
  @Input() typeLabel: string = '💰 Tipo';
  @Input() dateFromLabel: string = '📅 Desde';
  @Input() dateToLabel: string = '📅 Hasta';
  @Input() minAmountLabel: string = '💵 Mínimo €';
  @Input() maxAmountLabel: string = '💵 Máximo €';
  @Input() categoryLabel: string = '🏷️ Categoría';
  
  // Outputs para comunicar cambios al componente padre
  @Output() filtersChange = new EventEmitter<TransactionFilters>();
  @Output() searchChange = new EventEmitter<string>();
  @Output() clearFilters = new EventEmitter<void>();
  @Output() applyFilters = new EventEmitter<TransactionFilters>();
  
  // Estado interno de los filtros
  filters: TransactionFilters = {
    search: '',
    dateFrom: '',
    dateTo: '',
    minAmount: undefined,
    maxAmount: undefined,
    type: null,
    categoryId: ''
  };

  constructor(private categoryService: CategoryService) {}

  ngOnInit() {
    // Aplicar filtros iniciales si se proporcionan
    if (this.initialFilters) {
      this.filters = { ...this.initialFilters };
    }
  }

  // Método cuando cambia la búsqueda (con debounce manejado por el padre)
  onSearchChange() {
    this.searchChange.emit(this.filters.search);
    this.emitFiltersChange();
  }

  // Método cuando cambian otros filtros
  onFilterChange() {
    this.emitFiltersChange();
  }

  // Método cuando cambia el tipo de transacción (cargar categorías)
  onTypeChange() {
    // Limpiar categoría seleccionada cuando cambia el tipo
    this.filters.categoryId = '';
    this.loadCategories();
    this.emitFiltersChange();
  }

  // Método para cargar categorías según el tipo seleccionado
  private loadCategories() {
    if (!this.filters.type) {
      this.categories = [];
      return;
    }

    this.loadingCategories = true;
    this.categoryService.getCategoriesForType(this.filters.type as 'INCOME' | 'EXPENSE')
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (categories) => {
          this.categories = categories;
          this.loadingCategories = false;
        },
        error: (error) => {
          console.error('Error loading categories:', error);
          this.categories = [];
          this.loadingCategories = false;
        }
      });
  }

  // Método para limpiar todos los filtros
  onClearFilters() {
    this.filters = {
      search: '',
      dateFrom: '',
      dateTo: '',
      minAmount: undefined,
      maxAmount: undefined,
      type: null,
      categoryId: ''
    };
    this.clearFilters.emit();
    this.emitFiltersChange();
  }

  // Método para aplicar filtros explícitamente
  onApplyFilters() {
    this.applyFilters.emit({ ...this.filters });
  }

  // Método privado para emitir cambios
  private emitFiltersChange() {
    this.filtersChange.emit({ ...this.filters });
  }

  // Método para actualizar filtros desde el exterior
  updateFilters(newFilters: TransactionFilters) {
    this.filters = { ...newFilters };
    // Cargar categorías si hay un tipo seleccionado
    if (this.filters.type) {
      this.loadCategories();
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}