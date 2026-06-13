import React, { useState } from 'react';
import { api } from '../services/api';
import { 
  PlusSquare, 
  Terminal, 
  Play, 
  CheckCircle2, 
  AlertCircle,
  HelpCircle
} from 'lucide-react';

export const Ingestion: React.FC = () => {
  const [logText, setLogText] = useState<string>('[]');
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

  const presets = [
    {
      name: 'Simulate Recon: Port Scan',
      description: 'Generates 12 connection events from IP 192.168.22.41 hitting distinct ports.',
      payload: () => {
        const logs = [];
        for (let port = 20; port <= 31; port++) {
          logs.push({
            timestamp: new Date().toISOString(),
            sourceIp: '192.168.22.41',
            destinationIp: '10.0.0.4',
            destinationPort: port,
            eventType: 'PORT_SCAN_PROBE',
            payload: { port_probed: port },
            severity: 'INFO',
            message: `Probe hit port: ${port}`
          });
        }
        return JSON.stringify(logs, null, 2);
      }
    },
    {
      name: 'Simulate Auth: Brute Force',
      description: 'Generates 6 failed logins targeting admin from IP 185.101.44.89.',
      payload: () => {
        const logs = [];
        for (let i = 1; i <= 6; i++) {
          logs.push({
            timestamp: new Date(Date.now() - (6 - i) * 10000).toISOString(),
            sourceIp: '185.101.44.89',
            destinationIp: '10.0.0.5',
            destinationPort: 443,
            eventType: 'LOGIN_FAILED',
            payload: { username: 'admin', attempt_number: i },
            severity: 'WARNING',
            message: `User authentication failure for account 'admin' (Attempt #${i}).`
          });
        }
        return JSON.stringify(logs, null, 2);
      }
    },
    {
      name: 'Simulate Intel: Malicious host',
      description: 'Outbound HTTP communication mapped to blacklisted Tor node indicator.',
      payload: () => {
        return JSON.stringify([
          {
            timestamp: new Date().toISOString(),
            sourceIp: '185.220.101.4',
            destinationIp: '10.0.0.4',
            destinationPort: 80,
            eventType: 'OUTBOUND_CONNECTION',
            payload: { proxy: 'tor-node-exit' },
            severity: 'WARNING',
            message: 'Outbound TCP connection established to proxy indicator.'
          }
        ], null, 2);
      }
    },
    {
      name: 'Simulate Vuln: Privilege Escalation',
      description: 'Sudo modifications from non-admin account.',
      payload: () => {
        return JSON.stringify([
          {
            timestamp: new Date().toISOString(),
            sourceIp: '10.0.0.12',
            destinationIp: '10.0.0.4',
            destinationPort: null,
            eventType: 'PRIVILEGE_ESCALATION',
            payload: { user: 'guest_operator', executed: 'sudo vi /etc/sudoers' },
            severity: 'CRITICAL',
            message: 'Userguest_operator: privilege escalation via sudoers file modification.'
          }
        ], null, 2);
      }
    }
  ];

  const handleApplyPreset = (presetFn: () => string) => {
    setLogText(presetFn());
    setStatus(null);
  };

  const handleIngest = async () => {
    setLoading(true);
    setStatus(null);
    try {
      const parsed = JSON.parse(logText);
      const logArray = Array.isArray(parsed) ? parsed : [parsed];
      
      const response = await api.post<{ message: string }>('/logs/ingest/bulk', logArray);
      
      setStatus({
        type: 'success',
        message: response.message || `Successfully ingested ${logArray.length} logs. Threats are running through correlation engine.`
      });
    } catch (e: any) {
      setStatus({
        type: 'error',
        message: e.message || 'Validation error: Ensure the payload is valid JSON.'
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      {/* Page Header */}
      <div className="flex items-center justify-between border-b border-slate-800 pb-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight">Security Log Ingestion Simulator</h2>
          <p className="text-slate-400 text-sm mt-1">Inject mock threat activity to test parsing, scoring, and AI summaries.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Side: Presets */}
        <div className="space-y-4">
          <h3 className="text-xs font-semibold font-mono text-slate-500 uppercase tracking-widest">Threat Preset Attack Signatures</h3>
          <div className="space-y-3">
            {presets.map((p) => (
              <button
                key={p.name}
                onClick={() => handleApplyPreset(p.payload)}
                className="w-full text-left p-4 bg-slate-900/40 border border-slate-800 rounded-xl hover:border-emerald-500/30 transition-all duration-200 group"
              >
                <h4 className="font-semibold text-xs text-slate-200 group-hover:text-emerald-400 transition-colors">{p.name}</h4>
                <p className="text-[11px] text-slate-400 mt-1 leading-normal">{p.description}</p>
              </button>
            ))}
          </div>

          <div className="p-4 bg-slate-900/20 border border-slate-800/80 rounded-xl flex gap-3 text-xs text-slate-400 leading-normal">
            <HelpCircle className="h-5 w-5 text-emerald-400 shrink-0 mt-0.5" />
            <p>
              Applying a preset loads standard JSON logs into the editor. Pressing **Ingest Payload** sends logs directly to the ingestion API.
            </p>
          </div>
        </div>

        {/* Right Side: Log JSON Editor */}
        <div className="lg:col-span-2 flex flex-col bg-slate-900/40 border border-slate-800 rounded-xl overflow-hidden min-h-[450px]">
          {/* Header toolbar */}
          <div className="flex items-center justify-between p-4 bg-slate-950 border-b border-slate-800">
            <span className="flex items-center gap-2 text-xs font-semibold font-mono text-slate-400">
              <Terminal className="h-4 w-4 text-emerald-400" />
              JSON Payload Editor
            </span>
            <button
              onClick={handleIngest}
              disabled={loading}
              className="flex items-center gap-1.5 px-3 py-1.5 bg-emerald-500 text-slate-950 font-bold rounded-lg text-xs hover:bg-emerald-400 transition disabled:opacity-50"
            >
              <Play className="h-3.5 w-3.5 fill-current" />
              {loading ? 'Ingesting...' : 'Ingest Payload'}
            </button>
          </div>

          {/* Editor Body */}
          <div className="flex-1 p-4 bg-slate-950/60 font-mono">
            <textarea
              value={logText}
              onChange={(e) => setLogText(e.target.value)}
              className="w-full h-full min-h-[300px] bg-transparent text-xs text-slate-200 outline-none border-none resize-none placeholder-slate-700"
              spellCheck="false"
            />
          </div>

          {/* Feedback strip */}
          {status && (
            <div className={`p-4 flex gap-3 text-xs leading-normal border-t ${
              status.type === 'success'
                ? 'bg-emerald-500/5 text-emerald-400 border-emerald-500/20'
                : 'bg-rose-500/5 text-rose-400 border-rose-500/20'
            }`}>
              {status.type === 'success' ? (
                <CheckCircle2 className="h-5 w-5 shrink-0" />
              ) : (
                <AlertCircle className="h-5 w-5 shrink-0" />
              )}
              <span>{status.message}</span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
