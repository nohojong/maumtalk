// src/api/conversation.js
import apiClient from "./index";

// 모든 대화 목록 조회
export const fetchConversations = async () => {
  return await apiClient.get("/conversations");
};

// 특정 대화 상세 조회
export const fetchConversationDetail = async (id) => {
  return await apiClient.get(`/conversations/${id}`);
};

// 대화 수정
export const updateConversation = async (id, data) => {
  return await apiClient.put(`/conversations/${id}`, {
    title: data.title,
    memo: data.memo,
  });
};

// 대화 삭제
export const deleteConversation = async (id) => {
  return await apiClient.delete(`/conversations/${id}`);
};

// import axios from "axios";

// const apiClient = axios.create({
//     baseURL: '/api/conversations',
//     withCredentials: true
// });

// // 모든 대화 목록 (GET /api/conversations)
// export const fetchConversations = () => {
//     return apiClient.get();
// }

// // 특정 대화 상세 (GET /api/conversations/{id})
// export const fetchConversationDetail = (id) => {
//     return apiClient.get(`/${id}`);
// }

// // 대화 수정 (PUT /api/conversations/{id})
// export const updateConversation = (id, data) => {
//     return apiClient.put(`/${id}`, {title: data.title, summary: data.summary});
// }

// // 대화 삭제 (DELETE /api/conversations/{id})
// export const deleteConversation = (id) => {
//     return apiClient.delete(`/${id}`);
// }
