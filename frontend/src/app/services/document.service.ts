import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({
    providedIn: 'root'
})
export class DocumentService {
    private baseUrl = 'http://localhost:8080/api/documents';

    constructor(private http: HttpClient, private authService: AuthService) { }

    upload(file: File): Observable<HttpEvent<any>> {
        const formData: FormData = new FormData();
        formData.append('file', file);

        const headers = this.authService.getAuthHeaders().delete('Content-Type');

        const req = new HttpRequest('POST', `${this.baseUrl}/upload`, formData, {
            reportProgress: true,
            responseType: 'json',
            headers: headers
        });

        return this.http.request(req);
    }

    importTransactions(file: File): Observable<any> {
        const formData: FormData = new FormData();
        formData.append('file', file);
        // Remove Content-Type so browser sets it to multipart/form-data with boundary
        const headers = this.authService.getAuthHeaders().delete('Content-Type');

        return this.http.post(`${this.baseUrl}/import/transactions`, formData, {
            headers: headers,
            responseType: 'text' // Backend returns string message
        });
    }

    importAssets(file: File): Observable<any> {
        const formData: FormData = new FormData();
        formData.append('file', file);
        const headers = this.authService.getAuthHeaders().delete('Content-Type');

        return this.http.post(`${this.baseUrl}/import/assets`, formData, {
            headers: headers,
            responseType: 'text'
        });
    }

    delete(id: number): Observable<any> {
        return this.http.delete(`${this.baseUrl}/${id}`, {
            headers: this.authService.getAuthHeaders()
        });
    }

    download(url: string): Observable<Blob> {
        return this.http.get(url, {
            headers: this.authService.getAuthHeaders(),
            responseType: 'blob'
        });
    }

    getFiles(): Observable<any> {
        return this.http.get(this.baseUrl, { headers: this.authService.getAuthHeaders() });
    }
}
