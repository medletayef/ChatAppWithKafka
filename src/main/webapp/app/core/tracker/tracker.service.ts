import { Injectable, inject } from '@angular/core';
import { Location } from '@angular/common';
import { Event, NavigationEnd, Router } from '@angular/router';
import { BehaviorSubject, Observable, Observer, Subscription } from 'rxjs';
import { filter, map } from 'rxjs/operators';

import SockJS from 'sockjs-client';
import { RxStomp } from '@stomp/rx-stomp';

import { AuthServerProvider } from 'app/core/auth/auth-jwt.service';
import { AccountService } from '../auth/account.service';
import { Account } from '../auth/account.model';
import { TrackerActivity } from './tracker-activity.model';

const DESTINATION_TRACKER = '/topic/tracker';
const DESTINATION_ACTIVITY = '/topic/activity';

@Injectable({ providedIn: 'root' })
export class TrackerService {
  userStatusSubject = new BehaviorSubject<any[]>([]);
  userStatus$ = this.userStatusSubject.asObservable();

  private rxStomp?: RxStomp;
  private routerSubscription: Subscription | null = null;

  private readonly router = inject(Router);
  private readonly accountService = inject(AccountService);
  private readonly authServerProvider = inject(AuthServerProvider);
  private readonly location = inject(Location);

  setup(): void {
    this.rxStomp = new RxStomp();
    this.rxStomp.configure({
      debug: (msg: string): void => console.log(new Date(), msg),
    });
    this.accountService.getAuthenticationState().subscribe({
      next: (account: Account | null) => {
        if (account) {
          this.connect();
        } else {
          this.disconnect();
        }
      },
    });

    this.rxStomp.connected$.subscribe(() => {
      this.sendActivity();

      this.routerSubscription = this.router.events
        .pipe(filter((event: Event) => event instanceof NavigationEnd))
        .subscribe(() => this.sendActivity());
    });
  }

  get stomp(): RxStomp {
    if (!this.rxStomp) {
      throw new Error('Stomp connection not initialized');
    }
    return this.rxStomp;
  }

  public subscribe(observer: Partial<Observer<TrackerActivity>>): Subscription {
    return this.stomp
      .watch(DESTINATION_TRACKER)
      .pipe(map(imessage => JSON.parse(imessage.body)))
      .subscribe(observer);
  }

  sendActivity(): void {
    this.stomp.publish({
      destination: DESTINATION_ACTIVITY,
      body: JSON.stringify({ page: this.router.routerState.snapshot.url }),
    });
  }

  getUsersStatus(): void {
    this.stomp
      .watch('/topic/user-status')
      .pipe(
        map(message => {
          const status = JSON.parse(message.body);
          this.accountService.identity().subscribe(account => {
            this.updateStatuses(status, account);
          });
        }),
      )
      .subscribe();
    // periodic heartbeat (optional)
    setInterval(() => {
      this.onVisibilityChange();
    }, 25000);
  }

  sendStatus(status: string): void {
    this.stomp.publish({ destination: '/topic/presence', body: JSON.stringify({ state: status }) });
  }

  watchRoomEvents(): Observable<any> {
    return this.stomp.watch(`/user/queue/room-event`).pipe(map(msg => JSON.parse(msg.body)));
  }

  watchMessageEvents(): Observable<any> {
    return this.stomp.watch(`/user/queue/messages`).pipe(map(msg => JSON.parse(msg.body)));
  }

  private connect(): void {
    this.updateCredentials();
    return this.stomp.activate();
  }

  private disconnect(): Promise<void> {
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
      this.routerSubscription = null;
    }
    return this.stomp.deactivate();
  }

  private buildUrl(): string {
    // building absolute path so that websocket doesn't fail when deploying with a context path
    let url = '/websocket/tracker';
    url = this.location.prepareExternalUrl(url);
    const authToken = this.authServerProvider.getToken();
    if (authToken) {
      return `${url}?access_token=${authToken}`;
    }
    return url;
  }

  private updateCredentials(): void {
    this.stomp.configure({
      webSocketFactory: () => SockJS(this.buildUrl()),
    });
  }

  private onVisibilityChange(): void {
    if (!document.hidden) {
      this.sendStatus('ACTIVE');
    } else {
      this.sendStatus('ABSENT');
    }
  }

  private updateStatuses(status: any, currentUser: any): void {
    const current = this.userStatusSubject.value;
    let updated = [...current.filter(u => u.userId !== status.userId), status];
    updated = updated.filter(u => u.state !== 'OFFLINE' && u.userId !== currentUser.login);
    this.userStatusSubject.next(updated);
  }
}
