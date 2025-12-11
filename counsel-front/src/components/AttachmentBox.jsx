// src/components/AttachmentBox.jsx
import React from "react";

// Props:
// - existingAttachments: 기존 첨부 파일 URL 배열
// - onNewFilesChange: (files) => void, 새 파일이 선택될 때 호출될 콜백
// - onDeletedUrlsChange: (urls) => void, 삭제할 파일 URL 목록이 변경될 때 호출될 콜백
// - canEdit: boolean, 수정 가능 여부
export default function AttachmentBox({
  existingAttachments = [],
  onNewFilesChange,
  onDeletedUrlsChange,
  canEdit = false,
}) {
  // 삭제할 파일 목록 상태는 이 컴포넌트가 직접 관리
  const [deletedUrls, setDeletedUrls] = React.useState([]);

  // 기존 파일 삭제 처리
  const handleDeleteAttachment = (urlToDelete) => {
    const newDeletedUrls = [...deletedUrls, urlToDelete];
    setDeletedUrls(newDeletedUrls);
    // 변경된 삭제 목록을 부모에게 알림
    onDeletedUrlsChange(newDeletedUrls);
  };

  // 새 파일 선택 처리
  const handleFileChange = (e) => {
    const files = Array.from(e.target.files);
    // 선택된 파일 목록을 부모에게 알림
    onNewFilesChange(files);
  };

  // 화면에 보여줄 파일 목록 (삭제된 파일은 제외)
  const attachmentsToShow = existingAttachments.filter(
    (url) => !deletedUrls.includes(url)
  );

  return (
    <div>
      <label className="label">첨부파일</label>

      {/* 기존 파일 목록 */}
      <div className="mb-2 space-y-1">
        {attachmentsToShow.map((url) => (
          <div key={url} className="flex items-center gap-2">
            <a
              href={url}
              target="_blank"
              rel="noopener noreferrer"
              className="link link-primary text-sm truncate"
            >
              {/* 파일 이름만 예쁘게 표시 */}
              {decodeURIComponent(url.split("/").pop())}
            </a>
            {canEdit && (
              <button
                type="button"
                onClick={() => handleDeleteAttachment(url)}
                className="btn btn-xs btn-outline btn-error"
              >
                삭제
              </button>
            )}
          </div>
        ))}
      </div>

      {/* 새 파일 선택 UI (수정 가능할 때만 보임) */}
      {canEdit && (
        <input
          type="file"
          multiple
          onChange={handleFileChange}
          className="file-input file-input-bordered w-full"
        />
      )}
    </div>
  );
}
