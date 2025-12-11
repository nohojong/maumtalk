// src/contexts/AuthContext.jsx
import { createContext, useState, useContext, useEffect } from "react";
import { checkAuthStatus, logout as logoutApi } from "../api/auth";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchUser = async () => {
      try {
        //  /api/auth/me 호출
        const response = await checkAuthStatus();
        // API 응답 데이터(사용자 객체)를 user 상태에 저장
        setUser(response.data.data);
      } catch (error) {
        console.log("사용자 정보를 가져오는데 실패했습니다.", error);
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };
    fetchUser();
  }, []);

  // LoginPage에서 호출될 login 함수.
  // 로그인 성공 후 받은 사용자 데이터를 인자로 받아 상태를 업데이트합니다.
  const login = (userData) => {
    setUser(userData);
  };

  const logout = async () => {
    try {
      await logoutApi();
      window.location.reload(); // 페이지를 새로고침하여 로그인 상태를 반영합니다.
    } catch (error) {
      console.log("로그 아웃 실패", error);
    } finally {
      // 상태를 null로 초기화
      setUser(null);
    }
  };

  // value에 user 객체와 isLoggedIn 불리언을 함께 제공
  const value = {
    user,
    login,
    logout,
    isLoading,
    isLoggedIn: !!user, // user 객체가 있으면 true, null이면 false
  };

  // 초기 로딩 중에는 로딩 화면을 표시
  if (isLoading) {
    return <div>Loading user...</div>;
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within a AuthProvider");
  }
  return context;
}
