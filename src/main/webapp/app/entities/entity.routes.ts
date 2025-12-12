import { Routes } from '@angular/router';
import { UserRouteAccessService } from '../core/auth/user-route-access.service';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'Authorities' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  // {
  //   path: 'chat-room',
  //   data: { pageTitle: 'ChatRooms' },
  //   loadChildren: () => import('./chat-room/chat-room.routes'),
  // },
  // {
  //   path: 'message',
  //   data: { pageTitle: 'Messages' },
  //   loadChildren: () => import('./message/message.routes'),
  // },
  {
    path: 'invitations',
    canActivate: [UserRouteAccessService],
    data: { pageTitle: 'Invitations' },
    loadComponent: () => import('./invitation/list-invitations/list-invitations.component').then(c => c.ListInvitationsComponent),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
