import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useLanguage } from '../../context/LanguageContext';
import authApi from '../../api/authApi';
import { HeartHandshake, AlertCircle } from 'lucide-react';

export default function ResetPassword() {
  const { t } = useLanguage();
  const location = useLocation();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (location.state?.email) {
      setEmail(location.state.email);
    } else {
      navigate('/forgot-password');
    }
  }, [location, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await authApi.resetPassword({
        email: email,
        otp_code: otp,
        new_password: password,
      });
      alert('Password reset successfully. You can now login.');
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid code or password configuration.');
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

        <h2 style={{ marginBottom: '12px', fontWeight: 600 }}>Set New Password</h2>
        <p style={{ fontSize: '14px', color: 'var(--text-muted)', marginBottom: '24px', lineHeight: '140%' }}>
          Enter the verification code sent to <strong style={{ color: 'var(--text-title)' }}>{email}</strong> and choose a new password.
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
            <label className="form-label">{t.otpCode}</label>
            <input
              type="text"
              className="form-input"
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              required
              placeholder="Enter reset code"
              style={{ textAlign: 'center', fontSize: '18px', fontWeight: 600 }}
            />
          </div>

          <div className="form-group">
            <label className="form-label">New Password</label>
            <input
              type="password"
              className="form-input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              placeholder="Enter new password"
            />
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: '100%', marginBottom: '16px' }} disabled={loading}>
            {loading ? 'Resetting...' : 'Change Password'}
          </button>
          
          <button type="button" className="btn btn-outline" style={{ width: '100%' }} onClick={() => navigate('/forgot-password')}>
            Back
          </button>
        </form>
      </div>
    </div>
  );
}
