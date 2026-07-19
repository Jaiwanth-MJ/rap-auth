import React, { useState, useEffect, useRef } from 'react';
import { Shield, ArrowRight, RefreshCw, Key, Fingerprint, Clock, Lock, Check } from 'lucide-react';

const API_BASE = 'http://localhost:8080/api';

export default function Login({ onLoginSuccess, onNavigate }) {
  // Step 1: 'INITIATE', Step 2: 'CHALLENGE'
  const [step, setStep] = useState('INITIATE');
  const [registerNumber, setRegisterNumber] = useState('');
  
  // Session details from initiate API
  const [session, setSession] = useState({
    sessionId: null,
    userId: null,
    fullName: '',
    registerNumber: '',
    authenticationMethod: '',
    expiresInSeconds: 60,
  });

  // Verification states
  const [credentialInput, setCredentialInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [infoMessage, setInfoMessage] = useState(null);

  // Regeneration count (limits to 2)
  const [regenerationCount, setRegenerationCount] = useState(0);

  // Timer states
  const [timeLeft, setTimeLeft] = useState(60);
  const timerRef = useRef(null);

  // Fingerprint simulation states
  const [fingerprintScanning, setFingerprintScanning] = useState(false);
  const [fingerprintSuccess, setFingerprintSuccess] = useState(false);

  // Handle Timeout / Expiration
  const handleTimeout = React.useCallback(async () => {
    setLoading(true);
    setError(null);
    setInfoMessage(null);
    setCredentialInput('');
    setFingerprintScanning(false);
    setFingerprintSuccess(false);

    try {
      const response = await fetch(`${API_BASE}/auth/timeout`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ sessionId: session.sessionId }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || 'Timeout processing failed');
      }

      if (data.locked) {
        setError(data.message);
        setSession((prev) => ({
          ...prev,
          expiresInSeconds: 0,
        }));
        setTimeLeft(0);
      } else {
        setSession((prev) => ({
          ...prev,
          authenticationMethod: data.authenticationMethod,
          expiresInSeconds: data.expiresInSeconds || 60,
        }));
        setRegenerationCount(data.regenerationCount);
        setInfoMessage(`Time expired. Authentication method rotated. Chance: ${data.regenerationCount}/2`);
      }
    } catch (err) {
      setError(err.message || 'Timeout processing failed');
    } finally {
      setLoading(false);
    }
  }, [session.sessionId]);

  // Start timer on entering Step 2
  useEffect(() => {
    if (step === 'CHALLENGE') {
      setTimeLeft(session.expiresInSeconds);
      
      if (timerRef.current) clearInterval(timerRef.current);
      
      timerRef.current = setInterval(() => {
        setTimeLeft((prev) => {
          if (prev <= 1) {
            clearInterval(timerRef.current);
            handleTimeout();
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    }

    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [step, session.sessionId, session.authenticationMethod, handleTimeout]);

  // Clean up timer on unmount
  useEffect(() => {
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, []);

  // Initiate Login Request
  const handleInitiate = async (e) => {
    e.preventDefault();
    if (!registerNumber.trim()) return;

    setLoading(true);
    setError(null);
    setInfoMessage(null);

    try {
      const response = await fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ registerNumber: registerNumber.trim() }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || 'User not found or login initialization failed');
      }

      setSession({
        sessionId: data.sessionId,
        userId: data.userId,
        fullName: data.fullName,
        registerNumber: data.registerNumber,
        authenticationMethod: data.authenticationMethod,
        expiresInSeconds: data.expiresInSeconds || 60,
      });

      setRegenerationCount(0);
      setCredentialInput('');
      setFingerprintScanning(false);
      setFingerprintSuccess(false);
      setStep('CHALLENGE');
    } catch (err) {
      setError(err.message || 'Failed to initiate authentication');
    } finally {
      setLoading(false);
    }
  };

  // Verify Login Request
  const handleVerify = async (credentialToSend) => {
    const cred = credentialToSend || credentialInput;
    if (!cred && session.authenticationMethod === 'PASSWORD') {
      setError('Password is required');
      return;
    }

    setLoading(true);
    setError(null);
    setInfoMessage(null);

    try {
      const response = await fetch(`${API_BASE}/auth/verify`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          sessionId: session.sessionId,
          credential: cred,
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || 'Verification failed');
      }

      if (!data.success) {
        throw new Error(data.message || 'Invalid credential');
      }

      // Login Successful!
      if (timerRef.current) clearInterval(timerRef.current);
      onLoginSuccess(data.token, {
        id: session.userId,
        fullName: session.fullName,
        registerNumber: session.registerNumber,
      });
    } catch (err) {
      setError(err.message || 'Verification failed');
    } finally {
      setLoading(false);
    }
  };

  // Regenerate/Switch Authentication Method
  const handleRegenerate = async () => {
    if (regenerationCount >= 2) return;

    setLoading(true);
    setError(null);
    setInfoMessage(null);
    setCredentialInput('');
    setFingerprintScanning(false);
    setFingerprintSuccess(false);

    try {
      const response = await fetch(`${API_BASE}/auth/regenerate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ sessionId: session.sessionId }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || 'Failed to switch method');
      }

      setSession((prev) => ({
        ...prev,
        authenticationMethod: data.authenticationMethod,
        sessionId: data.sessionId,
        expiresInSeconds: data.expiresInSeconds || 60,
      }));

      setRegenerationCount(data.regenerationCount);
      setInfoMessage(`Authentication method rotated successfully. Attempts: ${data.regenerationCount}/2`);
    } catch (err) {
      setError(err.message || 'Failed to switch authentication method');
    } finally {
      setLoading(false);
    }
  };

  // Fingerprint Scanner Simulation Action
  const triggerFingerprintScan = () => {
    if (fingerprintScanning || fingerprintSuccess || timeLeft === 0) return;
    
    setFingerprintScanning(true);
    setError(null);

    setTimeout(() => {
      setFingerprintScanning(false);
      setFingerprintSuccess(true);
      const simulatedCred = `${session.registerNumber}-fingerprint`;
      setCredentialInput(simulatedCred);
      // Auto-submit after success
      handleVerify(simulatedCred);
    }, 1200);
  };

  // Passkey Simulation Action
  const triggerPasskeyFill = () => {
    const cred = `${session.registerNumber}-passkey`;
    setCredentialInput(cred);
    handleVerify(cred);
  };

  // Authenticator auto-fill
  const triggerAuthenticatorFill = () => {
    const cred = '123456';
    setCredentialInput(cred);
    handleVerify(cred);
  };

  const handleReset = () => {
    setStep('INITIATE');
    setRegisterNumber('');
    setError(null);
    setInfoMessage(null);
    if (timerRef.current) clearInterval(timerRef.current);
  };

  if (step === 'INITIATE') {
    return (
      <div className="panel">
        <div className="panel-header">
          <span className="panel-title">PORTAL GATEWAY</span>
          <Shield size={20} className="text-secondary" />
        </div>
        <div className="panel-body">
          {error && (
            <div className="alert alert-error">
              {error}
            </div>
          )}

          <form onSubmit={handleInitiate}>
            <div className="form-group">
              <label className="form-label" htmlFor="regNumberInput">REGISTER NUMBER</label>
              <input
                type="text"
                id="regNumberInput"
                required
                className="form-input form-input-mono"
                placeholder="Enter register number (e.g. REG123)"
                value={registerNumber}
                onChange={(e) => setRegisterNumber(e.target.value)}
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="btn btn-primary"
              style={{ marginBottom: '1rem' }}
            >
              {loading ? 'VERIFYING...' : 'INITIATE LOGIN'}
            </button>

            <button
              type="button"
              onClick={() => onNavigate('register')}
              className="btn btn-secondary"
            >
              CREATE NEW PROFILE
            </button>
          </form>
        </div>
      </div>
    );
  }

  // Challenge Step
  return (
    <div className="panel">
      <div className="panel-header">
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <span className="panel-title" style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)' }}>SESSION VERIFICATION</span>
          <span style={{ fontSize: '1rem', fontWeight: '700', fontFamily: 'var(--font-mono)' }}>{session.fullName}</span>
        </div>
        <Lock size={18} style={{ color: 'var(--accent-amber)' }} />
      </div>
      <div className="panel-body">
        
        {/* Status Timer bar */}
        <div className="auth-status-bar">
          <div className={`timer-indicator ${timeLeft <= 15 ? 'warning' : ''}`}>
            <Clock size={16} />
            <span>{timeLeft > 0 ? `${timeLeft}s REMAINING` : 'EXPIRED'}</span>
          </div>
          <div className="attempts-left">
            SWITCHES: {regenerationCount}/2
          </div>
        </div>

        {error && (
          <div className="alert alert-error">
            {error}
          </div>
        )}

        {infoMessage && (
          <div className="alert alert-info">
            {infoMessage}
          </div>
        )}

        {timeLeft > 0 ? (
          <div>
            <div style={{ marginBottom: '1.5rem', textAlign: 'center' }}>
              <span style={{ fontSize: '0.75rem', textTransform: 'uppercase', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
                REQUIRED VERIFICATION METHOD:
              </span>
              <div style={{ fontSize: '1.25rem', fontWeight: '700', color: 'var(--accent-amber)', fontFamily: 'var(--font-mono)', marginTop: '0.25rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                {session.authenticationMethod}
              </div>
            </div>

            {/* PASSWORD METHOD UI */}
            {session.authenticationMethod === 'PASSWORD' && (
              <div className="form-group">
                <label className="form-label">ENTER ACCOUNT PASSWORD</label>
                <input
                  type="password"
                  className="form-input"
                  placeholder="Password"
                  autoFocus
                  value={credentialInput}
                  onChange={(e) => setCredentialInput(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleVerify()}
                />
              </div>
            )}

            {/* PASSKEY METHOD UI */}
            {session.authenticationMethod === 'PASSKEY' && (
              <div className="sim-widget">
                <Key size={36} style={{ color: 'var(--accent-amber)' }} />
                <span style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)' }}>
                  Simulating Secure WebAuthn Passkey Token
                </span>
                
                <div style={{ width: '100%' }}>
                  <button
                    onClick={triggerPasskeyFill}
                    disabled={loading}
                    className="btn btn-primary"
                    style={{ background: 'var(--accent-blue)', color: '#000', marginBottom: '0.75rem' }}
                  >
                    AUTHENTICATE WITH PASSKEY
                  </button>
                  
                  <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
                    Simulated Token Value: {session.registerNumber}-passkey
                  </div>
                </div>
              </div>
            )}

            {/* FINGERPRINT METHOD UI */}
            {session.authenticationMethod === 'FINGERPRINT' && (
              <div className="sim-widget">
                <button
                  type="button"
                  onClick={triggerFingerprintScan}
                  disabled={fingerprintScanning || fingerprintSuccess || loading}
                  className={`sim-scanner-btn ${fingerprintScanning ? 'active' : ''} ${fingerprintSuccess ? 'success' : ''}`}
                >
                  {fingerprintSuccess ? (
                    <Check size={32} style={{ color: 'var(--status-success)' }} />
                  ) : (
                    <Fingerprint size={32} />
                  )}
                </button>
                <span style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)' }}>
                  {fingerprintScanning ? 'SCANNING BIOMETRICS...' : fingerprintSuccess ? 'SCAN COMPLETE' : 'PRESS AND HOLD TO SIMULATE SCAN'}
                </span>
                <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
                  Simulated Sensor Target: {session.registerNumber}-fingerprint
                </div>
              </div>
            )}

            {/* AUTHENTICATOR METHOD UI */}
            {session.authenticationMethod === 'AUTHENTICATOR' && (
              <div className="form-group" style={{ textAlign: 'center' }}>
                <label className="form-label" style={{ textAlign: 'left' }}>ENTER 6-DIGIT AUTHENTICATOR PIN</label>
                <input
                  type="text"
                  maxLength={6}
                  className="form-input form-input-mono"
                  style={{ textAlign: 'center', fontSize: '1.25rem', letterSpacing: '0.3em', paddingLeft: '1.3em' }}
                  placeholder="000000"
                  autoFocus
                  value={credentialInput}
                  onChange={(e) => setCredentialInput(e.target.value.replace(/\D/g, ''))}
                  onKeyDown={(e) => e.key === 'Enter' && handleVerify()}
                />
                
                <button
                  type="button"
                  onClick={triggerAuthenticatorFill}
                  className="btn btn-secondary"
                  style={{ marginTop: '1rem', width: 'auto', padding: '0.5rem 1rem', fontSize: '0.75rem' }}
                >
                  AUTO-FILL CODE (123456)
                </button>
              </div>
            )}

            {/* VERIFY BUTTON (Only visible for fields that require manual typing) */}
            {(session.authenticationMethod === 'PASSWORD' || session.authenticationMethod === 'AUTHENTICATOR') && (
              <button
                onClick={() => handleVerify()}
                disabled={loading}
                className="btn btn-primary"
                style={{ marginBottom: '1rem' }}
              >
                {loading ? 'VERIFYING...' : 'VERIFY CREDENTIAL'}
              </button>
            )}

            {/* SWITCH METHOD BUTTON */}
            {regenerationCount < 2 && (
              <button
                type="button"
                onClick={handleRegenerate}
                disabled={loading}
                className="btn btn-secondary"
                style={{ marginBottom: '1rem', borderStyle: 'dashed' }}
              >
                <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
                SWITCH AUTHENTICATION METHOD
              </button>
            )}
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: '1rem 0' }}>
            <button onClick={handleReset} className="btn btn-primary">
              RESTART LOGIN FLOW
            </button>
          </div>
        )}

        <button
          type="button"
          onClick={handleReset}
          className="btn btn-secondary"
          style={{ marginTop: timeLeft > 0 ? '0' : '1rem' }}
        >
          CANCEL
        </button>
      </div>
    </div>
  );
}
