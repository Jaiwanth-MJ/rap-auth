import React, { useState, useEffect } from 'react';
import { LogOut, RefreshCw, Users, ShieldAlert, BadgeCheck, Mail, Hash, UserCheck } from 'lucide-react';

const API_BASE = 'http://localhost:8080/api';

export default function Dashboard({ token, userSessionInfo, onLogout }) {
  const [currentUser, setCurrentUser] = useState(null);
  const [allUsers, setAllUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchDashboardData = async () => {
    setLoading(true);
    setError(null);
    try {
      // 1. Fetch current user from /me
      const meResponse = await fetch(`${API_BASE}/users/me`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (!meResponse.ok) {
        throw new Error('Failed to load your profile session details.');
      }
      const meData = await meResponse.json();
      setCurrentUser(meData);

      // 2. Fetch all users directory
      const usersResponse = await fetch(`${API_BASE}/users`, {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });

      if (!usersResponse.ok) {
        throw new Error('Failed to load user directory.');
      }
      const usersData = await usersResponse.json();
      setAllUsers(usersData);
    } catch (err) {
      setError(err.message || 'An error occurred fetching dashboard data.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, [token]);

  return (
    <div className="dashboard-container">
      {/* Side Column: Profile Details */}
      <div className="dashboard-side">
        <div className="user-card">
          <div className="user-card-header">
            <div className="avatar-placeholder">
              {currentUser ? currentUser.fullName.charAt(0).toUpperCase() : userSessionInfo.fullName.charAt(0).toUpperCase()}
            </div>
            <div>
              <h2 className="user-name-title">{currentUser ? currentUser.fullName : userSessionInfo.fullName}</h2>
              <span className="user-role-badge">{currentUser ? currentUser.role : 'USER'}</span>
            </div>
          </div>

          <div style={{ marginBottom: '1.5rem' }}>
            <div className="profile-field">
              <span className="profile-field-label">EMAIL ADDRESS</span>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', marginTop: '0.25rem' }}>
                <Mail size={14} style={{ color: 'var(--text-muted)' }} />
                <span className="profile-field-value">{currentUser ? currentUser.email : 'Loading...'}</span>
              </div>
            </div>

            <div className="profile-field">
              <span className="profile-field-label">REGISTER NUMBER</span>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', marginTop: '0.25rem' }}>
                <Hash size={14} style={{ color: 'var(--text-muted)' }} />
                <span className="profile-field-value">{currentUser ? currentUser.registerNumber : userSessionInfo.registerNumber}</span>
              </div>
            </div>

            <div className="profile-field">
              <span className="profile-field-label">STATUS</span>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', marginTop: '0.25rem' }}>
                <UserCheck size={14} style={{ color: 'var(--status-success)' }} />
                <span className="profile-field-value" style={{ color: 'var(--status-success)' }}>
                  {currentUser && currentUser.active ? 'ACTIVE SESSION' : 'OFFLINE'}
                </span>
              </div>
            </div>
          </div>

          <button onClick={onLogout} className="btn btn-danger">
            <LogOut size={16} />
            TERMINATE SESSION
          </button>
        </div>

        {error && (
          <div className="alert alert-error">
            <ShieldAlert size={16} style={{ flexShrink: 0 }} />
            <span>{error}</span>
          </div>
        )}
      </div>

      {/* Main Column: Users Directory */}
      <div className="users-list">
        <div className="panel-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Users size={18} style={{ color: 'var(--accent-amber)' }} />
            <span className="panel-title">SYSTEM USER DIRECTORY</span>
          </div>
          <button 
            disabled={loading}
            onClick={fetchDashboardData}
            className="btn btn-secondary"
            style={{ width: 'auto', padding: '0.4rem 0.8rem', display: 'flex', gap: '0.25rem' }}
          >
            <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
            REFRESH
          </button>
        </div>

        <div className="table-container">
          {loading && allUsers.length === 0 ? (
            <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
              RETRIEVING ENCRYPTED DIRECTORY...
            </div>
          ) : allUsers.length === 0 ? (
            <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
              NO REGISTERED USERS FOUND.
            </div>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>User Details</th>
                  <th>Register No</th>
                  <th>Role</th>
                  <th>Enrolled Methods</th>
                </tr>
              </thead>
              <tbody>
                {allUsers.map((u) => {
                  const isSelf = u.registerNumber === userSessionInfo.registerNumber;
                  return (
                    <tr key={u.id} style={isSelf ? { backgroundColor: 'rgba(255, 184, 0, 0.02)' } : {}}>
                      <td>
                        <div style={{ display: 'flex', flexDirection: 'column' }}>
                          <span style={{ fontWeight: 600, color: isSelf ? 'var(--accent-amber)' : 'var(--text-primary)' }}>
                            {u.fullName} {isSelf && '(You)'}
                          </span>
                          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
                            {u.email}
                          </span>
                        </div>
                      </td>
                      <td style={{ fontFamily: 'var(--font-mono)' }}>{u.registerNumber}</td>
                      <td>
                        <span className="user-role-badge" style={{ marginTop: 0 }}>{u.role}</span>
                      </td>
                      <td>
                        <div className="methods-badges-list">
                          {u.authenticationMethods && u.authenticationMethods.map((m) => (
                            <span key={m} className="method-tag">
                              {m}
                            </span>
                          ))}
                          {(!u.authenticationMethods || u.authenticationMethods.length === 0) && (
                            <span style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>None</span>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}
