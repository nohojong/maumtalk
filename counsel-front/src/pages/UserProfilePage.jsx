// // src/pages/UserProfilePage.jsx
// import { useParams } from "react-router-dom";
// import UserProfile from "../components/UserProfile";

// const UserProfilePage = () => {
//   const { userId } = useParams();
//   return <UserProfile userId={userId} />;
// };

// export default UserProfilePage;
import UserProfile from "../components/UserProfile";

const UserProfilePage = () => {
  return <UserProfile />;
};

export default UserProfilePage;
