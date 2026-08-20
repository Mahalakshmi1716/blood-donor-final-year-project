import apiClient from './apiClient';

export const patientApi = {
  searchDonors: (payload) => apiClient.post('/api/patients/search', payload).then((res) => res.data),
  
  triggerSos: (payload) => apiClient.post('/api/patients/sos', payload).then((res) => res.data),
  
  acceptAlert: (alertId) => apiClient.post(`/api/patients/alerts/${alertId}/accept`, {}).then((res) => res.data),
  
  declineAlert: (alertId) => apiClient.post(`/api/patients/alerts/${alertId}/decline`, {}).then((res) => res.data),
  
  startTravel: (alertId) => apiClient.post(`/api/patients/alerts/${alertId}/start-travel`, {}).then((res) => res.data),
  
  startDonation: (alertId) => apiClient.post(`/api/patients/alerts/${alertId}/start-donation`, {}).then((res) => res.data),
  
  confirmDonation: (alertId) => apiClient.post(`/api/patients/alerts/${alertId}/confirm-donation`, {}).then((res) => res.data),
  
  cancelAlert: (alertId, reason = 'Cancelled') => apiClient.post(`/api/patients/alerts/${alertId}/cancel`, { reason }).then((res) => res.data),
  
  getAlerts: () => apiClient.get('/api/patients/alerts').then((res) => res.data),
  
  exportReportUrl: (alertId) => `${apiClient.defaults.baseURL}/api/patients/alerts/${alertId}/export`,
  
  getHospitalAnalytics: () => apiClient.get('/api/patients/hospital-analytics').then((res) => res.data),
  
  getPatientAnalytics: () => apiClient.get('/api/patients/analytics').then((res) => res.data),
};

export default patientApi;
