import { CommonModule } from '@angular/common';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { BankAccount, BankAccountServiceService, CreateBankAccount} from '../../services/bankAccount/bank-account-service.service';
// import { TransactionService } from '../../services/transaction/transaction.service'; // Este servicio no se usa directamente aquí, puede eliminarse si no se usa para otras cosas en DashboardComponent
import { TransactionListComponent } from '../transaction-list/transaction-list.component';
import { CreateTransactionModalComponent } from '../create-transaction-modal/create-transaction-modal.component';
import { Transaction } from '../../interfaces/transaction.interface';
import { ActiveAccountService } from '../../services/active-account/active-account.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule,FormsModule, TransactionListComponent, CreateTransactionModalComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  @ViewChild(TransactionListComponent) transactionListComponent!: TransactionListComponent;
  
  accounts: BankAccount[] = [];
  activeAccount: BankAccount | null = null;

  showCreateAccountForm = false;
  newAccountName = '';
  initialBalance = 0; 
  
  isLoading = false;
  isCreatingAccount = false;
  
  errorMessage = '';
  successMessage = '';

  showCreateTransactionModal: boolean = false;


  constructor(
    private bankAccountService: BankAccountServiceService,
    private activeAccountService: ActiveAccountService
    // private transactionService: TransactionService // Se eliminó porque no se usa directamente en este componente.
  ) {}

  ngOnInit(): void {
    this.loadUserAccounts();
    
    // Suscribirse a cambios en la cuenta activa
    this.activeAccountService.activeAccount$.subscribe(account => {
      this.activeAccount = account;
    });
  }

  // ✅ Getter para el template, ahora usa la cuenta activa del servicio
  get currentAccount(): BankAccount | null {
    return this.activeAccount;
  }

  loadUserAccounts(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = ''; 
    
    this.bankAccountService.getBankAccounts().subscribe({
      next: (accounts) => {
        this.accounts = accounts;
        console.log('Cuentas cargadas:', accounts);
        
        // Si no hay cuenta activa, establecer la primera como activa
        const currentActiveAccount = this.activeAccountService.getActiveAccount();
        if (!currentActiveAccount && accounts.length > 0) {
          this.activeAccountService.setActiveAccount(accounts[0]);
        }
        // Si la cuenta activa ya no existe en las cuentas cargadas, establecer la primera
        else if (currentActiveAccount && !accounts.find(acc => acc.id === currentActiveAccount.id) && accounts.length > 0) {
          this.activeAccountService.setActiveAccount(accounts[0]);
        }
        
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error cargando cuentas:', err);
        this.errorMessage = 'Error al cargar las cuentas';
        this.isLoading = false;
      }
    });
  }

  createFirstAccount(): void{
    this.showCreateAccountForm = true; 
    this.newAccountName = ''; 
    this.initialBalance = 0; 
    this.errorMessage = ''; 
    this.successMessage = '';
  }

  closeCreateAccountModal(): void {
    this.showCreateAccountForm = false;
    this.newAccountName = '';
    this.initialBalance = 0;
    this.errorMessage = '';
  }

  createAccount(): void {
    if (!this.newAccountName.trim()) {
      this.errorMessage = 'El nombre de la cuenta es obligatorio.';
      return;
    }
    if (this.initialBalance < 0) {
      this.errorMessage = 'El balance inicial no puede ser negativo.';
      return;
    }

    this.isCreatingAccount = true;
    this.errorMessage = '';
    this.successMessage = '';

    const create: CreateBankAccount = {
      accountName: this.newAccountName,
      initialBalance: this.initialBalance
    };

    this.bankAccountService.createBankAccount(create).subscribe({
      next: (newlyCreatedAccount) => {
        this.successMessage = 'Cuenta creada exitosamente.';
        this.closeCreateAccountModal(); 
        this.isCreatingAccount = false;
        
        // Recargar cuentas y establecer la nueva como activa
        this.loadUserAccounts();
        this.activeAccountService.setActiveAccount(newlyCreatedAccount);
      },
      error: (err) => {
        console.error('Error creando cuenta:', err);
        this.errorMessage = err.error?.message || 'Error al crear la cuenta.'; 
        this.isCreatingAccount = false;
      }
    });
  }
  
  nextAccount(): void {
    if (this.accounts.length > 1) {
      const currentIndex = this.accounts.findIndex(acc => acc.id === this.activeAccount?.id);
      const nextIndex = (currentIndex + 1) % this.accounts.length;
      this.activeAccountService.setActiveAccount(this.accounts[nextIndex]);
    }
  }

  previousAccount(): void {
    if (this.accounts.length > 1) {
      const currentIndex = this.accounts.findIndex(acc => acc.id === this.activeAccount?.id);
      const prevIndex = (currentIndex - 1 + this.accounts.length) % this.accounts.length;
      this.activeAccountService.setActiveAccount(this.accounts[prevIndex]);
    }
  }

  goToAccount(index: number): void {
    if (index >= 0 && index < this.accounts.length) {
      this.activeAccountService.setActiveAccount(this.accounts[index]);
    }
  }

  addTransaction(): void {
    console.log('Añadir transacción a:', this.activeAccount?.accountName);
    this.onAddTransactionFromList(); 
  }

  createNewAccount(): void {
    console.log('Crear nueva cuenta (desde botón)');
    this.createFirstAccount(); 
  }

  // Métodos del modal de transacciones
  onAddTransactionFromList(): void {
    console.log('Evento: Añadir transacción desde la lista');
    this.showCreateTransactionModal = true;
  }

  onCloseCreateTransactionModal(): void {
    this.showCreateTransactionModal = false;
  }

  onTransactionCreated(transaction: Transaction): void { 
    console.log('Transacción creada:', transaction);
    this.successMessage = `Transacción "${transaction.title}" creada exitosamente.`;
    
    if (this.transactionListComponent) {
      this.transactionListComponent.refreshTransactions(); // ✅ El TransactionListComponent recargará sus propias transacciones
    }
    
    this.loadUserAccounts(); // ✅ Recargar las cuentas para actualizar el balance
    
    setTimeout(() => this.successMessage = '', 3000); 
  }

  onTransactionDeletedSuccess(): void {
    console.log('Dashboard: Transacción eliminada exitosamente. Recargando cuentas para actualizar el saldo.');
    this.loadUserAccounts(); 
  }

}