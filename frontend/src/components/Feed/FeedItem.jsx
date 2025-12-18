import { formatDistanceToNow } from "date-fns"; // Import the function to format dates
import { Carousel } from "react-responsive-carousel";
import "react-responsive-carousel/lib/styles/carousel.min.css";
import Reaction from "./Reaction";
import OptionButton from "../Common/OptionButton";
import { useState } from "react";
import Modal from "../Common/Modal";
import { PostApi } from "../../utils/ultis";

function splitFirstSentence(text) {
  if (!text || typeof text !== "string") return ["", ""];

  // Match everything up to the first sentence-ending punctuation
  // followed by whitespace or end of string
  // This avoids breaking on abbreviations like "Mr.", "Dr.", "e.g.", etc.
  const match = text.match(/^.*?[.!?](?=\s|$)/s);

  if (!match) {
    // No sentence-ending punctuation found → whole text is the first "sentence"
    return [text.trim(), ""];
  }

  const firstSentence = match[0].trim();
  const rest = text.slice(match[0].length).trim();

  return [firstSentence, rest];
}
const FeedItem = ({ userId, post, openPost, goBack }) => {
  const [title, content] = splitFirstSentence(post.content);
  const [modalOpen, setModalOpen] = useState(false);
  const onEdit = () => {
    setModalOpen(true);
  };
  const onDelete = async () => {
    await PostApi.deletePost(post);
  };
  return (
    <div key={post.id} className="collection-card post-card">
      <div className="post-header" onClick={openPost && (() => openPost(post))}>
        {goBack && (
          <button
            className="back-button"
            onClick={
              goBack &&
              (() => {
                goBack();
              })
            }
          >
            <svg
              rpl=""
              className="rpl-rtl-icon"
              fill="currentColor"
              height="16"
              icon-name="arrow-back"
              viewBox="0 0 20 20"
              width="16"
              xmlns="http://www.w3.org/2000/svg"
            >
              {" "}
              <path d="M17.5 9.1H4.679l5.487-5.462a.898.898 0 00.003-1.272.898.898 0 00-1.272-.003l-7.032 7a.898.898 0 000 1.275l7.03 7a.896.896 0 001.273-.003.898.898 0 00-.002-1.272l-5.487-5.462h12.82a.9.9 0 000-1.8z"></path>{" "}
            </svg>
          </button>
        )}
        <div className="post-header-info featured-content">
          <div className="user-info" onClick={(e) => e.stopPropagation()}>
            <span className="profile-image">
              <img
                className="avatar"
                src="https://www.gravatar.com/avatar/placeholder" // Use a default avatar or user image here
                alt="user-avatar"
              />
            </span>
            <h3 className="card-author">{post.username}</h3>
            {"•"}
            <span className="card-subtitle">{post.location}</span>
            {"•"}
            <span className="card-subtitle">
              Posted{" "}
              {formatDistanceToNow(new Date(post.createdAt), {
                addSuffix: true,
              })}
            </span>
            {"•"}
            <span className="card-subtitle">
              Updated{" "}
              {formatDistanceToNow(new Date(post.updatedAt), {
                addSuffix: true,
              })}
            </span>
          </div>
          <span className="post-topic">{post.postTopic}</span>
        </div>
        <OptionButton
          onEdit={onEdit}
          onDelete={onDelete}
          onReport={() => { }}
        ></OptionButton>
        <Modal
          userId={userId}
          postId={post.id}
          titleInput={title}
          textInput={content}
          medias={post.medias.filter((media) => media.mediaType == "IMAGE")}
          isOpen={modalOpen}
          onClose={() => {
            setModalOpen(false);
          }}
        ></Modal>
      </div>
      <h2 className="post-title" onClick={openPost && (() => openPost(post))}>
        {title}
      </h2>
      <p className="post-content" onClick={openPost && (() => openPost(post))}>
        {content}
      </p>
      {post.medias.length > 0 ? (
        <div className="post-media">
          <Carousel showArrows={true} showThumbs={false} showStatus={false}>
            {post.medias
              .filter((media) => media.mediaType == "IMAGE")
              .map((media) => (
                <div key={media.id} className="image-container">
                  <div
                    className="background-image"
                    style={{ backgroundImage: `url(${media.mediaURL})` }}
                  ></div>
                  <img src={media.mediaURL} alt="" />
                </div>
              ))}
          </Carousel>
        </div>
      ) : null}
      <div className="post-footer">
        <Reaction
          userId={userId}
          userReaction={post.userReaction}
          reactions={post.reactionCount}
          interactableId={post.interactableItemId}
          entityId={post.id}
          targetType="POST"
        ></Reaction>
        <button className="highlight-item" onClick={() => openPost && openPost(post)}>
          <svg
            rpl=""
            aria-hidden="true"
            className="icon-comment"
            fill="currentColor"
            height="16"
            icon-name="comment"
            viewBox="0 0 20 20"
            width="16"
            xmlns="http://www.w3.org/2000/svg"
          >
            {" "}
            <path d="M10 1a9 9 0 00-9 9c0 1.947.79 3.58 1.935 4.957L.231 17.661A.784.784 0 00.785 19H10a9 9 0 009-9 9 9 0 00-9-9zm0 16.2H6.162c-.994.004-1.907.053-3.045.144l-.076-.188a36.981 36.981 0 002.328-2.087l-1.05-1.263C3.297 12.576 2.8 11.331 2.8 10c0-3.97 3.23-7.2 7.2-7.2s7.2 3.23 7.2 7.2-3.23 7.2-7.2 7.2z"></path>
          </svg>
          <span className="highlight-title">{post.commentCount} Comments</span>
        </button>
        <button className="highlight-item">
          <svg
            rpl=""
            aria-hidden="true"
            className="icon-share rpl-rtl-icon"
            fill="currentColor"
            height="16"
            icon-name="share"
            viewBox="0 0 20 20"
            width="16"
            xmlns="http://www.w3.org/2000/svg"
          >
            {" "}
            <path d="M12.8 17.524l6.89-6.887a.9.9 0 000-1.273L12.8 2.477a1.64 1.64 0 00-1.782-.349 1.64 1.64 0 00-1.014 1.518v2.593C4.054 6.728 1.192 12.075 1 17.376a1.353 1.353 0 00.862 1.32 1.35 1.35 0 001.531-.364l.334-.381c1.705-1.944 3.323-3.791 6.277-4.103v2.509c0 .667.398 1.262 1.014 1.518a1.638 1.638 0 001.783-.349v-.002zm-.994-1.548V12h-.9c-3.969 0-6.162 2.1-8.001 4.161.514-4.011 2.823-8.16 8-8.16h.9V4.024L17.784 10l-5.977 5.976z"></path>{" "}
          </svg>
          <div className="highlight-title">{post.shareCount} Shares</div>
        </button>
      </div>
    </div>
  );
};

export default FeedItem;
