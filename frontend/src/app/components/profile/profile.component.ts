import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BankAccount, BankAccountServiceService } from '../../services/bankAccount/bank-account-service.service';
import { AuthService } from '../../services/auth/auth.service';
import { jwtDecode } from 'jwt-decode';
import { Router } from '@angular/router';
import { AccountFormComponent } from '../account-form/account-form.component';
import { ActiveAccountService } from '../../services/active-account/active-account.service';

interface UserInfo {
  sub?: string; // email del usuario
  exp?: number;
  iat?: number;
  [key: string]: any; // Para otros campos que puedan existir
}

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, AccountFormComponent],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  
  userInfo: UserInfo | null = null;
  bankAccounts: BankAccount[] = [];
  defaultAccount: BankAccount | null = null;
  memberSince: string = '';
  isLoading = true;
  errorMessage = '';
  showCreateAccountForm = false;

  constructor(
    private authService: AuthService,
    private bankAccountService: BankAccountServiceService,
    private router: Router,
    private activeAccountService: ActiveAccountService
  ) {}

  ngOnInit(): void {
    this.loadUserProfile();
    this.loadBankAccounts();
  }

  private loadUserProfile(): void {
    const token = this.authService.getToken();
    if (token) {
      try {
        this.userInfo = jwtDecode<UserInfo>(token);
        if (this.userInfo.iat) {
          this.memberSince = this.formatMemberSince(this.userInfo.iat);
        }
      } catch (error) {
        console.error('Error al decodificar el token:', error);
        this.errorMessage = 'Error al cargar la información del usuario';
      }
    }
  }

  private loadBankAccounts(): void {
    this.bankAccountService.getBankAccounts().subscribe({
      next: (accounts) => {
        this.bankAccounts = accounts;
        
        // Obtener cuenta activa guardada o establecer la primera como activa
        const savedActiveAccountId = this.activeAccountService.getSavedActiveAccountId();
        let accountToSet: BankAccount | null = null;
        
        if (savedActiveAccountId) {
          accountToSet = accounts.find(acc => acc.id === savedActiveAccountId) || null;
        }
        
        if (!accountToSet && accounts.length > 0) {
          accountToSet = accounts[0];
        }
        
        this.defaultAccount = accountToSet;
        this.activeAccountService.setActiveAccount(accountToSet);
        
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error al cargar cuentas bancarias:', error);
        this.errorMessage = 'Error al cargar las cuentas bancarias';
        this.isLoading = false;
      }
    });
  }

  private formatMemberSince(timestamp: number): string {
    const date = new Date(timestamp * 1000);
    return date.toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  getTotalBalance(): number {
    return this.bankAccounts.reduce((total, account) => total + account.currentBalance, 0);
  }

  setAsDefaultAccount(account: BankAccount): void {
    this.defaultAccount = account;
    this.activeAccountService.setActiveAccount(account);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  editProfile(): void {
    // Implementar la funcionalidad de editar perfil
    console.log('Editar perfil');
  }

  addBankAccount(): void {
    this.showCreateAccountForm = true;
  }

  onAccountCreated(): void {
    this.loadBankAccounts(); // Recargar las cuentas después de crear una nueva
    this.showCreateAccountForm = false;
  }

  onModalClosed(): void {
    this.showCreateAccountForm = false;
  }

}
