# Healthcare QMS – Dynamic Survey Management System

A production-grade, full-stack Healthcare Quality Management System (QMS) application built with **Java 21 + Spring Boot 3.3**, **React 19 + TypeScript + Vite**, and **PostgreSQL**.

The application allows authorized healthcare administrators to create dynamic survey templates (visually or via JSON import), publish surveys, generate cryptographically secure public links, collect responses from public respondents without requiring login, store all submissions in PostgreSQL within strict ACID transactions, and analyze clinical quality feedback through an interactive analytics dashboard.

---

## 1. Key Features

- **Dynamic Question Engine**: Supports 10 distinct question types:
  - `rating` (1..N star scale or numeric buttons)
  - `single_choice` (radio options)
  - `multiple_choice` (checkboxes)
  - `short_text` (single-line input)
  - `long_text` (multi-line textarea)
  - `dropdown` (select dropdown)
  - `yes_no` (Yes/No toggle)
  - `date` (date picker)
  - `number` (numeric input with bounds)
  - `boolean` (consent/confirmation switch)
- **Visual Builder & Direct JSON Editor**: Toggle between an intuitive question builder and direct JSON editor with real-time two-way synchronization.
- **Immutable Published Versions**: Publishing locks a snapshot in `survey_version` and generates an unpredictable public token (`survey_link`) indexed via SHA-256 hashes.
- **Controlled Link Management**: Repeatedly clicking Publish reuses the existing active link to avoid duplicates. Dedicated link regeneration, link expiration dates, and max response limits are fully supported.
- **Public Respondent Experience**: Mobile-friendly, accessible UI allowing patients and families to submit survey feedback without logging in.
- **Admin Analytics Dashboard**: High-level KPIs, Recharts bar charts for rating distributions, daily completion trends, question-wise statistics, and CSV export.
- **Enterprise Security & Audit**: BCrypt password hashing, JWT access tokens, HttpOnly refresh token rotation, and comprehensive administrative audit logging (`audit_log`).

---

## 2. Technology Stack

| Layer | Technology |
|---|---|
| **Frontend** | React 19, TypeScript, Vite, Tailwind CSS, shadcn/ui inspired components |
| **Routing & Forms** | React Router 7, React Hook Form, Zod |
| **State & API** | Axios, TanStack Query, React Context |
| **Charts & Icons** | Recharts, Lucide React |
| **Backend** | Java 21, Spring Boot 3.3, Spring Security, JWT (jjwt 0.12.6) |
| **Data & ORM** | PostgreSQL, Spring Data JPA, Hibernate, Flyway Migrations |
| **Testing** | JUnit 5, Mockito, Spring Security Test, Vitest, React Testing Library |
| **API Docs** | OpenAPI 3.0 / Swagger UI (`springdoc-openapi`) |

---

## 3. Project Directory Structure

```text
Survey/
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   ├── src/main/java/com/healthcare/survey/
│   │   ├── config/              # OpenApi, DataInitializer, Security config
│   │   ├── controller/          # Auth, AdminSurvey, PublicSurvey REST controllers
│   │   ├── dto/                 # Request/Response data transfer objects
│   │   ├── entity/              # JPA Entities (AppUser, SurveyTemplate, Question, Version, Link, Response, AuditLog)
│   │   ├── exception/           # Custom exceptions & GlobalExceptionHandler
│   │   ├── repository/          # Spring Data JPA repositories
│   │   ├── security/           # JWT provider, filter, UserDetailsService
│   │   └── service/             # Business logic & transactional services
│   └── src/main/resources/
│       ├── application.yml      # PostgreSQL production configuration
│       ├── application-dev.yml  # Local dev profile
│       └── db/migration/        # Flyway SQL migrations (V1 schema, V2 seed)
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   ├── tailwind.config.js
│   ├── src/
│   │   ├── api/                 # Axios client with JWT refresh interceptors
│   │   ├── components/          # UI, Layout, QuestionBuilder, DynamicQuestionRenderer, Modals
│   │   ├── context/             # AuthContext, ToastContext
│   │   ├── pages/               # AdminLogin, Dashboard, SurveyList, Builder, Preview, Responses, Analytics, PublicSurvey
│   │   └── types/               # TypeScript interfaces & discriminated unions
│   └── src/test/                # Vitest & Testing Library suites
├── postman/
│   └── Healthcare_Survey_QMS.postman_collection.json
├── sample-surveys/
│   └── hospital_patient_satisfaction.json
├── docker-compose.yml
├── DEPLOYMENT.md
└── README.md
```

---

## 4. Quick Start (Local Setup)

### Prerequisites
- **Java 21+**
- **Maven 3.9+**
- **Node.js 20+ & npm**
- **PostgreSQL** (or run with dev profile / Docker)

### Default Admin Credentials
- **Username**: `admin`
- **Password**: `Admin@123456`
- **Role**: `ROLE_ADMIN`

---

### Step 1: Run the Backend

#### Option A: Running with local PostgreSQL
Create a database named `healthcare_survey`:
```sql
CREATE DATABASE healthcare_survey;
```
Start the Spring Boot backend:
```bash
cd backend
mvn spring-boot:run
```

