import { Routes, Route } from "react-router-dom";
import ChatPage from "./pages/ChatPage.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import SignupPage from "./pages/SignupPage.jsx";
import BoardPage from "./pages/BoardPage.jsx";
import PostDetailPage from "./pages/PostDetailPage.jsx";
import UserProfilePage from "./pages/UserProfilePage.jsx";
import UserDetailPage from "./pages/UserDetailPage.jsx";
import AdminPage from "./pages/AdminPage.jsx";
import AdminRoute from "./components/AdminRoute.jsx";

function App() {
  return (
    <Routes>
      <Route path="/" element={<ChatPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route path="/board" element={<BoardPage />} />
      <Route path="/board/:postId" element={<PostDetailPage />} />
      {/* 추가된 User 관련 페이지 */}
      <Route path="/admin" element={<AdminRoute />}>
        <Route index element={<AdminPage />} />
      </Route>
      <Route path="/user/:userId/profile" element={<UserProfilePage />} />
      <Route path="/user/:userId/detail" element={<UserDetailPage />} />
    </Routes>
  );
}

export default App;
