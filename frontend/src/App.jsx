import React, { useEffect, useState } from "react";
import FeedPage from "./Test";
import Post from "./Post.jsx";
import Header from "./Searchbar.jsx";
import Sidebar from "./Sidebar.jsx";

function App() {
  const [currentView, setCurrentView] = useState("home"); // 'feed' hoặc 'post'
  const [selectedPost, setSelectedPost] = useState(null);
  const [isDark, setIsDark] = useState(false);

  useEffect(() => {
    setIsDark(localStorage.getItem("theme") == "dark");
  }, []);
  // Function passed down to children
  const openPost = (post) => {
    setSelectedPost(post);
    setCurrentView("post");
    window.scrollTo(0, 0); // optional: scroll to top
  };

  const goBackToFeed = () => {
    setCurrentView("home");
    setSelectedPost(null);
  };

  return (
    <div className={`app grid ${isDark ? "dark" : "light"}`}>
      <Header userId={1}></Header>
      <Sidebar
        isDark={isDark}
        setIsDark={setIsDark}
        currentView={currentView}
        setCurrentView={setCurrentView}
      ></Sidebar>
      {currentView !== "post" && (
        <FeedPage userId={1} currentView={currentView} openPost={openPost} />
      )}

      {currentView === "post" && (
        <Post userId={1} post={selectedPost} goBack={goBackToFeed} />
      )}
      <div></div>
    </div>
  );
}

export default App;
