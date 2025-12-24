import React, { useEffect, useState } from "react";

import { formatDistanceToNow } from "date-fns"; // Import the function to format dates
import "../../styles/userInfoCard.css"; // We'll add the CSS below
import AvatarAndName from "./AvatarAndName";
import { FollowApi } from "../../utils/ultis";

const UserInfoCard = ({ user, target, postNumber }) => {
  const [followed, setFollowed] = useState(false);
  useEffect(() => {
    const fetchFollowStatus = async () => {
      if (target.username == user.username) {
        return;
      }
      const isFollowed = await FollowApi.checkUser(target.username);
      const data = await isFollowed.json();
      setFollowed(data);
    };

    fetchFollowStatus();
  }, []);
  const followUser = async () => {
    await FollowApi.addUser(target.username);
    setFollowed(true);
  };
  const unfollowUser = async () => {
    await FollowApi.deleteFollow(target.username);
    setFollowed(false);
  };

  return (
    <div className="user-card">
      <AvatarAndName target={target}></AvatarAndName>

      {/* Buttons */}
      {user.username != target.username && (
        <div className="buttons">
          <button
            className="follow-btn buttons"
            onClick={followed ? unfollowUser : followUser}
          >
            <svg
              rpl=""
              aria-hidden="true"
              class="button-leading-icon"
              fill="currentColor"
              height="16"
              icon-name="add-circle"
              viewBox="0 0 20 20"
              width="16"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path d="M10 1a9 9 0 100 18 9 9 0 000-18zm0 16.2a7.2 7.2 0 117.2-7.2 7.208 7.208 0 01-7.2 7.2zm.9-8.1H14v1.8h-3.1V14H9.1v-3.1H6V9.1h3.1V6h1.8v3.1z"></path>
            </svg>
            {followed ? "Unfollow" : "Follow"}
          </button>
          <button className="chat-btn buttons">
            <svg
              rpl=""
              aria-hidden="true"
              fill="currentColor"
              height="16"
              icon-name="chat"
              viewBox="0 0 20 20"
              width="16"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path d="M10 1a9 9 0 00-9 9c0 1.947.79 3.58 1.935 4.957L.231 17.661A.784.784 0 00.785 19H10a9 9 0 009-9 9 9 0 00-9-9zm0 16.2H6.162c-.994.004-1.907.053-3.045.144l-.076-.188a36.981 36.981 0 002.328-2.087l-1.05-1.263C3.297 12.576 2.8 11.331 2.8 10c0-3.97 3.23-7.2 7.2-7.2s7.2 3.23 7.2 7.2-3.23 7.2-7.2 7.2zm5.2-7.2a1.2 1.2 0 11-2.4 0 1.2 1.2 0 012.4 0zm-4 0a1.2 1.2 0 11-2.4 0 1.2 1.2 0 012.4 0zm-4 0a1.2 1.2 0 11-2.4 0 1.2 1.2 0 012.4 0z"></path>
            </svg>
            Start Chat
          </button>
        </div>
      )}

      {/* Stats */}
      <div className="stats">
        <div className="stat-row">
          <span>Posts</span>
          <strong>{postNumber}</strong>
        </div>
        <div className="stat-row">
          <span>Account Age</span>
          <strong>
            {formatDistanceToNow(new Date(target.createdAt), {
              addSuffix: false,
            })}{" "}
            🎂
          </strong>
        </div>
      </div>
    </div>
  );
};

export default UserInfoCard;