import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';

import { TransactionService } from '../../services/transaction/transaction.service';
import { BankAccount, BankAccountServiceService } from '../../services/bankAccount/bank-account-service.service';
import { CategoryService } from '../../services/category/category.service';
import { CreateTransactionDto, CreateTransactionWithImageDto, DebtDto, TransferDto } from '../../interfaces/create-transaction.interface';
import { Category } from '../../interfaces/category.interface';
import { Transaction } from '../../interfaces/transaction.interface';
import { FormsModule } from '@angular/forms';
import { ActiveAccountService } from '../../services/active-account/active-account.service';

type TransactionFormType = 'EXPENSE' | 'INCOME' | 'TRANSFER';
type TransactionRecurrence = 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY';

@Component({
  selector: 'app-create-transaction-modal',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './create-transaction-modal.component.html',
  styleUrl: './create-transaction-modal.component.css'
})

export class CreateTransactionModalComponent implements OnInit, OnChanges, OnDestroy {
  @Input() isVisible: boolean = false;
  @Input() mode: 'create' | 'edit' = 'create';
  @Input() transactionToEdit?: Transaction;
  @Output() closeModal = new EventEmitter<void>();
  @Output() transactionCreated = new EventEmitter<Transaction>();
  @Output() transactionUpdated = new EventEmitter<Transaction>();

  currentAccount: BankAccount | null = null;
  private readonly formBuilder = inject(FormBuilder);

  readonly transactionForm = this.formBuilder.group({
    title: ['', [Validators.required, Validators.maxLength(30)]],
    description: ['', [Validators.maxLength(100)]],
    amount: [0, [Validators.required, Validators.min(0.01)]],
    type: ['EXPENSE' as TransactionFormType, Validators.required],
    recurrence: ['NONE' as TransactionRecurrence, Validators.required],
    categoryId: [null as string | null],
    date: [this.getTodayDate(), Validators.required],
    originAccountId: [null as number | null],
    destinationAccountId: [null as number | null]
  });

  selectedImageFile: File | null = null;
  imagePreview: string | null = null;
  imageError: string | null = null;

  isSharedMode: boolean = false;
  personalAmount: number | undefined = undefined;
  sharedDebts: DebtDto[] = [];

  availableCategories: Category[] = [];
  availableAccounts: BankAccount[] = [];
  destinationAccounts: BankAccount[] = [];

  isLoading = false;
  isLoadingCategories = false;
  errorMessage: string | null = null;
  private readonly subscriptions = new Subscription();

  constructor(
    private transactionService: TransactionService,
    private categoryService: CategoryService,
    private activeAccountService: ActiveAccountService,
    private bankAccountService: BankAccountServiceService
  ) {}

  ngOnInit(): void {
    this.subscriptions.add(
      this.activeAccountService.activeAccount$.subscribe(account => {
        this.currentAccount = account;
        this.ensureOriginAccountDefault();
        this.updateDestinationAccounts();
      })
    );

    this.subscriptions.add(
      this.transactionForm.controls.type.valueChanges.subscribe(type => {
        this.applyTransactionTypeRules((type ?? 'EXPENSE') as TransactionFormType);
      })
    );

    this.subscriptions.add(
      this.transactionForm.controls.originAccountId.valueChanges.subscribe(() => {
        this.updateDestinationAccounts();
      })
    );

    this.applyTransactionTypeRules(this.selectedType, false);

    if (this.isVisible) {
      this.prepareModalState();
    }
  }

