// src/api/user.js
import apiClient from "./index";

// 특정 유저 정보 조회
export const getUser = async (id) => {
  return await apiClient.get(`/users/${id}`);
};

// 유저 삭제
export const deleteUser = async (id) => {
  return await apiClient.delete(`/users/${id}`);
};

// 유저 프로필 조회
export const getUserProfile = async (userId) => {
  return await apiClient.get(`/users/${userId}/profile`);
};

// 유저 프로필 수정
export const updateUserProfile = async (userId, profileData) => {
  return await apiClient.put(`/users/${userId}/profile`, profileData);
};

// import axios from "axios";

// const API_BASE = "http://localhost:8080/api/users";

// export const getUser = async (id) => {
//   const response = await axios.get(`${API_BASE}/${id}`);
//   return response.data;
// };

// export const deleteUser = async (id) => {
//   const response = await axios.delete(`${API_BASE}/${id}`);
//   return response.data;
// };

// export const getUserProfile = async (userId) => {
//   const response = await axios.get(`${API_BASE}/${userId}/profile`);
//   return response.data;
// };

// export const updateUserProfile = async (userId, profileData) => {
//   const params = new URLSearchParams(profileData).toString();
//   const response = await axios.put(`${API_BASE}/${userId}/profile?${params}`);
//   return response.data;
// };
