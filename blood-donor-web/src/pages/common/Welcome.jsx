import { useNavigate } from 'react-router-dom';
import { useLanguage } from '../../context/LanguageContext';
import { HeartHandshake } from 'lucide-react';

export default function Welcome() {
  const { t } = useLanguage();
  const navigate = useNavigate();

  return (
    <div className="auth-container">
      <div className="auth-card" style={{ maxWidth: '400px', padding: '40px', display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center' }}>
        <div style={{ marginBottom: '32px' }}>
          <HeartHandshake size={72} style={{ color: '#dc2626', marginBottom: '16px' }} />
          <h1 style={{
            fontSize: '28px',
            fontWeight: '700',
            color: '#0f172a',
            margin: 0,
            letterSpacing: '-0.5px',
            lineHeight: '120%'
          }}>
            {t.appTitle}
          </h1>
          <p style={{ color: '#64748b', fontSize: '15px', marginTop: '8px' }}>
            Connecting lives, saving futures.
          </p>
        </div>

        <h2 style={{ fontSize: '20px', fontWeight: 600, color: 'var(--text-title)', marginBottom: '32px' }}>
          {t.welcomeBack}
        </h2>

        <div style={{ display: 'flex', flexDirection: 'column', width: '100%', gap: '16px' }}>
          <button
            type="button"
            className="btn btn-primary"
            style={{ width: '100%', padding: '14px 20px', fontSize: '16px' }}
            onClick={() => navigate('/login')}
          >
            {t.login}
          </button>

          <button
            type="button"
            className="btn btn-outline"
            style={{ width: '100%', padding: '14px 20px', fontSize: '16px', borderColor: 'var(--primary)', color: 'var(--primary)' }}
            onClick={() => navigate('/register')}
          >
            Create Account
          </button>
        </div>
      </div>
    </div>
  );
}
