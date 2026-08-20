import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useLanguage } from '../../context/LanguageContext';
import authApi from '../../api/authApi';
import donorApi from '../../api/donorApi';
import patientApi from '../../api/patientApi';
import chatApi from '../../api/chatApi';

// Leaflet map imports
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix for default Leaflet marker assets in bundler
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconUrl: markerIcon,
  iconRetinaUrl: markerIcon2x,
  shadowUrl: markerShadow,
});

// Custom leafet marker icons
const userIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

const donorIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

const bankIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});

// Icons
import {
  HeartHandshake,
  Home,
  Map,
  MessageSquare,
  Bell,
  User,
  Settings,
  LogOut,
  PlusCircle,
  Compass,
  AlertTriangle,
  Award,
  Clock,
  Heart,
  ExternalLink,
  ChevronRight,
  Navigation,
  CheckCircle,
  AlertCircle
} from 'lucide-react';

export default function Dashboard() {
  const { user, logout, updateUserState } = useAuth();
  const { lang, t } = useLanguage();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const currentTab = searchParams.get('tab') || 'home';

  // State configurations
  const [donorProfile, setDonorProfile] = useState(null);
  const [tipOfTheDay, setTipOfTheDay] = useState('');
  const [alerts, setAlerts] = useState([]);
  const [conversations, setConversations] = useState([]);
  const [patientAnalytics, setPatientAnalytics] = useState(null);
  const [hospitalAnalytics, setHospitalAnalytics] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Map state
  const [mapCenter, setMapCenter] = useState(null); // Defaults to browser GPS coordinates
  const [mapDonors, setMapDonors] = useState([]);
  const [mapBanks, setMapBanks] = useState([]);

  // Modals state
  const [showSosModal, setShowSosModal] = useState(false);
  const [showDonationModal, setShowDonationModal] = useState(false);
  const [certInfo, setCertInfo] = useState(null);

  // SOS Form states
  const [sosBlood, setSosBlood] = useState('O+');
  const [sosHospital, setSosHospital] = useState('');
  const [sosLat, setSosLat] = useState('');
  const [sosLng, setSosLng] = useState('');
  const [sosUrgency, setSosUrgency] = useState('High');
  const [sosUnits, setSosUnits] = useState(1);

  // Donation Form state
  const [lastDonationDate, setLastDonationDate] = useState('');

  // SOS auto-location status states
  const [sosLocStatus, setSosLocStatus] = useState('');
  const [loadingSosLocation, setLoadingSosLocation] = useState(false);

  // Profile Edit fields
  const [editAge, setEditAge] = useState('');
  const [editGender, setEditGender] = useState('Male');
  const [editBloodGroup, setEditBloodGroup] = useState('O+');

  const setTab = (tabName) => {
    setSearchParams({ tab: tabName });
    setError('');
    setSuccess('');
  };

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }

    // Set initial form states
    setEditAge(user.age || '');
    setEditGender(user.gender || 'Male');
    setEditBloodGroup(user.blood_group || 'O+');
    if (user.latitude && user.longitude) {
      setMapCenter([user.latitude, user.longitude]);
      setSosLat(user.latitude.toString());
      setSosLng(user.longitude.toString());
    }

    // Fetch initial parameters based on roles
    if (user.user_type === 'Donor') {
      donorApi.getProfile()
        .then((res) => {
          setDonorProfile(res.profile);
        })
        .catch(() => {});
      
      donorApi.getTipOfTheDay()
        .then((res) => setTipOfTheDay(res.tip))
        .catch(() => {});
    } else if (user.user_type === 'Patient') {
      patientApi.getPatientAnalytics()
        .then((res) => setPatientAnalytics(res))
        .catch(() => {});
    } else if (user.user_type === 'Hospital') {
      patientApi.getHospitalAnalytics()
        .then((res) => setHospitalAnalytics(res))
        .catch(() => {});
    }
  }, [user, navigate]);

  // Handle Dynamic Tab Loads
  useEffect(() => {
    if (!user) return;

    if (currentTab === 'alerts') {
      patientApi.getAlerts()
        .then((res) => setAlerts(res.alerts || []))
        .catch(() => {});
    } else if (currentTab === 'chat') {
      chatApi.getConversations()
        .then((res) => setConversations(res.conversations || []))
        .catch(() => {});
    } else if (currentTab === 'map') {
      setError('');
      if (!navigator.geolocation) {
        setError('Geolocation is not supported by your browser.');
        return;
      }
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const uLat = position.coords.latitude;
          const uLng = position.coords.longitude;
          setMapCenter([uLat, uLng]);

          patientApi.searchDonors({
            blood_group: user.blood_group || 'O+',
            latitude: uLat,
            longitude: uLng,
            urgency: 'Normal'
          })
            .then((res) => {
              setMapDonors(res.donors || []);
              setMapBanks(res.fallback_blood_banks || []);
            })
            .catch((err) => {
              setError(err.response?.data?.message || 'Failed to search donors.');
            });
        },
        (err) => {
          setError('Location permission is required to center the map on your current location. Please enable location access in your browser settings.');
        },
        { enableHighAccuracy: true, timeout: 10000 }
      );
    }
  }, [currentTab, user]);

  // SOS auto-location capturing when SOS Modal opens
  useEffect(() => {
    if (showSosModal) {
      setSosLocStatus('Detecting your current location...');
      setLoadingSosLocation(true);
      if (!navigator.geolocation) {
        setSosLocStatus('Geolocation is not supported by your browser.');
        setLoadingSosLocation(false);
        return;
      }
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setSosLat(position.coords.latitude.toString());
          setSosLng(position.coords.longitude.toString());
          setSosLocStatus('Location coordinates captured automatically.');
          setLoadingSosLocation(false);
        },
        (err) => {
          setSosLocStatus('Location permission is required to send an SOS request. Please enable location access in your browser settings.');
          setSosLat('');
          setSosLng('');
          setLoadingSosLocation(false);
        },
        { enableHighAccuracy: true, timeout: 10000 }
      );
    } else {
      setSosLocStatus('');
      setSosLat('');
      setSosLng('');
    }
  }, [showSosModal]);

  // Manual Donation Logger
  const handleRecordDonation = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!lastDonationDate) {
      setError('Please select the date of your last donation.');
      return;
    }
    
    try {
      const data = await donorApi.recordDonation({ last_donation_date: lastDonationDate });
      setSuccess(data.message);
      setShowDonationModal(false);
      setLastDonationDate('');
      
      // Update donor profile stats
      const pRes = await donorApi.getProfile();
      setDonorProfile(pRes.profile);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to record donation.');
    }
  };

  // SOS Trigger
  const handleTriggerSos = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    try {
      const data = await patientApi.triggerSos({
        blood_group: sosBlood,
        hospital_name: sosHospital,
        latitude: parseFloat(sosLat),
        longitude: parseFloat(sosLng),
        urgency: sosUrgency,
        units_required: parseInt(sosUnits, 10)
      });

      setSuccess(data.message);
      setShowSosModal(false);
      
      // Refresh analytics
      if (user.user_type === 'Patient') {
        const res = await patientApi.getPatientAnalytics();
        setPatientAnalytics(res);
      } else if (user.user_type === 'Hospital') {
        const res = await patientApi.getHospitalAnalytics();
        setHospitalAnalytics(res);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to send SOS request.');
    }
  };

  // Alert actions
  const handleAcceptAlert = async (alertId) => {
    try {
      await patientApi.acceptAlert(alertId);
      setSuccess('Alert accepted successfully. Please proceed to destination.');
      // Refresh alerts list
      const res = await patientApi.getAlerts();
      setAlerts(res.alerts || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to accept alert.');
    }
  };

  const handleDeclineAlert = async (alertId) => {
    try {
      await patientApi.declineAlert(alertId);
      setSuccess('Alert declined.');
      const res = await patientApi.getAlerts();
      setAlerts(res.alerts || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to decline alert.');
    }
  };

  const handleStartTravel = async (alertId) => {
    try {
      await patientApi.startTravel(alertId);
      const res = await patientApi.getAlerts();
      setAlerts(res.alerts || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update traveling status.');
    }
  };

  const handleStartDonation = async (alertId) => {
    try {
      await patientApi.startDonation(alertId);
      const res = await patientApi.getAlerts();
      setAlerts(res.alerts || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update status.');
    }
  };

  const handleConfirmDonation = async (alertId) => {
    try {
      const data = await patientApi.confirmDonation(alertId);
      if (data.certificate) {
        setCertInfo(data.certificate);
      }
      setSuccess('Donation completed successfully!');
      const res = await patientApi.getAlerts();
      setAlerts(res.alerts || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to confirm donation.');
    }
  };

  const handleCancelAlert = async (alertId) => {
    if (!window.confirm('Are you sure you want to cancel this emergency request?')) return;
    try {
      await patientApi.cancelAlert(alertId);
      setSuccess('Emergency request cancelled.');
      const res = await patientApi.getAlerts();
      setAlerts(res.alerts || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to cancel request.');
    }
  };

  // Edit profile submit
  const handleEditProfile = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    
    try {
      const data = await authApi.updateProfile({
        age: editAge ? parseInt(editAge, 10) : null,
        gender: editGender,
        blood_group: editBloodGroup
      });
      updateUserState(data.user);
      setSuccess('Profile updated successfully.');
    } catch (err) {
      setError(err.response?.data?.message || 'Profile update failed.');
    }
  };

  return (
    <div className="app-layout">
      {/* Sidebar for Desktop */}
      <aside className="app-sidebar">
        <div className="sidebar-header">
          <HeartHandshake />
          <span>Blood Finder</span>
        </div>

        <nav className="sidebar-menu">
          <button onClick={() => setTab('home')} className={`menu-item btn-outline ${currentTab === 'home' ? 'active' : ''}`} style={{ border: 'none', background: 'transparent', textAlign: 'left', width: '100%', justifyContent: 'flex-start' }}>
            <Home size={18} />
            <span>Home</span>
          </button>
          
          <button onClick={() => setTab('map')} className={`menu-item btn-outline ${currentTab === 'map' ? 'active' : ''}`} style={{ border: 'none', background: 'transparent', textAlign: 'left', width: '100%', justifyContent: 'flex-start' }}>
            <Map size={18} />
            <span>{t.map}</span>
          </button>

          <button onClick={() => setTab('chat')} className={`menu-item btn-outline ${currentTab === 'chat' ? 'active' : ''}`} style={{ border: 'none', background: 'transparent', textAlign: 'left', width: '100%', justifyContent: 'flex-start' }}>
            <MessageSquare size={18} />
            <span>Messages</span>
          </button>

          <button onClick={() => setTab('alerts')} className={`menu-item btn-outline ${currentTab === 'alerts' ? 'active' : ''}`} style={{ border: 'none', background: 'transparent', textAlign: 'left', width: '100%', justifyContent: 'flex-start' }}>
            <Bell size={18} />
            <span>{t.alerts}</span>
          </button>

          <button onClick={() => setTab('profile')} className={`menu-item btn-outline ${currentTab === 'profile' ? 'active' : ''}`} style={{ border: 'none', background: 'transparent', textAlign: 'left', width: '100%', justifyContent: 'flex-start' }}>
            <User size={18} />
            <span>{t.profile}</span>
          </button>
        </nav>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', borderTop: '1px solid var(--border)', paddingTop: '16px' }}>
          <button onClick={() => navigate('/settings')} className="menu-item" style={{ border: 'none', background: 'transparent', display: 'flex', width: '100%', justifyContent: 'flex-start', cursor: 'pointer' }}>
            <Settings size={18} />
            <span>{t.settings}</span>
          </button>
          <button onClick={logout} className="menu-item" style={{ border: 'none', background: 'transparent', color: '#ef4444', display: 'flex', width: '100%', justifyContent: 'flex-start', cursor: 'pointer' }}>
            <LogOut size={18} />
            <span>{t.logout}</span>
          </button>
        </div>
      </aside>

      {/* Main Content viewport */}
      <main className="app-content">
        <header className="app-header">
          <div className="header-title">
            {currentTab === 'home' && 'Dashboard'}
            {currentTab === 'map' && t.map}
            {currentTab === 'chat' && 'Chats & Conversations'}
            {currentTab === 'alerts' && t.activeEmergency}
            {currentTab === 'profile' && t.profile}
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <span className="user-badge" style={{ textTransform: 'uppercase' }}>
              {user?.user_type}
            </span>
            <div style={{ fontWeight: 600, color: 'var(--text-title)' }}>{user?.name}</div>
          </div>
        </header>

        <div className="main-viewport">
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
              fontWeight: 500,
              textAlign: 'left'
            }}>
              <CheckCircle size={18} style={{ flexShrink: 0 }} />
              <span>{success}</span>
            </div>
          )}

          {/* TAB 1: HOME PANEL */}
          {currentTab === 'home' && (
            <div style={{ textAlign: 'left' }}>
              {/* DONOR SPECIFIC DASHBOARD VIEW */}
              {user?.user_type === 'Donor' && (
                <div>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px', marginBottom: '32px' }}>
                    <div style={{ backgroundColor: 'white', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)' }}>
                      <div style={{ display: 'flex', justifySelf: 'space-between', alignItems: 'center', width: '100%' }}>
                        <span style={{ fontSize: '14px', color: 'var(--text-muted)', fontWeight: 500 }}>Donations Completed</span>
                        <Heart size={20} style={{ color: '#ef4444' }} />
                      </div>
                      <h2 style={{ fontSize: '32px', fontWeight: 700, color: 'var(--text-title)', margin: '12px 0 4px' }}>
                        {donorProfile?.donation_count || 0}
                      </h2>
                    </div>

                    <div style={{ backgroundColor: 'white', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)' }}>
                      <div style={{ display: 'flex', justifySelf: 'space-between', alignItems: 'center', width: '100%' }}>
                        <span style={{ fontSize: '14px', color: 'var(--text-muted)', fontWeight: 500 }}>{t.trustScore}</span>
                        <Award size={20} style={{ color: '#eab308' }} />
                      </div>
                      <h2 style={{ fontSize: '32px', fontWeight: 700, color: 'var(--text-title)', margin: '12px 0 4px' }}>
                        {donorProfile?.trust_score || 0}%
                      </h2>
                    </div>

                    <div style={{ backgroundColor: 'white', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)' }}>
                      <div style={{ display: 'flex', justifySelf: 'space-between', alignItems: 'center', width: '100%' }}>
                        <span style={{ fontSize: '14px', color: 'var(--text-muted)', fontWeight: 500 }}>Eligibility Status</span>
                        <Clock size={20} style={{ color: '#3b82f6' }} />
                      </div>
                      <span style={{
                        display: 'inline-block',
                        padding: '6px 12px',
                        borderRadius: '20px',
                        fontSize: '13px',
                        fontWeight: 700,
                        marginTop: '12px',
                        backgroundColor: donorProfile?.eligibility_status === 'ELIGIBLE' ? '#f0fdf4' : '#fffbeb',
                        color: donorProfile?.eligibility_status === 'ELIGIBLE' ? '#16a34a' : '#d97706'
                      }}>
                        {donorProfile?.eligibility_status === 'ELIGIBLE' ? t.eligible : t.ineligible}
                      </span>
                    </div>
                  </div>

                  {/* Actions / Info */}
                  <div style={{ display: 'grid', gridTemplateColumns: '1.5fr 1fr', gap: '32px' }}>
                    <div>
                      {/* Health Tip Box */}
                      <div style={{ backgroundColor: 'white', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)', marginBottom: '24px' }}>
                        <h3 style={{ fontSize: '18px', fontWeight: 600, color: 'var(--text-title)', margin: '0 0 12px' }}>💡 {t.tipOfTheDay}</h3>
                        <p style={{ color: 'var(--text-main)', fontSize: '15px', lineHeight: '150%' }}>
                          {tipOfTheDay || 'Eat iron-rich foods such as spinach, red meat, and beans before your donation cycle.'}
                        </p>
                      </div>

                      {/* Donation history list */}
                      <div style={{ backgroundColor: 'white', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)' }}>
                        <h3 style={{ fontSize: '18px', fontWeight: 600, color: 'var(--text-title)', margin: '0 0 16px' }}>📋 {t.donationHistory}</h3>
                        {donorProfile?.donations && donorProfile.donations.length > 0 ? (
                          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                            {donorProfile.donations.map((rec) => (
                              <div key={rec.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '12px 16px', border: '1px solid var(--border)', borderRadius: '8px' }}>
                                <div>
                                  <div style={{ fontWeight: 600, color: 'var(--text-title)' }}>{rec.hospital_name}</div>
                                  <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{rec.location}</div>
                                </div>
                                <div style={{ fontSize: '14px', fontWeight: 500, color: 'var(--text-muted)' }}>{rec.donation_date}</div>
                              </div>
                            ))}
                          </div>
                        ) : (
                          <p style={{ color: 'var(--text-muted)', fontSize: '14px' }}>No previous donations recorded.</p>
                        )}
                      </div>
                    </div>

                    {/* Quick Access Sidebar */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                      <div style={{ backgroundColor: 'white', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)', textAlign: 'center' }}>
                        <h3 style={{ fontSize: '16px', fontWeight: 600, color: 'var(--text-title)', marginBottom: '16px' }}>Availability Status</h3>
                        <button type="button" className="btn btn-outline" style={{ width: '100%', marginBottom: '12px' }} onClick={() => navigate('/donor/availability')}>
                          Configure Availability
                        </button>
                        <button type="button" className="btn btn-primary" style={{ width: '100%' }} onClick={() => navigate('/donor/cooldown')}>
                          Check Cooldown Status
                        </button>
                      </div>

                      <button
                        type="button"
                        className="btn btn-danger"
                        style={{ padding: '16px', borderRadius: 'var(--radius)', fontSize: '16px', fontWeight: 700 }}
                        onClick={() => setShowDonationModal(true)}
                        disabled={donorProfile?.eligibility_status !== 'ELIGIBLE'}
                      >
                        ❤️ Log Completed Donation
                      </button>
                    </div>
                  </div>
                </div>
              )}

              {/* PATIENT SPECIFIC DASHBOARD VIEW */}
              {user?.user_type === 'Patient' && (
                <div>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '20px', marginBottom: '32px' }}>
                    <div style={{ backgroundColor: 'white', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)' }}>
                      <span style={{ fontSize: '14px', color: 'var(--text-muted)' }}>Total SOS Requests</span>
                      <h2 style={{ fontSize: '32px', fontWeight: 700, margin: '12px 0 0' }}>{patientAnalytics?.total_requests || 0}</h2>
                    </div>
                    <div style={{ backgroundColor: 'white', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)' }}>
                      <span style={{ fontSize: '14px', color: 'var(--text-muted)' }}>Completed Matches</span>
                      <h2 style={{ fontSize: '32px', fontWeight: 700, margin: '12px 0 0' }}>{patientAnalytics?.completed_requests || 0}</h2>
                    </div>
                    <div style={{ backgroundColor: 'white', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)' }}>
                      <span style={{ fontSize: '14px', color: 'var(--text-muted)' }}>Pending Requests</span>
                      <h2 style={{ fontSize: '32px', fontWeight: 700, margin: '12px 0 0' }}>{patientAnalytics?.pending_requests || 0}</h2>
                    </div>
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <button type="button" className="btn btn-danger" style={{ padding: '14px 28px', fontSize: '16px' }} onClick={() => setShowSosModal(true)}>
                        🚨 Trigger Emergency SOS Request
                      </button>
                      <button type="button" className="btn btn-outline" onClick={() => navigate('/matching')}>
                        AI Matching Search
                      </button>
                    </div>

                    {/* SOS Request history list */}
                    <div style={{ backgroundColor: 'white', padding: '24px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}>
                      <h3 style={{ fontSize: '18px', fontWeight: 600, color: 'var(--text-title)', margin: '0 0 16px' }}>My Active SOS Requests</h3>
                      {patientAnalytics?.history && patientAnalytics.history.length > 0 ? (
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                          {patientAnalytics.history.map((alert) => (
                            <div key={alert.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '16px', border: '1px solid var(--border)', borderRadius: '8px', alignItems: 'center' }}>
                              <div>
                                <div style={{ fontWeight: 600, color: 'var(--text-title)' }}>{alert.hospital_name} - {alert.blood_group} ({alert.units_required} Units)</div>
                                <span className={`urgency-badge urgency-${alert.urgency}`} style={{ margin: '6px 0 0' }}>{alert.urgency}</span>
                              </div>
                              <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                                <span style={{ fontSize: '14px', fontWeight: 600, textTransform: 'uppercase', color: 'var(--text-muted)' }}>{alert.status}</span>
                                {alert.status !== 'CLOSED' && alert.status !== 'CANCELLED' && alert.status !== 'COMPLETED' && (
                                  <button type="button" className="btn btn-outline" style={{ padding: '6px 12px', color: '#ef4444', borderColor: '#fca5a5' }} onClick={() => handleCancelAlert(alert.id)}>
                                    Cancel
                                  </button>
                                )}
                              </div>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <p style={{ color: 'var(--text-muted)', fontSize: '14px' }}>No previous SOS requests generated.</p>
                      )}
                    </div>
                  </div>
                </div>
              )}

              {/* HOSPITAL SPECIFIC DASHBOARD VIEW */}
              {user?.user_type === 'Hospital' && (
                <div>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '20px', marginBottom: '32px' }}>
                    <div style={{ backgroundColor: 'white', padding: '20px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)' }}>
                      <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>Total Blood Requests</span>
                      <h2 style={{ fontSize: '28px', fontWeight: 700, margin: '8px 0 0' }}>{hospitalAnalytics?.total_requests || 0}</h2>
                    </div>
                    <div style={{ backgroundColor: 'white', padding: '20px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)' }}>
                      <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>Active Emergencies</span>
                      <h2 style={{ fontSize: '28px', fontWeight: 700, margin: '8px 0 0' }}>{hospitalAnalytics?.active_requests || 0}</h2>
                    </div>
                    <div style={{ backgroundColor: 'white', padding: '20px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)' }}>
                      <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>Completed Matches</span>
                      <h2 style={{ fontSize: '28px', fontWeight: 700, margin: '8px 0 0' }}>{hospitalAnalytics?.completed_requests || 0}</h2>
                    </div>
                    <div style={{ backgroundColor: 'white', padding: '20px', borderRadius: 'var(--radius)', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)' }}>
                      <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>Request Match Success</span>
                      <h2 style={{ fontSize: '28px', fontWeight: 700, margin: '8px 0 0' }}>{hospitalAnalytics?.success_rate || 0}%</h2>
                    </div>
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
                    <div style={{ display: 'flex', gap: '16px' }}>
                      <button type="button" className="btn btn-danger" style={{ padding: '14px 28px', fontSize: '16px' }} onClick={() => setShowSosModal(true)} disabled={user.hospital_verification_status !== 'Verified'}>
                        🚨 Trigger Emergency SOS Request
                      </button>
                      <button type="button" className="btn btn-outline" onClick={() => navigate('/matching')}>
                        AI Donor Matching
                      </button>
                    </div>

                    {user.hospital_verification_status !== 'Verified' && (
                      <div style={{
                        backgroundColor: '#fffbeb',
                        color: '#d97706',
                        border: '1.5px solid #fde68a',
                        padding: '16px',
                        borderRadius: '8px',
                        fontSize: '14px',
                        lineHeight: '140%',
                        fontWeight: 500
                      }}>
                        ⚠️ Verification Pending: Only verified hospitals with checked licenses can trigger SOS emergency requests in the system.
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* TAB 2: MAP VIEW */}
          {currentTab === 'map' && (
            <div>
              <p style={{ textAlign: 'left', marginBottom: '16px', color: 'var(--text-muted)' }}>
                Viewing nearby active compatible blood donors and fallback blood banks pinned on OpenStreetMap.
              </p>
              
              <div className="map-container">
                {mapCenter ? (
                  <MapContainer center={mapCenter} zoom={13} style={{ height: '100%', width: '100%' }}>
                    <TileLayer
                      attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                      url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    />

                    {/* Marker for Current User */}
                    <Marker position={mapCenter} icon={userIcon}>
                      <Popup>
                        <div style={{ fontWeight: 600 }}>Your Current Location</div>
                      </Popup>
                    </Marker>

                    {/* Markers for Donors */}
                    {mapDonors.map((donor) => {
                      if (donor.latitude && donor.longitude) {
                        return (
                          <Marker key={donor.donor_id} position={[donor.latitude, donor.longitude]} icon={donorIcon}>
                            <Popup>
                              <div style={{ textAlign: 'left' }}>
                                <div style={{ fontWeight: 600, fontSize: '14px', color: 'var(--text-title)' }}>
                                  {donor.name} ({donor.blood_group})
                                </div>
                                <div style={{ fontSize: '12px', color: 'var(--text-muted)', margin: '4px 0' }}>
                                  Match Score: {donor.match_score}% | Trust: {donor.trust_score}%
                                </div>
                                <button
                                  type="button"
                                  className="btn btn-primary"
                                  style={{ padding: '4px 8px', fontSize: '12px', marginTop: '8px' }}
                                  onClick={() => navigate(`/chat/${donor.donor_id}/${encodeURIComponent(donor.name)}`)}
                                >
                                  Send Message
                                </button>
                              </div>
                            </Popup>
                          </Marker>
                        );
                      }
                      return null;
                    })}

                    {/* Markers for Fallback Blood Banks */}
                    {mapBanks.map((bank) => {
                      if (bank.latitude && bank.longitude) {
                        return (
                          <Marker key={bank.id} position={[bank.latitude, bank.longitude]} icon={bankIcon}>
                            <Popup>
                              <div style={{ textAlign: 'left' }}>
                                <div style={{ fontWeight: 600, color: 'var(--primary)' }}>🏦 {bank.blood_bank_name}</div>
                                <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{bank.location}</div>
                                <div style={{ fontSize: '12px', fontWeight: 600, color: '#16a34a', marginTop: '4px' }}>{bank.availability_status}</div>
                              </div>
                            </Popup>
                          </Marker>
                        );
                      }
                      return null;
                    })}
                  </MapContainer>
                ) : (
                  <div style={{
                    height: '100%',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    justifyContent: 'center',
                    backgroundColor: '#f8fafc',
                    borderRadius: '8px',
                    padding: '32px',
                    textAlign: 'center',
                    border: '1.5px dashed var(--border)'
                  }}>
                    <Compass size={48} style={{ color: 'var(--text-muted)', marginBottom: '16px', animation: 'spin 4s linear infinite' }} />
                    <h3 style={{ fontSize: '18px', fontWeight: 600, color: 'var(--text-title)', marginBottom: '8px' }}>
                      {error ? 'Location Access Blocked' : 'Locating Your Coordinates...'}
                    </h3>
                    <p style={{ color: 'var(--text-muted)', fontSize: '14px', maxWidth: '380px', lineHeight: '140%', margin: 0 }}>
                      {error ? error : 'Please allow browser location permission to center the map on your location and find nearby donors.'}
                    </p>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* TAB 3: CHAT VIEW */}
          {currentTab === 'chat' && (
            <div style={{ textAlign: 'left' }}>
              {conversations.length === 0 ? (
                <div style={{
                  backgroundColor: 'white',
                  padding: '40px',
                  borderRadius: 'var(--radius)',
                  border: '1px solid var(--border)',
                  textAlign: 'center',
                  color: 'var(--text-muted)'
                }}>
                  {t.noConversations}
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  {conversations.map((conv) => (
                    <div
                      key={conv.user_id}
                      onClick={() => navigate(`/chat/${conv.user_id}/${encodeURIComponent(conv.name)}`)}
                      style={{
                        backgroundColor: 'white',
                        padding: '16px 20px',
                        borderRadius: 'var(--radius)',
                        border: '1px solid var(--border)',
                        boxShadow: 'var(--shadow-sm)',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        cursor: 'pointer',
                        transition: 'all 0.2s ease'
                      }}
                      onMouseEnter={(e) => e.currentTarget.style.borderColor = 'var(--primary-border)'}
                      onMouseLeave={(e) => e.currentTarget.style.borderColor = 'var(--border)'}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                        <div style={{
                          width: '44px',
                          height: '44px',
                          borderRadius: '50%',
                          backgroundColor: 'var(--primary-light)',
                          color: 'var(--primary)',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          fontWeight: 700
                        }}>
                          {conv.name[0]}
                        </div>
                        <div>
                          <div style={{ fontWeight: 600, color: 'var(--text-title)' }}>{conv.name}</div>
                          <div style={{ fontSize: '13px', color: 'var(--text-muted)', marginTop: '4px', maxWidth: '380px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                            {conv.last_message || 'Start chatting...'}
                          </div>
                        </div>
                      </div>

                      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'end', gap: '8px' }}>
                        <ChevronRight size={18} style={{ color: 'var(--text-muted)' }} />
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* TAB 4: ALERTS VIEW */}
          {currentTab === 'alerts' && (
            <div style={{ textAlign: 'left' }}>
              {alerts.length === 0 ? (
                <div style={{
                  backgroundColor: 'white',
                  padding: '40px',
                  borderRadius: 'var(--radius)',
                  border: '1px solid var(--border)',
                  textAlign: 'center',
                  color: 'var(--text-muted)'
                }}>
                  {t.noAlerts}
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                  {alerts.map((alert) => {
                    const isOwnRequest = alert.patient_id === user?.id;
                    const isAcceptedByMe = alert.accepted_by_donor_id === user?.id;

                    return (
                      <div key={alert.id} className="alert-card">
                        <div className="alert-info">
                          <span className={`urgency-badge urgency-${alert.urgency}`}>{alert.urgency}</span>
                          <h3 style={{ fontSize: '18px', fontWeight: 700, color: 'var(--text-title)', margin: '0 0 6px' }}>
                            {alert.hospital_name}
                          </h3>
                          <div style={{ fontSize: '14px', color: 'var(--text-muted)', display: 'flex', gap: '16px' }}>
                            <span>Blood Group: <strong style={{ color: 'var(--text-title)' }}>{alert.blood_group}</strong></span>
                            <span>Units: {alert.units_required}</span>
                          </div>
                          
                          {/* Travel & status logs */}
                          {alert.status === 'DONOR_ACCEPTED' && (
                            <div style={{ marginTop: '12px', fontSize: '14px', color: 'var(--primary)', fontWeight: 500 }}>
                              Donor matches accepted the SOS! Donor name: {alert.accepted_donor_name || 'Raj Donor'}
                            </div>
                          )}

                          {alert.status === 'TRAVELING' && (
                            <div style={{ marginTop: '12px', fontSize: '14px', color: '#3b82f6', fontWeight: 600 }}>
                              Donor is currently traveling. Estimated Arrival: {alert.travel_duration} mins
                            </div>
                          )}

                          {alert.status === 'IN_PROGRESS' && (
                            <div style={{ marginTop: '12px', fontSize: '14px', color: '#eab308', fontWeight: 600 }}>
                              Donation is currently in progress...
                            </div>
                          )}
                        </div>

                        {/* Actions block */}
                        <div style={{ display: 'flex', gap: '12px' }}>
                          {/* Patient actions */}
                          {isOwnRequest && (
                            <>
                              {alert.status === 'DONOR_ACCEPTED' && (
                                <button type="button" className="btn btn-primary" onClick={() => handleConfirmDonation(alert.id)}>
                                  Confirm Completed Donation
                                </button>
                              )}
                              {alert.status === 'TRAVELING' && (
                                <button type="button" className="btn btn-primary" onClick={() => handleConfirmDonation(alert.id)}>
                                  Confirm Donation
                                </button>
                              )}
                              {alert.status !== 'COMPLETED' && alert.status !== 'CLOSED' && (
                                <button type="button" className="btn btn-outline" style={{ color: '#ef4444' }} onClick={() => handleCancelAlert(alert.id)}>
                                  Cancel Request
                                </button>
                              )}
                            </>
                          )}

                          {/* Donor actions */}
                          {user?.user_type === 'Donor' && (
                            <>
                              {alert.status === 'ALERT_SENT' && (
                                <>
                                  <button type="button" className="btn btn-primary" onClick={() => handleAcceptAlert(alert.id)}>
                                    {t.accept}
                                  </button>
                                  <button type="button" className="btn btn-outline" onClick={() => handleDeclineAlert(alert.id)}>
                                    {t.decline}
                                  </button>
                                </>
                              )}

                              {isAcceptedByMe && (
                                <>
                                  {alert.status === 'DONOR_ACCEPTED' && (
                                    <button type="button" className="btn btn-primary" onClick={() => handleStartTravel(alert.id)}>
                                      {t.startTravel}
                                    </button>
                                  )}
                                  {alert.status === 'TRAVELING' && (
                                    <button type="button" className="btn btn-primary" onClick={() => handleStartDonation(alert.id)}>
                                      {t.startDonation}
                                    </button>
                                  )}
                                  {alert.status === 'IN_PROGRESS' && (
                                    <span style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-muted)' }}>
                                      Awaiting Patient/Hospital confirmation
                                    </span>
                                  )}
                                </>
                              )}
                            </>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          )}

          {/* TAB 5: PROFILE VIEW */}
          {currentTab === 'profile' && (
            <div style={{ maxWidth: '640px', margin: '0 auto', textAlign: 'left' }}>
              <div style={{
                backgroundColor: 'white',
                borderRadius: 'var(--radius)',
                border: '1px solid var(--border)',
                boxShadow: 'var(--shadow-sm)',
                padding: '32px'
              }}>
                <form onSubmit={handleEditProfile}>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                    <div className="form-group">
                      <label className="form-label">{t.name}</label>
                      <input type="text" className="form-input" value={user?.name || ''} disabled style={{ backgroundColor: '#f8fafc', color: 'var(--text-muted)' }} />
                    </div>
                    <div className="form-group">
                      <label className="form-label">{t.phoneNumber}</label>
                      <input type="text" className="form-input" value={user?.phone_number || ''} disabled style={{ backgroundColor: '#f8fafc', color: 'var(--text-muted)' }} />
                    </div>
                  </div>

                  <div className="form-group">
                    <label className="form-label">{t.emailAddress}</label>
                    <input type="text" className="form-input" value={user?.email || ''} disabled style={{ backgroundColor: '#f8fafc', color: 'var(--text-muted)' }} />
                  </div>

                  {user?.user_type === 'Hospital' ? (
                    <>
                      <div className="form-group">
                        <label className="form-label">{t.hospitalLicense}</label>
                        <input type="text" className="form-input" value={user?.hospital_license || ''} disabled style={{ backgroundColor: '#f8fafc', color: 'var(--text-muted)' }} />
                      </div>
                      <div className="form-group">
                        <label className="form-label">{t.registeredAddress}</label>
                        <input type="text" className="form-input" value={user?.registered_address || ''} disabled style={{ backgroundColor: '#f8fafc', color: 'var(--text-muted)' }} />
                      </div>
                    </>
                  ) : (
                    <>
                      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '12px' }}>
                        <div className="form-group">
                          <label className="form-label">{t.bloodGroup}</label>
                          <select className="form-select" value={editBloodGroup} onChange={(e) => setEditBloodGroup(e.target.value)}>
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
                        <div className="form-group">
                          <label className="form-label">{t.age}</label>
                          <input type="number" className="form-input" value={editAge} onChange={(e) => setEditAge(e.target.value)} required />
                        </div>
                        <div className="form-group">
                          <label className="form-label">{t.gender}</label>
                          <select className="form-select" value={editGender} onChange={(e) => setEditGender(e.target.value)}>
                            <option value="Male">Male</option>
                            <option value="Female">Female</option>
                            <option value="Other">Other</option>
                          </select>
                        </div>
                      </div>
                    </>
                  )}

                  <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '16px' }}>
                    Save Profile Changes
                  </button>
                </form>
              </div>
            </div>
          )}
        </div>

        {/* Bottom Navigation for Mobile */}
        <nav className="app-bottom-nav">
          <button onClick={() => setTab('home')} className={`bottom-nav-item btn-outline ${currentTab === 'home' ? 'active' : ''}`} style={{ border: 'none', background: 'transparent' }}>
            <Home />
            <span>Home</span>
          </button>
          
          <button onClick={() => setTab('map')} className={`bottom-nav-item btn-outline ${currentTab === 'map' ? 'active' : ''}`} style={{ border: 'none', background: 'transparent' }}>
            <Map />
            <span>Map</span>
          </button>

          <button onClick={() => setTab('chat')} className={`bottom-nav-item btn-outline ${currentTab === 'chat' ? 'active' : ''}`} style={{ border: 'none', background: 'transparent' }}>
            <MessageSquare />
            <span>Chat</span>
          </button>

          <button onClick={() => setTab('alerts')} className={`bottom-nav-item btn-outline ${currentTab === 'alerts' ? 'active' : ''}`} style={{ border: 'none', background: 'transparent' }}>
            <Bell />
            <span>Alerts</span>
          </button>

          <button onClick={() => setTab('profile')} className={`bottom-nav-item btn-outline ${currentTab === 'profile' ? 'active' : ''}`} style={{ border: 'none', background: 'transparent' }}>
            <User />
            <span>Profile</span>
          </button>
        </nav>
      </main>

      {/* SOS TRIGGER MODAL */}
      {showSosModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <AlertTriangle style={{ color: '#ef4444' }} />
              <span>{t.triggerEmergency}</span>
            </div>

            <form onSubmit={handleTriggerSos}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
                <div className="form-group" style={{ margin: 0 }}>
                  <label className="form-label">{t.bloodGroup}</label>
                  <select className="form-select" value={sosBlood} onChange={(e) => setSosBlood(e.target.value)}>
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
                  <select className="form-select" value={sosUrgency} onChange={(e) => setSosUrgency(e.target.value)}>
                    <option value="Critical">Critical</option>
                    <option value="High">High</option>
                    <option value="Moderate">Moderate</option>
                    <option value="Normal">Normal</option>
                  </select>
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
                <div className="form-group" style={{ margin: 0 }}>
                  <label className="form-label">{t.unitsRequired}</label>
                  <input type="number" className="form-input" min="1" value={sosUnits} onChange={(e) => setSosUnits(e.target.value)} required />
                </div>
                <div className="form-group" style={{ margin: 0 }}>
                  <label className="form-label">{t.hospitalName}</label>
                  <input type="text" className="form-input" value={sosHospital} onChange={(e) => setSosHospital(e.target.value)} placeholder="e.g. City Hospital" required />
                </div>
              </div>

              {sosLocStatus && (
                <div style={{
                  marginBottom: '20px',
                  fontSize: '14px',
                  color: sosLocStatus.includes('successfully') || sosLocStatus.includes('automatically') ? '#16a34a' : 'var(--text-muted)',
                  fontWeight: 500,
                  textAlign: 'left',
                  backgroundColor: '#f8fafc',
                  padding: '12px',
                  borderRadius: '6px',
                  border: '1px solid var(--border)'
                }}>
                  {sosLocStatus}
                </div>
              )}

              <div style={{ display: 'flex', gap: '12px' }}>
                <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setShowSosModal(false)}>
                  {t.cancel}
                </button>
                <button type="submit" className="btn btn-primary btn-danger" style={{ flex: 1 }} disabled={loadingSosLocation || !sosLat || !sosLng}>
                  {t.confirmTrigger}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* DONATION RECORD MODAL */}
      {showDonationModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <Heart style={{ color: '#ef4444' }} />
              <span>Record a Completed Donation</span>
            </div>

            <p style={{ fontSize: '14px', color: 'var(--text-muted)', marginBottom: '24px', lineHeight: '140%', textAlign: 'left' }}>
              Confirm that you have completed a blood donation cycle. Your profile status will be updated to INELIGIBLE, and a 90-day cooldown period will be activated.
            </p>

            <form onSubmit={handleRecordDonation}>
              <div className="form-group" style={{ textAlign: 'left', marginBottom: '24px' }}>
                <label className="form-label">Last Donation Date</label>
                <input
                  type="date"
                  className="form-input"
                  value={lastDonationDate}
                  onChange={(e) => setLastDonationDate(e.target.value)}
                  max={new Date().toISOString().split('T')[0]}
                  required
                />
              </div>

              <div style={{ display: 'flex', gap: '12px' }}>
                <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setShowDonationModal(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" style={{ flex: 1 }}>
                  Confirm Donation
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* DIGITAL CERTIFICATE POPUP */}
      {certInfo && (
        <div className="modal-overlay">
          <div className="modal-content" style={{ maxWidth: '400px', textAlign: 'center' }}>
            <Award size={64} style={{ color: '#eab308', marginBottom: '16px' }} />
            <h2 style={{ fontSize: '22px', fontWeight: 700, color: 'var(--text-title)', margin: '0 0 8px' }}>
              Donation Certificate
            </h2>
            <p style={{ fontSize: '13px', color: 'var(--text-muted)', marginBottom: '24px' }}>
              Verification ID: {certInfo.certificate_id}
            </p>

            <div style={{
              border: '1.5px solid var(--border)',
              borderRadius: '8px',
              padding: '16px',
              backgroundColor: '#f8fafc',
              textAlign: 'left',
              fontSize: '14px',
              marginBottom: '24px',
              display: 'flex',
              flexDirection: 'column',
              gap: '8px'
            }}>
              <div>Donor: <strong style={{ color: 'var(--text-title)' }}>{certInfo.donor_name}</strong></div>
              <div>Blood Group: <strong>{certInfo.blood_group}</strong></div>
              <div>Hospital: <strong>{certInfo.hospital_name}</strong></div>
              <div>Date: <strong>{certInfo.donation_date}</strong></div>
            </div>

            <div style={{
              display: 'inline-block',
              padding: '12px',
              backgroundColor: 'white',
              border: '1px solid var(--border)',
              borderRadius: '8px',
              marginBottom: '24px'
            }}>
              {/* Mock QR display */}
              <div style={{ width: '120px', height: '120px', backgroundColor: '#f1f5f9', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '11px', color: 'var(--text-muted)', fontWeight: 500 }}>
                QR Code QR Verified
              </div>
            </div>

            <button type="button" className="btn btn-primary" style={{ width: '100%' }} onClick={() => setCertInfo(null)}>
              Close Certificate
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
