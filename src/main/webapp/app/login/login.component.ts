import { AfterViewInit, Component, ElementRef, OnInit, inject, signal, viewChild, NO_ERRORS_SCHEMA, Renderer2 } from '@angular/core';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { LoginService } from 'app/login/login.service';
import { AccountService } from 'app/core/auth/account.service';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';

@Component({
  standalone: true,
  selector: 'jhi-login',
  imports: [SharedModule, FormsModule, ReactiveFormsModule, RouterModule, MatBadgeModule, MatButtonModule],
  templateUrl: './login.component.html',
  schemas: [NO_ERRORS_SCHEMA],
  styleUrl: './login.component.scss',
})
export default class LoginComponent implements OnInit, AfterViewInit {
  username = viewChild.required<ElementRef>('username');
  errorMessage: string = '';
  authenticationError = signal(false);

  loginForm = new FormGroup({
    username: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    rememberMe: new FormControl(false, { nonNullable: true, validators: [Validators.required] }),
  });

  private readonly accountService = inject(AccountService);
  private readonly loginService = inject(LoginService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    // if already authenticated then navigate to home page
    this.accountService.identity().subscribe(
      () => {
        if (this.accountService.isAuthenticated()) {
          this.router.navigate(['']);
        }
      },
      () => {
        console.log('not authenticated');
      },
    );
  }

  ngAfterViewInit(): void {
    this.username().nativeElement.focus();
  }

  login(): void {
    this.loginService.login(this.loginForm.getRawValue()).subscribe({
      next: () => {
        this.authenticationError.set(false);
        if (!this.router.getCurrentNavigation()) {
          // There were no routing during login (eg from navigationToStoredUrl)
          this.router.navigate(['']);
        }
      },
      error: err => {
        this.authenticationError.set(true);
        console.log('error = ', err);
        if (err.status === 401) {
          this.errorMessage = 'Invalid username or password';
        } else if (err.status === 500 && err.error?.detail?.includes('not activated')) {
          this.errorMessage = 'Your account is not activated. Please check your email.';
        } else {
          this.errorMessage = 'An unexpected error occurred. Please try again later.';
        }
      },
    });
  }
}
