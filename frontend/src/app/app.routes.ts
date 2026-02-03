import { Routes } from '@angular/router';
import { LoginRegisterComponent } from './components/auth-components/login-register/login-register.component';
import { RegisterComponent } from './components/register/register.component';
import { TransactionListComponent } from './components/transaction-list/transaction-list.component';
import { authGuard } from './auth.guard';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { TransactionDetailComponent } from './components/transaction-detail/transaction-detail.component';
import { ProfileComponent } from './components/profile/profile.component';
import { AllTransactionsComponent } from './components/all-transactions/all-transactions.component';

export const routes: Routes = [

    {path: '', redirectTo: '/login', pathMatch: 'full' },
    { path: 'login', component: LoginRegisterComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'transactions', canActivate: [authGuard], component: TransactionListComponent },
    { path: 'all-transactions', canActivate: [authGuard], component: AllTransactionsComponent },
    { path: 'dashboard', canActivate: [authGuard], component: DashboardComponent },
    { path: 'transaction/:id', canActivate: [authGuard], component: TransactionDetailComponent},
    { path: 'profile', canActivate: [authGuard], component: ProfileComponent }
    
];
