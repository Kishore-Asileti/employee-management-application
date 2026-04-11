import { Component, OnInit } from '@angular/core';
import { Employee } from '../../models/models';
import { EmployeeService } from '../../services/employee.service';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-employee-dashboard',
  templateUrl: './employee-dashboard.component.html'
})
export class EmployeeDashboardComponent implements OnInit {
  profile: Employee | null = null;
  allEmployees: Employee[] = [];
  loading = false;
  searchTerm = '';
  activeTab: 'profile' | 'list' = 'profile';

  camundaTasklistUrl = `${environment.camundaUrl}/app/tasklist/`;

  constructor(public auth: AuthService, private empSvc: EmployeeService) {}

  ngOnInit() {
    this.loading = true;
    this.empSvc.getMyProfile().subscribe({
      next: r => { this.profile = r.data ?? null; this.loading = false; },
      error: () => { this.loading = false; }
    });
    this.empSvc.getEmployeeList().subscribe({
      next: r => this.allEmployees = r.data ?? []
    });
  }

  get filteredEmployees(): Employee[] {
    if (!this.searchTerm.trim()) return this.allEmployees;
    const t = this.searchTerm.toLowerCase();
    return this.allEmployees.filter(e =>
      (e.firstName + ' ' + e.lastName).toLowerCase().includes(t) ||
      e.email.toLowerCase().includes(t) ||
      (e.department ?? '').toLowerCase().includes(t)
    );
  }
}
