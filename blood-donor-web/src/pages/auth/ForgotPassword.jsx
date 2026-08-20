import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useLanguage } from '../../context/LanguageContext';
import authApi from '../../api/authApi';
import { HeartHandshake, AlertCircle } from 'lucide-react';

export default function ForgotPassword() {
  const { t } = useLanguage();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await authApi.forgotPassword(email);
      // Navigate to reset password page, passing the email as state
      navigate('/reset-password', { state: { email } });
    } catch (err) {
      setError(err.response?.data?.message || 'User with this email was not found.');
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

        <h2 style={{ marginBottom: '12px', fontWeight: 600 }}>Reset Password</h2>
        <p style={{ fontSize: '14px', color: 'var(--text-muted)', marginBottom: '24px', lineHeight: '140%' }}>
          Enter your registered email address to receive a verification code for password reset.
        </p>

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
            <label className="form-label">{t.emailAddress || 'Email Address'}</label>
            <input
              type="email"
              className="form-input"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              placeholder="Enter your registered email address"
            />
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: '100%', marginBottom: '16px' }} disabled={loading}>
            {loading ? 'Sending code...' : 'Request Reset OTP'}
          </button>
          
          <button type="button" className="btn btn-outline" style={{ width: '100%' }} onClick={() => navigate('/login')}>
            Back to Login
          </button>
        </form>
      </div>
    </div>
  );
}
