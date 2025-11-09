import { Routes } from '@angular/router';
import { LoginRegisterComponent } from './components/auth-components/login-register/login-register.component';
import { RegisterComponent } from './components/register/register.component';
import { TransactionListComponent } from './components/transaction-list/transaction-list.component';
import { authGuard } from './auth.guard';

export const routes: Routes = [

    {path: '', redirectTo: '/login', pathMatch: 'full' },
    { path: 'login', component: LoginRegisterComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'transactions', canActivate: [authGuard], component: TransactionListComponent },


];
