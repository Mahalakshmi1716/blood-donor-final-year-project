import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useLanguage } from '../../context/LanguageContext';
import authApi from '../../api/authApi';
import { ArrowLeft, Languages, Globe, Save } from 'lucide-react';

export default function Settings() {
  const { user, updateUserState } = useAuth();
  const { lang, changeLanguage, t } = useLanguage();
  const navigate = useNavigate();
  const [selectedLang, setSelectedLang] = useState(lang);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState('');

  const handleSave = async () => {
    setSaving(true);
    setSuccess('');

    try {
      if (user) {
        const data = await authApi.updateProfile({
          preferred_language: selectedLang,
        });
        updateUserState(data.user);
      }
      changeLanguage(selectedLang);
      setSuccess('Settings updated successfully.');
    } catch (err) {
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto', textAlign: 'left' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px' }}>
        <button type="button" className="btn btn-outline" style={{ padding: '8px' }} onClick={() => navigate('/dashboard')}>
          <ArrowLeft size={20} />
        </button>
        <h1 style={{ fontSize: '24px', margin: 0, fontWeight: 700 }}>{t.settings}</h1>
      </div>

      {success && (
        <div style={{
          backgroundColor: '#f0fdf4',
          color: '#16a34a',
          padding: '12px 16px',
          borderRadius: '8px',
          marginBottom: '20px',
          fontSize: '14px',
          fontWeight: 500
        }}>
          {success}
        </div>
      )}

      <div style={{
        backgroundColor: 'white',
        borderRadius: 'var(--radius)',
        border: '1px solid var(--border)',
        boxShadow: 'var(--shadow-sm)',
        padding: '32px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px' }}>
          <Languages size={24} style={{ color: 'var(--primary)' }} />
          <h2 style={{ fontSize: '18px', fontWeight: 600, color: 'var(--text-title)', margin: 0 }}>App Language / भाषा / மொழி</h2>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '32px' }}>
          <label style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '16px 20px',
            borderRadius: '8px',
            border: '1.5px solid',
            borderColor: selectedLang === 'en' ? 'var(--primary)' : 'var(--border)',
            backgroundColor: selectedLang === 'en' ? 'var(--primary-light)' : 'transparent',
            cursor: 'pointer'
          }}>
            <span style={{ fontSize: '16px', fontWeight: 500, color: 'var(--text-title)' }}>English</span>
            <input
              type="radio"
              name="lang"
              checked={selectedLang === 'en'}
              onChange={() => setSelectedLang('en')}
              style={{ width: '18px', height: '18px' }}
            />
          </label>

          <label style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '16px 20px',
            borderRadius: '8px',
            border: '1.5px solid',
            borderColor: selectedLang === 'hi' ? 'var(--primary)' : 'var(--border)',
            backgroundColor: selectedLang === 'hi' ? 'var(--primary-light)' : 'transparent',
            cursor: 'pointer'
          }}>
            <span style={{ fontSize: '16px', fontWeight: 500, color: 'var(--text-title)' }}>हिन्दी (Hindi)</span>
            <input
              type="radio"
              name="lang"
              checked={selectedLang === 'hi'}
              onChange={() => setSelectedLang('hi')}
              style={{ width: '18px', height: '18px' }}
            />
          </label>

          <label style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '16px 20px',
            borderRadius: '8px',
            border: '1.5px solid',
            borderColor: selectedLang === 'ta' ? 'var(--primary)' : 'var(--border)',
            backgroundColor: selectedLang === 'ta' ? 'var(--primary-light)' : 'transparent',
            cursor: 'pointer'
          }}>
            <span style={{ fontSize: '16px', fontWeight: 500, color: 'var(--text-title)' }}>தமிழ் (Tamil)</span>
            <input
              type="radio"
              name="lang"
              checked={selectedLang === 'ta'}
              onChange={() => setSelectedLang('ta')}
              style={{ width: '18px', height: '18px' }}
            />
          </label>
        </div>

        <button type="button" className="btn btn-primary" style={{ width: '100%' }} onClick={handleSave} disabled={saving}>
          <Save size={16} />
          <span>{saving ? 'Saving...' : 'Save Settings'}</span>
        </button>
      </div>
    </div>
  );
}
