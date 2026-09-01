import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { Subscription, forkJoin } from 'rxjs';
import { DecimalPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { NgApexchartsModule } from 'ng-apexcharts';

import { ChartsService } from '../../services/charts.service';
import { ReportService } from '../../services/report.service';
import { BankAccountServiceService, BankAccount } from '../../services/bankAccount/bank-account-service.service';
import { PieChartDto, BarLineChartDto, TimelineChartDto, TransactionType } from '../../interfaces/chart.interface';
import { ThemeService } from '../../services/theme/theme.service';
import { TransactionService } from '../../services/transaction/transaction.service';
import { CategoryService } from '../../services/category/category.service';
import { Category } from '../../interfaces/category.interface';
import { Transaction } from '../../interfaces/transaction.interface';
import { TransactionFilters } from '../../interfaces/pagination.interface';

@Component({
  selector: 'app-charts',
  standalone: true,
  imports: [FormsModule, NgApexchartsModule, DecimalPipe, DatePipe, RouterLink],
  templateUrl: './charts.component.html',
  styleUrls: ['./charts.component.css']
})
export class ChartsComponent implements OnInit, OnDestroy {
  TransactionType = TransactionType;

  // ── Account / date state ─────────────────────────────────────────────────
  bankAccounts: BankAccount[] = [];
  selectedAccountId: number | null = null;
  currentYear = new Date().getFullYear();
  currentMonth = new Date().getMonth() + 1;
  selectedYear = this.currentYear;
  selectedMonth = this.currentMonth;
  viewType: 'monthly' | 'yearly' = 'monthly';

  // ── Category detail panel ────────────────────────────────────────────────
  selectedCategory: { name: string; color: string } | null = null;
  categoryTransactions: Transaction[] = [];
  categoryTotal = 0;
  isLoadingDetails = false;
  private categoryMap = new Map<string, Category>();

  // ── Loading states ───────────────────────────────────────────────────────
  loadingPieIncomes = false;
  loadingPieExpenses = false;
  loadingBarChart = false;
  loadingTimelineChart = false;
  generatingPdf = false;

  // ── Totals ───────────────────────────────────────────────────────────────
  incomeTotal = 0;
  expenseTotal = 0;
  balance = 0;

  // ── ApexCharts data ──────────────────────────────────────────────────────
  incomePieSeries: number[] = [];
  incomePieReady = false;
  incomePieOptions: any = {};

  expensePieSeries: number[] = [];
  expensePieReady = false;
  expensePieOptions: any = {};

  barSeries: any[] = [];
  barReady = false;
  barOptions: any = {};

  timelineSeries: any[] = [];
  timelineReady = false;
  timelineOptions: any = {};

  private readonly PIE_COLORS = [
    '#38bdf8', '#818cf8', '#34d399', '#fb923c',
    '#e879f9', '#f472b6', '#a3e635', '#facc15',
    '#2dd4bf', '#f87171', '#60a5fa', '#a78bfa'
  ];

  private get activePieColors(): string[] {
    return this.PIE_COLORS;
  }

  private themeSub?: Subscription;

  constructor(
    private chartsService: ChartsService,
    private bankAccountService: BankAccountServiceService,
    private reportService: ReportService,
    private themeService: ThemeService,
    private transactionService: TransactionService,
    private categoryService: CategoryService
  ) {}

  ngOnInit() {
    this.rebuildChartOptions();
    this.themeSub = this.themeService.theme$.subscribe(() => this.rebuildChartOptions());
    this.loadBankAccounts();
    this.loadCategories();
  }

  ngOnDestroy() {
    this.themeSub?.unsubscribe();
  }

  @HostListener('window:resize')
  onWindowResize() {
    this.rebuildChartOptions();
  }

  // ── Option builders ──────────────────────────────────────────────────────

  private rebuildChartOptions() {
    this.incomePieOptions = this.buildDonutOptions(TransactionType.INCOME, this.incomePieSeries.length > 0 ? this.incomePieOptions?.labels ?? [] : []);
    this.expensePieOptions = this.buildDonutOptions(TransactionType.EXPENSE, this.expensePieSeries.length > 0 ? this.expensePieOptions?.labels ?? [] : []);
    this.barOptions = this.buildBarOptions([]);
    if (this.timelineReady) {
      const cats = this.timelineOptions?.xaxis?.categories ?? [];
      this.timelineOptions = this.buildTimelineOptions(cats);
    }
  }

  private buildDonutOptions(type: TransactionType, labels: string[]): any {
    const isDark = this.themeService.isDark;
    const textColor = isDark ? '#94a3b8' : '#64748b';

    return {
      chart: {
        type: 'donut',
        height: 320,
        background: 'transparent',
        toolbar: { show: false },
        animations: { enabled: true, speed: 500, animateGradually: { enabled: true, delay: 100 } },
        events: {
          dataPointSelection: (_e: any, _ctx: any, config: any) => {
            this.handleDonutClick(config.dataPointIndex, type);
          }
        }
      },
      colors: this.activePieColors,
      labels,
      legend: {
        show: true,
        position: 'bottom',
        fontFamily: 'inherit',
        fontSize: '12px',
        labels: { colors: textColor },
        markers: { size: 8, shape: 'circle' },
        itemMargin: { horizontal: 8, vertical: 4 }
      },
      plotOptions: {
        pie: {
          donut: {
            size: '68%',
            labels: {
              show: true,
              name: { show: true, fontSize: '13px', color: textColor, offsetY: -4 },
              value: {
                show: true,
                fontSize: '20px',
                fontWeight: 700,
                color: isDark ? '#e2e8f0' : '#1e293b',
                offsetY: 4,
                formatter: (val: string) => this.formatAmount(parseFloat(val))
              },
              total: {
                show: true,
                label: 'Total',
                fontSize: '12px',
                color: textColor,
                formatter: (w: any) => {
                  const total = w.globals.seriesTotals.reduce((a: number, b: number) => a + b, 0);
                  return this.formatAmount(total);
                }
              }
            }
          }
        }
      },
      dataLabels: { enabled: false },
      stroke: { width: 2, colors: [isDark ? '#0f172a' : '#f8fafc'] },
      tooltip: {
        theme: isDark ? 'dark' : 'light',
        style: { fontFamily: 'inherit', fontSize: '13px' },
        y: {
          formatter: (val: number, opts: any) => {
            const total = opts.globals.seriesTotals.reduce((a: number, b: number) => a + b, 0);
            const pct = total > 0 ? ((val / total) * 100).toFixed(1) : '0.0';
            return `${this.formatAmount(val)} · ${pct}%`;
          }
        }
      },
      states: {
        hover: { filter: { type: 'brighten', value: 0.08 } },
        active: { filter: { type: 'darken', value: 0.15 } }
      },
      theme: { mode: isDark ? 'dark' : 'light' }
    };
  }

  private buildBarOptions(categories: string[]): any {
    const isDark = this.themeService.isDark;
    const textColor = isDark ? '#94a3b8' : '#64748b';
    const gridColor = isDark ? 'rgba(148,163,184,0.1)' : 'rgba(148,163,184,0.2)';

    return {
      chart: {
        type: 'bar',
        height: 320,
        background: 'transparent',
        toolbar: { show: false },
        animations: { enabled: true, speed: 500 }
      },
      colors: ['#34d399', '#f87171'],
      plotOptions: {
        bar: {
          horizontal: false,
          columnWidth: '42%',
          borderRadius: 8,
          borderRadiusApplication: 'end'
        }
      },
      dataLabels: { enabled: false },
      stroke: { show: false },
      legend: {
        show: true,
        position: 'top',
        fontFamily: 'inherit',
        fontSize: '12px',
        labels: { colors: textColor },
        markers: { size: 8, shape: 'circle' }
      },
      xaxis: {
        categories,
        labels: { style: { colors: textColor, fontFamily: 'inherit', fontSize: '12px' } },
        axisBorder: { show: false },
        axisTicks: { show: false }
      },
      yaxis: {
        labels: {
          style: { colors: textColor, fontFamily: 'inherit', fontSize: '12px' },
          formatter: (val: number) => this.formatCompactCurrency(val)
        }
      },
      grid: {
        borderColor: gridColor,
        strokeDashArray: 4,
        xaxis: { lines: { show: false } }
      },
      tooltip: {
        theme: isDark ? 'dark' : 'light',
        style: { fontFamily: 'inherit', fontSize: '13px' },
        y: { formatter: (val: number) => this.formatAmount(val) }
      },
      fill: {
        type: 'gradient',
        gradient: {
          shade: isDark ? 'dark' : 'light',
          type: 'vertical',
          shadeIntensity: 0.15,
          opacityFrom: 0.95,
          opacityTo: 0.72
        }
      },
      theme: { mode: isDark ? 'dark' : 'light' }
    };
  }

  private buildTimelineOptions(categories: string[]): any {
    const isDark = this.themeService.isDark;
    const isMobile = this.isMobileViewport();
    const textColor = isDark ? '#94a3b8' : '#64748b';
    const gridColor = isDark ? 'rgba(148,163,184,0.1)' : 'rgba(148,163,184,0.2)';

    return {
      chart: {
        type: 'area',
        height: isMobile ? 280 : 384,
        background: 'transparent',
        toolbar: { show: false },
        zoom: { enabled: false },
        animations: { enabled: true, speed: 600 }
      },
      colors: ['#38bdf8', '#34d399', '#f87171'],
      fill: {
        type: ['gradient', 'solid', 'solid'],
        gradient: {
          shade: isDark ? 'dark' : 'light',
          type: 'vertical',
          shadeIntensity: 0.25,
          opacityFrom: 0.5,
          opacityTo: 0.02,
          stops: [0, 90, 100]
        },
        opacity: [1, 0.12, 0.12]
      },
      stroke: { curve: 'smooth', width: [3, 2, 2] },
      dataLabels: { enabled: false },
      markers: {
        size: isMobile ? 0 : [4, 3, 3],
        hover: { size: isMobile ? 4 : 6 }
      },
      xaxis: {
        categories,
        labels: {
          style: { colors: textColor, fontFamily: 'inherit', fontSize: '11px' },
          rotate: 0,
          hideOverlappingLabels: true
        },
        axisBorder: { show: false },
        axisTicks: { show: false },
        tickAmount: isMobile ? 4 : 8
      },
      yaxis: {
        labels: {
          style: { colors: textColor, fontFamily: 'inherit', fontSize: '11px' },
          formatter: (val: number) => this.formatCompactCurrency(val)
        }
      },
      grid: {
        borderColor: gridColor,
        strokeDashArray: 4,
        xaxis: { lines: { show: false } }
      },
      legend: {
        show: true,
        position: 'top',
        fontFamily: 'inherit',
        fontSize: '12px',
        labels: { colors: textColor },
        markers: { size: 8, shape: 'circle' }
      },
      tooltip: {
        theme: isDark ? 'dark' : 'light',
        style: { fontFamily: 'inherit', fontSize: '13px' },
        shared: true,
        intersect: false,
        y: { formatter: (val: number) => this.formatAmount(val) }
      },
      theme: { mode: isDark ? 'dark' : 'light' }
    };
  }

  // ── Data loaders ─────────────────────────────────────────────────────────

  private loadBankAccounts() {
    this.bankAccountService.getBankAccounts().subscribe({
      next: (accounts) => {
        this.bankAccounts = accounts;
        if (accounts.length > 0) {
          this.selectedAccountId = accounts[0].id;
          this.loadCharts();
        }
      },
      error: (error) => console.error('Error al cargar cuentas bancarias:', error)
    });
  }

  onAccountChange() { this.closeDetails(); this.loadCharts(); }
  onDateChange() { this.closeDetails(); this.loadCharts(); }
  onViewTypeChange() { this.loadCharts(); }

  loadCharts() {
    if (!this.selectedAccountId) return;
    this.loadPieChart(TransactionType.INCOME);
    this.loadPieChart(TransactionType.EXPENSE);
    this.loadBarChart();
    this.loadTimelineChart();
  }

  private loadPieChart(type: TransactionType) {
    if (!this.selectedAccountId) return;
    const isIncome = type === TransactionType.INCOME;
    if (isIncome) this.loadingPieIncomes = true;
    else this.loadingPieExpenses = true;

    const obs = this.viewType === 'monthly'
      ? this.chartsService.getPieChartByMonth(this.selectedAccountId, this.selectedYear, this.selectedMonth, type)
      : this.chartsService.getPieChartByYear(this.selectedAccountId, this.selectedYear, type);

    obs.subscribe({
      next: (data: PieChartDto) => {
        const opts = { ...this.buildDonutOptions(type, data.labels), labels: data.labels };
        if (isIncome) {
          this.incomePieSeries = data.data;
          this.incomePieOptions = opts;
          this.incomeTotal = data.totalAmount;
          this.incomePieReady = data.data.length > 0;
          this.loadingPieIncomes = false;
        } else {
          this.expensePieSeries = data.data;
          this.expensePieOptions = opts;
          this.expenseTotal = data.totalAmount;
          this.expensePieReady = data.data.length > 0;
          this.loadingPieExpenses = false;
        }
        this.balance = this.incomeTotal - this.expenseTotal;
      },
      error: () => {
        if (isIncome) { this.loadingPieIncomes = false; this.incomePieReady = false; this.incomeTotal = 0; }
        else { this.loadingPieExpenses = false; this.expensePieReady = false; this.expenseTotal = 0; }
        this.balance = this.incomeTotal - this.expenseTotal;
      }
    });
  }

  private loadBarChart() {
    if (!this.selectedAccountId) return;
    this.loadingBarChart = true;

    const obs = this.viewType === 'monthly'
      ? this.chartsService.getBarLineChartByMonth(this.selectedAccountId, this.selectedYear, this.selectedMonth)
      : this.chartsService.getBarLineChartByYear(this.selectedAccountId, this.selectedYear);

    obs.subscribe({
      next: (data: BarLineChartDto) => {
        this.barSeries = [
          { name: 'Ingresos', data: [data.data[0], 0] },
          { name: 'Gastos', data: [0, data.data[1]] }
        ];
        this.barOptions = this.buildBarOptions(data.labels);
        this.barReady = true;
        this.loadingBarChart = false;
      },
      error: () => { this.barReady = false; this.loadingBarChart = false; }
    });
  }

  private loadTimelineChart() {
    if (!this.selectedAccountId) return;
    this.loadingTimelineChart = true;

    const obs = this.viewType === 'monthly'
      ? this.chartsService.getTimelineChartByMonth(this.selectedAccountId, this.selectedYear, this.selectedMonth)
      : this.chartsService.getTimelineChartByYear(this.selectedAccountId, this.selectedYear);

    obs.subscribe({
      next: (data: TimelineChartDto) => {
        this.timelineSeries = [
          { name: 'Balance', data: data.balanceData },
          { name: 'Ingresos', data: data.incomesData },
          { name: 'Gastos', data: data.expensesData }
        ];
        this.timelineOptions = this.buildTimelineOptions(data.labels);
        this.timelineReady = true;
        this.loadingTimelineChart = false;
      },
      error: () => { this.timelineReady = false; this.loadingTimelineChart = false; }
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private isMobileViewport(): boolean {
    return typeof window !== 'undefined' && window.innerWidth <= 768;
  }

  private formatAmount(value: number): string {
    return `${new Intl.NumberFormat('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(value)} €`;
  }

  private formatCompactCurrency(value: number): string {
    const isDark = this.themeService.isDark;
    const formatted = new Intl.NumberFormat('es-ES', {
      notation: Math.abs(value) >= 1000 ? 'compact' : 'standard',
      maximumFractionDigits: Math.abs(value) >= 1000 ? 1 : 0
    }).format(value);
    return `${formatted} €`;
  }

  getMonthName(month: number): string {
    const months = ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'];
    return months[month - 1];
  }

  // ── Category detail panel ─────────────────────────────────────────────────

  private loadCategories() {
    forkJoin({
      income: this.categoryService.getCategoriesForType('INCOME'),
      expense: this.categoryService.getCategoriesForType('EXPENSE')
    }).subscribe({
      next: ({ income, expense }) => {
        [...income, ...expense].forEach(cat => this.categoryMap.set(cat.name, cat));
      }
    });
  }

  private getDateRange(): { dateFrom: string; dateTo: string } {
    const pad = (n: number) => String(n).padStart(2, '0');
    if (this.viewType === 'monthly') {
      const daysInMonth = new Date(this.selectedYear, this.selectedMonth, 0).getDate();
      return {
        dateFrom: `${this.selectedYear}-${pad(this.selectedMonth)}-01`,
        dateTo: `${this.selectedYear}-${pad(this.selectedMonth)}-${pad(daysInMonth)}`
      };
    }
    return { dateFrom: `${this.selectedYear}-01-01`, dateTo: `${this.selectedYear}-12-31` };
  }

  handleDonutClick(index: number, type: TransactionType) {
    const labels = type === TransactionType.INCOME ? this.incomePieOptions.labels : this.expensePieOptions.labels;
    const series = type === TransactionType.INCOME ? this.incomePieSeries : this.expensePieSeries;
    if (!labels || index < 0 || index >= labels.length) return;
    const label = labels[index];
    const value = series[index] ?? 0;
    const color = this.activePieColors[index % this.activePieColors.length];
    this.onCategoryClick(label, value, type, color);
  }

  onCategoryClick(categoryName: string, totalAmount: number, type: TransactionType, color = '#64748b') {
    const category = this.categoryMap.get(categoryName);
    this.selectedCategory = { name: categoryName, color };
    this.categoryTotal = totalAmount;
    this.categoryTransactions = [];
    this.isLoadingDetails = true;

    const { dateFrom, dateTo } = this.getDateRange();
    const filters: TransactionFilters = {
      dateFrom,
      dateTo,
      type: type as 'INCOME' | 'EXPENSE',
      ...(category ? { categoryId: String(category.id) } : {})
    };

    this.transactionService.getTransactionsPaginated(this.selectedAccountId!, 0, 50, filters).subscribe({
      next: (response) => {
        this.categoryTransactions = category
          ? response.content
          : response.content.filter(t => t.category.name === categoryName);
        this.isLoadingDetails = false;
      },
      error: () => { this.isLoadingDetails = false; }
    });
  }

  closeDetails() {
    this.selectedCategory = null;
    this.categoryTransactions = [];
    this.categoryTotal = 0;
    this.isLoadingDetails = false;
  }

  generateMonthlyPdf() {
    if (!this.selectedAccountId || this.generatingPdf) return;
    this.generatingPdf = true;
    this.reportService.getReportData(this.selectedAccountId, this.selectedYear, this.selectedMonth)
      .subscribe({
        next: async (reportData) => {
          try {
            await this.reportService.generateMonthlyPdf(reportData, this.selectedYear, this.selectedMonth);
          } catch (error) {
            console.error('Error generando PDF:', error);
          } finally {
            this.generatingPdf = false;
          }
        },
        error: () => { this.generatingPdf = false; }
      });
  }
}
