# TaskFlow – Architecture Document

## 1. System Architecture Overview

TaskFlow follows a standard 3-tier web application architecture:

```
┌──────────────────────────────────────────────────────────┐
│                     Frontend (React)                      │
│  Vite + TypeScript + Tailwind CSS + React Query           │
│  Port: 5173                                               │
├──────────────────────────────────────────────────────────┤
│                        HTTP/REST                          │
│                      (Axios + JWT)                        │
├──────────────────────────────────────────────────────────┤
│                   Backend (Spring Boot)                    │
│  Java 17 + Spring Security + Spring Data JPA              │
│  Port: 8080                                               │
├──────────────────────────────────────────────────────────┤
│                       PostgreSQL                          │
│                      Port: 5432                           │
└──────────────────────────────────────────────────────────┘
```

---

## 2. Backend Architecture

### 2.1 Layered Architecture

```
Request → JwtAuthFilter → Controller → Service → Repository → Database
                                ↑              ↑
                             DTOs          Entities
```

**Controller Layer**: Handles HTTP requests, validates input, delegates to services, returns DTOs.

**Service Layer**: Contains business logic, enforces authorization rules (project ownership), orchestrates repository calls, performs DTO ↔ Entity mapping.

**Repository Layer**: Spring Data JPA interfaces providing CRUD and custom query methods.

**Entity Layer**: JPA-annotated domain objects mapped to database tables.

### 2.2 Security Architecture

```
                    ┌─────────────┐
                    │   Request   │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │  JwtFilter  │──── Extract & validate token
                    └──────┬──────┘
                           │
              ┌────────────▼────────────┐
              │  SecurityFilterChain    │
              │  - /api/auth/** permit  │
              │  - /** authenticated    │
              └────────────┬────────────┘
                           │
                    ┌──────▼──────┐
                    │ Controller  │
                    └─────────────┘
```

- **Stateless sessions** – no server-side session storage.
- **JWT tokens** contain user ID and email, signed with HMAC-SHA256.
- **BCrypt** password hashing with default strength.
- **CORS** configured for frontend origin.

### 2.3 Package Structure

```
com.taskflow
├── TaskFlowApplication.java
├── config/
│   ├── SecurityConfig.java          # Security filter chain, CORS
│   ├── JwtConfig.java               # JWT properties binding
│   └── WebConfig.java               # Additional web configuration
├── controller/
│   ├── AuthController.java          # POST /api/auth/register, /api/auth/login
│   ├── ProjectController.java       # CRUD /api/projects
│   └── TaskController.java          # CRUD /api/projects/{id}/tasks
├── dto/
│   ├── request/
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── CreateProjectRequest.java
│   │   ├── UpdateProjectRequest.java
│   │   ├── CreateTaskRequest.java
│   │   ├── UpdateTaskRequest.java
│   │   ├── UpdateTaskStatusRequest.java
│   │   └── AssignTaskRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── ProjectResponse.java
│       ├── ProjectSummaryResponse.java
│       ├── TaskResponse.java
│       ├── PagedResponse.java
│       └── ErrorResponse.java
├── entity/
│   ├── User.java
│   ├── Project.java
│   └── Task.java
├── enums/
│   ├── TaskStatus.java              # TODO, IN_PROGRESS, DONE
│   └── TaskPriority.java            # LOW, MEDIUM, HIGH
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── UnauthorizedException.java
│   └── BadRequestException.java
├── repository/
│   ├── UserRepository.java
│   ├── ProjectRepository.java
│   └── TaskRepository.java
├── security/
│   ├── JwtTokenProvider.java        # Token generation & validation
│   ├── JwtAuthenticationFilter.java # OncePerRequestFilter
│   └── CustomUserDetailsService.java
└── service/
    ├── AuthService.java
    ├── ProjectService.java
    └── TaskService.java
```

---

## 3. Frontend Architecture

### 3.1 Component Hierarchy

```
App
├── AuthProvider (Context)
│   ├── PublicRoutes
│   │   ├── LoginPage
│   │   │   └── LoginForm
│   │   └── RegisterPage
│   │       └── RegisterForm
│   └── ProtectedRoutes
│       ├── Layout (Header + Content)
│       │   ├── DashboardPage
│       │   │   ├── ProjectList
│       │   │   │   └── ProjectCard[]
│       │   │   └── CreateProjectModal
│       │   └── ProjectPage
│       │       ├── KanbanBoard
│       │       │   └── KanbanColumn[]
│       │       │       └── TaskCard[]
│       │       ├── TaskModal
│       │       └── FilterBar
```

### 3.2 State Management

