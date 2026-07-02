import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private router: Router) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = localStorage.getItem('authToken');

    if (token) {
      // Clona la petición y añade la cabecera de autorización
      // El formato es "Bearer <tu_token_jwt>"
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });

    } else {
      console.warn('No se encontró token de autenticación en el almacenamiento local.');
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // Interceptar error 401 (Unauthorized)
        if (error.status === 401) {
          console.error('❌ Sesión expirada o token inválido:', error.error?.error);

          // Limpiar el token del localStorage
          localStorage.removeItem('authToken');

          // Redirigir al login
          this.router.navigate(['/login'], {
            queryParams: { returnUrl: this.router.url }
          });
        }

        return throwError(() => error);
      })
    );
  }
}
