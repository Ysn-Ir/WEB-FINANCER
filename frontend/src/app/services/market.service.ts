import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
    providedIn: 'root'
})
export class MarketService {
    private apiUrl = 'http://localhost:8080/api/market';

    constructor(private http: HttpClient, private authService: AuthService) { }

    search(query: string, type: string = 'ALL'): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/search?query=${query}&type=${type}`, { headers: this.authService.getAuthHeaders() });
    }

    getPrice(symbol: string, type: string = 'STOCK'): Observable<number> {
        return this.http.get<number>(`${this.apiUrl}/price/${symbol}?type=${type}`, { headers: this.authService.getAuthHeaders() });
    }

    getHistory(symbol: string, period: string = '1D', currentPrice?: number): Observable<any[]> {
        let url = `${this.apiUrl}/history/${symbol}?period=${period}`;
        if (currentPrice) {
            url += `&referencePrice=${currentPrice}`;
        }
        return this.http.get<any[]>(url, { headers: this.authService.getAuthHeaders() });
    }
}
