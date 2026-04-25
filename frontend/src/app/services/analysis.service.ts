import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { environment } from '../environments/environment';
import type {
  CategoryTrendDto,
  FixedExpensesDto,
  ForecastBalanceDto
} from '../interfaces/analysis.models';

@Injectable({
  providedIn: 'root'
})
export class AnalysisService {
  private readonly baseUrl = `${environment.apiUrl}/charts`;

  constructor(private readonly http: HttpClient) {}

  getFixedExpenses(accountId: number): Observable<FixedExpensesDto> {
    const params = new HttpParams().set('accountId', accountId.toString());

    return this.http
      .get<FixedExpensesDto>(`${this.baseUrl}/fixed-expenses`, { params })
      .pipe(catchError((error) => this.handleError(error, 'fixed expenses')));
  }

  getForecastBalance(accountId: number): Observable<ForecastBalanceDto> {
    const params = new HttpParams().set('accountId', accountId.toString());

    return this.http
      .get<ForecastBalanceDto>(`${this.baseUrl}/forecast-balance`, { params })
      .pipe(catchError((error) => this.handleError(error, 'forecast balance')));
  }

  getCategoryTrend(accountId: number): Observable<CategoryTrendDto> {
    const params = new HttpParams().set('accountId', accountId.toString());

    return this.http
      .get<CategoryTrendDto>(`${this.baseUrl}/category-trend`, { params })
      .pipe(catchError((error) => this.handleError(error, 'category trend')));
  }

  private handleError(error: unknown, endpoint: string): Observable<never> {
    console.error(`AnalysisService error on ${endpoint}:`, error);
    return throwError(() => new Error(`Unable to load ${endpoint} data.`));
  }
}
