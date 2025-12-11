import { useRef, useEffect } from "react";
import { MicIcon, VoiceChatIcon, SendIcon } from "./Icons";

function ChatInput({
  input,
  setInput,
  chatMode,
  handleSendMessage,
  handleModeSwitch,
}) {
  const textareaRef = useRef(null);

  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = "auto";
      textareaRef.current.style.height = `${textareaRef.current.scrollHeight}px`;
    }
  }, [input]);

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  return (
    <div className="p-4 bg-base-100">
      <div className="flex items-end gap-2 bg-base-100 rounded-2xl p-2 border border-base-content/30">
        <textarea
          ref={textareaRef}
          rows="1"
          className="bg-transparent w-full focus:outline-none focus:ring-0 border-none resize-none overflow-y-auto max-h-32 py-2 px-3"
          placeholder="메시지를 입력하세요..."
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          readOnly={chatMode !== "text"}
        />
        {input.length > 0 ? (
          <button
            className="btn btn-info btn-circle"
            onClick={handleSendMessage}
          >
            <SendIcon />
          </button>
        ) : (
          <>
            <button
              className={`btn btn-ghost btn-circle ${
                chatMode === "voiceInput" ? "text-primary" : ""
              }`}
              onClick={() =>
                handleModeSwitch(
                  chatMode === "voiceInput" ? "text" : "voiceInput"
                )
              }
            >
              <MicIcon />
            </button>
            <button
              className={`btn btn-ghost btn-circle ${
                chatMode === "voiceChat" ? "text-primary" : ""
              }`}
              onClick={() =>
                handleModeSwitch(
                  chatMode === "voiceChat" ? "text" : "voiceChat"
                )
              }
            >
              <VoiceChatIcon />
            </button>
          </>
        )}
      </div>
    </div>
  );
}

export default ChatInput;
