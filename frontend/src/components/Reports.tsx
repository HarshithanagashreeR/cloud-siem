import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { Report } from '../types';
import { useAuth } from '../context/AuthContext';
import { FolderOpen, FileText, Calendar, ExternalLink, RefreshCw, AlertCircle } from 'lucide-react';

export const Reports: React.FC = () => {
  const { hasRole } = useAuth();
  const [reports, setReports] = useState<Report[]>([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [status, setStatus] = useState<string | null>(null);

  const isOperator = hasRole('analyst') || hasRole('admin');

  const fetchReports = async () => {
    try {
      const data = await api.get<Report[]>('/reports');
      setReports(data || []);
    } catch (e) {
      console.warn("Failed to fetch reports. Loading mock archives.", e);
      loadMockReports();
    } finally {
      setLoading(false);
    }
  };

  const loadMockReports = () => {
    setReports([
      {
        id: 'rep_101',
        name: 'SIEM System Posture Report (Weekly Audit)',
        type: 'EXEC_SUMMARY',
        s3Url: 'https://mock-siem-reports.s3.amazonaws.com/posture_report_weekly.pdf',
        createdBy: { id: '1', username: 'admin_sys', email: 'admin@amazon.com', roles: ['ROLE_ADMIN'] },
        createdAt: new Date(Date.now() - 86400000 * 2).toISOString()
      },
      {
        id: 'rep_102',
        name: 'Incident Report: PORT_SCAN targeting internal assets',
        type: 'INCIDENT_REPORT',
        s3Url: 'https://mock-siem-reports.s3.amazonaws.com/incident_101.pdf',
        createdBy: { id: '2', username: 'sec_analyst_m', email: 'm@amazon.com', roles: ['ROLE_ANALYST'] },
        createdAt: new Date(Date.now() - 3600000 * 4).toISOString()
      }
    ]);
  };

  useEffect(() => {
    fetchReports();
  }, []);

  const handleGenerateSystemReport = async () => {
    setGenerating(true);
    setStatus(null);
    try {
      const newReport = await api.post<Report>('/reports/system', {});
      setReports([newReport, ...reports]);
      setStatus("System Posture Report compiled and uploaded to S3 successfully!");
    } catch (e) {
      // Offline fallback simulation
      const mockReport: Report = {
        id: `rep_${Date.now()}`,
        name: 'SIEM System Posture Report',
        type: 'EXEC_SUMMARY',
        s3Url: 'https://mock-siem-reports.s3.amazonaws.com/posture_report_' + Date.now() + '.pdf',
        createdBy: { id: '1', username: 'sec_operator', email: 'op@amazon.com', roles: ['ROLE_ANALYST'] },
        createdAt: new Date().toISOString()
      };
      setReports([mockReport, ...reports]);
      setStatus("System Posture Report simulated and saved to list.");
    } finally {
      setGenerating(false);
    }
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Page Header */}
      <div className="flex items-center justify-between border-b border-slate-800 pb-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Security PDF Reports Archive</h2>
          <p className="text-slate-400 text-sm mt-1">Review operational audits, incident files, and executive summaries.</p>
        </div>
        
        {isOperator && (
          <button
            onClick={handleGenerateSystemReport}
            disabled={generating}
            className="flex items-center gap-2 px-3 py-2 bg-emerald-500 text-slate-950 font-bold rounded-lg hover:bg-emerald-400 transition text-sm disabled:opacity-50"
          >
            <RefreshCw className={`h-4 w-4 ${generating ? 'animate-spin' : ''}`} />
            {generating ? 'Compiling S3...' : 'Compile Posture Report'}
          </button>
        )}
      </div>

      {status && (
        <div className="p-4 bg-emerald-500/5 text-emerald-400 border border-emerald-500/20 rounded-xl text-xs flex gap-2 items-center">
          <CheckCircle className="h-4 w-4 shrink-0" />
          <span>{status}</span>
        </div>
      )}

      {/* Grid List */}
      {loading ? (
        <div className="flex h-48 items-center justify-center">
          <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-emerald-500"></div>
        </div>
      ) : reports.length === 0 ? (
        <div className="text-center py-12 bg-slate-900/20 border border-slate-800/80 rounded-xl text-slate-500 text-sm">
          No compiled PDF reports stored in database.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {reports.map((report) => (
            <div
              key={report.id}
              className="bg-slate-900/40 border border-slate-800 p-5 rounded-2xl flex flex-col justify-between gap-5 hover:border-emerald-500/20 transition-all duration-300 relative group"
            >
              <div className="flex items-start gap-4">
                <div className="bg-slate-950 p-3 rounded-xl border border-slate-800 group-hover:border-emerald-500/30 transition-colors shrink-0">
                  <FileText className="h-6 w-6 text-emerald-400" />
                </div>
                <div className="space-y-1 min-w-0">
                  <h4 className="font-bold text-sm text-slate-200 truncate pr-4">{report.name}</h4>
                  <span className="inline-block text-[9px] font-mono font-bold bg-slate-950 border border-slate-800 text-slate-400 px-2 py-0.5 rounded uppercase tracking-wider">
                    {report.type.replace('_', ' ')}
                  </span>
                  <p className="text-[11px] text-slate-500 font-mono pt-1 truncate">{report.s3Url}</p>
                </div>
              </div>

              <div className="flex items-center justify-between pt-4 border-t border-slate-800/60 text-[11px] text-slate-400">
                <div className="flex items-center gap-2">
                  <Calendar className="h-4.5 w-4.5 text-slate-500" />
                  <span>{new Date(report.createdAt).toLocaleDateString()}</span>
                  <span className="text-slate-600">|</span>
                  <span className="truncate">By: {report.createdBy ? report.createdBy.username : 'System'}</span>
                </div>
                <a
                  href={report.s3Url}
                  target="_blank"
                  rel="noreferrer"
                  className="flex items-center gap-1 bg-slate-950 hover:bg-slate-800 text-slate-300 border border-slate-800 px-3 py-1.5 rounded-lg transition"
                >
                  Download
                  <ExternalLink className="h-3.5 w-3.5" />
                </a>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

// Simple icon wrapper
const CheckCircle: React.FC<{ className?: string }> = ({ className }) => (
  <svg className={className} xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
    <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
  </svg>
);
