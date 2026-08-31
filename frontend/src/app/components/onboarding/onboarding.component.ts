import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-onboarding',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './onboarding.component.html',
  styleUrl: './onboarding.component.css'
})
export class OnboardingComponent {
  acceptedCheckbox = false;
  isLoading = false;
  errorMessage = '';

  constructor(
    private router: Router,
    private http: HttpClient,
    private authService: AuthService
  ) {}

  acceptPrivacy(): void {
    if (!this.acceptedCheckbox) return;

    this.isLoading = true;
    this.errorMessage = '';

    this.http.patch('/api/v1/users/me/accept-privacy', {}).subscribe({
      next: () => {
        this.authService.updateLocalPrivacyStatus(true);
        this.isLoading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Hubo un problema al guardar tus preferencias. Por favor, inténtalo de nuevo.';
        console.error(err);
      }
    });
  }
}
