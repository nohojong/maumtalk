// // src/components/Sidebar.jsx
// import { Link, useNavigate } from "react-router-dom";
// import { useState, useEffect } from "react";
// import { useAuth } from "../contexts/AuthContext";
// import ConversationEditModal from "./ConversationEditModal"; // 모달 컴포넌트 임포트

// function Sidebar({
//   userId,
//   conversations,
//   activeConversationId,
//   // editingId,
//   onNewConversation,
//   onSelectConversation,
//   onDeleteConversation,
//   onUpdateConversation,
//   onCloseSidebar,
// }) {
//   const { isLoggedIn } = useAuth();
//   const navigate = useNavigate();

//   const [editingConversation, setEditingConversation] = useState(null);

//   const themes = ["light", "dark"];
//   const [theme, setTheme] = useState(
//     localStorage.getItem("theme") || themes[0]
//   );

//   const toggleTheme = () => {
//     const nextTheme = theme === "light" ? "dark" : "light";
//     setTheme(nextTheme);
//     localStorage.setItem("theme", nextTheme);
//     document.documentElement.setAttribute("data-theme", nextTheme);
//   };

//   // useEffect(() => {
//   //   if (editingId) {
//   //     const conversationToEdit = conversations.find((c) => c.id === editingId);
//   //     if (conversationToEdit) {
//   //       setEditedTitle(conversationToEdit.title || "");
//   //     }
//   //   }
//   // }, [editingId, conversations]);

//   useEffect(() => {
//     document.documentElement.setAttribute("data-theme", theme);
//   }, [theme]);

//   const handleClose = () => {
//     const drawer = document.getElementById("my-drawer");
//     if (drawer) drawer.checked = false;
//     if (onCloseSidebar) onCloseSidebar();
//   };

//   const handleOpenEditModal = (conversation) => {
//     setEditingConversation(conversation);
//   };

//   const handleCloseEditModal = () => {
//     setEditingConversation(null);
//   };

//   return (
//     <div className="drawer-side border-r w-64 bg-base-100 flex flex-col justify-between">
//       <div className="menu p-4 overflow-y-auto w-64">
//         {/* 대화 목록 헤더 */}
//         <div className="flex justify-between items-center mb-2">
//           <h2 className="text-lg font-bold">대화 목록</h2>
//           {/* 화면이 lg 이상이면 X 버튼 숨김 */}
//           <button
//             className="btn btn-xs btn-ghost text-black hover:bg-gray-200 lg:hidden"
//             onClick={handleClose}
//           >
//             ✖
//           </button>
//         </div>

//         {/* 새 대화 버튼 */}
//         <button
//           className="btn btn-sm border-none w-full mb-2 flex items-center justify-start"
//           onClick={onNewConversation}
//         >
//           <span className="text-base mr-2">💬</span> 새 대화
//         </button>

//         {/* 대화 목록 */}
//         {Array.isArray(conversations) &&
//           conversations.map((c) => (
//             <div
//               key={c.id}
//               className={`group flex w-full items-center justify-between mb-0 rounded-lg hover:bg-base-200 ${
//                 activeConversationId === c.id ? "bg-base-200 font-bold" : ""
//               }`}
//             >
//               <div
//                 className="flex-1 cursor-pointer truncate p-2"
//                 title={c.title}
//                 onClick={() => onSelectConversation(c.id)}
//               >
//                 {c.title || "새로운 고민 상담"}
//               </div>
//               <button
//                 className="btn btn-xs btn-ghost"
//                 onClick={() => handleOpenEditModal(c)}
//               >
//                 ✏️
//               </button>
//               <button
//                 className="btn btn-xs btn-ghost"
//                 onClick={() => onDeleteConversation(c.id)}
//               >
//                 🗑
//               </button>
//             </div>
//           ))}
//       </div>
//       {/* 대화 편집 모달 */}
//       <ConversationEditModal
//         isOpen={!!editingConversation}
//         onClose={handleCloseEditModal}
//         conversation={editingConversation}
//         onUpdate={onUpdateConversation}
//       />

//       {/* 하단: 게시판 + 마이페이지 + 테마 */}
//       <div className="p-4 w-64 flex flex-col gap-2">
//         <button
//           className="btn btn-ghost w-full text-left justify-start"
//           onClick={() => {
//             if (isLoggedIn) navigate("/board");
//             else {
//               alert("로그인 후 이용 가능합니다.");
//               navigate("/login");
//             }
//             handleClose();
//           }}
//         >
//           게시판
//         </button>

