import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subject, forkJoin, takeUntil } from 'rxjs';

import { ActiveAccountService } from '../../services/active-account/active-account.service';
import { AnalysisService } from '../../services/analysis.service';
import type {
  CategoryTrendDto,
  FixedExpensesDto,
  ForecastBalanceDto
} from '../../interfaces/analysis.models';
import type { BankAccount } from '../../services/bankAccount/bank-account-service.service';

import { FixedExpensesCardComponent } from './fixed-expenses-card/fixed-expenses-card.component';
import { ForecastBalanceChartComponent } from './forecast-balance-chart/forecast-balance-chart.component';
import { CategoryTrendsListComponent } from './category-trends-list/category-trends-list.component';

@Component({
  selector: 'app-forecast-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FixedExpensesCardComponent,
    ForecastBalanceChartComponent,
    CategoryTrendsListComponent
  ],
  templateUrl: './forecast-dashboard.component.html',
  styleUrl: './forecast-dashboard.component.scss'
})
export class ForecastDashboardComponent implements OnInit, OnDestroy {
  activeAccount: BankAccount | null = null;

  fixedExpensesData: FixedExpensesDto | null = null;
  forecastBalanceData: ForecastBalanceDto | null = null;
  categoryTrendData: CategoryTrendDto | null = null;

  isLoading = true;
  errorMessage = '';

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly activeAccountService: ActiveAccountService,
    private readonly analysisService: AnalysisService
  ) {}

  ngOnInit(): void {
    this.activeAccountService.activeAccount$
      .pipe(takeUntil(this.destroy$))
      .subscribe((account) => {
        this.activeAccount = account;

        if (account?.id) {
          this.loadForecastData(account.id);
        } else {
          this.resetData();
          this.isLoading = false;
          this.errorMessage = 'Selecciona una cuenta para ver la previsión.';
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  reload(): void {
    if (this.activeAccount?.id) {
      this.loadForecastData(this.activeAccount.id);
    }
  }

  private loadForecastData(accountId: number): void {
    this.isLoading = true;
    this.errorMessage = '';

    forkJoin({
      fixedExpenses: this.analysisService.getFixedExpenses(accountId),
      forecastBalance: this.analysisService.getForecastBalance(accountId),
      categoryTrend: this.analysisService.getCategoryTrend(accountId)
    })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: ({ fixedExpenses, forecastBalance, categoryTrend }) => {
          this.fixedExpensesData = fixedExpenses;
          this.forecastBalanceData = forecastBalance;
          this.categoryTrendData = categoryTrend;
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading forecast dashboard:', error);
          this.resetData();
          this.errorMessage =
            'No fue posible cargar la previsión. Revisa que el endpoint de análisis esté disponible.';
          this.isLoading = false;
        }
      });
  }

  private resetData(): void {
    this.fixedExpensesData = null;
    this.forecastBalanceData = null;
    this.categoryTrendData = null;
  }
}
