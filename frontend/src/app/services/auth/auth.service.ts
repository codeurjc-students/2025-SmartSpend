import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthResponse } from './auth-response.model';
import { jwtDecode } from 'jwt-decode'; // Importa jwt-decode

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
          console.log('AuthService: Token almacenado después del login.'); // Mensaje de depuración
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
        console.log('AuthService: Token almacenado después del registro.'); // Mensaje de depuración
      })
    );
  }

  private storeToken(token: string): void {
    localStorage.setItem('authToken', token);
  }

  isAuthenticated(): boolean {
    const token = localStorage.getItem('authToken');
    
    if (!token) {
      return false; 
    }

    try {
      const decodedToken: any = jwtDecode(token); // Decodifica el token
      const currentTime = Date.now() / 1000; // Tiempo actual en segundos

      
      if (decodedToken.exp < currentTime) {
        console.log('AuthService: Token expirado. Cerrando sesión.'); 
        this.logout(); 
        return false;
      }
      
      console.log('AuthService: Token válido y no expirado. Usuario autenticado.'); // Mensaje de depuración
      return true; // El token existe y no ha expirado
    } catch (error) {
      console.error('AuthService: Error al decodificar el token JWT o token inválido:', error); // Mensaje de depuración
      this.logout(); // Si hay un error al decodificar, cierra sesión
      return false;
    }
  }

  logout(): void {
    localStorage.removeItem('authToken'); // Elimina el token del almacenamiento local
    console.log('AuthService: Token eliminado de localStorage. Sesión cerrada.'); // Mensaje de depuración
    // Opcional: redirigir al login después de cerrar sesión
    // const router = inject(Router); // Si quieres redirigir aquí, necesitarías inyectar Router
    // router.navigate(['/login']); 
  }
  
  getToken(): string | null {
    return localStorage.getItem('authToken');
  }
}