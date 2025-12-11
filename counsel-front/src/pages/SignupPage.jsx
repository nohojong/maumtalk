import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { signup } from "../api/auth";

function SignupPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    try {
      const response = await signup(email, password);
      localStorage.setItem("accessToken", response.data.accessToken);
      navigate("/");
    } catch (err) {
      setError("회원가입에 실패했습니다. 이메일 혹은 비밀번호를 확인해주세요.");
      console.error(err);
    }
  };

  return (
    <div className="hero min-h-screen bg-base-200">
      <div className="hero-content flex-col lg:flex-row-reverse">
        <div className="text-center lg:text-left">
          <h1 className="text-5xl font-bold">고민 상담</h1>
          <p className="py-6">당신의 마음에 귀 기울이는 AI 친구</p>
        </div>
        <div className="card shrink-0 w-full max-w-sm shadow-2xl bg-base-100">
          <form className="card-body" onSubmit={handleSubmit}>
            <div className="form-control">
              <label className="label">
                <span className="label-text">이메일</span>
              </label>
              <input
                type="email"
                placeholder="hello@example.com"
                className="input input-bordered"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
            <div className="form-control">
              <label className="label">
                <span className="label-text">비밀번호</span>
              </label>
              <input
                type="password"
                placeholder="password"
                className="input input-bordered"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
            {error && <p className="text-error text-sm">{error}</p>}
            <div className="form-control mt-6">
              <button type="submit" className="btn btn-primary">
                회원가입
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

export default SignupPage;
