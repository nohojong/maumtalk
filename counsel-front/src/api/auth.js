import axios from 'axios';

const apiClient = axios.create({
  baseURL: '/api/auth' // Vite 개발 서버의 프록시 설정을 이용할 예정
});

export const signup = (email, password) => {
  return apiClient.post('/signup', { email, password });
};

export const login = (email, password) => {
  return apiClient.post('/login', { email, password });
};

export const logout = () => {
  return apiClient.post('/logout');
};