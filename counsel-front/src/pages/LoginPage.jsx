// src/pages/LoginPage.jsx
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
// checkAuthStatus API 함수를 추가로 임포트합니다.
import { login as loginApi, checkAuthStatus } from "../api/auth";
import { useAuth } from "../contexts/AuthContext";
import { FcGoogle } from "react-icons/fc";
import { SiNaver } from "react-icons/si";

function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      // 1. 로그인 API 호출 (성공 시 서버가 HttpOnly 쿠키를 설정해 줌)
      await loginApi(email, password);

      // 2. 로그인 성공 직후, 방금 받은 쿠키를 이용해 사용자 정보 조회 API(/auth/me)를 바로 호출
      const response = await checkAuthStatus();
      const userData = response.data.data; // ex: { id: 1, email: 'user@test.com' }

      // 3. 받아온 사용자 정보로 AuthContext의 전역 상태를 업데이트
      login(userData);

      // 4. 메인 페이지로 이동
      navigate("/");
    } catch (err) {
      setError("로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.");
      console.error(err);
    }
  };

  const handleOAuthLogin = (provider) => {
    window.location.href = `/oauth2/authorization/${provider}`;
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 dark:bg-gray-900 px-4">
      <div className="w-full max-w-6xl grid grid-cols-1 lg:grid-cols-2 gap-12">
        {/* 왼쪽: 소개 카드 */}
        <div className="flex flex-col items-center justify-center text-center p-10 bg-white dark:bg-gray-800 rounded-2xl shadow-lg">
          <h1 className="text-5xl font-bold mb-4 text-gray-900 dark:text-white">
            고민 상담
          </h1>
          <p className="text-lg mb-6 text-gray-700 dark:text-gray-300">
            당신의 마음에 귀 기울이는 AI 친구
          </p>
          <img
            src="/회원가입그림.jpg"
            alt="회원가입 안내"
            className="w-80 rounded-lg shadow-md"
          />
        </div>

        {/* 오른쪽: 로그인 카드 */}
        <div className="flex flex-col bg-white dark:bg-gray-800 rounded-2xl shadow-lg p-10">
          <h2 className="text-3xl font-semibold text-center mb-8 text-gray-900 dark:text-white">
            로그인 / 회원가입
          </h2>

          <form className="flex flex-col gap-4" onSubmit={handleSubmit}>
            <input
              type="email"
              placeholder="이메일"
              className="input input-bordered w-full dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
            <input
              type="password"
              placeholder="비밀번호"
              className="input input-bordered w-full dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            {error && <p className="text-error text-sm">{error}</p>}
            <button
              type="submit"
              className="btn btn-primary w-full mt-2 dark:bg-blue-600 dark:text-white dark:hover:bg-blue-700"
            >
              이메일로 로그인
            </button>
          </form>

          <div className="divider my-6 border-gray-300 dark:border-gray-600 text-gray-500 dark:text-gray-400">
            또는
          </div>

          {/* OAuth2 로그인 버튼 */}
          <div className="flex flex-col gap-3">
            <button
              onClick={() => handleOAuthLogin("google")}
              className="btn btn-outline flex items-center justify-center gap-3 w-full border-gray-300 text-gray-900 hover:bg-gray-50 dark:border-gray-600 dark:text-white dark:hover:bg-gray-700"
            >
              <FcGoogle size={24} />
              구글로 로그인
            </button>
            <button
              onClick={() => handleOAuthLogin("naver")}
              className="btn flex items-center justify-center gap-3 w-full bg-[#03C75A] text-white hover:bg-[#03C75A]/90 dark:bg-[#03C75A] dark:hover:bg-[#03C75A]/80"
            >
              <SiNaver size={24} />
              네이버로 로그인
            </button>
          </div>

          <div className="text-center mt-6">
            <Link
              to="/signup"
              className="link link-hover text-gray-700 dark:text-gray-300"
            >
              아직 회원이 아니신가요? 회원가입
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;

// import { useState } from "react";
// import { Link } from "react-router-dom";
// import { login as loginApi } from "../api/auth";
// import { useAuth } from "../contexts/AuthContext";
// import { FcGoogle } from "react-icons/fc"; // 구글 아이콘
// import { SiNaver } from "react-icons/si";  // 네이버 아이콘

// function LoginPage() {
//   const [email, setEmail] = useState("");
//   const [password, setPassword] = useState("");
//   const [error, setError] = useState("");
//   const { login } = useAuth();

//   const handleSubmit = async (e) => {
//     e.preventDefault();
//     setError("");
//     try {
//       const response = await loginApi(email, password);
//       login(response.data.accessToken);
//     } catch (err) {
//       setError("로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.");
//       console.error(err);
//     }
//   };

//   const handleOAuthLogin = (provider) => {
//     window.location.href = `http://localhost:8080/oauth2/authorization/${provider}`;
//   };

//   return (
//     <div className="min-h-screen flex items-center justify-center bg-gray-100 dark:bg-gray-900 px-4">
//       <div className="w-full max-w-6xl grid grid-cols-1 lg:grid-cols-2 gap-12">

//         {/* 왼쪽: 소개 카드 */}
//         <div className="flex flex-col items-center justify-center text-center p-10 bg-white dark:bg-gray-800 rounded-2xl shadow-lg">
//           <h1 className="text-5xl font-bold mb-4 text-gray-900 dark:text-white">고민 상담</h1>
//           <p className="text-lg mb-6 text-gray-700 dark:text-gray-300">당신의 마음에 귀 기울이는 AI 친구</p>
//           <img
//             src="/회원가입그림.jpg"
//             alt="회원가입 안내"
//             className="w-80 rounded-lg shadow-md"
//           />
//         </div>

//         {/* 오른쪽: 로그인 카드 */}
//         <div className="flex flex-col bg-white dark:bg-gray-800 rounded-2xl shadow-lg p-10">
//           <h2 className="text-3xl font-semibold text-center mb-8 text-gray-900 dark:text-white">
//             로그인 / 회원가입
//           </h2>

//           {/* 이메일 로그인 */}
//           <form className="flex flex-col gap-4" onSubmit={handleSubmit}>
//             <input
//               type="email"
//               placeholder="이메일"
//               className="input input-bordered w-full dark:bg-gray-700 dark:border-gray-600 dark:text-white"
//               required
//               value={email}
//               onChange={(e) => setEmail(e.target.value)}
//             />
//             <input
//               type="password"
//               placeholder="비밀번호"
//               className="input input-bordered w-full dark:bg-gray-700 dark:border-gray-600 dark:text-white"
//               required
//               value={password}
//               onChange={(e) => setPassword(e.target.value)}
//             />
//             {error && <p className="text-error text-sm">{error}</p>}
//             <button
//               type="submit"
//               className="btn btn-primary w-full mt-2 dark:bg-blue-600 dark:text-white dark:hover:bg-blue-700"
//             >
//               이메일로 로그인
//             </button>
//           </form>

//           <div className="divider my-6 border-gray-300 dark:border-gray-600 text-gray-500 dark:text-gray-400">
//             또는
//           </div>

//           {/* OAuth2 로그인 버튼 */}
//           <div className="flex flex-col gap-3">
//             <button
//               onClick={() => handleOAuthLogin("google")}
//               className="btn btn-outline flex items-center justify-center gap-3 w-full border-gray-300 text-gray-900 hover:bg-gray-50 dark:border-gray-600 dark:text-white dark:hover:bg-gray-700"

//             >
//               <FcGoogle size={24} />
//               구글로 로그인
//             </button>
//             <button
//               onClick={() => handleOAuthLogin("naver")}
//               className="btn flex items-center justify-center gap-3 w-full bg-[#03C75A] text-white hover:bg-[#03C75A]/90 dark:bg-[#03C75A] dark:hover:bg-[#03C75A]/80"
//             >
//               <SiNaver size={24} />
//               네이버로 로그인
//             </button>
//           </div>

//           <div className="text-center mt-6">
//             <Link to="/signup" className="link link-hover text-gray-700 dark:text-gray-300">
//               아직 회원이 아니신가요? 회원가입
//             </Link>
//           </div>

//         </div>
//       </div>
//     </div>
//   );
// }

// export default LoginPage;
