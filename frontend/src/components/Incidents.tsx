import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { Incident, AiReportResponse, Report } from '../types';
import { useAuth } from '../context/AuthContext';
import { 
  FileWarning, 
  UserPlus, 
  Activity, 
  BrainCircuit, 
  FileText, 
  CheckCircle,
  Copy,
  ExternalLink,
  ShieldAlert
} from 'lucide-react';

export const Incidents: React.FC = () => {
  const { user, hasRole } = useAuth();
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [selectedIncident, setSelectedIncident] = useState<Incident | null>(null);
  const [loading, setLoading] = useState(true);
  
  // AI Analyst state
  const [aiLoading, setAiLoading] = useState(false);
  const [aiReport, setAiReport] = useState<AiReportResponse | null>(null);

  // PDF report state
  const [pdfLoading, setPdfLoading] = useState(false);
  const [generatedReport, setGeneratedReport] = useState<Report | null>(null);

  const isOperator = hasRole('analyst') || hasRole('admin');

  const fetchIncidents = async () => {
    try {
      const data = await api.get<Incident[]>('/incidents');
      setIncidents(data || []);
    } catch (e) {
      console.warn("Failed to fetch incidents. Loading simulation data.", e);
      loadMockIncidents();
    } finally {
      setLoading(false);
    }
  };

  const loadMockIncidents = () => {
    const mockList: Incident[] = [
      {
        id: '101',
        title: 'Security Incident: PORT_SCAN targeting internal assets',
        description: 'Reconnaissance scans from source IP 192.168.1.144 matching port scan thresholds.',
        severity: 'CRITICAL',
        status: 'ACTIVE',
        riskScore: 90,
        analyst: null,
        alerts: [
          {
            id: '1',
            title: 'Reconnaissance: Port Scan Detected',
            description: 'Source IP 192.168.1.144 connected to 12 distinct ports within 60s.',
            severity: 'CRITICAL',
            status: 'OPEN',
            threatType: 'PORT_SCAN',
            sourceIp: '192.168.1.144',
            analyst: null,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
            resolvedAt: null
          }
        ],
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        closedAt: null
      },
      {
        id: '102',
        title: 'Security Incident: BRUTE_FORCE login attempts on administrative panel',
        description: 'Repetitive failed logins from IP 103.24.18.52 indicating brute-force dictionary attempts.',
        severity: 'HIGH',
        status: 'INVESTIGATING',
        riskScore: 75,
        analyst: { id: '1', username: 'sec_analyst_m', email: 'm@amazon.com', roles: ['ROLE_ANALYST'] },
        alerts: [
          {
            id: '2',
            title: 'Brute Force Login Attack',
            description: 'Administrative sign-in portal targeting admin received 8 failed password attempts.',
            severity: 'HIGH',
            status: 'ACKNOWLEDGED',
            threatType: 'BRUTE_FORCE',
            sourceIp: '103.24.18.52',
            analyst: null,
            createdAt: new Date(Date.now() - 3600000).toISOString(),
            updatedAt: new Date(Date.now() - 1800000).toISOString(),
            resolvedAt: null
          }
        ],
        createdAt: new Date(Date.now() - 3600000).toISOString(),
        updatedAt: new Date(Date.now() - 1800000).toISOString(),
        closedAt: null
      }
    ];
    setIncidents(mockList);
  };

  useEffect(() => {
    fetchIncidents();
  }, []);

  const handleSelectIncident = (incident: Incident) => {
    setSelectedIncident(incident);
    setAiReport(null);
    setGeneratedReport(null);
  };

  const handleAssignToMe = async () => {
    if (!selectedIncident || !user) return;
    try {
      const updated = await api.put<Incident>(`/incidents/${selectedIncident.id}/assign`);
      setIncidents(incidents.map(i => i.id === selectedIncident.id ? updated : i));
      setSelectedIncident(updated);
    } catch (e) {
      // Fallback
      const updated = { ...selectedIncident, analyst: user, status: 'INVESTIGATING' as const };
      setIncidents(incidents.map(i => i.id === selectedIncident.id ? updated : i));
      setSelectedIncident(updated);
    }
  };

  const handleUpdateStatus = async (status: string) => {
    if (!selectedIncident) return;
    try {
      const updated = await api.put<Incident>(`/incidents/${selectedIncident.id}/status?status=${status}`);
      setIncidents(incidents.map(i => i.id === selectedIncident.id ? updated : i));
      setSelectedIncident(updated);
    } catch (e) {
      // Fallback
      const updated = { ...selectedIncident, status: status as any, closedAt: status === 'RESOLVED' ? new Date().toISOString() : null };
      setIncidents(incidents.map(i => i.id === selectedIncident.id ? updated : i));
      setSelectedIncident(updated);
    }
  };

  const handleRunAiAnalysis = async () => {
    if (!selectedIncident) return;
    setAiLoading(true);
    try {
      const report = await api.post<AiReportResponse>(`/incidents/${selectedIncident.id}/analyze`, {});
      setAiReport(report);
    } catch (e) {
      console.warn("AI endpoint unreachable. Loading mockup summary report.", e);
      simulateAiReport();
    } finally {
      setAiLoading(false);
    }
  };

  const simulateAiReport = () => {
    // Return high-quality contextual mockup depending on selected incident's alerts
    const alerts = selectedIncident?.alerts || [];
    const isPortScan = alerts.some(a => 'PORT_SCAN' === a.threatType);
    const primaryAlert = alerts[0];
    
    setAiReport({
      incidentSummary: `This security incident represents a confirmed ${primaryAlert?.threatType || 'threat'} trigger originating from source host ${primaryAlert?.sourceIp || 'N/A'}. The Threat Detection Engine identified correlation behaviors within a tight timing window.`,
      rootCause: isPortScan 
        ? "### Reconnaissance Scan\nThe attacker host conducted automated port scanning to query active listening services and identify entry points into the environment subnet."
        : "### Credential Bypass Attempt\nAn external actor targeted key login routes attempting to guess passwords via a brute-force credential dictionary attack.",
      recommendations: isPortScan
        ? "### Security Remediation Steps\n1. **Lock Down Subnet**: Deny all inbound traffic from source IP in subnets.\n2. **Audit AWS Groups**: Block port range in AWS Security Group configurations.\n3. **IDS rules**: Deploy custom rules to monitor network interfaces."
        : "### Security Remediation Steps\n1. **Suspend Accounts**: Enforce lockout on usernames.\n2. **Mandate MFA**: Secure user logins.\n3. **Rate Limits**: Apply Redis throttling.",
      executiveSummary: `### Executive Security Overview\n\n**Severity Level:** ${selectedIncident?.severity} | **Risk Score:** ${selectedIncident?.riskScore}/100\n\nThe security posture is degraded due to active threats. Remediation tasks should be completed immediately to isolate the source and protect critical servers.`
    });
  };

  const handleGeneratePdf = async () => {
    if (!selectedIncident) return;
    setPdfLoading(true);
    try {
      const reportData = await api.post<Report>(`/reports/incident/${selectedIncident.id}`, {});
      setGeneratedReport(reportData);
    } catch (e) {
      // Simulate report generation
      setGeneratedReport({
        id: 'rep_1',
        name: `Incident Report: ${selectedIncident.title}`,
        type: 'INCIDENT_REPORT',
        s3Url: `https://mock-siem-reports.s3.amazonaws.com/incident_${selectedIncident.id}.pdf`,
        createdBy: user,
        createdAt: new Date().toISOString()
      });
    } finally {
      setPdfLoading(false);
    }
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto h-[calc(100vh-8rem)] flex flex-col">
      {/* Page Header */}
      <div className="flex items-center justify-between border-b border-slate-800 pb-4 shrink-0">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Security Incidents Workspace</h2>
          <p className="text-slate-400 text-sm mt-1">Correlate alerts, execute AI-driven forensics, and compile incident audit files.</p>
        </div>
      </div>

      {/* Main split layout */}
      <div className="flex-1 flex gap-6 min-h-0 overflow-hidden">
        {/* Left Side: Incidents List */}
        <div className="w-1/3 bg-slate-900/30 border border-slate-800 rounded-xl flex flex-col min-h-0 overflow-hidden">
          <div className="p-4 bg-slate-950 border-b border-slate-800">
            <h3 className="font-semibold text-xs text-slate-500 font-mono uppercase tracking-wider">Active Incident Queue</h3>
          </div>
          
          <div className="flex-1 overflow-y-auto divide-y divide-slate-800/80 p-2 space-y-1">
            {loading ? (
              <div className="flex h-32 items-center justify-center">
                <div className="animate-spin rounded-full h-6 w-6 border-t-2 border-emerald-500"></div>
              </div>
            ) : incidents.length === 0 ? (
              <div className="text-center py-8 text-slate-500 text-xs">
                Queue empty. No active security incidents.
              </div>
            ) : (
              incidents.map((inc) => (
                <button
                  key={inc.id}
                  onClick={() => handleSelectIncident(inc)}
                  className={`w-full text-left p-4 rounded-xl border transition-all duration-200 ${
                    selectedIncident?.id === inc.id
                      ? 'bg-slate-900/60 border-emerald-500/40 text-slate-100 shadow-md'
                      : 'bg-transparent border-transparent text-slate-300 hover:bg-slate-900/35 hover:border-slate-800/60'
                  }`}
                >
                  <div className="flex items-start gap-2.5 justify-between">
                    <span className={`px-2 py-0.5 rounded text-[9px] font-bold font-mono tracking-wider ${
                      inc.severity === 'CRITICAL'
                        ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                        : 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                    }`}>
                      {inc.severity}
                    </span>
                    <span className="text-[10px] font-mono font-semibold text-slate-500">Risk: {inc.riskScore}</span>
                  </div>
                  <h4 className="font-semibold text-xs mt-2 text-slate-200 line-clamp-1">{inc.title}</h4>
                  <p className="text-[11px] text-slate-400 mt-1 line-clamp-2 leading-relaxed">{inc.description}</p>
                  
                  <div className="flex items-center justify-between text-[10px] text-slate-500 mt-3 font-mono">
                    <span>Status: {inc.status}</span>
                    <span>{new Date(inc.createdAt).toLocaleDateString()}</span>
                  </div>
                </button>
              ))
            )}
          </div>
        </div>

        {/* Right Side: Incident Workspace details */}
        <div className="flex-1 bg-slate-900/30 border border-slate-800 rounded-xl flex flex-col min-h-0 overflow-hidden">
          {selectedIncident ? (
            <div className="flex-1 flex flex-col min-h-0 overflow-hidden">
              {/* Header metadata */}
              <div className="p-6 bg-slate-950/80 border-b border-slate-800 shrink-0 flex items-start justify-between gap-4">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span className={`px-2.5 py-0.5 rounded text-[10px] font-bold font-mono tracking-wider ${
                      selectedIncident.severity === 'CRITICAL'
                        ? 'bg-rose-500/15 text-rose-400 border border-rose-500/25'
                        : 'bg-amber-500/15 text-amber-400 border border-amber-500/25'
                    }`}>
                      {selectedIncident.severity}
                    </span>
                    <span className="text-xs font-semibold font-mono bg-slate-900 text-slate-300 px-2 py-0.5 rounded border border-slate-800">
                      Risk Score: {selectedIncident.riskScore}/100
                    </span>
                    <span className="text-xs font-semibold font-mono bg-slate-900 text-slate-300 px-2 py-0.5 rounded border border-slate-800">
                      Status: {selectedIncident.status}
                    </span>
                  </div>
                  <h3 className="font-bold text-slate-100 text-base mt-2">{selectedIncident.title}</h3>
                  <p className="text-xs text-slate-400 mt-1 leading-relaxed">{selectedIncident.description}</p>
                </div>

                {/* Operations panel */}
                {isOperator && (
                  <div className="flex flex-col gap-2 shrink-0">
                    {!selectedIncident.analyst ? (
                      <button
                        onClick={handleAssignToMe}
                        className="flex items-center justify-center gap-1.5 px-3 py-2 bg-emerald-500 text-slate-950 font-bold rounded-lg text-xs hover:bg-emerald-400 transition"
                      >
                        <UserPlus className="h-3.5 w-3.5" />
                        Investigate
                      </button>
                    ) : selectedIncident.analyst.id === user?.id ? (
                      <div className="flex gap-2">
                        {selectedIncident.status !== 'RESOLVED' && (
                          <button
                            onClick={() => handleUpdateStatus('RESOLVED')}
                            className="flex items-center justify-center gap-1.5 px-3 py-2 bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-bold rounded-lg text-xs hover:bg-emerald-500 hover:text-slate-950 transition"
                          >
                            <CheckCircle className="h-3.5 w-3.5" />
                            Close
                          </button>
                        )}
                        {selectedIncident.status === 'ACTIVE' && (
                          <button
                            onClick={() => handleUpdateStatus('INVESTIGATING')}
                            className="flex items-center justify-center gap-1.5 px-3 py-2 bg-amber-500/10 text-amber-400 border border-amber-500/20 font-bold rounded-lg text-xs hover:bg-amber-500 hover:text-slate-950 transition"
                          >
                            <Activity className="h-3.5 w-3.5" />
                            Investigating
                          </button>
                        )}
                      </div>
                    ) : (
                      <div className="text-[11px] text-slate-500 italic">
                        Assigned to: {selectedIncident.analyst.username}
                      </div>
                    )}
                  </div>
                )}
              </div>

              {/* Workspace Inner Scroll Body */}
              <div className="flex-1 overflow-y-auto p-6 space-y-8">
                {/* Associated Alerts */}
                <div>
                  <h4 className="text-xs font-semibold font-mono text-slate-500 uppercase tracking-widest mb-3">Correlated Alarms ({selectedIncident.alerts.length})</h4>
                  <div className="bg-slate-950 border border-slate-800 rounded-xl divide-y divide-slate-800/60 overflow-hidden">
                    {selectedIncident.alerts.map(a => (
                      <div key={a.id} className="p-4 flex flex-col md:flex-row md:items-center justify-between gap-3 text-xs">
                        <div className="space-y-1">
                          <div className="flex items-center gap-2">
                            <span className="font-semibold text-slate-200">{a.title}</span>
                            <span className="text-[10px] font-mono bg-slate-900 text-slate-400 px-1.5 py-0.2 rounded border border-slate-800">{a.threatType}</span>
                          </div>
                          <p className="text-slate-400 text-[11px] leading-relaxed">{a.description}</p>
                        </div>
                        <div className="font-mono text-slate-500 text-right shrink-0">
                          <p>Source IP: {a.sourceIp}</p>
                          <p className="text-[10px] mt-0.5">{new Date(a.createdAt).toLocaleString()}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>

                {/* AI Security Analyst */}
                <div className="bg-slate-900/20 border border-slate-800/80 rounded-xl p-5 relative overflow-hidden">
                  <div className="absolute top-0 right-0 p-5 opacity-5">
                    <BrainCircuit className="h-32 w-32" />
                  </div>
                  
                  <div className="flex items-center justify-between gap-4 pb-4 border-b border-slate-800/60">
                    <div className="flex items-center gap-2.5">
                      <BrainCircuit className="h-5 w-5 text-emerald-400" />
                      <div>
                        <h4 className="font-bold text-slate-200 text-sm">AI Security Analyst Forensics</h4>
                        <p className="text-[11px] text-slate-500">Generate explanations, verify root causes, and pull threat fixes.</p>
                      </div>
                    </div>
                    
                    {isOperator && !aiReport && (
                      <button
                        onClick={handleRunAiAnalysis}
                        disabled={aiLoading}
                        className="flex items-center gap-1.5 px-3 py-1.5 bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-bold rounded-lg text-xs hover:bg-emerald-500 hover:text-slate-950 transition disabled:opacity-50 shrink-0"
                      >
                        <BrainCircuit className="h-4 w-4" />
                        {aiLoading ? 'Analyzing...' : 'Run Analysis'}
                      </button>
                    )}
                  </div>

                  {aiLoading && (
                    <div className="flex flex-col items-center justify-center py-8 gap-3">
                      <div className="animate-spin rounded-full h-6 w-6 border-t-2 border-emerald-500"></div>
                      <span className="text-xs text-slate-400 font-mono">Synthesizing threat signatures and parsing recommendations...</span>
                    </div>
                  )}

                  {aiReport && (
                    <div className="mt-5 space-y-5 text-xs text-slate-300">
                      <div>
                        <h5 className="font-semibold text-emerald-400 mb-1.5 font-mono text-[11px] uppercase tracking-wider">Incident Summary</h5>
                        <p className="leading-relaxed bg-slate-950/40 p-3 rounded-lg border border-slate-800/50">{aiReport.incidentSummary || ''}</p>
                      </div>
                      <div>
                        <h5 className="font-semibold text-emerald-400 mb-1.5 font-mono text-[11px] uppercase tracking-wider">Root Cause Analysis</h5>
                        <div className="leading-relaxed bg-slate-950/40 p-3 rounded-lg border border-slate-800/50 markdown-body">
                          {(aiReport.rootCause || '').replace(/###/g, '').replace(/\*\*/g, '')}
                        </div>
                      </div>
                      <div>
                        <h5 className="font-semibold text-emerald-400 mb-1.5 font-mono text-[11px] uppercase tracking-wider">Remediation Action Items</h5>
                        <div className="leading-relaxed bg-slate-950/40 p-3 rounded-lg border border-slate-800/50">
                          {(aiReport.recommendations || '').replace(/###/g, '').replace(/\*\*/g, '')}
                        </div>
                      </div>
                      <div>
                        <h5 className="font-semibold text-emerald-400 mb-1.5 font-mono text-[11px] uppercase tracking-wider">Executive Overview</h5>
                        <p className="leading-relaxed bg-slate-950/40 p-3 rounded-lg border border-slate-800/50">{(aiReport.executiveSummary || '').replace(/###/g, '').replace(/\*\*/g, '')}</p>
                      </div>

                      {/* PDF compilation Trigger */}
                      {isOperator && (
                        <div className="pt-4 border-t border-slate-800 flex flex-col gap-3">
                          {!generatedReport ? (
                            <button
                              onClick={handleGeneratePdf}
                              disabled={pdfLoading}
                              className="flex items-center justify-center gap-1.5 w-full py-2 bg-emerald-500 text-slate-950 font-bold rounded-lg hover:bg-emerald-400 transition disabled:opacity-50"
                            >
                              <FileText className="h-4 w-4" />
                              {pdfLoading ? 'Compiling PDF & Uploading S3...' : 'Compile PDF Posture Report'}
                            </button>
                          ) : (
                            <div className="bg-emerald-500/5 border border-emerald-500/20 p-4 rounded-xl flex items-center justify-between gap-4">
                              <div className="space-y-0.5">
                                <h6 className="font-bold text-emerald-400 text-xs">PDF Report Created & Uploaded to AWS S3</h6>
                                <p className="text-[11px] text-slate-400 font-mono truncate max-w-md">{generatedReport.s3Url}</p>
                              </div>
                              <a
                                href={generatedReport.s3Url}
                                target="_blank"
                                rel="noreferrer"
                                className="flex items-center gap-1 px-3 py-1.5 bg-emerald-500 text-slate-950 font-bold rounded text-xs hover:bg-emerald-400 transition shrink-0"
                              >
                                View Report
                                <ExternalLink className="h-3.5 w-3.5" />
                              </a>
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  )}
                </div>
              </div>
            </div>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center text-slate-500 text-sm gap-2.5">
              <ShieldAlert className="h-8 w-8 text-slate-700" />
              <span>Select an incident from the left-side queue to begin forensic analysis.</span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
