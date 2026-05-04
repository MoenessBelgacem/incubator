import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface LandingSection {
  id?: number;
  title: string;
  subtitle: string;
  content: string;
  imagePath: string | null;
  backgroundColor: string;
  layout: string;
  orderIndex: number;
  visible: boolean;
  createdAt?: string;
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class LandingSectionService {
  private apiUrl = `${environment.apiUrl}/api/landing/sections`;

  constructor(private http: HttpClient) {}

  getVisibleSections(): Observable<{ success: boolean, message: string, data: LandingSection[] }> {
    return this.http.get<any>(this.apiUrl);
  }

  getAllSections(): Observable<{ success: boolean, message: string, data: LandingSection[] }> {
    return this.http.get<any>(`${this.apiUrl}/all`);
  }

  getById(id: number): Observable<{ success: boolean, message: string, data: LandingSection }> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  create(section: LandingSection, image?: File): Observable<any> {
    const formData = new FormData();
    formData.append('section', new Blob([JSON.stringify(section)], { type: 'application/json' }));
    if (image) formData.append('image', image);
    return this.http.post<any>(this.apiUrl, formData);
  }

  update(id: number, section: LandingSection, image?: File): Observable<any> {
    const formData = new FormData();
    formData.append('section', new Blob([JSON.stringify(section)], { type: 'application/json' }));
    if (image) formData.append('image', image);
    return this.http.put<any>(`${this.apiUrl}/${id}`, formData);
  }

  reorder(orderedIds: number[]): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/reorder`, orderedIds);
  }

  delete(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}
