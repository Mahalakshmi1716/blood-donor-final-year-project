import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:5000';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor to inject JWT from localStorage
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    console.log(`[API REQUEST] ${config.method.toUpperCase()} ${config.url}`, config.data || '');
    return config;
  },
  (error) => {
    console.error(`[API REQUEST ERROR]`, error);
    return Promise.reject(error);
  }
);

// Interceptor to handle session expirations
apiClient.interceptors.response.use(
  (response) => {
    console.log(`[API RESPONSE] ${response.status} ${response.config.url}`, response.data || '');
    return response;
  },
  (error) => {
    if (error.response) {
      console.error(`[API RESPONSE ERROR] ${error.response.status} ${error.response.config.url}`, error.response.data || '');
      
      if (error.response.status === 401 || error.response.status === 403) {
        // If it says unverified user, don't clear session since they must verify OTP
        if (error.response.data && error.response.data.unverified) {
          return Promise.reject(error);
        }
        
        // Otherwise, clear invalid session
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        if (window.location.pathname !== '/login' && window.location.pathname !== '/register' && window.location.pathname !== '/splash' && !window.location.pathname.startsWith('/verify-otp')) {
          window.location.href = '/login';
        }
      }
    } else {
      console.error(`[API NETWORK/UNKNOWN ERROR]`, error);
    }
    return Promise.reject(error);
  }
);

export default apiClient;
export { API_BASE_URL };
