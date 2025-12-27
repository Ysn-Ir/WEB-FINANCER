import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../layout/sidebar/sidebar.component';
import { AssetService } from '../../services/asset.service';
import { TransactionService } from '../../services/transaction.service';

import { MarketService } from '../../services/market.service';
import { ThemeService } from '../../services/theme.service';
import { RouterModule } from '@angular/router';
import { BaseChartDirective } from 'ng2-charts';
import { ChartData, ChartOptions } from 'chart.js';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, SidebarComponent, RouterModule, BaseChartDirective],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  netWorth: number = 0;
  monthlyCashFlow: number = 0;
  isLoading: boolean = true;
  isUpdating: boolean = false;

  constructor(
    private assetService: AssetService,
    private transactionService: TransactionService,
    private marketService: MarketService,
    private themeService: ThemeService // Inject
  ) { }

  ngOnInit() {
    this.calculateFinancials();
    this.themeService.isDarkTheme$.subscribe(isDark => {
      this.updateChartTheme(isDark);
    });
  }

  // ... (Data definitions)

  chartOptions: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: 'rgba(0,0,0,0.8)',
        titleColor: '#fff',
        bodyColor: '#fff',
        borderColor: 'rgba(255,255,255,0.1)',
        borderWidth: 1
      }
    },
    scales: {
      x: { display: false },
      y: { display: false }
    }
  };

  portfolioChartData: ChartData<'doughnut'> = {
    labels: [],
    datasets: [{ data: [], backgroundColor: ['#6366f1', '#8b5cf6', '#ec4899', '#10b981'], borderWidth: 0 }]
  };

  cashFlowChartData: ChartData<'line'> = {
    labels: [],
    datasets: [
      { label: 'Income', data: [], borderColor: '#10b981', tension: 0.4, fill: true, backgroundColor: 'rgba(16, 185, 129, 0.1)' },
      { label: 'Expenses', data: [], borderColor: '#ef4444', tension: 0.4, fill: true, backgroundColor: 'rgba(239, 68, 68, 0.1)' }
    ]
  };

  lineChartOptions: ChartOptions<'line'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { labels: { color: '#a1a1aa' } }
    },
    scales: {
      y: {
        grid: { color: 'rgba(255,255,255,0.05)' },
        ticks: { color: '#a1a1aa' }
      },
      x: {
        grid: { display: false },
        ticks: { color: '#a1a1aa' }
      }
    }
  };

  updateChartTheme(isDark: boolean) {
    const textColor = isDark ? '#a1a1aa' : '#64748b';
    const gridColor = isDark ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.05)';

    // Update Line Chart Options
    this.lineChartOptions = {
      ...this.lineChartOptions,
      plugins: {
        legend: { labels: { color: textColor } }
      },
      scales: {
        y: {
          grid: { color: gridColor },
          ticks: { color: textColor }
        },
        x: {
          grid: { display: false },
          ticks: { color: textColor }
        }
      }
    };
  }

  recentTransactions: any[] = [];

  calculateFinancials() {
    this.isLoading = true;

    // 1. Assets & Portfolio
    this.assetService.getAllAssets().subscribe({
      next: (assets) => {
        this.netWorth = assets.reduce((total, asset) => {
          const price = asset.currentPrice || asset.avgCost || 0;
          return total + (asset.quantity * price);
        }, 0);

        // Process Portfolio Allocation
        const allocation: { [key: string]: number } = {};
        assets.forEach(asset => {
          const val = asset.quantity * (asset.currentPrice || asset.avgCost || 0);
          allocation[asset.type] = (allocation[asset.type] || 0) + val;
        });

        this.portfolioChartData = {
          labels: Object.keys(allocation),
          datasets: [{
            data: Object.values(allocation),
            backgroundColor: ['#6366f1', '#f59e0b', '#10b981', '#ec4899'],
            hoverOffset: 4,
            borderWidth: 0
          }]
        };
      },
      error: (err) => console.error('Error fetching assets', err)
    });

    // 2. Transactions & Cash Flow
    this.transactionService.getAllTransactions().subscribe({
      next: (transactions) => {
        // Cash Flow for Card
        let income = 0;
        let expenses = 0;
        transactions.forEach(tx => {
          if (tx.type === 'INCOME') income += tx.amount;
          else if (tx.type === 'EXPENSE') expenses += tx.amount;
        });
        this.monthlyCashFlow = income - expenses;

        // Recent Transactions
        this.recentTransactions = [...transactions]
          .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())
          .slice(0, 5);

        // Cash Flow Trends (Last 6 Months)
        const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
        const flowData: { [key: string]: { inc: number, exp: number } } = {};

        // Init last 6 months buckets
        const today = new Date();
        const labels = [];
        for (let i = 5; i >= 0; i--) {
          const d = new Date(today.getFullYear(), today.getMonth() - i, 1);
          const label = months[d.getMonth()];
          labels.push(label);
          flowData[label] = { inc: 0, exp: 0 };
        }

        transactions.forEach(tx => {
          const d = new Date(tx.date);
          const label = months[d.getMonth()];
          if (flowData[label]) {
            if (tx.type === 'INCOME') flowData[label].inc += tx.amount;
            if (tx.type === 'EXPENSE') flowData[label].exp += tx.amount;
          }
        });

        this.cashFlowChartData = {
          labels: labels,
          datasets: [
            { label: 'Income', data: labels.map(l => flowData[l].inc), borderColor: '#10b981', tension: 0.4, fill: true, backgroundColor: 'rgba(16, 185, 129, 0.1)' },
            { label: 'Expenses', data: labels.map(l => flowData[l].exp), borderColor: '#ef4444', tension: 0.4, fill: true, backgroundColor: 'rgba(239, 68, 68, 0.1)' }
          ]
        };

        this.isLoading = false;
      },
      error: (err) => console.error('Error fetching transactions', err)
    });
  }

  // updateChart() logic is now merged above, removing standalone method
  updateChart() { }

  refreshPrices() {
    this.isUpdating = true;
    this.assetService.refreshPrices().subscribe({
      next: () => {
        this.calculateFinancials(); // Re-fetch to get new values
        this.isUpdating = false;
      },
      error: (err: any) => {
        console.error('Failed to update prices', err);
        this.isUpdating = false;
      }
    });
  }
}
