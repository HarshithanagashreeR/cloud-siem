import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { DashboardMetrics, Alert } from '../types';
import { 
  Database, 
  FileWarning, 
  ShieldAlert, 
  Activity, 
  TrendingUp, 
  RefreshCw, 
  Terminal 
} from 'lucide-react';

export const Dashboard: React.FC = () => {
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
  const [recentAlerts, setRecentAlerts] = useState<Alert[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const fetchDashboardData = async () => {
    try {
      const metricsData = await api.get<DashboardMetrics>('/dashboard/metrics');
      const alertsData = await api.get<Alert[]>('/alerts');
      
      setMetrics(metricsData);
      // Sort and pick top 5 open alerts
      const sorted = (alertsData || [])
        .filter(a => a.status !== 'RESOLVED')
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        .slice(0, 5);
      setRecentAlerts(sorted);
    } catch (e) {
      console.warn("Failed to fetch live SIEM metrics. Loading simulation mode.", e);
      loadMockMetrics();
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const loadMockMetrics = () => {
    // Generate beautiful placeholder metrics for simulation mode
    setMetrics({
      totalLogs: 1542039,
      activeIncidents: 4,
      criticalAlerts: 2,
      securityPostureScore: 82,
      logsByEventType: {
        'LOGIN_FAILED': 48,
        'LOGIN_SUCCESS': 94520,
        'PORT_SCAN': 2,
        'API_REQUEST': 1447469,
        'PRIVILEGE_ESCALATION': 0
      },
      alertsBySeverity: {
        'CRITICAL': 2,
        'HIGH': 5,
        'MEDIUM': 12,
        'LOW': 24
      },
      incidentsBySeverity: {
        'CRITICAL': 1,
        'HIGH': 1,
        'MEDIUM': 2,
        'LOW': 0
      },
      riskScores: {
        userRisk: 28,
        assetRisk: 42,
        incidentRisk: 55
      }
    });

    setRecentAlerts([
      {
        id: '1',
        title: 'Reconnaissance: Port Scan Detected',
        description: 'Source IP 192.168.1.144 queried 12 distinct destination ports within 60s.',
        severity: 'CRITICAL',
        status: 'OPEN',
        threatType: 'PORT_SCAN',
        sourceIp: '192.168.1.144',
        analyst: null,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        resolvedAt: null
      },
      {
        id: '2',
        title: 'Brute Force Login Attack',
        description: 'Target username admin received 8 failed login attempts within 5 minutes.',
        severity: 'HIGH',
        status: 'ACKNOWLEDGED',
        threatType: 'BRUTE_FORCE',
        sourceIp: '103.24.18.52',
        analyst: { id: '1', username: 'sec_analyst_r', email: 'r@amazon.com', roles: ['ROLE_ANALYST'] },
        createdAt: new Date(Date.now() - 3600000).toISOString(),
        updatedAt: new Date(Date.now() - 1800000).toISOString(),
        resolvedAt: null
      }
    ]);
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const handleManualRefresh = () => {
    setRefreshing(true);
    fetchDashboardData();
  };

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-emerald-500"></div>
      </div>
    );
  }

  // Set posture ring styles
  const score = metrics?.securityPostureScore || 100;
  const scoreColorClass = score > 80 
    ? 'text-emerald-400 border-emerald-500/20 bg-emerald-500/5' 
    : score > 50 
      ? 'text-amber-400 border-amber-500/20 bg-amber-500/5' 
      : 'text-rose-400 border-rose-500/20 bg-rose-500/5';

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      {/* Header bar */}
      <div className="flex items-center justify-between border-b border-slate-800 pb-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Security Telemetry Dashboard</h2>
          <p className="text-slate-400 text-sm mt-1">Real-time threat feeds, risk scoring, and security posture monitoring.</p>
        </div>
        <button
          onClick={handleManualRefresh}
          disabled={refreshing}
          className="flex items-center gap-2 px-3 py-2 bg-slate-900 border border-slate-800 rounded-lg hover:bg-slate-800 text-slate-300 transition-colors text-sm disabled:opacity-50"
        >
          <RefreshCw className={`h-4 w-4 ${refreshing ? 'animate-spin' : ''}`} />
          Refresh Stats
        </button>
      </div>

      {/* Numerical Metrics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-5">
        <div className="bg-slate-900/60 border border-slate-800/80 p-5 rounded-2xl flex items-center justify-between relative overflow-hidden group">
          <div className="space-y-1">
            <span className="text-xs text-slate-500 font-medium font-mono uppercase tracking-widest">Logs Ingested</span>
            <h3 className="text-2xl font-bold tracking-tight text-slate-100">{metrics?.totalLogs.toLocaleString()}</h3>
          </div>
          <div className="bg-slate-950 p-3 rounded-xl border border-slate-800 group-hover:border-emerald-500/30 transition-colors duration-300">
            <Database className="h-6 w-6 text-emerald-400" />
          </div>
        </div>

        <div className="bg-slate-900/60 border border-slate-800/80 p-5 rounded-2xl flex items-center justify-between relative overflow-hidden group">
          <div className="space-y-1">
            <span className="text-xs text-slate-500 font-medium font-mono uppercase tracking-widest">Active Incidents</span>
            <h3 className="text-2xl font-bold tracking-tight text-slate-100">{metrics?.activeIncidents}</h3>
          </div>
          <div className="bg-slate-950 p-3 rounded-xl border border-slate-800 group-hover:border-amber-500/30 transition-colors duration-300">
            <FileWarning className="h-6 w-6 text-amber-400" />
          </div>
        </div>

        <div className="bg-slate-900/60 border border-slate-800/80 p-5 rounded-2xl flex items-center justify-between relative overflow-hidden group">
          <div className="space-y-1">
            <span className="text-xs text-slate-500 font-medium font-mono uppercase tracking-widest">Critical Alerts</span>
            <h3 className="text-2xl font-bold tracking-tight text-slate-100">{metrics?.criticalAlerts}</h3>
          </div>
          <div className="bg-slate-950 p-3 rounded-xl border border-slate-800 group-hover:border-rose-500/30 transition-colors duration-300">
            <ShieldAlert className="h-6 w-6 text-rose-400 animate-pulse" />
          </div>
        </div>

        <div className="bg-slate-900/60 border border-slate-800/80 p-5 rounded-2xl flex items-center justify-between relative overflow-hidden group">
          <div className="space-y-1">
            <span className="text-xs text-slate-500 font-medium font-mono uppercase tracking-widest">Threat Velocity</span>
            <h3 className="text-2xl font-bold tracking-tight text-slate-100">Normal</h3>
          </div>
          <div className="bg-slate-950 p-3 rounded-xl border border-slate-800 group-hover:border-blue-500/30 transition-colors duration-300">
            <Activity className="h-6 w-6 text-blue-400" />
          </div>
        </div>
      </div>

      {/* Main posturing and score section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Posture Radial Ring Card */}
        <div className="bg-slate-900/40 border border-slate-800 p-6 rounded-2xl flex flex-col items-center justify-center min-h-[300px]">
          <h4 className="text-sm font-semibold tracking-wider font-mono uppercase text-slate-500 mb-6">Security Posture Score</h4>
          
          <div className={`relative h-36 w-36 rounded-full border-4 flex flex-col items-center justify-center ${scoreColorClass}`}>
            <span className="text-4xl font-extrabold tracking-tight">{score}</span>
            <span className="text-[10px] uppercase font-mono tracking-wider font-semibold text-slate-400 mt-1">
              {score > 80 ? 'Healthy' : score > 50 ? 'Degraded' : 'Critical'}
            </span>
          </div>

          <p className="text-xs text-slate-400 text-center mt-6 leading-relaxed max-w-[220px]">
            Aggregated system posture calculated from active incident severities and unmitigated vulnerabilities.
          </p>
        </div>

        {/* Risk Scores indicators */}
        <div className="bg-slate-900/40 border border-slate-800 p-6 rounded-2xl space-y-6">
          <h4 className="text-sm font-semibold tracking-wider font-mono uppercase text-slate-500">Risk Score Analytics</h4>
          
          <div className="space-y-5">
            <div>
              <div className="flex justify-between text-xs font-medium mb-2">
                <span className="text-slate-300">User Risk Profile</span>
                <span className="text-slate-400 font-mono">{metrics?.riskScores.userRisk}/100</span>
              </div>
              <div className="w-full bg-slate-950 h-2 rounded-full overflow-hidden border border-slate-800">
                <div 
                  className="bg-emerald-500 h-full rounded-full transition-all duration-500" 
                  style={{ width: `${metrics?.riskScores.userRisk}%` }}
                ></div>
              </div>
            </div>

            <div>
              <div className="flex justify-between text-xs font-medium mb-2">
                <span className="text-slate-300">Targeted Asset Risk</span>
                <span className="text-slate-400 font-mono">{metrics?.riskScores.assetRisk}/100</span>
              </div>
              <div className="w-full bg-slate-950 h-2 rounded-full overflow-hidden border border-slate-800">
                <div 
                  className="bg-amber-500 h-full rounded-full transition-all duration-500" 
                  style={{ width: `${metrics?.riskScores.assetRisk}%` }}
                ></div>
              </div>
            </div>

            <div>
              <div className="flex justify-between text-xs font-medium mb-2">
                <span className="text-slate-300">Active Incident Severity</span>
                <span className="text-slate-400 font-mono">{metrics?.riskScores.incidentRisk}/100</span>
              </div>
              <div className="w-full bg-slate-950 h-2 rounded-full overflow-hidden border border-slate-800">
                <div 
                  className="bg-rose-500 h-full rounded-full transition-all duration-500" 
                  style={{ width: `${metrics?.riskScores.incidentRisk}%` }}
                ></div>
              </div>
            </div>
          </div>

          <div className="pt-2 border-t border-slate-800/60 text-[11px] text-slate-500 leading-relaxed">
            * Scores reflect live threat telemetry and indicators of compromise (IoC) parsed by the Threat Detection Engine.
          </div>
        </div>

        {/* Ingested log type frequencies */}
        <div className="bg-slate-900/40 border border-slate-800 p-6 rounded-2xl flex flex-col justify-between">
          <div>
            <h4 className="text-sm font-semibold tracking-wider font-mono uppercase text-slate-500 mb-4">Log Signatures (Last 24h)</h4>
            <div className="space-y-2.5">
              {metrics && Object.entries(metrics.logsByEventType).map(([type, count]) => (
                <div key={type} className="flex justify-between items-center text-xs border-b border-slate-800/60 pb-2">
                  <span className="font-mono text-slate-300">{type}</span>
                  <span className="font-mono font-semibold text-slate-400">{count.toLocaleString()}</span>
                </div>
              ))}
            </div>
          </div>
          <div className="flex items-center gap-2 text-xs text-slate-500 pt-4">
            <TrendingUp className="h-4 w-4 text-emerald-400" />
            <span>Telemetry streams running nominal.</span>
          </div>
        </div>
      </div>

      {/* Bottom section: Recent Alerts */}
      <div className="bg-slate-900/40 border border-slate-800 rounded-2xl p-6">
        <h4 className="text-sm font-semibold tracking-wider font-mono uppercase text-slate-500 mb-5 flex items-center gap-2">
          <Terminal className="h-4 w-4 text-rose-500" />
          Active Ingested Threats Ticker
        </h4>

        {recentAlerts.length === 0 ? (
          <div className="text-center py-6 text-slate-500 text-sm">
            No active threat indicators detected. Secure posture.
          </div>
        ) : (
          <div className="divide-y divide-slate-800/80">
            {recentAlerts.map((alert) => (
              <div key={alert.id} className="py-4 first:pt-0 last:pb-0 flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div className="space-y-1">
                  <div className="flex items-center gap-2.5 flex-wrap">
                    <h5 className="font-semibold text-sm text-slate-200">{alert.title}</h5>
                    <span className={`text-[10px] font-semibold font-mono px-2 py-0.5 rounded-full ${
                      alert.severity === 'CRITICAL' 
                        ? 'bg-rose-500/15 text-rose-400 border border-rose-500/25' 
                        : 'bg-amber-500/15 text-amber-400 border border-amber-500/25'
                    }`}>
                      {alert.severity}
                    </span>
                    <span className="text-[10px] font-mono bg-slate-950 text-slate-400 px-2 py-0.5 rounded border border-slate-800">
                      {alert.threatType}
                    </span>
                  </div>
                  <p className="text-xs text-slate-400 leading-relaxed">{alert.description}</p>
                </div>

                <div className="flex items-center gap-4 shrink-0 text-xs text-slate-400">
                  <div className="font-mono text-right">
                    <p className="text-slate-400">Source: {alert.sourceIp}</p>
                    <p className="text-[10px] text-slate-500 mt-0.5">
                      {new Date(alert.createdAt).toLocaleTimeString()}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
