import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../environments/environment';
import { PieChartDto, BarLineChartDto, ComparisonChartDto, TimelineChartDto, TransactionType } from '../interfaces/chart.interface';

@Injectable({
  providedIn: 'root'
})
export class ChartsService {
  private baseUrl = `${environment.apiUrl}/charts`;

  constructor(private http: HttpClient) {}

  /**
   * Obtiene datos para gráfico de pastel mensual
   */
  getPieChartByMonth(
    accountId: number, 
    year: number, 
    month: number, 
    type: TransactionType
  ): Observable<PieChartDto> {
    const params = new HttpParams()
      .set('accountId', accountId.toString())
      .set('year', year.toString())
      .set('month', month.toString())
      .set('type', type);

    return this.http.get<PieChartDto>(`${this.baseUrl}/pie/monthly`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Obtiene datos para gráfico de pastel anual
   */
  getPieChartByYear(
    accountId: number, 
    year: number, 
    type: TransactionType
  ): Observable<PieChartDto> {
    const params = new HttpParams()
      .set('accountId', accountId.toString())
      .set('year', year.toString())
      .set('type', type);

    return this.http.get<PieChartDto>(`${this.baseUrl}/pie/yearly`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Obtiene datos para gráfico de barras mensual (ingresos vs gastos)
   */
  getBarLineChartByMonth(
    accountId: number, 
    year: number, 
    month: number
  ): Observable<BarLineChartDto> {
    const params = new HttpParams()
      .set('accountId', accountId.toString())
      .set('year', year.toString())
      .set('month', month.toString());

    return this.http.get<BarLineChartDto>(`${this.baseUrl}/bar/monthly`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Obtiene datos para gráfico de barras anual (ingresos vs gastos)
   */
  getBarLineChartByYear(
    accountId: number, 
    year: number
  ): Observable<BarLineChartDto> {
    const params = new HttpParams()
      .set('accountId', accountId.toString())
      .set('year', year.toString());

    return this.http.get<BarLineChartDto>(`${this.baseUrl}/bar/yearly`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Obtiene datos para gráfico timeline mensual (evolución del balance)
   */
  getTimelineChartByMonth(
    accountId: number,
    year: number,
    month: number
  ): Observable<TimelineChartDto> {
    const params = new HttpParams()
      .set('accountId', accountId.toString())
      .set('year', year.toString())
      .set('month', month.toString());

    return this.http.get<TimelineChartDto>(`${this.baseUrl}/timeline/monthly`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Obtiene datos para gráfico timeline anual (evolución del balance)
   */
  getTimelineChartByYear(
    accountId: number,
    year: number
  ): Observable<TimelineChartDto> {
    const params = new HttpParams()
      .set('accountId', accountId.toString())
      .set('year', year.toString());

    return this.http.get<TimelineChartDto>(`${this.baseUrl}/timeline/yearly`, { params })
      .pipe(
        catchError(this.handleError)
      );
  }

  /**
   * Manejo de errores
   */
  private handleError(error: any): Observable<never> {
    console.error('Error en ChartsService:', error);
    return throwError(() => new Error('Error al cargar los datos del gráfico'));
  }
}