import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  private apiUrl = 'http://localhost:8080/api/transactions';

  constructor(private http: HttpClient, private authService: AuthService) { }

  getAllTransactions(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl, { headers: this.authService.getAuthHeaders() });
  }

  createTransaction(transaction: any): Observable<any> {
    return this.http.post(this.apiUrl, transaction, { headers: this.authService.getAuthHeaders() });
  }

  updateTransaction(id: number, transaction: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, transaction, { headers: this.authService.getAuthHeaders() });
  }

  deleteTransaction(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, { headers: this.authService.getAuthHeaders() });
  }
}
