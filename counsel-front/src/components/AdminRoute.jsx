// src/components/AdminRoute.jsx (새 파일)
import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";

export default function AdminRoute() {
  const { user, isLoading } = useAuth();

  // 사용자 정보를 로딩 중일 때는 잠시 기다립니다.
  if (isLoading) {
    return <div>권한 확인 중...</div>;
  }

  // 로딩이 끝났고, 유저 정보가 있거나 유저의 role이 'ADMIN'이면
  // 자식 컴포넌트(AdminPage)를 보여줍니다.
  // 중요: 백엔드의 User 객체에 role 필드가 있고, AuthContext의 user 상태에 포함되어야 합니다.
  if (user && user.role === "ADMIN") {
    return <Outlet />;
  }

  // 관리자가 아니면 홈으로 리디렉션합니다.
  alert("접근 권한이 없습니다.");
  return <Navigate to="/" replace />;
}
