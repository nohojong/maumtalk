import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";

function Navbar() {
  const { isLoggedIn, logout } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="navbar bg-base-100 shadow-md relative">
      {/* 좌측 메뉴 버튼 (모바일용) */}
      <div className="navbar-start">
        <label
          htmlFor="my-drawer"
          className="btn btn-square btn-ghost lg:hidden"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            className="inline-block w-5 h-5 stroke-current"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth="2"
              d="M4 6h16M4 12h16M4 18h16"
            ></path>
          </svg>
        </label>
      </div>

      {/* 고민 상담 버튼: 중앙 고정 */}
      <div className="absolute left-1/2 transform -translate-x-1/2">
        <button
          className="
            text-xl font-bold px-6 py-2            
            bg-transparent focus:outline-none focus:ring-0
            text-base-content
            hover:bg-base-200 transition-colors duration-200
            cursor-pointer
          "
          style={{ lineHeight: 1 }}
          onClick={() => {}}
        >
          마음 톡톡
        </button>
      </div>

      {/* 오른쪽 버튼들: 로그인/로그아웃 */}
      <div className="navbar-end gap-2 flex items-center">
        {isLoggedIn ? (
          <button onClick={logout} className="btn btn-ghost">
            로그아웃
          </button>
        ) : (
          <button onClick={() => navigate("/login")} className="btn btn-ghost">
            로그인
          </button>
        )}
      </div>
    </div>
  );
}

export default Navbar;
