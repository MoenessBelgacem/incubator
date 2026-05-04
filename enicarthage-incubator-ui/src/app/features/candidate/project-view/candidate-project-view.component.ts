import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { EvolutionService, ProjectEvolution } from '../../../core/services/evolution.service';
import { WebSocketService, ChatMessage } from '../../../core/services/websocket.service';
import { AuthService } from '../../../core/services/auth.service';
import { FormsModule } from '@angular/forms';
import { VideoMeetingComponent } from '../../shared/video-meeting/video-meeting.component';

@Component({
  selector: 'app-candidate-project-view',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, VideoMeetingComponent],
  template: `
    <div class="space-y-6">
      <!-- Header -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div class="flex items-center gap-4">
          <a routerLink="/candidate/applications" class="p-2 bg-white rounded-xl border border-slate-200 text-slate-500 hover:text-primary-600 hover:border-primary-200 transition-all shadow-sm">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18"/></svg>
          </a>
          <div>
            <h1 class="page-title mb-1">Mon Espace Mentoring</h1>
            <p class="page-subtitle">Ajoutez des mises à jour et communiquez avec votre mentor.</p>
          </div>
        </div>
        
        <div class="flex items-center gap-3">
          <button (click)="startMeet()" class="btn-primary flex items-center gap-2">
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"/></svg>
            Rejoindre le Meet
          </button>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">
        <!-- Main Content: Evolution & Chat -->
        <div class="lg:col-span-8 space-y-8">
          
          <!-- Carnet de Bord -->
          <div class="card bg-white p-6">
            <div class="flex items-center justify-between mb-6">
              <h2 class="text-lg font-bold text-text-primary flex items-center gap-2">
                <svg class="w-5 h-5 text-primary-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"/></svg>
                Carnet de Bord
              </h2>
              <button (click)="showEvolutionForm = !showEvolutionForm" class="btn-primary btn-sm flex items-center gap-1">
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4"/></svg>
                Ajouter une évolution
              </button>
            </div>

            <!-- Evolution Form -->
            <div *ngIf="showEvolutionForm" class="mb-8 bg-slate-50 p-4 rounded-2xl border border-slate-200">
              <h3 class="font-bold text-text-primary mb-4 text-sm">Nouvelle mise à jour</h3>
              <div class="space-y-4">
                <div>
                  <input [(ngModel)]="newEvolution.title" type="text" placeholder="Titre de l'évolution (ex: Finalisation de l'API)" class="input-field w-full bg-white">
                </div>
                <div>
                  <textarea [(ngModel)]="newEvolution.content" rows="3" placeholder="Détails de ce qui a été accompli..." class="input-field w-full bg-white"></textarea>
                </div>
                <div class="flex justify-end gap-2">
                  <button (click)="showEvolutionForm = false" class="btn-ghost btn-sm">Annuler</button>
                  <button (click)="submitEvolution()" [disabled]="!newEvolution.title || !newEvolution.content" class="btn-primary btn-sm">Publier</button>
                </div>
              </div>
            </div>
            
            <div *ngIf="loadingEvolutions" class="flex justify-center py-8">
              <div class="animate-spin rounded-full h-8 w-8 border-2 border-primary-500 border-t-transparent"></div>
            </div>

            <div *ngIf="!loadingEvolutions && evolutions.length === 0" class="text-center py-12 bg-slate-50 rounded-2xl border-2 border-dashed border-slate-200">
              <p class="text-text-muted text-sm italic">Vous n'avez pas encore ajouté de mise à jour.</p>
            </div>

            <div *ngIf="!loadingEvolutions && evolutions.length > 0" class="space-y-6">
              <div *ngFor="let ev of evolutions" class="group relative pl-6 border-l-2 border-slate-100 hover:border-primary-300 transition-colors">
                <div class="absolute -left-[9px] top-0 w-4 h-4 rounded-full bg-white border-2 border-slate-200 group-hover:border-primary-400 group-hover:bg-primary-50 transition-all"></div>
                <div class="flex justify-between items-start mb-2">
                  <h3 class="font-bold text-text-primary group-hover:text-primary-700">{{ev.title}}</h3>
                  <span class="text-[10px] font-medium text-text-muted bg-slate-100 px-2 py-0.5 rounded-full">{{ev.createdAt | date:'dd MMM yyyy, HH:mm'}}</span>
                </div>
                <p class="text-sm text-text-secondary whitespace-pre-wrap bg-slate-50/50 p-4 rounded-xl border border-slate-100/50">{{ev.content}}</p>
              </div>
            </div>
          </div>

          <!-- Chat -->
          <div class="card bg-white flex flex-col h-[600px] overflow-hidden">
            <div class="p-4 border-b border-slate-100 bg-slate-50/30 flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-full bg-primary-100 text-primary-700 flex items-center justify-center font-bold">💬</div>
                <div>
                  <h2 class="text-sm font-bold text-text-primary">Discussion avec le Mentor</h2>
                  <p class="text-[10px] text-success-600 flex items-center gap-1">
                    <span class="w-1.5 h-1.5 rounded-full bg-success-500 animate-pulse"></span>
                    Canal sécurisé
                  </p>
                </div>
              </div>
            </div>
            
            <div class="flex-1 p-6 overflow-y-auto bg-slate-50/50 space-y-4 flex flex-col">
              <div *ngFor="let msg of chatHistory" 
                   [ngClass]="msg.senderId === currentUserId ? 'self-end' : 'self-start'"
                   class="max-w-[85%] flex flex-col gap-1">
                <div [ngClass]="msg.senderId === currentUserId ? 'items-end' : 'items-start'" class="flex flex-col">
                  <div *ngIf="msg.senderId !== currentUserId" class="text-[10px] font-bold text-text-muted ml-2 mb-1">{{msg.senderName}}</div>
                  <div [ngClass]="msg.senderId === currentUserId ? 'bg-primary-600 text-white rounded-2xl rounded-tr-none shadow-blue-100' : 'bg-white text-text-primary rounded-2xl rounded-tl-none border border-slate-200 shadow-slate-100'"
                       class="px-4 py-2.5 shadow-sm text-sm whitespace-pre-wrap">
                    {{msg.content}}
                  </div>
                  <div class="text-[9px] text-text-muted mt-1 px-1">{{msg.timestamp | date:'HH:mm'}}</div>
                </div>
              </div>
            </div>

            <div class="p-4 bg-white border-t border-slate-100">
              <div class="flex items-center gap-2 bg-slate-50 p-2 rounded-2xl border border-slate-200 focus-within:border-primary-400 focus-within:ring-2 focus-within:ring-primary-50 transition-all">
                <input [(ngModel)]="newMessage" (keyup.enter)="sendMessage()" type="text" placeholder="Écrivez un message..." class="bg-transparent border-none focus:ring-0 flex-1 text-sm px-2">
                <button (click)="sendMessage()" [disabled]="!newMessage.trim()" class="bg-primary-600 text-white p-2 rounded-xl hover:bg-primary-700 disabled:opacity-50 transition-colors shadow-lg shadow-primary-100">
                  <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"/></svg>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Sidebar Actions -->
        <div class="lg:col-span-4 space-y-6">
          <div class="card bg-gradient-to-br from-primary-600 to-primary-700 p-6 text-white overflow-hidden relative">
            <div class="absolute -right-4 -top-4 w-24 h-24 bg-white/10 rounded-full blur-2xl"></div>
            <div class="relative z-10">
              <div class="w-12 h-12 bg-white/20 backdrop-blur-md text-white rounded-2xl flex items-center justify-center mb-4">
                <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"/></svg>
              </div>
              <h3 class="text-lg font-bold mb-2 font-display">Visioconférence</h3>
              <p class="text-primary-100 text-xs mb-6 leading-relaxed">Rejoignez l'appel vidéo avec votre mentor pour faire le point sur l'avancement de votre projet.</p>
              <button (click)="startMeet()" class="w-full py-3 bg-white text-primary-700 font-bold rounded-xl hover:bg-primary-50 transition-colors shadow-xl text-sm">
                Rejoindre l'appel vidéo
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Video Meeting Overlay -->
      <app-video-meeting #videoMeet [applicationId]="applicationId"></app-video-meeting>
    </div>
  `
})
export class CandidateProjectViewComponent implements OnInit, OnDestroy {
  applicationId!: number;
  evolutions: ProjectEvolution[] = [];
  loadingEvolutions = true;
  showEvolutionForm = false;
  newEvolution = { title: '', content: '' };
  
