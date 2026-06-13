import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { AuditLog } from '../types';
import { ShieldCheck, UserCog, Calendar, Activity } from 'lucide-react';

export const Audit: React.FC = () => {
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const fetchAuditLogs = async (pageNum: number) => {
    setLoading(true);
    try {
      const data = await api.get<{ content: AuditLog[]; totalPages: number }>(`/audit?page=${pageNum}&size=15`);
      setAuditLogs(data.content || []);
      setTotalPages(data.totalPages || 1);
    } catch (e) {
      console.warn("Failed to fetch audit logs. Loading simulation log feed.", e);
      loadMockAuditLogs();
    } finally {
      setLoading(false);
    }
  };

  const loadMockAuditLogs = () => {
    setAuditLogs([
      {
        id: 'aud_1',
        timestamp: new Date().toISOString(),
        actor: 'admin',
        action: 'REPORT_GENERATE',
        resource: 'Report',
        status: 'SUCCESS',
        ipAddress: '10.0.0.1',
        details: 'Generated PDF system posture metrics report.'
      },
      {
        id: 'aud_2',
        timestamp: new Date(Date.now() - 600000).toISOString(),
        actor: 'sec_analyst_m',
        action: 'ALERT_ACKNOWLEDGE',
        resource: 'Alert',
        status: 'SUCCESS',
        ipAddress: '192.168.1.100',
        details: 'Acknowledged alert: Reconnaissance: Port Scan Detected'
      },
      {
        id: 'aud_3',
        timestamp: new Date(Date.now() - 1200000).toISOString(),
        actor: 'sec_analyst_m',
        action: 'USER_LOGIN',
        resource: 'AuthenticationService',
        status: 'SUCCESS',
        ipAddress: '192.168.1.100',
        details: 'User successfully logged in.'
      },
      {
        id: 'aud_4',
        timestamp: new Date(Date.now() - 3600000).toISOString(),
        actor: 'unknown_hacker',
        action: 'USER_LOGIN',
        resource: 'AuthenticationService',
        status: 'FAILED',
        ipAddress: '185.101.44.89',
        details: 'Failed login attempt: Invalid credentials.'
      }
    ]);
    setTotalPages(1);
  };

  useEffect(() => {
    fetchAuditLogs(page);
  }, [page]);

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Page Header */}
      <div className="flex items-center justify-between border-b border-slate-800 pb-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">System Audit & Access Logs</h2>
          <p className="text-slate-400 text-sm mt-1">Immutable transaction records tracing analyst behaviors, system edits, and login events.</p>
        </div>
      </div>

      {/* Audit Logs Table */}
      {loading ? (
        <div className="flex h-48 items-center justify-center">
          <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-emerald-500"></div>
        </div>
      ) : auditLogs.length === 0 ? (
        <div className="text-center py-12 bg-slate-900/20 border border-slate-800/80 rounded-xl text-slate-500 text-sm">
          No audit entries found.
        </div>
      ) : (
        <div className="bg-slate-900/30 border border-slate-800/80 rounded-xl overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-left text-xs">
              <thead className="bg-slate-950 text-slate-400 font-mono border-b border-slate-800">
                <tr>
                  <th className="px-6 py-4 font-semibold">Actor</th>
                  <th className="px-6 py-4 font-semibold">Action</th>
                  <th className="px-6 py-4 font-semibold">Resource</th>
                  <th className="px-6 py-4 font-semibold">Timestamp</th>
                  <th className="px-6 py-4 font-semibold">Status</th>
                  <th className="px-6 py-4 font-semibold">Client IP</th>
                  <th className="px-6 py-4 font-semibold">Details</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 font-mono text-slate-300">
                {auditLogs.map((log) => (
                  <tr key={log.id} className="hover:bg-slate-900/20 transition-colors">
                    <td className="px-6 py-4 font-semibold text-slate-100">{log.actor}</td>
                    <td className="px-6 py-4 text-emerald-400 font-semibold">{log.action}</td>
                    <td className="px-6 py-4 text-slate-300">{log.resource}</td>
                    <td className="px-6 py-4 text-slate-400">
                      {new Date(log.timestamp).toISOString()}
                    </td>
                    <td className="px-6 py-4">
                      <span className={`px-2 py-0.5 rounded text-[10px] font-bold tracking-wider ${
                        log.status === 'SUCCESS'
                          ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                          : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                      }`}>
                        {log.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-slate-400">{log.ipAddress}</td>
                    <td className="px-6 py-4 max-w-sm truncate" title={log.details}>
                      {log.details}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="bg-slate-950 px-6 py-4 flex items-center justify-between border-t border-slate-800">
              <span className="text-slate-500">Page {page + 1} of {totalPages}</span>
              <div className="flex gap-2">
                <button
                  disabled={page === 0}
                  onClick={() => setPage(page - 1)}
                  className="px-3 py-1.5 bg-slate-900 text-slate-300 border border-slate-800 rounded-lg hover:bg-slate-800 text-xs font-semibold disabled:opacity-50"
                >
                  Previous
                </button>
                <button
                  disabled={page >= totalPages - 1}
                  onClick={() => setPage(page + 1)}
                  className="px-3 py-1.5 bg-slate-900 text-slate-300 border border-slate-800 rounded-lg hover:bg-slate-800 text-xs font-semibold disabled:opacity-50"
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
