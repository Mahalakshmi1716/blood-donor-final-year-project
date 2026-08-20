import { createContext, useContext, useState, useEffect } from 'react';

const LanguageContext = createContext(null);

const translations = {
  en: {
    appTitle: "Smart Blood Donor Finder",
    login: "Login",
    register: "Register",
    signup: "Sign Up",
    phoneNumber: "Phone Number",
    emailAddress: "Email Address",
    password: "Password",
    name: "Full Name",
    bloodGroup: "Blood Group",
    gender: "Gender",
    age: "Age",
    role: "User Role",
    donor: "Donor",
    patient: "Patient",
    hospital: "Hospital",
    sos: "Emergency SOS",
    alerts: "Alerts",
    map: "Donor Map",
    chat: "Chat Messages",
    profile: "User Profile",
    settings: "Settings",
    availability: "Availability",
    matching: "AI Donor Matching",
    cooldown: "Donation Cooldown",
    logout: "Log Out",
    welcomeBack: "Welcome Back",
    dontHaveAccount: "Don't have an account?",
    alreadyHaveAccount: "Already have an account?",
    verifyOtp: "Verify OTP",
    verifyOtpDesc: "We have sent a verification code to",
    otpCode: "OTP Code",
    verify: "Verify",
    resendOtp: "Resend OTP",
    activeEmergency: "Active Emergency Alerts",
    triggerEmergency: "Trigger SOS Request",
    hospitalLicense: "Hospital License Number",
    registeredAddress: "Registered Address",
    latitude: "Latitude",
    longitude: "Longitude",
    unitsRequired: "Blood Units Required",
    urgencyLevel: "Urgency Level",
    hospitalName: "Hospital Name",
    confirmTrigger: "Send Alert",
    cancel: "Cancel",
    tipOfTheDay: "Tip of the Day",
    donationHistory: "Donation History",
    trustScore: "Trust Score",
    healthScore: "Health Score",
    status: "Status",
    eligible: "ELIGIBLE",
    ineligible: "INELIGIBLE (Cooldown Active)",
    daysLeft: "days remaining",
    availableToday: "Available for Donation Today",
    updateAvailability: "Update Availability",
    noAlerts: "No active emergency alerts found.",
    noConversations: "No chat history found. Search matching donors to initiate a chat.",
    accept: "Accept",
    decline: "Decline",
    startTravel: "Start Travel",
    startDonation: "Start Donation Procedure",
    confirmDonation: "Confirm Completion",
    downloadReport: "Download CSV Report",
  },
  hi: {
    appTitle: "स्मार्ट रक्तदाता खोजक",
    login: "लॉगिन",
    register: "पंजीकरण करें",
    signup: "साइन अप",
    phoneNumber: "फ़ोन नंबर",
    emailAddress: "ईमेल पता",
    password: "पासवर्ड",
    name: "पूरा नाम",
    bloodGroup: "रक्त समूह",
    gender: "लिंग",
    age: "उम्र",
    role: "उपयोगकर्ता भूमिका",
    donor: "रक्तदाता",
    patient: "मरीज",
    hospital: "अस्पताल",
    sos: "आपातकालीन एसओएस",
    alerts: "अलर्ट",
    map: "रक्तदाता मानचित्र",
    chat: "चैट संदेश",
    profile: "प्रोफ़ाइल",
    settings: "सेटिंग्स",
    availability: "उपलब्धता",
    matching: "एआई दाता मिलान",
    cooldown: "दान कूलडाउन",
    logout: "लॉग आउट",
    welcomeBack: "आपका स्वागत है",
    dontHaveAccount: "खाता नहीं है?",
    alreadyHaveAccount: "पहले से खाता है?",
    verifyOtp: "ओटीपी सत्यापित करें",
    verifyOtpDesc: "हमने एक सत्यापन कोड भेजा है",
    otpCode: "ओटीपी कोड",
    verify: "सत्यापित करें",
    resendOtp: "ओटीपी पुनः भेजें",
    activeEmergency: "सक्रिय आपातकालीन अलर्ट",
    triggerEmergency: "एसओएस अनुरोध भेजें",
    hospitalLicense: "अस्पताल लाइसेंस संख्या",
    registeredAddress: "पंजीकृत पता",
    latitude: "अक्षांश",
    longitude: "देशांतर",
    unitsRequired: "रक्त इकाइयों की आवश्यकता",
    urgencyLevel: "अति-आवश्यकता स्तर",
    hospitalName: "अस्पताल का नाम",
    confirmTrigger: "अलर्ट भेजें",
    cancel: "रद्द करें",
    tipOfTheDay: "आज का सुझाव",
    donationHistory: "रक्तदान का इतिहास",
    trustScore: "विश्वास स्कोर",
    healthScore: "स्वास्थ्य स्कोर",
    status: "स्थिति",
    eligible: "योग्य",
    ineligible: "अयोग्य (कूलडाउन सक्रिय)",
    daysLeft: "दिन शेष",
    availableToday: "आज दान के लिए उपलब्ध",
    updateAvailability: "उपलब्धता अपडेट करें",
    noAlerts: "कोई सक्रिय आपातकालीन अलर्ट नहीं मिला।",
    noConversations: "कोई चैट इतिहास नहीं मिला। चैट शुरू करने के लिए दाताओं की खोज करें।",
    accept: "स्वीकार करें",
    decline: "अस्वीकार करें",
    startTravel: "यात्रा शुरू करें",
    startDonation: "रक्तदान प्रक्रिया शुरू करें",
    confirmDonation: "पूर्णता की पुष्टि करें",
    downloadReport: "सीएसवी रिपोर्ट डाउनलोड करें",
  },
  ta: {
    appTitle: "ஸ்மார்ட் இரத்த தானம் செயலி",
    login: "உள்நுழைக",
    register: "பதிவு செய்க",
    signup: "பதிவு செய்க",
    phoneNumber: "தொலைபேசி எண்",
    emailAddress: "மின்னஞ்சல் முகவரி",
    password: "கடவுச்சொல்",
    name: "முழு பெயர்",
    bloodGroup: "இரத்த வகை",
    gender: "பாலினம்",
    age: "வயது",
    role: "பயனர் வகை",
    donor: "கொடையாளர்",
    patient: "நோயாளி",
    hospital: "மருத்துவமனை",
    sos: "அவசர உதவி",
    alerts: "அறிவிப்புகள்",
    map: "வரைபடம்",
    chat: "உரையாடல்கள்",
    profile: "சுயவிவரம்",
    settings: "அமைப்புகள்",
    availability: "இருப்பு நிலை",
    matching: "பொருத்தமான கொடையாளர்",
    cooldown: "தான ஓய்வு காலம்",
    logout: "வெளியேறு",
    welcomeBack: "வரவேற்கிறோம்",
    dontHaveAccount: "கணக்கு இல்லையா?",
    alreadyHaveAccount: "ஏற்கனவே கணக்கு உள்ளதா?",
    verifyOtp: "OTP சரிபார்ப்பு",
    verifyOtpDesc: "சரிபார்ப்புக் குறியீடு அனுப்பப்பட்டுள்ளது",
    otpCode: "OTP குறியீடு",
    verify: "சரிபார்",
    resendOtp: "OTP ஐ மீண்டும் அனுப்பு",
    activeEmergency: "செயலில் உள்ள அவசர அறிவிப்புகள்",
    triggerEmergency: "அவசர கோரிக்கை அனுப்பு",
    hospitalLicense: "மருத்துவமனை உரிம எண்",
    registeredAddress: "பதிவு செய்யப்பட்ட முகவரி",
    latitude: "அட்சரேகை",
    longitude: "தீர்க்கரேகை",
    unitsRequired: "தேவைப்படும் அலகுகள்",
    urgencyLevel: "அவசர நிலை",
    hospitalName: "மருத்துவமனையின் பெயர்",
    confirmTrigger: "அறிவிப்பை அனுப்பு",
    cancel: "ரத்து செய்",
    tipOfTheDay: "இன்றைய குறிப்பு",
    donationHistory: "தான வரலாறு",
    trustScore: "நம்பகத்தன்மை மதிப்பு",
    healthScore: "உடல்நல மதிப்பு",
    status: "நிலை",
    eligible: "தகுதியுடையவர்",
    ineligible: "தகுதியற்றவர் (இடைவெளி காலம்)",
    daysLeft: "நாட்கள் மீதமுள்ளன",
    availableToday: "இன்று தானம் செய்ய தயார்",
    updateAvailability: "இருப்பை புதுப்பி",
    noAlerts: "அவசர அறிவிப்புகள் எதுவும் இல்லை.",
    noConversations: "உரையாடல்கள் எதுவும் இல்லை. தானம் செய்வோரைத் தேடி உரையாடலைத் தொடங்கவும்.",
    accept: "ஏற்றுக்கொள்",
    decline: "நிராகரி",
    startTravel: "பயணத்தை தொடங்கு",
    startDonation: "தானத்தை தொடங்கு",
    confirmDonation: "தானம் முடிந்ததை உறுதிசெய்",
    downloadReport: "அறிக்கையை பதிவிறக்கு",
  }
};

export const LanguageProvider = ({ children }) => {
  const [lang, setLang] = useState(() => {
    // Sync language preference from user storage or local preference
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      try {
        const u = JSON.parse(savedUser);
        if (u.preferred_language && translations[u.preferred_language]) {
          return u.preferred_language;
        }
      } catch (e) {}
    }
    return localStorage.getItem('lang') || 'en';
  });

  const changeLanguage = (newLang) => {
    if (translations[newLang]) {
      setLang(newLang);
      localStorage.setItem('lang', newLang);
    }
  };

  const t = translations[lang] || translations.en;

  return (
    <LanguageContext.Provider value={{ lang, changeLanguage, t }}>
      {children}
    </LanguageContext.Provider>
  );
};

export const useLanguage = () => {
  const context = useContext(LanguageContext);
  if (!context) {
    throw new Error('useLanguage must be used within a LanguageProvider');
  }
  return context;
};
