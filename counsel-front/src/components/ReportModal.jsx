// src/components/ReportModal.jsx
import { useState, useEffect, useRef } from "react";

// props: isOpen(boolean), onClose(function), onSubmit(function)
export default function ReportModal({ isOpen, onClose, onSubmit }) {
  const [reason, setReason] = useState("");
  const modalRef = useRef(null);

  // isOpen prop이 변경될 때마다 모달을 열거나 닫도록 처리
  useEffect(() => {
    if (isOpen) {
      modalRef.current?.showModal();
    } else {
      modalRef.current?.close();
    }
  }, [isOpen]);

  const handleSubmit = (e) => {
    e.preventDefault();
    // 부모로부터 받은 onSubmit 함수에 현재 state인 reason을 담아 호출
    onSubmit(reason);
  };

  const handleClose = () => {
    // 부모로부터 받은 onClose 함수를 호출하여 닫기 상태를 동기화
    onClose();
  };

  return (
    <dialog ref={modalRef} className="modal" onCancel={handleClose}>
      <div className="modal-box">
        <h3 className="font-bold text-lg">댓글 신고하기</h3>
        <form onSubmit={handleSubmit}>
          <div className="py-4">
            <label htmlFor="reportReason" className="label">
              <span className="label-text">신고 사유</span>
            </label>
            <textarea
              id="reportReason"
              className="textarea textarea-bordered w-full"
              placeholder="신고 사유를 자세히 입력해주세요."
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              required
            ></textarea>
          </div>
          <div className="modal-action">
            <button type="submit" className="btn btn-error">
              신고하기
            </button>
            <button type="button" onClick={handleClose} className="btn">
              취소
            </button>
          </div>
        </form>
      </div>
    </dialog>
  );
}
