import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class GoalService {
  private apiUrl = 'http://localhost:8080/api/goals';

  constructor(private http: HttpClient, private authService: AuthService) { }

  getAllGoals(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl, { headers: this.authService.getAuthHeaders() });
  }

  createGoal(goal: any): Observable<any> {
    return this.http.post(this.apiUrl, goal, { headers: this.authService.getAuthHeaders() });
  }

  updateGoal(id: number, goal: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, goal, { headers: this.authService.getAuthHeaders() });
  }

  deleteGoal(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`, { headers: this.authService.getAuthHeaders() });
  }
}
