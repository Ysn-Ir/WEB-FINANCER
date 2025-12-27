import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from '../layout/sidebar/sidebar.component';
import { ForecastingService } from '../../services/forecasting.service';
import { FormsModule } from '@angular/forms';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';

@Component({
    selector: 'app-forecasting',
    standalone: true,
    imports: [CommonModule, SidebarComponent, FormsModule, BaseChartDirective],
    templateUrl: './forecasting.component.html',
    styleUrl: './forecasting.component.css'
})
export class ForecastingComponent implements OnInit {
    forecastData: any = null;
    loading: boolean = false;
    years: number = 5;
    monthlyContribution: number = 500;
    annualReturn: number = 7;

    summary: any = {
        projectedValue: 0,
        totalInvested: 0,
        totalProfit: 0,
        roi: 0
    };

    @ViewChild(BaseChartDirective) chart: BaseChartDirective | undefined;

    public lineChartData: ChartConfiguration['data'] = {
        datasets: [
            {
                data: [],
                label: 'Optimistic',
                borderColor: '#10b981', // Green
                backgroundColor: 'rgba(16, 185, 129, 0.1)',
                fill: false,
                tension: 0.4
            },
            {
                data: [],
                label: 'Expected',
                borderColor: '#6366f1', // Indigo
                backgroundColor: 'rgba(99, 102, 241, 0.1)',
                fill: false,
                tension: 0.4
            },
            {
                data: [],
                label: 'Conservative',
                borderColor: '#ef4444', // Red
                backgroundColor: 'rgba(239, 68, 68, 0.1)',
                fill: false,
                tension: 0.4
            },
            {
                data: [],
                label: 'Total Invested', // Principal
                borderColor: '#9ca3af', // Gray
                backgroundColor: 'transparent',
                borderDash: [5, 5],
                fill: false,
                pointRadius: 0,
                tension: 0
            }
        ],
        labels: []
    };

    public lineChartOptions: ChartConfiguration['options'] = {
        responsive: true,
        elements: {
            line: { tension: 0.4 },
            point: { radius: 2 }
        },
        scales: {
            y: {
                grid: { color: 'rgba(255,255,255,0.1)' },
                ticks: { color: '#ccc' }
            },
            x: {
                grid: { display: false },
                ticks: { color: '#ccc' }
            }
        },
        plugins: {
            legend: { labels: { color: '#fff' } }
        }
    };

    public lineChartType: ChartType = 'line';

    constructor(private forecastingService: ForecastingService) { }

    ngOnInit() {
        this.runForecast();
    }

    runForecast() {
        this.loading = true;
        this.forecastingService.getForecast(this.years, this.annualReturn, this.monthlyContribution).subscribe({
            next: (data) => {
                this.forecastData = data;
                this.updateChart(data);
                this.loading = false;
            },
            error: (err) => {
                console.error('Forecast failed', err);
                this.loading = false;
            }
        });
    }

    updateChart(data: any) {
        if (data.expectedScenario && data.expectedScenario.length > 0) {
            // Simplify labels
            this.lineChartData.labels = data.expectedScenario.map((_: any, index: number) => {
                const monthInfo = index + 1;
                return monthInfo % 12 === 0 ? `Year ${monthInfo / 12}` : '';
            });

            // Extract values
            const optimistic = data.optimisticScenario.map((d: any) => d.totalValue);
            const expected = data.expectedScenario.map((d: any) => d.totalValue);
            const conservative = data.conservativeScenario.map((d: any) => d.totalValue);

            // Calculate Principal (Invested)
            const initialValue = data.initialValue || 0;
            const principalData = data.expectedScenario.map((_: any, index: number) => {
                return initialValue + (this.monthlyContribution * index); // index 0 is start? usually index 0 is month 0
            });
            // Adjust: Backend loops 0 to months. 
            // If backend result includes month 0 (start), then principal is just initial.
            // If backend result is end of month 1..N, principal is initial + contribution * month.
            // Looking at backend code: loop i=0 to months. so index 0 is month 0.

            // Re-map principal properly
            const principal = data.expectedScenario.map((d: any) => {
                // d.month is present in backend response
                return initialValue + (this.monthlyContribution * d.month);
            });

            this.lineChartData.datasets[0].data = optimistic;
            this.lineChartData.datasets[1].data = expected;
            this.lineChartData.datasets[2].data = conservative;
            this.lineChartData.datasets[3].data = principal;

            // Stats
            const lastIndex = expected.length - 1;
            const finalValue = expected[lastIndex];
            const finalInvested = principal[lastIndex];

            this.summary = {
                projectedValue: finalValue,
                totalInvested: finalInvested,
                totalProfit: finalValue - finalInvested,
                roi: finalInvested > 0 ? ((finalValue - finalInvested) / finalInvested) * 100 : 0
            };

            if (this.chart) {
                this.chart.update();
            }
        }
    }
}
