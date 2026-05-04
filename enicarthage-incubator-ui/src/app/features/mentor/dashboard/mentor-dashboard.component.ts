import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MentorService, IncubatedApplication } from '../../../core/services/mentor.service';

@Component({
  selector: 'app-mentor-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="space-y-8">
      <div>
        <h1 class="page-title mb-2">Espace Mentorat</h1>
        <p class="page-subtitle">Gérez vos startups incubées et suivez leurs progrès.</p>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-3 gap-6">
        <div class="stat-card">
          <div class="w-12 h-12 rounded-2xl bg-primary-50 flex items-center justify-center">
            <svg class="w-6 h-6 text-primary-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"><path stroke-linecap="round" stroke-linejoin="round" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"/></svg>
          </div>
          <div>
            <p class="text-2xl font-bold">{{ applications.length }}</p>
            <p class="text-xs text-text-muted">Projets suivis</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="w-12 h-12 rounded-2xl bg-success-50 flex items-center justify-center">
            <svg class="w-6 h-6 text-success-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
          </div>
          <div>
            <p class="text-2xl font-bold">Actif</p>
            <p class="text-xs text-text-muted">Statut Mentor</p>
          </div>
        </div>
        <div class="stat-card">
          <div class="w-12 h-12 rounded-2xl bg-accent-50 flex items-center justify-center">
            <svg class="w-6 h-6 text-accent-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"><path stroke-linecap="round" stroke-linejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
          </div>
          <div>
            <p class="text-2xl font-bold">{{ today | date:'dd MMM' }}</p>
            <p class="text-xs text-text-muted">Aujourd'hui</p>
          </div>
        </div>
      </div>

      <div class="flex items-center justify-between">
        <h2 class="text-lg font-semibold text-text-primary">Vos Startups</h2>
      </div>

      <div *ngIf="loading" class="flex justify-center py-12">
        <div class="animate-spin rounded-full h-8 w-8 border-2 border-primary-500 border-t-transparent"></div>
      </div>

      <div *ngIf="!loading && applications.length === 0" class="card p-12 text-center bg-white/50 border-dashed">
        <div class="w-16 h-16 bg-slate-100 rounded-2xl flex items-center justify-center mx-auto mb-4">
          <svg class="w-8 h-8 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"/></svg>
        </div>
        <h3 class="text-lg font-bold text-text-primary mb-2">Aucun projet assigné</h3>
        <p class="text-text-muted text-sm max-w-sm mx-auto">Vous n'avez pas encore de startups à coacher. L'administration vous notifiera dès qu'une assignation sera faite.</p>
      </div>

      <div *ngIf="!loading && applications.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div *ngFor="let app of applications" class="group card bg-white hover:border-primary-200 transition-all cursor-pointer p-6" [routerLink]="['/mentor/applications', app.applicationId]">
          <div class="flex items-start justify-between mb-4">
            <div class="w-12 h-12 rounded-xl bg-primary-50 text-primary-600 flex items-center justify-center text-xl font-bold">
              {{app.projectTitle.charAt(0)}}
            </div>
            <span class="badge badge-success">Incubé</span>
          </div>
          
          <h3 class="font-bold text-text-primary group-hover:text-primary-600 transition-colors mb-1">{{app.projectTitle}}</h3>
          <p class="text-xs text-text-muted mb-4">Porté par <span class="font-medium text-text-secondary">{{app.candidateName}}</span></p>
          
          <p class="text-sm text-text-secondary line-clamp-2 mb-6">{{app.projectDescription}}</p>
          
          <div class="flex items-center justify-between pt-4 border-t border-slate-50">
            <span class="text-xs font-semibold text-primary-600 group-hover:underline">Accéder à l'espace</span>
            <svg class="w-4 h-4 text-primary-500 group-hover:translate-x-1 transition-transform" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7"/></svg>
          </div>
        </div>
      </div>
    </div>

  `
})
export class MentorDashboardComponent implements OnInit {
  applications: IncubatedApplication[] = [];
  loading = true;
  today = new Date();

  constructor(private mentorService: MentorService) {}

  ngOnInit(): void {
    this.mentorService.getMyIncubatedApplications().subscribe({
      next: (res) => {
        this.applications = res.data || [];
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }
}
