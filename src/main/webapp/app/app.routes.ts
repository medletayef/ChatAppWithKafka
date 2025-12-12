import { Routes } from '@angular/router';

import { Authority } from 'app/config/authority.constants';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { errorRoute } from './layouts/error/error.route';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./home/home.component'),
    title: 'ChatApp',
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'admin',
    data: {
      authorities: [Authority.ADMIN],
    },
    canActivate: [UserRouteAccessService],
    loadChildren: () => import('./admin/admin.routes'),
  },

  {
    path: 'account',
    loadChildren: () => import('./account/account.route'),
  },
  {
    path: 'login',
    loadComponent: () => import('./login/login.component'),
    title: 'login.',
  },
  {
    path: 'invitations',
    title: 'Invitations',
    canActivate: [UserRouteAccessService],
    data: { pageTitle: 'Invitations' },
    loadComponent: () => import('./entities/invitation/list-invitations/list-invitations.component').then(c => c.ListInvitationsComponent),
  },
  // {
  //   path: '',
  //   loadChildren: () => import(`./entities/entity.routes`),
  // },
  ...errorRoute,
];

export default routes;
