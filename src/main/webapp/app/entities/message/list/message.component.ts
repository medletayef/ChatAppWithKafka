import {
  Component,
  NgZone,
  OnInit,
  WritableSignal,
  computed,
  inject,
  signal,
  Input,
  ChangeDetectionStrategy,
  NO_ERRORS_SCHEMA,
  Output,
  EventEmitter,
  viewChild,
  ElementRef,
  ViewChild,
  Renderer2,
  AfterViewInit,
} from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { ActivatedRoute, Data, ParamMap, Router, RouterModule } from '@angular/router';
import { Observable, Subscription, combineLatest, filter, tap } from 'rxjs';
import { NgbDropdownModule, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { faArrowRight, faFile, faListAlt, faPhone, faSearch, faUsers, faVideoCamera } from '@fortawesome/free-solid-svg-icons';
import SharedModule from 'app/shared/shared.module';
import { SortByDirective, SortDirective, SortService, type SortState, sortStateSignal } from 'app/shared/sort';
import { DurationPipe, FormatMediumDatePipe, FormatMediumDatetimePipe } from 'app/shared/date';
import { FormsModule } from '@angular/forms';

import { ITEMS_PER_PAGE } from 'app/config/pagination.constants';
import { DEFAULT_SORT_DATA, ITEM_DELETED_EVENT, SORT } from 'app/config/navigation.constants';
import { ParseLinks } from 'app/core/util/parse-links.service';
import { InfiniteScrollDirective } from 'ngx-infinite-scroll';
import { EntityArrayResponseType, MessageService } from '../service/message.service';
import { MessageDeleteDialogComponent } from '../delete/message-delete-dialog.component';
import { IMessage, NewMessage } from '../message.model';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TrackerService } from '../../../core/tracker/tracker.service';
import { MatIconModule } from '@angular/material/icon';
import dayjs from 'dayjs';
import { AccountService } from '../../../core/auth/account.service';
import { Account } from '../../../core/auth/account.model';
import { MatButtonModule } from '@angular/material/button';
import { InvitationComponent } from '../../invitation/invitation.component';
import { MatSnackBar } from '@angular/material/snack-bar';
import TimestampPipe from '../../../shared/date/format-timestamp.pipe';
import { ChatRoomService } from '../../chat-room/service/chat-room.service';
import { ContentScrollDirective } from '../../../shared/ScrollUpDirective';
import { MutingComponent } from '../muting/muting.component';
import { animate, style, transition, trigger } from '@angular/animations';
@Component({
  standalone: true,
  selector: 'jhi-message',
  templateUrl: './message.component.html',
  imports: [
    RouterModule,
    FormsModule,
    SharedModule,
    MatButtonModule,
    SortDirective,
    SortByDirective,
    DurationPipe,
    FormatMediumDatetimePipe,
    FormatMediumDatePipe,
    InfiniteScrollDirective,
    FontAwesomeModule,
    MatIconModule,
    NgbDropdownModule,
    TimestampPipe,
    ContentScrollDirective,
  ],
  schemas: [NO_ERRORS_SCHEMA],
  styleUrl: './message.component.scss',
  animations: [
    trigger('slideRightToLeft', [
      transition(':enter', [style({ transform: 'translateX(100%)' }), animate('0.3s ease-out', style({ transform: 'translateX(0)' }))]),
    ]),
  ],
})
export class MessageComponent implements OnInit {
  @Input()
  public chatRoomSummary: any = null;
  @Input()
  public oneMember: any = null;
  @Output() onReceiveMessage = new EventEmitter<boolean>();
  @Output() onLeaveRoom = new EventEmitter<boolean>();
  @Output() onDeleteRoom = new EventEmitter<boolean>();
  iconSearch = faSearch;
  iconPhone = faPhone;
  iconCamera = faVideoCamera;
  iconArrow = faArrowRight;
  iconFile = faFile;
  iconListAlt = faListAlt;
  iconsUsers = faUsers;
  subscription: Subscription | null = null;
  isLoading = false;
  trackerService = inject(TrackerService);
  usersStatus$ = this.trackerService.userStatus$;
  sortState = sortStateSignal({});
  messages: any[] = [];
  itemsPerPage = ITEMS_PER_PAGE;
  links: WritableSignal<Record<string, undefined | Record<string, string | undefined>>> = signal({});
  hasMorePage = computed(() => !!this.links().next);
  isFirstFetch = computed(() => Object.keys(this.links()).length === 0);
  message: string = '';
  searchedMessage = '';
  searchMessageView = false;
  account: Account | null = null;
  size = 5;
  renderer = inject(Renderer2);
  @ViewChild('msgBodyElement') msgBodyElement!: ElementRef;

