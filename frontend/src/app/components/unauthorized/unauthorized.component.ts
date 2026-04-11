import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-unauthorized',
  template: `
    <div class="unauth-container">
      <div class="unauth-card">
        <div class="unauth-icon">🚫</div>
        <h2>Access Denied</h2>
        <p>You don't have permission to view this page.</p>
        <button class="btn-primary" (click)="goBack()">Go Back</button>
      </div>
    </div>
  `
})
export class UnauthorizedComponent {
  constructor(private router: Router) {}
  goBack() { this.router.navigate(['/login']); }
}
