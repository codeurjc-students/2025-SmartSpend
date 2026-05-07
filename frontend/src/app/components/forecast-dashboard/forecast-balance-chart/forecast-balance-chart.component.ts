import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';

import { BaseChartDirective } from 'ng2-charts';
import { Chart, ChartConfiguration, registerables } from 'chart.js';

import type { ForecastBalanceDto } from '../../../interfaces/analysis.models';
import { ThemeService } from '../../../services/theme/theme.service';

Chart.register(...registerables);

@Component({
  selector: 'app-forecast-balance-chart',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  templateUrl: './forecast-balance-chart.component.html',
  styleUrl: './forecast-balance-chart.component.scss'
})
export class ForecastBalanceChartComponent implements OnChanges, OnInit, OnDestroy {
  @Input({ required: true }) forecast: ForecastBalanceDto | null = null;

  chartData: ChartConfiguration<'bar'>['data'] | null = null;

  private themeSub?: Subscription;

  constructor(private themeService: ThemeService) {}

  private get tickColor(): string {
    return this.themeService.isDark ? '#e0e0e0' : '#334155';
  }

  chartOptions: ChartConfiguration<'bar'>['options'] = {};

  private buildChartOptions(): ChartConfiguration<'bar'>['options'] {
    const color = this.tickColor;
    return {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: true,
          labels: {
            color
          }
        }
      },
      scales: {
        x: {
          grid: {
            display: false
          },
          ticks: {
            color
          }
        },
        y: {
          beginAtZero: true,
          grid: {
            display: false
          },
          ticks: {
            color,
            callback: (value: string | number) => `€${value}`
          }
        }
      }
    };
  }

  ngOnInit(): void {
    this.chartOptions = this.buildChartOptions();
    this.themeSub = this.themeService.theme$.subscribe(() => {
      this.chartOptions = this.buildChartOptions();
    });
  }

  ngOnDestroy(): void {
    this.themeSub?.unsubscribe();
  }

  ngOnChanges(): void {
    this.chartData = this.buildChartData();
  }

  private buildChartData(): ChartConfiguration<'bar'>['data'] | null {
    if (!this.forecast) {
      return null;
    }

    const labels = this.forecast.forecastBalanceIncomesChart.labels;
    const incomes = this.forecast.forecastBalanceIncomesChart.data;
    const expenses = this.forecast.forecastBalanceExpensesChart.data;

    return {
      labels,
      datasets: [
        {
          label: 'Ingresos',
          data: incomes,
          backgroundColor: [
            'rgba(148, 163, 184, 0.8)',
            'rgba(56, 216, 144, 1)',
            'rgba(56, 216, 144, 0.35)'
          ],
          borderRadius: 10,
          barThickness: 24
        },
        {
          label: 'Gastos',
          data: expenses,
          backgroundColor: [
            'rgba(148, 163, 184, 0.8)',
            'rgba(240, 110, 122, 1)',
            'rgba(240, 110, 122, 0.35)'
          ],
          borderRadius: 10,
          barThickness: 24
        }
      ]
    };
  }
}
