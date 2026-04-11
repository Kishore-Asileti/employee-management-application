import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html'
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;
  errorMsg = '';
  showPassword = false;

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) {
    this.loginForm = this.fb.group({
      email:    ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });

    // Redirect if already logged in
    if (this.auth.isLoggedIn) this.redirectByRole();
  }

  onSubmit() {
    if (this.loginForm.invalid) return;
    this.loading = true;
    this.errorMsg = '';

    this.auth.login(this.loginForm.value).subscribe({
      next: res => {
        this.loading = false;
        if (res.success) this.redirectByRole();
        else this.errorMsg = res.message;
      },
      error: err => {
        this.loading = false;
        this.errorMsg = err.error?.message || 'Login failed. Please try again.';
      }
    });
  }

  private redirectByRole() {
    const role = this.auth.currentUser?.role;
    if (role === 'HR') this.router.navigate(['/hr-dashboard']);
    else if (role === 'MANAGER') this.router.navigate(['/manager-dashboard']);
    else this.router.navigate(['/employee-dashboard']);
  }

  get f() { return this.loginForm.controls; }
}
