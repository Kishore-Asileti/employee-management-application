import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CamundaTask, Employee, EmployeeUpdateRequest } from '../../models/models';
import { EmployeeService } from '../../services/employee.service';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../../environments/environment';

type TabType = 'overview' | 'employees' | 'tasks' | 'add';

@Component({
  selector: 'app-hr-dashboard',
  templateUrl: './hr-dashboard.component.html'
})
export class HrDashboardComponent implements OnInit {
  activeTab: TabType = 'overview';
  employees: Employee[] = [];
  pendingEmployees: Employee[] = [];
  tasks: CamundaTask[] = [];

  selectedEmployee: Employee | null = null;
  editForm!: FormGroup;
  addForm!: FormGroup;

  loading = false;
  taskLoading = false;
  msg = '';
  msgType: 'success' | 'error' = 'success';

  searchTerm = '';
  filterRole = '';
  filterStatus = '';

  camundaTasklistUrl = `${environment.camundaUrl}/app/tasklist/`;

  departments = ['Engineering','Finance','HR','Marketing','Operations','Sales','IT','Legal'];
  roles = ['HR','MANAGER','EMPLOYEE'];
  statuses = ['PENDING','ACTIVE','REJECTED','INACTIVE'];

  constructor(
    private empSvc: EmployeeService,
    public auth: AuthService,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.initForms();
    this.loadAll();
  }

  initForms() {
    this.editForm = this.fb.group({
      firstName:   ['', Validators.required],
      lastName:    ['', Validators.required],
      phone:       [''],
      department:  [''],
      designation: [''],
      role:        ['EMPLOYEE'],
      status:      ['ACTIVE'],
      managerId:   ['']
    });

    this.addForm = this.fb.group({
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

  loadAll() {
    this.loading = true;
    this.empSvc.hrGetAllEmployees().subscribe({
      next: r => { this.employees = r.data ?? []; this.loading = false; },
      error: () => { this.loading = false; this.showMsg('Failed to load employees', 'error'); }
    });
    this.empSvc.hrGetPendingEmployees().subscribe({
      next: r => this.pendingEmployees = r.data ?? []
    });
    this.loadTasks();
  }

  loadTasks() {
    this.taskLoading = true;
    this.empSvc.hrGetTasks().subscribe({
      next: r => { this.tasks = r.data ?? []; this.taskLoading = false; },
      error: () => { this.taskLoading = false; }
    });
  }

  // ── Filtering ──────────────────────────────────────────────────────────────
  get filteredEmployees(): Employee[] {
    return this.employees.filter(e => {
      const term = this.searchTerm.toLowerCase();
      const matchSearch = !term ||
        (e.firstName + ' ' + e.lastName).toLowerCase().includes(term) ||
        e.email.toLowerCase().includes(term) ||
        (e.department ?? '').toLowerCase().includes(term);
      const matchRole   = !this.filterRole   || e.role === this.filterRole;
      const matchStatus = !this.filterStatus || e.status === this.filterStatus;
      return matchSearch && matchRole && matchStatus;
    });
  }

  // ── Edit Employee ──────────────────────────────────────────────────────────
  openEdit(emp: Employee) {
    this.selectedEmployee = emp;
    this.editForm.patchValue({
      firstName: emp.firstName, lastName: emp.lastName,
      phone: emp.phone ?? '', department: emp.department ?? '',
      designation: emp.designation ?? '', role: emp.role,
      status: emp.status, managerId: emp.managerId ?? ''
    });
  }

  saveEdit() {
    if (!this.selectedEmployee || this.editForm.invalid) return;
    const req: EmployeeUpdateRequest = this.editForm.value;
    this.empSvc.hrUpdateEmployee(this.selectedEmployee.id, req).subscribe({
      next: r => {
        const idx = this.employees.findIndex(e => e.id === this.selectedEmployee!.id);
        if (idx >= 0 && r.data) this.employees[idx] = r.data;
        this.selectedEmployee = null;
        this.showMsg('Employee updated successfully!', 'success');
      },
      error: () => this.showMsg('Update failed', 'error')
    });
  }

  // ── Delete Employee ────────────────────────────────────────────────────────
  deleteEmployee(id: string) {
    if (!confirm('Deactivate this employee?')) return;
    this.empSvc.hrDeleteEmployee(id).subscribe({
      next: () => {
        this.employees = this.employees.filter(e => e.id !== id);
        this.showMsg('Employee deactivated.', 'success');
      },
      error: () => this.showMsg('Delete failed', 'error')
    });
  }

  // ── Add Employee (HR direct add) ───────────────────────────────────────────
  addEmployee() {
    if (this.addForm.invalid) return;
    import('../../services/auth.service').then(() => {
      this.auth.register(this.addForm.value).subscribe({
        next: r => {
          if (r.success) {
            this.showMsg('Employee registration started via Camunda process!', 'success');
            this.addForm.reset({ role: 'EMPLOYEE' });
            this.activeTab = 'employees';
            this.loadAll();
          } else {
            this.showMsg(r.message, 'error');
          }
        },
        error: err => this.showMsg(err.error?.message ?? 'Add failed', 'error')
      });
    });
  }

  // ── HR Approval ────────────────────────────────────────────────────────────
  approveTask(taskId: string, approved: boolean) {
    this.empSvc.hrApproveTask(taskId, approved).subscribe({
      next: r => {
        this.tasks = this.tasks.filter(t => t.taskId !== taskId);
        this.showMsg(r.message, 'success');
        this.loadAll();
      },
      error: () => this.showMsg('Action failed', 'error')
    });
  }

  // ── Stats ──────────────────────────────────────────────────────────────────
  get totalEmployees() { return this.employees.length; }
  get activeCount() { return this.employees.filter(e => e.status === 'ACTIVE').length; }
  get pendingCount() { return this.employees.filter(e => e.status === 'PENDING').length; }
  get hrCount() { return this.employees.filter(e => e.role === 'HR').length; }
  get managerCount() { return this.employees.filter(e => e.role === 'MANAGER').length; }

  showMsg(msg: string, type: 'success' | 'error') {
    this.msg = msg;
    this.msgType = type;
    setTimeout(() => this.msg = '', 4000);
  }

  setTab(tab: TabType) {
    this.activeTab = tab;
    this.selectedEmployee = null;
    this.msg = '';
  }
}
