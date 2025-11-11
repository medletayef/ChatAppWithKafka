import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IChatRoom, NewChatRoom } from '../chat-room.model';
import { IUser } from '../../user/user.model';

export type PartialUpdateChatRoom = Partial<IChatRoom> & Pick<IChatRoom, 'id'>;

type RestOf<T extends IChatRoom | NewChatRoom> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

export type RestChatRoom = RestOf<IChatRoom>;

export type NewRestChatRoom = RestOf<NewChatRoom>;

export type PartialUpdateRestChatRoom = RestOf<PartialUpdateChatRoom>;

export type EntityResponseType = HttpResponse<IChatRoom>;
export type EntityArrayResponseType = HttpResponse<IChatRoom[]>;

@Injectable({ providedIn: 'root' })
export class ChatRoomService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/chat-rooms');

  create(chatRoom: NewChatRoom): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(chatRoom);
    return this.http
      .post<RestChatRoom>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(chatRoom: IChatRoom): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(chatRoom);
    return this.http
      .put<RestChatRoom>(`${this.resourceUrl}/${this.getChatRoomIdentifier(chatRoom)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(chatRoom: PartialUpdateChatRoom): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(chatRoom);
    return this.http
      .patch<RestChatRoom>(`${this.resourceUrl}/${this.getChatRoomIdentifier(chatRoom)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestChatRoom>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  findRelatedChatroomsWith(user: any): Observable<EntityArrayResponseType> {
    return this.http
      .get<RestChatRoom[]>(`${this.resourceUrl}/directly-related-to/member?memberLogin=${user.userId}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestChatRoom[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getChatRoomIdentifier(chatRoom: Pick<IChatRoom, 'id'>): number {
    return chatRoom.id;
  }

  compareChatRoom(o1: Pick<IChatRoom, 'id'> | null, o2: Pick<IChatRoom, 'id'> | null): boolean {
    return o1 && o2 ? this.getChatRoomIdentifier(o1) === this.getChatRoomIdentifier(o2) : o1 === o2;
  }

  addChatRoomToCollectionIfMissing<Type extends Pick<IChatRoom, 'id'>>(
    chatRoomCollection: Type[],
    ...chatRoomsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const chatRooms: Type[] = chatRoomsToCheck.filter(isPresent);
    if (chatRooms.length > 0) {
      const chatRoomCollectionIdentifiers = chatRoomCollection.map(chatRoomItem => this.getChatRoomIdentifier(chatRoomItem));
      const chatRoomsToAdd = chatRooms.filter(chatRoomItem => {
        const chatRoomIdentifier = this.getChatRoomIdentifier(chatRoomItem);
        if (chatRoomCollectionIdentifiers.includes(chatRoomIdentifier)) {
          return false;
        }
        chatRoomCollectionIdentifiers.push(chatRoomIdentifier);
        return true;
      });
      return [...chatRoomsToAdd, ...chatRoomCollection];
    }
    return chatRoomCollection;
  }

  protected convertDateFromClient<T extends IChatRoom | NewChatRoom | PartialUpdateChatRoom>(chatRoom: T): RestOf<T> {
    return {
      ...chatRoom,
      createdAt: chatRoom.createdAt?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restChatRoom: RestChatRoom): IChatRoom {
    return {
      ...restChatRoom,
      createdAt: restChatRoom.createdAt ? dayjs(restChatRoom.createdAt) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestChatRoom>): HttpResponse<IChatRoom> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestChatRoom[]>): HttpResponse<IChatRoom[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
