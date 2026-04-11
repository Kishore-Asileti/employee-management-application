# Employee Management System

A full-stack application based on the `employee_management_application.bpmn` process, built with:

- **Backend**: Spring Boot 3.2 · Camunda BPM 7.20 · Couchbase · Maven · JWT Security
- **Frontend**: Angular 17 · Reactive Forms · Role-based routing
- **Process Engine**: Camunda 7 (Tasklist / Cockpit / Admin at port **8081**)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│  Angular (port 4200)                                        │
│  Login ─► Register ─► HR / Manager / Employee Dashboard    │
└────────────────────┬────────────────────────────────────────┘
                     │ HTTP + JWT
┌────────────────────▼────────────────────────────────────────┐
│  Spring Boot (port 8081)                                    │
│  /api/auth   – login, register (starts Camunda process)    │
│  /api/hr     – HR CRUD + task approval                      │
│  /api/manager– team management + reports                    │
│  /api/employee – self-service profile                       │
│  /api/camunda – process instances / tasks / history        │
│  /camunda/app/tasklist  ◄── Camunda Tasklist UI             │
│  /camunda/app/cockpit   ◄── Camunda Cockpit UI              │
│  /engine-rest/**        ◄── Camunda REST API                │
└────────────────────┬───────────────────┬───────────────────┘
                     │                   │
          ┌──────────▼──────┐   ┌────────▼────────┐
          │  Couchbase DB   │   │  H2 (Camunda)   │
          │  (employees)    │   │  (process state)│
          └─────────────────┘   └─────────────────┘
```

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+ |
| Maven | 3.8+ |
| Node.js | 18+ |
| Angular CLI | 17+ (`npm install -g @angular/cli`) |
| Couchbase Server | 7.x |

---

## 1 — Couchbase Setup

1. Install and start Couchbase Server (Community or Enterprise)
2. Open **http://localhost:8091** → complete the setup wizard
3. Create a bucket named **`employee_management`**
4. Create a user `Administrator` / `password` with full bucket access
5. Create a primary index:
   ```sql
   CREATE PRIMARY INDEX ON `employee_management`;
   ```

> To use different credentials, update `application.properties`:
> ```
> spring.couchbase.connection-string=couchbase://localhost
> spring.couchbase.username=Administrator
> spring.couchbase.password=password
> spring.data.couchbase.bucket-name=employee_management
> ```

---

## 2 — Backend Setup

```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
```

The server starts on **http://localhost:8081**

### Camunda Web Apps (no extra install needed)

| App | URL | Credentials |
|-----|-----|-------------|
| Tasklist | http://localhost:8081/camunda/app/tasklist/ | admin / admin |
| Cockpit  | http://localhost:8081/camunda/app/cockpit/  | admin / admin |
| Admin    | http://localhost:8081/camunda/app/admin/    | admin / admin |

The BPMN file `employee_management_application.bpmn` is auto-deployed on startup.

---

## 3 — Frontend Setup

```bash
cd frontend
npm install
ng serve --proxy-config proxy.conf.json
```

App runs at **http://localhost:4200**

---

## 4 — Default Login Accounts

Seeded automatically on first startup (requires Couchbase to be running):

| Role | Email | Password |
|------|-------|----------|
| HR | hr@company.com | Admin@123 |
| Manager | manager@company.com | Admin@123 |
| Employee | emp@company.com | Admin@123 |

---

## 5 — BPMN Process Flow

```
Employee visits portal
        │
        ▼
  Login / Register?
   ├─ New Employee ──► Fill Registration Form
   │                        │
   │               Validate & Save to Couchbase
   │                        │
   │               Valid? ──┤
   │                  │     └─ Invalid → Show Errors → Loop back
   │                  ▼
   │           Send Welcome Email (EmailServiceDelegate)
   │                  │
   │           HR Reviews & Approves  ◄── Camunda Tasklist HR task
   │                  │
   │           Approved? ──┤
   │                │      └─ Rejected → End (Rejected)
   │                ▼
   │       Activate Account (AccountServiceDelegate)
   │                │
   └─ Existing ─────┘
        ▼
  Enter Credentials
        │
  Authenticate (AuthServiceDelegate)
        │
  Role Check ──► HR Dashboard / Manager Dashboard / Employee Dashboard
```

---

## 6 — Key API Endpoints

### Auth
```
POST /api/auth/register    – Start registration Camunda process
POST /api/auth/login       – Login, returns JWT
```

### HR (requires HR role)
```
GET    /api/hr/employees          – List all employees
GET    /api/hr/employees/pending  – Pending approval
PUT    /api/hr/employees/{id}     – Update employee
DELETE /api/hr/employees/{id}     – Soft-delete (INACTIVE)
GET    /api/hr/tasks              – Camunda HR approval tasks
POST   /api/hr/tasks/{id}/approve?approved=true|false
```

### Manager
```
GET    /api/manager/team              – My team members
GET    /api/manager/employees         – All employees (read)
PUT    /api/manager/employees/{id}    – Edit employee
DELETE /api/manager/employees/{id}    – Soft-delete
GET    /api/manager/report            – Team report
```

### Employee
```
GET /api/employee/me    – Own profile
GET /api/employee/list  – View colleagues
```

### Camunda (HR only)
```
GET  /api/camunda/instances           – Running process instances
GET  /api/camunda/instances/history   – Process history
GET  /api/camunda/tasks/all           – All open tasks
POST /api/camunda/tasks/{id}/complete – Complete a task
GET  /api/camunda/process/{id}/variables
```

---

## 7 — Project Structure

```
employee-management-project/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/company/empmanagement/
│       │   ├── EmployeeManagementApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   ├── CouchbaseConfig.java
│       │   │   ├── CamundaConfig.java
│       │   │   ├── DataInitializer.java
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   ├── HRController.java
│       │   │   ├── ManagerController.java
│       │   │   ├── EmployeeController.java
│       │   │   └── CamundaTaskController.java
│       │   ├── delegate/           ← Camunda JavaDelegates
│       │   │   ├── EmailServiceDelegate.java
│       │   │   ├── AuthServiceDelegate.java
│       │   │   ├── AccountServiceDelegate.java
│       │   │   └── ValidateRegistrationDelegate.java
│       │   ├── model/
│       │   │   ├── Employee.java
│       │   │   ├── Role.java
│       │   │   └── AccountStatus.java
│       │   ├── repository/
│       │   │   └── EmployeeRepository.java
│       │   ├── security/
│       │   │   ├── JwtUtils.java
│       │   │   └── JwtAuthFilter.java
│       │   ├── service/
│       │   │   └── EmployeeService.java
│       │   └── dto/
│       │       ├── LoginRequest.java
│       │       ├── LoginResponse.java
│       │       ├── RegisterRequest.java
│       │       ├── ApiResponse.java
│       │       └── EmployeeUpdateRequest.java
│       └── resources/
│           ├── application.properties
│           └── processes/
│               └── employee_management_application.bpmn
│
└── frontend/
    ├── angular.json
    ├── package.json
    ├── proxy.conf.json
    └── src/
        ├── app/
        │   ├── app.module.ts
        │   ├── app-routing.module.ts
        │   ├── components/
        │   │   ├── login/
        │   │   ├── register/
        │   │   ├── hr-dashboard/
        │   │   ├── manager-dashboard/
        │   │   ├── employee-dashboard/
        │   │   ├── navbar/
        │   │   └── unauthorized/
        │   ├── models/models.ts
        │   ├── services/
        │   │   ├── auth.service.ts
        │   │   ├── employee.service.ts
        │   │   └── jwt.interceptor.ts
        │   └── guards/auth.guard.ts
        ├── environments/
        ├── styles.css
        ├── index.html
        └── main.ts
```

---

## 8 — Troubleshooting

**Couchbase connection fails on startup**
- Ensure Couchbase is running and bucket `employee_management` exists
- The DataInitializer will fail silently if Couchbase is unreachable; start Couchbase first

**Camunda Tasklist not loading**
- Navigate to http://localhost:8081/camunda/app/tasklist/
- Login with `admin` / `admin`
- HR tasks appear after an employee registers

**CORS errors in Angular**
- Make sure Angular is started with: `ng serve --proxy-config proxy.conf.json`

**H2 console (Camunda DB inspection)**
- http://localhost:8081/h2-console
- JDBC URL: `jdbc:h2:mem:camunda`
- Username: `sa` / Password: (empty)
