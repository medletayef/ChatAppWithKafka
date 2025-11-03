import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IChatRoom } from '../chat-room.model';
import { ChatRoomService } from '../service/chat-room.service';

const chatRoomResolve = (route: ActivatedRouteSnapshot): Observable<null | IChatRoom> => {
  const id = route.params.id;
  if (id) {
    return inject(ChatRoomService)
      .find(id)
      .pipe(
        mergeMap((chatRoom: HttpResponse<IChatRoom>) => {
          if (chatRoom.body) {
            return of(chatRoom.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default chatRoomResolve;