  ngOnChanges(): void {
    if (this.isVisible) {
      this.prepareModalState();
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  get selectedType(): TransactionFormType {
    return (this.transactionForm.controls.type.value as TransactionFormType) ?? 'EXPENSE';
  }

  get isTransferSelected(): boolean {
    return this.selectedType === 'TRANSFER';
  }

  get isExpenseSelected(): boolean {
    return this.selectedType === 'EXPENSE';
  }

  get hasExistingDebts(): boolean {
    return !!(this.transactionToEdit?.debts && this.transactionToEdit.debts.length > 0);
  }

  setTransactionType(type: TransactionFormType): void {
    if (type === 'TRANSFER' && this.mode === 'edit') {
      return;
    }

    this.transactionForm.controls.type.setValue(type);
  }

  private prepareModalState(): void {
    this.initializeForm();
    this.loadAccounts();
  }

  private initializeForm(): void {
    if (this.mode === 'edit' && this.transactionToEdit) {
      this.transactionForm.reset({
        title: this.transactionToEdit.title,
        description: this.transactionToEdit.description || '',
        amount: this.transactionToEdit.amount,
        type: this.transactionToEdit.type,
        recurrence: this.transactionToEdit.recurrence as TransactionRecurrence,
        categoryId: this.transactionToEdit.category.id.toString(),
        date: this.transactionToEdit.date,
        originAccountId: this.transactionToEdit.accountId,
        destinationAccountId: null
      });

      if (this.transactionToEdit.hasImage && this.transactionToEdit.imageBase64) {
        this.imagePreview = `data:${this.transactionToEdit.imageType};base64,${this.transactionToEdit.imageBase64}`;
      } else {
        this.clearImage();
      }
    } else {
      this.resetForm();
    }

    this.ensureOriginAccountDefault();
    this.applyTransactionTypeRules(this.selectedType);
  }

  private getTodayDate(): string {
    return new Date().toISOString().split('T')[0];
  }

  loadAccounts(): void {
    this.bankAccountService.getBankAccounts().subscribe({
      next: (accounts) => {
        this.availableAccounts = accounts;
        this.ensureOriginAccountDefault();
        this.updateDestinationAccounts();
      },
      error: (err) => {
        console.error('Error loading accounts:', err);
        this.availableAccounts = [];
        this.destinationAccounts = [];
      }
    });
  }

  loadCategories(type: TransactionFormType = this.selectedType): void {
    if (type === 'TRANSFER') {
      return;
    }

    this.isLoadingCategories = true;
    this.categoryService.getCategoriesForType(type).subscribe({
      next: (categories) => {
        this.availableCategories = categories;
        this.isLoadingCategories = false;

        const categoryControl = this.transactionForm.controls.categoryId;
        if (categories.length > 0 && !categoryControl.value) {
          categoryControl.setValue(categories[0].id.toString());
        }
      },
      error: (err) => {
        console.error('Error loading categories:', err);
        this.availableCategories = [];
        this.isLoadingCategories = false;
      }
    });
  }

  private applyTransactionTypeRules(type: TransactionFormType, loadCategories: boolean = true): void {
    const categoryControl = this.transactionForm.controls.categoryId;
    const originAccountControl = this.transactionForm.controls.originAccountId;
    const destinationAccountControl = this.transactionForm.controls.destinationAccountId;
    const recurrenceControl = this.transactionForm.controls.recurrence;

    if (type === 'TRANSFER') {
      categoryControl.clearValidators();
      categoryControl.setValue(null, { emitEvent: false });
      originAccountControl.setValidators([Validators.required]);
      destinationAccountControl.setValidators([Validators.required]);
      this.availableCategories = [];
      this.resetSharedMode();
      this.clearImage();
    } else {
      categoryControl.setValidators([Validators.required]);
      originAccountControl.clearValidators();
      destinationAccountControl.clearValidators();

      if (loadCategories) {
        this.loadCategories(type as 'EXPENSE' | 'INCOME');
      }
    }

    if (type !== 'EXPENSE') {
      this.resetSharedMode();
    }

    categoryControl.updateValueAndValidity({ emitEvent: false });
    originAccountControl.updateValueAndValidity({ emitEvent: false });
    destinationAccountControl.updateValueAndValidity({ emitEvent: false });
    recurrenceControl.updateValueAndValidity({ emitEvent: false });

    this.ensureOriginAccountDefault();
    this.updateDestinationAccounts();
  }

  private ensureOriginAccountDefault(): void {
    const originAccountControl = this.transactionForm.controls.originAccountId;

    if (this.mode === 'edit' && this.transactionToEdit) {
      originAccountControl.setValue(this.transactionToEdit.accountId, { emitEvent: false });
      return;
    }

    if (this.currentAccount) {
      originAccountControl.setValue(this.currentAccount.id, { emitEvent: false });
    }
  }

  private updateDestinationAccounts(): void {
    const originAccountId = this.transactionForm.controls.originAccountId.value;
    this.destinationAccounts = this.availableAccounts.filter(account => account.id !== originAccountId);

    const destinationControl = this.transactionForm.controls.destinationAccountId;
    if (!this.destinationAccounts.some(account => account.id === destinationControl.value)) {
      destinationControl.setValue(null, { emitEvent: false });
    }
  }

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
    if (this.transactionForm.controls.amount.value === 0) {
      this.transactionForm.controls.amount.setValue(null);
    }
  }


  onSubmit(): void {
    if (!this.currentAccount && !this.isTransferSelected) {
      this.errorMessage = 'No hay una cuenta seleccionada';
      return;
    }

    if (!this.isFormValid()) {
      this.errorMessage = 'Por favor, completa todos los campos requeridos';
      this.transactionForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;

    if (this.isTransferSelected) {
      if (this.mode === 'edit') {
        this.errorMessage = 'La edición de traspasos no está soportada desde este modal.';
        this.isLoading = false;
        return;
      }

      this.createTransfer();
      return;
    }

    if (this.mode === 'edit' && this.transactionToEdit) {
      this.updateTransaction();
    } else {
      this.createTransaction();
    }
  }

  private updateTransaction(): void {
    const formValue = this.transactionForm.getRawValue();

    const updateData = {
      id: this.transactionToEdit!.id,
      title: formValue.title!.trim(),
      description: formValue.description?.trim() || '',
      amount: Number(formValue.amount),
      type: formValue.type as 'EXPENSE' | 'INCOME',
      recurrence: formValue.recurrence as TransactionRecurrence,
      categoryId: formValue.categoryId!,
      date: formValue.date || this.getTodayDate(),
      accountId: this.currentAccount!.id,
      imageFile: this.selectedImageFile || undefined
    };

    this.transactionService.updateTransaction(updateData).subscribe({
      next: (updatedTransaction) => {
        this.transactionUpdated.emit(updatedTransaction);
        this.close();
      },
      error: (err) => {
        console.error('Error updating transaction:', err);
        this.errorMessage = err.error?.message || 'Error al actualizar la transacción.';
        this.isLoading = false;
      }
    });
  }

  private createTransaction(): void {
    const formValue = this.transactionForm.getRawValue();

    const transactionData: CreateTransactionWithImageDto = {
      title: formValue.title!.trim(),
      description: formValue.description?.trim() || '',
      amount: Number(formValue.amount),
      type: formValue.type as 'EXPENSE' | 'INCOME',
      recurrence: formValue.recurrence as TransactionRecurrence,
      categoryId: formValue.categoryId!,
      date: formValue.date || this.getTodayDate(),
      accountId: this.currentAccount!.id,
      imageFile: this.selectedImageFile || undefined,
      ...(this.isSharedMode && this.sharedDebts.length > 0 && {
        personalAmount: this.personalAmount,
        excludeFromStats: false,
        debts: this.sharedDebts
      })
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

  private createTransfer(): void {
    const formValue = this.transactionForm.getRawValue();
    const transferData: TransferDto = {
      originAccountId: Number(formValue.originAccountId),
      destinationAccountId: Number(formValue.destinationAccountId),
      amount: Number(formValue.amount),
      title: formValue.title!.trim(),
      date: formValue.date || this.getTodayDate(),
      description: formValue.description?.trim() || '',
      recurrence: formValue.recurrence as TransactionRecurrence
    };

    this.transactionService.createTransfer(transferData).subscribe({
      next: (response) => {
        this.transactionCreated.emit(this.normalizeTransferResponse(response, transferData));
        this.close();
      },
      error: (err) => {
        console.error('Error creating transfer:', err);
        this.errorMessage = err.error?.message || 'Error al crear el traspaso. Inténtalo de nuevo.';
        this.isLoading = false;
      }
    });
  }

  private normalizeTransferResponse(response: unknown, transfer: TransferDto): Transaction {
    const candidate = response as Partial<Transaction> | null;

    if (candidate?.title && candidate.category && candidate.accountId !== undefined) {
      return candidate as Transaction;
    }

    return {
      id: 0,
      title: transfer.title,
      description: transfer.description,
      amount: transfer.amount,
      date: transfer.date,
      type: 'EXPENSE',
      recurrence: transfer.recurrence,
      accountId: transfer.originAccountId,
      accountName: this.getAccountNameById(transfer.originAccountId),
      category: {
        id: 0,
        name: 'Traspaso',
        color: '#64748b',
        icon: '⇄',
        type: 'EXPENSE',
        isDefault: true
      },
      hasImage: false
    };
  }

  private getAccountNameById(accountId: number): string {
    return this.availableAccounts.find(account => account.id === accountId)?.accountName || 'Cuenta origen';
  }

  isFormValid(): boolean {
    const formValue = this.transactionForm.getRawValue();
    const hasBaseFields = !!(
      formValue.title &&
      formValue.title.trim() &&
      formValue.amount &&
      Number(formValue.amount) > 0 &&
      formValue.date
    );

    if (!hasBaseFields) {
      return false;
    }

    if (this.isTransferSelected) {
      return !!(
        formValue.originAccountId &&
        formValue.destinationAccountId &&
        formValue.originAccountId !== formValue.destinationAccountId
      );
    }

    const baseValid = !!(
      formValue.type &&
      formValue.recurrence &&
      formValue.categoryId
    );

    if (!baseValid) return false;
    if (this.isSharedMode) return this.isSharedValid();
    return true;
  }

  close(): void {
    this.closeModal.emit();
  }

  resetForm(): void {
    this.transactionForm.reset({
      title: '',
      description: '',
      amount: 0,
      type: 'EXPENSE',
      recurrence: 'NONE',
      categoryId: null,
      date: this.getTodayDate(),
      originAccountId: this.currentAccount?.id ?? null,
      destinationAccountId: null
    });
    this.clearImage();
    this.resetSharedMode();
    this.errorMessage = null;
    this.isLoading = false;
    this.applyTransactionTypeRules(this.selectedType, false);
    this.loadCategories('EXPENSE');
  }

  resetSharedMode(): void {
    this.isSharedMode = false;
    this.personalAmount = undefined;
    this.sharedDebts = [];
  }

  toggleSharedMode(): void {
    this.isSharedMode = !this.isSharedMode;
    if (!this.isSharedMode) {
      this.personalAmount = undefined;
      this.sharedDebts = [];
    }
  }

  addDebt(): void {
    this.sharedDebts.push({ name: '', amount: 0, isPaid: false });
  }

  removeDebt(index: number): void {
    this.sharedDebts.splice(index, 1);
  }

  splitEqually(): void {
    const total = this.transactionForm.controls.amount.value ?? 0;
    const count = this.sharedDebts.length + 1; // +1 por el usuario
    if (count < 2) return;
    const share = Math.round((total / count) * 100) / 100;
    this.personalAmount = share;
    this.sharedDebts = this.sharedDebts.map(d => ({ ...d, amount: share }));
  }

  getDebtsTotal(): number {
    return this.sharedDebts.reduce((acc, d) => acc + (d.amount ?? 0), 0);
  }

  getRemainder(): number {
    const total = this.transactionForm.controls.amount.value ?? 0;
    const personal = this.personalAmount ?? 0;
    return Math.round((total - personal - this.getDebtsTotal()) * 100) / 100;
  }

  isSharedValid(): boolean {
    if (this.sharedDebts.length === 0) return false;
    const allNamed = this.sharedDebts.every(d => d.name.trim().length > 0);
    return allNamed && this.getRemainder() === 0;
  }

// Helper method para obtener la categoría seleccionada
getSelectedCategory(): Category | undefined {
  const categoryId = this.transactionForm.controls.categoryId.value;
  if (categoryId === undefined || categoryId === null || categoryId === '') {
    return undefined;
  }
  const id = typeof categoryId === 'string' ? parseInt(categoryId, 10) : categoryId;
  return this.availableCategories.find(cat => cat.id === id);
}
}
