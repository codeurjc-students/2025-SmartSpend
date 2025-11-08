import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthResponse } from './auth-response.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = environment.apiUrl

  constructor(private http: HttpClient){}
  
  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`/api/v1/auth/login`, { email, password })
      .pipe(
        tap(response => {
          this.storeToken(response.token);
        })
      );
  }

  register(username: string, email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`/api/v1/auth/register`, { 
      username, 
      email, 
      password 
    }).pipe(
      tap(response => {
        this.storeToken(response.token);
      })
    );
  }

  private storeToken(token: string): void {
    localStorage.setItem('authToken', token);
  }

  isAuthenticated(): boolean {
    const token = localStorage.getItem('authToken');
    return !!token;
  }
  
}
