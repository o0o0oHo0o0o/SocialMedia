import FeedItem from "./FeedItem";
import Comment from "./Comment.jsx";
import ReplyInput from "./ReplyInput.jsx";
import { CommentApi } from "../../utils/ultis.jsx";
import { useState } from "react";
const Post = ({ userId, post, openUser, goBack }) => {
  const [commentList, setCommentList] = useState([]);
  const handlePostReply = async (replyData) => {
    const response = await CommentApi.createForPost(replyData);
    const data = await response.json();
    setCommentList([...commentList, { ...data, showReplies: false }]);
  };
  return (
    <div className="content-container">
      <div className="post-page">
        <FeedItem
          userId={userId}
          post={post}
          openUser={openUser}
          goBack={goBack}
        ></FeedItem>
        <ReplyInput
          onSubmit={handlePostReply}
          postId={post.interactableItemId}
        />
        <hr />
        <Comment
          userId={userId}
          postId={post.id}
          setCommentList={setCommentList}
          commentList={commentList}
          openUser={openUser}
        ></Comment>
      </div>
      <div className="more-info-bar"></div>
    </div>
  );
};
export default Post;
