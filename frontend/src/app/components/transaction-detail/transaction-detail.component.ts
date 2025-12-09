import { Component } from '@angular/core';
import { Transaction } from '../../interfaces/transaction.interface';
import { ActivatedRoute } from '@angular/router';
import { TransactionService } from '../../services/transaction/transaction.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-transaction-detail',
  standalone: true,
  imports: [CommonModule], 
  templateUrl: './transaction-detail.component.html',
  styleUrl: './transaction-detail.component.css'
})
export class TransactionDetailComponent {

  transactionId: number | null = null;
  transaction: Transaction | null = null;
  error: string | null = null;

  constructor(private route: ActivatedRoute, private transactionService: TransactionService) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const idParam = params.get('id');
      this.transactionId = idParam ? +idParam : null;
      
      if (this.transactionId !== null) {
        this.transactionService.getTransactionById(this.transactionId).subscribe({
          next: (data: Transaction) => {
            this.transaction = data;
            this.error = null;
          },
          error: (err) => {
            this.error = 'Error fetching transaction details: ' + err.message;
            this.transaction = null;
          }
        });
      } else {
        this.error = 'Invalid transaction ID';
      }

    });
  }
}
