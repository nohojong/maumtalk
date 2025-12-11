function ChatMessage({ message }) {
  const isUser = message.sender?.toLowerCase() === "user";

  return (
    <div className={`chat ${isUser ? "chat-end" : "chat-start"}`}>
      <div className={`chat-bubble ${isUser ? "chat-bubble-info" : ""}`}>
        {message.message}
      </div>
    </div>
  );
}

export default ChatMessage;
