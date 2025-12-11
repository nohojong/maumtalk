import { useEffect } from "react";
import { useAuth } from "../contexts/AuthContext";
import { useNavigate } from "react-router-dom";

export default function OAuthCallback() {
  const { login } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const code = params.get("code");

    if (code) {
      // 서버에 코드 전달 → accessToken 발급
      fetch(`/api/oauth/callback?code=${code}`)
        .then((res) => res.json())
        .then((data) => {
          login(data.accessToken); // Context에 저장
          navigate("/board");      // 게시판 페이지로 이동
        })
        .catch((err) => {
          console.error("OAuth 처리 실패", err);
          navigate("/"); // 실패 시 로그인 페이지로 이동
        });
    }
  }, [login, navigate]);

  return <div>로그인 처리 중...</div>;
}
