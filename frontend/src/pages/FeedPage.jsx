import React, { useEffect, useState } from "react";
import "react-responsive-carousel/lib/styles/carousel.min.css";
import FeedItem from "../components/Feed/FeedItem";
import Post from "../components/Feed/Post";
import Sidebar from "../components/Common/Sidebar";
import SearchBar from "../components/Common/SearchBar";
import { FeedApi } from "../utils/ultis";
import "../styles/feed.css";

const FeedPage = ({
  userId,
  isDark,
  setIsDark,
  onNavigateToMessenger,
  onLogout,
}) => {
  const [feed, setFeed] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentView, setCurrentView] = useState("home");
  const [selectedPost, setSelectedPost] = useState(null);
  const [refreshKey, setRefreshKey] = useState(0);

  const fetchFeed = async () => {
    setLoading(true);
    try {
      const link = "http://localhost:8080/api/feed/";
      let api;
      switch (currentView) {
        case "home":
          api = link + `home`;
          break;
        case "popular":
          api = link + `popular`;
          break;
        case "discussion":
          api = link + `discussion`;
          break;
        default:
          api = link + `home`;
      }

      const response = await FeedApi.getFeedFrom(api);
      const data = await response.json();
      setFeed(data);
    } catch (error) {
      console.error("There was an error fetching the feed data:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFeed();
  }, [currentView, userId, refreshKey]);

  useEffect(() => {
    const handlePostCreated = () => {
      fetchFeed();
    };
    window.addEventListener('postCreated', handlePostCreated);
    return () => window.removeEventListener('postCreated', handlePostCreated);
  }, [currentView, userId]);

  useEffect(() => {
    const handlePostCreated = () => {
      fetchFeed();
    };
    window.addEventListener('postCreated', handlePostCreated);
    return () => window.removeEventListener('postCreated', handlePostCreated);
  }, [currentView, userId]);

  const handleCreatePost = () => {
    setRefreshKey((prev) => prev + 1);
  };

  const openPost = (post) => {
    setSelectedPost(post);
    setCurrentView("post");
    window.scrollTo(0, 0);
  };

  const goBack = () => {
    setCurrentView("home");
    setSelectedPost(null);
  };

  return (
    <div className={`app grid ${isDark ? "dark" : "light"}`}>
      <SearchBar userId={userId} onCreatePost={handleCreatePost} />
      <Sidebar
        isDark={isDark}
        setIsDark={setIsDark}
        currentView={currentView}
        setCurrentView={setCurrentView}
        onNavigateToMessenger={onNavigateToMessenger}
        onLogout={onLogout}
      />
      {currentView !== "post" ? (
        loading ? (
          <div className="loading">Loading...</div>
        ) : (
          <div className="feed-container">
            {feed.map((post) => (
              <React.Fragment key={post.id}>
                <FeedItem userId={userId} post={post} openPost={openPost} />
                <hr />
              </React.Fragment>
            ))}
          </div>
        )
      ) : (
        <Post userId={userId} post={selectedPost} goBack={goBack} />
      )}
      <div></div>
    </div>
  );
};

export default FeedPage;