#### Option B: Running with H2 In-Memory (No PostgreSQL installation required)
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The backend starts at: **`http://localhost:8080`**  
OpenAPI Swagger UI: **`http://localhost:8080/swagger-ui.html`**

---

### Step 2: Run the Frontend

```bash
cd frontend
npm install --legacy-peer-deps
npm run dev
```

The React frontend starts at: **`http://localhost:5173`**  
Requests to `/api/*` are automatically proxied to the Spring Boot backend.

---

### Option C: Run Everything via Docker Compose
```bash
docker-compose up --build
```
This spins up PostgreSQL on port `5432` and the Spring Boot backend on port `8080`.

---

## 5. End-to-End Acceptance Verification

Follow these steps to verify complete functionality:

1. **Administrator Login**:
   - Open `http://localhost:5173/login`.
   - Enter `admin` / `Admin@123456`.
   - You are redirected to the **Admin Dashboard** (`/admin/dashboard`).
2. **Template Creation via JSON Import**:
   - Navigate to **Surveys** > click **Import JSON**.
   - Click **"Load Sample JSON"** (or paste `sample-surveys/hospital_patient_satisfaction.json`).
   - Click **"Import Template"**.
   - The survey appears in the list with all 5 questions.
3. **Publishing & Link Generation**:
   - Click **"Publish Now"** or **Share Link** icon on the survey.
   - Click **"Publish & Generate Link"**.
   - A unique public URL is generated (e.g. `http://localhost:5173/survey/<token>`).
   - Click **"Copy Link"**.
4. **Public Response Submission**:
   - Open an incognito browser window and navigate to the copied URL.
   - The survey title, description, confidentiality notice, and dynamic questions render without requiring login.
   - Answer the required questions (Waiting time rating, doctor explanation, cleanliness rating).
   - Click **"Submit Survey Response"**.
   - A success confirmation screen appears displaying a **Confirmation Reference ID** (e.g., `RESP-9A8B7C`).
5. **Multiple Independent Responses**:
   - Refresh the page or submit another response with different answers.
   - Both submissions are saved independently in PostgreSQL.
6. **Admin Verification & Analytics**:
   - In your Admin window, open **Responses** (`/admin/surveys/1/responses`) to inspect the submissions.
   - Open **Analytics** (`/admin/surveys/1/analytics`) to see real-time Recharts visualizations and question statistics.
   - Click **"Export CSV"** to download the response records.

---

## 6. Testing

### Run Backend Unit & Integration Tests (27 Tests)
```bash
cd backend
mvn test
```
Covers:
- Template creation, validation rules, rating bounds, choice option validation
- Link publishing, duplicate prevention on repeated publishing, link deactivation
- Public survey retrieval, invalid/expired/deactivated tokens, limit enforcement
- Transactional response submissions and validation
- Spring Security role authorization & 401 handling

### Run Frontend Unit Tests (7 Tests)
```bash
cd frontend
npm test
```
Covers:
- DynamicQuestionRenderer for all question types (rating, single/multiple choice, text, etc.)
- Required field validation and error messaging
- QuestionBuilder additions, updates, and reordering

---

## 7. REST API Reference

### Authentication
- `POST /api/auth/login` – Admin login, issues JWT access token + HttpOnly refresh cookie
- `POST /api/auth/refresh` – Rotates refresh token and issues new access token
- `POST /api/auth/logout` – Clears session and refresh cookie
- `GET /api/auth/me` – Returns authenticated admin profile

### Admin Survey Management
- `POST /api/admin/surveys` – Create new template
- `POST /api/admin/surveys/import` – Import template from JSON
- `GET /api/admin/surveys` – List all surveys with metrics
- `GET /api/admin/surveys/{id}` – Get survey template with questions
- `PUT /api/admin/surveys/{id}` – Update draft survey
- `POST /api/admin/surveys/{id}/publish` – Publish survey snapshot & return public URL
- `POST /api/admin/surveys/{id}/regenerate-link` – Force new public link
- `GET /api/admin/surveys/{id}/links` – List link history
- `PATCH /api/admin/survey-links/{id}/deactivate` – Deactivate link
- `POST /api/admin/surveys/{id}/archive` – Archive survey template
- `GET /api/admin/surveys/{id}/responses` – View submitted responses
- `GET /api/admin/surveys/{id}/analytics` – Question-wise statistics & distributions
- `GET /api/admin/surveys/{id}/export` – Download CSV export
- `GET /api/admin/dashboard/stats` – High-level KPI metrics
- `GET /api/admin/audit-logs` – Compliance audit logs

### Public Survey APIs (No Auth Required)
- `GET /api/public/surveys/{token}` – Retrieve published survey questions by public token
- `POST /api/public/surveys/{token}/responses` – Submit survey answers transactionally

---

## 8. Deployment

See [DEPLOYMENT.md](file:///d:/Healthcare%20projects/Survey/DEPLOYMENT.md) for full cloud deployment guides on **Vercel** (Frontend) and **Render / AWS** (Backend & PostgreSQL).
