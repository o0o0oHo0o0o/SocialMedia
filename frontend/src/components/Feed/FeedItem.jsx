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
const FeedItem = ({ userId, post, openPost, openUser, goBack, small }) => {
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
      <div className="post-container">
        <div className="post-text">
          <div className="post-header">
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
                  <svg
                    className="avatar"
                    viewBox="0 -0.5 1025 1025"
                    class="icon"
                    version="1.1"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="#000000"
                  >
                    <g id="SVGRepo_bgCarrier" stroke-width="0"></g>
                    <g
                      id="SVGRepo_tracerCarrier"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    ></g>
                    <g id="SVGRepo_iconCarrier">
                      <path
                        d="M513.8875 0C231.1375 0 1.891667 229.25 1.891667 512c0 174.154167 86.9875 327.979167 219.8625 420.475 25.9375-90.3375 91.558333-158.35 180.8375-189.3375a310.0875 310.0875 0 0 1 10.029166-3.2875c0.954167-0.295833 1.904167-0.608333 2.866667-0.9a328.041667 328.041667 0 0 1 26.495833-6.741667c1.204167-0.254167 2.425-0.479167 3.6375-0.720833a332.75 332.75 0 0 1 24.3125-3.945833c1.379167-0.175 2.75-0.366667 4.133334-0.525a357.916667 357.916667 0 0 1 14.079166-1.358334h18.545834c1.770833 0.120833 3.658333 0.120833 5.55 0h1.654166c-130.470833 0-236.6625-103.670833-240.858333-233.120833-0.145833-0.091667-0.291667-0.166667-0.4375-0.258333-8.295833-155.104167 131.7875-250.454167 233.391667-248.5375 2.629167-0.0875 5.254167-0.2 7.904166-0.2 2.516667 0 5.004167 0.1125 7.495834 0.1875 29.391667-0.5 61.9375 7.083333 93.2125 21.8625a241.125 241.125 0 0 1 57.345833 37.083333c50.608333 43.075 87.366667 108.058333 83.004167 189.6l-0.195834 0.116667c-4.120833 129.520833-110.3375 233.2625-240.8625 233.2625h25.983334a356.083333 356.083333 0 0 1 14.058333 1.358333c1.4 0.158333 2.779167 0.35 4.170833 0.529167a309.825 309.825 0 0 1 14.358334 2.1125c3.3 0.558333 6.570833 1.166667 9.820833 1.816666 1.254167 0.25 2.5125 0.479167 3.754167 0.745834a327.791667 327.791667 0 0 1 26.379166 6.7125c1.033333 0.3125 2.054167 0.645833 3.0875 0.966666 3.283333 1.020833 6.5375 2.083333 9.7625 3.204167 89.25 30.9625 154.895833 98.954167 180.9125 189.270833 132.7875-92.504167 219.708333-246.279167 219.708334-420.370833C1025.891667 229.25 796.641667 0 513.8875 0z"
                        fill="#F08E83"
                      ></path>
                      <path
                        d="M754.754167 492.391667c-63.754167 39.791667-230.933333-17.7625-280.608334-105.491667-46.325 81.2625-124.820833 153.2375-201.1125 105.633333 4.195833 129.45 110.383333 233.120833 240.858334 233.120834 130.520833 0 236.7375-103.741667 240.8625-233.2625zM614.6 265.591667a260.729167 260.729167 0 0 1 57.345833 37.083333 241.158333 241.158333 0 0 0-57.345833-37.083333zM521.3875 243.729167c-2.495833-0.075-4.979167-0.1875-7.495833-0.1875-2.65 0-5.275 0.1125-7.904167 0.2 2.533333 0.05 5.066667 0.104167 7.554167 0.275 2.575-0.179167 5.208333-0.245833 7.845833-0.2875z"
                        fill="#FCE9EA"
                      ></path>
                      <path
                        d="M441.979167 732.204167c1.204167-0.254167 2.425-0.479167 3.6375-0.720834-1.2125 0.241667-2.433333 0.470833-3.6375 0.720834zM455.566667 729.645833zM553.929167 727.0125c1.4 0.158333 2.779167 0.35 4.170833 0.529167-1.391667-0.179167-2.770833-0.370833-4.170833-0.529167zM567.941667 728.916667zM412.616667 739.845833c0.954167-0.295833 1.904167-0.608333 2.866666-0.9-0.9625 0.291667-1.908333 0.604167-2.866666 0.9zM582.283333 731.470833c1.25 0.25 2.5125 0.483333 3.754167 0.745834-1.241667-0.266667-2.5-0.5-3.754167-0.745834zM612.416667 738.929167c1.033333 0.3125 2.054167 0.645833 3.0875 0.966666-1.029167-0.320833-2.05-0.654167-3.0875-0.966666zM625.266667 743.1l2.1375 0.758333a119.479167 119.479167 0 0 1-10.475 22.158334c17.145833 26.404167 12.841667 141.5375-12.925 123.341666l-45.116667-31.654166-45-31.654167 2.95-2.079167c-0.983333 0.025-1.9625 0.075-2.95 0.075-1.029167 0-2.045833-0.054167-3.070833-0.079166l2.95 2.083333-45 31.654167-45.116667 31.654166c-25.9125 18.229167-30.204167-97.329167-12.883333-123.470833a119.583333 119.583333 0 0 1-10.379167-21.975c0.733333-0.2625 1.4625-0.520833 2.195833-0.775-89.279167 30.991667-154.9 99.004167-180.8375 189.3375 82.854167 57.679167 183.5375 91.525 292.1375 91.525 108.666667 0 209.404167-33.8875 292.291667-91.629167-26.0125-90.316667-91.658333-158.3125-180.908333-189.270833zM469.929167 727.5375c1.379167-0.175 2.75-0.366667 4.133333-0.525-1.3875 0.158333-2.754167 0.35-4.133333 0.525z"
                        fill="#CFE07D"
                      ></path>
                      <path
                        d="M410.770833 765.8875c3.441667-5.195833 7.733333-6.879167 12.883334-3.258333l45.116666 31.654166 42.045834 29.6875c1.025 0.025 2.041667 0.079167 3.070833 0.079167 0.9875 0 1.9625-0.05 2.95-0.075l42.054167-29.691667 45.116666-31.654166c5.175-3.654167 9.475-1.9125 12.925 3.3875a119.479167 119.479167 0 0 0 10.475-22.158334l-2.1375-0.758333a318.3625 318.3625 0 0 0-9.7625-3.204167c-1.033333-0.320833-2.054167-0.654167-3.0875-0.966666a327.791667 327.791667 0 0 0-26.379166-6.7125c-1.245833-0.2625-2.508333-0.495833-3.754167-0.745834a349.516667 349.516667 0 0 0-14.341667-2.554166 353.654167 353.654167 0 0 0-9.8375-1.375c-1.391667-0.179167-2.770833-0.370833-4.170833-0.529167a356.083333 356.083333 0 0 0-14.058333-1.358333h-27.6375c-1.891667 0.120833-3.779167 0.120833-5.55 0h-18.545834a357.916667 357.916667 0 0 0-14.079166 1.358333c-1.3875 0.158333-2.754167 0.35-4.133334 0.525a343.516667 343.516667 0 0 0-14.3625 2.108333c-3.341667 0.566667-6.658333 1.179167-9.95 1.8375-1.2125 0.241667-2.429167 0.466667-3.6375 0.720834a328.041667 328.041667 0 0 0-26.495833 6.741666c-0.9625 0.2875-1.908333 0.604167-2.866667 0.9a313.4875 313.4875 0 0 0-12.225 4.0625 119.333333 119.333333 0 0 0 10.375 21.979167z"
                        fill="#FEFEFE"
                      ></path>
                      <path
                        d="M604.008333 762.625l-45.116666 31.654167-42.054167 29.691666-2.95 2.079167 45 31.654167 45.116667 31.654166c25.766667 18.195833 30.070833-96.9375 12.925-123.341666-3.445833-5.304167-7.745833-7.045833-12.920834-3.391667zM423.654167 762.625c-5.15-3.620833-9.441667-1.9375-12.883334 3.258333-17.320833 26.1375-13.029167 141.7 12.883334 123.470834l45.116666-31.654167 45-31.654167-2.95-2.083333-42.045833-29.6875-45.120833-31.65z"
                        fill="#7EA701"
                      ></path>
                      <path
                        d="M474.145833 386.9c49.675 87.729167 216.854167 145.283333 280.608334 105.491667l0.195833-0.116667c4.3625-81.545833-32.391667-146.529167-83.004167-189.6a260.533333 260.533333 0 0 0-57.345833-37.083333c-31.275-14.779167-63.820833-22.3625-93.2125-21.8625-2.6375 0.045833-5.270833 0.108333-7.85 0.283333 8.341667 29.533333-8.270833 88.3-39.391667 142.8875z"
                        fill="#F7B970"
                      ></path>
                      <path
                        d="M474.145833 386.9c31.120833-54.5875 47.733333-113.354167 39.391667-142.8875a155.220833 155.220833 0 0 0-7.554167-0.275c-101.6-1.916667-241.683333 93.433333-233.391666 248.5375 0.145833 0.091667 0.291667 0.166667 0.4375 0.258333 76.295833 47.604167 154.791667-24.370833 201.116666-105.633333z"
                        fill="#FBCE77"
                      ></path>
                    </g>
                  </svg>
                </span>
                <h3
                  className="card-author"
                  onClick={() => {
                    openUser(post.user);
                  }}
                >
                  {post.user.username}
                </h3>
                {!small && (
                  <>
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
                  </>
                )}
              </div>
              {!small && <span className="post-topic">{post.postTopic}</span>}
            </div>
            {!small && (
              <OptionButton
                onEdit={onEdit}
                onDelete={onDelete}
                onReport={() => {}}
              ></OptionButton>
            )}
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
          <h2
            className="post-title"
            onClick={openPost && (() => openPost(post))}
          >
            {title}
          </h2>
          <p
            className="post-content"
            onClick={openPost && (() => openPost(post))}
          >
            {content}
          </p>
        </div>
        {post.medias.some((media) => {
          media.mediaType == "IMAGE";
        }) ? (
          <div className="post-media">
            <Carousel showArrows={true} showThumbs={false} showStatus={false}>
              {post.medias
                .filter((media) => media.mediaType == "IMAGE")
                .map((media) => (
                  <div key={media.id} className="image-container">
                    <div
                      className="background-image"
                      style={{
                        backgroundImage: `url(${media.mediaURL}), url(${media.fileName})`,
                      }}
                    ></div>
                    <img
                      src={media.mediaURL}
                      alt=""
                      onError={(e) => {
                        // Prevent fallback if already trying the fallback image
                        if (e.target.src !== media.fileName) {
                          e.target.src = media.fileName || ""; // Fallback to fileName if mediaURL fails
                        }
                      }}
                    />
                  </div>
                ))}
            </Carousel>
          </div>
        ) : (
          post.medias.map((media) => {
            return (
              <div className="post-media">
                <div
                  className="background-image"
                  style={{
                    backgroundImage: `url(${media.mediaURL}), url(${media.fileName})`,
                  }}
                ></div>
                <video
                  src={media.mediaURL}
                  onError={(e) => {
                    // Prevent fallback if already trying the fallback image
                    if (e.target.src !== media.fileName) {
                      e.target.src = media.fileName || ""; // Fallback to fileName if mediaURL fails
                    }
                  }}
                  controls
                ></video>
              </div>
            );
          })
        )}
      </div>
      <div className="post-footer">
        {!small && (
          <Reaction
            userId={userId}
            userReaction={post.userReaction}
            reactions={post.reactionCount}
            interactableId={post.interactableItemId}
            entityId={post.id}
            targetType="POST"
          ></Reaction>
        )}
        <button
          className="highlight-item"
          onClick={() => openPost && openPost(post)}
        >
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
