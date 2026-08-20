import { useNavigate } from 'react-router-dom';
import { useLanguage } from '../../context/LanguageContext';
import { Languages, Globe } from 'lucide-react';

export default function LanguageSelection() {
  const { lang, changeLanguage } = useLanguage();
  const navigate = useNavigate();

  const handleLanguageSelect = (selectedLang) => {
    changeLanguage(selectedLang);
    navigate('/onboarding');
  };

  return (
    <div className="auth-container">
      <div className="auth-card" style={{ maxWidth: '400px' }}>
        <div className="auth-logo" style={{ marginBottom: '20px' }}>
          <Languages size={48} style={{ color: 'var(--primary)', marginBottom: '12px' }} />
          <h1 style={{ fontSize: '24px' }}>Select Preferred Language</h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '14px', marginTop: '4px' }}>
            कृपया अपनी भाषा चुनें / உங்கள் மொழியைத் தேர்ந்தெடுக்கவும்
          </p>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginTop: '24px' }}>
          <button
            type="button"
            className="btn btn-outline"
            style={{
              padding: '16px 20px',
              justifyContent: 'space-between',
              fontSize: '16px',
              borderColor: lang === 'en' ? 'var(--primary)' : 'var(--border)',
              backgroundColor: lang === 'en' ? 'var(--primary-light)' : 'transparent',
              color: lang === 'en' ? 'var(--primary)' : 'var(--text-title)'
            }}
            onClick={() => handleLanguageSelect('en')}
          >
            <span>English</span>
            <Globe size={18} />
          </button>

          <button
            type="button"
            className="btn btn-outline"
            style={{
              padding: '16px 20px',
              justifyContent: 'space-between',
              fontSize: '16px',
              borderColor: lang === 'hi' ? 'var(--primary)' : 'var(--border)',
              backgroundColor: lang === 'hi' ? 'var(--primary-light)' : 'transparent',
              color: lang === 'hi' ? 'var(--primary)' : 'var(--text-title)'
            }}
            onClick={() => handleLanguageSelect('hi')}
          >
            <span>हिन्दी (Hindi)</span>
            <Globe size={18} />
          </button>

          <button
            type="button"
            className="btn btn-outline"
            style={{
              padding: '16px 20px',
              justifyContent: 'space-between',
              fontSize: '16px',
              borderColor: lang === 'ta' ? 'var(--primary)' : 'var(--border)',
              backgroundColor: lang === 'ta' ? 'var(--primary-light)' : 'transparent',
              color: lang === 'ta' ? 'var(--primary)' : 'var(--text-title)'
            }}
            onClick={() => handleLanguageSelect('ta')}
          >
            <span>தமிழ் (Tamil)</span>
            <Globe size={18} />
          </button>
        </div>
      </div>
    </div>
  );
}
