import React, { createContext, useState, useEffect, useContext } from 'react';
import { User } from '../types';
import { api } from '../services/api';

interface AuthContextType {
  user: User | null;
  token: string | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, email: string, roles: string[], password: string) => Promise<void>;
  logout: () => void;
  hasRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedToken = localStorage.getItem('siem_token');
    const storedUser = localStorage.getItem('siem_user');
    if (storedToken && storedUser) {
      setToken(storedToken);
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const login = async (username: string, password: string) => {
    const data = await api.post<{ token: string; id: string; username: string; email: string; roles: string[] }>(
      '/auth/signin',
      { username, password }
    );
    
    const loggedUser: User = {
      id: data.id,
      username: data.username,
      email: data.email,
      roles: data.roles,
    };

    localStorage.setItem('siem_token', data.token);
    localStorage.setItem('siem_user', JSON.stringify(loggedUser));
    setToken(data.token);
    setUser(loggedUser);
  };

  const register = async (username: string, email: string, roles: string[], password: string) => {
    await api.post('/auth/signup', { username, email, role: roles, password });
  };

  const logout = () => {
    localStorage.removeItem('siem_token');
    localStorage.removeItem('siem_user');
    setToken(null);
    setUser(null);
  };

  const hasRole = (role: string) => {
    if (!user) return false;
    // Spring security roles returned have ROLE_ prefix
    const prefixedRole = role.startsWith('ROLE_') ? role : `ROLE_${role.toUpperCase()}`;
    return user.roles.includes(prefixedRole);
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, logout, hasRole }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