| State Type | Technology | Examples |
|-----------|------------|---------|
| Server State | React Query | Projects, Tasks, User |
| Auth State | React Context | Token, current user, login/logout |
| UI State | useState | Modal open/close, form inputs, filter selections |

### 3.3 API Layer

```typescript
// Centralized Axios instance with JWT interceptor
api/
├── client.ts         // Axios instance + interceptors
├── auth.ts           // login(), register()
├── projects.ts       // getProjects(), createProject(), ...
└── tasks.ts          // getTasks(), createTask(), updateTask(), ...
```

The Axios instance automatically:
- Attaches `Authorization: Bearer <token>` header.
- Redirects to login on 401 responses.

### 3.4 Routing

| Path | Component | Access |
|------|-----------|--------|
| `/login` | LoginPage | Public |
| `/register` | RegisterPage | Public |
| `/dashboard` | DashboardPage | Protected |
| `/projects/:id` | ProjectPage | Protected |

---

## 4. Database Design

### 4.1 Entity-Relationship Diagram

```
┌──────────┐       1:N        ┌───────────┐       1:N        ┌──────────┐
│  Users   │──────────────────│  Projects  │──────────────────│  Tasks   │
│          │  (owner_id)      │            │  (project_id)    │          │
└──────────┘                  └───────────┘                   └──────────┘
     │                                                             │
     │                              1:N                            │
     └─────────────────────────────────────────────────────────────┘
                            (assignee_id)
```

### 4.2 Indexes

- `users.email` – UNIQUE index (login lookup).
- `projects.owner_id` – Index (list user's projects).
- `tasks.project_id` – Index (list project's tasks).
- `tasks.assignee_id` – Index (filter by assignee).
- `tasks.status` – Index (filter by status).

---

## 5. API Design Principles

1. **RESTful resource naming**: Nouns for resources, HTTP verbs for actions.
2. **Nested resources**: Tasks nested under projects (`/projects/{id}/tasks`).
3. **Consistent response format**: All responses wrapped in a consistent structure.
4. **HTTP status codes**: 200 (OK), 201 (Created), 204 (No Content), 400 (Bad Request), 401 (Unauthorized), 403 (Forbidden), 404 (Not Found).
5. **Pagination**: Page-based with `page`, `size`, `totalElements`, `totalPages`.
6. **Filtering**: Query parameters for status, priority, assignee.
7. **Sorting**: `sortBy` and `sortDir` query parameters.

---

## 6. Security Considerations

1. **Passwords**: BCrypt hashing, never stored or returned in plaintext.
2. **JWT**: Short-lived tokens (24h), HMAC-SHA256 signed, includes user ID and email.
3. **CORS**: Restricted to frontend origin only.
4. **Input Validation**: Bean Validation on all request DTOs.
5. **Authorization**: Project ownership checks in service layer – users can only access their own projects and tasks.
6. **SQL Injection**: Prevented by using parameterized queries via JPA.
7. **XSS**: React auto-escapes output; API returns JSON only.

---

## 7. Error Handling Strategy

### Backend
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "title": "Title is required",
    "priority": "Invalid priority value"
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Frontend
- **Network errors**: Toast notification + retry option.
- **Validation errors**: Inline field-level error messages.
- **404 errors**: "Not found" page or message.
- **401 errors**: Redirect to login.
- **Generic errors**: Error boundary with fallback UI.

---

## 8. Performance Considerations

1. **Pagination**: Server-side pagination for task lists.
2. **React Query caching**: Reduces redundant API calls.
3. **Lazy loading**: Route-based code splitting (React.lazy).
4. **Database indexes**: On frequently queried columns.
5. **Stateless auth**: No server-side session overhead.

---

## 9. Development Environment

| Tool | Version | Purpose |
|------|---------|---------|
| Java | 17.0.2 | Backend runtime |
| Maven | 3.9.8 | Backend build & dependency management |
| Node.js | 26.8.1 | Frontend runtime |
| npm | 11.19.0 | Frontend package management |
| Git | 2.54.0 | Version control |
| PostgreSQL | 15+ | Database (via Docker or local) |

### Running the Application

```bash
# Backend
cd backend
export JAVA_HOME=$HOME/tools/jdk-17.0.2.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$HOME/tools/apache-maven-3.9.8/bin:$PATH"
mvn spring-boot:run

# Frontend
cd frontend
npm install
npm run dev

# Database (Docker)
docker run -d --name taskflow-db \
  -e POSTGRES_DB=taskflow \
  -e POSTGRES_USER=taskflow \
  -e POSTGRES_PASSWORD=taskflow \
  -p 5432:5432 postgres:15
```
