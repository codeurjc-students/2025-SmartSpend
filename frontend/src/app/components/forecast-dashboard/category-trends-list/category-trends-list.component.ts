import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

import type { CategoryTrendDto, CategoryMetricsDto } from '../../../interfaces/analysis.models';

interface CategoryTrendView {
  categoryName: string;
  metrics: CategoryMetricsDto;
}

@Component({
  selector: 'app-category-trends-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './category-trends-list.component.html',
  styleUrl: './category-trends-list.component.scss'
})
export class CategoryTrendsListComponent {
  @Input({ required: true }) categoryTrend: CategoryTrendDto | null = null;

  get categories(): CategoryTrendView[] {
    if (!this.categoryTrend?.categories) {
      return [];
    }

    return Object.entries(this.categoryTrend.categories)
      .map(([categoryName, metrics]) => ({ categoryName, metrics }))
      .sort((a, b) => b.metrics.currentMonthForecast - a.metrics.currentMonthForecast);
  }

  getVariationClass(trend: CategoryMetricsDto['trend']): string {
    if (trend === 'UP') {
      return 'trend-up';
    }

    if (trend === 'DOWN') {
      return 'trend-down';
    }

    return 'trend-stable';
  }

  getVariationIcon(trend: CategoryMetricsDto['trend']): string {
    if (trend === 'UP') {
      return '↑';
    }

    if (trend === 'DOWN') {
      return '↓';
    }

    return '→';
  }

  calculateBarWidth(value: number, metrics: CategoryMetricsDto): number {
    const maxValue = Math.max(metrics.lastMonthAmount, metrics.currentMonthActual, metrics.currentMonthForecast, 1);
    return (value / maxValue) * 100;
  }
}
