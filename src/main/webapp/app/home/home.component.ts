import { Component, OnDestroy, OnInit, inject, signal, viewChild, ElementRef, ViewChild, EventEmitter, Output } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';
import NavbarComponent from '../layouts/navbar/navbar.component';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { ChatRoomService } from '../entities/chat-room/service/chat-room.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { TrackerService } from '../core/tracker/tracker.service';
import { InvitationComponent } from '../entities/invitation/invitation.component';
import { MessageComponent } from '../entities/message/list/message.component';
import { UserService } from '../entities/user/service/user.service';
import { ITEMS_PER_PAGE } from '../config/pagination.constants';
import TimestampPipe from '../shared/date/format-timestamp.pipe';
import { faPlus, faRefresh } from '@fortawesome/free-solid-svg-icons';
import { ModalCreateRoomComponent } from '../entities/chat-room/modal-create-room/modal-create-room.component';

@Component({
  standalone: true,
  selector: 'jhi-home',
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  imports: [
    TimestampPipe,
    SharedModule,
    NavbarComponent,
    FaIconComponent,
    NavbarComponent,
    NavbarComponent,
    NavbarComponent,
    MessageComponent,
  ],
})
export default class HomeComponent implements OnInit, OnDestroy {
  account = signal<Account | null>(null);
  currentAccount: Account | null = null;
  trackerService = inject(TrackerService);
  usersStatus$ = this.trackerService.userStatus$;

  member: any = null;
  chatRoom: any = null;

  size = ITEMS_PER_PAGE;
  memberLogin = '';
  listRoomsSummaray: any[] = [];
  page = 0;

  iconRefresh = faRefresh;
  iconPlus = faPlus;

  @ViewChild('msg') messageComponent: MessageComponent = new MessageComponent();

  protected isAuthenticated = signal(false);

  private modalService = inject(NgbModal);
  private readonly destroy$ = new Subject<void>();

  private readonly accountService = inject(AccountService);
  private readonly userService = inject(UserService);
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
            this._snackBar.dismiss();
            if (roomEvent.type === 'INVITATION_SENT') {
              const modalRef = this.modalService.open(InvitationComponent);
              modalRef.componentInstance.roomEvent = roomEvent;
              modalRef.closed.subscribe(resModal => {
                if (resModal === 'accepted') {
                  this._snackBar.open('Invitation accepted to join room : ' + roomEvent.roomName, 'close', { duration: 5000 });
                  this.getRoomsSummary();
                } else if (resModal === 'rejected') {
                  this._snackBar.open('Invitation rejected', 'close', { duration: 5000 });
                }
              });
            } else if (roomEvent.type === 'ROOM_JOINED') {
              this._snackBar.open(roomEvent.sender + ' has accepted invitation to join room:' + roomEvent.roomName, 'close', {
                duration: 5000,
              });
              if (this.chatRoom && roomEvent.roomId === this.chatRoom.id) {
                this.chatRoomService.find(roomEvent.roomId).subscribe(resCR => {
                  if (resCR.body?.members?.length === 2) {
                    this.getTheOnlyPartnerMember(resCR.body);
                  } else {
                    this.chatInRoom(this.chatRoom);
                  }
                });
              }
            }
          },
          err => {
            // This will catch JSON parsing errors or WebSocket errors
            console.error('CRITICAL ERROR: Failed to process room event message.', err);
          },
          () => {
            console.log('Subscription completed unexpectedly.');
          },
        );
        this.trackerService.watchMessageEvents().subscribe(() => {
          this.initializeListRoom();
        });
      });

    this.accountService.identity().subscribe(account => {
      this.currentAccount = account;
    });

    this.usersStatus$.subscribe(res => {
      if (this.chatRoom && this.chatRoom.members.length === 2) {
        let filtered: any = [];
        if (this.chatRoom.members.some((item: any) => typeof item === 'string')) {
          if (this.chatRoom.members[0] === this.currentAccount?.login) {
            filtered = res.filter(value => value.userId === this.chatRoom.members[1]);
          } else {
            filtered = res.filter(value => value.userId === this.chatRoom.members[0]);
          }
        } else {
          if (this.chatRoom.members[0].login === this.currentAccount?.login) {
            filtered = res.filter(value => value.userId === this.chatRoom.members[1].login);
          } else {
            filtered = res.filter(value => value.userId === this.chatRoom.members[0].login);
          }
        }

        if (filtered.length > 0) {
          this.member = { ...this.member, state: filtered[0].state };
        } else {
          this.member = { ...this.member, state: 'OFFLINE' };
        }
        console.log('member ', this.member);
      }
    });
    this.getRoomsSummary();
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

  chatInRoom(roomSummary: any): void {
    if (this.chatRoom && this.chatRoom.members.length === 2) {
      if (this.chatRoom.members[0].login === this.currentAccount?.login) {
        this.member = { fullName: this.chatRoom.members[1].fullName, imageUrl: this.chatRoom.members[1]?.imageUrl };
      } else {
        this.member = { fullName: this.chatRoom.members[0]?.fullName, imageUrl: this.chatRoom.members[0]?.imageUrl };
      }
    }
    this.messageComponent.chatRoomSummary = roomSummary;
    this.messageComponent.getMessages();
  }
  getTheOnlyPartnerMember(roomSummary: any): void {
    this.chatRoom = roomSummary;
    if (this.chatRoom && this.chatRoom.members.length === 2) {
      if (this.chatRoom.members[0] === this.currentAccount?.login) {
        this.accountService.getUserByLogin(this.chatRoom.members[1]).subscribe(resMember => {
          this.member = { fullName: resMember.firstName + ' ' + resMember.lastName, imageUrl: resMember.imageUrl };
        });
      } else {
        this.accountService.getUserByLogin(this.chatRoom.members[0]).subscribe(resMember => {
          this.member = { fullName: resMember.firstName + ' ' + resMember.lastName, imageUrl: resMember.imageUrl };
        });
      }
    } else {
      this.member = null;
    }
    console.log('roomSummary = ', this.chatRoom);
  }

  getRoomsSummary(): void {
    if (this.memberLogin && this.memberLogin.length > 0) {
      this.chatRoomService.findRelatedsChatroomWith(this.memberLogin, this.page, this.size).subscribe(res => {
        if (res.body) {
          const rooms = res.body;
          if (rooms.length > 0) {
            this.listRoomsSummaray = rooms;
          }
        }
      });
    } else {
      this.chatRoomService.findRelatedsChatroomWith('', this.page, this.size).subscribe(res => {
        if (res.body) {
          const rooms = res.body;
          if (rooms.length > 0) {
            this.listRoomsSummaray = rooms;
          }
        }
      });
    }
  }

  scrollAndGetRooms(): void {
    this.size += 5;
    this.getRoomsSummary();
  }

  initializeListRoom(): void {
    this.size = ITEMS_PER_PAGE;
    this.memberLogin = '';
    this.getRoomsSummary();
  }

  createRoom(): void {
    const modalRef = this.modalService.open(ModalCreateRoomComponent);
    modalRef.closed.subscribe(resModal => {
      if (resModal === 'room created') {
        this._snackBar.open('room created successfully', 'close');
        this.initializeListRoom();
      }
    });
  }
}
