# Cloud-Native SIEM & AI Security Analyst Platform

An enterprise-grade, high-throughput Security Information and Event Management (SIEM) system with built-in threat correlation and automated AI Security Analyst forensics. Engineered utilizing clean-architecture principles and modern Cloud-Native standards.

---

## Resume-Worthy Project Description

**Principal Security Platform Engineer | Cloud-Native SIEM & AI Security Analyst Platform**
* **High-Throughput Ingestion (Project Loom & JDBC Batching):** Engineered a reactive log ingestion endpoint utilizing Java 21 Virtual Threads (Project Loom) and Spring `JdbcTemplate` batch writes, achieving high-throughput event processing and asynchronous correlation with near-zero thread context-switching overhead.
* **Threat Correlation Engine:** Built a real-time correlation processor analyzing log streams against signatures for brute force attacks, port scans, suspicious IPs, and privilege escalations. Correlated alarms into logical, priority-sorted incidents.
* **AI Security Analyst Forensics:** Integrated Gemini API to perform automated forensic evaluations on open security incidents, generating markdown summaries, root cause explanations, and remediation checklists.
* **Resilient Distributed Services:** Integrated Redis-based token-bucket rate limiting to secure authentication routes, and cached Threat Intelligence indicator feeds to bypass DB roundtrips.
* **Report compiler & AWS Archiver:** Designed an automated PDF reporting service utilizing OpenPDF that compiles reports with AI findings, archiving them securely in AWS S3 and notifying operators of critical alerts via AWS SNS.

---

## Technology Stack Justifications

* **Java 21 (Virtual Threads):** Essential for microsecond-scale log ingestion concurrency without choking on OS thread pools.
* **Spring Boot 3 & Security 6:** Enforces robust role-based access controls (RBAC) and secures administrative endpoints.
* **PostgreSQL (JSONB & GIN Indexes):** Provides ACID reliability for alert management, while permitting schema-less JSON log payload parsing with indexes.
* **Redis:** Delivers sub-millisecond lookups for rate limiting and threat feed lookups.
* **React + TS + Tailwind (Vite):** A professional, dark-themed dashboard that keeps SOC analysts updated with zero-refresh metrics.
* **AWS S3, SNS, CloudWatch:** Enterprise standards for report storage, notifications, and telemetry.

---

## Directory Layout Overview

```
├── backend/                  # Spring Boot Maven application
│   ├── src/main/java/        # Clean Architecture package structure
│   │   └── com/amazon/siem/
│   │       ├── config/       # Security, Redis, AWS, AI setups
│   │       ├── controller/   # REST Controllers (Auth, Logs, Incidents, Reports)
│   │       ├── dto/          # Data Transfer Objects
│   │       ├── model/        # JPA Entities (Lombok-free, JDK 25 compliant)
│   │       ├── service/      # Ingestion, Threat Detection, AI Analyst services
│   │       └── util/         # JWT and Rate limiter utilities
│   └── src/main/resources/
│       ├── application.yml   # Configuration properties
│       └── schema.sql        # Database DDL schema and indexes
├── frontend/                 # React TS Vite frontend web application
│   ├── src/
│   │   ├── components/       # Dashboard, Alerts, Incidents workspace, simulator
│   │   ├── context/          # Auth context and Token storage
│   │   ├── services/         # API fetch client
│   │   └── types/            # TypeScript model interfaces
│   └── index.html
├── docker/                   # Docker configurations
│   ├── docker-compose.yml    # Full service orchestration
│   ├── backend.Dockerfile    # Multi-stage secure build
│   └── frontend.Dockerfile   # Nginx SPA release builder
└── docs/                     # Guides and architectural reviews
    ├── architecture.md       # Diagram and design decisions
    ├── api.md                # Endpoint documentation
    ├── deployment.md         # Running database and apps
    └── aws-setup.md          # AWS S3, SNS, CloudWatch configs
```

---

## Quick Start (Docker Compose)

Launch the entire SIEM platform (PostgreSQL, Redis, Backend, and Frontend) in seconds:

1. **Set Environment Variables (Optional):**
   ```bash
   export AI_API_KEY="your-gemini-api-key"
   ```
2. **Launch Services:**
   ```bash
   docker-compose -f docker/docker-compose.yml up --build -d
   ```
3. **Access Services:**
   * **Security Dashboard UI:** Open `http://localhost` (Port 80)
   * **REST API Endpoints:** Mapped to `http://localhost:8080`
   * **Database Engine:** Port `5432`, Username: `postgres`, Password: `postgrespassword`
   * **Redis Cache:** Port `6379`
