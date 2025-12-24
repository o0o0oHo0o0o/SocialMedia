import React, { useEffect, useState, useRef } from 'react';
import AuthPage from './pages/AuthPage';
import Messenger from './pages/Messenger';
import FeedPage from './pages/FeedPage';
import OAuth2Callback from './pages/OAuth2Callback';
import GalaxyBackground from './components/Background/GalaxyBackground';
import SnowBackground from './components/Chat/SnowBackground';
import './styles/base.css';
import './styles/responsive.css';
import api from './services/api';

// Video call / socket
import { useWebRTC } from './hooks/useWebRTC';
import VideoCallUI from './components/VideoCall/VideoCallUI';
import { connectSocket, disconnectSocket } from './services/chatSocket';
import { CONFIG } from './config/constants';

export default function App() {
  const [isDark, setIsDark] = useState(() => localStorage.getItem('theme') === 'dark');
  const [currentPage, setCurrentPage] = useState('auth');
  const [userId, setUserId] = useState(null);
  const [userMe, setUserMe] = useState(null);

  // State socket & cờ kết nối
  const [stompClient, setStompClient] = useState(null);
  const [isConnected, setIsConnected] = useState(false);

  // Refs quan trọng
  const clientRef = useRef(null);
  const isConnectingRef = useRef(false);
  const signalHandlerRef = useRef(null);

  // WebRTC Hook (Nhận clientRef)
  const {
    localStream, remoteStream, callStatus, remoteUser,
    startCall, acceptCall, rejectCall, endCall, handleIncomingSignal
  } = useWebRTC(clientRef, userMe);

  // Update ref to latest handler so subscription doesn't need to re-subscribe
  useEffect(() => {
    signalHandlerRef.current = handleIncomingSignal;
  }, [handleIncomingSignal]);

  // --- 1. INIT SOCKET ---
  const initSocket = async () => {
    if (clientRef.current && clientRef.current.active) return clientRef.current;
    if (isConnectingRef.current) return;

    isConnectingRef.current = true;
    console.log('[App] Bắt đầu initSocket...');

    const normalize = (base) => {
      if (!base) return null;
      const trimmed = base.trim().replace(/\/+$/, '');
      const isSecure = trimmed.startsWith('https://') || trimmed.startsWith('wss://');
      const scheme = isSecure ? 'wss://' : 'ws://';
      const rest = trimmed.replace(/^(https?:\/\/|ws:\/\/|wss:\/\/)*/, '');
      return `${scheme}${rest}/ws`;
    };

    // Lấy từ CONFIG đã sửa ở Bước 1
    const wsUrl = normalize(CONFIG.API_BASE_URL);
    console.log('[App] Using WebSocket URL from CONFIG:', wsUrl);

    try {
      const tok = await api.getWebSocketToken();
      const client = await connectSocket(wsUrl, {
        connectHeaders: { 'X-WS-TOKEN': tok?.token },
        onConnect: () => {
          console.log('✅ [App] STOMP Connected!');
          isConnectingRef.current = false;
          setIsConnected(true);
        },
        onDisconnect: () => {
          console.log('❌ [App] STOMP Disconnected');
          isConnectingRef.current = false;
          setIsConnected(false);
        },
        onError: (err) => {
          console.error('❌ [App] STOMP Error:', err);
          isConnectingRef.current = false;
          setIsConnected(false);
        }
      });

      clientRef.current = client;
      setStompClient(client);
      return client;
    } catch (e) {
      console.error('❌ [App] Init socket failed:', e);
      isConnectingRef.current = false;
      return null;
    }
  };

  // --- 2. AUTH HANDLERS ---
  const handleAuthSuccess = async () => {
    try {
      const me = await api.me();
      if (me) {
        setUserId(me.userId || me.id);
        setUserMe(me);
      }
    } catch (e) { console.error(e); }
    setCurrentPage('feed');
  };

  const handleLogout = async () => {
    try { await api.logout(); } catch (e) { }
    localStorage.removeItem('autoLogin');

    // Cleanup Socket khi Logout
    if (clientRef.current) {
      try { disconnectSocket(); } catch (e) { }
      clientRef.current = null;
    }
    setStompClient(null);
    setIsConnected(false);

    setUserId(null);
    setUserMe(null);
    setCurrentPage('auth');
  };

  // --- 3. LIFECYCLE: Connect Socket khi có User ---
  useEffect(() => {
    // Chỉ connect nếu đã có user và chưa có socket active
    if (userId && !clientRef.current) {
      initSocket();
    }
    // KHÔNG RETURN CLEANUP ĐỂ TRÁNH NGẮT KẾT NỐI KHI RE-RENDER
  }, [userId]);

  // --- 4. LIFECYCLE: Subscribe Video Call ---
  useEffect(() => {
    if (!isConnected || !clientRef.current || !clientRef.current.active) return;

    console.log('📡 [App] Subscribe Video Call Channel...');
    let sub = null;
    try {
      sub = clientRef.current.subscribe('/user/queue/video-call', (message) => {
        console.log('📞 [App] Signal Received:', message.body);
        try {
          const payload = JSON.parse(message.body);
          if (signalHandlerRef.current) signalHandlerRef.current(payload);
        } catch (e) { console.error(e); }
      });
    } catch (e) { console.error(e); }

    return () => {
      // FIX: Chỉ unsubscribe khi socket còn sống
      if (sub && clientRef.current && (clientRef.current.active || clientRef.current.connected)) {
        try {
          console.log('[App] Unsubscribing Video Call...');
          sub.unsubscribe();
        } catch (e) { /* ignore if socket already closed */ }
      }
    };
  }, [isConnected]);

  // --- 5. CHECK LOGIN STARTUP ---
  useEffect(() => {
    let mounted = true;
    const path = window.location.pathname;
    if (path === '/oauth2/success') { setCurrentPage('oauth2-callback'); return; }

    const shouldAutoLogin = localStorage.getItem('autoLogin') === '1';
    (async () => {
      if (!shouldAutoLogin) return;
      try {
        const me = await api.me();
        if (mounted && me) {
          setUserId(me.userId || me.id);
          setUserMe(me);
          setCurrentPage('feed');
          // Không cần gọi initSocket() ở đây nữa, useEffect[userId] sẽ lo
        }
      } catch (e) { }
    })();
    return () => { mounted = false; };
  }, []);

  useEffect(() => { localStorage.setItem('theme', isDark ? 'dark' : 'light'); }, [isDark]);

  // --- RENDER ---
  const handleNavigateToMessenger = () => setCurrentPage('messenger');
  const handleNavigateToFeed = () => setCurrentPage('feed');

  const renderPage = () => {
    switch (currentPage) {
      case 'auth': return <AuthPage isDark={isDark} onToggleDark={() => setIsDark(!isDark)} onAuthSuccess={handleAuthSuccess} />;
      case 'oauth2-callback': return <OAuth2Callback onAuthSuccess={handleAuthSuccess} />;
      case 'feed':
        return <FeedPage userId={userId} isDark={isDark} setIsDark={setIsDark} onNavigateToMessenger={handleNavigateToMessenger} onLogout={handleLogout} onStartCall={startCall} />;
      case 'messenger':
        return <Messenger onBack={handleNavigateToFeed} onStartVideoCall={startCall} stompClient={stompClient} currentUser={userMe} />;
      default: return null;
    }
  };

  return (
    <div className={`app ${isDark ? 'dark' : 'light'}`}>
      {isDark && <SnowBackground enabled={true} />}
      {currentPage === 'auth' && <GalaxyBackground isDark={isDark} />}
      <div className="app-content">{renderPage()}</div>

      {/* GLOBAL VIDEO CALL UI */}
      <VideoCallUI
        callStatus={callStatus}
        localStream={localStream}
        remoteStream={remoteStream}
        remoteUser={remoteUser}
        onAccept={acceptCall}
        onReject={rejectCall}
        onEndCall={endCall}
      />
    </div>
  );
}