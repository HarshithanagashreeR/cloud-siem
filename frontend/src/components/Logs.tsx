import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { LogEntry } from '../types';
import { Search, Database, RefreshCw } from 'lucide-react';

export const Logs: React.FC = () => {
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [severityFilter, setSeverityFilter] = useState('ALL');
  const [refreshing, setRefreshing] = useState(false);

  const fetchLogs = async () => {
    try {
      const data = await api.get<{ content: LogEntry[] }>('/logs');
      // Spring Data JPA returns Page object, but check if it is direct array or content object
      const logsList = Array.isArray(data) ? data : (data.content || []);
      setLogs(logsList);
    } catch (e) {
      console.warn("Failed to fetch logs. Loading simulation log feed.", e);
      loadMockLogs();
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const loadMockLogs = () => {
    setLogs([
      {
        id: '1',
        timestamp: new Date().toISOString(),
        sourceIp: '192.168.1.144',
        destinationIp: '10.0.0.4',
        destinationPort: 22,
        eventType: 'PORT_SCAN',
        payload: { target_ports: [21, 22, 80, 443] },
        severity: 'CRITICAL',
        status: 'UNPROCESSED',
        message: 'Multiple connections attempted to distinct ports within a 60-second window.',
        createdAt: new Date().toISOString()
      },
      {
        id: '2',
        timestamp: new Date(Date.now() - 300000).toISOString(),
        sourceIp: '103.24.18.52',
        destinationIp: '10.0.0.5',
        destinationPort: 443,
        eventType: 'LOGIN_FAILED',
        payload: { attempted_user: 'admin', user_agent: 'Mozilla/5.0' },
        severity: 'WARNING',
        status: 'PROCESSED',
        message: 'Failed login request: Invalid authentication token signatures.',
        createdAt: new Date(Date.now() - 300000).toISOString()
      },
      {
        id: '3',
        timestamp: new Date(Date.now() - 600000).toISOString(),
        sourceIp: '192.168.1.100',
        destinationIp: '10.0.0.5',
        destinationPort: 80,
        eventType: 'LOGIN_SUCCESS',
        payload: { authenticated_user: 'sec_analyst_r' },
        severity: 'INFO',
        status: 'PROCESSED',
        message: 'Operator logged into the dashboard session.',
        createdAt: new Date(Date.now() - 600000).toISOString()
      },
      {
        id: '4',
        timestamp: new Date(Date.now() - 1200000).toISOString(),
        sourceIp: '10.0.0.2',
        destinationIp: '10.0.0.4',
        destinationPort: 8080,
        eventType: 'API_REQUEST',
        payload: { path: '/api/v1/dashboard/metrics', method: 'GET' },
        severity: 'INFO',
        status: 'PROCESSED',
        message: 'REST API lookup request.',
        createdAt: new Date(Date.now() - 1200000).toISOString()
      }
    ]);
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  const handleManualRefresh = () => {
    setRefreshing(true);
    fetchLogs();
  };

  const filteredLogs = logs.filter(log => {
    const matchesSev = severityFilter === 'ALL' || log.severity === severityFilter;
    const matchesSearch = log.eventType.toLowerCase().includes(searchTerm.toLowerCase()) || 
                          log.sourceIp.includes(searchTerm) ||
                          (log.message && log.message.toLowerCase().includes(searchTerm.toLowerCase()));
    return matchesSev && matchesSearch;
  });

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Page Header */}
      <div className="flex items-center justify-between border-b border-slate-800 pb-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Security Incident Logs Database</h2>
          <p className="text-slate-400 text-sm mt-1">Review raw logs, inspect payloads, and audit system event flows.</p>
        </div>
        <button
          onClick={handleManualRefresh}
          disabled={refreshing}
          className="flex items-center gap-2 px-3 py-2 bg-slate-900 border border-slate-800 rounded-lg hover:bg-slate-800 text-slate-300 transition-colors text-sm disabled:opacity-50"
        >
          <RefreshCw className={`h-4 w-4 ${refreshing ? 'animate-spin' : ''}`} />
          Reload logs
        </button>
      </div>

      {/* Filters */}
      <div className="flex flex-col md:flex-row gap-4 justify-between bg-slate-900/40 p-4 rounded-xl border border-slate-800">
        <div className="flex flex-1 gap-2 items-center bg-slate-950 px-3 py-2 rounded-lg border border-slate-800 focus-within:border-emerald-500/50 transition-colors">
          <Search className="h-4 w-4 text-slate-500" />
          <input
            type="text"
            placeholder="Search by Event Type, Source IP, or message contents..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="bg-transparent border-none outline-none text-xs text-slate-200 w-full placeholder-slate-600"
          />
        </div>

        <div className="flex items-center gap-2">
          <span className="text-xs text-slate-500 font-mono font-medium">Severity:</span>
          <select
            value={severityFilter}
            onChange={(e) => setSeverityFilter(e.target.value)}
            className="bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs font-semibold text-slate-300 outline-none"
          >
            <option value="ALL">ALL</option>
            <option value="CRITICAL">CRITICAL</option>
            <option value="WARNING">WARNING</option>
            <option value="INFO">INFO</option>
          </select>
        </div>
      </div>

      {/* Logs Table */}
      {loading ? (
        <div className="flex h-48 items-center justify-center">
          <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-emerald-500"></div>
        </div>
      ) : filteredLogs.length === 0 ? (
        <div className="text-center py-12 bg-slate-900/20 border border-slate-800/80 rounded-xl text-slate-500 text-sm">
          No log events found.
        </div>
      ) : (
        <div className="bg-slate-900/30 border border-slate-800/80 rounded-xl overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-left text-xs">
              <thead className="bg-slate-950 text-slate-400 font-mono border-b border-slate-800">
                <tr>
                  <th className="px-6 py-4 font-semibold">Severity</th>
                  <th className="px-6 py-4 font-semibold">Event Type</th>
                  <th className="px-6 py-4 font-semibold">Timestamp</th>
                  <th className="px-6 py-4 font-semibold">Source IP</th>
                  <th className="px-6 py-4 font-semibold">Destination Info</th>
                  <th className="px-6 py-4 font-semibold">Message & Payload</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 font-mono">
                {filteredLogs.map((log) => (
                  <tr key={log.id} className="hover:bg-slate-900/20 transition-colors">
                    {/* Severity Badge */}
                    <td className="px-6 py-4">
                      <span className={`px-2 py-0.5 rounded text-[10px] font-bold tracking-wider ${
                        log.severity === 'CRITICAL'
                          ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                          : log.severity === 'WARNING'
                            ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                            : 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                      }`}>
                        {log.severity}
                      </span>
                    </td>

                    {/* Event Type */}
                    <td className="px-6 py-4 font-semibold text-slate-200">{log.eventType}</td>

                    {/* Timestamp */}
                    <td className="px-6 py-4 text-slate-400">
                      {new Date(log.timestamp).toISOString()}
                    </td>

                    {/* Source IP */}
                    <td className="px-6 py-4 text-slate-300">{log.sourceIp}</td>

                    {/* Destination IP & Port */}
                    <td className="px-6 py-4 text-slate-300">
                      {log.destinationIp ? (
                        <span>
                          {log.destinationIp}
                          {log.destinationPort ? `:${log.destinationPort}` : ''}
                        </span>
                      ) : (
                        <span className="text-slate-600">-</span>
                      )}
                    </td>

                    {/* Message & Payload Details */}
                    <td className="px-6 py-4 max-w-lg">
                      <p className="text-slate-300 leading-normal mb-1">{log.message}</p>
                      {log.payload && Object.keys(log.payload).length > 0 && (
                        <pre className="text-[10px] bg-slate-950/80 p-2.5 rounded border border-slate-800/80 text-slate-400 overflow-x-auto max-w-md">
                          {JSON.stringify(log.payload, null, 2)}
                        </pre>
                      )}
                    </td>
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
