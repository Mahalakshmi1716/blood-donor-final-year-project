import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useLanguage } from '../../context/LanguageContext';
import patientApi from '../../api/patientApi';
import { ArrowLeft, Search, MessageSquare, Shield, Award, MapPin, Phone, Building } from 'lucide-react';

export default function AiMatching() {
  const { user } = useAuth();
  const { t } = useLanguage();
  const navigate = useNavigate();

  const [bloodGroup, setBloodGroup] = useState(user?.blood_group || 'O+');
  const [lat, setLat] = useState(user?.latitude ? user.latitude.toString() : '');
  const [lng, setLng] = useState(user?.longitude ? user.longitude.toString() : '');
  const [urgency, setUrgency] = useState('High');

  const [donors, setDonors] = useState([]);
  const [fallbackActive, setFallbackActive] = useState(false);
  const [bloodBanks, setBloodBanks] = useState([]);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!lat || !lng) {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            setLat(position.coords.latitude.toString());
            setLng(position.coords.longitude.toString());
          },
          (err) => {
            setError('Location permission is required to search matching donors around you. Please enable location access in your browser settings.');
          }
        );
      }
    }
  }, [lat, lng]);

  const handleSearch = async (e) => {
    if (e) e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const data = await patientApi.searchDonors({
        blood_group: bloodGroup,
        latitude: parseFloat(lat),
        longitude: parseFloat(lng),
        urgency: urgency
      });

      setDonors(data.donors || []);
      setFallbackActive(data.fallback_activated || false);
      setBloodBanks(data.fallback_blood_banks || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Search failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  // Run initial search on mount if we have user details
  useEffect(() => {
    if (user) {
      handleSearch();
    }
  }, [user]);

  return (
    <div style={{ maxWidth: '900px', margin: '0 auto', textAlign: 'left' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '24px' }}>
        <button type="button" className="btn btn-outline" style={{ padding: '8px' }} onClick={() => navigate('/dashboard')}>
          <ArrowLeft size={20} />
        </button>
        <h1 style={{ fontSize: '24px', margin: 0, fontWeight: 700 }}>{t.matching}</h1>
      </div>

      {error && (
        <div style={{
          backgroundColor: '#fee2e2',
          color: '#ef4444',
          padding: '12px 16px',
          borderRadius: '8px',
          marginBottom: '20px',
          fontSize: '14px'
        }}>
          {error}
        </div>
      )}

      {/* Search Filter Box */}
      <div style={{
        backgroundColor: 'white',
        padding: '24px',
        borderRadius: 'var(--radius)',
        border: '1px solid var(--border)',
        boxShadow: 'var(--shadow-sm)',
        marginBottom: '32px'
      }}>
        <form onSubmit={handleSearch} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr auto', gap: '16px', alignItems: 'end' }}>
          <div className="form-group" style={{ margin: 0 }}>
            <label className="form-label">{t.bloodGroup}</label>
            <select className="form-select" value={bloodGroup} onChange={(e) => setBloodGroup(e.target.value)}>
              <option value="A+">A+</option>
              <option value="A-">A-</option>
              <option value="B+">B+</option>
              <option value="B-">B-</option>
              <option value="AB+">AB+</option>
              <option value="AB-">AB-</option>
              <option value="O+">O+</option>
              <option value="O-">O-</option>
            </select>
          </div>

          <div className="form-group" style={{ margin: 0 }}>
            <label className="form-label">{t.urgencyLevel}</label>
            <select className="form-select" value={urgency} onChange={(e) => setUrgency(e.target.value)}>
              <option value="Critical">Critical</option>
              <option value="High">High</option>
              <option value="Moderate">Moderate</option>
              <option value="Normal">Normal</option>
            </select>
          </div>

          <div className="form-group" style={{ margin: 0 }}>
            <label className="form-label">Search Coordinates (Lat / Lng)</label>
            <div style={{ display: 'flex', gap: '8px' }}>
              <input
                type="number"
                step="any"
                className="form-input"
                value={lat}
                onChange={(e) => setLat(e.target.value)}
                placeholder="Lat"
                style={{ padding: '8px' }}
              />
              <input
                type="number"
                step="any"
                className="form-input"
                value={lng}
                onChange={(e) => setLng(e.target.value)}
                placeholder="Lng"
                style={{ padding: '8px' }}
              />
            </div>
          </div>

          <button type="submit" className="btn btn-primary" style={{ height: '46px' }} disabled={loading}>
            <Search size={18} />
            <span>Search</span>
          </button>
        </form>
      </div>

      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center', fontSize: '16px' }}>Searching AI-Matched Donors...</div>
      ) : (
        <>
          {/* Fallback Blood Banks section if active */}
          {fallbackActive ? (
            <div style={{ marginBottom: '32px' }}>
              <div style={{
                backgroundColor: '#eff6ff',
                border: '1.5px solid #bfdbfe',
                color: '#1e40af',
                padding: '16px',
                borderRadius: '8px',
                marginBottom: '24px',
                fontWeight: 500
              }}>
                ℹ️ No compatible donors are available online. Emergency supply fallback has been activated to query fallback Blood Banks.
              </div>

              <h2 style={{ fontSize: '20px', fontWeight: 600, color: 'var(--text-title)', marginBottom: '16px' }}>Nearby Blood Banks</h2>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                {bloodBanks.map((bank) => (
                  <div key={bank.id} style={{
                    backgroundColor: 'white',
                    padding: '20px',
                    borderRadius: 'var(--radius)',
                    border: '1px solid var(--border)',
                    boxShadow: 'var(--shadow-sm)'
                  }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '12px' }}>
                      <Building size={20} style={{ color: 'var(--primary)' }} />
                      <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 600, color: 'var(--text-title)' }}>{bank.blood_bank_name}</h3>
                    </div>
                    <p style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', color: 'var(--text-muted)', marginBottom: '8px' }}>
                      <MapPin size={14} />
                      <span>{bank.location}</span>
                    </p>
                    <p style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', color: 'var(--text-muted)', marginBottom: '12px' }}>
                      <Phone size={14} />
                      <span>{bank.contact_number}</span>
                    </p>
                    <span style={{
                      display: 'inline-block',
                      padding: '4px 10px',
                      borderRadius: '12px',
                      fontSize: '12px',
                      fontWeight: 600,
                      backgroundColor: '#f0fdf4',
                      color: '#16a34a'
                    }}>{bank.availability_status}</span>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div>
              <h2 style={{ fontSize: '20px', fontWeight: 600, color: 'var(--text-title)', marginBottom: '16px' }}>AI-Scored Matching Donors</h2>
              {donors.length === 0 ? (
                <div style={{ padding: '40px', backgroundColor: 'white', borderRadius: 'var(--radius)', border: '1px solid var(--border)', textAlign: 'center', color: 'var(--text-muted)' }}>
                  No matching donors found. Try changing your search filters.
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                  {donors.map((donor) => (
                    <div key={donor.donor_id} style={{
                      backgroundColor: 'white',
                      padding: '24px',
                      borderRadius: 'var(--radius)',
                      border: '1px solid var(--border)',
                      boxShadow: 'var(--shadow-sm)',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center'
                    }}>
                      <div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
                          <span style={{ fontSize: '18px', fontWeight: 700, color: 'var(--text-title)' }}>{donor.name}</span>
                          <span style={{
                            padding: '2px 8px',
                            borderRadius: '4px',
                            backgroundColor: 'var(--primary-light)',
                            color: 'var(--primary)',
                            fontSize: '12px',
                            fontWeight: 700
                          }}>{donor.blood_group}</span>
                          
                          <span style={{
                            padding: '2px 8px',
                            borderRadius: '4px',
                            backgroundColor: '#f0fdf4',
                            color: '#16a34a',
                            fontSize: '12px',
                            fontWeight: 700
                          }}>{donor.match_score}% Match Score</span>
                        </div>

                        <div style={{ display: 'flex', gap: '16px', fontSize: '13px', color: 'var(--text-muted)' }}>
                          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                            <MapPin size={13} /> Approx {donor.distance_km?.toFixed(1)} km away
                          </span>
                          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                            <Award size={13} /> {donor.trust_score}% Trust Score
                          </span>
                          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                            <Shield size={13} /> {donor.health_score}% Health Score
                          </span>
                        </div>
                      </div>

                      <button
                        type="button"
                        className="btn btn-primary"
                        onClick={() => navigate(`/chat/${donor.donor_id}/${encodeURIComponent(donor.name)}`)}
                      >
                        <MessageSquare size={16} />
                        <span>Chat</span>
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </>
      )}
    </div>
  );
}
