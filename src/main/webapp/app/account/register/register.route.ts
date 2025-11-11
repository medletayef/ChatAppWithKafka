import { Route } from '@angular/router';

import RegisterComponent from './register.component';
import { AuthRouteAccessService } from '../../core/auth/auth-route-access.service';

const registerRoute: Route = {
  path: 'register',
  component: RegisterComponent,
  title: 'Registration',
  canActivate: [AuthRouteAccessService],
};

export default registerRoute;
