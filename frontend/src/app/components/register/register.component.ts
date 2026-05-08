import { Component, OnInit, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';
import { environment } from '../../environments/environment';

declare const window: any;

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  formData = {
    email: '',
    password: ''
  };
  showPassword = false;

  isLoading = false;
  errorMessage = '';
  successMessage = '';
  private isBrowser: boolean;

  constructor(
    private authService: AuthService,
    private router: Router,
    @Inject(PLATFORM_ID) platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  ngOnInit(): void {
    if (!this.isBrowser) return;

    const initGoogle = () => {
      window.google.accounts.id.initialize({
        client_id: environment.googleClientId,
        callback: (response: any) => {
          if (response?.credential) {
            this.authService.loginWithGoogle(response.credential).subscribe({
              next: () => this.router.navigate(['/dashboard']),
              error: (err: any) => {
                console.error(err);
                this.errorMessage = 'Error al registrarse con Google';
              }
            });
          }
        }
      });

      const btnContainer = document.getElementById('google-register-btn');
      if (btnContainer) {
        const containerWidth = btnContainer.getBoundingClientRect().width;
        const googleButtonWidth = Math.max(220, Math.min(260, Math.floor(containerWidth || 240)));

        window.google.accounts.id.renderButton(btnContainer, {
          type: 'standard',
          size: 'large',
          theme: 'filled_blue',
          text: 'signup_with',
          shape: 'rectangular',
          logo_alignment: 'left',
          width: googleButtonWidth
        });
      }
    };

    if (window.google?.accounts?.id) {
      initGoogle();
    } else {
      const script = document.querySelector('script[src*="accounts.google.com/gsi/client"]');
      if (script) {
        script.addEventListener('load', initGoogle);
      }
    }
  }

  onSubmit() {
    if (this.isLoading) return;

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.register(this.formData.email, this.formData.password)
      .subscribe({
        next: (response: any) => {
          console.log('Registro exitoso:', response);
          this.successMessage = '¡Cuenta creada exitosamente! Redirigiendo al login...';

          // Redirigir al login después de 2 segundos
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000);
        },
        error: (error: any) => {
          console.error('Error en registro:', error);
          this.errorMessage = 'Error al crear la cuenta. Por favor, inténtalo de nuevo.';
          this.isLoading = false;
        },
        complete: () => {
          this.isLoading = false;
        }
      });
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }
}
