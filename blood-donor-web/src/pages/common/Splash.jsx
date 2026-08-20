import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { HeartHandshake } from 'lucide-react';

export default function Splash() {
  const navigate = useNavigate();

  useEffect(() => {
    const timer = setTimeout(() => {
      const token = localStorage.getItem('token');
      if (token) {
        navigate('/dashboard');
      } else {
        navigate('/select-language');
      }
    }, 2000);

    return () => clearTimeout(timer);
  }, [navigate]);

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'radial-gradient(circle at 10% 20%, rgba(254, 242, 242, 1) 0%, rgba(254, 244, 244, 1) 90%)',
    }}>
      <div style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        animation: 'pulse 1.8s infinite ease-in-out'
      }}>
        <HeartHandshake size={80} style={{ color: '#dc2626', marginBottom: '20px' }} />
        <h1 style={{
          fontSize: '36px',
          fontWeight: '700',
          color: '#0f172a',
          margin: 0,
          letterSpacing: '-1px'
        }}>
          Smart Blood Donor Finder
        </h1>
        <p style={{ color: '#64748b', fontSize: '16px', marginTop: '12px' }}>
          Connecting lives, saving futures.
        </p>
      </div>

      <style>{`
        @keyframes pulse {
          0% { transform: scale(0.98); opacity: 0.9; }
          50% { transform: scale(1.02); opacity: 1; }
          100% { transform: scale(0.98); opacity: 0.9; }
        }
      `}</style>
    </div>
  );
}
