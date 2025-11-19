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
} from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { ActivatedRoute, Data, ParamMap, Router, RouterModule } from '@angular/router';
import { Observable, Subscription, combineLatest, filter, tap } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import {
  faArrowRight,
  faCamera,
  faCommentDots,
  faFile,
  faFill,
  faFillDrip,
  faHandDots,
  faListDots,
  faPhone,
  faSearch,
  faVideoCamera,
} from '@fortawesome/free-solid-svg-icons';
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
import { IMessage } from '../message.model';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TrackerService } from '../../../core/tracker/tracker.service';
import { MatIconModule } from '@angular/material/icon';
import dayjs from 'dayjs';
import { AccountService } from '../../../core/auth/account.service';
import { Account } from '../../../core/auth/account.model';
import { MatButtonModule } from '@angular/material/button';
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
  ],
  schemas: [NO_ERRORS_SCHEMA],
  styleUrl: './message.component.scss',
})
export class MessageComponent implements OnInit {
  @Input()
  public chatRoom: any = null;
  @Input()
  public oneMember: any = null;
  iconSearch = faSearch;
  iconPhone = faPhone;
  iconCamera = faVideoCamera;
  iconArrow = faArrowRight;
  iconFile = faFile;
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

  account: Account | null = null;

  public readonly router = inject(Router);
  protected readonly messageService = inject(MessageService);
  protected readonly accountService = inject(AccountService);
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly sortService = inject(SortService);
  protected parseLinks = inject(ParseLinks);
  protected modalService = inject(NgbModal);
  protected ngZone = inject(NgZone);

  trackId = (item: IMessage): number => this.messageService.getMessageIdentifier(item);

  ngOnInit(): void {
    // this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
    //   .pipe(
    //     tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
    //     tap(() => this.reset()),
    //     tap(() => this.load()),
    //   )
    //   .subscribe();

    this.accountService.identity().subscribe(res => {
      this.account = res;
    });
    this.messages = [
      {
        id: 11,
        content: 'hello',
        sentAt: dayjs().year(2025).month(10).date(19).hour(14).minute(38),
        room: { id: 1 },
        sender: { login: 'admin', fullName: 'mohamed letaief', imageUrl: '' },
      },
      {
        id: 12,
        content: 'how are you ?',
        sentAt: dayjs().year(2025).month(10).date(19).hour(14).minute(39),
        room: { id: 1 },
        sender: {
          login: 'user',
          fullName: 'foulen lefleni',
          imageUrl: 'https://res.cloudinary.com/dzswzlj6e/image/upload/v1762477265/jhipster_family_member_1_head-256_bjdiov.png',
        },
      },
      {
        id: 11,
        content: 'hello',
        sentAt: dayjs().year(2025).month(10).date(19).hour(14).minute(38),
        room: { id: 1 },
        sender: { login: 'admin', fullName: 'mohamed letaief', imageUrl: '' },
      },
      {
        id: 12,
        content: 'how are you ?',
        sentAt: dayjs().year(2025).month(10).date(19).hour(14).minute(39),
        room: { id: 1 },
        sender: {
          login: 'user',
          fullName: 'foulen lefleni',
          imageUrl: 'https://res.cloudinary.com/dzswzlj6e/image/upload/v1762477265/jhipster_family_member_1_head-256_bjdiov.png',
        },
      },
      {
        id: 11,
        content: 'hello',
        sentAt: dayjs().year(2025).month(10).date(19).hour(14).minute(38),
        room: { id: 1 },
        sender: { login: 'admin', fullName: 'mohamed letaief', imageUrl: '' },
      },
      {
        id: 12,
        content: 'how are you ?',
        sentAt: dayjs().year(2025).month(10).date(19).hour(14).minute(39),
        room: { id: 1 },
        sender: {
          login: 'user',
          fullName: 'foulen lefleni',
          imageUrl: 'https://res.cloudinary.com/dzswzlj6e/image/upload/v1762477265/jhipster_family_member_1_head-256_bjdiov.png',
        },
      },
      {
        id: 11,
        content: 'hello',
        sentAt: dayjs().year(2025).month(10).date(19).hour(14).minute(38),
        room: { id: 1 },
        sender: { login: 'admin', fullName: 'mohamed letaief', imageUrl: '' },
      },
      {
        id: 12,
        content: 'how are you ?',
        sentAt: dayjs().year(2025).month(10).date(19).hour(14).minute(39),
        room: { id: 1 },
        sender: {
          login: 'user',
          fullName: 'foulen lefleni',
          imageUrl: 'https://res.cloudinary.com/dzswzlj6e/image/upload/v1762477265/jhipster_family_member_1_head-256_bjdiov.png',
        },
      },
      {
        id: 11,
        content: 'hello',
        sentAt: dayjs().year(2025).month(10).date(19).hour(14).minute(38),
        room: { id: 1 },
        sender: { login: 'admin', fullName: 'mohamed letaief', imageUrl: '' },
      },
      {
        id: 12,
        content: 'how are you ?',
        sentAt: dayjs().year(2025).month(10).date(19).hour(14).minute(39),
        room: { id: 1 },
        sender: {
          login: 'user',
          fullName: 'foulen lefleni',
          imageUrl: 'https://res.cloudinary.com/dzswzlj6e/image/upload/v1762477265/jhipster_family_member_1_head-256_bjdiov.png',
        },
      },
    ];

    //
    // this.messageService.getMessagesByRoomId(this.chatRoom.id,0,ITEMS_PER_PAGE).subscribe(
    //   (res)=>{
    //     this.messages = res.body as IMessage[];
    //   }
    // );
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
}
