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
    // 👈 Detectar la ruta inicial inmediatamente
    this.checkCurrentRoute();
    
    // Escucha los cambios de ruta posteriores
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.updateNavbarVisibility(event.url);
      });
  }

  private checkCurrentRoute() {
    const currentUrl = this.router.url;
    this.updateNavbarVisibility(currentUrl);
  }

  private updateNavbarVisibility(url: string) {
    // Oculta el navbar en las rutas de login y register
    this.showNavbar = !['/login', '/register', '/'].includes(url);
    console.log('Current URL:', url, 'Show navbar:', this.showNavbar);
  }

}
