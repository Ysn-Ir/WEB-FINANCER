import { Component, OnInit, ViewChildren, QueryList } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../layout/sidebar/sidebar.component';
import { TransactionService } from '../../services/transaction.service';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration } from 'chart.js';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, SidebarComponent, BaseChartDirective],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.css'
})
export class ReportsComponent implements OnInit {
  isLoading = true;

  // Summary Stats
  totalIncome = 0;
  totalExpenses = 0;
  savingsRate = 0;

  @ViewChildren(BaseChartDirective) charts: QueryList<BaseChartDirective> | undefined;

  // Chart 1: Income vs Expenses (Bar)
  public barChartData: ChartConfiguration<'bar'>['data'] = {
    labels: [],
    datasets: [
      { data: [], label: 'Income', backgroundColor: '#10b981', hoverBackgroundColor: '#059669' },
      { data: [], label: 'Expense', backgroundColor: '#ef4444', hoverBackgroundColor: '#dc2626' }
    ]
  };
  public barChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { labels: { color: '#ccc' } } },
    scales: {
      x: { ticks: { color: '#999' }, grid: { display: false } },
      y: { ticks: { color: '#999' }, grid: { color: 'rgba(255,255,255,0.05)' } }
    }
  };

  // Chart 2: Category Breakdown (Doughnut)
  public doughnutChartData: ChartConfiguration<'doughnut'>['data'] = {
    labels: [],
    datasets: [{
      data: [],
      backgroundColor: ['#6366f1', '#8b5cf6', '#ec4899', '#f43f5e', '#10b981', '#f59e0b'],
      borderWidth: 0
    }]
  };
  public doughnutChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { position: 'right', labels: { color: '#ccc' } } },
    cutout: '70%'
  };

  constructor(private transactionService: TransactionService) { }

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.isLoading = true;
    this.transactionService.getAllTransactions().subscribe({
      next: (transactions) => {
        this.processData(transactions);
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading report data', err);
        this.isLoading = false;
      }
    });
  }

  processData(transactions: any[]) {
    // Reset stats
    this.totalIncome = 0;
    this.totalExpenses = 0;

    // Time-based aggregation
    const monthlyStats = new Map<string, { income: number, expense: number }>();
    const categoryStats = new Map<string, number>();

    // Helper to get Year-Month key
    const getMonthKey = (dateStr: string) => {
      const d = new Date(dateStr);
      return `${d.getFullYear()}-${d.getMonth() + 1}`; // e.g. "2024-5"
    };

    transactions.forEach(t => {
      const key = getMonthKey(t.date);
      if (!monthlyStats.has(key)) monthlyStats.set(key, { income: 0, expense: 0 });
      const m = monthlyStats.get(key)!;

      if (t.type === 'INCOME') {
        this.totalIncome += t.amount;
        m.income += t.amount;
      } else {
        this.totalExpenses += t.amount;
        m.expense += t.amount;

        // Category Logic
        categoryStats.set(t.category, (categoryStats.get(t.category) || 0) + t.amount);
      }
    });

    this.savingsRate = this.totalIncome > 0
      ? ((this.totalIncome - this.totalExpenses) / this.totalIncome) * 100
      : 0;

    // Sort months chronologically
    const sortedKeys = Array.from(monthlyStats.keys()).sort((a, b) => {
      const [y1, m1] = a.split('-').map(Number);
      const [y2, m2] = b.split('-').map(Number);
      return (y1 * 12 + m1) - (y2 * 12 + m2);
    }).slice(-6); // Last 6 months

    // Update Bar Chart
    this.barChartData.labels = sortedKeys.map(k => {
      const [y, m] = k.split('-');
      const date = new Date(Number(y), Number(m) - 1);
      return date.toLocaleString('default', { month: 'short' });
    });
    this.barChartData.datasets[0].data = sortedKeys.map(k => monthlyStats.get(k)!.income);
    this.barChartData.datasets[1].data = sortedKeys.map(k => monthlyStats.get(k)!.expense);

    // Update Doughnut Chart (Top 6 Categories)
    const sortedCats = Array.from(categoryStats.entries())
      .sort((a, b) => b[1] - a[1]) // highest expense first
      .slice(0, 6);

    this.doughnutChartData.labels = sortedCats.map(c => c[0]);
    this.doughnutChartData.datasets[0].data = sortedCats.map(c => c[1]);

    // Force chart update
    if (this.charts) {
      this.charts.forEach(c => c.update());
    }
  }
}
