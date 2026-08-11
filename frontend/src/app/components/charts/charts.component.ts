import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { Subscription, forkJoin } from 'rxjs';

import { DecimalPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BaseChartDirective } from 'ng2-charts';
import { Chart, ChartConfiguration, registerables } from 'chart.js';

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

// Registrar todos los componentes de Chart.js
Chart.register(...registerables);

@Component({
  selector: 'app-charts',
  standalone: true,
  imports: [FormsModule, BaseChartDirective, DecimalPipe, DatePipe, RouterLink],
  templateUrl: './charts.component.html',
  styleUrls: ['./charts.component.css']
})
export class ChartsComponent implements OnInit, OnDestroy {
  TransactionType = TransactionType;  // Para usar en el template

  // Datos del usuario
  bankAccounts: BankAccount[] = [];
  selectedAccountId: number | null = null;
  currentYear = new Date().getFullYear();
  currentMonth = new Date().getMonth() + 1;

  // Configuración de fechas
  selectedYear = this.currentYear;
  selectedMonth = this.currentMonth;
  viewType: 'monthly' | 'yearly' = 'monthly'; // Nuevo selector de período

  // Estado del panel de detalle de categoría (lazy loading)
  selectedCategory: { name: string; color: string } | null = null;
  categoryTransactions: Transaction[] = [];
  categoryTotal = 0;
  isLoadingDetails = false;
  private categoryMap = new Map<string, Category>();

  // Estados de carga
  loadingPieIncomes = false;
  loadingPieExpenses = false;
  loadingBarChart = false;
  loadingTimelineChart = false;
  generatingPdf = false; // Nueva propiedad para el estado del PDF

  // Datos para los gráficos
  pieIncomesData: ChartConfiguration<'pie'>['data'] | null = null;
  pieExpensesData: ChartConfiguration<'pie'>['data'] | null = null;
  barChartData: ChartConfiguration<'bar'>['data'] | null = null;
  timelineChartData: ChartConfiguration<'line'>['data'] | null = null;
  private timelineSourceData: TimelineChartDto | null = null;
  private themeSub?: Subscription;

  // Totales calculados
  incomeTotal = 0;
  expenseTotal = 0;
  balance = 0;

  // Configuraciones de Chart.js
  pieChartOptions: ChartConfiguration<'pie'>['options'] = {
    responsive: true,
    plugins: {
      legend: {
        display: true,
        position: 'top'
      },
      tooltip: {
        callbacks: {
          label: (context) => {
            const label = context.label || '';
            const value = typeof context.raw === 'number' ? context.raw : Number(context.raw ?? 0);
            const dataset = Array.isArray(context.dataset.data)
              ? context.dataset.data as Array<number | null | undefined>
              : [];
            const total = dataset.reduce<number>(
              (sum, item) => sum + (typeof item === 'number' ? item : Number(item ?? 0)),
              0
            );
            const percentage = total > 0 ? (value / total) * 100 : 0;
            const formattedAmount = new Intl.NumberFormat('es-ES', {
              minimumFractionDigits: 2,
              maximumFractionDigits: 2
            }).format(value);
            const formattedPercentage = new Intl.NumberFormat('es-ES', {
              minimumFractionDigits: 1,
              maximumFractionDigits: 1
            }).format(percentage);

            return `${label}: €${formattedAmount} (${formattedPercentage}%)`;
          }
        }
      }
    }
  };

  barChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    plugins: {
      legend: {
        display: true
      },
      tooltip: {
        callbacks: {
          label: (context) => {
            const label = context.dataset.label || '';
            const value = context.formattedValue || '';
            return `${label}: €${value}`;
          }
        }
      }
    },
    scales: {
      y: {
        beginAtZero: true
      }
    }
  };

  lineChartOptions: ChartConfiguration<'line'>['options'] = {};

  constructor(
    private chartsService: ChartsService,
    private bankAccountService: BankAccountServiceService,
    private reportService: ReportService,
    private themeService: ThemeService,
    private transactionService: TransactionService,
    private categoryService: CategoryService
  ) {}

  ngOnInit() {
    this.updateTimelineChartPresentation();
    this.themeSub = this.themeService.theme$.subscribe(() => {
      this.updateTimelineChartPresentation();
    });
    this.loadBankAccounts();
    this.loadCategories();
  }

  ngOnDestroy() {
    this.themeSub?.unsubscribe();
  }

  @HostListener('window:resize')
  onWindowResize() {
    this.updateTimelineChartPresentation();
  }

  private loadBankAccounts() {
    this.bankAccountService.getBankAccounts().subscribe({
      next: (accounts) => {
        this.bankAccounts = accounts;
        if (accounts.length > 0) {
          this.selectedAccountId = accounts[0].id;
          this.loadCharts();
        }
      },
      error: (error) => {
        console.error('Error al cargar cuentas bancarias:', error);
      }
    });
  }

  onAccountChange() {
    this.closeDetails();
    this.loadCharts();
  }

  onDateChange() {
    this.closeDetails();
    this.loadCharts();
  }

  loadCharts() {
    if (!this.selectedAccountId) return;

    this.loadPieChart(TransactionType.INCOME);
    this.loadPieChart(TransactionType.EXPENSE);
    this.loadBarChart();
    this.loadTimelineChart();
  }

  onViewTypeChange() {
    this.loadCharts();
  }

  private loadPieChart(type: TransactionType) {
    if (!this.selectedAccountId) return;

    const isIncomes = type === TransactionType.INCOME;

    if (isIncomes) {
      this.loadingPieIncomes = true;
    } else {
      this.loadingPieExpenses = true;
    }

    // Elegir entre endpoint mensual o anual
    let chartObservable;
    if (this.viewType === 'monthly') {
      chartObservable = this.chartsService.getPieChartByMonth(
        this.selectedAccountId,
        this.selectedYear,
        this.selectedMonth,
        type
      );
    } else {
      chartObservable = this.chartsService.getPieChartByYear(
        this.selectedAccountId,
        this.selectedYear,
        type
      );
    }

    chartObservable.subscribe({
      next: (data: PieChartDto) => {
        const chartData = this.createPieChartData(data, type);

        if (isIncomes) {
          this.pieIncomesData = chartData;
          this.incomeTotal = data.totalAmount;
          this.loadingPieIncomes = false;
        } else {
          this.pieExpensesData = chartData;
          this.expenseTotal = data.totalAmount;
          this.loadingPieExpenses = false;
        }

        // Calcular balance cuando tengamos ambos totales
        this.balance = this.incomeTotal - this.expenseTotal;
      },
      error: (error) => {
        console.error(`Error al cargar gráfico de ${type}:`, error);
        if (isIncomes) {
          this.loadingPieIncomes = false;
          this.pieIncomesData = null;
          this.incomeTotal = 0;
        } else {
          this.loadingPieExpenses = false;
          this.pieExpensesData = null;
          this.expenseTotal = 0;
        }
        this.balance = this.incomeTotal - this.expenseTotal;
      }
    });
  }

  private loadBarChart() {
    if (!this.selectedAccountId) return;

    this.loadingBarChart = true;

    // Elegir entre endpoint mensual o anual
    let chartObservable;
    if (this.viewType === 'monthly') {
      chartObservable = this.chartsService.getBarLineChartByMonth(
        this.selectedAccountId,
        this.selectedYear,
        this.selectedMonth
      );
    } else {
      chartObservable = this.chartsService.getBarLineChartByYear(
        this.selectedAccountId,
        this.selectedYear
      );
    }

    chartObservable.subscribe({
      next: (data: BarLineChartDto) => {
        this.barChartData = this.createBarChartData(data);
        this.loadingBarChart = false;
      },
      error: (error: any) => {
        console.error('Error al cargar gráfico de barras:', error);
        this.loadingBarChart = false;
        this.barChartData = null;
      }
    });
  }

  private createPieChartData(data: PieChartDto, type: TransactionType): ChartConfiguration<'pie'>['data'] {
    // Paleta de colores más variada y atractiva
    const colors = [
      '#3B82F6', // Azul
      '#EF4444', // Rojo
      '#10B981', // Verde
      '#F59E0B', // Amarillo/Naranja
      '#8B5CF6', // Púrpura
      '#06B6D4', // Cian
      '#F97316', // Naranja
      '#84CC16', // Lima
      '#EC4899', // Rosa
      '#6B7280', // Gris
      '#14B8A6', // Teal
      '#F87171'  // Rojo claro
    ];

    return {
      labels: data.labels,
      datasets: [{
        data: data.data,
        backgroundColor: colors.slice(0, data.labels.length),
        borderWidth: 2,
        borderColor: '#1e293b' // Color del borde como el fondo
      }]
    };
  }

  private createBarChartData(data: BarLineChartDto): ChartConfiguration<'bar'>['data'] {
    // Backend devuelve: labels: ["Incomes", "Expenses"], data: [totalIngresos, totalGastos]
    return {
      labels: data.labels,
      datasets: [
        {
          label: 'Ingresos',
          data: [data.data[0], 0], // Solo el primer valor (ingresos)
          backgroundColor: '#10B981',
          borderColor: '#059669',
          borderWidth: 1
        },
        {
          label: 'Gastos',
          data: [0, data.data[1]], // Solo el segundo valor (gastos)
          backgroundColor: '#EF4444',
          borderColor: '#DC2626',
          borderWidth: 1
        }
      ]
    };
  }

  private loadTimelineChart() {
    if (!this.selectedAccountId) return;

    this.loadingTimelineChart = true;

    // Elegir entre endpoint mensual o anual
    let chartObservable;
    if (this.viewType === 'monthly') {
      chartObservable = this.chartsService.getTimelineChartByMonth(
        this.selectedAccountId,
        this.selectedYear,
        this.selectedMonth
      );
    } else {
      chartObservable = this.chartsService.getTimelineChartByYear(
        this.selectedAccountId,
        this.selectedYear
      );
    }

    chartObservable.subscribe({
      next: (data: TimelineChartDto) => {
        this.timelineSourceData = data;
        this.timelineChartData = this.createTimelineChartData(data);
        this.loadingTimelineChart = false;
      },
      error: (error: any) => {
        console.error('Error al cargar gráfico timeline:', error);
        this.loadingTimelineChart = false;
        this.timelineSourceData = null;
        this.timelineChartData = null;
      }
    });
  }

  private createTimelineChartData(data: TimelineChartDto): ChartConfiguration<'line'>['data'] {
    const isMobile = this.isMobileViewport();

    return {
      labels: data.labels,
      datasets: [
        {
          label: 'Balance Acumulado',
          data: data.balanceData,
          borderColor: '#64B5F6',
          backgroundColor: 'rgba(100, 181, 246, 0.1)',
          borderWidth: isMobile ? 2.5 : 3,
          fill: true,
          tension: 0.35,
          pointRadius: isMobile ? 0 : 4,
          pointHoverRadius: isMobile ? 4 : 6,
          pointBackgroundColor: '#64B5F6'
        },
        {
          label: 'Ingresos Acumulados',
          data: data.incomesData,
          borderColor: '#66BB6A',
          backgroundColor: 'rgba(102, 187, 106, 0.1)',
          borderWidth: 2,
          fill: false,
          tension: 0.35,
          pointRadius: isMobile ? 0 : 3,
          pointHoverRadius: isMobile ? 3 : 5,
          pointBackgroundColor: '#66BB6A'
        },
        {
          label: 'Gastos Acumulados',
          data: data.expensesData,
          borderColor: '#EF7D7D',
          backgroundColor: 'rgba(239, 125, 125, 0.1)',
          borderWidth: 2,
          fill: false,
          tension: 0.35,
          pointRadius: isMobile ? 0 : 3,
          pointHoverRadius: isMobile ? 3 : 5,
          pointBackgroundColor: '#EF7D7D'
        }
      ]
    };
  }

  private updateTimelineChartPresentation() {
    this.lineChartOptions = this.buildLineChartOptions();

    if (this.timelineSourceData) {
      this.timelineChartData = this.createTimelineChartData(this.timelineSourceData);
    }
  }

  private buildLineChartOptions(): ChartConfiguration<'line'>['options'] {
    const isMobile = this.isMobileViewport();
    const isDark = this.themeService.isDark;
    const tickColor = isDark ? '#94a3b8' : '#64748b';
    const gridColor = isDark ? 'rgba(148, 163, 184, 0.14)' : 'rgba(148, 163, 184, 0.28)';
    const tooltipBackground = isDark ? 'rgba(15, 23, 42, 0.96)' : 'rgba(255, 255, 255, 0.96)';
    const tooltipTitle = isDark ? '#f8fafc' : '#0f172a';
    const tooltipBody = isDark ? '#e2e8f0' : '#334155';
    const tooltipBorder = isDark ? 'rgba(103, 232, 249, 0.2)' : 'rgba(148, 163, 184, 0.4)';

    return {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
        },
        tooltip: {
          backgroundColor: tooltipBackground,
          titleColor: tooltipTitle,
          bodyColor: tooltipBody,
          borderColor: tooltipBorder,
          borderWidth: 1,
          callbacks: {
            label: (context) => {
              const label = context.dataset.label || '';
              const value = context.parsed.y ?? 0;
              return `${label}: ${this.formatCompactCurrency(value)}`;
            }
          }
        }
      },
      layout: {
        padding: {
          top: 8,
          right: isMobile ? 8 : 14,
          bottom: 0,
          left: isMobile ? 4 : 10
        }
      },
      scales: {
        x: {
          grid: {
            display: false
          },
          border: {
            display: false
          },
          ticks: {
            color: tickColor,
            autoSkip: true,
            maxRotation: 0,
            minRotation: 0,
            maxTicksLimit: isMobile ? 5 : 8
          },
          title: {
            display: !isMobile,
            text: 'Tiempo',
            color: tickColor
          }
        },
        y: {
          beginAtZero: false,
          grid: {
            color: gridColor
          },
          border: {
            display: false
          },
          ticks: {
            color: tickColor,
            maxTicksLimit: isMobile ? 4 : 6,
            callback: (tickValue) => this.formatCompactCurrency(Number(tickValue))
          },
          title: {
            display: !isMobile,
            text: 'Cantidad (€)',
            color: tickColor
          }
        }
      },
      interaction: {
        intersect: false,
        mode: 'index'
      }
    };
  }

  private isMobileViewport(): boolean {
    return typeof window !== 'undefined' && window.innerWidth <= 768;
  }

  private formatCompactCurrency(value: number): string {
    const formatter = new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR',
      notation: Math.abs(value) >= 1000 ? 'compact' : 'standard',
      maximumFractionDigits: Math.abs(value) >= 1000 ? 1 : 0
    });

    return formatter.format(value);
  }

  generateMonthlyPdf() {
    if (!this.selectedAccountId || this.generatingPdf) return;

    console.log('Iniciando generación de PDF...');
    console.log('Parámetros:', {
      accountId: this.selectedAccountId,
      year: this.selectedYear,
      month: this.selectedMonth
    });

    this.generatingPdf = true;

    // Primero obtenemos los datos del reporte
    this.reportService.getReportData(this.selectedAccountId, this.selectedYear, this.selectedMonth)
      .subscribe({
        next: async (reportData) => {
          console.log('Datos del reporte recibidos exitosamente:', reportData);

          try {
            // Luego generamos el PDF con jsPDF (ahora async)
            await this.reportService.generateMonthlyPdf(reportData, this.selectedYear, this.selectedMonth);
            console.log('PDF generado exitosamente!');
          } catch (error) {
            console.error('Error generando PDF:', error);
            alert('Error al generar PDF: ' + error);
          } finally {
            this.generatingPdf = false;
          }
        },
        error: (error) => {
          console.error('Error obteniendo datos del reporte:', error);
          console.error('Error completo:', error);
          this.generatingPdf = false;
          alert('Error obteniendo datos del reporte. Revisa la consola para más detalles.');
        }
      });
  }

  getMonthName(month: number): string {
    const months = [
      'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
      'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
    ];
    return months[month - 1];
  }

  // ─── Category lazy-loading ───────────────────────────────────────────────

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

  onPieChartClick(event: { event?: unknown; active?: object[] }, type: TransactionType) {
    if (!event.active || event.active.length === 0) return;
    const index = (event.active[0] as { index: number }).index;
    const chartData = type === TransactionType.EXPENSE ? this.pieExpensesData : this.pieIncomesData;
    if (!chartData?.labels || !chartData?.datasets) return;
    const label = chartData.labels[index] as string;
    const value = (chartData.datasets[0].data[index] as number) ?? 0;
    const bgColors = chartData.datasets[0].backgroundColor;
    const color = Array.isArray(bgColors) ? (bgColors[index] as string) : '#64748b';
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
      error: () => {
        this.isLoadingDetails = false;
      }
    });
  }

  closeDetails() {
    this.selectedCategory = null;
    this.categoryTransactions = [];
    this.categoryTotal = 0;
    this.isLoadingDetails = false;
  }
}
