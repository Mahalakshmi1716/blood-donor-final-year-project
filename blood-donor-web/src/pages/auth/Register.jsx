import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useLanguage } from '../../context/LanguageContext';
import authApi from '../../api/authApi';
import { HeartHandshake, AlertCircle, ChevronRight, ChevronLeft } from 'lucide-react';

export default function Register() {
  const { t } = useLanguage();
  const navigate = useNavigate();
  
  // Registration steps
  const [activeStep, setActiveStep] = useState(0); // Step 0: Select Role, Step 1: Basic Info, Step 2: Specific Details
  
  // Basic Information (Step 1)
  const [role, setRole] = useState('Donor');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  
  // Donor/Patient specific (Step 2)
  const [bloodGroup, setBloodGroup] = useState('O+');
  const [age, setAge] = useState('');
  const [gender, setGender] = useState('Male');
  const [lastDonationDate, setLastDonationDate] = useState('');

  // Hospital specific (Step 2)
  const [address, setAddress] = useState('');
  const [license, setLicense] = useState('');
  const [lat, setLat] = useState('');
  const [lng, setLng] = useState('');
  const [gpsStatus, setGpsStatus] = useState('');

  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Auto-detect coordinates when Hospital detail step is shown
  useEffect(() => {
    if (role === 'Hospital' && activeStep === 2) {
      setGpsStatus('Detecting hospital coordinates...');
      if (!navigator.geolocation) {
        setGpsStatus('Geolocation not supported by browser.');
        return;
      }
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setLat(position.coords.latitude.toString());
          setLng(position.coords.longitude.toString());
          setGpsStatus('📍 Location detected successfully.');
        },
        (err) => {
          setGpsStatus('❌ Location permission required for Hospital coordinates.');
        },
        { enableHighAccuracy: true, timeout: 10000 }
      );
    }
  }, [role, activeStep]);

  const handleNextStep1 = (e) => {
    e.preventDefault();
    setError('');

    if (!name || !email || !phone || !password || !confirmPassword) {
      setError('Please fill in all basic fields.');
      return;
    }

    if (!email.includes('@')) {
      setError('Please enter a valid email address.');
      return;
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters.');
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setActiveStep(2);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    const payload = {
      name,
      phone_number: phone,
      password,
      email: email.trim().toLowerCase(),
      user_type: role,
    };

    if (role === 'Donor') {
      payload.blood_group = bloodGroup;
      payload.age = age ? parseInt(age, 10) : null;
      payload.gender = gender;
      payload.last_donation_date = lastDonationDate || null;
    } else if (role === 'Patient') {
      payload.blood_group = bloodGroup;
      payload.age = age ? parseInt(age, 10) : null;
      payload.gender = gender;
    } else if (role === 'Hospital') {
      payload.registered_address = address;
      payload.hospital_license = license;
      if (!lat || !lng) {
        setError('Location coordinates are required to register a hospital.');
        setLoading(false);
        return;
      }
      payload.latitude = parseFloat(lat);
      payload.longitude = parseFloat(lng);
    }

    try {
      await authApi.register(payload);
      navigate(`/verify-otp/${encodeURIComponent(email)}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card" style={{ maxWidth: '480px', padding: '32px' }}>
        <div className="auth-logo" style={{ marginBottom: '24px' }}>
          <HeartHandshake />
          <h1>{t.appTitle}</h1>
        </div>

        {/* Step indicators */}
        <div style={{ display: 'flex', gap: '8px', marginBottom: '24px', justifyContent: 'center' }}>
          {[0, 1, 2].map((stepIdx) => (
            <div
              key={stepIdx}
              style={{
                width: activeStep === stepIdx ? '28px' : '10px',
                height: '10px',
                borderRadius: '5px',
                backgroundColor: activeStep === stepIdx ? 'var(--primary)' : '#cbd5e1',
                transition: 'all 0.3s ease'
              }}
            />
          ))}
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
            fontSize: '14px',
            textAlign: 'left'
          }}>
            <AlertCircle size={18} style={{ flexShrink: 0 }} />
            <span>{error}</span>
          </div>
        )}

        {/* STEP 0: Select Role */}
        {activeStep === 0 && (
          <div>
            <h3 style={{ fontSize: '18px', fontWeight: 600, color: 'var(--text-title)', marginBottom: '20px', textAlign: 'center' }}>
              Select Account Type
            </h3>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <button
                type="button"
                className="btn btn-outline"
                style={{
                  padding: '18px',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  height: 'auto',
                  gap: '6px',
                  borderColor: role === 'Donor' ? 'var(--primary)' : 'var(--border)',
                  backgroundColor: role === 'Donor' ? 'var(--primary-light)' : 'transparent',
                }}
                onClick={() => setRole('Donor')}
              >
                <span style={{ fontSize: '16px', fontWeight: 700, color: 'var(--primary)' }}>🩸 Blood Donor</span>
                <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Donate blood, configure availability, and receive alerts.</span>
              </button>

              <button
                type="button"
                className="btn btn-outline"
                style={{
                  padding: '18px',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  height: 'auto',
                  gap: '6px',
                  borderColor: role === 'Patient' ? 'var(--primary)' : 'var(--border)',
                  backgroundColor: role === 'Patient' ? 'var(--primary-light)' : 'transparent',
                }}
                onClick={() => setRole('Patient')}
              >
                <span style={{ fontSize: '16px', fontWeight: 700, color: 'var(--primary)' }}>👤 Patient / Family</span>
                <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Search compatible donors and trigger emergency alerts.</span>
              </button>

              <button
                type="button"
                className="btn btn-outline"
                style={{
                  padding: '18px',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  height: 'auto',
                  gap: '6px',
                  borderColor: role === 'Hospital' ? 'var(--primary)' : 'var(--border)',
                  backgroundColor: role === 'Hospital' ? 'var(--primary-light)' : 'transparent',
                }}
                onClick={() => setRole('Hospital')}
              >
                <span style={{ fontSize: '16px', fontWeight: 700, color: 'var(--primary)' }}>🏦 Hospital</span>
                <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Verified medical institution creating SOS blood requests.</span>
              </button>
            </div>

            <div style={{ display: 'flex', gap: '16px', marginTop: '24px' }}>
              <button
                type="button"
                className="btn btn-secondary"
                style={{ flex: 1 }}
                onClick={() => navigate('/welcome')}
              >
                Back
              </button>
              <button
                type="button"
                className="btn btn-primary"
                style={{ flex: 1 }}
                onClick={() => setActiveStep(1)}
              >
                <span>Next</span>
                <ChevronRight size={18} />
              </button>
            </div>
          </div>
        )}

        {/* STEP 1: Basic Information */}
        {activeStep === 1 && (
          <form onSubmit={handleNextStep1}>
            <h3 style={{ fontSize: '18px', fontWeight: 600, color: 'var(--text-title)', marginBottom: '20px', textAlign: 'center' }}>
              Basic Information ({role})
            </h3>

            <div className="form-group">
              <label className="form-label">{t.name}</label>
              <input
                type="text"
                className="form-input"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                placeholder="e.g. John Doe"
              />
            </div>

            <div className="form-group">
              <label className="form-label">{t.emailAddress}</label>
              <input
                type="email"
                className="form-input"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                placeholder="e.g. john@example.com"
              />
            </div>

            <div className="form-group">
              <label className="form-label">{t.phoneNumber}</label>
              <input
                type="tel"
                className="form-input"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                required
                placeholder="e.g. 9876543210"
              />
            </div>

            <div className="form-group">
              <label className="form-label">{t.password}</label>
              <input
                type="password"
                className="form-input"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                placeholder="Min 6 characters"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Confirm Password</label>
              <input
                type="password"
                className="form-input"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
                placeholder="Repeat password"
              />
            </div>

            <div style={{ display: 'flex', gap: '16px', marginTop: '24px' }}>
              <button
                type="button"
                className="btn btn-secondary"
                style={{ flex: 1 }}
                onClick={() => setActiveStep(0)}
              >
                <ChevronLeft size={18} />
                <span>Back</span>
              </button>
              <button
                type="submit"
                className="btn btn-primary"
                style={{ flex: 1 }}
              >
                <span>Next</span>
                <ChevronRight size={18} />
              </button>
            </div>
          </form>
        )}

        {/* STEP 2: Specific Details */}
        {activeStep === 2 && (
          <form onSubmit={handleSubmit}>
            <h3 style={{ fontSize: '18px', fontWeight: 600, color: 'var(--text-title)', marginBottom: '20px', textAlign: 'center' }}>
              Configure Account Details
            </h3>

            {/* Donor Form Details */}
            {role === 'Donor' && (
              <>
                <div className="form-group">
                  <label className="form-label">{t.bloodGroup}</label>
                  <select
                    className="form-select"
                    value={bloodGroup}
                    onChange={(e) => setBloodGroup(e.target.value)}
                  >
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

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                  <div className="form-group">
                    <label className="form-label">{t.age}</label>
                    <input
                      type="number"
                      className="form-input"
                      value={age}
                      onChange={(e) => setAge(e.target.value)}
                      required
                      min="18"
                      max="65"
                      placeholder="18"
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">{t.gender}</label>
                    <select
                      className="form-select"
                      value={gender}
                      onChange={(e) => setGender(e.target.value)}
                    >
                      <option value="Male">Male</option>
                      <option value="Female">Female</option>
                      <option value="Other">Other</option>
                    </select>
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label">Last Donation Date (Optional)</label>
                  <input
                    type="date"
                    className="form-input"
                    value={lastDonationDate}
                    max={new Date().toISOString().split('T')[0]}
                    onChange={(e) => setLastDonationDate(e.target.value)}
                  />
                </div>
              </>
            )}

            {/* Patient Form Details */}
            {role === 'Patient' && (
              <>
                <div className="form-group">
                  <label className="form-label">{t.bloodGroup}</label>
                  <select
                    className="form-select"
                    value={bloodGroup}
                    onChange={(e) => setBloodGroup(e.target.value)}
                  >
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

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                  <div className="form-group">
                    <label className="form-label">{t.age}</label>
                    <input
                      type="number"
                      className="form-input"
                      value={age}
                      onChange={(e) => setAge(e.target.value)}
                      required
                      min="1"
                      max="100"
                      placeholder="25"
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">{t.gender}</label>
                    <select
                      className="form-select"
                      value={gender}
                      onChange={(e) => setGender(e.target.value)}
                    >
                      <option value="Male">Male</option>
                      <option value="Female">Female</option>
                      <option value="Other">Other</option>
                    </select>
                  </div>
                </div>
              </>
            )}

            {/* Hospital Form Details */}
            {role === 'Hospital' && (
              <>
                <div className="form-group">
                  <label className="form-label">{t.hospitalLicense}</label>
                  <input
                    type="text"
                    className="form-input"
                    value={license}
                    onChange={(e) => setLicense(e.target.value)}
                    required
                    placeholder="e.g. LIC-998877"
                  />
                </div>
                
                <div className="form-group">
                  <label className="form-label">{t.registeredAddress}</label>
                  <input
                    type="text"
                    className="form-input"
                    value={address}
                    onChange={(e) => setAddress(e.target.value)}
                    required
                    placeholder="e.g. 123 Main St, New Delhi"
                  />
                </div>

                {gpsStatus && (
                  <div style={{
                    marginBottom: '20px',
                    fontSize: '14px',
                    color: gpsStatus.includes('successfully') ? '#16a34a' : 'var(--text-muted)',
                    fontWeight: 500,
                    textAlign: 'left',
                    backgroundColor: '#f8fafc',
                    padding: '12px',
                    borderRadius: '6px',
                    border: '1px solid var(--border)'
                  }}>
                    {gpsStatus}
                  </div>
                )}
              </>
            )}

            <div style={{ display: 'flex', gap: '16px', marginTop: '24px' }}>
              <button
                type="button"
                className="btn btn-secondary"
                style={{ flex: 1 }}
                onClick={() => setActiveStep(1)}
              >
                <ChevronLeft size={18} />
                <span>Back</span>
              </button>
              <button
                type="submit"
                className="btn btn-primary"
                style={{ flex: 1 }}
                disabled={loading || (role === 'Hospital' && (!lat || !lng))}
              >
                {loading ? 'Submitting...' : 'Register'}
              </button>
            </div>
          </form>
        )}

        <p style={{ marginTop: '24px', fontSize: '14px', color: 'var(--text-muted)' }}>
          {t.alreadyHaveAccount}{' '}
          <Link to="/login" style={{ color: 'var(--primary)', fontWeight: 600, textDecoration: 'none' }}>
            {t.login}
          </Link>
        </p>
      </div>
    </div>
  );
}
