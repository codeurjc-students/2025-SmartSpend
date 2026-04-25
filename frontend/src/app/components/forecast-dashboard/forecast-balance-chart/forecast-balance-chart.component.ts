import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges } from '@angular/core';

import { BaseChartDirective } from 'ng2-charts';
import { Chart, ChartConfiguration, registerables } from 'chart.js';

import type { ForecastBalanceDto } from '../../../interfaces/analysis.models';

Chart.register(...registerables);

@Component({
  selector: 'app-forecast-balance-chart',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  templateUrl: './forecast-balance-chart.component.html',
  styleUrl: './forecast-balance-chart.component.scss'
})
export class ForecastBalanceChartComponent implements OnChanges {
  @Input({ required: true }) forecast: ForecastBalanceDto | null = null;

  chartData: ChartConfiguration<'bar'>['data'] | null = null;

  chartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        labels: {
          color: '#e0e0e0'
        }
      }
    },
    scales: {
      x: {
        grid: {
          display: false
        },
        ticks: {
          color: '#e0e0e0'
        }
      },
      y: {
        beginAtZero: true,
        grid: {
          display: false
        },
        ticks: {
          color: '#e0e0e0',
          callback: (value: string | number) => `€${value}`
        }
      }
    }
  };

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