  public readonly router = inject(Router);
  protected readonly messageService = inject(MessageService);
  protected readonly accountService = inject(AccountService);
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly sortService = inject(SortService);
  protected readonly chatRoomService = inject(ChatRoomService);
  protected parseLinks = inject(ParseLinks);
  protected modalService = inject(NgbModal);
  protected ngZone = inject(NgZone);
  private _snackBar = inject(MatSnackBar);
  trackId = (item: IMessage): number => this.messageService.getMessageIdentifier(item);

  ngOnInit(): void {
    // this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
    //   .pipe(
    //     tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
    //     tap(() => this.reset()),
    //     tap(() => this.load()),
    //   )
    //   .subscribe();

    //   console.log('chatRoomSummary', this.chatRoomSummary);
    this.accountService.identity().subscribe(res => {
      this.account = res;
    });

    this.getMessages();
    this.trackerService.watchMessageEvents().subscribe(msg => {
      if (msg && msg.room.id === this.chatRoomSummary.id && msg.sender.login !== this.account?.login) {
        this.messages.push(msg);
      }
    });
  }

  reset(): void {
    this.messages = [];
  }

  loadNextPage(): void {
    this.load();
  }

  delete(message: IMessage): void {
    const modalRef = this.modalService.open(MessageDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
    modalRef.componentInstance.message = message;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed
      .pipe(
        filter(reason => reason === ITEM_DELETED_EVENT),
        tap(() => this.load()),
      )
      .subscribe();
  }

  load(): void {
    this.queryBackend().subscribe({
      next: (res: EntityArrayResponseType) => {
        this.onResponseSuccess(res);
      },
    });
  }

  public getMessages(): void {
    //  console.log('get messages');
    if (this.chatRoomSummary) {
      this.messageService.getMessagesByRoomIdAndContent(this.chatRoomSummary.id, this.searchedMessage, 0, this.size).subscribe(res => {
        this.messages = res.body as IMessage[];
        this.messages = this.messages.reverse();
      });
    }
  }

  navigateToWithComponentValues(event: SortState): void {
    this.handleNavigation(event);
  }

  protected fillComponentAttributeFromRoute(params: ParamMap, data: Data): void {
    this.sortState.set(this.sortService.parseSortParam(params.get(SORT) ?? data[DEFAULT_SORT_DATA]));
  }

  protected onResponseSuccess(response: EntityArrayResponseType): void {
    this.fillComponentAttributesFromResponseHeader(response.headers);
    const dataFromBody = this.fillComponentAttributesFromResponseBody(response.body);
    this.messages = dataFromBody;
  }

  protected fillComponentAttributesFromResponseBody(data: IMessage[] | null): IMessage[] {
    // If there is previous link, data is a infinite scroll pagination content.
    if (this.links().prev) {
      const messagesNew = this.messages ?? [];
      if (data) {
        for (const d of data) {
          if (messagesNew.some(op => op.id === d.id)) {
            messagesNew.push(d);
          }
        }
      }
      return messagesNew;
    }
    return data ?? [];
  }

  protected fillComponentAttributesFromResponseHeader(headers: HttpHeaders): void {
    const linkHeader = headers.get('link');
    if (linkHeader) {
      this.links.set(this.parseLinks.parseAll(linkHeader));
    } else {
      this.links.set({});
    }
  }

  protected queryBackend(): Observable<EntityArrayResponseType> {
    this.isLoading = true;
    const queryObject: any = {
      size: this.itemsPerPage,
      eagerload: true,
    };
    if (this.hasMorePage()) {
      Object.assign(queryObject, this.links().next);
    } else if (this.isFirstFetch()) {
      Object.assign(queryObject, { sort: this.sortService.buildSortParam(this.sortState()) });
    }

    return this.messageService.query(queryObject).pipe(tap(() => (this.isLoading = false)));
  }

  protected handleNavigation(sortState: SortState): void {
    this.links.set({});

    const queryParamsObj = {
      sort: this.sortService.buildSortParam(sortState),
    };

    this.ngZone.run(() => {
      this.router.navigate(['./'], {
        relativeTo: this.activatedRoute,
        queryParams: queryParamsObj,
      });
    });
  }

  protected evaluateDate(date: any): string {
    if (date.isSame(dayjs(), 'day')) {
      return date.format('HH:mm');
    } else {
      return date.format('DD-MM-YYYY HH:mm');
    }
  }

  protected inviteMembers(): void {
    if (!this.modalService.hasOpenModals()) {
      let modalRef: any = null;
      const usersStatuses = this.usersStatus$.subscribe(res => {
        const membersToInvite = res.filter(user => !this.chatRoomSummary.members.map((m: any) => m.login).includes(user.userId));

        if (membersToInvite && membersToInvite.length > 0) {
          modalRef = this.modalService.open(InvitationComponent);
          if (modalRef && modalRef.componentInstance) {
            modalRef.componentInstance.roomEvent = this.chatRoomSummary;
            modalRef.componentInstance.sendInvitationsToMembers = true;
          }
        } else if (!modalRef) {
          this._snackBar.open('there is no looged user outside this room', 'close', { duration: 5000 });
        }
      });
      if (modalRef) {
        modalRef.closed.subscribe((resModal: any) => {
          if (resModal === 'invitations sent') {
            this._snackBar.open('invitations sent to join room ' + this.chatRoomSummary.name, 'close', { duration: 5000 });
          }
        });
      }
      usersStatuses.unsubscribe();
    }
  }

  protected getOldMessages(): void {
    this.size += 5;
    this.getMessages();
  }

  protected sendMessage(): void {
    if (this.message.length > 0) {
      const chatRoom = { id: this.chatRoomSummary.id };
      const message = { content: this.message, sender: this.account, room: chatRoom, sentAt: new Date() } as unknown as NewMessage;
      this.messageService.create(message).subscribe(resMSG => {
        this.message = '';
        //     console.log('message sent ', resMSG);
        this.onReceiveMessage.emit(true);
        this.messages.push(resMSG.body);
        setTimeout(() => this.scrollToBottom(), 0);
      });
    }
  }

  protected onEnter(event: any): void {
    event.preventDefault();
    this.sendMessage();
  }

  protected onScrollUp(): void {
    const element = this.msgBodyElement.nativeElement;
    if (element.scrollTop === 0) {
      this.getOldMessages();
    }
  }

  protected scrollToBottom(): void {
    const element = this.msgBodyElement.nativeElement;
    element.scroll({ top: element.scrollHeight, behavior: 'smooth' });
  }

  protected leaveRoom(): void {
    this.chatRoomService.leaveRoom(this.chatRoomSummary.id).subscribe(
      () => {
        console.log('leave room');
      },
      () => {
        console.log('error leave room');
      },
      () => {
        this.onLeaveRoom.emit(true);
      },
    );
  }

  protected deleteRoom(): void {
    this.chatRoomService.delete(this.chatRoomSummary.id).subscribe(() => {
      this.onDeleteRoom.emit(true);
      this._snackBar.open('room deleted successfully', 'close', { duration: 5000 });
    });
  }

  protected muting(): void {
    const modalRef = this.modalService.open(MutingComponent, { size: 'sm', backdrop: 'static' });
    modalRef.componentInstance.chatRoomSummary = this.chatRoomSummary;
  }

  protected searchMessages(): void {
    this.getMessages();
  }
}
