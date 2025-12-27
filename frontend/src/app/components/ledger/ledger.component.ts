import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../layout/sidebar/sidebar.component';
import { TransactionService } from '../../services/transaction.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-ledger',
  standalone: true,
  imports: [CommonModule, SidebarComponent, FormsModule],
  templateUrl: './ledger.component.html',
  styleUrl: './ledger.component.css'
})
export class LedgerComponent implements OnInit {
  transactions: any[] = [];
  isAdding: boolean = false;
  isEditing: boolean = false;
  currentId: number | null = null;
  newTxn: any = {
    description: '',
    amount: 0,
    type: 'EXPENSE',
    category: 'General',
    date: new Date().toISOString().split('T')[0]
  };

  constructor(private transactionService: TransactionService) { }

  ngOnInit() {
    this.loadTransactions();
  }

  loadTransactions() {
    this.transactionService.getAllTransactions().subscribe({
      next: (data) => {
        this.transactions = data.sort((a: any, b: any) => new Date(b.date).getTime() - new Date(a.date).getTime());
      },
      error: (err) => {
        console.error('Failed to load transactions', err);
      }
    });
  }

  toggleAdd() {
    this.isAdding = !this.isAdding;
    if (!this.isAdding) this.resetForm();
  }

  saveTransaction() {
    // Basic validation
    if (!this.newTxn.description || !this.newTxn.amount) return;

    if (this.isEditing && this.currentId) {
      this.transactionService.updateTransaction(this.currentId, this.newTxn).subscribe({
        next: (saved) => {
          console.log('Transaction updated', saved);
          this.resetForm();
          this.loadTransactions();
        },
        error: (err) => console.error('Failed to update', err)
      });
    } else {
      this.transactionService.createTransaction(this.newTxn).subscribe({
        next: (saved) => {
          console.log('Transaction saved', saved);
          this.resetForm();
          this.loadTransactions();
        },
        error: (err) => console.error('Failed to save', err)
      });
    }
  }

  editTransaction(txn: any) {
    this.isAdding = true;
    this.isEditing = true;
    this.currentId = txn.id;
    this.newTxn = { ...txn, date: txn.date.split('T')[0] }; // Clone and fix date format
  }

  deleteTransaction(id: number) {
    if (confirm('Are you sure you want to delete this transaction?')) {
      this.transactionService.deleteTransaction(id).subscribe({
        next: () => {
          console.log('Transaction deleted');
          this.loadTransactions();
        },
        error: (err) => console.error('Failed to delete', err)
      });
    }
  }

  resetForm() {
    this.isAdding = false;
    this.isEditing = false;
    this.currentId = null;
    this.newTxn = {
      description: '',
      amount: 0,
      type: 'EXPENSE',
      category: 'General',
      date: new Date().toISOString().split('T')[0]
    };
  }
}
