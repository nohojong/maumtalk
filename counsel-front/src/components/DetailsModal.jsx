// src/components/DetailsModal.jsx
import { useEffect, useRef } from "react";

export default function DetailsModal({ isOpen, onClose, title, content }) {
  const modalRef = useRef(null);

  useEffect(() => {
    if (isOpen) {
      modalRef.current?.showModal();
    } else {
      modalRef.current?.close();
    }
  }, [isOpen]);

  return (
    <dialog ref={modalRef} className="modal" onCancel={onClose}>
      <div className="modal-box">
        <h3 className="font-bold text-lg mb-4">{title}</h3>
        {/* whitespace-pre-wrap: 공백과 줄바꿈을 유지하면서 자동 줄바꿈을 지원 */}
        <p className="py-4 whitespace-pre-wrap bg-base-200 p-4 rounded-md">
          {content}
        </p>
        <div className="modal-action">
          <button onClick={onClose} className="btn">
            닫기
          </button>
        </div>
      </div>
      {/* 모달 바깥을 클릭해도 닫히도록 form 태그 추가 */}
      <form method="dialog" className="modal-backdrop">
        <button>close</button>
      </form>
    </dialog>
  );
}
