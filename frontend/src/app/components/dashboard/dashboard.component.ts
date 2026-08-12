import { CommonModule } from '@angular/common';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BaseChartDirective } from 'ng2-charts';
import { Chart, ChartConfiguration, registerables } from 'chart.js';

import { BankAccount, BankAccountServiceService, CreateBankAccount} from '../../services/bankAccount/bank-account-service.service';
// import { TransactionService } from '../../services/transaction/transaction.service'; // Este servicio no se usa directamente aquí, puede eliminarse si no se usa para otras cosas en DashboardComponent
import { TransactionListComponent } from '../transaction-list/transaction-list.component';
import { CreateTransactionModalComponent } from '../create-transaction-modal/create-transaction-modal.component';
import { OnboardingComponent } from '../onboarding/onboarding.component';
import { Transaction } from '../../interfaces/transaction.interface';
import { PendingDebtSummary } from '../../interfaces/pending-debt-summary.interface';
import { ActiveAccountService } from '../../services/active-account/active-account.service';
import { AnalysisService } from '../../services/analysis.service';
import { ChartsService } from '../../services/charts.service';
import { TransactionService } from '../../services/transaction/transaction.service';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    TransactionListComponent,
    CreateTransactionModalComponent,
    BaseChartDirective,
    OnboardingComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  @ViewChild(TransactionListComponent) transactionListComponent!: TransactionListComponent;

  accounts: BankAccount[] = [];
  activeAccount: BankAccount | null = null;

  showCreateAccountForm = false;
  newAccountName = '';
  initialBalance: number | undefined = undefined;

  isLoading = false;
  isCreatingAccount = false;

  errorMessage = '';
  successMessage = '';

  showCreateTransactionModal: boolean = false;
  showOnboarding = false;
  pendingDebtsSummary: PendingDebtSummary[] = [];
  isLoadingPendingDebts = false;
  fixedExpenses: Transaction[] = [];
  isLoadingFixedExpenses = false;

  monthlyBalanceChartData: ChartConfiguration<'bar'>['data'] | null = null;
  isLoadingMonthlyBalance = false;

  monthlyBalanceChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false
      },
      tooltip: {
        callbacks: {
          label: (context) => `€${context.formattedValue}`
        }
      }
    },
    scales: {
      x: {
        ticks: {
          color: '#cbd5e1'
        },
        grid: {
          display: false
        }
      },
      y: {
        beginAtZero: true,
        ticks: {
          color: '#94a3b8',
          callback: (value: string | number) => `€${value}`
        },
        grid: {
          color: 'rgba(148, 163, 184, 0.15)'
        }
      }
    }
  };


  constructor(
    private bankAccountService: BankAccountServiceService,
    private activeAccountService: ActiveAccountService,
    private analysisService: AnalysisService,
    private chartsService: ChartsService,
    private transactionService: TransactionService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.loadUserAccounts();
    this.loadPendingDebtsSummary();
    if (!OnboardingComponent.isCompleted()) {
      this.showOnboarding = true;
    }
    this.route.queryParams.subscribe(params => {
      if (params['tutorial'] === 'true') {
        this.showOnboarding = true;
        this.router.navigate([], { queryParams: {}, replaceUrl: true });
      }
    });

    // Suscribirse a cambios en la cuenta activa
    this.activeAccountService.activeAccount$.subscribe(account => {
      this.activeAccount = account;

      if (account?.id) {
        this.loadFixedExpenses(account.id);
        this.loadMonthlyBalance(account.id);
      } else {
        this.fixedExpenses = [];
        this.monthlyBalanceChartData = null;
      }
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

        // Obtener la cuenta activa actual para actualizar su balance
        const currentActiveAccount = this.activeAccountService.getActiveAccount();

        if (currentActiveAccount) {
          // Buscar la cuenta activa en las cuentas cargadas para obtener el balance actualizado
          const updatedActiveAccount = accounts.find(acc => acc.id === currentActiveAccount.id);
          if (updatedActiveAccount) {
            // Actualizar la cuenta activa con el balance actualizado
            this.activeAccountService.setActiveAccount(updatedActiveAccount);
          } else if (accounts.length > 0) {
            // Si la cuenta activa ya no existe, establecer la primera
            this.activeAccountService.setActiveAccount(accounts[0]);
          }
        } else if (accounts.length > 0) {
          // Si no hay cuenta activa, establecer la primera
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
    this.initialBalance = undefined;
    this.errorMessage = '';
    this.successMessage = '';
  }

  onInitialBalanceFocus(): void {
    if (this.initialBalance === 0) {
      this.initialBalance = undefined;
    }
  }

  closeCreateAccountModal(): void {
    this.showCreateAccountForm = false;
    this.newAccountName = '';
    this.initialBalance = undefined;
    this.errorMessage = '';
  }

  createAccount(): void {
    if (!this.newAccountName.trim()) {
      this.errorMessage = 'El nombre de la cuenta es obligatorio.';
      return;
    }
    if (this.initialBalance === undefined || Number.isNaN(this.initialBalance)) {
      this.errorMessage = 'El saldo inicial es obligatorio.';
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

    if (this.activeAccount?.id) {
      this.loadFixedExpenses(this.activeAccount.id);
      this.loadMonthlyBalance(this.activeAccount.id);
    }

    this.loadPendingDebtsSummary();

    setTimeout(() => this.successMessage = '', 3000);
  }

  onTransactionDeletedSuccess(): void {
    console.log('Dashboard: Transacción eliminada exitosamente. Recargando cuentas para actualizar el saldo.');
    this.loadUserAccounts();

    if (this.activeAccount?.id) {
      this.loadFixedExpenses(this.activeAccount.id);
      this.loadMonthlyBalance(this.activeAccount.id);
    }

    this.loadPendingDebtsSummary();
  }

  private loadPendingDebtsSummary(): void {
    this.isLoadingPendingDebts = true;

    this.transactionService.getPendingDebtsSummary().subscribe({
      next: (pendingDebts) => {
        this.pendingDebtsSummary = pendingDebts ?? [];
        this.isLoadingPendingDebts = false;
      },
      error: (err) => {
        console.error('Error cargando resumen de deudas pendientes:', err);
        this.pendingDebtsSummary = [];
        this.isLoadingPendingDebts = false;
      }
    });
  }

  private loadFixedExpenses(accountId: number): void {
    this.isLoadingFixedExpenses = true;

    this.analysisService.getFixedExpenses(accountId).subscribe({
      next: (data) => {
        this.fixedExpenses = data.fixedExpenses ?? [];
        this.isLoadingFixedExpenses = false;
      },
      error: (err) => {
        console.error('Error cargando gastos fijos:', err);
        this.fixedExpenses = [];
        this.isLoadingFixedExpenses = false;
      }
    });
  }

  private loadMonthlyBalance(accountId: number): void {
    this.isLoadingMonthlyBalance = true;

    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth() + 1;

    this.chartsService.getBarLineChartByMonth(accountId, year, month).subscribe({
      next: (data) => {
        const income = Number(data.data?.[0] ?? 0);
        const expense = Number(data.data?.[1] ?? 0);

        this.monthlyBalanceChartData = {
          labels: ['Ingresos', 'Gastos'],
          datasets: [
            {
              data: [income, expense],
              backgroundColor: ['rgba(34, 197, 94, 0.75)', 'rgba(239, 68, 68, 0.75)'],
              borderColor: ['#22c55e', '#ef4444'],
              borderWidth: 1,
              borderRadius: 10,
              barThickness: 26
            }
          ]
        };

        this.isLoadingMonthlyBalance = false;
      },
      error: (err) => {
        console.error('Error cargando balance mensual:', err);
        this.monthlyBalanceChartData = null;
        this.isLoadingMonthlyBalance = false;
      }
    });
  }

  isPaidThisMonth(tx: Transaction): boolean {
    if (!tx.nextRecurrenceDate) return false;
    const next = new Date(tx.nextRecurrenceDate + 'T00:00:00');
    const now = new Date();
    // Si nextRecurrenceDate ya pasó al mes siguiente, el scheduler ya generó el hijo este mes
    return next.getFullYear() > now.getFullYear() ||
           (next.getFullYear() === now.getFullYear() && next.getMonth() > now.getMonth());
  }

  /**
   * Devuelve la fecha de la ocurrencia de este mes:
   * - Si ya está pagado, calcula la fecha en el mes actual con el mismo día que nextRecurrenceDate
   * - Si está pendiente, devuelve nextRecurrenceDate directamente
   */
  getOccurrenceDateThisMonth(tx: Transaction): Date {
    if (!tx.nextRecurrenceDate) return new Date(tx.date);
    const next = new Date(tx.nextRecurrenceDate + 'T00:00:00');
    if (this.isPaidThisMonth(tx)) {
      const now = new Date();
      return new Date(now.getFullYear(), now.getMonth(), next.getDate());
    }
    return next;
  }

  trackByTransactionId(index: number, tx: Transaction): number {
    return tx.id ?? index;
  }

  openTransactionDetail(transactionId: number): void {
    this.router.navigate(['/transaction', transactionId]);
  }

  openOnboarding(): void {
    this.showOnboarding = true;
  }

  onOnboardingClosed(): void {
    this.showOnboarding = false;
  }

}
