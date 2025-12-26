import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor() {}

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
      console.warn('No se encontró token de autenticación en el almacenamiento local.');}

    return next.handle(request);
  }
}