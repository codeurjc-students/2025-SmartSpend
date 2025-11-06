import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, Router } from '@angular/router';
import { TransactionListComponent } from './components/transaction-list/transaction-list.component';
import { LoginRegisterComponent } from './components/auth-components/login-register/login-register.component';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    TransactionListComponent,
    RouterOutlet,
    LoginRegisterComponent,
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})




export class AppComponent {
  title = 'smartspend-frontend';
}