  chatHistory: ChatMessage[] = [];
  newMessage = '';
  currentUserId!: number;

  @ViewChild('videoMeet') videoMeet!: VideoMeetingComponent;

  constructor(
    private route: ActivatedRoute,
    private evolutionService: EvolutionService,
    private wsService: WebSocketService,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUserId = this.auth.currentUser()?.userId || 0;
    this.route.paramMap.subscribe(params => {
      this.applicationId = Number(params.get('id'));
      this.loadEvolutions();
      this.loadChatHistory();
      
      // Init WebSocket
      this.wsService.joinRoom(this.applicationId);
      this.wsService.getChatMessages().subscribe(msg => {
        this.chatHistory.push(msg);
      });
    });
  }

  ngOnDestroy(): void {
    this.wsService.disconnect();
  }

  loadEvolutions() {
    this.loadingEvolutions = true;
    this.evolutionService.getEvolutions(this.applicationId).subscribe({
      next: (res) => {
        this.evolutions = res.data || [];
        this.loadingEvolutions = false;
      },
      error: () => this.loadingEvolutions = false
    });
  }

  submitEvolution() {
    this.evolutionService.addEvolution(this.applicationId, this.newEvolution.title, this.newEvolution.content).subscribe({
      next: () => {
        this.newEvolution = { title: '', content: '' };
        this.showEvolutionForm = false;
        this.loadEvolutions();
      },
      error: () => {
        alert("Erreur lors de l'ajout de l'évolution.");
      }
    });
  }

  loadChatHistory() {
    this.evolutionService.getChatHistory(this.applicationId).subscribe(res => {
      this.chatHistory = res.data || [];
    });
  }

  sendMessage() {
    if (!this.newMessage.trim()) return;
    
    const msg: ChatMessage = {
      applicationId: this.applicationId,
      content: this.newMessage
    };
    
    this.wsService.sendChatMessage(msg);
    this.newMessage = '';
  }

  startMeet() {
    this.videoMeet.startCall(false); // Candidate joins the call
  }
}
