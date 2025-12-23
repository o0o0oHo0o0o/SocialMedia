import React, { useEffect, useState } from "react";
import FeedItem from "./FeedItem";
import { PostApi, CommentApi, UserApi } from "../../utils/ultis";
import CommentItem from "./CommentItem";
import "../../styles/searchPage.css";
import UserInfoCard from "./UserInfoCard";
import AvatarAndName from "./AvatarAndName";

function SearchPage({ keyword, openPost }) {
  const userId = 1;
  const [userList, setUserList] = useState([]);
  const [postList, setPostList] = useState([]);
  const [commentList, setCommentList] = useState([]);
  const [currentPage, setCurrentPage] = useState("post");

  useEffect(() => {
    async function fetchUserData() {
      if (currentPage == "post") {
        const response = await PostApi.getFromKeyword(keyword);
        const data = await response.json();
        setPostList([...data]);
      } else if (currentPage == "comment") {
        const response = await CommentApi.getFromKeyword(keyword);
        const data = await response.json();
        console.log(data);
        setCommentList([
          ...data.map((comment) => {
            comment.replied = false;
            return comment;
          }),
        ]);
      } else {
        const response = await UserApi.getFromKeyword(keyword);
        const data = await response.json();
        setUserList([...data]);
      }
    }
    fetchUserData();
  }, [currentPage, keyword]);

  return (
    <div className="content-container">
      <div className="search-page">
        <div className="page-controller">
          <button
            className={currentPage == "post" ? "active" : ""}
            onClick={() => {
              setCurrentPage("post");
            }}
          >
            Post
          </button>
          <button
            className={currentPage == "comment" ? "active" : ""}
            onClick={() => {
              setCurrentPage("comment");
            }}
          >
            Comment
          </button>
          <button
            className={currentPage == "user" ? "active" : ""}
            onClick={() => {
              setCurrentPage("user");
            }}
          >
            User
          </button>
        </div>
        {currentPage == "post" &&
          postList.map((post) => (
            <React.Fragment key={post.id}>
              <FeedItem userId={userId} post={post} openPost={openPost} />
              <hr />
            </React.Fragment>
          ))}
        {currentPage == "comment" &&
          commentList.map((comment, index) => (
            <CommentItem
              key={comment.id}
              userId={userId}
              commentIndex={index}
              postId={comment.postId}
              commentList={commentList}
              setCommentList={setCommentList}
              openPost={openPost}
            ></CommentItem>
          ))}
        {currentPage == "user" &&
          userList.map((user) => <AvatarAndName target={user}></AvatarAndName>)}
      </div>
      <div className="more-info-bar"></div>
    </div>
  );
}

export default SearchPage;
