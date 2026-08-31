import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './services/auth/auth.service';

const PRIVACY_EXEMPT_ROUTES = ['/onboarding', '/politica-privacidad', '/terminos-condiciones', '/cookies'];

export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }

  if (!auth.getPrivacyStatus() && !PRIVACY_EXEMPT_ROUTES.includes(state.url)) {
    router.navigate(['/onboarding']);
    return false;
  }

  return true;
};
