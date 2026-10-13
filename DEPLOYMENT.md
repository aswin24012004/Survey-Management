# Deployment Guide – Healthcare QMS Dynamic Survey Management System

This document outlines deployment configurations for the full-stack Healthcare Survey application across cloud environments.

---

## 1. Architecture Overview

- **Frontend**: React + TypeScript + Vite SPA deployed on **Vercel**
- **Backend**: Java 21 + Spring Boot 3.3 REST API deployed on **Render** or **AWS (ECS / Elastic Beanstalk)**
- **Database**: Managed PostgreSQL (Render PostgreSQL, AWS RDS, or Supabase)

---

## 2. Frontend Deployment – Vercel

### Step 1: Push Code to Git
Ensure your `frontend` directory is pushed to GitHub or GitLab.

### Step 2: Import Project in Vercel
1. Log in to [vercel.com](https://vercel.com) and click **"Add New Project"**.
2. Select your repository.
3. Set **Root Directory** to `frontend`.
4. Build settings:
   - **Framework Preset**: Vite
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
   - **Install Command**: `npm install --legacy-peer-deps`

### Step 3: Configure Environment Variables
In the Vercel Project Settings > Environment Variables, add:
```env
VITE_API_BASE_URL=https://your-backend-domain.onrender.com/api
```

### Step 4: SPA Routing Fallback
A `frontend/vercel.json` file is already provided to redirect all routes to `index.html`:
```json
{
  "rewrites": [
    { "source": "/(.*)", "destination": "/index.html" }
  ]
}
```

---

## 3. Backend Deployment – Render

### Step 1: Provision Managed PostgreSQL on Render
1. In Render Dashboard, click **New +** > **PostgreSQL**.
2. Name: `healthcare-survey-db`
3. Database: `healthcare_survey`
4. User: `postgres`
5. Note down the **Internal Database URL** and **External Connection String**.

### Step 2: Deploy Backend Web Service
1. Click **New +** > **Web Service**.
2. Connect your Git repository.
3. Configure the service:
   - **Root Directory**: `backend`
   - **Runtime**: `Docker` (Render will automatically detect `backend/Dockerfile`), or `Native Java` with Maven:
     - Build Command: `mvn clean package -DskipTests`
     - Start Command: `java -jar target/survey-service-1.0.0.jar`
   - **Instance Type**: Starter / Standard

### Step 3: Configure Environment Variables in Render
Add the following under **Environment**:
```env
PORT=8080
DB_URL=jdbc:postgresql://<render-db-host>:5432/healthcare_survey
DB_USERNAME=<db-user>
DB_PASSWORD=<db-password>
JWT_SECRET=<generate-random-256-bit-key>
JWT_ACCESS_EXPIRATION_MS=900000
JWT_REFRESH_EXPIRATION_MS=604800000
CORS_ALLOWED_ORIGINS=https://your-app.vercel.app,http://localhost:5173
PUBLIC_SURVEY_BASE_URL=https://your-app.vercel.app/survey
```

Flyway will automatically execute migrations `V1__create_initial_schema.sql` and `V2__seed_admin_and_sample_survey.sql` upon container startup.

---

## 4. Backend Deployment – AWS (ECS / Elastic Beanstalk)

### Option A: AWS Elastic Beanstalk (Corretto 21)
1. Initialize EB CLI in `backend/`:
   ```bash
   eb init -p "Corretto 21" healthcare-survey-api
   ```
2. Build the JAR:
   ```bash
   mvn clean package -DskipTests
   ```
3. Create environment:
   ```bash
   eb create healthcare-survey-env
   ```
4. Set Environment Properties in AWS Console > Software Configuration:
   - `DB_URL`: JDBC URL to AWS RDS PostgreSQL
   - `DB_USERNAME`, `DB_PASSWORD`
   - `JWT_SECRET`
   - `CORS_ALLOWED_ORIGINS`
   - `PUBLIC_SURVEY_BASE_URL`

### Option B: AWS ECS Fargate with Docker
1. Build & Push Docker image to Amazon ECR:
   ```bash
   docker build -t <aws_account_id>.dkr.ecr.<region>.amazonaws.com/healthcare-survey:latest ./backend
   docker push <aws_account_id>.dkr.ecr.<region>.amazonaws.com/healthcare-survey:latest
   ```
2. Create ECS Task Definition with environment variables mapped to AWS Secrets Manager.
3. Deploy behind an Application Load Balancer with HTTPS enabled.

---

## 5. Security & Healthcare Compliance Checklist

- [x] Enforce HTTPS in production via TLS termination on Vercel and Render/ALB.
- [x] Configure HttpOnly, Secure, SameSite=Lax cookies for JWT refresh tokens.
- [x] Never store plain administrative passwords or raw security keys in git.
- [x] Restrict database access to private VPC/network connections.
- [x] Enable Flyway migrations for auditable database versioning.
- [x] Public tokens generated with cryptographically secure random bytes and indexed via SHA-256 hashes.
