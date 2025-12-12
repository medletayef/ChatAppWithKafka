import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ApplicationConfigService } from '../../../core/config/application-config.service';
import { Observable } from 'rxjs';
import { IInvitation } from '../model/invitation';
import { NewChatRoom } from '../../chat-room/chat-room.model';
import { EntityArrayResponseType } from '../../user/service/user.service';

@Injectable({
  providedIn: 'root',
})
export class InvitationService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/invitations');

  getInvitationByRoomId(roomId: number): Observable<IInvitation> {
    return this.http.get<IInvitation>(this.resourceUrl + '/byRoomId?roomId=' + roomId);
  }
  getInvitationsByUserId(page: number, size: number): Observable<any> {
    return this.http.get<any[]>(this.resourceUrl + '/get-invitations?page=' + page + '&size=' + size, { observe: 'response' });
  }

  updateInvitation(invitation: IInvitation): Observable<IInvitation> {
    return this.http.put<IInvitation>(this.resourceUrl, invitation);
  }

  partialUpdateInvitation(invitation: any): Observable<any> {
    return this.http.patch<any>(this.resourceUrl + '/' + invitation.id, invitation);
  }

  sendInvitationsToRoom(room: NewChatRoom): Observable<any> {
    return this.http.post<any>(this.resourceUrl + '/invite-members', room);
  }
}
