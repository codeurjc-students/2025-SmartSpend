import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { TransactionService } from '../../services/transaction/transaction.service';
import { BankAccount } from '../../services/bankAccount/bank-account-service.service';
import { CategoryService } from '../../services/category/category.service';
import { CreateTransactionDto, CreateTransactionWithImageDto } from '../../interfaces/create-transaction.interface';
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

  // Form data - ahora con imagen
  newTransaction: Partial<CreateTransactionWithImageDto> = {
    title: '',
    description: '',
    amount: undefined,
    type: 'EXPENSE',
    recurrence: 'NONE',
    categoryId: undefined,
    date: new Date().toISOString().split('T')[0], // Fecha actual en formato YYYY-MM-DD
    imageFile: undefined // Nuevo campo para la imagen
  };

  // Nuevas propiedades para manejo de imagen
  selectedImageFile: File | null = null;
  imagePreview: string | null = null;
  imageError: string | null = null;

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

  // Nuevo método para manejar selección de imagen
  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    
    if (!file) {
      this.clearImage();
      return;
    }

    // Validaciones de imagen
    this.imageError = null;
    
    // Validar tipo
    if (!file.type.startsWith('image/')) {
      this.imageError = 'Por favor selecciona un archivo de imagen válido';
      this.clearImage();
      return;
    }

    // Validar tamaño (5MB máximo)
    const maxSize = 5 * 1024 * 1024; // 5MB
    if (file.size > maxSize) {
      this.imageError = 'La imagen no puede superar los 5MB';
      this.clearImage();
      return;
    }

    // Guardar archivo y crear preview
    this.selectedImageFile = file;
    this.newTransaction.imageFile = file;
    
    // Crear preview para mostrar al usuario
    const reader = new FileReader();
    reader.onload = (e) => {
      this.imagePreview = e.target?.result as string;
    };
    reader.readAsDataURL(file);
  }

  // Método para limpiar imagen
  clearImage(): void {
    this.selectedImageFile = null;
    this.imagePreview = null;
    this.imageError = null;
    this.newTransaction.imageFile = undefined;
    
    // Limpiar el input file
    const fileInput = document.getElementById('imageInput') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  // Método para abrir el selector de archivo
  openFileSelector(): void {
    const fileInput = document.getElementById('imageInput') as HTMLInputElement;
    if (fileInput) {
      fileInput.click();
    }
  }

  onAmountFocus(): void {
    if (this.newTransaction.amount === 0) {
      this.newTransaction.amount = undefined;
    }
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

    const transactionData: CreateTransactionWithImageDto = {
      title: this.newTransaction.title!,
      description: this.newTransaction.description || '',
      amount: this.newTransaction.amount!,
      type: this.newTransaction.type!,
      recurrence: this.newTransaction.recurrence!,
      categoryId: this.newTransaction.categoryId!,
      date: this.newTransaction.date ?? new Date().toISOString().split('T')[0],
      accountId: this.currentAccount.id,
      imageFile: this.selectedImageFile || undefined
    };

    // Decidir qué método usar según si hay imagen o no
    const serviceCall = this.selectedImageFile 
      ? this.transactionService.createTransactionWithImage(transactionData)
      : this.transactionService.createTransaction(transactionData as CreateTransactionDto);

    serviceCall.subscribe({
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
      date: new Date().toISOString().split('T')[0],
      imageFile: undefined
    };
    this.clearImage();
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