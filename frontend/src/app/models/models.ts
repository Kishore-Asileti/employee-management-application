export type Role = 'HR' | 'MANAGER' | 'EMPLOYEE';
export type AccountStatus = 'PENDING' | 'ACTIVE' | 'REJECTED' | 'INACTIVE';

export interface Employee {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  department?: string;
  designation?: string;
  role: Role;
  status: AccountStatus;
  managerId?: string;
  camundaProcessInstanceId?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone?: string;
  department?: string;
  designation?: string;
  role?: Role;
}

export interface LoginResponse {
  token: string;
  employeeId: string;
  email: string;
  fullName: string;
  role: Role;
  processInstanceId?: string;
}

export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data?: T;
}

export interface CamundaTask {
  taskId: string;
  taskName: string;
  processInstanceId: string;
  created: string;
}

export interface EmployeeUpdateRequest {
  firstName?: string;
  lastName?: string;
  phone?: string;
  department?: string;
  designation?: string;
  role?: Role;
  status?: AccountStatus;
  managerId?: string;
}
