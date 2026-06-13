import React from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  ShieldAlert, 
  Terminal, 
  FileWarning, 
  LayoutGrid, 
  PlusSquare, 
  FolderOpen, 
  UserCog, 
  LogOut,
  ShieldAlert as ShieldIcon
} from 'lucide-react';

export const Layout: React.FC = () => {
  const { user, logout, hasRole } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    { name: 'Dashboard', path: '/', icon: LayoutGrid, roles: ['viewer', 'analyst', 'admin'] },
    { name: 'Alerts Feed', path: '/alerts', icon: ShieldAlert, roles: ['viewer', 'analyst', 'admin'] },
    { name: 'Incidents Panel', path: '/incidents', icon: FileWarning, roles: ['viewer', 'analyst', 'admin'] },
    { name: 'Raw Logs', path: '/logs', icon: Terminal, roles: ['viewer', 'analyst', 'admin'] },
    { name: 'Ingestion Simulator', path: '/ingestion', icon: PlusSquare, roles: ['analyst', 'admin'] },
    { name: 'Report Archives', path: '/reports', icon: FolderOpen, roles: ['viewer', 'analyst', 'admin'] },
    { name: 'Audit Trails', path: '/audit', icon: UserCog, roles: ['admin'] },
  ];

  const filteredNavItems = navItems.filter(item => 
    item.roles.some(role => hasRole(role))
  );

  return (
    <div className="flex h-screen bg-slate-950 text-slate-100 overflow-hidden font-sans">
      {/* Sidebar */}
      <aside className="w-64 bg-slate-900 border-r border-slate-800 flex flex-col justify-between shrink-0">
        <div>
          {/* Brand header */}
          <div className="h-16 flex items-center px-6 border-b border-slate-800 gap-3">
            <ShieldIcon className="h-8 w-8 text-emerald-400 animate-pulse" />
            <div>
              <h1 className="font-bold text-lg leading-tight tracking-wider text-emerald-400">CLOUD SIEM</h1>
              <span className="text-xs text-slate-500 font-mono">v1.0.0 (SEC-OPS)</span>
            </div>
          </div>

          {/* Navigation Links */}
          <nav className="p-4 space-y-1.5">
            {filteredNavItems.map((item) => {
              const Icon = item.icon;
              const isActive = location.pathname === item.path;
              return (
                <Link
                  key={item.name}
                  to={item.path}
                  className={`flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-all duration-200 ${
                    isActive 
                      ? 'bg-emerald-500/10 text-emerald-400 border-l-2 border-emerald-500 shadow-[inset_0_0_8px_rgba(16,185,129,0.05)]' 
                      : 'text-slate-400 hover:bg-slate-800/60 hover:text-slate-200'
                  }`}
                >
                  <Icon className={`h-5 w-5 ${isActive ? 'text-emerald-400' : 'text-slate-400'}`} />
                  {item.name}
                </Link>
              );
            })}
          </nav>
        </div>

        {/* Sidebar Footer / User Panel */}
        <div className="p-4 border-t border-slate-800 bg-slate-950/40">
          {user && (
            <div className="flex flex-col gap-2">
              <div className="px-2">
                <p className="text-xs text-slate-500 font-mono uppercase tracking-widest">Active Operator</p>
                <h4 className="font-semibold text-slate-200 truncate">{user.username}</h4>
                <p className="text-xs text-slate-400 truncate mt-0.5">{user.email}</p>
                <div className="flex flex-wrap gap-1 mt-1.5">
                  {user.roles.map(role => (
                    <span key={role} className="text-[10px] font-mono font-medium bg-slate-800 text-slate-300 px-1.5 py-0.5 rounded border border-slate-700">
                      {role.replace('ROLE_', '')}
                    </span>
                  ))}
                </div>
              </div>
              <button
                onClick={handleLogout}
                className="flex items-center justify-center gap-2 w-full mt-2 px-4 py-2.5 rounded-lg text-sm font-semibold bg-rose-500/10 text-rose-400 hover:bg-rose-500 hover:text-white transition-all duration-200 border border-rose-500/20"
              >
                <LogOut className="h-4 w-4" />
                Sign Out
              </button>
            </div>
          )}
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Main Content Scroll Container */}
        <main className="flex-1 overflow-y-auto p-8 relative">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
