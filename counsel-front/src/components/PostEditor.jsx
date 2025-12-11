// src/components/PostEditor.jsx
import { useState } from "react";
import AttachmentBox from "./AttachmentBox"; // AttachmentBox는 그대로 사용

// Props:
// - initialPost: { title, content, attachments } 초기 데이터
// - onSave: "저장" 버튼 클릭 시 호출될 함수. (editedPost, newFiles, deletedUrls) 전달
// - onCancel: "취소" 버튼 클릭 시 호출될 함수
// - isNewPost: 새 글 작성 여부
export default function PostEditor({
  initialPost,
  onSave,
  onCancel,
  isNewPost,
}) {
  // Editor는 자신만의 내부 상태를 가짐 (부모로부터 초기값만 받음)
  const [title, setTitle] = useState(initialPost.title);
  const [content, setContent] = useState(initialPost.content);
  const [newFiles, setNewFiles] = useState([]);
  const [deletedUrls, setDeletedUrls] = useState([]);

  const handleSubmit = (e) => {
    e.preventDefault();
    onSave({ title, content }, newFiles, deletedUrls);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="label">제목</label>
        <input
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          className="input input-bordered w-full"
          required
        />
      </div>
      <div>
        <label className="label">내용</label>
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          className="textarea textarea-bordered w-full h-40"
          required
        />
      </div>

      <AttachmentBox
        existingAttachments={initialPost.attachments}
        onNewFilesChange={setNewFiles}
        onDeletedUrlsChange={setDeletedUrls}
        canEdit={true} // Editor 모드에서는 항상 수정 가능
      />

      <div className="flex gap-2 pt-4">
        <button type="submit" className="btn btn-primary">
          {isNewPost ? "작성 완료" : "수정 완료"}
        </button>
        {/* 새 글 작성 시에는 취소 버튼이 목록으로 돌아가는 역할을 함 */}
        <button type="button" onClick={onCancel} className="btn">
          {isNewPost ? "목록으로" : "취소"}
        </button>
      </div>
    </form>
  );
}
