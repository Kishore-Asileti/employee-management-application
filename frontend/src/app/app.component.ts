import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './services/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})
export class AppComponent {
  constructor(public auth: AuthService, private router: Router) {}

  getDashboardRoute(): string {
    const role = this.auth.currentUser?.role;
    if (role === 'HR') return '/hr-dashboard';
    if (role === 'MANAGER') return '/manager-dashboard';
    return '/employee-dashboard';
  }
}
