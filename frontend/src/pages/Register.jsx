import React, { useState } from 'react';
import { ShieldCheck, UserPlus, Info, Check } from 'lucide-react';

const API_BASE = 'http://localhost:8080/api';

export default function Register({ onNavigate }) {
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    registerNumber: '',
    password: '',
    role: 'STUDENT',
  });
  
  const [authMethods, setAuthMethods] = useState(['PASSWORD']);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successData, setSuccessData] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleToggleMethod = (method) => {
    setAuthMethods((prev) => {
      if (prev.includes(method)) {
        // Must have at least one method
        if (prev.length === 1) return prev;
        return prev.filter((m) => m !== method);
      } else {
        return [...prev, method];
      }
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccessData(null);

    if (authMethods.length === 0) {
      setError('Select at least one authentication method.');
      setLoading(false);
      return;
    }

    try {
      const response = await fetch(`${API_BASE}/auth/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          ...formData,
          authenticationMethods: authMethods,
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || 'Registration failed');
      }

      setSuccessData(data);
    } catch (err) {
      setError(err.message || 'An unexpected error occurred');
    } finally {
      setLoading(false);
    }
  };

  if (successData) {
    return (
      <div className="panel">
        <div className="panel-header">
          <span className="panel-title">REGISTRATION SUCCESSFUL</span>
          <ShieldCheck size={20} className="text-success" style={{ color: 'var(--status-success)' }} />
        </div>
        <div className="panel-body">
          <div className="alert alert-success">
            Account created successfully for {successData.fullName} ({successData.registerNumber}).
          </div>

          <h3 style={{ fontSize: '1rem', fontFamily: 'var(--font-mono)', marginBottom: '1rem', textTransform: 'uppercase' }}>
            YOUR SIMULATED CREDENTIALS
          </h3>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginBottom: '2rem' }}>
            {authMethods.includes('PASSWORD') && (
              <div style={{ backgroundColor: 'var(--bg-main)', padding: '0.75rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-muted)' }}>
                <span className="profile-field-label">PASSWORD METHOD</span>
                <div style={{ fontFamily: 'var(--font-mono)', color: 'var(--accent-amber)', fontSize: '0.875rem', marginTop: '0.25rem' }}>
                  Use password: <strong style={{ color: '#fff' }}>{formData.password}</strong>
                </div>
              </div>
            )}
            {authMethods.includes('PASSKEY') && (
              <div style={{ backgroundColor: 'var(--bg-main)', padding: '0.75rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-muted)' }}>
                <span className="profile-field-label">PASSKEY METHOD</span>
                <div style={{ fontFamily: 'var(--font-mono)', color: 'var(--accent-amber)', fontSize: '0.875rem', marginTop: '0.25rem' }}>
                  Simulated PIN: <strong style={{ color: '#fff' }}>{formData.registerNumber}-passkey</strong>
                </div>
              </div>
            )}
            {authMethods.includes('FINGERPRINT') && (
              <div style={{ backgroundColor: 'var(--bg-main)', padding: '0.75rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-muted)' }}>
                <span className="profile-field-label">FINGERPRINT METHOD</span>
                <div style={{ fontFamily: 'var(--font-mono)', color: 'var(--accent-amber)', fontSize: '0.875rem', marginTop: '0.25rem' }}>
                  Simulated Scan: <strong style={{ color: '#fff' }}>{formData.registerNumber}-fingerprint</strong>
                </div>
              </div>
            )}
            {authMethods.includes('AUTHENTICATOR') && (
              <div style={{ backgroundColor: 'var(--bg-main)', padding: '0.75rem', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-muted)' }}>
                <span className="profile-field-label">AUTHENTICATOR METHOD</span>
                <div style={{ fontFamily: 'var(--font-mono)', color: 'var(--accent-amber)', fontSize: '0.875rem', marginTop: '0.25rem' }}>
                  Simulated Code: <strong style={{ color: '#fff' }}>123456</strong>
                </div>
              </div>
            )}
          </div>

          <button onClick={() => onNavigate('login')} className="btn btn-primary">
            GO TO LOGIN
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="panel">
      <div className="panel-header">
        <span className="panel-title">CREATE PROFILE</span>
        <UserPlus size={20} className="text-secondary" />
      </div>
      <div className="panel-body">
        {error && (
          <div className="alert alert-error">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="fullName">Full Name</label>
            <input
              type="text"
              id="fullName"
              name="fullName"
              required
              className="form-input"
              placeholder="e.g. John Doe"
              value={formData.fullName}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="email">Email Address</label>
            <input
              type="email"
              id="email"
              name="email"
              required
              className="form-input form-input-mono"
              placeholder="e.g. john@example.com"
              value={formData.email}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="registerNumber">Register Number</label>
            <input
              type="text"
              id="registerNumber"
              name="registerNumber"
              required
              className="form-input form-input-mono"
              placeholder="e.g. REG123"
              value={formData.registerNumber}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="password">Default Password</label>
            <input
              type="password"
              id="password"
              name="password"
              required
              className="form-input"
              placeholder="••••••••"
              value={formData.password}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="role">Role</label>
            <select
              id="role"
              name="role"
              className="form-input"
              style={{ background: 'var(--bg-main)' }}
              value={formData.role}
              onChange={handleChange}
            >
              <option value="STUDENT">STUDENT</option>
              <option value="FACULTY">FACULTY</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Enroll Authentication Methods</label>
            <div className="options-grid">
              {['PASSWORD', 'PASSKEY', 'FINGERPRINT', 'AUTHENTICATOR'].map((method) => {
                const isSelected = authMethods.includes(method);
                return (
                  <div
                    key={method}
                    onClick={() => handleToggleMethod(method)}
                    className={`option-card ${isSelected ? 'selected' : ''}`}
                  >
                    <div style={{
                      width: '16px',
                      height: '16px',
                      borderRadius: '2px',
                      border: '1px solid var(--border-muted)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      backgroundColor: isSelected ? 'var(--accent-amber)' : 'transparent',
                      borderColor: isSelected ? 'var(--accent-amber)' : 'var(--border-muted)',
                    }}>
                      {isSelected && <Check size={12} color="#000" strokeWidth={3} />}
                    </div>
                    <span className="option-label">{method}</span>
                  </div>
                );
              })}
            </div>
            
            <div style={{ display: 'flex', gap: '0.5rem', color: 'var(--text-muted)', fontSize: '0.75rem', marginTop: '0.5rem' }}>
              <Info size={14} style={{ flexShrink: 0, marginTop: '0.1rem' }} />
              <span>
                Selected methods will be randomly chosen during login. Credentials for simulation will be generated automatically.
              </span>
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="btn btn-primary"
            style={{ marginBottom: '1rem' }}
          >
            {loading ? 'CREATING PROFILE...' : 'CREATE PROFILE'}
          </button>

          <button
            type="button"
            onClick={() => onNavigate('login')}
            className="btn btn-secondary"
          >
            BACK TO LOGIN
          </button>
        </form>
      </div>
    </div>
  );
}
