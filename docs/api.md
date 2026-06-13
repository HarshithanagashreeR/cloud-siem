# REST API Documentation

The backend services expose standard REST endpoints under `/api/v1`.

---

## 1. Authentication Service

### Register Operator
* **Route:** `POST /api/v1/auth/signup`
* **Access:** Anonymous
* **Request Body:**
  ```json
  {
    "username": "sec_analyst_r",
    "email": "r@amazon.com",
    "role": ["analyst"],
    "password": "strongPassword123"
  }
  ```
* **Success Response (200 OK):**
  ```json
  {
    "message": "User registered successfully!"
  }
  ```

### Sign In Operator
* **Route:** `POST /api/v1/auth/signin`
* **Access:** Anonymous (Rate limited)
* **Request Body:**
  ```json
  {
    "username": "sec_analyst_r",
    "password": "strongPassword123"
  }
  ```
* **Success Response (200 OK):**
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiJ9.ey...",
    "type": "Bearer",
    "id": "e9c1542f-87a3-41a4-912f-2c7c569f6db2",
    "username": "sec_analyst_r",
    "email": "r@amazon.com",
    "roles": ["ROLE_ANALYST"]
  }
  ```

---

## 2. Log Ingestion Service

### Ingest Single Log
* **Route:** `POST /api/v1/logs/ingest`
* **Access:** Anonymous / Forwarder Agent
* **Request Body:**
  ```json
  {
    "timestamp": "2026-06-13T12:00:00Z",
    "sourceIp": "192.168.1.144",
    "destinationIp": "10.0.0.5",
    "destinationPort": 22,
    "eventType": "PORT_SCAN_PROBE",
    "payload": { "target_port": 22 },
    "severity": "INFO",
    "message": "Probe hit port: 22"
  }
  ```
* **Success Response (200 OK):** returns the persisted LogEntry entity.

### Ingest Bulk Logs
* **Route:** `POST /api/v1/logs/ingest/bulk`
* **Access:** Anonymous / Forwarder Agent
* **Request Body:** Array of Log objects.
* **Success Response (200 OK):**
  ```json
  {
    "message": "Ingested 12 logs successfully."
  }
  ```

---

## 3. Alerts Feed

### Get All Alerts
* **Route:** `GET /api/v1/alerts`
* **Headers:** `Authorization: Bearer <token>`
* **Success Response (200 OK):** Array of Alert objects.

### Acknowledge Alert
* **Route:** `PUT /api/v1/alerts/{id}/acknowledge`
* **Access:** Analyst, Admin
* **Headers:** `Authorization: Bearer <token>`
* **Success Response (200 OK):** Returns the updated Alert object.

### Resolve Alert
* **Route:** `PUT /api/v1/alerts/{id}/resolve`
* **Access:** Analyst, Admin
* **Headers:** `Authorization: Bearer <token>`
* **Success Response (200 OK):** Returns the resolved Alert object.

---

## 4. Incidents Workspace

### Get All Incidents
* **Route:** `GET /api/v1/incidents`
* **Headers:** `Authorization: Bearer <token>`
* **Success Response (200 OK):** Array of Incident objects.

### Assign Investigator
* **Route:** `PUT /api/v1/incidents/{id}/assign`
* **Access:** Analyst, Admin
* **Headers:** `Authorization: Bearer <token>`
* **Success Response (200 OK):** Returns the Incident object (status changes to `INVESTIGATING`).

### Run AI Security Analyst
* **Route:** `POST /api/v1/incidents/{id}/analyze`
* **Access:** Analyst, Admin
* **Headers:** `Authorization: Bearer <token>`
* **Success Response (200 OK):**
  ```json
  {
    "incidentSummary": "Automated port scan identified targeting critical subnets...",
    "rootCause": "### Network Reconnaissance\nActor host connected to...",
    "recommendations": "### Action Checklist\n1. Block host IP...\n2. Check subnets...",
    "executiveSummary": "### Executive Security Overview\n\n**Severity Level:** CRITICAL..."
  }
  ```

---

## 5. Reporting Module

### Generate PDF Incident Report
* **Route:** `POST /api/v1/reports/incident/{incidentId}`
* **Access:** Analyst, Admin
* **Headers:** `Authorization: Bearer <token>`
* **Success Response (200 OK):** Returns the Report object containing the S3 file URL.

---

## 6. Audit & Dashboard

### Get Audit Logs (Admin Only)
* **Route:** `GET /api/v1/audit?page=0&size=15`
* **Access:** Admin
* **Headers:** `Authorization: Bearer <token>`
* **Success Response (200 OK):** Spring Page object of AuditLog entries.

### Get Dashboard Metrics
* **Route:** `GET /api/v1/dashboard/metrics`
* **Headers:** `Authorization: Bearer <token>`
* **Success Response (200 OK):**
  ```json
  {
    "totalLogs": 1542039,
    "activeIncidents": 4,
    "criticalAlerts": 2,
    "securityPostureScore": 82,
    "logsByEventType": {
      "LOGIN_FAILED": 48,
      "LOGIN_SUCCESS": 94520,
      "PORT_SCAN": 2
    },
    "alertsBySeverity": {
      "CRITICAL": 2,
      "HIGH": 5
    },
    "riskScores": {
      "userRisk": 15,
      "assetRisk": 30,
      "incidentRisk": 45
    }
  }
  ```
