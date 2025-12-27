import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
    providedIn: 'root'
})
export class ForecastingService {
    private apiUrl = 'http://localhost:8080/api/forecast';

    constructor(private http: HttpClient, private authService: AuthService) { }

    getForecast(years: number = 10, annualReturnRate: number = 7.0, monthlyContribution?: number): Observable<any> {
        let params = new HttpParams()
            .set('years', years)
            .set('annualReturnRate', annualReturnRate);

        if (monthlyContribution != null) {
            params = params.set('monthlyContribution', monthlyContribution);
        }

        return this.http.get<any>(this.apiUrl, {
            headers: this.authService.getAuthHeaders(),
            params: params
        });
    }
}
