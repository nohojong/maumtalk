import { createBrowserRouter } from "react-router-dom";
import ChatPage from "../pages/ChatPage";
import LoginPage from "../pages/LoginPage";
import SignupPage from "../pages/SignupPage";

const router = createBrowserRouter([
  {
    path: "/",
    element: <ChatPage />,
    // TODO: 로그인이 필요한 페이지 보호 로직 추가
  },
  {
    path: "/login",
    element: <LoginPage />,
  },
  {
    path: "/signup",
    element: <SignupPage />,
  },
]);

export default router;
