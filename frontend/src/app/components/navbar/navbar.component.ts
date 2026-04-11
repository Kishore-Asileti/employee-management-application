import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html'
})
export class NavbarComponent {
  constructor(public auth: AuthService, private router: Router) {}

  logout() {
    this.auth.logout();
  }

  openTasklist() {
    window.open('http://localhost:8081/camunda/app/tasklist/', '_blank');
  }

  openCockpit() {
    window.open('http://localhost:8081/camunda/app/cockpit/', '_blank');
  }
}
