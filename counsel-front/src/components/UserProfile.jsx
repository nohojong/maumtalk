// src/components/UserProfile.jsx
import { useEffect, useState } from "react";
import { getUserProfile, updateUserProfile } from "../api/user";
import { useAuth } from "../contexts/AuthContext";

const UserProfile = () => {
  const { user } = useAuth();
  const userId = user?.id;

  const [profile, setProfile] = useState({
    성별: "",
    나이: "",
    관심사: "",
    고민: "",
    결제종료일: "",
  });
  const [editMode, setEditMode] = useState(false);
  const [loading, setLoading] = useState(true);

  // 프로필 조회
  useEffect(() => {
    const fetchProfile = async () => {
      if (!userId) {
        setLoading(false);
        return;
      }
      
      setLoading(true);
      try {
        const response = await getUserProfile(userId);
        const data = response.data;
        console.log("받아온 프로필 데이터:", data);

        setProfile({
          성별: data?.gender || "선택안함",
          나이: data?.age ? String(data.age) : "",
          관심사: data?.interests || "",
          고민: data?.concern || "",
          결제종료일: data?.accessUntil
            ? new Date(data.accessUntil).toLocaleDateString()
            : "없음",
        });
      } catch (err) {
        console.error("프로필 로딩 실패", err);
        alert("프로필 불러오기 중 오류가 발생했습니다.");
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [userId]);

  // 로딩 중
  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <span className="loading loading-spinner loading-lg text-gray-400"></span>
      </div>
    );
  }
  
  // 로그인 안됨
  if (!userId) {
    return (
      <div className="p-8">
        <p className="text-gray-600">로그인 후 이용 가능합니다.</p>
      </div>
    );
  }

  // 저장 처리
  const handleSave = async () => {
    try {
      const updateData = {
        gender: profile.성별,
        age: Number(profile.나이),
        interests: profile.관심사,
        concern: profile.고민,
      };

      await updateUserProfile(userId, updateData);

      // 최신 데이터 다시 가져오기
      const response = await getUserProfile(userId);
      const data = response.data;
      setProfile({
        성별: data?.gender || "선택안함",
        나이: data?.age ? String(data.age) : "",
        관심사: data?.interests || "",
        고민: data?.concern || "",
        결제종료일: data?.accessUntil
          ? new Date(data.accessUntil).toLocaleDateString()
          : "없음",
      });

      setEditMode(false);
      alert("프로필이 업데이트되었습니다!");
    } catch (err) {
      console.error("프로필 저장 실패", err);
      alert("저장 실패");
    }
  };

  // 값 변경 처리
  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name === "나이" && Number(value) < 0) return;
    setProfile({ ...profile, [name]: value });
  };

  return (
    <div className="p-8 max-w-5xl mx-auto">
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 왼쪽: 프로필 이미지 카드 */}
        <div className="lg:col-span-1">
          <div className="bg-gradient-to-br from-blue-50 to-indigo-50 border border-gray-200 rounded-lg shadow-sm overflow-hidden">
            <div className="p-6 flex flex-col items-center text-center">
              <div className="w-32 h-32 bg-white rounded-full flex items-center justify-center mb-4 shadow-sm border border-gray-100">
                <span className="text-5xl">
                  {profile.성별 === "남" ? "👨" : profile.성별 === "여" ? "👩" : "👤"}
                </span>
              </div>
              <h2 className="text-2xl font-bold text-gray-800 mb-2">
                {user?.name || "사용자"}
              </h2>
              <div className="px-3 py-1 bg-indigo-100 text-indigo-700 rounded-full text-sm font-medium">
                {profile.나이 ? `${profile.나이}세` : "나이 미입력"}
              </div>
              
              <div className="w-full h-px bg-gray-200 my-4"></div>
              
              <div className="w-full text-left space-y-2">
                <div className="flex items-center gap-2 text-gray-600 text-sm">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  <span>결제 종료일</span>
                </div>
                <p className="text-gray-800 font-medium pl-6">{profile.결제종료일}</p>
              </div>
            </div>
          </div>
        </div>

        {/* 오른쪽: 프로필 정보 카드 */}
        <div className="lg:col-span-2">
          <div className="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden">
            <div className="p-6">
              <div className="flex items-center gap-2 mb-6">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
                <h2 className="text-2xl font-bold text-gray-800">내 프로필</h2>
              </div>

              {editMode ? (
                <div className="space-y-4">
                  {/* 성별 */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">성별</label>
                    <div className="flex gap-3">
                      <label className="flex items-center gap-2 border border-gray-300 rounded-lg px-4 py-2.5 cursor-pointer hover:bg-gray-50 transition">
                        <input
                          type="radio"
                          name="성별"
                          value="남"
                          checked={profile.성별 === "남"}
                          onChange={handleChange}
                          className="w-4 h-4 text-indigo-600"
                        />
                        <span className="text-gray-700">남성</span>
                      </label>
                      <label className="flex items-center gap-2 border border-gray-300 rounded-lg px-4 py-2.5 cursor-pointer hover:bg-gray-50 transition">
                        <input
                          type="radio"
                          name="성별"
                          value="여"
                          checked={profile.성별 === "여"}
                          onChange={handleChange}
                          className="w-4 h-4 text-indigo-600"
                        />
                        <span className="text-gray-700">여성</span>
                      </label>
                    </div>
                  </div>

                  {/* 나이 */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">나이</label>
                    <input
                      name="나이"
                      type="number"
                      value={profile.나이}
                      onChange={handleChange}
                      placeholder="나이를 입력하세요"
                      className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                      min="0"
                    />
                  </div>

                  {/* 관심사 */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">관심사</label>
                    <input
                      name="관심사"
                      value={profile.관심사}
                      onChange={handleChange}
                      placeholder="관심사를 입력하세요"
                      className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                    />
                  </div>

                  {/* 고민 */}
                  <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-2">고민</label>
                    <textarea
                      name="고민"
                      value={profile.고민}
                      onChange={handleChange}
                      placeholder="고민을 입력하세요"
                      className="w-full px-4 py-2.5 border border-gray-300 rounded-lg h-24 resize-none focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                    />
                  </div>

                  <div className="flex justify-end gap-2 mt-6 pt-4 border-t border-gray-200">
                    <button 
                      className="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg transition"
                      onClick={() => setEditMode(false)}
                    >
                      취소
                    </button>
                    <button 
                      className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition"
                      onClick={handleSave}
                    >
                      저장
                    </button>
                  </div>
                </div>
              ) : (
                <div className="space-y-4">
                  {/* 성별 */}
                  <div className="flex items-center gap-3 p-4 rounded-lg bg-gray-50 border border-gray-100">
                    <div className="px-2.5 py-1 bg-indigo-100 text-indigo-700 rounded text-xs font-semibold">성별</div>
                    <span className="text-gray-800 font-medium">{profile.성별}</span>
                  </div>

                  {/* 나이 */}
                  <div className="flex items-center gap-3 p-4 rounded-lg bg-gray-50 border border-gray-100">
                    <div className="px-2.5 py-1 bg-indigo-100 text-indigo-700 rounded text-xs font-semibold">나이</div>
                    <span className="text-gray-800 font-medium">{profile.나이 || "미입력"}</span>
                  </div>

                  {/* 관심사 */}
                  <div className="p-4 rounded-lg bg-gray-50 border border-gray-100">
                    <div className="px-2.5 py-1 bg-indigo-100 text-indigo-700 rounded text-xs font-semibold mb-2 inline-block">관심사</div>
                    <p className="text-gray-800">{profile.관심사 || "미입력"}</p>
                  </div>

                  {/* 고민 */}
                  <div className="p-4 rounded-lg bg-gray-50 border border-gray-100">
                    <div className="px-2.5 py-1 bg-indigo-100 text-indigo-700 rounded text-xs font-semibold mb-2 inline-block">고민</div>
                    <p className="text-gray-800 whitespace-pre-wrap">{profile.고민 || "미입력"}</p>
                  </div>

                  <div className="flex justify-end mt-6 pt-4 border-t border-gray-200">
                    <button
                      className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition"
                      onClick={() => setEditMode(true)}
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                      </svg>
                      프로필 수정
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserProfile;