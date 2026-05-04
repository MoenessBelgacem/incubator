import { Injectable } from '@angular/core';
import { Client, Message, StompSubscription } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

export interface ChatMessage {
  id?: number;
  applicationId: number;
  senderId?: number;
  senderName?: string;
  senderRole?: string;
  content: string;
  timestamp?: string;
}

export interface WebRtcSignal {
  applicationId: number;
  type: 'offer' | 'answer' | 'candidate';
  payload: any;
}

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client;
  private connected$ = new BehaviorSubject<boolean>(false);
  private chatSubject = new Subject<ChatMessage>();
  private signalingSubject = new Subject<WebRtcSignal>();
  private currentApplicationId: number | null = null;
  private chatSub?: StompSubscription;
  private sigSub?: StompSubscription;

  constructor(private authService: AuthService) {
    this.client = new Client({
      webSocketFactory: () => {
        const wsUrl = `${environment.apiUrl}/ws`;
        const SockJSConstructor = (SockJS as any).default || SockJS;
        return new SockJSConstructor(wsUrl);
      },
      connectHeaders: {
        Authorization: `Bearer ${this.authService.getToken()}`
      },
      debug: (msg: string) => console.log('STOMP: ', msg),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });

    this.client.onConnect = (frame) => {
      console.log('STOMP Connected: ', frame);
      this.connected$.next(true);
      if (this.currentApplicationId) {
        this.subscribeToRoom(this.currentApplicationId);
      }
    };

    this.client.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };

    this.client.onDisconnect = () => {
      this.connected$.next(false);
    };
  }

  connect() {
    if (!this.client.active) {
      this.client.connectHeaders = { Authorization: `Bearer ${this.authService.getToken()}` };
      this.client.activate();
    }
  }

  disconnect() {
    if (this.client.active) {
      this.client.deactivate();
    }
  }

  joinRoom(applicationId: number) {
    this.currentApplicationId = applicationId;
    if (this.connected$.value) {
      this.subscribeToRoom(applicationId);
    } else {
      this.connect();
    }
  }

  private subscribeToRoom(appId: number) {
    if (this.chatSub) this.chatSub.unsubscribe();
    if (this.sigSub) this.sigSub.unsubscribe();

    this.chatSub = this.client.subscribe(`/topic/chat/${appId}`, (message: Message) => {
      this.chatSubject.next(JSON.parse(message.body));
    });

    this.sigSub = this.client.subscribe(`/topic/signaling/${appId}`, (message: Message) => {
      this.signalingSubject.next(JSON.parse(message.body));
    });
  }

  sendChatMessage(message: ChatMessage) {
    if (this.client.active) {
      this.client.publish({
        destination: '/app/chat.send',
        body: JSON.stringify(message)
      });
    }
  }

  sendSignal(signal: WebRtcSignal) {
    if (this.client.active) {
      this.client.publish({
        destination: '/app/signaling',
        body: JSON.stringify(signal)
      });
    }
  }

  getChatMessages(): Observable<ChatMessage> {
    return this.chatSubject.asObservable();
  }

  getSignals(): Observable<WebRtcSignal> {
    return this.signalingSubject.asObservable();
  }
}
