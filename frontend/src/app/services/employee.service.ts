import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, CamundaTask, Employee, EmployeeUpdateRequest } from '../models/models';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private api = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // ── HR ──────────────────────────────────────────────────────────────────────
  hrGetAllEmployees(): Observable<ApiResponse<Employee[]>> {
    return this.http.get<ApiResponse<Employee[]>>(`${this.api}/hr/employees`);
  }

  hrGetPendingEmployees(): Observable<ApiResponse<Employee[]>> {
    return this.http.get<ApiResponse<Employee[]>>(`${this.api}/hr/employees/pending`);
  }

  hrGetEmployee(id: string): Observable<ApiResponse<Employee>> {
    return this.http.get<ApiResponse<Employee>>(`${this.api}/hr/employees/${id}`);
  }

  hrUpdateEmployee(id: string, req: EmployeeUpdateRequest): Observable<ApiResponse<Employee>> {
    return this.http.put<ApiResponse<Employee>>(`${this.api}/hr/employees/${id}`, req);
  }

  hrDeleteEmployee(id: string): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${this.api}/hr/employees/${id}`);
  }

  hrGetTasks(): Observable<ApiResponse<CamundaTask[]>> {
    return this.http.get<ApiResponse<CamundaTask[]>>(`${this.api}/hr/tasks`);
  }

  hrApproveTask(taskId: string, approved: boolean): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(
      `${this.api}/hr/tasks/${taskId}/approve?approved=${approved}`, {}
    );
  }

  hrGetByRole(role: string): Observable<ApiResponse<Employee[]>> {
    return this.http.get<ApiResponse<Employee[]>>(`${this.api}/hr/employees/role/${role}`);
  }

  // ── Manager ─────────────────────────────────────────────────────────────────
  mgrGetMyTeam(): Observable<ApiResponse<Employee[]>> {
    return this.http.get<ApiResponse<Employee[]>>(`${this.api}/manager/team`);
  }

  mgrGetAllEmployees(): Observable<ApiResponse<Employee[]>> {
    return this.http.get<ApiResponse<Employee[]>>(`${this.api}/manager/employees`);
  }

  mgrUpdateEmployee(id: string, req: EmployeeUpdateRequest): Observable<ApiResponse<Employee>> {
    return this.http.put<ApiResponse<Employee>>(`${this.api}/manager/employees/${id}`, req);
  }

  mgrDeleteEmployee(id: string): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${this.api}/manager/employees/${id}`);
  }

  mgrGetReport(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${this.api}/manager/report`);
  }

  mgrGetByDepartment(dept: string): Observable<ApiResponse<Employee[]>> {
    return this.http.get<ApiResponse<Employee[]>>(`${this.api}/manager/employees/department/${dept}`);
  }

  // ── Employee ─────────────────────────────────────────────────────────────────
  getMyProfile(): Observable<ApiResponse<Employee>> {
    return this.http.get<ApiResponse<Employee>>(`${this.api}/employee/me`);
  }

  getEmployeeList(): Observable<ApiResponse<Employee[]>> {
    return this.http.get<ApiResponse<Employee[]>>(`${this.api}/employee/list`);
  }
}