//         <div className="dropdown dropdown-end dropdown-top w-full relative">
//           <label
//             tabIndex={0}
//             className="btn btn-ghost w-full flex justify-between items-center"
//           >
//             <span>마이 페이지</span>
//             <span className="text-lg">⋯</span>
//           </label>
//           <ul
//             tabIndex={0}
//             className="dropdown-content menu p-2 shadow bg-base-100 rounded-box absolute right-0 mb-2 min-w-max text-right"
//           >
//             <li>
//               <Link to={`/user/${userId}/profile`} onClick={handleClose}>
//                 프로필
//               </Link>
//             </li>
//             <li>
//               <Link to="/change-password" onClick={handleClose}>
//                 비밀번호 변경
//               </Link>
//             </li>
//             <li>
//               <Link to="/settings" onClick={handleClose}>
//                 설정
//               </Link>
//             </li>
//             <li>
//               <button onClick={toggleTheme} className="w-full text-left">
//                 {theme === "light" ? "🌞 Light" : "🌙 Dark"}
//               </button>
//             </li>
//           </ul>
//         </div>
//       </div>
//     </div>
//   );
// }

// export default Sidebar;
import { Link, useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import { useAuth } from "../contexts/AuthContext";
import ConversationEditModal from "./ConversationEditModal";

function Sidebar({
  conversations,
  activeConversationId,
  onNewConversation,
  onSelectConversation,
  onDeleteConversation,
  onUpdateConversation,
  onCloseSidebar,
}) {
  const { isLoggedIn, user } = useAuth();
  const userId = user?.id;
  const navigate = useNavigate();

  const [editingConversation, setEditingConversation] = useState(null);
  const themes = ["light", "dark"];
  const [theme, setTheme] = useState(localStorage.getItem("theme") || themes[0]);

  const toggleTheme = () => {
    const nextTheme = theme === "light" ? "dark" : "light";
    setTheme(nextTheme);
    localStorage.setItem("theme", nextTheme);
    document.documentElement.setAttribute("data-theme", nextTheme);
  };

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
  }, [theme]);

  const handleClose = () => {
    const drawer = document.getElementById("my-drawer");
    if (drawer) drawer.checked = false;
    if (onCloseSidebar) onCloseSidebar();
  };

  const handleOpenEditModal = (conversation) => setEditingConversation(conversation);
  const handleCloseEditModal = () => setEditingConversation(null);

  return (
    <div className="drawer-side border-r w-64 bg-base-100 flex flex-col justify-between">
      <div className="menu p-4 overflow-y-auto w-64">
        <div className="flex justify-between items-center mb-2">
          <h2 className="text-lg font-bold">대화 목록</h2>
          <button
            className="btn btn-xs btn-ghost text-black hover:bg-gray-200 lg:hidden"
            onClick={handleClose}
          >
            ✖
          </button>
        </div>

        <button
          className="btn btn-sm border-none w-full mb-2 flex items-center justify-start"
          onClick={onNewConversation}
        >
          <span className="text-base mr-2">💬</span> 새 대화
        </button>

        {Array.isArray(conversations) &&
          conversations.map((c) => (
            <div
              key={c.id}
              className={`group flex w-full items-center justify-between mb-0 rounded-lg hover:bg-base-200 ${
                activeConversationId === c.id ? "bg-base-200 font-bold" : ""
              }`}
            >
              <div
                className="flex-1 cursor-pointer truncate p-2"
                title={c.title}
                onClick={() => onSelectConversation(c.id)}
              >
                {c.title || "새로운 고민 상담"}
              </div>
              <button
                className="btn btn-xs btn-ghost"
                onClick={() => handleOpenEditModal(c)}
              >
                ✏️
              </button>
              <button
                className="btn btn-xs btn-ghost"
                onClick={() => onDeleteConversation(c.id)}
              >
                🗑
              </button>
            </div>
          ))}
      </div>

      <ConversationEditModal
        isOpen={!!editingConversation}
        onClose={handleCloseEditModal}
        conversation={editingConversation}
        onUpdate={onUpdateConversation}
      />

      <div className="p-4 w-64 flex flex-col gap-2">
        <button
          className="btn btn-ghost w-full text-left justify-start"
          onClick={() => {
            if (isLoggedIn) navigate("/board");
            else {
              alert("로그인 후 이용 가능합니다.");
              navigate("/login");
            }
            handleClose();
          }}
        >
          게시판
        </button>

        <div className="dropdown dropdown-end dropdown-top w-full relative">
          <label
            tabIndex={0}
            className="btn btn-ghost w-full flex justify-between items-center"
          >
            <span>마이 페이지</span>
            <span className="text-lg">⋯</span>
          </label>
          <ul
            tabIndex={0}
            className="dropdown-content menu p-2 shadow bg-base-100 rounded-box absolute right-0 mb-2 min-w-max text-right"
          >
            <li>
              <Link to={`/user/${userId}/profile`} onClick={handleClose}>
                프로필
              </Link>
            </li>
            <li>
              <Link to="/change-password" onClick={handleClose}>
                비밀번호 변경
              </Link>
            </li>
            <li>
              <Link to="/settings" onClick={handleClose}>
                설정
              </Link>
            </li>
            <li>
              <button onClick={toggleTheme} className="w-full text-left">
                {theme === "light" ? "🌞 Light" : "🌙 Dark"}
              </button>
            </li>
          </ul>
        </div>
      </div>
    </div>
  );
}

export default Sidebar;
