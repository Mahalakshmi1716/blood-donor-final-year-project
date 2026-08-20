import apiClient from './apiClient';

export const donorApi = {
  createProfile: (payload) => apiClient.post('/api/donors/profile', payload).then((res) => res.data),
  
  getProfile: () => apiClient.get('/api/donors/profile').then((res) => res.data),
  
  updateAvailability: (payload) => apiClient.post('/api/donors/availability', payload).then((res) => res.data),
  
  recordDonation: (payload) => apiClient.post('/api/donors/record-donation', payload || {}).then((res) => res.data),
  
  getTipOfTheDay: () => apiClient.get('/api/donors/tip-of-the-day').then((res) => res.data),
};

export default donorApi;
