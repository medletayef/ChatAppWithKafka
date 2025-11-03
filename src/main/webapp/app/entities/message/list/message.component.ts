import { Component, NgZone, OnInit, WritableSignal, computed, inject, signal } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { ActivatedRoute, Data, ParamMap, Router, RouterModule } from '@angular/router';
import { Observable, Subscription, combineLatest, filter, tap } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

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
import { FaIconComponent } from '@fortawesome/angular-fontawesome';

@Component({
  standalone: true,
  selector: 'jhi-message',
  templateUrl: './message.component.html',
  imports: [
    RouterModule,
    FormsModule,
    SharedModule,
    SortDirective,
    SortByDirective,
    DurationPipe,
    FormatMediumDatetimePipe,
    FormatMediumDatePipe,
    InfiniteScrollDirective,
    FaIconComponent,
  ],
})
export class MessageComponent implements OnInit {
  subscription: Subscription | null = null;
  messages?: IMessage[];
  isLoading = false;

  sortState = sortStateSignal({});

  itemsPerPage = ITEMS_PER_PAGE;
  links: WritableSignal<Record<string, undefined | Record<string, string | undefined>>> = signal({});
  hasMorePage = computed(() => !!this.links().next);
  isFirstFetch = computed(() => Object.keys(this.links()).length === 0);

  public readonly router = inject(Router);
  protected readonly messageService = inject(MessageService);
  protected readonly activatedRoute = inject(ActivatedRoute);
  protected readonly sortService = inject(SortService);
  protected parseLinks = inject(ParseLinks);
  protected modalService = inject(NgbModal);
  protected ngZone = inject(NgZone);

  trackId = (item: IMessage): number => this.messageService.getMessageIdentifier(item);

  ngOnInit(): void {
    this.subscription = combineLatest([this.activatedRoute.queryParamMap, this.activatedRoute.data])
      .pipe(
        tap(([params, data]) => this.fillComponentAttributeFromRoute(params, data)),
        tap(() => this.reset()),
        tap(() => this.load()),
      )
      .subscribe();
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
}
