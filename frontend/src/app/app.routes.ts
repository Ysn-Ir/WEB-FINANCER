import { Routes } from '@angular/router';
import { LoginComponent } from './components/auth/login/login.component';
import { RegisterComponent } from './components/auth/register/register.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { LedgerComponent } from './components/ledger/ledger.component';
import { VaultComponent } from './components/vault/vault.component';
import { GoalsComponent } from './components/goals/goals.component';
import { ReportsComponent } from './components/reports/reports.component';

export const routes: Routes = [
    { path: '', redirectTo: '/login', pathMatch: 'full' },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'dashboard', component: DashboardComponent },
    { path: 'ledger', component: LedgerComponent },
    { path: 'vault', component: VaultComponent },
    { path: 'goals', component: GoalsComponent },
    { path: 'reports', component: ReportsComponent },
    { path: 'simulation', loadComponent: () => import('./components/forecasting/forecasting.component').then(m => m.ForecastingComponent) },
    { path: 'documents', loadComponent: () => import('./components/documents/documents.component').then(m => m.DocumentsComponent) },
    { path: 'market', loadComponent: () => import('./components/market/market.component').then(m => m.MarketComponent) }
];

// Routes configuration for the application
