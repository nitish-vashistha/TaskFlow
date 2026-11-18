# TaskFlow – Implementation Plan

## 1. Project Overview

**TaskFlow** is a full-stack Team Task Management System built as a professional SaaS-style application. It allows users to register, create projects, manage tasks within projects via a Kanban board, assign team members, and track progress through filtering, sorting, and status management.

### Technology Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.x, Maven |
| Security | Spring Security, JWT (jjwt) |
| Persistence | Spring Data JPA, Hibernate, PostgreSQL |
| Backend Testing | JUnit 5, Mockito |
| Frontend | React 18+, TypeScript, Vite |
| Styling | Tailwind CSS |
| HTTP Client | Axios |
| Server State | TanStack React Query |
| Auth State | React Context API |
| Frontend Testing | Jest, React Testing Library |
| Version Control | Git (Conventional Commits) |

> **Deviation Note:** The original assignment specifies Kotlin and MockK. Java and Mockito are selected alternatives. This deviation must be verified with recruiter approval. Risk: low – the functional requirements and architecture remain identical.

---

## 2. Functional Requirements

### Authentication
- FR-1: User registration with email, full name, and password.
- FR-2: User login returning a JWT access token.
- FR-3: Passwords hashed with BCrypt.
- FR-4: JWT-based stateless authentication for all protected endpoints.
- FR-5: Token validation and refresh handling.

### Project Management
- FR-6: Authenticated users can create projects (name, description).
- FR-7: Users can list their own projects.
- FR-8: Project ownership enforced – only the owner can manage the project and its tasks.
- FR-9: Projects expose a summary (total tasks, completed tasks, overdue tasks).

### Task Management
- FR-10: Create tasks within a project (title, description, priority, status, due date).
- FR-11: Update task details.
- FR-12: Delete tasks.
- FR-13: Update task status (TODO → IN_PROGRESS → DONE).
- FR-14: Assign tasks to users.
- FR-15: Filter tasks by status, priority, assignee.
- FR-16: Sort tasks by due date, priority, creation date.
- FR-17: Paginate task lists.
- FR-18: Calculate and flag overdue tasks.

### Frontend
- FR-19: Login and registration pages.
- FR-20: Protected route system.
- FR-21: Projects dashboard listing user projects.
- FR-22: Project detail view with Kanban board.
- FR-23: Task creation and editing via modal.
- FR-24: Drag-and-drop or click-based status transitions on Kanban.
- FR-25: Filter and sort controls.
- FR-26: Loading, empty, and error states.
- FR-27: Responsive layout (desktop and mobile).

---

## 3. Backend Architecture

```
┌─────────────────────────────────────────────────┐
│                   Controllers                    │
│  AuthController  ProjectController  TaskController│
├─────────────────────────────────────────────────┤
│                    Services                      │
│  AuthService   ProjectService    TaskService     │
├─────────────────────────────────────────────────┤
│                  Repositories                    │
│  UserRepository ProjectRepository TaskRepository │
├─────────────────────────────────────────────────┤
│                    Entities                       │
│       User         Project          Task          │
├─────────────────────────────────────────────────┤
│           Spring Data JPA / Hibernate            │
├─────────────────────────────────────────────────┤
│                  PostgreSQL                       │
└─────────────────────────────────────────────────┘
```

### Cross-Cutting Concerns
- **Security**: JWT filter chain, SecurityConfig, authentication entry point.
- **Exception Handling**: Global `@RestControllerAdvice` with structured error responses.
- **Validation**: Bean Validation (`@Valid`, `@NotBlank`, etc.).
- **DTOs**: Request/Response objects decoupled from entities.
- **Mapping**: Manual or MapStruct-based entity ↔ DTO conversion.

### Design Patterns
- **Repository Pattern**: Spring Data JPA repositories.
- **Service Layer Pattern**: Business logic encapsulation.
- **DTO Pattern**: API boundary separation.
- **Strategy Pattern**: Task filtering/sorting strategies (if complexity warrants).
- **Builder Pattern**: Entity/DTO construction.
- **Filter Chain**: Spring Security filter pipeline.

---

## 4. Frontend Architecture

