import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Shield, Lock, User, Mail, ShieldAlert, CheckCircle2 } from 'lucide-react';

export const Signup: React.FC = () => {
  const navigate = useNavigate();
  const { register } = useAuth();
  
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<string>('viewer');
  
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);
    try {
      await register(username, email, [role], password);
      setSuccess('Operator account registered successfully! Redirecting to sign in...');
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err: any) {
      setError(err.message || 'Operator registration failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-950 px-4 py-12 relative overflow-hidden font-sans">
      {/* Background ambient light */}
      <div className="absolute top-1/4 left-1/4 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-emerald-500/10 rounded-full blur-[120px] pointer-events-none"></div>
      <div className="absolute bottom-1/4 right-1/4 translate-x-1/2 translate-y-1/2 w-[400px] h-[400px] bg-emerald-600/5 rounded-full blur-[100px] pointer-events-none"></div>

      {/* Registration Card */}
      <div className="w-full max-w-md bg-slate-900/60 border border-slate-800 rounded-3xl p-8 shadow-2xl backdrop-blur-md relative">
        <div className="flex flex-col items-center gap-2 mb-8 text-center">
          <div className="bg-emerald-500/10 p-4 rounded-2xl border border-emerald-500/25 mb-2">
            <Shield className="h-10 w-10 text-emerald-400" />
          </div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-100">Enroll Operator</h2>
          <p className="text-slate-400 text-sm">Register credential profiles for platform security access.</p>
        </div>

        {error && (
          <div className="p-4 bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs rounded-xl flex gap-2 items-center mb-6">
            <ShieldAlert className="h-5 w-5 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {success && (
          <div className="p-4 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs rounded-xl flex gap-2 items-center mb-6">
            <CheckCircle2 className="h-5 w-5 shrink-0" />
            <span>{success}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Username */}
          <div className="space-y-1.5">
            <label className="text-xs font-mono font-medium text-slate-400 uppercase tracking-widest">Operator Name</label>
            <div className="flex items-center gap-2 bg-slate-950 px-3.5 py-2.5 rounded-xl border border-slate-800 focus-within:border-emerald-500/40 transition-colors">
              <User className="h-4 w-4 text-slate-500" />
              <input
                type="text"
                required
                placeholder="sec_analyst_r"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="bg-transparent border-none outline-none text-xs text-slate-200 w-full placeholder-slate-600"
              />
            </div>
          </div>

          {/* Email */}
          <div className="space-y-1.5">
            <label className="text-xs font-mono font-medium text-slate-400 uppercase tracking-widest">Secure Email</label>
            <div className="flex items-center gap-2 bg-slate-950 px-3.5 py-2.5 rounded-xl border border-slate-800 focus-within:border-emerald-500/40 transition-colors">
              <Mail className="h-4 w-4 text-slate-500" />
              <input
                type="email"
                required
                placeholder="r@amazon.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="bg-transparent border-none outline-none text-xs text-slate-200 w-full placeholder-slate-600"
              />
            </div>
          </div>

          {/* Password */}
          <div className="space-y-1.5">
            <label className="text-xs font-mono font-medium text-slate-400 uppercase tracking-widest">Secret Key / Password</label>
            <div className="flex items-center gap-2 bg-slate-950 px-3.5 py-2.5 rounded-xl border border-slate-800 focus-within:border-emerald-500/40 transition-colors">
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

          {/* Role Choice */}
          <div className="space-y-1.5">
            <label className="text-xs font-mono font-medium text-slate-400 uppercase tracking-widest">Authorization Scope</label>
            <div className="grid grid-cols-3 gap-2">
              {['viewer', 'analyst', 'admin'].map((r) => (
                <button
                  key={r}
                  type="button"
                  onClick={() => setRole(r)}
                  className={`py-2 rounded-xl text-[10px] font-bold uppercase border transition-all duration-200 ${
                    role === r
                      ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/35'
                      : 'bg-slate-950 text-slate-500 border-slate-800 hover:text-slate-350 hover:bg-slate-900/60'
                  }`}
                >
                  {r}
                </button>
              ))}
            </div>
          </div>

          {/* Action button */}
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-emerald-500 text-slate-950 font-bold py-3.5 rounded-xl hover:bg-emerald-400 transition-all duration-300 text-xs tracking-wider uppercase shadow-lg shadow-emerald-500/10 disabled:opacity-50 mt-2"
          >
            {loading ? 'Registering...' : 'Enroll Operator'}
          </button>
        </form>

        <div className="text-center mt-6">
          <p className="text-xs text-slate-500">
            Already enrolled?{' '}
            <Link to="/login" className="text-emerald-400 hover:text-emerald-300 font-medium transition-colors">
              Operator Login
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};
