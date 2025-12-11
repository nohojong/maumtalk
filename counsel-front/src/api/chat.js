// src/api/chat.js
export const streamChat = async (
  conversationId,
  userMessage,
  onMessage,
  onError,
  onComplete
) => {
  try {
    const response = await fetch("/api/conversations/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        conversationId: conversationId,
        message: userMessage,
      }),
      credentials: "include",
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder("utf-8");

    try {
      while (true) {
        const { value, done } = await reader.read();
        if (done) break;

        const chunk = decoder.decode(value, { stream: true });

        // 스트리밍 데이터를 onMessage 콜백으로 전달
        if (onMessage && chunk) {
          onMessage(chunk);
        }
      }

      // 스트리밍 완료 시 onComplete 호출
      if (onComplete) {
        onComplete();
      }
    } catch (streamError) {
      console.error("Stream reading error:", streamError);
      if (onError) {
        onError(streamError);
      }
    } finally {
      reader.releaseLock();
    }
  } catch (error) {
    console.error("Fetch error:", error);
    if (onError) {
      onError(error);
    }
  }
};
