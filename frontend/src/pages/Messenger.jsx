import React, { useEffect, useMemo, useRef, useState, useCallback } from 'react';
import '../styles/chat.css';
import './Messenger/Messenger.css';
import api from '../services/api';
import { sendTyping } from '../services/chatSocket';
import ChatDrawer from '../components/Chat/ChatDrawer';
import MessengerMain from './Messenger/MessengerMain';
import { useConversationMembers } from '../hooks/useConversationMedia';
import { useChatMessages } from '../hooks/useChatMessagesClean';
import { CONFIG } from '../config/constants';
import { formatTime } from '../utils/format';
import LogoutDialog from '../components/Chat/Dialogs/LogoutDialog';
import ProfileDialog from '../components/Chat/Dialogs/ProfileDialog';
import UserInfoDialog from '../components/Chat/Dialogs/UserInfoDialog';
import NicknameDialog from '../components/Chat/Dialogs/NicknameDialog';
import useConversations from '../hooks/useConversations';
import useConversationActions from '../hooks/useConversationActions';
import useRecording from '../hooks/useRecording';

const MAX_FILE_SIZE = 5 * 1024 * 1024;

export default function Messenger({ onBack }) {
  const [conversationId, setConversationId] = useState('');
  const { conversations, setConversations, activeConv, setActiveConv, addConversation, refreshConversations } = useConversations();
  const [messages, setMessages] = useState([]);
  const [me, setMe] = useState(null);
  const [content, setContent] = useState('');
  const [files, setFiles] = useState([]);
  const replacedTempIdsRef = useRef(new Set()); // Track replaced temp IDs to prevent duplicate replace
  const [msgLoading, setMsgLoading] = useState(false);
  const [hasMoreMessages, setHasMoreMessages] = useState(true);
  const msgContainerRef = useRef(null);
  const prevLastMessageIdRef = useRef(null);
  const initialLoadRef = useRef(true);
  const isPrependingRef = useRef(false);
  const loadMoreObserverRef = useRef(null);
  const USE_SENTINEL = true;
  const currentPageRef = useRef(1); // Track current page (1-based, matches backend)
  const scrollLoadingRef = useRef(false); // Debounce scroll load trigger
  const typingTimers = useRef({}); // Track typing timers per userId
  const [typingUsers, setTypingUsers] = useState([]); // Track multiple typing users: [{ username, userId }]
  const [readReceipts, setReadReceipts] = useState({}); // Track read status by messageId
  const [logoutOpen, setLogoutOpen] = useState(false);
  const [meOpen, setMeOpen] = useState(false);
  const [userInfoOpen, setUserInfoOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [imagePreview, setImagePreview] = useState(null); // For modal preview
  const [newNickname, setNewNickname] = useState('');
  const [nicknameModalOpen, setNicknameModalOpen] = useState(false);
  const { isRecording, recordingTime, startRecording, stopRecording, cancelRecording } = useRecording();
  const [drawerOpen, setDrawerOpen] = useState(false); // Panel bên phải
  const [drawerRefreshKey, setDrawerRefreshKey] = useState(0); // Force drawer refresh on socket events

  // Hook that manages socket/typing helpers — parent (Messenger) is single source of truth for messages
  const handleReactionEvent = useCallback((payload) => {
    try {
      setMessages(prev => prev.map(m => {
        const msgObj = m?.payload ? m.payload : m;
        const id = msgObj?.messageId || m?.messageId;
        if (!id || Number(id) !== Number(payload?.messageId)) return m;
        // normalize existing reactions from possible locations
        const existing = msgObj?.reactions || msgObj?.interactableItem?.reactions || [];
        let next = Array.isArray(existing) ? [...existing] : [];
        const userId = payload?.userId;
        const action = payload?.action;
        const reactionType = payload?.reactionType || null;
        if (action === 'REMOVED') {
          next = next.filter(r => Number(r.userId) !== Number(userId));
        } else if (action === 'UPDATED') {
          next = next.map(r => (Number(r.userId) === Number(userId) ? { ...r, reactionType } : r));
        } else if (action === 'ADDED') {
          next.push({ userId: payload.userId, username: payload.username, fullName: payload.fullName || payload.username, avatarUrl: payload.avatarUrl || null, reactionType });
        }
        const newMsgObj = { ...msgObj, reactions: next };
        return m?.payload ? { ...m, payload: newMsgObj } : newMsgObj;
      }));
    } catch (e) { console.error('[Messenger] handleReactionEvent failed', e); }
  }, []);

  const handleReadReceipt = useCallback((payload) => {
    if (!payload) return;
    const messageId = payload?.messageId || payload?.lastMessageId;
    if (!messageId) return;
    const key = String(messageId);
    setReadReceipts(prev => ({ ...prev, [key]: payload }));
    setMessages(prev => prev.map(m => {
      const id = (m?.payload?.messageId || m?.messageId);
      if (!id || String(id) !== key) return m;
      const updatedPayload = m?.payload ? { ...m.payload, isRead: true, isDelivered: true } : { ...m, isRead: true, isDelivered: true };
      return m?.payload ? { ...m, payload: updatedPayload } : updatedPayload;
    }));
  }, []);

  const chatHooks = useChatMessages({
    activeConv,
    me,
    wsBase: CONFIG.API_BASE_URL,
    onMessage: (incoming) => {
      try {
        // Normalize id and dedupe before appending
        const incomingId = incoming?.payload?.messageId || incoming?.messageId;
        setMessages(prev => {
          if (!incomingId) return [...prev, incoming];
          const exists = prev.some(m => (m?.payload?.messageId || m?.messageId) === incomingId);
          if (exists) return prev;
          return [...prev, incoming];
        });
      } catch (e) { console.error('[Messenger] onMessage handler failed', e); }
    }
    ,
    onReaction: handleReactionEvent
    ,
    onReadReceipt: handleReadReceipt
  });

  useEffect(() => {
    if (!chatHooks) return;
    if (typeof chatHooks.msgLoading !== 'undefined') setMsgLoading(chatHooks.msgLoading);
    if (typeof chatHooks.hasMoreMessages !== 'undefined') setHasMoreMessages(chatHooks.hasMoreMessages);
    if (chatHooks.typingUsers) setTypingUsers(chatHooks.typingUsers);
  }, [chatHooks]);

  // Scroll helper: only scroll if user near bottom or when forced
  const scrollToBottom = useCallback((force = false, cb) => {
    const el = msgContainerRef.current;
    if (!el) {
      if (typeof cb === 'function') cb();
      return;
    }
    const distanceFromBottom = el.scrollHeight - (el.scrollTop + el.clientHeight);
    if (!force && distanceFromBottom > 200) {
      if (typeof cb === 'function') cb();
      return;
    }
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        try { el.scrollTop = el.scrollHeight; } catch (e) { /* noop */ }
        if (typeof cb === 'function') cb();
      });
    });
  }, []);

  // Auto-scroll to newest message when the last message changes (append/new message)
  useEffect(() => {
    try {
      if (!messages || messages.length === 0) {
        prevLastMessageIdRef.current = null;
        return;
      }
      if (isPrependingRef.current) return; // don't auto-scroll when we're prepending older messages

      const last = messages[messages.length - 1];
      const lastId = last?.messageId || last?.payload?.messageId || null;
      // If last message changed (newer message appended), scroll to bottom if user near bottom
      if (prevLastMessageIdRef.current !== lastId) {
        scrollToBottom(false);
      }
      prevLastMessageIdRef.current = lastId;
    } catch (e) { /* noop */ }
  }, [messages, scrollToBottom]);

  // Conversation actions (rename, nickname updates)
  const { handleRenameGroup, handleNicknameUpdated } = useConversationActions({ activeConv, setActiveConv, setConversations, setDrawerRefreshKey });

  // Load members for active conversation so we can show nicknames on messages
  const { members: convMembers, fetchMembers: fetchConvMembers, resetMembers: resetConvMembers } = useConversationMembers(activeConv?.conversationId);

  const membersMap = useMemo(() => {
    const map = {};
    (convMembers || []).forEach(m => { if (m?.userId) map[m.userId] = m; });
    return map;
  }, [convMembers]);

  const conversationRecipient = useMemo(() => {
    if (!convMembers || !me) return null;
    const others = convMembers.filter(member => Number(member.userId) !== Number(me.id));
    return others.length > 0 ? others[0] : null;
  }, [convMembers, me]);

  // Determine whether this conversation should be treated as a group
  const isGroup = useMemo(() => {
    try {
      if (activeConv?.isGroupChat) return true;
      if (convMembers && convMembers.length > 2) return true;
    } catch (e) { /* noop */ }
    return false;
  }, [activeConv, convMembers]);

  useEffect(() => {
    if (activeConv?.conversationId) {
      fetchConvMembers();
    } else {
      resetConvMembers();
    }
  }, [activeConv?.conversationId, fetchConvMembers, resetConvMembers]);

  // Reset last-message ref and force-scroll when switching conversations
  useEffect(() => {
    prevLastMessageIdRef.current = null;
    // Do NOT auto-scroll here; auto-scroll is handled centrally in the messages effect.
  }, [activeConv?.conversationId]);

  const handleMediaLoaded = useCallback((msgId) => {
    try {
      const container = msgContainerRef.current;
      if (!container) return;
      const last = messages[messages.length - 1];
      const lastId = last?.messageId || last?.payload?.messageId || null;
      const nearBottom = (container.scrollHeight - (container.scrollTop + container.clientHeight)) < 200;
      if (msgId === lastId || nearBottom) {
        requestAnimationFrame(() => {
          requestAnimationFrame(() => { try { container.scrollTop = container.scrollHeight; } catch (e) { } });
        });
      }
    } catch (e) { /* noop */ }
  }, [messages]);

  // --- Scroll restoration: store previous scroll height before loading more ---
  const prevScrollHeightRef = useRef(0);

  // --- Load more messages with scroll restoration ---
  const loadMoreMessages = async () => {
    if (msgLoading || !activeConv || !hasMoreMessages) return;
    // Store scroll height before loading
    if (msgContainerRef.current) {
      prevScrollHeightRef.current = msgContainerRef.current.scrollHeight;
    }
    setMsgLoading(true);
    isPrependingRef.current = true;
    try {
      const nextPage = currentPageRef.current + 1;
      const newMsgs = await api.getMessages(activeConv.conversationId, 20, nextPage);
      const newList = Array.isArray(newMsgs) ? newMsgs : (newMsgs?.items || []);
      if (!newList || newList.length < 20) {
        setHasMoreMessages(false);
      }
      if (newList && newList.length > 0) {
        setMessages(prev => {
          const existingIds = new Set(prev.map(m => m?.messageId || m?.payload?.messageId));
          const dedupedList = newList
            .map(m => (m?.payload ? m : { payload: m, type: 'MESSAGE' }))
            .filter(m => {
              const msgId = m?.messageId || m?.payload?.messageId;
              return !existingIds.has(msgId);
            });
          return [...dedupedList, ...prev];
        });
        currentPageRef.current = nextPage;
      }
    } catch (e) {
      console.error(e);
      setHasMoreMessages(false);
    } finally {
      setMsgLoading(false);
      // isPrependingRef.current will be reset in useLayoutEffect
    }
  };

  // --- useLayoutEffect: restore scroll position after prepending messages ---
  React.useLayoutEffect(() => {
    if (isPrependingRef.current && msgContainerRef.current) {
      const container = msgContainerRef.current;
      const newScrollHeight = container.scrollHeight;
      const oldScrollHeight = prevScrollHeightRef.current;
      const diff = newScrollHeight - oldScrollHeight;
      container.scrollTop = diff;
      isPrependingRef.current = false;
    }
  }, [messages]);




  // fetch current user to identify my bubbles

  useEffect(() => {
    (async () => { try { const m = await api.me(); setMe(m); } catch (e) { } })();
  }, []);

  // conversations are managed by useConversations hook





  // Manage sentinel observer lifecycle: clean up on conversation change/unmount
  useEffect(() => {
    return () => {
      try {
        if (loadMoreObserverRef.current?.observer) loadMoreObserverRef.current.observer.disconnect();
      } catch (e) { /* noop */ }
      try {
        if (loadMoreObserverRef.current?.sentinel) loadMoreObserverRef.current.sentinel.remove();
      } catch (e) { /* noop */ }
      loadMoreObserverRef.current = null;
    };
  }, [activeConv?.conversationId]);

  // Track which messages have been marked as read to avoid duplicate API calls
  const markedAsReadRef = useRef(0); // Track the highest messageId that has been marked as read
  const markReadDebounceRef = useRef(null); // Debounce timer for mark-read API
  const pendingMarkReadRef = useRef(0); // Pending messageId to mark as read

  // Intersection Observer: Mark messages as read when they become visible (debounced)
  useEffect(() => {
    const messageObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting && entry.target.dataset.messageId) {
          const messageId = parseInt(entry.target.dataset.messageId);
          const isFromOther = entry.target.dataset.isMe === 'false';

          // Only process if from other user and higher than already marked
          if (isFromOther && messageId > markedAsReadRef.current) {
            // Update pending to the max visible messageId
            if (messageId > pendingMarkReadRef.current) {
              pendingMarkReadRef.current = messageId;
            }

            // Debounce: wait 500ms after user stops scrolling to call API
            if (markReadDebounceRef.current) {
              clearTimeout(markReadDebounceRef.current);
            }
            markReadDebounceRef.current = setTimeout(() => {
              if (pendingMarkReadRef.current > markedAsReadRef.current && activeConv?.conversationId) {
                const msgIdToMark = pendingMarkReadRef.current;
                markedAsReadRef.current = msgIdToMark; // Update marked ref
                api.markChatRead({
                  conversationId: activeConv.conversationId,
                  lastMessageId: msgIdToMark
                }).catch(e => console.error('[IntersectionObserver] Mark read failed:', e));
                console.log('[MarkRead] Debounced call for messageId:', msgIdToMark);
              }
            }, 500);
          }
        }
      });
    }, { root: msgContainerRef.current, threshold: 0.5 });

    // Observe all messages with data-message-id attribute
    if (msgContainerRef.current) {
      const messageElements = msgContainerRef.current.querySelectorAll('[data-message-id]');
      messageElements.forEach(el => messageObserver.observe(el));
    }

    return () => {
      messageObserver.disconnect();
      if (markReadDebounceRef.current) {
        clearTimeout(markReadDebounceRef.current);
      }
    };
  }, [activeConv?.conversationId, messages]);

  // Handle Escape key to close image preview
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape' && imagePreview) {
        setImagePreview(null);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [imagePreview]);

  const onPickFiles = (e) => {
    const list = Array.from(e.target.files || []);
    const tooBig = list.find(f => f.size > MAX_FILE_SIZE);
    if (tooBig) { alert(`File quá lớn (> 5MB): ${tooBig.name}`); return; }
    setFiles(list);
  };

  const handleAddConversationFromInput = () => {
    const id = Number(conversationId);
    if (!id) return;
    const exists = conversations.find(c => c.conversationId === id);
    const conv = exists || { conversationId: id, conversationName: `Conversation ${id}`, isGroupChat: false, unreadCount: 0 };
    if (!exists) addConversation(conv);
    setActiveConv(conv);
  };

  // fetch messages when active conversation changes (but ONLY after WS is ready)
  useEffect(() => {
    (async () => {
      if (!activeConv?.conversationId) return;

      // Clear replaced tempIds when conversation changes (memory cleanup)
      replacedTempIdsRef.current.clear();

      try {
        setHasMoreMessages(true);
        currentPageRef.current = 1;
        // Load only 20 messages for initial load
        const msgs = await api.getMessages(activeConv.conversationId, 20, 1);
        const msgList = Array.isArray(msgs) ? msgs : (msgs?.items || []);

        // Deduplicate and normalize
        const seen = new Set();
        const unique = msgList.filter(m => {
          const id = m?.payload?.messageId || m?.messageId;
          if (seen.has(id)) return false;
          seen.add(id);
          return true;
        }).map(m => (m?.payload ? m : { payload: m, type: 'MESSAGE' }));

        const reversedMessages = unique.reverse();
        setMessages(reversedMessages);

        // Check if there are more messages to load (compare with 20)
        if (unique.length < 20) {
          setHasMoreMessages(false);
        }

        // Robust post-layout scroll: scroll, then scroll again after layout settles, then create sentinel
        scrollToBottom(true, () => {
          setTimeout(() => {
            scrollToBottom(true);
            initialLoadRef.current = false;

            if (loadMoreObserverRef.current) {
              try { loadMoreObserverRef.current.observer.disconnect(); } catch (e) { }
              try { loadMoreObserverRef.current.sentinel.remove(); } catch (e) { }
              loadMoreObserverRef.current = null;
            }

            if (USE_SENTINEL && msgContainerRef.current) {
              const sentinel = document.createElement('div');
              sentinel.id = 'load-more-sentinel';
              msgContainerRef.current.insertBefore(sentinel, msgContainerRef.current.firstChild);
              const observer = new IntersectionObserver(async (entries) => {
                if (initialLoadRef.current) return;
                if (entries[0]?.isIntersecting && hasMoreMessages && !msgLoading && !scrollLoadingRef.current) {
                  scrollLoadingRef.current = true;
                  await loadMoreMessages();
                  scrollLoadingRef.current = false;
                }
              }, { root: msgContainerRef.current, threshold: 0.1 });
              observer.observe(sentinel);
              loadMoreObserverRef.current = { observer, sentinel };
            }
          }, 60);
        });
      } catch (e) { /* noop */ }
    })();
  }, [activeConv?.conversationId]);

  const handleStopRecording = async () => {
    try {
      const file = await stopRecording();
      if (file) setFiles(prev => [...prev, file]);
    } catch (e) {
      console.error('[Messenger] stopRecording failed', e);
    }
  };

  const handleCancelRecording = () => {
    try {
      cancelRecording();
    } catch (e) {
      console.error('[Messenger] cancelRecording failed', e);
    }
  };

  // Use shared formatTime from utils

  const myTypingTimerRef = useRef(null); // Timer for sending typing=false
  const connected = chatHooks?.connected;
  const onContentChange = useCallback((e) => {
    const val = typeof e === 'string' ? e : e.target.value;
    setContent(val);
    if (!activeConv || !me || !connected) {
      return; // Need me object
    }
    sendTyping(activeConv.conversationId, true, me.id, me.username);
    if (myTypingTimerRef.current) clearTimeout(myTypingTimerRef.current);
    myTypingTimerRef.current = setTimeout(() => {
      sendTyping(activeConv.conversationId, false, me.id, me.username);
    }, 1200);
  }, [activeConv, me, connected]);

  // Send message via API (files + json payload). WS will deliver the message back via subscription.
  const sendMessage = async () => {
    if (!activeConv || !activeConv.conversationId) return;
    const payload = { conversationId: activeConv.conversationId, content: content || '' };
    const jsonData = JSON.stringify(payload);
    try {
      await api.sendChatMessage({ jsonData, files });
      setContent('');
      setFiles([]);
    } catch (e) {
      console.error('[Messenger] sendMessage failed', e);
      alert('Gửi tin nhắn thất bại: ' + (e?.message || e));
    }
  };

  const onTextareaKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  // Older loadMoreMessages implementation removed; using top-level `loadMoreMessages` with useLayoutEffect

  const handleSaveNickname = async () => {
    if (!newNickname.trim()) {
      alert('Vui lòng nhập biệt danh');
      return;
    }

    try {
      const response = await api.put(`/chat/${activeConv.conversationId}/nickname`, {
        nickname: newNickname.trim()
      });

      alert(response?.message || 'Cập nhật biệt danh thành công');
      setNicknameModalOpen(false);
      setNewNickname('');

      // Refresh conversation data to show updated nickname
      const list = await api.getConversations();
      setConversations(Array.isArray(list) ? list : (list?.items || []));
    } catch (e) {
      console.error('Error updating nickname:', e);
      alert('Lỗi: ' + (e?.response?.data?.message || e.message || 'Cập nhật biệt danh thất bại'));
    }
  };

  const onMessagesScroll = (e) => {
    const container = e.target;

    // Only load when scroll to TOP for old messages
    const isAtTop = container.scrollTop <= 50;

    // If sentinel-based infinite scroll is enabled, skip manual onScroll loading
    if (USE_SENTINEL) return;

    if (isAtTop && !msgLoading && hasMoreMessages && !scrollLoadingRef.current) {
      scrollLoadingRef.current = true;
      console.log('[Chat] Scroll to top detected, loading more...');
      loadMoreMessages().finally(() => {
        scrollLoadingRef.current = false;
      });
    }
  };

  // Render
  return (
    <>
      <div className="chat-global-actions">
        <button className="chat-action-btn chat-back-btn" title="Quay lại Feed" onClick={onBack}>
          <svg fill="currentColor" height="20" viewBox="0 0 24 24" width="20" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z" />
          </svg>
          Feed
        </button>
        <button className="chat-action-btn" title="Đăng xuất" onClick={() => setLogoutOpen(true)}>🚪 Đăng xuất</button>
      </div>
      <MessengerMain
        conversations={conversations}
        activeConv={activeConv}
        setActiveConv={setActiveConv}
        me={me}
        conversationId={conversationId}
        setConversationId={setConversationId}
        handleAddConversationFromInput={handleAddConversationFromInput}
        messages={messages}
        typingUsers={typingUsers}
        msgLoading={msgLoading}
        msgContainerRef={msgContainerRef}
        onMessagesScroll={onMessagesScroll}
        setSelectedUser={setSelectedUser}
        setUserInfoOpen={setUserInfoOpen}
        content={content}
        onContentChange={onContentChange}
        onTextareaKeyDown={onTextareaKeyDown}
        files={files}
        isRecording={isRecording}
        recordingTime={formatTime(recordingTime)}
        startRecording={startRecording}
        stopRecording={handleStopRecording}
        cancelRecording={handleCancelRecording}
        onPickFiles={onPickFiles}
        removeFile={(i) => setFiles(prev => prev.filter((_, idx) => idx !== i))}
        sendMessage={sendMessage}
        setDrawerOpen={setDrawerOpen}
        drawerOpen={drawerOpen}
        readReceipts={readReceipts}
        conversationRecipient={conversationRecipient}
      />

      {/* Dialogs */}
      <LogoutDialog
        open={logoutOpen}
        onClose={() => setLogoutOpen(false)}
        onConfirm={async () => {
          const done = await api.logout();
          try { localStorage.removeItem('autoLogin'); } catch (e) { }
          setLogoutOpen(false);
          if (done) {
            if (typeof onBack === 'function') onBack();
            window.location.href = '/';
          }
        }}
      />

      <ProfileDialog open={meOpen} onClose={() => setMeOpen(false)} me={me} />

      <UserInfoDialog
        open={userInfoOpen}
        onClose={() => setUserInfoOpen(false)}
        selectedUser={selectedUser}
      />

      {/* Image Preview Modal */}
      {imagePreview && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(0, 0, 0, 0.9)',
            zIndex: 1000,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '20px'
          }}
          onClick={() => setImagePreview(null)}
        >
          <div style={{ position: 'relative', maxWidth: '90vw', maxHeight: '90vh', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <img
              src={imagePreview}
              alt="Preview"
              style={{
                maxWidth: '90vw',
                maxHeight: '85vh',
                borderRadius: '12px',
                objectFit: 'contain',
                boxShadow: '0 8px 32px rgba(0, 0, 0, 0.5)'
              }}
              onClick={e => e.stopPropagation()}
            />
            <button
              onClick={() => setImagePreview(null)}
              style={{
                position: 'absolute',
                top: '-50px',
                right: '0',
                background: 'rgba(255, 255, 255, 0.2)',
                color: 'white',
                border: 'none',
                borderRadius: '50%',
                width: '40px',
                height: '40px',
                fontSize: '24px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                transition: 'background 0.2s'
              }}
              onMouseEnter={e => e.target.style.background = 'rgba(255, 255, 255, 0.3)'}
              onMouseLeave={e => e.target.style.background = 'rgba(255, 255, 255, 0.2)'}
              title="Close preview"
            >
              ✕
            </button>
            <div style={{ marginTop: '12px', fontSize: '12px', color: 'rgba(255, 255, 255, 0.7)', textAlign: 'center' }}>
              Click to close or press Escape
            </div>
          </div>
        </div>
      )}

      {/* Chat Drawer - Panel bên phải */}
      <ChatDrawer
        open={drawerOpen}
        conversationId={activeConv?.conversationId}
        onClose={() => setDrawerOpen(false)}
        isGroupChat={activeConv?.isGroupChat || false}
        groupName={activeConv?.conversationName || ''}
        refreshKey={drawerRefreshKey}
        onRenameGroup={handleRenameGroup}
        onNicknameUpdated={handleNicknameUpdated}
      />
    </>
  );
}

