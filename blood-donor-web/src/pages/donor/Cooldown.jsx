import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useLanguage } from '../../context/LanguageContext';
import donorApi from '../../api/donorApi';
import { ArrowLeft, Clock, ShieldCheck, Heart, Award } from 'lucide-react';

export default function Cooldown() {
  const { t } = useLanguage();
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [daysRemaining, setDaysRemaining] = useState(0);
  const [nextDate, setNextDate] = useState('');

  useEffect(() => {
    donorApi.getProfile()
      .then((data) => {
        setProfile(data.profile);
        
        // Calculate remaining cooldown days
        if (data.profile.last_donation_date) {
          const donationDate = new Date(data.profile.last_donation_date);
          const nextAvailable = new Date(donationDate.getTime() + 90 * 24 * 60 * 60 * 1000);
          
          setNextDate(nextAvailable.toLocaleDateString(undefined, {
            year: 'numeric', month: 'short', day: 'numeric'
          }));

          const today = new Date();
          const diffTime = nextAvailable.getTime() - today.getTime();
          const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
          setDaysRemaining(diffDays > 0 ? diffDays : 0);
        }
      })
      .catch((err) => {
        console.error(err);
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  if (loading) {
    return <div style={{ padding: '40px', fontSize: '18px' }}>Loading cooldown status...</div>;
  }

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto', textAlign: 'left' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px' }}>
        <button type="button" className="btn btn-outline" style={{ padding: '8px' }} onClick={() => navigate('/dashboard')}>
          <ArrowLeft size={20} />
        </button>
        <h1 style={{ fontSize: '24px', margin: 0, fontWeight: 700 }}>{t.cooldown} Status</h1>
      </div>

      <div style={{
        backgroundColor: 'white',
        borderRadius: 'var(--radius)',
        border: '1px solid var(--border)',
        boxShadow: 'var(--shadow-sm)',
        padding: '40px 32px',
        textAlign: 'center',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center'
      }}>
        {profile?.eligibility_status === 'ELIGIBLE' ? (
          <>
            <ShieldCheck size={72} style={{ color: '#16a34a', marginBottom: '20px' }} />
            <h2 style={{ fontSize: '28px', color: '#16a34a', fontWeight: 700, margin: '0 0 12px' }}>
              You Are Eligible!
            </h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '15px', maxWidth: '380px', lineHeight: '140%', margin: '0 0 24px' }}>
              You currently have no active cooldown restrictions. You can toggle your availability on the dashboard and receive emergency SOS requests.
            </p>
          </>
        ) : (
          <>
            <Clock size={72} style={{ color: '#d97706', marginBottom: '20px', animation: 'spin 20s linear infinite' }} />
            <h2 style={{ fontSize: '28px', color: '#d97706', fontWeight: 700, margin: '0 0 12px' }}>
              Cooldown Active
            </h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '15px', maxWidth: '380px', lineHeight: '140%', margin: '0 0 24px' }}>
              To protect your health, you must wait 90 days between donations. You will be eligible to donate again on:
              <br />
              <strong style={{ color: 'var(--text-title)', fontSize: '16px', display: 'inline-block', marginTop: '8px' }}>{nextDate}</strong>
            </p>

            <div style={{
              width: '100%',
              backgroundColor: '#f8fafc',
              border: '1px solid var(--border)',
              borderRadius: '8px',
              padding: '20px',
              textAlign: 'left',
              marginBottom: '28px',
              fontSize: '15px',
              display: 'flex',
              flexDirection: 'column',
              gap: '10px'
            }}>
              <div>Status: <span style={{ fontWeight: 700, color: '#d97706' }}>INELIGIBLE</span></div>
              <div>Last Donation Date: <strong style={{ color: 'var(--text-title)' }}>{profile?.last_donation_date ? new Date(profile.last_donation_date).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' }) : 'N/A'}</strong></div>
              <div>Next Eligible Date: <strong style={{ color: 'var(--text-title)' }}>{nextDate}</strong></div>
              <div>Days Remaining: <strong style={{ color: '#d97706' }}>{daysRemaining} days</strong></div>
            </div>

            <div style={{
              backgroundColor: '#fffbeb',
              color: '#d97706',
              padding: '12px 24px',
              borderRadius: '30px',
              fontWeight: 700,
              fontSize: '16px',
              border: '1.5px solid #fde68a',
              marginBottom: '32px'
            }}>
              {daysRemaining} {t.daysLeft}
            </div>
          </>
        )}

        <div style={{
          display: 'grid',
          gridTemplateColumns: '1fr 1fr 1fr',
          gap: '16px',
          width: '100%',
          borderTop: '1px solid var(--border)',
          paddingTop: '32px',
          marginTop: '12px'
        }}>
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <Heart size={24} style={{ color: '#ef4444', marginBottom: '6px' }} />
            <span style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: 500 }}>Donations</span>
            <span style={{ fontSize: '20px', fontWeight: 700, color: 'var(--text-title)' }}>{profile?.donation_count || 0}</span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <Award size={24} style={{ color: '#eab308', marginBottom: '6px' }} />
            <span style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: 500 }}>{t.trustScore}</span>
            <span style={{ fontSize: '20px', fontWeight: 700, color: 'var(--text-title)' }}>{profile?.trust_score || 0}%</span>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <ShieldCheck size={24} style={{ color: '#3b82f6', marginBottom: '6px' }} />
            <span style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: 500 }}>{t.healthScore}</span>
            <span style={{ fontSize: '20px', fontWeight: 700, color: 'var(--text-title)' }}>{profile?.health_score || 0}%</span>
          </div>
        </div>
      </div>

      <style>{`
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
}
