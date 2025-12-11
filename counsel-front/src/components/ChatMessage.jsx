function ChatMessage({ message }) {
  const isUser = message.sender === "user";

  return (
    <div className={`chat ${isUser ? "chat-end" : "chat-start"}`}>
      <div className={`chat-bubble ${isUser ? "chat-bubble-info" : ""}`}>
        {message.text}
      </div>
    </div>
  );
}

export default ChatMessage;
