import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { TransactionListComponent } from './components/transaction-list/transaction-list.component';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    TransactionListComponent,
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})




export class AppComponent {
  title = 'smartspend-frontend';
}
