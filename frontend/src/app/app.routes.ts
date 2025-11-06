import { Routes } from '@angular/router';
import { LoginRegisterComponent } from './components/auth-components/login-register/login-register.component';
import { TransactionListComponent } from './components/transaction-list/transaction-list.component';
import { authGuard } from './auth.guard';

export const routes: Routes = [

    { path: 'login', component: LoginRegisterComponent },
    { path: 'register', component: LoginRegisterComponent },
    { path: 'transactions', canActivate: [authGuard], component: TransactionListComponent },


];
