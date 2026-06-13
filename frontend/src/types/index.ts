export interface User {
  id: string;
  username: string;
  email: string;
  roles: string[];
}

export interface LogEntry {
  id: string;
  timestamp: string;
  sourceIp: string;
  destinationIp: string;
  destinationPort: number | null;
  eventType: string;
  payload: any;
  severity: 'INFO' | 'WARNING' | 'ERROR' | 'CRITICAL';
  status: string;
  message: string;
  createdAt: string;
}

export interface Alert {
  id: string;
  title: string;
  description: string;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  status: 'OPEN' | 'ACKNOWLEDGED' | 'RESOLVED';
  threatType: string;
  sourceIp: string;
  analyst: User | null;
  createdAt: string;
  updatedAt: string;
  resolvedAt: string | null;
}

export interface Incident {
  id: string;
  title: string;
  description: string;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  status: 'ACTIVE' | 'INVESTIGATING' | 'RESOLVED';
  riskScore: number;
  analyst: User | null;
  alerts: Alert[];
  createdAt: string;
  updatedAt: string;
  closedAt: string | null;
}

export interface Report {
  id: string;
  name: string;
  type: 'PDF_SECURITY' | 'INCIDENT_REPORT' | 'EXEC_SUMMARY';
  s3Url: string;
  createdBy: User | null;
  createdAt: string;
}

export interface AuditLog {
  id: string;
  timestamp: string;
  actor: String;
  action: string;
  resource: string;
  status: string;
  ipAddress: string;
  details: string;
}

export interface DashboardMetrics {
  totalLogs: number;
  activeIncidents: number;
  criticalAlerts: number;
  securityPostureScore: number;
  logsByEventType: Record<string, number>;
  alertsBySeverity: Record<string, number>;
  incidentsBySeverity: Record<string, number>;
  riskScores: {
    userRisk: number;
    assetRisk: number;
    incidentRisk: number;
  };
}

export interface AiReportResponse {
  incidentSummary: string;
  rootCause: string;
  recommendations: string;
  executiveSummary: string;
}
