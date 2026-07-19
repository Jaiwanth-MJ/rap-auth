import React, { useState, useEffect } from 'react';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import { ShieldAlert, Terminal, FileCode, CheckCircle } from 'lucide-react';

export default function App() {
  const [page, setPage] = useState('login');
  const [token, setToken] = useState(null);
  const [userSessionInfo, setUserSessionInfo] = useState(null);

  // Restore session from localStorage on mount
  useEffect(() => {
    const storedToken = localStorage.getItem('rapauth_token');
    const storedSession = localStorage.getItem('rapauth_session');
    
    if (storedToken && storedSession) {
      setToken(storedToken);
      setUserSessionInfo(JSON.parse(storedSession));
      setPage('dashboard');
    }
  }, []);

  const handleLoginSuccess = (jwtToken, userInfo) => {
    localStorage.setItem('rapauth_token', jwtToken);
    localStorage.setItem('rapauth_session', JSON.stringify(userInfo));
    setToken(jwtToken);
    setUserSessionInfo(userInfo);
    setPage('dashboard');
  };

  const handleLogout = () => {
    localStorage.removeItem('rapauth_token');
    localStorage.removeItem('rapauth_session');
    setToken(null);
    setUserSessionInfo(null);
    setPage('login');
  };

  const navigateTo = (destination) => {
    setPage(destination);
  };

  return (
    <div className="app-container">
      {/* Top Navigation Bar */}
      <header className="navbar">
        <div className="logo-container">
          <Terminal size={20} style={{ color: 'var(--accent-amber)' }} />
          <span className="logo-text">RAP.AUTH</span>
          <span className="logo-badge">SECURE_NODE</span>
        </div>
        
        <nav className="nav-links">
          {page === 'dashboard' ? (
            <div className="user-status">
              <span className="user-email" style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                <CheckCircle size={14} style={{ color: 'var(--status-success)' }} />
                SESSION: {userSessionInfo?.registerNumber}
              </span>
            </div>
          ) : (
            <div style={{ display: 'flex', gap: '1rem' }}>
              <button 
                onClick={() => navigateTo('login')} 
                className={`btn btn-secondary`} 
                style={{ 
                  width: 'auto', 
                  padding: '0.4rem 0.8rem', 
                  fontSize: '0.75rem',
                  borderColor: page === 'login' ? 'var(--border-focus)' : 'var(--border-muted)',
                  color: page === 'login' ? 'var(--accent-amber)' : 'var(--text-primary)'
                }}
              >
                LOGIN
              </button>
              <button 
                onClick={() => navigateTo('register')} 
                className={`btn btn-secondary`} 
                style={{ 
                  width: 'auto', 
                  padding: '0.4rem 0.8rem', 
                  fontSize: '0.75rem',
                  borderColor: page === 'register' ? 'var(--border-focus)' : 'var(--border-muted)',
                  color: page === 'register' ? 'var(--accent-amber)' : 'var(--text-primary)'
                }}
              >
                REGISTER
              </button>
            </div>
          )}
        </nav>
      </header>

      {/* Main Container Area */}
      <main className="main-content">
        {page === 'login' && (
          <Login onLoginSuccess={handleLoginSuccess} onNavigate={navigateTo} />
        )}
        {page === 'register' && (
          <Register onNavigate={navigateTo} />
        )}
        {page === 'dashboard' && (
          <Dashboard 
            token={token} 
            userSessionInfo={userSessionInfo} 
            onLogout={handleLogout} 
          />
        )}
      </main>

      {/* Footer Details */}
      <footer className="footer">
        <div>RANDOMIZED AUTHENTICATION PROTOCOL // DEV CORE 1.0</div>
        <div style={{ color: 'var(--text-muted)', marginTop: '0.25rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.25rem' }}>
          <FileCode size={12} />
          <span>REACT_VITE_SYSTEM</span>
        </div>
      </footer>
    </div>
  );
}
