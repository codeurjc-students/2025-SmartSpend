import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ThemeService } from '../../services/theme/theme.service';

type NavItem = {
  label: string;
  route: string;
};

@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './nav-bar.component.html',
  styleUrl: './nav-bar.component.css'
})
export class NavBarComponent {
  readonly navItems: NavItem[] = [
    { label: 'INICIO', route: '/dashboard' },
    { label: 'HISTORIAL', route: '/all-transactions' },
    { label: 'GRÁFICOS', route: '/charts' },
    { label: 'PREVISIÓN', route: '/forecast' },
    { label: 'RECURRENTES', route: '/recurring-transactions' },
    { label: 'PERFIL', route: '/profile' }
  ];

  isMobileMenuOpen = false;

  constructor(public themeService: ThemeService) {}

  toggleTheme(): void {
    this.themeService.toggle();
  }

  toggleMobileMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  closeMobileMenu(): void {
    this.isMobileMenuOpen = false;
  }
}
