// src/api/admin.js
import apiClient from "./index";

// 신고 목록 조회
export const getReports = async ({
  page = 0,
  size = 10,
  status = "", // "PENDING", "APPROVED", "REJECTED" or "" (전체)
  targetType = "", // "POST", "COMMENT" or "" (전체)
} = {}) => {
      
  const params = { page, size };
  if (status) params.status = status;
  if (targetType) params.targetType = targetType;

  return await apiClient.get("/reports/admin", { params });
};

// 신고 상태 변경 (PATCH /api/reports/admin/{reportId}/status)
export const updateReportStatus = async (reportId, status) => {
  return await apiClient.patch(`/reports/admin/${reportId}/status`, null, {
    params: { status },
  });
};