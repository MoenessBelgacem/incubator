import { Component, ElementRef, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WebSocketService, WebRtcSignal } from '../../../core/services/websocket.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-video-meeting',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="isOpen" class="fixed inset-0 z-[100] bg-navy-900/95 flex flex-col">
      <!-- Header -->
      <div class="h-16 flex items-center justify-between px-6 border-b border-white/10 shrink-0">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-full bg-primary-600/20 text-primary-500 flex items-center justify-center">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"/></svg>
          </div>
          <div>
            <h3 class="text-white font-bold">Session de Mentorat</h3>
            <p class="text-xs text-slate-400">En direct</p>
          </div>
        </div>
        
        <button (click)="endCall()" class="p-2 bg-danger-500/10 text-danger-500 rounded-full hover:bg-danger-500 hover:text-white transition-colors">
          <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/></svg>
        </button>
      </div>

      <!-- Video Grid -->
      <div class="flex-1 p-6 flex items-center justify-center relative">
        <div class="w-full max-w-5xl aspect-video bg-black rounded-2xl overflow-hidden relative shadow-2xl border border-white/10">
          
          <!-- Remote Video (Full Screen) -->
          <video #remoteVideo autoplay playsinline class="w-full h-full object-cover"></video>
          
          <div *ngIf="!remoteStreamActive" class="absolute inset-0 flex flex-col items-center justify-center bg-slate-800">
            <div class="animate-pulse flex flex-col items-center">
              <div class="w-20 h-20 bg-slate-700 rounded-full mb-4 flex items-center justify-center">
                <svg class="w-8 h-8 text-slate-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
              </div>
              <p class="text-white font-medium">En attente du correspondant...</p>
            </div>
          </div>

          <!-- Local Video (PiP) -->
          <div class="absolute bottom-6 right-6 w-48 aspect-video bg-slate-900 rounded-xl overflow-hidden border-2 border-white shadow-lg z-10">
            <video #localVideo autoplay playsinline muted class="w-full h-full object-cover transform scale-x-[-1]"></video>
            <div *ngIf="!isCameraOn" class="absolute inset-0 flex items-center justify-center bg-slate-800">
              <svg class="w-6 h-6 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"/></svg>
            </div>
          </div>

        </div>
      </div>

      <!-- Controls -->
      <div class="h-24 flex items-center justify-center gap-6 shrink-0 bg-navy-900 border-t border-white/10">
        <!-- Mic -->
        <button (click)="toggleAudio()" [ngClass]="isMuted ? 'bg-danger-500 text-white' : 'bg-slate-700 hover:bg-slate-600 text-white'" class="w-12 h-12 rounded-full flex items-center justify-center transition-colors">
          <svg *ngIf="!isMuted" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M19 11a7 7 0 01-7 7m0 0a7 7 0 01-7-7m7 7v4m0 0H8m4 0h4m-4-8a3 3 0 01-3-3V5a3 3 0 116 0v6a3 3 0 01-3 3z"/></svg>
          <svg *ngIf="isMuted" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z"/><path stroke-linecap="round" stroke-linejoin="round" d="M17 14l2-2m0 0l2-2m-2 2l-2 2m2-2l2 2"/></svg>
        </button>

        <!-- Call -->
        <button (click)="endCall()" class="w-14 h-14 rounded-full bg-danger-500 hover:bg-danger-600 text-white flex items-center justify-center transition-colors shadow-lg shadow-danger-500/30">
          <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M16 8l2-2m0 0l2-2m-2 2l-2 2m2-2l2 2M5 11l7-7 7 7M5 19h14a2 2 0 002-2v-5a2 2 0 00-2-2H5a2 2 0 00-2 2v5a2 2 0 002 2z"/></svg>
        </button>

        <!-- Camera -->
        <button (click)="toggleVideo()" [ngClass]="!isCameraOn ? 'bg-danger-500 text-white' : 'bg-slate-700 hover:bg-slate-600 text-white'" class="w-12 h-12 rounded-full flex items-center justify-center transition-colors">
          <svg *ngIf="isCameraOn" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"/></svg>
          <svg *ngIf="!isCameraOn" class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"/></svg>
        </button>
      </div>
    </div>
  `
})
export class VideoMeetingComponent implements OnInit, OnDestroy {
  @Input() applicationId!: number;
  isOpen = false;
  isMuted = false;
  isCameraOn = true;
  remoteStreamActive = false;

  @ViewChild('localVideo') localVideo!: ElementRef<HTMLVideoElement>;
  @ViewChild('remoteVideo') remoteVideo!: ElementRef<HTMLVideoElement>;

  private peerConnection!: RTCPeerConnection;
  private localStream!: MediaStream;
  private signalingSub!: Subscription;
  private isInitiator = false;

  private configuration: RTCConfiguration = {
    iceServers: [
      { urls: 'stun:stun.l.google.com:19302' },
      { urls: 'stun:stun1.l.google.com:19302' }
    ]
  };

  constructor(private wsService: WebSocketService) {}

  ngOnInit() {}

  async startCall(isInitiator: boolean = true) {
    this.isOpen = true;
    this.isInitiator = isInitiator;
    
    setTimeout(async () => {
      try {
        this.localStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
        if (this.localVideo) this.localVideo.nativeElement.srcObject = this.localStream;

        this.initPeerConnection();

        if (this.isInitiator) {
          const offer = await this.peerConnection.createOffer();
          await this.peerConnection.setLocalDescription(offer);
          this.wsService.sendSignal({
            applicationId: this.applicationId,
            type: 'offer',
            payload: offer
          });
        }
      } catch (e) {
        console.error('Erreur accès caméra/micro', e);
        alert("Impossible d'accéder à la caméra ou au microphone.");
        this.endCall();
      }
    }, 100);

    this.signalingSub = this.wsService.getSignals().subscribe(async (signal: WebRtcSignal) => {
      if (!this.peerConnection) return;

      if (signal.type === 'offer' && !this.isInitiator) {
        await this.peerConnection.setRemoteDescription(new RTCSessionDescription(signal.payload));
        const answer = await this.peerConnection.createAnswer();
        await this.peerConnection.setLocalDescription(answer);
        this.wsService.sendSignal({
          applicationId: this.applicationId,
          type: 'answer',
          payload: answer
        });
      } else if (signal.type === 'answer' && this.isInitiator) {
        await this.peerConnection.setRemoteDescription(new RTCSessionDescription(signal.payload));
      } else if (signal.type === 'candidate') {
        await this.peerConnection.addIceCandidate(new RTCIceCandidate(signal.payload));
      }
    });
  }

  private initPeerConnection() {
    this.peerConnection = new RTCPeerConnection(this.configuration);

    this.localStream.getTracks().forEach(track => {
      this.peerConnection.addTrack(track, this.localStream);
    });

    this.peerConnection.onicecandidate = (event) => {
      if (event.candidate) {
        this.wsService.sendSignal({
          applicationId: this.applicationId,
          type: 'candidate',
          payload: event.candidate
        });
      }
    };

    this.peerConnection.ontrack = (event) => {
      if (this.remoteVideo) {
        this.remoteVideo.nativeElement.srcObject = event.streams[0];
        this.remoteStreamActive = true;
      }
    };
  }

  toggleAudio() {
    this.isMuted = !this.isMuted;
    this.localStream.getAudioTracks().forEach(t => t.enabled = !this.isMuted);
  }

  toggleVideo() {
    this.isCameraOn = !this.isCameraOn;
    this.localStream.getVideoTracks().forEach(t => t.enabled = this.isCameraOn);
  }

  endCall() {
    this.isOpen = false;
    this.remoteStreamActive = false;
    if (this.peerConnection) {
      this.peerConnection.close();
    }
    if (this.localStream) {
      this.localStream.getTracks().forEach(track => track.stop());
    }
    if (this.signalingSub) {
      this.signalingSub.unsubscribe();
    }
  }

  ngOnDestroy() {
    this.endCall();
  }
}
