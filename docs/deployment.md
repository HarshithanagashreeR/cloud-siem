# Platform Deployment Guide

Follow these instructions to deploy and run the SIEM platform locally in development mode.

---

## 1. Prerequisites
Ensure the following tools are installed:
* **Java 21 or 25** (OpenJDK recommended)
* **Maven 3.8+**
* **Node.js v20+ & npm**
* **Docker & Docker Compose** (highly recommended for starting PostgreSQL and Redis)

---

## 2. Deploy Databases (Docker)

If you don't have local PostgreSQL or Redis engines running, start them using Docker:
```bash
docker run --name siem-postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=siem -p 5432:5432 -d postgres:15-alpine
docker run --name siem-redis -p 6379:6379 -d redis:7-alpine
```

---

## 3. Run Backend (Spring Boot)

1. **Verify Database connection** inside `backend/src/main/resources/application.yml`.
2. **Inject Gemini API Key (Optional):**
   ```bash
   export AI_API_KEY="your-actual-api-key"
   ```
3. **Run compiler & start application:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   * Spring Boot will start on port `8080`.
   * On startup, the DDL schema in `schema.sql` is automatically parsed and tables/indexes are created.

---

## 4. Run Frontend (React Development)

1. **Navigate to frontend directory:**
   ```bash
   cd frontend
   ```
2. **Install Node Packages:**
   ```bash
   npm install
   ```
3. **Start local development server:**
   ```bash
   npm run dev
   ```
   * The Vite dev server will start on `http://localhost:5173`.
   * Open this URL in your browser.

---

## 5. Walkthrough: Threat Testing & Ingestion

Once both the backend and frontend are active:

1. **Register User Operator:**
   * Go to `http://localhost:5173/signup`.
   * Create an account (e.g. `analyst_user`, `user@example.com`, Password: `password123`) and select **Analyst** scope.
2. **Sign In:**
   * Login using the credentials on `http://localhost:5173/login`.
3. **Simulate a Port Scan Threat:**
   * Go to the **Ingestion Simulator** panel in the sidebar.
   * Click the **Simulate Recon: Port Scan** preset.
   * Review the generated JSON payloads in the editor.
   * Click **Ingest Payload**.
4. **Inspect Alarms & Correlation:**
   * Go to the **Alerts Feed**. You will see a `CRITICAL` alert titled **Reconnaissance: Port Scan Detected**.
   * Go to **Incidents Panel**. You will find an active incident created automatically for this threat.
5. **Run AI Analyst:**
   * Click the incident to open the workspace.
   * Click **Run Analysis** to call the AI Analyst.
   * Review the generated summary, root cause explanation, and security recommendations.
6. **Compile S3 Report Archive:**
   * Click **Compile PDF Posture Report**.
   * The system generates a PDF report containing the alerts list and the AI recommendations, uploads it to S3, and returns the download link!
