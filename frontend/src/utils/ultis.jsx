const FeedApi = (function () {
  async function getFeedFrom(api) {
    return await fetch(api, {
      credentials: 'include'
    });
  }
  return { getFeedFrom };
})();
const CommentApi = (function () {
  const getFromPost = async function (postId) {
    return await fetch(
      `http://localhost:8080/api/comments/post/${postId}`,
      { credentials: 'include' }
    );
  };
  const getFromComment = async function (parentCommentId) {
    return await fetch(
      `http://localhost:8080/api/comments/replies/${parentCommentId}`,
      { credentials: 'include' }
    );
  };
  const createForPost = async function (replyData) {
    return await fetch("http://localhost:8080/api/comments", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: 'include',
      body: JSON.stringify({
        content: replyData.content,
        targetInteractableItemID: replyData.postId,
      }),
    });
  };
  const createForComment = async function (replyData) {
    return await fetch("http://localhost:8080/api/comments", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: 'include',
      body: JSON.stringify({
        content: replyData.content,
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
      },
      credentials: "include",
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
      },
      credentials: "include",
    });
  }
  async function updateOrCreatePost(postId, formData) {
    return await fetch(
      `http://localhost:8080/api/posts${postId ? `/${postId}` : ""}?`,
      {
        method: "POST",
        body: formData,
        credentials: "include",
        // DO NOT set Content-Type header! Let browser set it with correct boundary
      },
    );
  }
  return { deletePost, updateOrCreatePost };
})();
const ReactionApi = (function () {
  async function fetchReaction(interactableItemId) {
    const url = `http://localhost:8080/api/reactions/stats/${interactableItemId}`;

    const response = await fetch(url, { credentials: 'include' });

    if (!response.ok) {
      throw new Error("Failed to fetch reaction stats");
    }

    return await response.json();
  }
  async function createReaction(targetId, reactionType, targetType) {
    const res = await fetch("http://localhost:8080/api/reactions", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: 'include',
      body: JSON.stringify({
        targetId: targetId,
        reactionType: reactionType,
        targetType: targetType,
      }),
    });
    if (!res.ok) {
      // try to include server error message for easier debugging
      let bodyText = '';
      try { bodyText = await res.text(); } catch (e) { /* ignore */ }
      console.error(`ReactionApi.createReaction failed: HTTP ${res.status}`, bodyText);
    }
    return res;
  }
  return { fetchReaction, createReaction };
})();
export { CommentApi, PostApi, ReactionApi, FeedApi };
