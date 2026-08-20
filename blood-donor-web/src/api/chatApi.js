import apiClient from './apiClient';

export const chatApi = {
  sendMessage: (receiverId, content) => apiClient.post('/api/chat/send', { receiver_id: receiverId, content }).then((res) => res.data),
  
  getChatHistory: (otherUserId) => apiClient.get(`/api/chat/history/${otherUserId}`).then((res) => res.data),
  
  getConversations: () => apiClient.get('/api/chat/conversations').then((res) => res.data),
};

export default chatApi;
