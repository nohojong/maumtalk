import { createContext, useState, useContext, useEffect } from "react";
import { useNavigate } from "react-router-dom";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [accessToken, setAccessToken] = useState(
    localStorage.getItem("accessToken")
  );
  const navigate = useNavigate();

  const isLoggedIn = !!accessToken;

  useEffect(() => {
    if (accessToken) {
      localStorage.setItem("accessToken", accessToken);
    } else {
      localStorage.removeItem("accessToken");
    }
  }, [accessToken]);

  const login = (token) => {
    setAccessToken(token);
    navigate("/");
  };

  const logout = () => {
    setAccessToken(null);
    navigate("login");
  };

  const value = { isLoggedIn, accessToken, login, logout };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
