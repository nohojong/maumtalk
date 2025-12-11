// src/components/CommentsBox.jsx
import { useState, useEffect, useRef } from "react";
import { useAuth } from "../contexts/AuthContext";
import {
  getCommentsByPostId,
  createComment,
  updateComment,
  deleteComment,
  reportComment,
} from "../api/comment";
import ReportModal from "./ReportModal";

export default function CommentsBox({ postId }) {
  const { user } = useAuth(); // 현재 로그인된 사용자 정보
  useEffect(() => {
    console.log("CommentsBox User State:", user);
  }, [user]);

  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState("");
  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editingContent, setEditingContent] = useState("");

  // 신고 모달 관련 state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [reportingCommentId, setReportingCommentId] = useState(null);

  const isProcessingReport = useRef(false);

  useEffect(() => {
    fetchComments();
  }, [postId]);

  // 댓글 목록을 불러오는 함수
  const fetchComments = async () => {
    try {
      const res = await getCommentsByPostId(postId);
      setComments(res.data || []);
    } catch (error) {
      console.error("댓글 조회 실패", error);
    }
  };

  // 새 댓글 등록 처리
  const handleCommentSubmit = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;
    try {
      await createComment(postId, { content: newComment });
      setNewComment("");
      fetchComments(); // 댓글 목록 새로고침
    } catch (error) {
      console.error("댓글 작성 실패", error);
      alert("댓글 작성에 실패했습니다.");
    }
  };

  // 댓글 삭제 처리
  const handleDeleteComment = async (commentId) => {
    if (window.confirm("정말 이 댓글을 삭제하시겠습니까?")) {
      try {
        await deleteComment(commentId);
        fetchComments(); // 댓글 목록 새로고침
      } catch (error) {
        console.error("댓글 삭제 실패", error);
        alert("댓글 삭제에 실패했습니다.");
      }
    }
  };

  // 댓글 수정 모드로 전환
  const handleEditMode = (comment) => {
    setEditingCommentId(comment.id);
    setEditingContent(comment.content);
  };

  // 댓글 수정 내용 저장
  const handleUpdateComment = async (e) => {
    e.preventDefault();
    if (!editingContent.trim()) return;
    try {
      await updateComment(editingCommentId, { content: editingContent });
      setEditingCommentId(null);
      setEditingContent("");
      fetchComments(); // 댓글 목록 새로고침
    } catch (error) {
      console.error("댓글 수정 실패", error);
      alert("댓글 수정에 실패했습니다.");
    }
  };

  // 모달을 여는 함수
  const handleOpenModal = (commentId) => {
    setReportingCommentId(commentId);
    setIsModalOpen(true);
  };

  // 모달을 닫는 함수
  const handleCloseModal = () => {
    setIsModalOpen(false);
    setReportingCommentId(null);
  };

  // 신고 제출 처리 함수 (ReportModal로부터 reason을 인자로 받음)
  const handleSubmitReport = async (reason) => {
    // 중복 요청시 즉시 종료
    if (isProcessingReport.current) {
      return;
    }

    if (!reason || !reason.trim()) {
      alert("신고 사유를 입력해야 합니다.");
      return;
    }

    try {
      isProcessingReport.current = true;
      await reportComment(reportingCommentId, reason);
      alert("댓글이 성공적으로 신고되었습니다.");
      handleCloseModal(); // 성공 시 모달 닫기
    } catch (error) {
      console.error("댓글 신고 실패", error);
      const errorMessage =
        error.response?.data?.message || "댓글 신고에 실패했습니다.";
      alert(errorMessage);
    } finally {
      isProcessingReport.current = false;
    }
  };

  return (
    <div>
      <h2 className="text-2xl font-bold mb-4">댓글</h2>
      {/* 댓글 목록 */}
      <div className="space-y-3 mb-6">
        {comments.map((comment) => (
          <div key={comment.id} className="p-3 border rounded bg-base-200">
            {editingCommentId === comment.id ? (
              // 수정 모드일 때
              <form onSubmit={handleUpdateComment} className="flex gap-2">
                <input
                  type="text"
                  value={editingContent}
                  onChange={(e) => setEditingContent(e.target.value)}
                  className="input input-bordered flex-grow"
                />
                <button type="submit" className="btn btn-primary">
                  저장
                </button>
                <button
                  type="button"
                  onClick={() => setEditingCommentId(null)}
                  className="btn"
                >
                  취소
                </button>
              </form>
            ) : (
              <div>
                <p className="font-semibold">{comment.writerEmail}</p>
                {/* blinded 상태에 따라 댓글 내용 분기 처리 */}
                {comment.blinded ? (
                  <p className="whitespace-pre-wrap italic">
                    {comment.content}
                  </p>
                ) : (
                  <p className="whitespace-pre-wrap">{comment.content}</p>
                )}
                <div className="flex justify-between items-center mt-2">
                  <p className="text-xs text-gray-500">
                    {new Date(comment.createdAt).toLocaleString()}
                  </p>
                  {/* 버튼 컨테이너 */}
                  <div className="flex gap-2">
                    {!comment.blinded &&
                      user &&
                      user.email === comment.writerEmail && (
                        <>
                          <button
                            onClick={() => handleEditMode(comment)}
                            className="btn btn-xs"
                          >
                            수정
                          </button>
                          <button
                            onClick={() => handleDeleteComment(comment.id)}
                            className="btn btn-xs btn-error"
                          >
                            삭제
                          </button>
                        </>
                      )}
                    {/* 로그인 유저가 타인의 댓글을 볼 때 신고 버튼 표시 */}
                    {!comment.blinded &&
                      user &&
                      user.email !== comment.writerEmail && (
                        <button
                          onClick={() => handleOpenModal(comment.id)}
                          className="btn btn-xs btn-warning"
                        >
                          신고
                        </button>
                      )}
                  </div>
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
      {/* 댓글 작성 폼 (로그인한 사용자에게만 보임) */}
      {user && (
        <form onSubmit={handleCommentSubmit} className="flex gap-2">
          <input
            type="text"
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            placeholder="댓글을 입력하세요"
            className="input input-bordered flex-grow"
          />
          <button type="submit" className="btn btn-secondary">
            등록
          </button>
        </form>
      )}
      {/* 분리된 ReportModal 컴포넌트 렌더링 */}
      <ReportModal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        onSubmit={handleSubmitReport}
      />
    </div>
  );
}
