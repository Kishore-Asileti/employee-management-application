import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  regForm: FormGroup;
  loading = false;
  errorMsg = '';
  successMsg = '';
  processInstanceId = '';

  departments = ['Engineering', 'Finance', 'HR', 'Marketing', 'Operations', 'Sales', 'IT', 'Legal'];

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) {
    this.regForm = this.fb.group({
      firstName:   ['', Validators.required],
      lastName:    ['', Validators.required],
      email:       ['', [Validators.required, Validators.email]],
      password:    ['', [Validators.required, Validators.minLength(6)]],
      phone:       [''],
      department:  ['', Validators.required],
      designation: ['', Validators.required],
      role:        ['EMPLOYEE']
    });
  }

  onSubmit() {
    if (this.regForm.invalid) return;
    this.loading = true;
    this.errorMsg = '';
    this.successMsg = '';

    this.auth.register(this.regForm.value).subscribe({
      next: res => {
        this.loading = false;
        if (res.success) {
          this.processInstanceId = res.data?.processInstanceId ?? '';
          this.successMsg = res.message;
        } else {
          this.errorMsg = res.message;
        }
      },
      error: err => {
        this.loading = false;
        this.errorMsg = err.error?.message || 'Registration failed. Please try again.';
      }
    });
  }

  get f() { return this.regForm.controls; }
}
