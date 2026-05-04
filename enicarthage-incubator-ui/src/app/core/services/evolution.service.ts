import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ProjectEvolution {
  id: number;
  applicationId: number;
  title: string;
  content: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class EvolutionService {
  private apiUrl = `${environment.apiUrl}/api`;

  constructor(private http: HttpClient) {}

  getEvolutions(applicationId: number): Observable<{ message: string, data: ProjectEvolution[] }> {
    return this.http.get<{ message: string, data: ProjectEvolution[] }>(`${this.apiUrl}/applications/${applicationId}/evolutions`);
  }

  addEvolution(applicationId: number, title: string, content: string): Observable<{ message: string, data: ProjectEvolution }> {
    return this.http.post<{ message: string, data: ProjectEvolution }>(`${this.apiUrl}/applications/${applicationId}/evolutions`, { title, content });
  }

  getChatHistory(applicationId: number): Observable<{ message: string, data: any[] }> {
    return this.http.get<{ message: string, data: any[] }>(`${this.apiUrl}/applications/${applicationId}/chat`);
  }
}
