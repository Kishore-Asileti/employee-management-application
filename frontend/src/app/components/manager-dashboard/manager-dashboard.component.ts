import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Employee, EmployeeUpdateRequest } from '../../models/models';
import { EmployeeService } from '../../services/employee.service';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../../environments/environment';

type TabType = 'overview' | 'team' | 'all' | 'report';

@Component({
  selector: 'app-manager-dashboard',
  templateUrl: './manager-dashboard.component.html'
})
export class ManagerDashboardComponent implements OnInit {
  activeTab: TabType = 'overview';
  allEmployees: Employee[] = [];
  teamEmployees: Employee[] = [];
  report: any = null;

  selectedEmployee: Employee | null = null;
  editForm!: FormGroup;

  loading = false;
  msg = '';
  msgType: 'success' | 'error' = 'success';
  searchTerm = '';
  filterDept = '';

  departments = ['Engineering','Finance','HR','Marketing','Operations','Sales','IT','Legal'];
  camundaTasklistUrl = `${environment.camundaUrl}/app/tasklist/`;

  constructor(
    private empSvc: EmployeeService,
    public auth: AuthService,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.editForm = this.fb.group({
      firstName:   ['', Validators.required],
      lastName:    ['', Validators.required],
      phone:       [''],
      department:  [''],
      designation: ['']
    });
    this.loadAll();
  }

  loadAll() {
    this.loading = true;
    this.empSvc.mgrGetAllEmployees().subscribe({
      next: r => { this.allEmployees = r.data ?? []; this.loading = false; },
      error: () => { this.loading = false; this.showMsg('Failed to load employees', 'error'); }
    });
    this.empSvc.mgrGetMyTeam().subscribe({
      next: r => this.teamEmployees = r.data ?? []
    });
  }

  loadReport() {
    this.empSvc.mgrGetReport().subscribe({
      next: r => this.report = r.data,
      error: () => this.showMsg('Report generation failed', 'error')
    });
  }

  get filteredAll(): Employee[] {
    return this.allEmployees.filter(e => {
      const term = this.searchTerm.toLowerCase();
      const matchSearch = !term ||
        (e.firstName + ' ' + e.lastName).toLowerCase().includes(term) ||
        e.email.toLowerCase().includes(term);
      const matchDept = !this.filterDept || e.department === this.filterDept;
      return matchSearch && matchDept;
    });
  }

  openEdit(emp: Employee) {
    this.selectedEmployee = emp;
    this.editForm.patchValue({
      firstName: emp.firstName, lastName: emp.lastName,
      phone: emp.phone ?? '', department: emp.department ?? '',
      designation: emp.designation ?? ''
    });
  }

  saveEdit() {
    if (!this.selectedEmployee || this.editForm.invalid) return;
    const req: EmployeeUpdateRequest = this.editForm.value;
    this.empSvc.mgrUpdateEmployee(this.selectedEmployee.id, req).subscribe({
      next: r => {
        const idx = this.allEmployees.findIndex(e => e.id === this.selectedEmployee!.id);
        if (idx >= 0 && r.data) this.allEmployees[idx] = r.data;
        this.selectedEmployee = null;
        this.showMsg('Employee updated!', 'success');
      },
      error: () => this.showMsg('Update failed', 'error')
    });
  }

  deleteEmployee(id: string) {
    if (!confirm('Deactivate this employee?')) return;
    this.empSvc.mgrDeleteEmployee(id).subscribe({
      next: () => {
        this.allEmployees = this.allEmployees.filter(e => e.id !== id);
        this.showMsg('Employee deactivated.', 'success');
      },
      error: () => this.showMsg('Delete failed', 'error')
    });
  }

  setTab(tab: TabType) {
    this.activeTab = tab;
    this.selectedEmployee = null;
    if (tab === 'report') this.loadReport();
  }

  showMsg(msg: string, type: 'success' | 'error') {
    this.msg = msg; this.msgType = type;
    setTimeout(() => this.msg = '', 4000);
  }

  get teamActive() { return this.teamEmployees.filter(e => e.status === 'ACTIVE').length; }
  get uniqueDepts() { return [...new Set(this.allEmployees.map(e => e.department).filter(Boolean))]; }
}
