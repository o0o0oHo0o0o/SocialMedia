const FeedApi = (function () {
  async function getFeedFrom(api) {
    return await fetch(api);
  }
  return { getFeedFrom };
})();
const CommentApi = (function () {
  const getFromPost = async function (postId) {
    return await fetch(
      // `http://localhost:8080/api/comments/${postId}?page=0&size=10`,
      `http://localhost:8080/api/comments/${postId}`,
    );
  };
  const getFromComment = async function (parentCommentId) {
    return await fetch(
      // `http://localhost:8080/api/comments/parent/${parentCommentId}?page=0&size=10`,
      `http://localhost:8080/api/comments/parent/${parentCommentId}`,
    );
  };
  const createForPost = async function (replyData, userId, post) {
    return await fetch("http://localhost:8080/api/comments", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        content: replyData.content,
        userId: userId,
        targetInteractableItemID: post.interactableItemId,
      }),
    });
  };
  const createForComment = async function (replyData, userId) {
    return await fetch("http://localhost:8080/api/comments", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        content: replyData.content,
        userId: userId,
        targetInteractableItemID: replyData.postId,
        parentCommentId: replyData.parentCommentId,
      }),
    });
  };
  async function deleteComment(comment) {
    return await fetch(`http://localhost:8080/api/comments/${comment.id}`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        // If you use authentication (JWT, session, etc.), add it here:
        // "Authorization": "Bearer " + yourToken,
      },
      // credentials: "include", // ← uncomment if you're using session cookies
    });
  }
  return {
    getFromComment,
    getFromPost,
    createForPost,
    createForComment,
    deleteComment,
  };
})();
const PostApi = (function () {
  async function deletePost(post) {
    return await fetch(`http://localhost:8080/api/posts/${post.id}`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
        // If you use authentication (JWT, session, etc.), add it here:
        // "Authorization": "Bearer " + yourToken,
      },
      // credentials: "include", // ← uncomment if you're using session cookies
    });
  }
  async function updateOrCreatePost(postId, formData) {
    return await fetch(
      `http://localhost:8080/api/posts${postId ? `/${postId}` : ""}?`,
      {
        method: "POST",
        body: formData,
        // DO NOT set Content-Type header! Let browser set it with correct boundary
      },
    );
  }
  return { deletePost, updateOrCreatePost };
})();
const ReactionApi = (function () {
  async function fetchReaction(userId, interactableItemId) {
    const url = `http://localhost:8080/api/reactions/${userId}/${interactableItemId}`;

    const response = await fetch(url);

    if (!response.ok) {
      throw new Error("Failed to fetch reaction");
    }

    return await response.json();
  }
  async function createReaction(interactableId, userId, name, targetType) {
    return await fetch("http://localhost:8080/api/reactions", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        interactableItemId: interactableId,
        userId: userId,
        reactionType: name,
        targetType,
      }),
    });
  }
  return { fetchReaction, createReaction };
})();
export { CommentApi, PostApi, ReactionApi, FeedApi };
