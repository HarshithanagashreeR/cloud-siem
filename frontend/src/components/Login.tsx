import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Shield, Lock, User, AlertTriangle } from 'lucide-react';

export const Login: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      await login(username, password);
      navigate('/');
    } catch (err: any) {
      setError(err.message || 'Login failed. Please verify credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-950 px-4 py-12 relative overflow-hidden font-sans">
      {/* Background ambient light */}
      <div className="absolute top-1/4 left-1/4 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-emerald-500/10 rounded-full blur-[120px] pointer-events-none"></div>
      <div className="absolute bottom-1/4 right-1/4 translate-x-1/2 translate-y-1/2 w-[400px] h-[400px] bg-emerald-600/5 rounded-full blur-[100px] pointer-events-none"></div>

      {/* Login Card */}
      <div className="w-full max-w-md bg-slate-900/60 border border-slate-800 rounded-3xl p-8 shadow-2xl backdrop-blur-md relative">
        <div className="flex flex-col items-center gap-2 mb-8 text-center">
          <div className="bg-emerald-500/10 p-4 rounded-2xl border border-emerald-500/25 mb-2">
            <Shield className="h-10 w-10 text-emerald-400" />
          </div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-100">Sec-Ops Console</h2>
          <p className="text-slate-400 text-sm">Sign in to access Cloud-Native SIEM.</p>
        </div>

        {error && (
          <div className="p-4 bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs rounded-xl flex gap-2 items-center mb-6">
            <AlertTriangle className="h-5 w-5 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-5">
          {/* Username */}
          <div className="space-y-1.5">
            <label className="text-xs font-mono font-medium text-slate-400 uppercase tracking-widest">Username</label>
            <div className="flex items-center gap-2 bg-slate-950 px-3.5 py-3 rounded-xl border border-slate-800 focus-within:border-emerald-500/40 transition-colors">
              <User className="h-4 w-4 text-slate-500" />
              <input
                type="text"
                required
                placeholder="sec_analyst_a"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="bg-transparent border-none outline-none text-xs text-slate-200 w-full placeholder-slate-600"
              />
            </div>
          </div>

          {/* Password */}
          <div className="space-y-1.5">
            <label className="text-xs font-mono font-medium text-slate-400 uppercase tracking-widest">Password</label>
            <div className="flex items-center gap-2 bg-slate-950 px-3.5 py-3 rounded-xl border border-slate-800 focus-within:border-emerald-500/40 transition-colors">
              <Lock className="h-4 w-4 text-slate-500" />
              <input
                type="password"
                required
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="bg-transparent border-none outline-none text-xs text-slate-200 w-full placeholder-slate-700"
              />
            </div>
          </div>

          {/* Action button */}
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-emerald-500 text-slate-950 font-bold py-3.5 rounded-xl hover:bg-emerald-400 transition-all duration-300 text-xs tracking-wider uppercase shadow-lg shadow-emerald-500/10 disabled:opacity-50 mt-2"
          >
            {loading ? 'Authenticating...' : 'Sign In'}
          </button>
        </form>

        <div className="text-center mt-6">
          <p className="text-xs text-slate-500">
            Need a secure access token?{' '}
            <Link to="/signup" className="text-emerald-400 hover:text-emerald-300 font-medium transition-colors">
              Register Operator
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};
