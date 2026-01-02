// Modal.js
import React, { useEffect, useState } from "react";
import "../../styles/modal.css";
import { Carousel } from "react-responsive-carousel";
import { PostApi } from "../../utils/ultis";

function Modal({ postId, titleInput, textInput, medias, isOpen, onClose }) {
  const MAX_IMAGES = 10;
  const MAX_VIDEOS = 1;
  const MAX_FILE_SIZE_MB = 100;
  const MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;

  const [mediaPreviews, setMediaPreviews] = useState(
    medias ? medias.map((media) => media.mediaURL) : [],
  );
  const [title, setTitle] = useState(titleInput ? titleInput : "");
  const [text, setText] = useState(textInput ? textInput : "");
  const [selectedFiles, setSelectedFiles] = useState([]); // actual File objects for upload
  const [selectedFlair, setSelectedFlair] = useState(null);
  const [currentSlide, setCurrentSlide] = useState(0);
  const currentMediaType =
    mediaPreviews.length > 0 ? mediaPreviews[0].type : "none";
  const imageCount = mediaPreviews.filter((m) => m.type === "image").length;
  const videoCount = mediaPreviews.filter((m) => m.type === "video").length;
  const canAddImage = currentMediaType !== "video" && imageCount < MAX_IMAGES;
  const canAddVideo = currentMediaType !== "image" && videoCount < MAX_VIDEOS;

  useEffect(() => {
    medias && setMediaPreviews(medias.map((media) => media.mediaURL));
  }, [medias]);
  if (!isOpen) return null;
  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    // File size check (applies to both image and video)
    if (file.size > MAX_FILE_SIZE_BYTES) {
      alert(`File size exceeds ${MAX_FILE_SIZE_MB}MB limit.`);
      return;
    }

    const type = file.type.startsWith("image/") ? "image" : "video";

    // Additional validation based on current content
    if (type === "image" && !canAddImage) {
      alert(
        `You can only upload up to ${MAX_IMAGES} images. Remove the video first if you want to upload images.`,
      );
      return;
    }
    if (type === "video" && !canAddVideo) {
      alert(
        "You can only upload 1 video. Remove existing images first if you want to upload a video.",
      );
      return;
    } // If switching type, clear previous media
    if (mediaPreviews.length > 0 && mediaPreviews[0].type !== type) {
      if (
        !window.confirm(
          `You already have ${mediaPreviews[0].type}s uploaded. Adding a ${type} will remove them. Continue?`,
        )
      ) {
        return;
      }
      setCurrentSlide(0);
      setMediaPreviews([]);
    }
    if (type == "image") {
      handleImageChange(e);
    }
    if (type == "video") {
      handleVideoChange(e);
    }
  };
  function handleVideoChange(e) {
    const file = Array.from(e.target.files)[0];
    const reader = new FileReader();
    reader.onloadend = () => {
      setMediaPreviews((prev) => [...prev, reader.result]);
    };
    reader.readAsDataURL(file);
    // Save actual file for upload
    console.log(file);
    setSelectedFiles((prev) => [...prev, file]);
  }

  const removeMedia = (index) => {
    setMediaPreviews((prev) => prev.filter((_, i) => i !== index));
  };

  const handleImageChange = (e) => {
    const files = Array.from(e.target.files);

    files.forEach((file) => {
      if (mediaPreviews.length + files.length >= MAX_IMAGES) {
        alert(`You can only upload up to ${MAX_IMAGES} images.`);
        return;
      }

      if (file && file.type.startsWith("image/")) {
        const reader = new FileReader();
        reader.onloadend = () => {
          console.log(reader.result.type);
          setMediaPreviews((prev) => [...prev, reader.result]);
        };
        reader.readAsDataURL(file);
        // Save actual file for upload
        setSelectedFiles((prev) => [...prev, file]);
      }
    });

    // Reset input so same file can be selected again if needed
    e.target.value = "";
  };

  const removeImage = (index) => {
    if (!mediaPreviews[index].includes("localhost")) {
      setSelectedFiles((prev) => prev.filter((_, i) => i !== index)); // Also update the current slide if needed
    }
    setMediaPreviews((prev) => {
      const newPreviews = prev.filter((_, i) => i !== index);

      // Also update the current slide if needed
      setCurrentSlide((prevSlide) => {
        if (prevSlide >= newPreviews.length) {
          return Math.max(0, newPreviews.length - 1); // go to last
        }
        if (prevSlide > index) {
          return prevSlide - 1; // shift left if we removed earlier slide
        }
        return prevSlide;
      });

      return newPreviews;
    });
  };

  const handleCancel = () => {
    setTitle("");
    setText("");
    setMediaPreviews([]);
    onClose();
  };
  const handlePost = async () => {
    // Basic validation
    if (!title.trim() && !text.trim() && mediaPreviews.length === 0) {
      alert("Add some content or an image!");
      return;
    }

    // Validate postTopic is required
    if (!selectedFlair) {
      alert("Please select a flair/topic for your post!");
      return;
    }

    // 1. Prepare the JSON part exactly as your API wants
    const postRequest = {
      content: title.trim() + ". " + text.trim(),
      postTopic: selectedFlair,
      location: "New York", // make dynamic later if you want
    };

    // 2. Create FormData
    const formData = new FormData();

    // // Add the JSON as a string (exactly like your curl -F "postRequest={\"content\":...}")
    // formData.append("postRequest", JSON.stringify(postRequest));// Instead of JSON.stringify → send as Blob with correct type
    const jsonBlob = new Blob([JSON.stringify(postRequest)], {
      type: "application/json",
    });
    formData.append("postRequest", jsonBlob);

    // 3. Add all selected images (your API accepts multiple files in one field)
    // We need the actual File objects, not just base64 previews
    // → We'll store them when user selects files
    selectedFiles.length > 0 &&
      selectedFiles.forEach((file) => {
        formData.append("mediaFile", file); // name must be "mediaFile"
      });

    medias &&
      medias
        .filter((media) => {
          return !mediaPreviews.includes(media.mediaURL);
        })
        .forEach((media) => {
          formData.append("deleteFile", media.id);
        });
    try {
      const response = await PostApi.updateOrCreatePost(postId, formData);

      if (response.ok) {
        const result = await response.json();
        console.log("Posted successfully!", result);
        alert("Posted!");

        // Reset + close modal
        setTitle("");
        setText("");
        setMediaPreviews([]);
        setSelectedFiles([]); // important!
        setSelectedFlair(null);
        onClose();
        // Trigger feed refresh
        window.dispatchEvent(new CustomEvent("postCreated"));
      } else {
        const errorText = await response.text();
        console.error("Post failed:", response.status, errorText);
        alert("Failed to post: " + response.status);
      }
    } catch (err) {
      console.error("Network error:", err);
      alert("Network error – is your backend running?");
    }
  };
  const handleBackdropClick = (e) => {
    if (e.target === e.currentTarget) onClose();
  };

  return (
    <>
      <div className="modal-backdrop" onClick={handleBackdropClick} />

      <div className="modal-container">
        <div className="modal-header">
          <h2>Create a post</h2>
          <button className="modal-close-btn" onClick={onClose}>
            ×
          </button>
        </div>

        <div className="modal-body">
          <input
            type="text"
            placeholder="Title"
            className="modal-title-input"
            value={title}
            onInput={(e) => setTitle(e.target.value)}
          />

          <textarea
            placeholder="What do you want to talk about?"
            className="modal-textarea"
            value={text}
            rows="6"
            onInput={(e) => setText(e.target.value)}
          />
          {/* ====== FLAIR SELECTOR (NEW!) ====== */}
          <div className="flair-selector" style={{ margin: "12px 0" }}>
            <label>Flair</label>
            <div>
              {[
                "Health",
                "Discussion",
                "Study",
                "Travel",
                "Food",
                "Entertainment",
              ].map((flair) => {
                const isSelected = selectedFlair === flair;

                return (
                  <button
                    key={flair}
                    onClick={() => setSelectedFlair(isSelected ? null : flair)}
                    className="flair-option"
                    style={{
                      fontWeight: isSelected ? "bold" : "normal",
                    }}
                  >
                    {isSelected && "✓ "} {flair}
                  </button>
                );
              })}
              {selectedFlair && (
                <button
                  className="flair-option-delete"
                  onClick={() => setSelectedFlair(null)}
                >
                  Clear
                </button>
              )}
            </div>
          </div>
          <div className="upload-media">
            <Carousel
              showArrows={true}
              showThumbs={false}
              showStatus={false}
              selectedItem={currentSlide}
              onChange={setCurrentSlide}
            >
              {mediaPreviews.map((media, index) => (
                <div className="image-container image-preview-container">
                  {media.startsWith("data:image/") ? (
                    <>
                      <div
                        className="background-image"
                        style={{ backgroundImage: `url(${media})` }}
                      ></div>
                      <img src={media} alt="" />
                      <button
                        className="remove-image-btn"
                        onClick={() => removeImage(index)}
                      >
                        <svg
                          rpl=""
                          fill="white"
                          height="16"
                          icon-name="delete"
                          viewBox="0 0 20 20"
                          width="16"
                          xmlns="http://www.w3.org/2000/svg"
                        >
                          {" "}
                          <path d="M15.2 15.7c0 .83-.67 1.5-1.5 1.5H6.3c-.83 0-1.5-.67-1.5-1.5V7.6H3v8.1C3 17.52 4.48 19 6.3 19h7.4c1.82 0 3.3-1.48 3.3-3.3V7.6h-1.8v8.1zM17.5 5.8c.5 0 .9-.4.9-.9S18 4 17.5 4h-3.63c-.15-1.68-1.55-3-3.27-3H9.4C7.68 1 6.28 2.32 6.13 4H2.5c-.5 0-.9.4-.9.9s.4.9.9.9h15zM7.93 4c.14-.68.75-1.2 1.47-1.2h1.2c.72 0 1.33.52 1.47 1.2H7.93z"></path>
                        </svg>
                      </button>
                    </>
                  ) : (
                    <>
                      <video
                        controls
                        style={{ maxWidth: "100%", height: "auto" }}
                      >
                        <source src={media} />
                        Your browser does not support the video tag.
                      </video>
                      <button
                        className="remove-image-btn"
                        onClick={() => removeMedia(index)}
                      >
                        <svg
                          fill="white"
                          height="16"
                          viewBox="0 0 20 20"
                          width="16"
                          xmlns="http://www.w3.org/2000/svg"
                        >
                          <path d="M15.2 15.7c0 .83-.67 1.5-1.5 1.5H6.3c-.83 0-1.5-.67-1.5-1.5V7.6H3v8.1C3 17.52 4.48 19 6.3 19h7.4c1.82 0 3.3-1.48 3.3-3.3V7.6h-1.8v8.1zM17.5 5.8c.5 0 .9-.4.9-.9S18 4 17.5 4h-3.63c-.15-1.68-1.55-3-3.27-3H9.4C7.68 1 6.28 2.32 6.13 4H2.5c-.5 0-.9.4-.9.9s.4.9.9.9h15zM7.93 4c.14-.68.75-1.2 1.47-1.2h1.2c.72 0 1.33.52 1.47 1.2H7.93z"></path>
                        </svg>
                      </button>
                    </>
                  )}
                </div>
              ))}
            </Carousel>

            {/* Image Upload Area */}
            {mediaPreviews.length < MAX_IMAGES && (
              <div
                className={`image-upload-area ${mediaPreviews.length > 0 && "upload-btn"}`}
              >
                <label className="image-upload-label">
                  <input
                    type="file"
                    accept="image/*,video/*"
                    onChange={handleFileChange}
                    style={{ display: "none" }}
                  />
                  {mediaPreviews.length > 0 ? (
                    <div className="upload-placeholder">
                      <svg
                        rpl=""
                        fill="currentColor"
                        height="16"
                        icon-name="gallery"
                        viewBox="0 0 20 20"
                        width="16"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        {" "}
                        <path d="M15.7 5H15v-.7C15 2.48 13.52 1 11.7 1H4.3C2.48 1 1 2.48 1 4.3v7.4C1 13.52 2.48 15 4.3 15H5v.7C5 17.52 6.48 19 8.3 19h7.4c1.82 0 3.3-1.48 3.3-3.3V8.3C19 6.48 17.52 5 15.7 5zM4.3 13.2c-.83 0-1.5-.67-1.5-1.5V4.3c0-.83.67-1.5 1.5-1.5h7.4c.83 0 1.5.67 1.5 1.5V5H8.3C6.48 5 5 6.48 5 8.3v4.9h-.7zm2.69 3.22l2.24-2.24c.5-.5 1.31-.5 1.81 0l3.03 3.02H8.3c-.57 0-1.05-.32-1.31-.78zm10.21-.72c0 .56-.32 1.05-.78 1.31l-4.1-4.1a3.09 3.09 0 00-4.36 0L6.8 14.07V8.31c0-.83.67-1.5 1.5-1.5h7.4c.83 0 1.5.67 1.5 1.5v7.39z"></path>
                        <path d="M14 11.5a1.5 1.5 0 100-3 1.5 1.5 0 000 3z"></path>{" "}
                      </svg>
                      <p>Add</p>
                    </div>
                  ) : (
                    <div className="upload-placeholder">
                      <svg
                        width="48"
                        height="48"
                        viewBox="0 0 24 24"
                        fill="#878a8c"
                      >
                        <path d="M14,2H6A2,2 0 0,0 4,4V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V8L14,2M18,20H6V4H13V9H18V20Z" />
                      </svg>
                      <p>
                        Drag and drop an image/video or <span>browse</span>
                      </p>
                    </div>
                  )}
                </label>
              </div>
            )}
          </div>
        </div>

        <div className="modal-footer">
          <button className="modal-cancel-btn" onClick={handleCancel}>
            Cancel
          </button>
          <button
            className="modal-post-btn"
            onClick={handlePost}
            disabled={
              !title.trim() && !text.trim() && selectedFiles.length === 0
            }
          >
            Post
          </button>
        </div>
      </div>
      {!canAddImage && !canAddVideo && (
        <div style={{ textAlign: "center", color: "#666", marginTop: "16px" }}>
          {currentMediaType === "video"
            ? "Maximum 1 video reached."
            : `Maximum ${MAX_IMAGES} images reached.`}
        </div>
      )}
    </>
  );
}

export default Modal;
