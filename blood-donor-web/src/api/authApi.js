import apiClient from './apiClient';

export const authApi = {
  register: (payload) => apiClient.post('/api/auth/register', payload).then((res) => res.data),
  
  login: (payload) => apiClient.post('/api/auth/login', payload).then((res) => res.data),
  
  sendOtp: (email) => apiClient.post('/api/auth/send-email-otp', { email }).then((res) => res.data),
  
  verifyOtp: (email, otpCode) => apiClient.post('/api/auth/verify-email-otp', { email, otp_code: otpCode }).then((res) => res.data),
  
  getMe: () => apiClient.get('/api/auth/me').then((res) => res.data),
  
  updateProfile: (payload) => apiClient.post('/api/auth/update', payload).then((res) => res.data),
  
  forgotPassword: (email) => apiClient.post('/api/auth/forgot-password', { email }).then((res) => res.data),
  
  resetPassword: (payload) => apiClient.post('/api/auth/reset-password', payload).then((res) => res.data),
  
  verifyHospital: (hospitalId, status = 'Verified') => apiClient.post('/api/auth/verify-hospital', { hospital_id: hospitalId, status }).then((res) => res.data),
};

export default authApi;
