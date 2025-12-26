import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { TransactionService } from '../../services/transaction/transaction.service';
import { BankAccount } from '../../services/bankAccount/bank-account-service.service';
import { CategoryService } from '../../services/category/category.service';
import { CreateTransactionDto } from '../../interfaces/create-transaction.interface';
import { Category } from '../../interfaces/category.interface';
import { Transaction } from '../../interfaces/transaction.interface';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActiveAccountService } from '../../services/active-account/active-account.service';

@Component({
  selector: 'app-create-transaction-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create-transaction-modal.component.html',
  styleUrl: './create-transaction-modal.component.css'
})

export class CreateTransactionModalComponent implements OnInit {
  @Input() isVisible: boolean = false;
  @Output() closeModal = new EventEmitter<void>();
  @Output() transactionCreated = new EventEmitter<Transaction>();

  // La cuenta activa se obtiene del servicio en lugar de ser un Input
  currentAccount: BankAccount | null = null;

  // Form data
  newTransaction: Partial<CreateTransactionDto> = {
    title: '',
    description: '',
    amount: 0,
    type: 'EXPENSE',
    recurrence: 'NONE',
    categoryId: undefined,
    date: new Date().toISOString().split('T')[0] // Fecha actual en formato YYYY-MM-DD
  };

  // Categories for current transaction type
  availableCategories: Category[] = [];
  
  // Loading states
  isLoading = false;
  isLoadingCategories = false;
  errorMessage: string | null = null;

  constructor(
    private transactionService: TransactionService,
    private categoryService: CategoryService,
    private activeAccountService: ActiveAccountService
  ) {}

  ngOnInit(): void {    // Suscribirse a cambios en la cuenta activa
    this.activeAccountService.activeAccount$.subscribe(account => {
      this.currentAccount = account;
    });
        if (this.isVisible) {
      this.loadCategories();
    }
  }

  ngOnChanges(): void {
    if (this.isVisible) {
      this.resetForm();
      this.loadCategories();
    }
  }

  loadCategories(): void {
    if (!this.newTransaction.type) return;
    
    this.isLoadingCategories = true;
    this.categoryService.getCategoriesForType(this.newTransaction.type).subscribe({
      next: (categories) => {
        this.availableCategories = categories;
        this.isLoadingCategories = false;
        // Auto-seleccionar la primera categoría si no hay una seleccionada
        if (categories.length > 0 && !this.newTransaction.categoryId) {
          this.newTransaction.categoryId = categories[0].id.toString();
        }
      },
      error: (err) => {
        console.error('Error loading categories:', err);
        this.availableCategories = [];
        this.isLoadingCategories = false;
      }
    });
  }

  onTransactionTypeChange(): void {
    this.newTransaction.categoryId = undefined; // Reset category selection
    this.loadCategories();
  }

  onSubmit(): void {
    if (!this.currentAccount) {
      this.errorMessage = 'No hay una cuenta seleccionada';
      return;
    }

    if (!this.isFormValid()) {
      this.errorMessage = 'Por favor, completa todos los campos requeridos';
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;

    const transactionDto: CreateTransactionDto = {
      title: this.newTransaction.title!,
      description: this.newTransaction.description || undefined,
      amount: this.newTransaction.amount!,
      date: this.newTransaction.date ?? new Date().toISOString().split('T')[0],
      type: this.newTransaction.type!,
      recurrence: this.newTransaction.recurrence!,
      accountId: this.currentAccount.id,
      categoryId: this.newTransaction.categoryId!
    };

    this.transactionService.createTransaction(transactionDto).subscribe({
      next: (transaction) => {
        this.transactionCreated.emit(transaction);
        this.close();
      },
      error: (err) => {
        console.error('Error creating transaction:', err);
        this.errorMessage = err.error?.message || 'Error al crear la transacción. Inténtalo de nuevo.';
        this.isLoading = false;
      }
    });
  }

  isFormValid(): boolean {
    return !!(
      this.newTransaction.title &&
      this.newTransaction.title.trim() &&
      this.newTransaction.amount &&
      this.newTransaction.amount > 0 &&
      this.newTransaction.type &&
      this.newTransaction.recurrence &&
      this.newTransaction.categoryId
    );
  }

  close(): void {
    this.closeModal.emit();
  }

  resetForm(): void {
    this.newTransaction = {
      title: '',
      description: '',
      amount: 0,
      type: 'EXPENSE',
      recurrence: 'NONE',
      categoryId: undefined,
      date: new Date().toISOString().split('T')[0]
    };
    this.errorMessage = null;
    this.isLoading = false;
  }

// Helper method para obtener la categoría seleccionada
getSelectedCategory(): Category | undefined {
  const categoryId = this.newTransaction.categoryId;
  if (categoryId === undefined || categoryId === null || categoryId === '') {
    return undefined;
  }
  const id = typeof categoryId === 'string' ? parseInt(categoryId, 10) : categoryId;
  return this.availableCategories.find(cat => cat.id === id);
}
}