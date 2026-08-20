import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useLanguage } from '../../context/LanguageContext';
import authApi from '../../api/authApi';
import { HeartHandshake, AlertCircle } from 'lucide-react';

export default function VerifyOtp() {
  const { email } = useParams();
  const { login } = useAuth();
  const { t } = useLanguage();
  const navigate = useNavigate();
  const [otp, setOtp] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!email) {
      navigate('/login');
    }
  }, [email, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');
    setLoading(true);

    try {
      const data = await authApi.verifyOtp(decodeURIComponent(email), otp);
      login(data.user, data.token);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid or expired OTP. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    setError('');
    setMessage('');
    
    try {
      await authApi.sendOtp(decodeURIComponent(email));
      setMessage('A new verification code has been sent to your email.');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to resend OTP. Please try again later.');
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-logo">
          <HeartHandshake />
          <h1>{t.appTitle}</h1>
        </div>

        <h2 style={{ marginBottom: '12px', fontWeight: 600 }}>{t.verifyOtp}</h2>
        <p style={{ fontSize: '14px', color: 'var(--text-muted)', marginBottom: '24px', lineHeight: '140%' }}>
          {t.verifyOtpDesc} <strong style={{ color: 'var(--text-title)' }}>{decodeURIComponent(email)}</strong>.
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

        {message && (
          <div style={{
            backgroundColor: '#f0fdf4',
            color: '#16a34a',
            padding: '12px 16px',
            borderRadius: '8px',
            marginBottom: '20px',
            fontSize: '14px',
            textAlign: 'left',
            fontWeight: 500
          }}>
            {message}
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
              maxLength="6"
              placeholder="Enter 6-digit code"
              style={{ letterSpacing: '4px', textAlign: 'center', fontSize: '20px', fontWeight: 700 }}
            />
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: '100%', marginBottom: '16px' }} disabled={loading}>
            {loading ? 'Verifying...' : t.verify}
          </button>
          
          <button type="button" className="btn btn-outline" style={{ width: '100%' }} onClick={handleResend}>
            {t.resendOtp}
          </button>
        </form>
      </div>
    </div>
  );
}
