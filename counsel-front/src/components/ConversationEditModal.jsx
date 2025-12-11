// src/components/ConversationEditModal.jsx
import { useState, useEffect } from "react";

function ConversationEditModal({ isOpen, onClose, conversation, onUpdate }) {
  const [title, setTitle] = useState("");
  const [memo, setMemo] = useState("");

  useEffect(() => {
    if (conversation) {
      setTitle(conversation.title || "");
      setMemo(conversation.memo || "");
    }
  }, [conversation]);

  if (!isOpen) return null;

  const handleUpdate = () => {
    if (!title.trim()) {
      alert("제목을 비워둘 수 없습니다.");
      return;
    }
    onUpdate(conversation.id, { title, memo });
    onClose();
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center">
      <div className="bg-base-100 p-6 rounded-lg shadow-xl w-96">
        <h2 className="text-xl font-bold mb-4">대화 편집</h2>
        <div className="form-control mb-4">
          <label className="label">
            <span className="label-text">제목</span>
          </label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="input input-bordered w-full"
          />
        </div>
        <div className="form-control mb-6">
          <label className="label">
            <span className="label-text">메모</span>
          </label>
          <textarea
            value={memo}
            onChange={(e) => setMemo(e.target.value)}
            className="textarea textarea-bordered h-24 w-full"
          ></textarea>
        </div>
        <div className="flex justify-end gap-2">
          <button className="btn" onClick={onClose}>
            취소
          </button>
          <button className="btn btn-primary" onClick={handleUpdate}>
            저장
          </button>
        </div>
      </div>
    </div>
  );
}

export default ConversationEditModal;
