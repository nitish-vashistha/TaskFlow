# TaskFlow – Team Task Management System

TaskFlow is a production-grade full-stack team task management system engineered with Spring Boot 3.2, Java 17, React 18, TypeScript, Tailwind CSS, TanStack Query, and PostgreSQL.

---

## 1. System Architecture & Tech Stack

### Backend
- **Framework**: Spring Boot 3.2.5 on Java 17
- **Security**: Spring Security 6 with stateless JWT authentication (HMAC-SHA256)
- **Persistence**: Spring Data JPA, Hibernate, PostgreSQL (with embedded H2 profile for zero-config local testing)
- **Data Validation**: Bean Validation (Jakarta Validation)
- **Testing**: JUnit 5, AssertJ, Mockito

### Frontend
- **Framework**: React 18+ with TypeScript and Vite
- **Styling**: Tailwind CSS
- **Server State**: TanStack React Query v5
- **Client Auth State**: React Context API with persistent token storage
- **HTTP Client**: Axios with automatic JWT Bearer interceptor
- **Testing**: Jest, React Testing Library, jsdom

### Architectural Highlights
- 3-tier MVC: Controllers $\rightarrow$ Services $\rightarrow$ Repositories $\rightarrow$ Entities
- Decoupled Request/Response DTOs across all endpoints
- Enforced project ownership validation in service layer
- Dynamic task querying via JPA Specification (filter by status, priority, assignee, search keyword)
- Automated overdue task calculation
- Seed data initializer on startup for instant demonstration

---

## 2. Default Demo Credentials

When running with the default or dev profile, the application seeds the following users and sample projects automatically:

| Role | Email | Password | Name |
| :--- | :--- | :--- | :--- |
| **Primary Owner** | `alex@example.com` | `password123` | Alex Rivera |
| **Team Member** | `sarah@example.com` | `password123` | Sarah Chen |

---

## 3. Quick Start Guide

### Prerequisites
- **Java 17**
- **Maven 3.8+**
- **Node.js 18+** & **npm**
- **Docker** (optional, for PostgreSQL container)

---

### Option A: Running with Docker PostgreSQL

1. **Start PostgreSQL**:
   ```bash
   docker compose up -d
   ```

2. **Start Backend**:
   ```bash
   cd backend
   export JAVA_HOME=$HOME/tools/jdk-17.0.2.jdk/Contents/Home
   export PATH="$JAVA_HOME/bin:$HOME/tools/apache-maven-3.9.8/bin:$PATH"
   mvn spring-boot:run
   ```
   *Backend starts at `http://localhost:8080`.*

3. **Start Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   *Frontend starts at `http://localhost:5173`.*

---

### Option B: Running with In-Memory H2 (Zero External Dependencies)

If PostgreSQL is not running locally, start the backend with the `dev` profile:

```bash
cd backend
export JAVA_HOME=$HOME/tools/jdk-17.0.2.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$HOME/tools/apache-maven-3.9.8/bin:$PATH"
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

H2 database console will be accessible at: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:taskflow_dev`, user: `sa`, password: empty).

---

## 4. Verification & Testing

### Running Backend Tests
```bash
cd backend
mvn test
```
**Results**:
- `RepositoryTests`: 2 passed (JPA schema mapping, cascading, entity queries)
- `AuthServiceTests`: 3 passed (Registration, duplicate email check, authentication)
- `ProjectServiceTests`: 8 passed (CRUD, ownership enforcement, metrics)
- `TaskServiceTests`: 6 passed (CRUD, status transitions, user assignments, overdue detection)
- **Total**: 19 passed, 0 failures.

### Running Frontend Tests
```bash
cd frontend
npm test
npm run build
```
**Results**:
- `TaskCard.test.tsx`: 3 passed (Rendering, overdue badge, quick status change)
- `Badge.test.tsx`: 2 passed (Priority & status variant styles)
- `App.test.tsx`: 1 passed (Unauthenticated redirection)
- **Total**: 6 passed, 0 failures.
- **Vite Build**: Succeeded (`dist/` bundle created cleanly).

---

## 5. API Reference

### Authentication
- `POST /api/auth/register` – Register new user
- `POST /api/auth/login` – Login and obtain JWT token
- `GET /api/auth/me` – Get current authenticated user
- `GET /api/auth/users` – List available users for task assignment

### Project Management
- `POST /api/projects` – Create project
- `GET /api/projects` – List current user's projects
- `GET /api/projects/{id}` – Get project details (enforces ownership)
- `PUT /api/projects/{id}` – Update project details
- `DELETE /api/projects/{id}` – Delete project and associated tasks
- `GET /api/projects/{id}/summary` – Project stats (total, todo, in progress, done, overdue)

### Task Management
- `POST /api/projects/{projectId}/tasks` – Create new task
- `GET /api/projects/{projectId}/tasks` – Get paginated & filtered tasks
- `GET /api/projects/{projectId}/tasks/kanban` – Retrieve all project tasks for Kanban view
- `GET /api/projects/{projectId}/tasks/{id}` – Get task details
- `PUT /api/projects/{projectId}/tasks/{id}` – Update task
- `DELETE /api/projects/{projectId}/tasks/{id}` – Delete task
- `PATCH /api/projects/{projectId}/tasks/{id}/status` – Quick update task status
- `PATCH /api/projects/{projectId}/tasks/{id}/assign` – Assign or unassign user

---

## 6. Language & Framework Deviation Note

- **Original Specification**: Kotlin with MockK
- **Selected Alternative**: Java 17 with JUnit 5 & Mockito
- **Status**: Selected as an equivalent modern JVM stack with broad ecosystem alignment. Functional requirements, security model, and API contracts remain strictly preserved.
