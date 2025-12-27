import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) { }

  register(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, user);
  }

  login(credentials: any): Observable<any> {
    // Store credentials temporarily for the session
    const token = btoa(`${credentials.username}:${credentials.password}`);
    localStorage.setItem('auth_token', token);
    localStorage.setItem('is_logged_in', 'true');
    return this.http.post(`${this.apiUrl}/login`, credentials);
  }

  logout() {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('is_logged_in');
  }

  isLoggedIn(): boolean {
    return localStorage.getItem('is_logged_in') === 'true';
  }

  getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('auth_token');
    if (token) {
      return new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': 'Basic ' + token
      });
    }
    return new HttpHeaders({ 'Content-Type': 'application/json' });
  }
}
