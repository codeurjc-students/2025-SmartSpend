import { Component, OnInit, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../../../services/auth/auth.service';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../environments/environment';

declare const window: any;

@Component({
  selector: 'app-login-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login-register.component.html',
  styleUrls: ['./login-register.component.css']
})
export class LoginRegisterComponent implements OnInit {
  email: string = '';
  password: string = '';
  showPassword = false;
  errorMessage: string | null = null;
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
              error: (err) => {
                console.error(err);
                this.errorMessage = 'Error al iniciar sesión con Google';
              }
            });
          }
        }
      });

      const btnContainer = document.getElementById('google-signin-btn');
      if (btnContainer) {
        const containerWidth = btnContainer.getBoundingClientRect().width;
        const googleButtonWidth = Math.max(220, Math.min(300, Math.floor(containerWidth || 280)));

        window.google.accounts.id.renderButton(btnContainer, {
          type: 'standard',
          size: 'large',
          theme: 'filled_blue',
          text: 'signin_with',
          shape: 'rectangular',
          logo_alignment: 'left',
          width: googleButtonWidth
        });
      }
    };

    if (window.google?.accounts?.id) {
      initGoogle();
    } else {
      // El script aún no ha cargado, esperamos el evento de carga
      const script = document.querySelector('script[src*="accounts.google.com/gsi/client"]');
      if (script) {
        script.addEventListener('load', initGoogle);
      }
    }
  }

  onSubmit() {
    this.authService.login(this.email, this.password).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Email o contraseña incorrectas';
      },
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }
}