```
src/
├── api/              # Axios instance, API service functions
├── components/       # Reusable UI components
│   ├── common/       # Button, Input, Modal, Card, Badge, Spinner
│   ├── auth/         # LoginForm, RegisterForm
│   ├── projects/     # ProjectCard, ProjectList, CreateProjectModal
│   ├── tasks/        # TaskCard, TaskModal, KanbanBoard, KanbanColumn
│   └── layout/       # Header, Sidebar, PageLayout
├── contexts/         # AuthContext
├── hooks/            # useAuth, useProjects, useTasks
├── pages/            # LoginPage, RegisterPage, DashboardPage, ProjectPage
├── types/            # TypeScript interfaces and enums
├── utils/            # Date formatting, validation helpers
├── App.tsx
├── main.tsx
└── index.css
```

### State Management Strategy
- **Server State**: TanStack React Query (projects, tasks, user data).
- **Auth State**: React Context + localStorage for JWT token.
- **Local UI State**: Component-level `useState`/`useReducer`.

---

## 5. Database Schema

### Users Table
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT |
| email | VARCHAR(255) | UNIQUE, NOT NULL |
| full_name | VARCHAR(255) | NOT NULL |
| password | VARCHAR(255) | NOT NULL (hashed) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW |
| updated_at | TIMESTAMP | NOT NULL |

### Projects Table
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT |
| name | VARCHAR(255) | NOT NULL |
| description | TEXT | NULLABLE |
| owner_id | BIGINT | FK → users.id, NOT NULL |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

### Tasks Table
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT |
| title | VARCHAR(255) | NOT NULL |
| description | TEXT | NULLABLE |
| status | VARCHAR(20) | NOT NULL (TODO, IN_PROGRESS, DONE) |
| priority | VARCHAR(20) | NOT NULL (LOW, MEDIUM, HIGH) |
| due_date | DATE | NULLABLE |
| project_id | BIGINT | FK → projects.id, NOT NULL |
| assignee_id | BIGINT | FK → users.id, NULLABLE |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

### Relationships
- User 1:N Projects (owner)
- Project 1:N Tasks
- User 1:N Tasks (assignee)

---

## 6. API Endpoint Specifications

### Authentication
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | Login, returns JWT | No |

### Projects
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/projects` | Create project | Yes |
| GET | `/api/projects` | List user's projects | Yes |
| GET | `/api/projects/{id}` | Get project details | Yes |
| PUT | `/api/projects/{id}` | Update project | Yes |
| DELETE | `/api/projects/{id}` | Delete project | Yes |
| GET | `/api/projects/{id}/summary` | Get project summary | Yes |

### Tasks
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/projects/{projectId}/tasks` | Create task | Yes |
| GET | `/api/projects/{projectId}/tasks` | List tasks (filter, sort, page) | Yes |
| GET | `/api/projects/{projectId}/tasks/{id}` | Get task details | Yes |
| PUT | `/api/projects/{projectId}/tasks/{id}` | Update task | Yes |
| DELETE | `/api/projects/{projectId}/tasks/{id}` | Delete task | Yes |
| PATCH | `/api/projects/{projectId}/tasks/{id}/status` | Update task status | Yes |
| PATCH | `/api/projects/{projectId}/tasks/{id}/assign` | Assign task | Yes |

### Query Parameters for Task Listing
- `status` – filter by status (TODO, IN_PROGRESS, DONE)
- `priority` – filter by priority (LOW, MEDIUM, HIGH)
- `assigneeId` – filter by assignee
- `sortBy` – field to sort (dueDate, priority, createdAt)
- `sortDir` – direction (asc, desc)
- `page` – page number (0-indexed)
- `size` – page size (default 20)

---

## 7. Folder Structure

