// src/pages/ChatPage.jsx
import { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import Navbar from "../components/Navbar";
import Sidebar from "../components/Sidebar";
import ChatMessage from "../components/ChatMessage";
import ChatInput from "../components/ChatInput";
import { streamChat } from "../api/chat";
import {
  updateConversation,
  deleteConversation,
  fetchConversations,
  fetchConversationDetail,
} from "../api/conversation";
import { checkAccessStatus, updateAccessStatus } from "../api/payment";

function ChatPage() {
  const { isLoggedIn, isLoading } = useAuth();
  const navigate = useNavigate();
  const mainRef = useRef(null);
  const tempMessagesRef = useRef([]);

  const [conversations, setConversations] = useState([]);
  const [tempMessages, setTempMessages] = useState([]);
  const [activeConversationId, setActiveConversationId] = useState(null);
  const [editingConversationId, setEditingConversationId] = useState(null);
  const [input, setInput] = useState("");
  const [chatMode, setChatMode] = useState("text");
  const [isSending, setIsSending] = useState(false);
  const [accessUntil, setAccessUntil] = useState(null);

  const activeConversation = conversations.find(
    (c) => c.id === activeConversationId
  );
  const displayMessages = activeConversationId
    ? activeConversation?.messages
    : tempMessages;

  // --- 로그인 및 초기 데이터 로드 ---
  useEffect(() => {
    if (isLoading) return;
    if (!isLoggedIn) return navigate("/login");

    const init = async () => {
      try {
        // 대화 목록
        const convResp = await fetchConversations();
        setConversations(convResp.data);

        // 이용권 확인
        const accessResp = await checkAccessStatus();
        setAccessUntil(new Date(accessResp.data.accessUntil));
      } catch (e) {
        console.error("초기 로딩 실패:", e);
      }
    };
    init();
  }, [isLoggedIn, isLoading, navigate]);

  useEffect(() => {
    tempMessagesRef.current = tempMessages;
  }, [tempMessages]);
  useEffect(() => {
    if (mainRef.current)
      mainRef.current.scrollTop = mainRef.current.scrollHeight;
  }, [displayMessages]);

  // --- 결제 ---
  const requestPay = () => {
    const { IMP } = window;
    IMP.init("imp04144282");

    IMP.request_pay(
      {
        pg: "kakaopay.TC0ONETIME",
        pay_method: "card",
        merchant_uid: `mid_${Date.now()}`,
        name: "한 달 이용권",
        amount: 100,
        buyer_email: "test@example.com",
        buyer_name: "홍길동",
        buyer_tel: "010-1234-5678",
      },
      async (rsp) => {
        if (!rsp.success) return alert(`결제 실패: ${rsp.error_msg}`);

        try {
          await updateAccessStatus({
            imp_uid: rsp.imp_uid,
            merchant_uid: rsp.merchant_uid,
          });
          const accessResp = await checkAccessStatus();
          const newAccessUntil = new Date(accessResp.data.accessUntil);
          setAccessUntil(newAccessUntil);
          alert("결제 완료! 한 달 이용권이 갱신되었습니다.");
        } catch (e) {
          console.error("결제 검증 실패:", e);
          alert("결제는 성공했지만 이용권 갱신에 실패했습니다.");
        }
      }
    );
  };

  // --- 새 대화 생성 ---
  const handleNewConversation = () => {
    setActiveConversationId(null);
    setTempMessages([]);
  };

  // --- 대화 선택 ---
  const handleSelectConversation = async (id) => {
    if (editingConversationId === id) return;
    setActiveConversationId(id);
    setTempMessages([]);

    const selectedConv = conversations.find((c) => c.id === id);
    if (!selectedConv || !selectedConv.messages) {
      try {
        const resp = await fetchConversationDetail(id);
        const detail = resp.data;
        setConversations((prev) =>
          prev.map((c) => (c.id === id ? { ...c, ...detail } : c))
        );
      } catch (e) {
        console.error("대화 상세 정보 로딩 실패:", e);
      }
    }
  };

  const handleUpdateConversation = async (id, data) => {
    try {
      const res = await updateConversation(id, data);
      setConversations((prev) =>
        prev.map((c) => (c.id === id ? { ...c, ...res.data } : c))
      );
    } catch (e) {
      console.error(e);
    }
  };

  // --- 메시지 전송 ---
  const handleSendMessage = async () => {
    if (!input.trim() || !isLoggedIn || isSending) return;

    const now = new Date();
    if (!accessUntil || now > accessUntil) {
      alert("이용권이 만료되었습니다. 결제 후 이용해주세요.");
      return requestPay();
    }

    const text = input.trim();
    setInput("");
    setIsSending(true);

    const isNewConv = !activeConversationId;
    const userMsg = { id: Date.now(), message: text, sender: "user" };
    const aiMsgId = Date.now() + 1;
    const aiMsg = { id: aiMsgId, message: "...", sender: "ai" };

    if (isNewConv) setTempMessages((prev) => [...prev, userMsg, aiMsg]);
    else
      setConversations((prev) =>
        prev.map((c) =>
          c.id === activeConversationId
            ? { ...c, messages: [...(c.messages || []), userMsg, aiMsg] }
            : c
        )
      );

    streamChat(
      activeConversationId,
      text,
      (token) => {
        const updater = (messages) => {
          const last = messages[messages.length - 1];
          if (last?.id === aiMsgId) {
            return [
              ...messages.slice(0, -1),
              {
                ...last,
                message: last.message === "..." ? token : last.message + token,
              },
            ];
          }
          return messages;
        };
        isNewConv
          ? setTempMessages(updater)
          : setConversations((prev) =>
              prev.map((c) =>
                c.id === activeConversationId
                  ? { ...c, messages: updater(c.messages) }
                  : c
              )
            );
      },
      () => setIsSending(false),
      async () => {
        setIsSending(false);
        if (!isNewConv) return;
        try {
          const { data: list } = await fetchConversations();
          setConversations((prev) => [
            { ...list[0], messages: tempMessagesRef.current },
            ...prev,
          ]);
          setActiveConversationId(list[0].id);
          setTempMessages([]);
        } catch (e) {
          console.error(e);
        }
      }
    );
  };

  return (
    <div className="drawer lg:drawer-open">
      <input id="my-drawer" type="checkbox" className="drawer-toggle" />
      <div className="drawer-content flex flex-col h-screen">
        <Navbar />
        <main ref={mainRef} className="flex-1 overflow-y-auto p-4">
          {!accessUntil || new Date() > accessUntil ? (
            <div className="flex flex-col items-center justify-center h-full text-gray-400">
              <p className="text-lg mb-4">한 달 이용권이 필요합니다.</p>
              <button onClick={requestPay} className="btn btn-primary">
                결제하기
              </button>
            </div>
          ) : displayMessages?.length ? (
            displayMessages.map((m) => <ChatMessage key={m.id} message={m} />)
          ) : (
            <div className="flex items-center justify-center h-screen">
              <p className="text-gray-400 text-center">
                새로운 대화를 시작해보세요 ✨
              </p>
            </div>
          )}
        </main>
        <ChatInput
          input={input}
          setInput={setInput}
          chatMode={chatMode}
          handleSendMessage={handleSendMessage}
          handleModeSwitch={setChatMode}
        />
      </div>
      <Sidebar
        conversations={conversations}
        activeConversationId={activeConversationId}
        onNewConversation={handleNewConversation}
        onSelectConversation={handleSelectConversation}
        onDeleteConversation={async (id) => {
          if (!confirm("삭제하시겠습니까?")) return;
          try {
            await deleteConversation(id);
            setConversations((prev) => prev.filter((c) => c.id !== id));
            if (activeConversationId === id) setActiveConversationId(null);
          } catch (e) {
            console.error(e);
          }
        }}
        onUpdateConversation={handleUpdateConversation}
        editingId={editingConversationId}
        onStartEdit={setEditingConversationId}
        onCancelEdit={() => setEditingConversationId(null)}
      />
    </div>
  );
}

export default ChatPage;
