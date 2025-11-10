import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { BankAccount, BankAccountServiceService, CreateBankAccount} from '../../services/bankAccount/bank-account-service.service';
import { TransactionService } from '../../services/transaction/transaction.service'; // Mantener si se usa para otras operaciones de transacciones

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule,FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  // Propiedades básicas
  accounts: BankAccount[] = [];
  currentAccountIndex = 0;
  
  // Para crear nueva cuenta (modal)
  showCreateAccountForm = false;
  newAccountName = '';
  initialBalance = 0; 
  
  // Estados de carga
  isLoading = false;
  isCreatingAccount = false;
  
  // Mensajes
  errorMessage = '';
  successMessage = '';

  constructor(
    private bankAccountService: BankAccountServiceService,
    private transactionService: TransactionService // Mantener si se usa para otras operaciones de transacciones
  ) {}

  ngOnInit(): void {
    this.loadUserAccounts();
  }

  // ✅ Getter para obtener la cuenta actualmente seleccionada
  get currentAccount(): BankAccount | null {
    return this.accounts.length > 0 ? this.accounts[this.currentAccountIndex] : null;
  }

  // Método para cargar cuentas
  loadUserAccounts(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = ''; // Limpiar mensajes al recargar
    
    this.bankAccountService.getBankAccounts().subscribe({
      next: (accounts) => {
        this.accounts = accounts;
        console.log('Cuentas cargadas:', accounts);
        
        if (this.accounts.length > 0) {
          if (this.currentAccountIndex >= this.accounts.length) {
            this.currentAccountIndex = 0;
          }
        } else {
          this.currentAccountIndex = 0; // Resetear si no hay cuentas
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

  // ✅ Método para cerrar el modal
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
        this.closeCreateAccountModal(); // ✅ Cerrar modal solo al éxito
        this.isCreatingAccount = false;
        
        this.bankAccountService.getBankAccounts().subscribe({
          next: (accounts) => {
            this.accounts = accounts;
            const newAccountIndex = accounts.findIndex(acc => acc.id === newlyCreatedAccount.id);
            if (newAccountIndex !== -1) {
              this.currentAccountIndex = newAccountIndex;
            } else if (accounts.length > 0) {
              this.currentAccountIndex = 0; // Fallback a la primera cuenta
            } else {
              this.currentAccountIndex = 0; // No hay cuentas
            }
            this.isLoading = false; // Asegurar que el estado de carga se desactive
          },
          error: (err) => {
            console.error('Error recargando cuentas después de crear:', err);
            this.errorMessage = 'Error al recargar las cuentas.';
            this.isLoading = false;
          }
        });
      },
      error: (err) => {
        console.error('Error creando cuenta:', err);
        this.errorMessage = err.error?.message || 'Error al crear la cuenta.'; // ✅ Mejor manejo de errores
        this.isCreatingAccount = false;
      }
    });
  }
  
  // ✅ Métodos de navegación
  nextAccount(): void {
    if (this.accounts.length > 1) {
      this.currentAccountIndex = (this.currentAccountIndex + 1) % this.accounts.length;
    }
  }

  previousAccount(): void {
    if (this.accounts.length > 1) {
      this.currentAccountIndex = (this.currentAccountIndex - 1 + this.accounts.length) % this.accounts.length;
    }
  }

  goToAccount(index: number): void {
    if (index >= 0 && index < this.accounts.length) {
      this.currentAccountIndex = index;
    }
  }

  // ✅ Métodos para los botones de acción (añadidos/restaurados)
  addTransaction(): void {
    console.log('Añadir transacción a:', this.currentAccount?.accountName);
    // Aquí iría la lógica para abrir un modal de añadir transacción
  }



  createNewAccount(): void {
    console.log('Crear nueva cuenta (desde botón)');
    this.createFirstAccount(); // Reutilizar el método para mostrar el modal
  }

}