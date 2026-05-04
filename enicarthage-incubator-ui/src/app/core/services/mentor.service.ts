import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface IncubatedApplication {
  applicationId: number;
  candidateName: string;
  candidateEmail: string;
  projectTitle: string;
  projectDescription: string;
  status: string;
}

@Injectable({
  providedIn: 'root'
})
export class MentorService {
  private apiUrl = `${environment.apiUrl}/api/mentor`;

  constructor(private http: HttpClient) {}

  getMyIncubatedApplications(): Observable<{ message: string, data: IncubatedApplication[] }> {
    return this.http.get<{ message: string, data: IncubatedApplication[] }>(`${this.apiUrl}/applications`);
  }
}
