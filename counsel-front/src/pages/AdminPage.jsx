// src/pages/AdminPage.jsx
import { useState, useEffect } from "react";
import { getReports, updateReportStatus } from "../api/admin";
import DetailsModal from "../components/DetailsModal";

export default function AdminPage() {
  const [reports, setReports] = useState([]);
  const [pageInfo, setPageInfo] = useState({
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 1,
  });

  // 필터링을 위한 state 추가
  const [filters, setFilters] = useState({
    status: "", // PENDING, APPROVED, REJECTED
    targetType: "", // POST, COMMENT
  });

  // 상세 보기 모달을 위한 state 추가
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false);
  const [modalContent, setModalContent] = useState(""); // 모달에 표시될 텍스트

  // 데이터 조회 로직을 필터와 페이지 번호 변경 시 모두 실행되도록 수정
  useEffect(() => {
    const fetchReports = async () => {
      try {
        const response = await getReports({
          page: pageInfo.page,
          status: filters.status,
          targetType: filters.targetType,
        });
        setReports(response.data.content);
        setPageInfo({
          page: response.data.page.number,
          size: response.data.page.size,
          totalElements: response.data.page.totalElements,
          totalPages: response.data.page.totalPages,
        });
      } catch (error) {
        console.error("신고 목록 조회 실패:", error);
        alert("신고 목록을 불러오는 데 실패했습니다.");
      }
    };
    fetchReports();
  }, [pageInfo.page, filters]); // filters가 변경될 때도 데이터 다시 조회

  // 모달을 여는 핸들러
  const handleOpenDetailsModal = (content) => {
    setModalContent(content);
    setIsDetailsModalOpen(true);
  };

  // 모달을 닫는 핸들러
  const handleCloseDetailsModal = () => {
    setIsDetailsModalOpen(false);
    setModalContent(""); // 내용을 비워주는 것이 좋음
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    // 필터 변경 시, 첫 페이지부터 다시 보도록 page를 0으로 초기화
    setPageInfo((prev) => ({ ...prev, page: 0 }));
    setFilters((prev) => ({ ...prev, [name]: value }));
  };

  // 신고 처리 핸들러 (승인/반려)
  const handleProcessReport = async (reportId, newStatus) => {
    if (!window.confirm(`이 신고를 '${newStatus}' 상태로 처리하시겠습니까?`)) {
      return;
    }
    try {
      await updateReportStatus(reportId, newStatus);
      alert("신고가 성공적으로 처리되었습니다.");
      // 처리 후 목록을 새로고침하여 변경된 상태를 반영
      setReports((prevReports) =>
        prevReports.map((report) =>
          report.id === reportId ? { ...report, status: newStatus } : report
        )
      );
    } catch (error) {
      console.error("신고 처리 실패:", error);
      alert("신고 처리에 실패했습니다.");
    }
  };

  const handlePageChange = (newPage) => {
    // 페이지 번호가 유효한 범위 내에 있을 때만 상태 업데이트
    if (newPage >= 0 && newPage < pageInfo.totalPages) {
      setPageInfo((prev) => ({ ...prev, page: newPage }));
    }
  };

  return (
    <div className="p-8 max-w-7xl mx-auto">
      <h1 className="text-3xl font-bold mb-6">관리자 페이지 - 신고 내역</h1>

      {/* 필터링 UI 추가 */}
      <div className="flex gap-4 mb-4 p-4 bg-base-200 rounded-lg">
        <div className="form-control">
          <label className="label">
            <span className="label-text">상태</span>
          </label>
          <select
            name="status"
            value={filters.status}
            onChange={handleFilterChange}
            className="select select-bordered"
          >
            <option value="">전체</option>
            <option value="PENDING">대기중</option>
            <option value="APPROVED">승인</option>
            <option value="REJECTED">반려</option>
          </select>
        </div>
        <div className="form-control">
          <label className="label">
            <span className="label-text">대상 유형</span>
          </label>
          <select
            name="targetType"
            value={filters.targetType}
            onChange={handleFilterChange}
            className="select select-bordered"
          >
            <option value="">전체</option>
            <option value="POST">게시글</option>
            <option value="COMMENT">댓글</option>
          </select>
        </div>
      </div>

      {/* 신고 내역 테이블 */}
      <div className="overflow-x-auto">
        <table className="table w-full">
          <thead>
            <tr>
              <th>ID</th>
              <th>신고자</th>
              <th>대상 유형</th>
              <th>대상 ID</th>
              <th>신고 사유</th>
              <th>AI 판단 근거</th>
              <th>상태</th>
              <th>신고일</th>
              <th>관리</th>
            </tr>
          </thead>
          <tbody>
            {reports.map((report) => (
              <tr key={report.id}>
                <td>{report.id}</td>
                <td>{report.reporterEmail}</td>
                <td>{report.targetType}</td>
                <td>{report.targetId}</td>
                {/* 신고 사유 셀 수정 */}
                <td>
                  <div className="flex items-center gap-2">
                    <span className="truncate max-w-xs" title={report.reason}>
                      {report.reason}
                    </span>
                    {/* 신고 사유가 길 경우를 대비해 여기도 상세보기 버튼 추가 */}
                    {report.reason && (
                      <button
                        onClick={() => handleOpenDetailsModal(report.reason)}
                        className="btn btn-xs btn-ghost"
                      >
                        보기
                      </button>
                    )}
                  </div>
                </td>
                {/* AI 판단 근거 셀 수정 */}
                <td>
                  <div className="flex items-center gap-2">
                    <span
                      className="truncate max-w-xs text-info"
                      title={report.justification}
                    >
                      {report.justification || "N/A"}
                    </span>
                    {/* AI 판단 근거가 있을 때만 '상세보기' 버튼 표시 */}
                    {report.justification && (
                      <button
                        onClick={() =>
                          handleOpenDetailsModal(report.justification)
                        }
                        className="btn btn-xs btn-info btn-outline"
                      >
                        상세보기
                      </button>
                    )}
                  </div>
                </td>
                <td>
                  <span
                    className={`badge ${
                      report.status === "PENDING"
                        ? "badge-warning"
                        : report.status === "APPROVED"
                        ? "badge-success"
                        : "badge-error"
                    }`}
                  >
                    {report.status}
                  </span>
                </td>
                <td>{new Date(report.createdAt).toLocaleString()}</td>
                <td>
                  {/* 처리 버튼 그룹 (PENDING 상태일 때만 보임) */}
                  {report.status === "PENDING" ? (
                    <div className="flex gap-2">
                      <button
                        onClick={() =>
                          handleProcessReport(report.id, "APPROVED")
                        }
                        className="btn btn-xs btn-success"
                      >
                        승인
                      </button>
                      <button
                        onClick={() =>
                          handleProcessReport(report.id, "REJECTED")
                        }
                        className="btn btn-xs btn-error"
                      >
                        반려
                      </button>
                    </div>
                  ) : (
                    <span>처리 완료</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* 페이지네이션 */}
      <div className="flex justify-center items-center gap-4 mt-8">
        <button
          onClick={() => handlePageChange(pageInfo.page - 1)}
          disabled={pageInfo.page === 0} // 첫 페이지일 때 비활성화
          className="btn"
        >
          이전
        </button>
        <span>
          페이지 {pageInfo.page + 1} / {pageInfo.totalPages}
        </span>
        <button
          onClick={() => handlePageChange(pageInfo.page + 1)}
          disabled={pageInfo.page + 1 >= pageInfo.totalPages} // 마지막 페이지일 때 비활성화
          className="btn"
        >
          다음
        </button>
      </div>
      {/* DetailsModal 컴포넌트 렌더링 */}
      <DetailsModal
        isOpen={isDetailsModalOpen}
        onClose={handleCloseDetailsModal}
        title="상세 내용"
        content={modalContent}
      />
    </div>
  );
}
