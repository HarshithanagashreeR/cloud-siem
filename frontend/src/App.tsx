import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Layout } from './components/Layout';
import { Dashboard } from './components/Dashboard';
import { Alerts } from './components/Alerts';
import { Incidents } from './components/Incidents';
import { Logs } from './components/Logs';
import { Ingestion } from './components/Ingestion';
import { Reports } from './components/Reports';
import { Audit } from './components/Audit';
import { Login } from './components/Login';
import { Signup } from './components/Signup';

// Protected Route Guard
const ProtectedRoute: React.FC<{ children: React.ReactNode; requiredRole?: string }> = ({ 
  children, 
  requiredRole 
}) => {
  const { token, loading, hasRole } = useAuth();

  if (loading) {
    return (
      <div className="flex h-screen bg-slate-950 items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-emerald-500"></div>
      </div>
    );
  }

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && !hasRole(requiredRole)) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};

export const App: React.FC = () => {
  return (
    <Router>
      <AuthProvider>
        <Routes>
          {/* Public Auth Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />

          {/* Secure Operator Dashboard Routes */}
          <Route path="/" element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }>
            <Route index element={<Dashboard />} />
            <Route path="alerts" element={<Alerts />} />
            <Route path="incidents" element={<Incidents />} />
            <Route path="logs" element={<Logs />} />
            
            {/* Analyst/Admin Only */}
            <Route path="ingestion" element={
              <ProtectedRoute requiredRole="analyst">
                <Ingestion />
              </ProtectedRoute>
            } />
            
            <Route path="reports" element={<Reports />} />
            
            {/* Admin Only */}
            <Route path="audit" element={
              <ProtectedRoute requiredRole="admin">
                <Audit />
              </ProtectedRoute>
            } />
          </Route>

          {/* Fallback Catch-All */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </Router>
  );
};

export default App;
