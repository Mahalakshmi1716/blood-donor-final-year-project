import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useLanguage } from '../../context/LanguageContext';
import donorApi from '../../api/donorApi';
import { ArrowLeft, CheckCircle, AlertCircle, Compass } from 'lucide-react';

export default function Availability() {
  const { user } = useAuth();
  const { t } = useLanguage();
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [isAvailable, setIsAvailable] = useState(false);
  const [lat, setLat] = useState('');
  const [lng, setLng] = useState('');
  const [state, setStateName] = useState('');
  const [district, setDistrict] = useState('');
  const [city, setCity] = useState('');
  const [locStatus, setLocStatus] = useState('');
  const [loadingLocation, setLoadingLocation] = useState(false);

  useEffect(() => {
    donorApi.getProfile()
      .then((data) => {
        setProfile(data.profile);
        setIsAvailable(data.profile.today_availability);
        setLat(data.profile.latitude || '');
        setLng(data.profile.longitude || '');
        setStateName(data.profile.state || '');
        setDistrict(data.profile.district || '');
        setCity(data.profile.city || '');
        if (data.profile.today_availability && data.profile.latitude) {
          setLocStatus('Location coordinates captured automatically.');
        }
      })
      .catch((err) => {
        setError(err.response?.data?.message || 'Failed to fetch donor profile.');
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  const handleAvailableToggle = (checked) => {
    setError('');
    setLocStatus('');
    if (checked) {
      if (!navigator.geolocation) {
        setError('Geolocation is not supported by your browser.');
        setIsAvailable(false);
        return;
      }
      setLoadingLocation(true);
      setLocStatus('Detecting your current location...');
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setLat(position.coords.latitude.toString());
          setLng(position.coords.longitude.toString());
          setIsAvailable(true);
          setLocStatus('Location coordinates captured automatically.');
          setLoadingLocation(false);
        },
        (err) => {
          setError('Location permission is required to show you as available. Please enable location access in your browser settings.');
          setIsAvailable(false);
          setLocStatus('Location permission required.');
          setLoadingLocation(false);
        },
        { enableHighAccuracy: true, timeout: 10000 }
      );
    } else {
      setIsAvailable(false);
      setLat('');
      setLng('');
      setLocStatus('');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (isAvailable && (!lat || !lng)) {
      setError('Coordinates must be captured before updating availability.');
      return;
    }

    setUpdating(true);

    try {
      const payload = {
        is_available_today: isAvailable,
        today_availability: isAvailable,
        latitude: lat ? parseFloat(lat) : null,
        longitude: lng ? parseFloat(lng) : null,
        state: state || null,
        district: district || null,
        city: city || null
      };

      const res = await donorApi.updateAvailability(payload);
      setProfile(res.profile);
      setSuccess('Availability and location updated successfully.');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update availability.');
    } finally {
      setUpdating(false);
    }
  };

  if (loading) {
    return <div style={{ padding: '40px', fontSize: '18px' }}>Loading donor profile...</div>;
  }

  return (
    <div style={{ maxWidth: '640px', margin: '0 auto', textAlign: 'left' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px' }}>
        <button type="button" className="btn btn-outline" style={{ padding: '8px' }} onClick={() => navigate('/dashboard')}>
          <ArrowLeft size={20} />
        </button>
        <h1 style={{ fontSize: '24px', margin: 0, fontWeight: 700 }}>{t.updateAvailability}</h1>
      </div>

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
          fontSize: '14px'
        }}>
          <AlertCircle size={18} style={{ flexShrink: 0 }} />
          <span>{error}</span>
        </div>
      )}

      {success && (
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          backgroundColor: '#f0fdf4',
          color: '#16a34a',
          padding: '12px 16px',
          borderRadius: '8px',
          marginBottom: '20px',
          fontSize: '14px',
          fontWeight: 500
        }}>
          <CheckCircle size={18} style={{ flexShrink: 0 }} />
          <span>{success}</span>
        </div>
      )}

      <div style={{ backgroundColor: 'white', padding: '32px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)' }}>
        {profile?.eligibility_status !== 'ELIGIBLE' && (
          <div style={{
            backgroundColor: '#fffbeb',
            color: '#d97706',
            border: '1.5px solid #fde68a',
            padding: '16px',
            borderRadius: '8px',
            marginBottom: '24px',
            fontSize: '15px',
            lineHeight: '140%',
            fontWeight: 500
          }}>
            ⚠️ Cooldown Active: You cannot make yourself available for donation. Cooldown resets 90 days after your last donation ({profile?.last_donation_date || 'N/A'}).
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginBottom: '28px', backgroundColor: '#f8fafc', padding: '16px', borderRadius: '8px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <input
                type="checkbox"
                id="is_available"
                checked={isAvailable}
                onChange={(e) => handleAvailableToggle(e.target.checked)}
                disabled={profile?.eligibility_status !== 'ELIGIBLE' || loadingLocation}
                style={{ width: '22px', height: '22px', cursor: profile?.eligibility_status === 'ELIGIBLE' ? 'pointer' : 'not-allowed' }}
              />
              <label htmlFor="is_available" style={{ fontSize: '16px', fontWeight: 600, color: 'var(--text-title)', cursor: profile?.eligibility_status === 'ELIGIBLE' ? 'pointer' : 'not-allowed' }}>
                {t.availableToday}
              </label>
            </div>
            {locStatus && (
              <span style={{ fontSize: '13px', color: locStatus.includes('captured') ? '#16a34a' : 'var(--text-muted)', fontWeight: 500, marginLeft: '34px' }}>
                📍 {locStatus}
              </span>
            )}
          </div>

          <div className="form-group">
            <label className="form-label">City / Town</label>
            <input
              type="text"
              className="form-input"
              value={city}
              onChange={(e) => setCity(e.target.value)}
              placeholder="e.g. New Delhi"
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div className="form-group">
              <label className="form-label">District</label>
              <input
                type="text"
                className="form-input"
                value={district}
                onChange={(e) => setDistrict(e.target.value)}
                placeholder="e.g. Central Delhi"
              />
            </div>
            <div className="form-group">
              <label className="form-label">State</label>
              <input
                type="text"
                className="form-input"
                value={state}
                onChange={(e) => setStateName(e.target.value)}
                placeholder="e.g. Delhi"
              />
            </div>
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '12px' }} disabled={updating || loadingLocation}>
            {updating ? 'Saving Status...' : 'Save Availability Settings'}
          </button>
        </form>
      </div>
    </div>
  );
}
