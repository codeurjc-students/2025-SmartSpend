import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts';
import { Chart, ChartConfiguration, registerables } from 'chart.js';

import { ChartsService } from '../../services/charts.service';
import { BankAccountServiceService, BankAccount } from '../../services/bankAccount/bank-account-service.service';
import { PieChartDto, BarLineChartDto, TransactionType } from '../../interfaces/chart.interface';

// Registrar todos los componentes de Chart.js
Chart.register(...registerables);

@Component({
  selector: 'app-charts',
  standalone: true,
  imports: [CommonModule, FormsModule, BaseChartDirective],
  templateUrl: './charts.component.html',
  styleUrls: ['./charts.component.css']
})
export class ChartsComponent implements OnInit {
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
  
  // Estados de carga
  loadingPieIncomes = false;
  loadingPieExpenses = false;
  loadingBarChart = false;
  
  // Datos para los gráficos
  pieIncomesData: ChartConfiguration<'pie'>['data'] | null = null;
  pieExpensesData: ChartConfiguration<'pie'>['data'] | null = null;
  barChartData: ChartConfiguration<'bar'>['data'] | null = null;
  
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
            const value = context.formattedValue || '';
            return `${label}: €${value}`;
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

  constructor(
    private chartsService: ChartsService,
    private bankAccountService: BankAccountServiceService
  ) {}

  ngOnInit() {
    this.loadBankAccounts();
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
    this.loadCharts();
  }

  onDateChange() {
    this.loadCharts();
  }

  loadCharts() {
    if (!this.selectedAccountId) return;
    
    this.loadPieChart(TransactionType.INCOME);
    this.loadPieChart(TransactionType.EXPENSE);
    this.loadBarChart();
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

  getMonthName(month: number): string {
    const months = [
      'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
      'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
    ];
    return months[month - 1];
  }
}