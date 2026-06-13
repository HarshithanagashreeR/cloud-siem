import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { Alert } from '../types';
import { useAuth } from '../context/AuthContext';
import { 
  ShieldCheck, 
  ShieldAlert, 
  Search, 
  Filter, 
  CheckCircle,
  Eye
} from 'lucide-react';

export const Alerts: React.FC = () => {
  const { user, hasRole } = useAuth();
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedSeverity, setSelectedSeverity] = useState<string>('ALL');
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');
  const [searchTerm, setSearchTerm] = useState<string>('');

  const isOperator = hasRole('analyst') || hasRole('admin');

  const fetchAlerts = async () => {
    try {
      const data = await api.get<Alert[]>('/alerts');
      setAlerts(data || []);
    } catch (e) {
      console.warn("Failed to fetch alerts. Loading simulation list.", e);
      loadMockAlerts();
    } finally {
      setLoading(false);
    }
  };

  const loadMockAlerts = () => {
    setAlerts([
      {
        id: '1',
        title: 'Reconnaissance: Port Scan Detected',
        description: 'Source IP 192.168.1.144 connected to 12 distinct ports in the last 60 seconds.',
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
        description: 'Host administrative auth page targeting admin received 8 failed password attempts.',
        severity: 'HIGH',
        status: 'ACKNOWLEDGED',
        threatType: 'BRUTE_FORCE',
        sourceIp: '103.24.18.52',
        analyst: { id: '1', username: 'sec_analyst_m', email: 'm@amazon.com', roles: ['ROLE_ANALYST'] },
        createdAt: new Date(Date.now() - 3600000).toISOString(),
        updatedAt: new Date(Date.now() - 1800000).toISOString(),
        resolvedAt: null
      },
      {
        id: '3',
        title: 'Threat Intel Match: Blacklisted Tor Node',
        description: 'Outbound HTTP communication mapped to verified TOR node IP indicator.',
        severity: 'HIGH',
        status: 'RESOLVED',
        threatType: 'THREAT_INTEL',
        sourceIp: '185.220.101.4',
        analyst: { id: '1', username: 'admin', email: 'admin@amazon.com', roles: ['ROLE_ADMIN'] },
        createdAt: new Date(Date.now() - 7200000).toISOString(),
        updatedAt: new Date(Date.now() - 3600000).toISOString(),
        resolvedAt: new Date(Date.now() - 3600000).toISOString()
      },
      {
        id: '4',
        title: 'Excessive Web Request Volumetrics',
        description: 'Ingress traffic volume spikes above 120req/10s from source gateway.',
        severity: 'MEDIUM',
        status: 'OPEN',
        threatType: 'RATE_LIMIT_EXCEEDED',
        sourceIp: '45.138.28.11',
        analyst: null,
        createdAt: new Date(Date.now() - 14400000).toISOString(),
        updatedAt: new Date(Date.now() - 14400000).toISOString(),
        resolvedAt: null
      }
    ]);
  };

  useEffect(() => {
    fetchAlerts();
  }, []);

  const handleAcknowledge = async (id: string) => {
    try {
      const updated = await api.put<Alert>(`/alerts/${id}/acknowledge`);
      setAlerts(alerts.map(a => a.id === id ? updated : a));
    } catch (e) {
      // Offline fallback
      setAlerts(alerts.map(a => a.id === id ? { ...a, status: 'ACKNOWLEDGED', analyst: user } : a));
    }
  };

  const handleResolve = async (id: string) => {
    try {
      const updated = await api.put<Alert>(`/alerts/${id}/resolve`);
      setAlerts(alerts.map(a => a.id === id ? updated : a));
    } catch (e) {
      // Offline fallback
      setAlerts(alerts.map(a => a.id === id ? { ...a, status: 'RESOLVED', resolvedAt: new Date().toISOString() } : a));
    }
  };

  // Filters application
  const filteredAlerts = alerts.filter(alert => {
    const matchesSev = selectedSeverity === 'ALL' || alert.severity === selectedSeverity;
    const matchesStatus = selectedStatus === 'ALL' || alert.status === selectedStatus;
    const matchesSearch = alert.title.toLowerCase().includes(searchTerm.toLowerCase()) || 
                          alert.threatType.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          (alert.sourceIp && alert.sourceIp.includes(searchTerm));
    return matchesSev && matchesStatus && matchesSearch;
  });

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Page Header */}
      <div className="flex items-center justify-between border-b border-slate-800 pb-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Vulnerability & Alerts Feed</h2>
          <p className="text-slate-400 text-sm mt-1">Review, assign, and resolve active platform security alarms.</p>
        </div>
      </div>

      {/* Control filters panel */}
      <div className="flex flex-col md:flex-row gap-4 justify-between bg-slate-900/40 p-4 rounded-xl border border-slate-800">
        <div className="flex flex-1 gap-2 items-center bg-slate-950 px-3 py-2 rounded-lg border border-slate-800 focus-within:border-emerald-500/50 transition-colors">
          <Search className="h-4 w-4 text-slate-500" />
          <input
            type="text"
            placeholder="Search by Title, Threat Type, or Source IP..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="bg-transparent border-none outline-none text-xs text-slate-200 w-full placeholder-slate-600"
          />
        </div>

        <div className="flex flex-wrap gap-3">
          {/* Severity selector */}
          <div className="flex items-center gap-2">
            <span className="text-xs text-slate-500 font-mono font-medium">Severity:</span>
            <select
              value={selectedSeverity}
              onChange={(e) => setSelectedSeverity(e.target.value)}
              className="bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs font-semibold text-slate-300 outline-none"
            >
              <option value="ALL">ALL</option>
              <option value="CRITICAL">CRITICAL</option>
              <option value="HIGH">HIGH</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="LOW">LOW</option>
            </select>
          </div>

          {/* Status selector */}
          <div className="flex items-center gap-2">
            <span className="text-xs text-slate-500 font-mono font-medium">Status:</span>
            <select
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
              className="bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs font-semibold text-slate-300 outline-none"
            >
              <option value="ALL">ALL</option>
              <option value="OPEN">OPEN</option>
              <option value="ACKNOWLEDGED">ACKNOWLEDGED</option>
              <option value="RESOLVED">RESOLVED</option>
            </select>
          </div>
        </div>
      </div>

      {/* Alerts Table */}
      {loading ? (
        <div className="flex h-48 items-center justify-center">
          <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-emerald-500"></div>
        </div>
      ) : filteredAlerts.length === 0 ? (
        <div className="text-center py-12 bg-slate-900/20 border border-slate-800/80 rounded-xl text-slate-500 text-sm">
          No alert items match the selected filter query criteria.
        </div>
      ) : (
        <div className="bg-slate-900/30 border border-slate-800/80 rounded-xl overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-left text-xs">
              <thead className="bg-slate-950 text-slate-400 font-mono border-b border-slate-800">
                <tr>
                  <th className="px-6 py-4 font-semibold">Severity / Title</th>
                  <th className="px-6 py-4 font-semibold">Threat Type</th>
                  <th className="px-6 py-4 font-semibold">Source IP</th>
                  <th className="px-6 py-4 font-semibold">Assigned Analyst</th>
                  <th className="px-6 py-4 font-semibold">Triggered Time</th>
                  <th className="px-6 py-4 font-semibold">Status</th>
                  {isOperator && <th className="px-6 py-4 font-semibold text-right">Actions</th>}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {filteredAlerts.map((alert) => (
                  <tr key={alert.id} className="hover:bg-slate-900/20 transition-colors">
                    {/* Title & Severity */}
                    <td className="px-6 py-4">
                      <div className="flex items-start gap-3">
                        <span className={`mt-0.5 shrink-0 px-2 py-0.5 rounded text-[10px] font-bold font-mono tracking-wider ${
                          alert.severity === 'CRITICAL' 
                            ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20' 
                            : alert.severity === 'HIGH'
                              ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                              : alert.severity === 'MEDIUM'
                                ? 'bg-blue-500/10 text-blue-400 border border-blue-500/20'
                                : 'bg-slate-800 text-slate-300'
                        }`}>
                          {alert.severity}
                        </span>
                        <div>
                          <p className="font-semibold text-slate-200 text-sm leading-snug">{alert.title}</p>
                          <p className="text-[11px] text-slate-400 mt-1 max-w-md line-clamp-1">{alert.description}</p>
                        </div>
                      </div>
                    </td>

                    {/* Threat Type */}
                    <td className="px-6 py-4 font-mono text-slate-300">{alert.threatType}</td>

                    {/* Source IP */}
                    <td className="px-6 py-4 font-mono text-slate-300">{alert.sourceIp || 'N/A'}</td>

                    {/* Analyst */}
                    <td className="px-6 py-4 text-slate-300">
                      {alert.analyst ? (
                        <span className="font-medium bg-slate-800/60 px-2 py-1 rounded text-slate-300 border border-slate-700/60">
                          {alert.analyst.username}
                        </span>
                      ) : (
                        <span className="text-slate-600 italic">Unassigned</span>
                      )}
                    </td>

                    {/* Time */}
                    <td className="px-6 py-4 font-mono text-slate-400">
                      {new Date(alert.createdAt).toLocaleString()}
                    </td>

                    {/* Status badge */}
                    <td className="px-6 py-4">
                      <span className={`px-2.5 py-1 rounded-full text-[10px] font-semibold font-mono border ${
                        alert.status === 'OPEN'
                          ? 'bg-red-500/5 text-red-400 border-red-500/20'
                          : alert.status === 'ACKNOWLEDGED'
                            ? 'bg-amber-500/5 text-amber-400 border-amber-500/20'
                            : 'bg-emerald-500/5 text-emerald-400 border-emerald-500/20'
                      }`}>
                        {alert.status}
                      </span>
                    </td>

                    {/* Operator actions */}
                    {isOperator && (
                      <td className="px-6 py-4 text-right">
                        <div className="flex items-center justify-end gap-2">
                          {alert.status === 'OPEN' && (
                            <button
                              onClick={() => handleAcknowledge(alert.id)}
                              className="flex items-center gap-1.5 bg-amber-500/10 text-amber-400 hover:bg-amber-500 hover:text-slate-950 px-2.5 py-1.5 rounded font-semibold transition border border-amber-500/20"
                              title="Acknowledge Alert"
                            >
                              <ShieldAlert className="h-3.5 w-3.5" />
                              Ack
                            </button>
                          )}
                          {alert.status !== 'RESOLVED' && (
                            <button
                              onClick={() => handleResolve(alert.id)}
                              className="flex items-center gap-1.5 bg-emerald-500/10 text-emerald-400 hover:bg-emerald-500 hover:text-slate-950 px-2.5 py-1.5 rounded font-semibold transition border border-emerald-500/20"
                              title="Resolve Alert"
                            >
                              <ShieldCheck className="h-3.5 w-3.5" />
                              Resolve
                            </button>
                          )}
                          {alert.status === 'RESOLVED' && (
                            <div className="flex items-center gap-1 text-slate-500 px-2 py-1">
                              <CheckCircle className="h-3.5 w-3.5 text-slate-500" />
                              <span>Closed</span>
                            </div>
                          )}
                        </div>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};
