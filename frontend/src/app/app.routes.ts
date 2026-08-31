import { Routes } from '@angular/router';
import { LoginRegisterComponent } from './components/auth-components/login-register/login-register.component';
import { RegisterComponent } from './components/register/register.component';
import { TransactionListComponent } from './components/transaction-list/transaction-list.component';
import { authGuard } from './auth.guard';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { TransactionDetailComponent } from './components/transaction-detail/transaction-detail.component';
import { ProfileComponent } from './components/profile/profile.component';
import { AllTransactionsComponent } from './components/all-transactions/all-transactions.component';
import { ChartsComponent } from './components/charts/charts.component';
import { ForecastDashboardComponent } from './components/forecast-dashboard/forecast-dashboard.component';
import { RecurringTransactionsComponent } from './components/recurring-transactions/recurring-transactions.component';
import { OnboardingComponent } from './components/onboarding/onboarding.component';
import { PrivacyPolicyComponent } from './components/legal/privacy-policy.component';
import { TermsComponent } from './components/legal/terms.component';

export const routes: Routes = [
    { path: '', redirectTo: '/login', pathMatch: 'full' },
    { path: 'login', component: LoginRegisterComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'politica-privacidad', component: PrivacyPolicyComponent },
    { path: 'terminos-condiciones', component: TermsComponent },
    { path: 'onboarding', canActivate: [authGuard], component: OnboardingComponent },
    { path: 'transactions', canActivate: [authGuard], component: TransactionListComponent },
    { path: 'all-transactions', canActivate: [authGuard], component: AllTransactionsComponent },
    { path: 'dashboard', canActivate: [authGuard], component: DashboardComponent },
    { path: 'charts', canActivate: [authGuard], component: ChartsComponent },
    { path: 'forecast', canActivate: [authGuard], component: ForecastDashboardComponent },
    { path: 'recurring-transactions', canActivate: [authGuard], component: RecurringTransactionsComponent },
    { path: 'transaction/:id', canActivate: [authGuard], component: TransactionDetailComponent },
    { path: 'profile', canActivate: [authGuard], component: ProfileComponent }
];
