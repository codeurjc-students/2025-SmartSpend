import { Component, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, Router, NavigationEnd } from '@angular/router';
import { TransactionListComponent } from './components/transaction-list/transaction-list.component';
import { LoginRegisterComponent } from './components/auth-components/login-register/login-register.component';
import { NavBarComponent } from './components/nav-bar/nav-bar.component';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    NavBarComponent,
    CommonModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})




export class AppComponent implements OnInit {
  title = 'smartspend-frontend';
  showNavbar = false; // Iniciar en false para evitar flash del navbar

  constructor(private router: Router) {
    // Verificar ruta inicial inmediatamente en el constructor
    const authRoutes = ['/login', '/register'];
    this.showNavbar = !authRoutes.some(route => this.router.url.includes(route));
  }

  ngOnInit() {
    // Detectar cambios de ruta para mostrar/ocultar navbar
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: NavigationEnd) => {
        // Rutas donde NO queremos mostrar el navbar
        const authRoutes = ['/login', '/register'];
        this.showNavbar = !authRoutes.some(route => event.url.includes(route));
        
        // Debug en consola
        console.log('Current URL:', event.url, 'Show Navbar:', this.showNavbar);
      });
  }
}
