import { Component, OnInit } from '@angular/core';
import { RouterOutlet, RouterLink, Router, NavigationEnd, ActivatedRoute } from '@angular/router';
import { TransactionListComponent } from './components/transaction-list/transaction-list.component';
import { LoginRegisterComponent } from './components/auth-components/login-register/login-register.component';
import { NavBarComponent } from './components/nav-bar/nav-bar.component';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';
import { ThemeService } from './services/theme/theme.service';
import { TutorialModalComponent } from './components/tutorial-modal/tutorial-modal.component';

declare const gtag: (...args: unknown[]) => void;

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    NavBarComponent,
    CommonModule,
    TutorialModalComponent,
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})




export class AppComponent implements OnInit {
  title = 'smartspend-frontend';
  showNavbar = false;
  showOnboarding = false;
  isAuthRoute = false;

  constructor(private router: Router, private themeService: ThemeService) {
    this.themeService.init();
    const authRoutes = ['/login', '/register'];
    this.showNavbar = !authRoutes.some(route => this.router.url.includes(route));
    this.isAuthRoute = authRoutes.some(route => this.router.url.includes(route));
  }

  ngOnInit() {
    this.checkCurrentRoute();

    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.updateNavbarVisibility(event.url);
        gtag('config', 'G-CSV9GS9Q7L', {
          page_path: event.urlAfterRedirects,
        });
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

  private readonly NO_NAVBAR_ROUTES = ['/login', '/register', '/', '/onboarding', '/politica-privacidad', '/terminos-condiciones', '/cookies'];
  private readonly AUTH_ONLY_ROUTES = ['/login', '/register', '/'];

  private updateNavbarVisibility(url: string) {
    const path = url.split('?')[0];
    this.showNavbar = !this.NO_NAVBAR_ROUTES.includes(path);
    this.isAuthRoute = this.AUTH_ONLY_ROUTES.includes(path);
    console.log('Current URL:', url, 'Show navbar:', this.showNavbar);
  }

}
