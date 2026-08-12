import { Component, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, Router, NavigationEnd, ActivatedRoute } from '@angular/router';
import { TransactionListComponent } from './components/transaction-list/transaction-list.component';
import { LoginRegisterComponent } from './components/auth-components/login-register/login-register.component';
import { NavBarComponent } from './components/nav-bar/nav-bar.component';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';
import { ThemeService } from './services/theme/theme.service';
import { OnboardingComponent } from './components/onboarding/onboarding.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    NavBarComponent,
    CommonModule,
    OnboardingComponent
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})




export class AppComponent implements OnInit {
  title = 'smartspend-frontend';
  showNavbar = false;
  showOnboarding = false;

  constructor(private router: Router, private themeService: ThemeService) {
    // Aplicar tema guardado lo antes posible para evitar flash
    this.themeService.init();
    // Verificar ruta inicial inmediatamente en el constructor
    const authRoutes = ['/login', '/register'];
    this.showNavbar = !authRoutes.some(route => this.router.url.includes(route));
  }

  ngOnInit() {
    this.checkCurrentRoute();

    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.updateNavbarVisibility(event.url);
        if (event.url.includes('tutorial=true')) {
          this.showOnboarding = true;
          this.router.navigate([], { queryParams: {}, replaceUrl: true });
        } else if (event.url.startsWith('/dashboard') && !OnboardingComponent.isCompleted()) {
          this.showOnboarding = true;
        }
      });
  }

  openTutorial(): void {
    this.showOnboarding = true;
  }

  onOnboardingClosed(): void {
    this.showOnboarding = false;
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
