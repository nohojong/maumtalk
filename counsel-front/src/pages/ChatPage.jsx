import { useState, useEffect, useRef } from "react";
import Navbar from "../components/Navbar";
import Sidebar from "../components/Sidebar";
import ChatMessage from "../components/ChatMessage";
import ChatInput from "../components/ChatInput";
import { VoiceChatIcon } from "../components/Icons";

function ChatPage() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [chatMode, setChatMode] = useState("text"); // 'text', 'voiceInput', 'voiceChat'

  const mainContentRef = useRef(null);

  useEffect(() => {
    if (chatMode !== "voiceChat" && mainContentRef.current) {
      mainContentRef.current.scrollTop = mainContentRef.current.scrollHeight;
    }
  }, [messages, chatMode]);

  const handleSendMessage = () => {
    if (input.trim() === "") return;

    const newUserMessage = {
      id: Date.now(),
      text: input,
      sender: "user",
    };

    setMessages((prev) => [...prev, newUserMessage]);

    setInput("");

    setTimeout(() => {
      const newAiMessage = {
        id: Date.now() + 1,
        text: `'${input}'에 대한 응답입니다. 지금은 정해진 답변만 할 수 있어요.`,
        sender: "ai",
      };
      setMessages((prev) => [...prev, newAiMessage]);

      if (chatMode === "voiceChat") {
        console.log("TTS 재생 (음성 채팅 모드):", newAiMessage.text);
      }
    }, 1000);
  };

  const handleModeSwitch = (mode) => {
    setChatMode(mode);
    if (mode === "voiceChat") {
      setMessages([]);
    }
  };

  const renderChatContent = () => {
    if (chatMode === "voiceChat") {
      return (
        <div className="flex flex-col items-center justify-center h-full text-base-content/70">
          <VoiceChatIcon />
          <p className="mt-4 text-lg">음성 대화 모드가 활성화되었습니다.</p>
          <p>마이크 버튼을 눌러 대화를 시작하세요.</p>
        </div>
      );
    }

    return messages.map((message) => (
      <ChatMessage key={message.id} message={message} />
    ));
  };

  return (
    <div className="drawer lg:drawer-open">
      <input id="my-drawer" type="checkbox" className="drawer-toggle" />
      <div className="drawer-content flex flex-col h-screen">
        <Navbar />
        <main ref={mainContentRef} className="flex-1 overflow-y-auto p-4">
          {renderChatContent()}
        </main>
        <ChatInput
          input={input}
          setInput={setInput}
          chatMode={chatMode}
          handleSendMessage={handleSendMessage}
          handleModeSwitch={handleModeSwitch}
        />
      </div>
      <Sidebar />
    </div>
  );
}

export default ChatPage;
