import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ApplicationConfigService } from '../../../core/config/application-config.service';
import { Observable } from 'rxjs';
import { IInvitation } from '../model/invitation';

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

  updateInvitation(invitation: IInvitation): Observable<IInvitation> {
    return this.http.put<IInvitation>(this.resourceUrl, invitation);
  }
}
