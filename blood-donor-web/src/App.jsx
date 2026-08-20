import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { LanguageProvider } from './context/LanguageContext';

// Auth Pages
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import VerifyOtp from './pages/auth/VerifyOtp';
import ForgotPassword from './pages/auth/ForgotPassword';
import ResetPassword from './pages/auth/ResetPassword';

// Common Pages
import Splash from './pages/common/Splash';
import LanguageSelection from './pages/common/LanguageSelection';
import Onboarding from './pages/common/Onboarding';
import Welcome from './pages/common/Welcome';
import Dashboard from './pages/common/Dashboard';
import Settings from './pages/common/Settings';
import ChatConversation from './pages/common/ChatConversation';

// Role Specific Pages
import Availability from './pages/donor/Availability';
import Cooldown from './pages/donor/Cooldown';
import AiMatching from './pages/patient/AiMatching';

import './App.css';

// Route Guard Component
const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '18px', fontWeight: 600 }}>
        Loading session context...
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.user_type)) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};

function App() {
  return (
    <AuthProvider>
      <LanguageProvider>
        <Router>
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<Splash />} />
            <Route path="/splash" element={<Splash />} />
            <Route path="/select-language" element={<LanguageSelection />} />
            <Route path="/onboarding" element={<Onboarding />} />
            <Route path="/welcome" element={<Welcome />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/verify-otp/:email" element={<VerifyOtp />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/reset-password" element={<ResetPassword />} />

            {/* Authenticated Common Routes */}
            <Route path="/dashboard" element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            } />
            <Route path="/settings" element={
              <ProtectedRoute>
                <Settings />
              </ProtectedRoute>
            } />
            <Route path="/chat/:receiverId/:receiverName" element={
              <ProtectedRoute>
                <ChatConversation />
              </ProtectedRoute>
            } />

            {/* Donor Restricted Routes */}
            <Route path="/donor/availability" element={
              <ProtectedRoute allowedRoles={['Donor']}>
                <Availability />
              </ProtectedRoute>
            } />
            <Route path="/donor/cooldown" element={
              <ProtectedRoute allowedRoles={['Donor']}>
                <Cooldown />
              </ProtectedRoute>
            } />

            {/* Patient/Hospital Restricted Routes */}
            <Route path="/matching" element={
              <ProtectedRoute allowedRoles={['Patient', 'Hospital']}>
                <AiMatching />
              </ProtectedRoute>
            } />

            {/* Fallback routing */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Router>
      </LanguageProvider>
    </AuthProvider>
  );
}

export default App;