```
TaskFlow/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/taskflow/
│       │   │   ├── TaskFlowApplication.java
│       │   │   ├── config/
│       │   │   │   ├── SecurityConfig.java
│       │   │   │   ├── JwtConfig.java
│       │   │   │   └── CorsConfig.java
│       │   │   ├── controller/
│       │   │   │   ├── AuthController.java
│       │   │   │   ├── ProjectController.java
│       │   │   │   └── TaskController.java
│       │   │   ├── dto/
│       │   │   │   ├── request/
│       │   │   │   └── response/
│       │   │   ├── entity/
│       │   │   │   ├── User.java
│       │   │   │   ├── Project.java
│       │   │   │   └── Task.java
│       │   │   ├── enums/
│       │   │   │   ├── TaskStatus.java
│       │   │   │   └── TaskPriority.java
│       │   │   ├── exception/
│       │   │   │   ├── GlobalExceptionHandler.java
│       │   │   │   └── ResourceNotFoundException.java
│       │   │   ├── repository/
│       │   │   │   ├── UserRepository.java
│       │   │   │   ├── ProjectRepository.java
│       │   │   │   └── TaskRepository.java
│       │   │   ├── security/
│       │   │   │   ├── JwtTokenProvider.java
│       │   │   │   ├── JwtAuthenticationFilter.java
│       │   │   │   └── CustomUserDetailsService.java
│       │   │   └── service/
│       │   │       ├── AuthService.java
│       │   │       ├── ProjectService.java
│       │   │       └── TaskService.java
│       │   └── resources/
│       │       └── application.yml
│       └── test/
│           └── java/com/taskflow/
│               ├── service/
│               └── controller/
├── frontend/
│   ├── package.json
│   ├── tsconfig.json
│   ├── vite.config.ts
│   ├── tailwind.config.js
│   ├── index.html
│   └── src/
│       ├── api/
│       ├── components/
│       ├── contexts/
│       ├── hooks/
│       ├── pages/
│       ├── types/
│       ├── utils/
│       ├── App.tsx
│       ├── main.tsx
│       └── index.css
├── docs/
│   ├── implementation-plan.md
│   └── architecture.md
├── docker-compose.yml (optional)
├── .gitignore
└── README.md
```

---

## 8. Design Pattern Implementation Plan

| Pattern | Usage | Location |
|---------|-------|----------|
| **MVC** | Controller → Service → Repository | Backend core |
| **Repository** | Data access abstraction | Spring Data JPA repos |
| **DTO** | API request/response objects | dto/ package |
| **Builder** | Entity and DTO construction | Lombok @Builder or manual |
| **Singleton** | Spring beans (default scope) | All @Service, @Component |
| **Filter Chain** | JWT authentication filter | Security filter pipeline |
| **Strategy** | Task sorting/filtering | Service layer |
| **Observer** | React Query cache invalidation | Frontend hooks |
| **Provider** | Auth context provider | React Context |
| **Custom Hook** | Reusable stateful logic | Frontend hooks/ |

---

## 9. Testing Strategy

### Backend Tests
- **Unit Tests**: Service layer tests with Mockito mocks for repositories.
- **Integration Tests**: Controller tests with `@WebMvcTest` or `@SpringBootTest`.
- **Security Tests**: Authentication and authorization verification.
- **Coverage Target**: Core business logic (services) must have tests.

### Frontend Tests
- **Component Tests**: Render and interaction testing with React Testing Library.
- **Hook Tests**: Custom hook testing with `renderHook`.
- **Integration Tests**: Page-level tests with mocked API responses.
- **Coverage Target**: Key components (auth forms, task card, kanban board).

### Test Commands
- Backend: `mvn test`
- Frontend: `npm test`

---

## 10. Phase-wise Implementation Roadmap

### Phase 0: Preflight & Documentation ✅
- Inspect workspace and tools
- Create implementation-plan.md
- Create architecture.md
- Set up progress tracker

### Phase 1: Project Initialization
- Initialize Spring Boot backend with Maven
- Initialize React + TypeScript frontend with Vite
- Configure PostgreSQL connection
- Configure environment variables
- Verify both applications compile/start

### Phase 2: Backend Domain
- User, Project, Task entities with JPA annotations
- Enum types (TaskStatus, TaskPriority)
- JPA repositories
- Database schema auto-generation via Hibernate

### Phase 3: Authentication
- AuthController (register, login)
- AuthService with BCrypt hashing
- JWT token generation and validation
- Spring Security configuration
- JwtAuthenticationFilter
- CustomUserDetailsService
- Unit tests for AuthService

### Phase 4: Project Management
- ProjectController (CRUD)
- ProjectService with ownership enforcement
- DTOs for project requests/responses
- Unit tests for ProjectService

### Phase 5: Task Management
- TaskController (CRUD + status + assign)
- TaskService with filtering, sorting, pagination
- Task DTOs
- Project summary endpoint
- Overdue task calculation
- Unit and integration tests

### Phase 6: Frontend Foundation
- Tailwind CSS design system setup
- React Router routing configuration
- Axios instance with JWT interceptor
- AuthContext and useAuth hook
- Protected route component
- Login and Registration pages

### Phase 7: Frontend Application
- Dashboard with project list
- Create project modal
- Kanban board with columns (TODO, IN_PROGRESS, DONE)
- Task cards with priority badges
- Task creation/editing modal
- Filter and sort controls
- Loading, empty, and error states
- Responsive layout

