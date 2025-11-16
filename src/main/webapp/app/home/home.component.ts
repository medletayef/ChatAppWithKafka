import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { first, Subject, switchMap } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import NavbarComponent from '../layouts/navbar/navbar.component';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { ChatRoomService } from '../entities/chat-room/service/chat-room.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatButtonModule } from '@angular/material/button';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ModalCreateRoomComponent } from '../entities/chat-room/modal-create-room/modal-create-room.component';
import { TrackerService } from '../core/tracker/tracker.service';
import { InvitationComponent } from '../entities/invitation/invitation.component';

@Component({
  standalone: true,
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  imports: [SharedModule, NavbarComponent, FaIconComponent, NavbarComponent, NavbarComponent, NavbarComponent],
})
export default class HomeComponent implements OnInit, OnDestroy {
  account = signal<Account | null>(null);
  trackerService = inject(TrackerService);
  usersStatus$ = this.trackerService.userStatus$;
  protected isAuthenticated = signal(false);
  private modalService = inject(NgbModal);

  private readonly destroy$ = new Subject<void>();

  private readonly accountService = inject(AccountService);
  private readonly router = inject(Router);
  private readonly chatRoomService = inject(ChatRoomService);
  private _snackBar = inject(MatSnackBar);

  ngOnInit(): void {
    this.accountService
      .getAuthenticationState()
      .pipe(takeUntil(this.destroy$))
      .subscribe(account => {
        this.account.set(account);
        this.trackerService.getUsersStatus();
        this.trackerService.watchRoomEvents().subscribe(
          roomEvent => {
            console.log('Room event = ', roomEvent);
            const modalRef = this.modalService.open(InvitationComponent);
            modalRef.componentInstance.roomEvent = roomEvent;
            modalRef.closed.subscribe(resModal => {
              if (resModal === 'accepted') {
                const invitation = {};
                this._snackBar.open('Invitation accepted to join room:' + roomEvent.roomName, 'close');
              } else if (resModal === 'rejected') {
                this._snackBar.open('Invitation rejected', 'close');
              }
            });
            this._snackBar.open(`Room event: ${roomEvent.type}`, 'close');
          },
          err => {
            // This will catch JSON parsing errors or WebSocket errors
            console.error('CRITICAL ERROR: Failed to process room event message.', err);
          },
          () => {
            console.log('Subscription completed unexpectedly.');
          },
        );
      });
  }

  login(): void {
    this.router.navigate(['/login']);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onWheelScroll(event: WheelEvent): void {
    if (event.deltaY !== 0) {
      event.preventDefault();
      const el = event.currentTarget as HTMLElement;
      el.scrollLeft += event.deltaY;
    }
  }

  chatWith(user: any): void {
    this.chatRoomService.findRelatedChatroomWith(user).subscribe(res => {
      if (res.body) {
        const rooms = res.body;
        if (rooms.length === 0) {
          const modalRef = this.modalService.open(ModalCreateRoomComponent);
          modalRef.componentInstance.user = user;
          modalRef.closed.subscribe(resModal => {
            if (resModal === 'room created') {
              this._snackBar.open('room created  and invitations sent successfully', 'close');
            }
          });
        }
      }
    });
  }
}
