import { AfterViewInit, Component, ElementRef, inject, NO_ERRORS_SCHEMA, OnInit, signal, viewChild } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { AbstractControl, FormControl, FormGroup, FormsModule, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';

import { EMAIL_ALREADY_USED_TYPE, LOGIN_ALREADY_USED_TYPE } from 'app/config/error.constants';
import SharedModule from 'app/shared/shared.module';
import PasswordStrengthBarComponent from '../password/password-strength-bar/password-strength-bar.component';
import { RegisterService } from './register.service';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';

@Component({
  standalone: true,
  selector: 'jhi-register',
  imports: [SharedModule, RouterModule, FormsModule, ReactiveFormsModule, PasswordStrengthBarComponent, FaIconComponent],
  templateUrl: './register.component.html',
  schemas: [NO_ERRORS_SCHEMA],
  styleUrl: './register.component.scss',
})
export default class RegisterComponent implements AfterViewInit {
  login = viewChild.required<ElementRef>('login');

  doNotMatch = signal(false);
  error = signal(false);
  errorEmailExists = signal(false);
  errorUserExists = signal(false);
  success = signal(false);
  imageUrl = signal<string | null>(null);
  image = viewChild.required<ElementRef<HTMLInputElement>>('imageRef');
  selectedFile: File | null = null;

  registerForm = new FormGroup(
    {
      login: new FormControl('', {
        nonNullable: true,
        validators: [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(50),
          Validators.pattern('^[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$|^[_.@A-Za-z0-9-]+$'),
        ],
      }),
      firstname: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(2), Validators.maxLength(254)],
      }),
      lastname: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(2), Validators.maxLength(254)],
      }),
      email: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(5), Validators.maxLength(254), Validators.email],
      }),
      password: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(4), Validators.maxLength(100)],
      }),
      confirmPassword: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(4), Validators.maxLength(100)],
      }),
    },
    { validators: this.passwordMatchValidator },
  );

  private readonly registerService = inject(RegisterService);

  constructor() {
    this.registerForm.get('confirmPassword')?.valueChanges.subscribe(() => {
      this.registerForm.updateValueAndValidity({ onlySelf: false, emitEvent: false });
    });
  }

  ngAfterViewInit(): void {
    this.login().nativeElement.focus();
  }

  register(): void {
    this.doNotMatch.set(false);
    this.error.set(false);
    this.errorEmailExists.set(false);
    this.errorUserExists.set(false);
    const formData = new FormData();
    const { password, confirmPassword } = this.registerForm.getRawValue();
    if (password !== confirmPassword) {
      this.doNotMatch.set(true);
    } else {
      formData.append('login', this.registerForm.getRawValue().login);
      formData.append('email', this.registerForm.getRawValue().email);
      formData.append('firstName', this.registerForm.getRawValue().firstname);
      formData.append('lastName', this.registerForm.getRawValue().lastname);
      formData.append('password', this.registerForm.getRawValue().password);
      formData.append('langKey', 'en');
      if (this.selectedFile) {
        formData.append('image', this.selectedFile);
      }

      this.registerService.save(formData).subscribe({ next: () => this.success.set(true), error: response => this.processError(response) });
    }
  }

  onFileChange(event: any): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = e => this.imageUrl.set(e.target?.result as string);
      reader.readAsDataURL(file);
      this.selectedFile = file;
    }
  }

  removeProfileImage(): void {
    const input = this.image().nativeElement;
    input.value = '';
    this.imageUrl.set(null);
  }

  passwordMatchValidator(form: AbstractControl): ValidationErrors | null {
    const password = form.get('password')?.value;
    const confirm = form.get('confirmPassword')?.value;
    return password === confirm ? null : { passwordMismatch: true };
  }

  private processError(response: HttpErrorResponse): void {
    if (response.status === 400 && response.error.type === LOGIN_ALREADY_USED_TYPE) {
      this.errorUserExists.set(true);
    } else if (response.status === 400 && response.error.type === EMAIL_ALREADY_USED_TYPE) {
      this.errorEmailExists.set(true);
    } else {
      this.error.set(true);
    }
  }
}
