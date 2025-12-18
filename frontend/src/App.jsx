
import React, { useEffect, useState } from 'react';
import AuthPage from './pages/AuthPage';
import Dashboard from './pages/Dashboard';
import Messenger from './pages/Messenger';
import FeedPage from './pages/FeedPage';
import OAuth2Callback from './pages/OAuth2Callback';
import GalaxyBackground from './components/Background/GalaxyBackground';
import './styles/base.css';
import './styles/responsive.css';
import api from './services/api';

export default function App() {
  const [isDark, setIsDark] = useState(() => {
    const saved = localStorage.getItem('theme');
    return saved ? saved === 'dark' : true;
  });
  const [currentPage, setCurrentPage] = useState('auth'); // 'auth' | 'feed' | 'messenger' | 'oauth2-callback'
  const [userId, setUserId] = useState(null);

  const handleLogout = async () => {
    try {
      await api.logout();
    } catch (e) {
      console.error('Logout error:', e);
    }
    localStorage.removeItem('autoLogin');
    setUserId(null);
    setCurrentPage('auth');
  };

  const handleAuthSuccess = async () => {
    // Get user info after login
    const me = await api.me();
    if (me) {
      setUserId(me.userId || me.id);
    }
    setCurrentPage('feed');
  };

  const handleNavigateToMessenger = () => {
    setCurrentPage('messenger');
  };

  const handleNavigateToFeed = () => {
    setCurrentPage('feed');
  };

  // Khi app load, kiểm tra route và session
  useEffect(() => {
    let mounted = true;

    // Kiểm tra nếu đang ở route OAuth2 callback
    const path = window.location.pathname;
    if (path === '/oauth2/success') {
      setCurrentPage('oauth2-callback');
      return;
    }

    // Auto-login nếu đã bật remember me
    const shouldAutoLogin = localStorage.getItem('autoLogin') === '1';
    (async () => {
      if (!shouldAutoLogin) return;
      const me = await api.me();
      if (mounted && me) {
        setUserId(me.userId || me.id);
        setCurrentPage('feed');
      }
    })();
    return () => { mounted = false; };
  }, []);

  // Sync dark mode with localStorage
  useEffect(() => {
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
  }, [isDark]);

  const renderPage = () => {
    switch (currentPage) {
      case 'auth':
        return (
          <AuthPage
            isDark={isDark}
            onToggleDark={() => setIsDark(!isDark)}
            onAuthSuccess={handleAuthSuccess}
          />
        );
      case 'oauth2-callback':
        return <OAuth2Callback onAuthSuccess={handleAuthSuccess} />;
      case 'feed':
        return (
          <FeedPage
            userId={userId}
            isDark={isDark}
            setIsDark={setIsDark}
            onNavigateToMessenger={handleNavigateToMessenger}
            onLogout={handleLogout}
          />
        );
      case 'messenger':
        return <Messenger onBack={handleNavigateToFeed} />;
      default:
        return null;
    }
  };

  return (
    <div className={`app ${isDark ? 'dark' : 'light'}`}>
      {currentPage === 'auth' && <GalaxyBackground isDark={isDark} />}
      <div className="app-content">
        {renderPage()}
      </div>
    </div>
  );
}
