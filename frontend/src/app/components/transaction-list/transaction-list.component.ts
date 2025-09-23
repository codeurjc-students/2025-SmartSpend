import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Transaction, TransactionService } from '../../services/transaction/transaction.service';


@Component({
  selector: 'app-transaction-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './transaction-list.component.html',
  styleUrl: './transaction-list.component.css'
})
export class TransactionListComponent {
  transactions: Transaction[] = [];

  constructor(private transactionService: TransactionService){}

    ngOnInit(): void{
      this.transactionService.getTransactions().subscribe(data => {
        this.transactions = data;
      })
    }
}
