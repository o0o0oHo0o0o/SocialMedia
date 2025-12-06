
import React, { useEffect, useState } from 'react';
import AuthPage from './pages/AuthPage';
import Dashboard from './pages/Dashboard';
import Messenger from './pages/Messenger';
import OAuth2Callback from './pages/OAuth2Callback';
import GalaxyBackground from './components/Background/GalaxyBackground';
import './styles/base.css';
import './styles/responsive.css';
import api from './services/api';

export default function App() {
  const [isDark, setIsDark] = useState(true);
  const [currentPage, setCurrentPage] = useState('auth'); // 'auth' | 'dashboard' | 'messenger' | 'oauth2-callback'

  const handleLogout = () => {
    setCurrentPage('auth');
  };

  const handleAuthSuccess = () => {
    setCurrentPage('dashboard');
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
        setCurrentPage('dashboard');
      }
    })();
    return () => { mounted = false; };
  }, []);

  return (
    <div className={`app ${isDark ? 'dark' : 'light'}`}>
      <GalaxyBackground isDark={isDark} />
      <div className="app-content">
        {currentPage === 'auth' ? (
          <AuthPage
            isDark={isDark}
            onToggleDark={() => setIsDark(!isDark)}
            onAuthSuccess={handleAuthSuccess}
          />
        ) : currentPage === 'oauth2-callback' ? (
          <OAuth2Callback onAuthSuccess={handleAuthSuccess} />
        ) : currentPage === 'dashboard' ? (
          <Dashboard />
        ) : (
          <Messenger onBack={() => setCurrentPage('dashboard')} />
        )}
      </div>
    </div>
  );
}
