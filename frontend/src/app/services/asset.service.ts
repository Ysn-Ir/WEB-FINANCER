import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AssetService {
  private apiUrl = 'http://localhost:8080/api/assets';

  constructor(private http: HttpClient, private authService: AuthService) { }

  getAllAssets(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl, { headers: this.authService.getAuthHeaders() });
  }

  createAsset(asset: any): Observable<any> {
    return this.http.post(this.apiUrl, asset, { headers: this.authService.getAuthHeaders() });
  }

  updateAsset(id: number, asset: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, asset, { headers: this.authService.getAuthHeaders() });
  }

  deleteAsset(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, { headers: this.authService.getAuthHeaders() });
  }

  refreshPrices(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/refresh`, {}, { headers: this.authService.getAuthHeaders() });
  }

  // Placeholder for real-time market data
  getMarketPrice(symbol: string): number {
    // In a real app, this would call an external API like Finnhub or AlphaVantage
    // Mocking random fluctuation for "Live" feel
    const basePrices: { [key: string]: number } = {
      'AAPL': 185.50,
      'BTC': 43500.00,
      'ETH': 2250.00,
      'TSLA': 240.00
    };
    return basePrices[symbol] || 100;
  }
}
