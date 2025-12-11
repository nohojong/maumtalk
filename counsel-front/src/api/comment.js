// src/api/comment.js
import apiClient from "./index";

// 특정 게시글의 모든 댓글 조회
export const getCommentsByPostId = async (postId) => {
  return await apiClient.get(`/board/${postId}/comments`);
};

// 댓글 생성
export const createComment = async (postId, commentData) => {
  return await apiClient.post(`/board/${postId}/comments`, commentData);
};

// 댓글 수정
export const updateComment = async (commentId, commentData) => {
  return await apiClient.put(`/board/comments/${commentId}`, commentData);
};

// 댓글 삭제
export const deleteComment = async (commentId) => {
  return await apiClient.delete(`/board/comments/${commentId}`);
};

// 댓글 신고
export const reportComment = async (commentId, reason) => {
  const reportData = {
    targetId: commentId,
    targetType: "COMMENT", // 신고 유형은 'COMMENT'로 고정
    reason: reason,
  };  
  return await apiClient.post("/reports", reportData);
};