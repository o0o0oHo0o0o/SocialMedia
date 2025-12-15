import FeedItem from "./FeedItem";
import Comment from "./Comment.jsx";
import ReplyInput from "./ReplyInput.jsx";
import { CommentApi } from "./ultis.jsx";
import { useState } from "react";
const Post = ({ userId, post, goBack }) => {
  const [commentList, setCommentList] = useState([]);
  const handlePostReply = async (replyData) => {
    const response = await CommentApi.createForPost(replyData, userId, post);
    const data = await response.json();
    setCommentList([...commentList, { ...data, showReplies: false }]);
  };
  return (
    <div className="post-page">
      <FeedItem userId={userId} post={post} goBack={goBack}></FeedItem>
      <ReplyInput onSubmit={handlePostReply} postId={post.interactableItemId} />
      <hr />
      <Comment
        userId={userId}
        postId={post.id}
        setCommentList={setCommentList}
        commentList={commentList}
      ></Comment>
    </div>
  );
};
export default Post;
