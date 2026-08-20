import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useLanguage } from '../../context/LanguageContext';
import authApi from '../../api/authApi';
import { HeartHandshake, AlertCircle } from 'lucide-react';

export default function Login() {
  const { login } = useAuth();
  const { t } = useLanguage();
  const navigate = useNavigate();
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const data = await authApi.login({
        phone_number: identifier, // The backend identifier field checks both email & phone number
        password: password,
      });

      login(data.user, data.token);
      navigate('/dashboard');
    } catch (err) {
      if (err.response && err.response.status === 403 && err.response.data.unverified) {
        // Redirect to OTP verification screen
        const email = err.response.data.email;
        navigate(`/verify-otp/${encodeURIComponent(email)}`);
      } else {
        setError(err.response?.data?.message || 'Login failed. Please check credentials.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-logo">
          <HeartHandshake />
          <h1>{t.appTitle}</h1>
        </div>

        <h2 style={{ marginBottom: '24px', fontWeight: 600 }}>{t.welcomeBack}</h2>

        {error && (
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            backgroundColor: '#fee2e2',
            color: '#ef4444',
            padding: '12px 16px',
            borderRadius: '8px',
            marginBottom: '20px',
            fontSize: '14px',
            textAlign: 'left'
          }}>
            <AlertCircle size={18} style={{ flexShrink: 0 }} />
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">{t.phoneNumber} / {t.emailAddress}</label>
            <input
              type="text"
              className="form-input"
              value={identifier}
              onChange={(e) => setIdentifier(e.target.value)}
              required
              placeholder="e.g. 1234567890 or mail@example.com"
            />
          </div>

          <div className="form-group">
            <label className="form-label">{t.password}</label>
            <input
              type="password"
              className="form-input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              placeholder="••••••••"
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '24px' }}>
            <Link to="/forgot-password" style={{ color: 'var(--primary)', fontSize: '14px', textDecoration: 'none', fontWeight: 500 }}>
              Forgot Password?
            </Link>
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={loading}>
            {loading ? 'Logging in...' : t.login}
          </button>
        </form>

        <p style={{ marginTop: '24px', fontSize: '14px', color: 'var(--text-muted)' }}>
          {t.dontHaveAccount}{' '}
          <Link to="/register" style={{ color: 'var(--primary)', fontWeight: 600, textDecoration: 'none' }}>
            {t.signup}
          </Link>
        </p>
      </div>
    </div>
  );
}
