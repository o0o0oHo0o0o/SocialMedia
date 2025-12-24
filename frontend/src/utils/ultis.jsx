const FeedApi = (function () {
  async function getFeedFrom(api) {
    return await fetch(api, {
      credentials: "include",
    });
  }
  return { getFeedFrom };
})();
const CommentApi = (function () {
  const getFromUser = async function (userId) {
    return await fetch(`/api/comments/user/${userId}`, {
      credentials: "include",
    });
  };
  const getFromPost = async function (postId) {
    return await fetch(`/api/comments/post/${postId}`, {
      credentials: "include",
    });
  };
  const getFromComment = async function (parentCommentId) {
    return await fetch(
      `http://localhost:8080/api/comments/replies/${parentCommentId}`,
      { credentials: "include" },
    );
  };
  const createForPost = async function (replyData) {
    return await fetch("http://localhost:8080/api/comments", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
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
      credentials: "include",
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
  const getFromKeyword = async function (keyword) {
    return await fetch(`/api/comments/search/${keyword}`, {
      method: "GET",
      credentials: "include",
    });
  };
  return {
    getFromUser,
    getFromComment,
    getFromPost,
    createForPost,
    createForComment,
    deleteComment,
    getFromKeyword,
  };
})();
const PostApi = (function () {
  async function getFromId(postId) {
    return await fetch(`/api/posts/${postId}`, {
      method: "GET",
      credentials: "include",
    });
  }
  async function getFromUser(userId) {
    return await fetch(`/api/posts/user/${userId}`, {
      method: "GET",
      credentials: "include",
    });
  }
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
  async function getFromKeyword(keyword) {
    return await fetch(`/api/posts/search/${keyword}`, {
      method: "GET",
      credentials: "include",
    });
  }
  return {
    getFromId,
    getFromUser,
    deletePost,
    updateOrCreatePost,
    getFromKeyword,
  };
})();
const ReactionApi = (function () {
  async function fetchReaction(interactableItemId) {
    const url = `http://localhost:8080/api/reactions/stats/${interactableItemId}`;

    const response = await fetch(url, { credentials: "include" });

    if (!response.ok) {
      throw new Error("Failed to fetch reaction stats");
    }

    return await response.json();
  }
  async function createReaction(targetId, reactionType, targetType) {
    const res = await fetch("http://localhost:8080/api/reactions", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify({
        targetId: targetId,
        reactionType: reactionType,
        targetType: targetType,
      }),
    });
    if (!res.ok) {
      // try to include server error message for easier debugging
      let bodyText = "";
      try {
        bodyText = await res.text();
      } catch (e) {
        console.log(e);
      }
      console.error(
        `ReactionApi.createReaction failed: HTTP ${res.status}`,
        bodyText,
      );
    }
    return res;
  }
  return { fetchReaction, createReaction };
})();
const FollowApi = (function () {
  const checkUser = async function (username) {
    return await fetch(`/api/follows/${username}`, {
      credentials: "include",
    });
  };
  const addUser = async function (username) {
    return await fetch(`/api/follows/${username}`, {
      method: "POST",
      credentials: "include",
    });
  };
  async function deleteFollow(username) {
    return await fetch(`/api/follows/${username}`, {
      method: "DELETE",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
    });
  }
  return { checkUser, addUser, deleteFollow };
})();
const UserApi = (function () {
  const getFromKeyword = async function (keyword) {
    return await fetch(`/api/users/search/${keyword}`, {
      credentials: "include",
    });
  };

  return { getFromKeyword };
})();
export { CommentApi, PostApi, ReactionApi, FeedApi, FollowApi, UserApi };
