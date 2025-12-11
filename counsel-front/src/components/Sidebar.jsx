function Sidebar() {
  return (
    <div className="drawer-side">
      <label
        htmlFor="my-drawer"
        aria-label="close sidebar"
        className="drawer-overlay"
      ></label>
      <ul className="menu p-4 w-80 min-h-full bg-base-100 text-base-content">
        <li className="menu-title">대화 목록</li>
      </ul>
    </div>
  );
}

export default Sidebar;
