import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BankAccountServiceService } from '../../services/bankAccount/bank-account-service.service';

@Component({
  selector: 'app-account-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './account-form.component.html',
  styleUrl: './account-form.component.css'
})
export class AccountFormComponent {
  
  @Input() showCreateAccountForm = false;
  @Output() accountCreated = new EventEmitter<void>();
  @Output() modalClosed = new EventEmitter<void>();

  newAccountName = '';
  initialBalance = 0;
  isCreatingAccount = false;
  errorMessage = '';

  constructor(private bankAccountService: BankAccountServiceService) {}

  closeCreateAccountModal(): void {
    this.showCreateAccountForm = false;
    this.resetForm();
    this.modalClosed.emit();
  }

  createAccount(): void {
    if (!this.newAccountName.trim()) {
      this.errorMessage = 'El nombre de la cuenta es requerido';
      return;
    }

    this.isCreatingAccount = true;
    this.errorMessage = '';

    const accountData = {
      accountName: this.newAccountName.trim(),
      initialBalance: this.initialBalance
    };

    this.bankAccountService.createBankAccount(accountData).subscribe({
      next: () => {
        this.isCreatingAccount = false;
        this.accountCreated.emit();
        this.closeCreateAccountModal();
      },
      error: (error) => {
        console.error('Error al crear cuenta:', error);
        this.errorMessage = 'Error al crear la cuenta. Inténtalo de nuevo.';
        this.isCreatingAccount = false;
      }
    });
  }

  private resetForm(): void {
    this.newAccountName = '';
    this.initialBalance = 0;
    this.errorMessage = '';
    this.isCreatingAccount = false;
  }
}