### Phase 8: Integration & Delivery
- Full frontend-backend integration
- End-to-end user journey testing
- Bug fixes
- README with setup instructions
- Docker Compose (optional)
- Final Git history review

---

## 11. Git Commit Strategy

Follow Conventional Commits format:

```
<type>(<scope>): <description>

[optional body]
```

Types: `feat`, `fix`, `docs`, `chore`, `test`, `refactor`, `style`

### Planned Commits
1. `chore: initialize TaskFlow repository`
2. `docs: add implementation plan and architecture`
3. `chore: initialize Spring Boot backend`
4. `chore: initialize React TypeScript frontend`
5. `feat(backend): implement domain entities and repositories`
6. `feat(backend): implement JWT authentication`
7. `test(backend): add authentication service tests`
8. `feat(backend): implement project management`
9. `test(backend): add project service tests`
10. `feat(backend): implement task management`
11. `test(backend): add task service and integration tests`
12. `feat(frontend): set up design system and routing`
13. `feat(frontend): implement authentication pages`
14. `feat(frontend): build project dashboard`
15. `feat(frontend): implement kanban board and task management`
16. `test(frontend): add component tests`
17. `fix: resolve integration issues`
18. `docs: add README and finalize documentation`

---

## 12. Acceptance Criteria

### Must-Have (Mandatory)
- [ ] User registration and login with JWT
- [ ] BCrypt password hashing
- [ ] Project CRUD with ownership enforcement
- [ ] Task CRUD with status management
- [ ] Task filtering, sorting, and pagination
- [ ] Overdue task calculation
- [ ] React frontend with TypeScript
- [ ] Protected routes
- [ ] Projects dashboard
- [ ] Kanban board
- [ ] Task creation and editing
- [ ] Loading, empty, and error states
- [ ] Responsive design
- [ ] Backend unit tests
- [ ] Frontend component tests
- [ ] Multiple meaningful Git commits
- [ ] README with setup instructions
- [ ] Architecture documentation

### Nice-to-Have (Optional)
- [ ] Docker Compose setup
- [ ] Swagger/OpenAPI documentation
- [ ] Drag-and-drop on Kanban board
- [ ] Seed data with sample credentials
- [ ] Email notifications
- [ ] User profile management

---

## 13. Known Assumptions and Risks

### Assumptions
1. PostgreSQL is available locally or via Docker.
2. Single-user project ownership (no team collaboration model beyond task assignment).
3. JWT tokens are short-lived; no refresh token flow required (but can be added).
4. Task assignment is limited to registered users.
5. No file uploads or attachments on tasks.

### Risks
| Risk | Impact | Mitigation |
|------|--------|------------|
| Java/Kotlin deviation not approved | High | Document clearly, seek approval |
| PostgreSQL not available locally | Medium | Provide H2 fallback for dev, Docker Compose |
| Complex drag-and-drop implementation | Low | Click-based status change as fallback |
| Test coverage insufficiency | Medium | Prioritize service layer and key components |

### Environment Assumptions
- Java 17 available on PATH
- Node.js 18+ available
- PostgreSQL accessible on localhost:5432
- Ports 8080 (backend) and 5173 (frontend) available

---

## Progress Tracker

| Phase | Status | Commit Hash | Notes |
|-------|--------|-------------|-------|
| Phase 0 – Preflight & Docs | COMPLETED | 25c351e | Planning documents & architecture established |
| Phase 1 – Project Init | COMPLETED | a447fa1 | Spring Boot 3.2.5 (Java 17) & React 18+ (TS, Vite, Tailwind, RTL) initialized |
| Phase 2 – Backend Domain | COMPLETED | 7530091 | User, Project, Task entities, enums, JPA repositories, test verified |
| Phase 3 – Authentication | COMPLETED | 9d5814f | Registration, Login, BCrypt, JWT provider & filter, Spring Security, tests |
| Phase 4 – Project Management | COMPLETED | 108af5e | Project CRUD, ownership enforcement, DTOs, summary endpoint, service tests |
| Phase 5 – Task Management | COMPLETED | Pending | Task CRUD, status updates, assignment, filtering, sorting, pagination, overdue calculation, service tests |
| Phase 6 – Frontend Foundation | IN_PROGRESS | – | Design system, routing, Axios client with JWT interceptor, AuthContext, protected routes, login & register pages |
| Phase 7 – Frontend Application | NOT_STARTED | – | – |
| Phase 8 – Integration & Delivery | NOT_STARTED | – | – |
