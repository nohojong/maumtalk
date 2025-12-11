import axios from "axios";

const apiClient = axios.create({
  baseURL: '/api/payments',
  withCredentials: true
});

// 사용자의 이용권 상태 조회
export const checkAccessStatus = () => {
  return apiClient.get('/access-status');
};

// 결제 정보 검증 및 이용권 만료일 갱신
export const updateAccessStatus = (paymentData) => {
  return apiClient.post('/verify', paymentData);
};