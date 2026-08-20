import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { HeartHandshake, ShieldAlert, Award, Compass, ChevronRight, ChevronLeft } from 'lucide-react';

export default function Onboarding() {
  const navigate = useNavigate();
  const [activeStep, setActiveStep] = useState(0);

  const steps = [
    {
      icon: <HeartHandshake size={64} style={{ color: '#dc2626' }} />,
      title: "Find Blood Donors",
      description: "Smart Blood Donor Finder connects patients with compatible blood donors in real-time. We help you find active donors nearby when seconds count."
    },
    {
      icon: <ShieldAlert size={64} style={{ color: '#dc2626' }} />,
      title: "Donor Alerts",
      description: "Registered donors receive critical emergency notifications via push alert and email when a matching compatibility request is generated nearby."
    },
    {
      icon: <Award size={64} style={{ color: '#dc2626' }} />,
      title: "Hospitals & SOS Requests",
      description: "Verified hospitals and patient families can trigger immediate SOS requests specifying required units, blood group, and urgency levels."
    },
    {
      icon: <Compass size={64} style={{ color: '#dc2626' }} />,
      title: "Location & Permissions",
      description: "We use browser GPS coordinates to calculate distance and match donors. We request permissions and profile details strictly to secure matching and ensure patient-donor safety."
    }
  ];

  const handleNext = () => {
    if (activeStep < steps.length - 1) {
      setActiveStep(activeStep + 1);
    } else {
      navigate('/welcome');
    }
  };

  const handleBack = () => {
    if (activeStep > 0) {
      setActiveStep(activeStep - 1);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card" style={{ maxWidth: '440px', padding: '40px', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <div style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          textAlign: 'center',
          flexGrow: 1,
          minHeight: '280px',
          justifyContent: 'center'
        }}>
          <div style={{ marginBottom: '24px', animation: 'fadeIn 0.5s ease' }}>
            {steps[activeStep].icon}
          </div>
          <h2 style={{ fontSize: '24px', fontWeight: 700, color: 'var(--text-title)', marginBottom: '16px' }}>
            {steps[activeStep].title}
          </h2>
          <p style={{ fontSize: '15px', color: 'var(--text-muted)', lineHeight: '150%' }}>
            {steps[activeStep].description}
          </p>
        </div>

        {/* Step indicators */}
        <div style={{ display: 'flex', gap: '8px', margin: '24px 0' }}>
          {steps.map((_, index) => (
            <div
              key={index}
              style={{
                width: index === activeStep ? '24px' : '8px',
                height: '8px',
                borderRadius: '4px',
                backgroundColor: index === activeStep ? '#dc2626' : '#cbd5e1',
                transition: 'all 0.3s ease'
              }}
            />
          ))}
        </div>

        <div style={{ display: 'flex', width: '100%', gap: '16px', marginTop: '12px' }}>
          {activeStep > 0 ? (
            <button
              type="button"
              className="btn btn-secondary"
              style={{ flex: 1 }}
              onClick={handleBack}
            >
              <ChevronLeft size={18} />
              <span>Back</span>
            </button>
          ) : (
            <button
              type="button"
              className="btn btn-outline"
              style={{ flex: 1 }}
              onClick={() => navigate('/welcome')}
            >
              Skip
            </button>
          )}
          
          <button
            type="button"
            className="btn btn-primary"
            style={{ flex: 1 }}
            onClick={handleNext}
          >
            <span>{activeStep === steps.length - 1 ? "Get Started" : "Next"}</span>
            <ChevronRight size={18} />
          </button>
        </div>
      </div>
      
      <style>{`
        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(10px); }
          to { opacity: 1; transform: translateY(0); }
        }
      `}</style>
    </div>
  );
}
