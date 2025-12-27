import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../layout/sidebar/sidebar.component';
import { FormsModule } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts';
import { MarketService } from '../../services/market.service';
import { ThemeService } from '../../services/theme.service';
import { ChartConfiguration, ChartData, ChartType, ScaleOptions } from 'chart.js';

@Component({
  selector: 'app-market',
  standalone: true,
  imports: [CommonModule, SidebarComponent, FormsModule, BaseChartDirective],
  templateUrl: './market.component.html',
  styleUrl: './market.component.css'
})
export class MarketComponent implements OnInit {
  searchQuery: string = '';
  searchResults: any[] = [];
  selectedAsset: any = null;
  priceHistory: any[] = [];
  currentPrice: number | null = null;
  loading: boolean = false;

  // Filters
  searchFilter: string = 'ALL';
  timePeriod: string = '1D';

  // Chart Properties
  @ViewChild(BaseChartDirective) chart: BaseChartDirective | undefined;
  public lineChartData: ChartConfiguration['data'] = {
    datasets: [
      {
        data: [],
        label: 'Price History',
        backgroundColor: 'rgba(64, 224, 208, 0.2)',
        borderColor: 'rgba(64, 224, 208, 1)',
        pointBackgroundColor: 'rgba(148,159,177,1)',
        pointBorderColor: '#fff',
        pointHoverBackgroundColor: '#fff',
        pointHoverBorderColor: 'rgba(148,159,177,0.8)',
        fill: 'origin',
      }
    ],
    labels: []
  };

  public lineChartOptions: ChartConfiguration['options'] = {
    elements: {
      line: {
        tension: 0.4, // Smoother curve
        borderWidth: 3,
        fill: true, // Enable gradient fill
      },
      point: {
        radius: 4,
        hoverRadius: 6,
      }
    },
    scales: {
      y: {
        position: 'right', // Standard for financial charts
        grid: {
          color: 'rgba(255,255,255,0.05)',
        },
        ticks: {
          color: '#9ca3af',
          callback: (value) => '$' + value, // Simple currency format
          font: { family: 'Inter', size: 11 }
        },
        title: {
          display: true,
          text: 'Price (USD)',
          color: '#6b7280',
          font: { size: 12, weight: 'bold' }
        }
      },
      x: {
        grid: {
          display: false, // Cleaner look
        },
        ticks: {
          color: '#9ca3af',
          font: { family: 'Inter', size: 11 }
        },
        title: {
          display: true,
          text: 'Time (Last 7 Days)',
          color: '#6b7280',
          font: { size: 12, weight: 'bold' }
        }
      }
    },
    plugins: {
      legend: { display: false }, // Hide legend for cleaner look
      tooltip: {
        backgroundColor: 'rgba(20, 20, 25, 0.9)',
        titleColor: '#fff',
        bodyColor: '#ccc',
        borderColor: 'rgba(255,255,255,0.1)',
        borderWidth: 1,
        padding: 12,
        displayColors: false,
        callbacks: {
          label: (context) => {
            if (context.parsed.y !== null) {
              return 'Price: $' + context.parsed.y.toLocaleString();
            }
            return '';
          }
        }
      }
    },
    interaction: {
      mode: 'index',
      intersect: false,
    }
  };

  public lineChartType: ChartType = 'line';

  constructor(private marketService: MarketService, private themeService: ThemeService) { }

  ngOnInit() {
    this.search('BTC');
    this.themeService.isDarkTheme$.subscribe(isDark => {
      this.updateChartTheme(isDark);
    });
  }

  updateChartTheme(isDark: boolean) {
    const textColor = isDark ? '#9ca3af' : '#4b5563'; // Gray-400 vs Gray-600
    const gridColor = isDark ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.05)';
    const titleColor = isDark ? '#6b7280' : '#374151';

    if (this.lineChartOptions?.scales?.['y']) {
      const scale = this.lineChartOptions.scales['y'] as any;
      scale.grid = { color: gridColor };
      scale.ticks = {
        ...scale.ticks,
        color: textColor
      };
      if (scale.title) {
        scale.title.color = titleColor;
      }
    }

    if (this.lineChartOptions?.scales?.['x']) {
      const scale = this.lineChartOptions.scales['x'] as any;
      scale.ticks = {
        ...scale.ticks,
        color: textColor
      };
      if (scale.title) {
        scale.title.color = titleColor;
      }
    }

    this.chart?.update();
  }

  search(initial?: string) {
    const q = initial || this.searchQuery;
    if (!q) return;

    this.loading = true;
    this.searchResults = []; // Clear previous

    this.marketService.search(q, this.searchFilter).subscribe({
      next: (results) => {
        this.searchResults = results;
        this.loading = false;

        // Auto-select first result if exact match
        if (results.length > 0 && results[0].symbol === q.toUpperCase()) {
          this.selectAsset(results[0]);
        }
      },
      error: (err) => {
        console.error('Search failed', err);
        this.loading = false;
        // The backend has a fallback, so this real error only happens if backend is down
      }
    });
  }

  setSearchFilter(filter: string) {
    this.searchFilter = filter;
    if (this.searchQuery) this.search();
  }

  setTimePeriod(period: string) {
    this.timePeriod = period;
    if (this.selectedAsset) this.selectAsset(this.selectedAsset); // Refresh data
  }

  selectAsset(asset: any) {
    this.selectedAsset = asset;
    this.loading = true;

    // 1. Get Live Price FIRST
    this.marketService.getPrice(asset.symbol, asset.type).subscribe({
      next: (price) => {
        this.currentPrice = price;

        // 2. Get History with Period & Price Ref
        this.marketService.getHistory(asset.symbol, this.timePeriod, price).subscribe({
          next: (history) => {
            this.priceHistory = history;
            this.updateChart(history);
            this.loading = false;
          },
          error: (err) => {
            console.error(err);
            this.loading = false;
          }
        });
      },
      error: (err) => {
        console.error("Price fetch failed", err);
        this.loading = false;
      }
    });
  }

  updateChart(history: any[]) {
    // History is descending, so reverse for chart
    const sorted = [...history].sort((a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime());

    // START FIX: Append the live price to the graph so it connects perfectly
    if (this.currentPrice !== null) {
      // Only append if the last history point is somewhat older (e.g. > 1 min) or just force it for visual continuity
      // For simplicity, we always add "Now"
      sorted.push({
        timestamp: new Date().toISOString(),
        price: this.currentPrice,
        symbol: this.selectedAsset.symbol
      });
    }
    // END FIX

    this.lineChartData.labels = sorted.map(h => {
      const d = new Date(h.timestamp);

      // Dynamic Date Formatting based on period
      if (this.timePeriod === '7D') {
        return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
      } else {
        return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      }
    });

    this.lineChartData.datasets[0].data = sorted.map(h => h.price);

    // Force update
    this.chart?.update();
  }
}
