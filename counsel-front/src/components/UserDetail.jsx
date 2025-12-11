import { useEffect, useState } from "react";
import { getUser, deleteUser } from "../api/user";

const UserDetail = ({ userId }) => {
  const [user, setUser] = useState(null);

  useEffect(() => {
    const fetchUser = async () => {
      const data = await getUser(userId);
      setUser(data);
    };
    fetchUser();
  }, [userId]);

  const handleDelete = async () => {
    await deleteUser(userId);
    alert("User deleted!");
    setUser(null);
  };

  if (!user) return <p>Loading...</p>;

  return (
    <div className="card bg-base-100 shadow-md p-6 mt-4">
      <h2 className="text-xl font-bold mb-4">User Detail</h2>
      <p>ID: {user.id}</p>
      <p>Email: {user.email}</p>
      <button className="btn btn-error mt-2" onClick={handleDelete}>
        Delete User
      </button>
    </div>
  );
};

export default UserDetail;
