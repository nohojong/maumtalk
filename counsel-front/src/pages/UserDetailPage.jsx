// src/pages/UserDetailPage.jsx
import { useParams } from "react-router-dom";
import UserDetail from "../components/UserDetail";

const UserDetailPage = () => {
  const { userId } = useParams();
  return <UserDetail userId={userId} />;
};

export default UserDetailPage;
