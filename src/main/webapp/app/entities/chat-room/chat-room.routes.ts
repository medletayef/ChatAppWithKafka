import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import ChatRoomResolve from './route/chat-room-routing-resolve.service';

const chatRoomRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/chat-room.component').then(m => m.ChatRoomComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/chat-room-detail.component').then(m => m.ChatRoomDetailComponent),
    resolve: {
      chatRoom: ChatRoomResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/chat-room-update.component').then(m => m.ChatRoomUpdateComponent),
    resolve: {
      chatRoom: ChatRoomResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/chat-room-update.component').then(m => m.ChatRoomUpdateComponent),
    resolve: {
      chatRoom: ChatRoomResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default chatRoomRoute;
