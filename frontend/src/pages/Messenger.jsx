import React, { useEffect, useMemo, useRef, useState } from 'react';
import '../styles/chat.css';
import Button from '../components/Common/Button';
import Dialog from '../components/Common/Dialog';
import api from '../services/api';
import { connectSocket, disconnectSocket, getClient, subscribeConversation, sendTyping } from '../services/chatSocket';

const MAX_FILE_SIZE = 5 * 1024 * 1024;

// Audio Player Component with waveform visualization
const AudioPlayer = ({ audioUrl, fileSize, fileName, isMe }) => {
  const audioRef = useRef(null);
  const canvasRef = useRef(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const animationRef = useRef(null);

  useEffect(() => {
    const audio = audioRef.current;
    if (!audio) return;

    const handlePlay = () => setIsPlaying(true);
    const handlePause = () => setIsPlaying(false);
    const handleLoadedMetadata = () => setDuration(audio.duration);

    const handleTimeUpdate = () => {
      setCurrentTime(audio.currentTime);
    };

    audio.addEventListener('play', handlePlay);
    audio.addEventListener('pause', handlePause);
    audio.addEventListener('loadedmetadata', handleLoadedMetadata);
    audio.addEventListener('timeupdate', handleTimeUpdate);

    return () => {
      audio.removeEventListener('play', handlePlay);
      audio.removeEventListener('pause', handlePause);
      audio.removeEventListener('loadedmetadata', handleLoadedMetadata);
      audio.removeEventListener('timeupdate', handleTimeUpdate);
      if (animationRef.current) cancelAnimationFrame(animationRef.current);
    };
  }, []);

  // Draw 20 thin waveform bars
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const bars = 20; // 20 vạch mỏng
    const barWidth = canvas.width / bars;

    const barColor = isMe ? 'rgba(255, 255, 255, 0.9)' : 'rgba(138, 180, 248, 0.95)';
    const barBgColor = isMe ? 'rgba(255, 255, 255, 0.2)' : 'rgba(138, 180, 248, 0.25)';
    const progressColor = isMe ? 'rgba(255, 255, 255, 1)' : 'rgba(138, 180, 248, 1)';

    const draw = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      const progress = duration > 0 ? currentTime / duration : 0;
      const progressX = progress * canvas.width;

      for (let i = 0; i < bars; i++) {
        const x = i * barWidth + 0.5;
        const barRight = x + barWidth;

        // Vẽ nền vạch
        ctx.fillStyle = barBgColor;
        ctx.fillRect(x, 0, barWidth - 1, canvas.height);

        // Chiều cao vạch - chạy bình thường theo audio (đơn giản hơn)
        const heightMultiplier = isPlaying
          ? Math.abs(Math.sin((currentTime + i * 0.1) * 3)) * 0.5 + 0.3
          : 0.3;

        // Vẽ vạch - phần đã phát sáng hơn
        if (barRight <= progressX) {
          ctx.fillStyle = progressColor;
          ctx.globalAlpha = 1;
        } else {
          ctx.fillStyle = barColor;
          ctx.globalAlpha = isPlaying ? 0.85 : 0.7;
        }

        const barHeight = canvas.height * heightMultiplier;
        ctx.fillRect(x, (canvas.height - barHeight) / 2, barWidth - 1, barHeight);
      }

      ctx.globalAlpha = 1;
      animationRef.current = requestAnimationFrame(draw);
    };

    animationRef.current = requestAnimationFrame(draw);

    return () => {
      if (animationRef.current) cancelAnimationFrame(animationRef.current);
    };
  }, [isPlaying, currentTime, duration, isMe]);

  const togglePlay = () => {
    if (audioRef.current) {
      if (isPlaying) {
        audioRef.current.pause();
      } else {
        audioRef.current.play();
      }
    }
  };

  // Format time as M:SS
  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const displayTime = formatTime(currentTime);

  const getDisplaySize = () => {
    if (!fileSize || typeof fileSize !== 'number' || fileSize <= 0) {
      return '';
    }
    if (fileSize >= 1024 * 1024) {
      return (fileSize / (1024 * 1024)).toFixed(1) + ' MB';
    }
    return (fileSize / 1024).toFixed(0) + ' KB';
  };

  return (
    <div style={{
      padding: '14px 18px',
      background: isMe ? 'rgba(255,255,255,0.12)' : 'rgba(255,255,255,0.08)',
      borderRadius: '22px',
      display: 'flex',
      alignItems: 'center',
      gap: '12px',
      minWidth: '300px',
      maxWidth: '360px'
    }}>
      <audio ref={audioRef} src={audioUrl} style={{ display: 'none' }} title={fileName} />

      {/* Play Button */}
      <button
        onClick={togglePlay}
        style={{
          background: isMe ? 'rgba(255,255,255,0.25)' : 'rgba(138, 180, 248, 0.45)',
          border: 'none',
          width: '42px',
          height: '42px',
          borderRadius: '50%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: 'pointer',
          fontSize: '18px',
          flexShrink: 0,
          transition: 'all 0.2s',
          color: isMe ? '#fff' : '#8ab4f8'
        }}
        onMouseEnter={(e) => e.target.style.background = isMe ? 'rgba(255,255,255,0.35)' : 'rgba(138, 180, 248, 0.6)'}
        onMouseLeave={(e) => e.target.style.background = isMe ? 'rgba(255,255,255,0.25)' : 'rgba(138, 180, 248, 0.45)'}
      >
        {isPlaying ? '⏸' : '▶'}
      </button>

      {/* Waveform - 20 vạch mỏng */}
      <canvas
        ref={canvasRef}
        width={130}
        height={36}
        style={{
          flex: 1,
          background: isMe ? 'rgba(0, 0, 0, 0.15)' : 'rgba(0, 0, 0, 0.25)',
          borderRadius: '10px',
          cursor: 'pointer'
        }}
        onClick={(e) => {
          const canvas = canvasRef.current;
          const rect = canvas.getBoundingClientRect();
          const x = e.clientX - rect.left;
          const percent = x / canvas.width;
          if (audioRef.current && audioRef.current.duration) {
            audioRef.current.currentTime = percent * audioRef.current.duration;
          }
        }}
      />

      {/* Time Display - 0:00 format */}
      <span style={{
        fontSize: '12px',
        opacity: 0.8,
        whiteSpace: 'nowrap',
        minWidth: '32px',
        textAlign: 'right',
        fontWeight: '500',
        fontFamily: 'monospace'
      }}>
        {displayTime}
      </span>
    </div>
  );
}; export default function Messenger({ onBack }) {
  const [conversationId, setConversationId] = useState('');
  const [conversations, setConversations] = useState([]);
  const [activeConv, setActiveConv] = useState(null);
  const [messages, setMessages] = useState([]);
  const [me, setMe] = useState(null);
  const [content, setContent] = useState('');
  const [files, setFiles] = useState([]);
  const [connected, setConnected] = useState(false);
  const [wsReady, setWsReady] = useState(false); // Ensure WS is fully ready
  const [wsBase, setWsBase] = useState('http://localhost:8080');
  const subRef = useRef(null);
  const replacedTempIdsRef = useRef(new Set()); // Track replaced temp IDs to prevent duplicate replace
  const [msgLoading, setMsgLoading] = useState(false);
  const [hasMoreMessages, setHasMoreMessages] = useState(true);
  const msgContainerRef = useRef(null);
  const currentPageRef = useRef(1); // Track current page (1-based, matches backend)
  const scrollLoadingRef = useRef(false); // Debounce scroll load trigger
  const typingTimer = useRef(null);
  const [typingUser, setTypingUser] = useState(null); // Track user typing: { username, avatar, userId }
  const [readReceipts, setReadReceipts] = useState({}); // Track read status by messageId
  const [logoutOpen, setLogoutOpen] = useState(false);
  const [meOpen, setMeOpen] = useState(false);
  const [userInfoOpen, setUserInfoOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [imagePreview, setImagePreview] = useState(null); // For modal preview
  const [isRecording, setIsRecording] = useState(false);
  const [recordingTime, setRecordingTime] = useState(0);
  const mediaRecorderRef = useRef(null);
  const audioChunksRef = useRef([]);
  const recordingTimerRef = useRef(null);
  const [menuOpen, setMenuOpen] = useState(false); // Menu 3 chấm
  const [nicknameModalOpen, setNicknameModalOpen] = useState(false);
  const [newNickname, setNewNickname] = useState('');

  const wsUrl = useMemo(() => {
    const isHttps = wsBase.startsWith('https://');
    const scheme = isHttps ? 'wss://' : 'ws://';
    const rest = wsBase.replace(/^https?:\/\//, '');
    return `${scheme}${rest}/ws`;
  }, [wsBase]);

  // fetch current user to identify my bubbles
  useEffect(() => {
    (async () => { try { const m = await api.me(); setMe(m); } catch (e) { } })();
  }, []);

  // fetch conversations list on mount
  useEffect(() => {
    (async () => {
      try {
        const list = await api.getConversations();
        setConversations(Array.isArray(list) ? list : (list?.items || []));
        if (!activeConv && Array.isArray(list) && list.length) {
          setActiveConv(list[0]);
        }
      } catch (e) { /* noop */ }
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        // Fetch WS token first, then connect with header
        const tok = await api.getWebSocketToken();
        if (tok?.token) {
          try {
            await connectSocket(wsUrl, {
              onConnect: () => { if (mounted) setConnected(true); },
              onError: (err) => { console.error('[WebSocket] Connection error:', err); },
              connectHeaders: { 'X-WS-TOKEN': tok.token }
            });
          } catch (wsErr) {
            console.error('[WebSocket] Failed to connect:', wsErr);
            // Don't crash - continue without WebSocket
          }
        }
      } catch (e) {
        console.error('[WS Token] Failed to get token:', e);
      }
    })();
    return () => { mounted = false; disconnectSocket(); };
  }, [wsUrl]);

  useEffect(() => {
    if (!connected || !activeConv) return;
    if (subRef.current) {
      try { subRef.current.unsubscribe(); } catch (e) { /* noop */ }
      subRef.current = null;
    }

    setWsReady(false); // Reset flag when subscribing to new conversation

    // Retry subscribe với delay để đảm bảo STOMP connection đã sẵn sàng
    let retryCount = 0;
    const maxRetries = 5;
    const trySubscribe = () => {
      try {
        const result = subscribeConversation(activeConv.conversationId, (evt) => {
          const type = evt?.type;
          if (type === 'TYPING') {
            // TypingPayload: { conversationId, username, userId, isTyping }
            const isTyping = Boolean(evt?.payload?.isTyping);
            const username = evt?.payload?.username;
            const userId = evt?.payload?.userId;

            // Skip if this is typing from current user (check by userId if available, else by username)
            const isFromMe = me && (
              (userId && userId === me.id) ||
              (username && username === me.username)
            );

            if (isFromMe) {
              return; // Skip own typing
            }

            if (isTyping && username) {
              // Show typing indicator
              setTypingUser({ username, userId, typing: true });
              if (typingTimer.current) clearTimeout(typingTimer.current);
              typingTimer.current = setTimeout(() => setTypingUser(null), 5000);
              // Auto scroll to bottom
              setTimeout(() => {
                if (msgContainerRef.current) {
                  msgContainerRef.current.scrollTop = msgContainerRef.current.scrollHeight;
                }
              }, 100);
            } else {
              // Hide typing indicator
              setTypingUser(null);
              if (typingTimer.current) clearTimeout(typingTimer.current);
            }
            return;
          }
          if (type === 'READ_RECEIPT') {
            // Update read status for message
            const messageId = evt?.payload?.messageId;
            const readBy = evt?.payload?.user;
            console.log('[Socket] Read receipt:', messageId, readBy);
            if (messageId) {
              setReadReceipts(prev => ({
                ...prev,
                [messageId]: { readBy, readAt: evt?.payload?.readAt }
              }));
              // Update messages to mark as read (both payload and root level)
              setMessages(prev => prev.map(m => {
                const mId = m?.payload?.messageId || m?.messageId;
                if (mId && mId <= messageId) {
                  if (m.payload) {
                    return { ...m, isRead: true, payload: { ...m.payload, isRead: true } };
                  }
                  return { ...m, isRead: true };
                }
                return m;
              }));
            }
            return;
          }
          try {
            // Get message ID and sender ID
            const msgId = evt?.payload?.messageId || evt?.messageId;
            const senderId = evt?.payload?.sender?.userId || evt?.sender?.userId;

            // Skip if this is a message from the current user (already handled by API response)
            if (me && senderId && Number(senderId) === Number(me.id)) {
              return; // Skip own messages - already added via API response
            }

            setMessages(prev => {
              // Check if message already exists to prevent duplicates
              const exists = prev.some(m => {
                const existId = m?.payload?.messageId || m?.messageId;
                return existId === msgId;
              });

              if (exists) {
                return prev; // Skip duplicate
              }

              // Normalize socket event to wrapper format
              let newMessage;
              if (evt?.payload) {
                // Already wrapped
                newMessage = {
                  ...evt,
                  payload: {
                    ...evt.payload,
                    isRead: evt.payload?.isRead ?? false,
                    isDelivered: evt.payload?.isDelivered ?? false,
                    status: undefined // Remove sending status when received from server
                  }
                };
              } else {
                // Flat structure - wrap it
                newMessage = {
                  payload: {
                    ...evt,
                    isRead: evt.isRead ?? false,
                    isDelivered: evt.isDelivered ?? false,
                    status: undefined
                  },
                  type: 'MESSAGE'
                };
              }
              return [...prev, newMessage]; // Append to end - newest at bottom
            });
            // Auto scroll to bottom when new message arrives
            setTimeout(() => {
              if (msgContainerRef.current) {
                msgContainerRef.current.scrollTop = msgContainerRef.current.scrollHeight;
              }
            }, 100);
            // Auto mark as read when receiving new message
            if (activeConv?.conversationId && msgId) {
              setTimeout(async () => {
                try {
                  await api.markChatRead({
                    conversationId: activeConv.conversationId,
                    lastMessageId: msgId
                  });
                } catch (e) {
                  console.error('[Socket] Failed to mark as read:', e);
                }
              }, 300);
            }
          } catch (e) {
            console.error('[Messenger] Error processing message:', e);
          }
        });

        if (result) {
          subRef.current = result;
          setWsReady(true); // ✅ Set ready when subscription successful
          console.log('[Messenger] Subscribed successfully to conversation:', activeConv.conversationId);
        } else if (retryCount < maxRetries) {
          // Retry sau 200ms
          retryCount++;
          console.log('[Messenger] Subscribe returned null, retrying... (' + retryCount + '/' + maxRetries + ')');
          setTimeout(trySubscribe, 200);
        }
      } catch (e) {
        console.error('[Messenger] Failed to subscribe:', e);
        if (retryCount < maxRetries) {
          retryCount++;
          console.log('[Messenger] Retrying subscribe... (' + retryCount + '/' + maxRetries + ')');
          setTimeout(trySubscribe, 200);
        }
      }
    };

    // Delay ban đầu 100ms để STOMP connection ổn định
    setTimeout(trySubscribe, 100);
  }, [connected, activeConv, wsReady, me]);

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

  const addConversation = () => {
    // Deprecated: now we load from backend; keep as fallback if ID typed
    const id = Number(conversationId);
    if (!id) return;
    const exists = conversations.find(c => c.conversationId === id);
    const conv = exists || { conversationId: id, conversationName: `Conversation ${id}`, isGroupChat: false, unreadCount: 0 };
    if (!exists) setConversations(prev => [conv, ...prev]);
    setActiveConv(conv);
  };

  // fetch messages when active conversation changes (but ONLY after WS is ready)
  useEffect(() => {
    (async () => {
      if (!activeConv?.conversationId) return;

      // Clear replaced tempIds when conversation changes (memory cleanup)
      replacedTempIdsRef.current.clear();

      // Wait for websocket to be ready before loading messages
      // This ensures socket listeners are ready to receive new messages
      if (!wsReady) {
        console.log('[Chat] Waiting for websocket to be ready...');
        return;
      }

      try {
        setHasMoreMessages(true); // Reset hasMore flag
        currentPageRef.current = 1; // Reset to page 1
        // Fetch with larger limit initially to ensure we get all recent messages including newly sent ones
        // Backend uses page (1-based) not offset
        const msgs = await api.getMessages(activeConv.conversationId, 100, 1);
        const msgList = Array.isArray(msgs) ? msgs : (msgs?.items || []);
        console.log('[Chat] API response type:', Array.isArray(msgs) ? 'array' : 'object', 'length:', msgList?.length);

        // Deduplicate by messageId and normalize to wrapper format
        const seen = new Set();
        const unique = msgList.filter(m => {
          const id = m?.payload?.messageId || m?.messageId;
          if (seen.has(id)) return false;
          seen.add(id);
          return true;
        }).map(m => {
          // Normalize to wrapper format if it's flat
          if (m?.payload) {
            return m; // Already wrapped
          } else {
            return { payload: m, type: 'MESSAGE' }; // Wrap flat structure
          }
        });

        // Backend now returns DESC (newest -> oldest)
        // Newest messages are first in array, need to reverse to show oldest first (for scroll)
        const reversedMessages = unique.reverse();
        setMessages(reversedMessages);

        console.log('[Chat] Initial load:', unique.length, 'messages');

        // Check if there are more messages to load
        if (unique.length < 100) {
          setHasMoreMessages(false);
          console.log('[Chat] No more old messages (got', unique.length, 'messages)');
        }

        // Scroll to bottom after loading
        setTimeout(() => {
          if (msgContainerRef.current) {
            msgContainerRef.current.scrollTop = msgContainerRef.current.scrollHeight;
          }
        }, 100);

        // Mark messages as read when viewing conversation
        // Get last message ID from reversed array (last element)
        if (reversedMessages.length > 0) {
          const lastMsg = reversedMessages[reversedMessages.length - 1];
          const lastMsgId = lastMsg?.messageId || lastMsg?.payload?.messageId;
          if (lastMsgId) {
            try {
              await api.markChatRead({
                conversationId: activeConv.conversationId,
                lastMessageId: lastMsgId
              });
              console.log('[Chat] Marked as read up to messageId:', lastMsgId);
            } catch (error) {
              console.error('[Chat] Failed to mark as read:', error);
            }
          }
        }
      } catch (e) { /* noop */ }
    })();
  }, [activeConv, wsReady]);

  const sendMessage = async () => {
    if (!activeConv) return;
    const payload = { conversationId: activeConv.conversationId, content: content || '', replyToMessageId: null };

    // Create temporary ID for optimistic update
    const tempId = `temp_${Date.now()}_${Math.random()}`;
    const tempMessage = {
      messageId: tempId,
      payload: {
        messageId: tempId,
        conversationId: activeConv.conversationId,
        content: content || '',
        sender: me,
        senderId: me?.id,
        sentAt: new Date().toISOString(),
        isRead: false,
        isDelivered: false,
        status: 'SENDING' // Mark as sending state
      },
      status: 'SENDING'
    };

    // Add to UI immediately (optimistic)
    setMessages(prev => [...prev, tempMessage]);

    // Clear input immediately
    setContent('');
    setFiles([]);
    try { const input = document.getElementById('chat-files'); if (input) input.value = ''; } catch (e) { /* noop */ }

    // Auto scroll to bottom after sending
    setTimeout(() => {
      if (msgContainerRef.current) {
        msgContainerRef.current.scrollTop = msgContainerRef.current.scrollHeight;
      }
    }, 100);

    try {
      const res = await api.sendChatMessage({ jsonData: JSON.stringify(payload), files });

      console.log('[Send] Response:', res);
      console.log('[Send] Temp ID to replace:', tempId);
      console.log('[Send] Response ID:', res?.messageId);

      // Replace temp message with real one from server
      if (res && res.messageId) {
        // Guard: skip if already replaced (prevent React.StrictMode double-replace)
        if (replacedTempIdsRef.current.has(tempId)) {
          console.log('[Send] Already replaced tempId', tempId, '- skipping duplicate replace');
          return;
        }
        replacedTempIdsRef.current.add(tempId); // Mark as replaced

        setMessages(prev => {
          console.log('[Send] Before replace, messages count:', prev.length);
          const updated = prev.map(m => {
            const mId = m?.messageId || m?.payload?.messageId;
            // Replace temp message with server response
            if (mId === tempId) {
              // Normalize API response to wrapper format
              // Ensure senderId is present so socket can skip it later
              const normalized = {
                payload: res.payload || res,
                type: res.type || 'MESSAGE',
                senderId: res?.sender?.userId || res?.senderId || me?.id // Add senderId for socket dedup
              };
              // Also set in payload if not present
              if (!normalized.payload.senderId && me?.id) {
                normalized.payload.senderId = me.id;
              }
              console.log('[Send] Replaced temp', tempId, 'with real', res.messageId, 'senderId:', normalized.senderId);
              return normalized;
            }
            return m;
          });
          console.log('[Send] After replace, messages count:', updated.length);
          return updated;
        });
      }
    } catch (e) {
      // Remove message from UI if sending failed
      setMessages(prev => {
        return prev.filter(m => {
          const mId = m?.messageId || m?.payload?.messageId;
          return mId !== tempId;
        });
      });

      console.error('[Send] Failed:', e);
      alert(`Gửi tin nhắn không thành công: ${e.message}`);
    }
  };

  const markRead = async () => {
    if (!activeConv) return;
    try {
      const last = messages.find(m => m?.messageId) || null;
      await api.markChatRead({ conversationId: activeConv.conversationId, lastMessageId: last?.messageId || null });
    } catch (e) { /* noop */ }
  };

  const startRecording = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const mediaRecorder = new MediaRecorder(stream);
      audioChunksRef.current = [];

      mediaRecorder.ondataavailable = (event) => {
        audioChunksRef.current.push(event.data);
      };

      mediaRecorder.onstop = () => {
        const audioBlob = new Blob(audioChunksRef.current, { type: 'audio/mp3' });
        const audioFile = new File([audioBlob], `audio_${Date.now()}.mp3`, { type: 'audio/mp3' });
        setFiles(prev => [...prev, audioFile]);
        stream.getTracks().forEach(track => track.stop());
      };

      mediaRecorderRef.current = mediaRecorder;
      mediaRecorder.start();
      setIsRecording(true);
      setRecordingTime(0);

      // Start timer
      recordingTimerRef.current = setInterval(() => {
        setRecordingTime(t => t + 1);
      }, 1000);

      console.log('[Recording] Started');
    } catch (error) {
      console.error('[Recording] Error accessing microphone:', error);
      alert('Không thể truy cập microphone. Vui lòng cho phép quyền truy cập.');
    }
  };

  const stopRecording = () => {
    if (mediaRecorderRef.current && isRecording) {
      mediaRecorderRef.current.stop();
      setIsRecording(false);
      if (recordingTimerRef.current) {
        clearInterval(recordingTimerRef.current);
      }
      console.log('[Recording] Stopped');
    }
  };

  const cancelRecording = () => {
    if (mediaRecorderRef.current && isRecording) {
      mediaRecorderRef.current.stop();
      setIsRecording(false);
      setRecordingTime(0);
      if (recordingTimerRef.current) {
        clearInterval(recordingTimerRef.current);
      }
      audioChunksRef.current = [];
      const stream = mediaRecorderRef.current.stream;
      if (stream) stream.getTracks().forEach(track => track.stop());
      console.log('[Recording] Cancelled');
    }
  };

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const onContentChange = (e) => {
    setContent(e.target.value);
    console.log('[Typing] onChange called, activeConv:', !!activeConv, 'connected:', connected, 'me:', !!me);
    if (!activeConv || !connected || !me) {
      console.log('[Typing] Early return - missing condition');
      return; // Need me object
    }
    console.log('[Typing] onContentChange triggered, sending typing=true');
    sendTyping(activeConv.conversationId, true, me.id, me.username);
    if (typingTimer.current) clearTimeout(typingTimer.current);
    typingTimer.current = setTimeout(() => {
      console.log('[Typing] 1200ms timeout, sending typing=false');
      sendTyping(activeConv.conversationId, false, me.id, me.username);
    }, 1200);
  };

  const onTextareaKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const loadMoreMessages = async () => {
    if (msgLoading || !activeConv || !hasMoreMessages) return;
    setMsgLoading(true);

    const container = msgContainerRef.current;
    const prevScrollHeight = container ? container.scrollHeight : 0;

    try {
      // Increment page for next request
      const nextPage = currentPageRef.current + 1;
      console.log('[Chat] Loading more messages - page:', nextPage, 'limit: 30');

      const newMsgs = await api.getMessages(activeConv.conversationId, 30, nextPage);
      const newList = Array.isArray(newMsgs) ? newMsgs : (newMsgs?.items || []);

      console.log('[Chat] Loaded', newList.length, 'messages at page', nextPage);

      // If we got less than 30 OR empty, no more messages to load
      if (!newList || newList.length === 0 || newList.length < 30) {
        console.log('[Chat] No more old messages (got', newList?.length || 0, 'messages)');
        setHasMoreMessages(false);
      }

      if (newList && newList.length > 0) {
        // Deduplicate: filter out messages that already exist
        let actualNewCount = 0;
        setMessages(prev => {
          const existingIds = new Set(
            prev.map(m => m?.messageId || m?.payload?.messageId)
          );

          // Normalize to wrapper format during deduplication
          const dedupedList = newList
            .map(m => {
              // Normalize to wrapper format if flat
              if (m?.payload) {
                return m; // Already wrapped
              } else {
                return { payload: m, type: 'MESSAGE' }; // Wrap flat structure
              }
            })
            .filter(m => {
              const msgId = m?.messageId || m?.payload?.messageId;
              const isDuplicate = existingIds.has(msgId);
              if (isDuplicate) {
                console.log('[Chat] Skipping duplicate message:', msgId);
              } else {
                actualNewCount++;
              }
              return !isDuplicate;
            });

          if (dedupedList.length < newList.length) {
            console.log('[Chat] Deduplicated:', newList.length, '->', dedupedList.length, 'messages');
          }

          // Prepend deduplicated messages - keep all for scrolling
          const updated = [...dedupedList, ...prev];
          console.log('[Chat] Messages now:', updated.length, 'total');
          return updated;
        });

        // If we got mostly duplicates, that means we've reached the limit
        const overlapRatio = (newList.length - actualNewCount) / newList.length;
        if (overlapRatio > 0.5 && actualNewCount > 0) {
          console.log('[Chat] High duplicate ratio:', overlapRatio, '- likely reached the beginning');
          setHasMoreMessages(false);
        }

        // Update page counter
        currentPageRef.current = nextPage;
        console.log('[Chat] Updated page to:', currentPageRef.current, '(added', actualNewCount, 'unique messages)');

        // Don't restore scroll position - let it flow naturally
        // When prepending messages, the browser will keep visual position stable
        // Trying to manually restore causes jump issues
        console.log('[Chat] Messages loaded, scroll will auto-adjust naturally');
      }
    } catch (e) {
      console.error('Load more messages failed:', e);
      setHasMoreMessages(false);
      // Safety: reset restoration flag if error
      isRestoringScrollRef.current = false;
    } finally {
      setMsgLoading(false);
    }
  };

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

    if (isAtTop && !msgLoading && hasMoreMessages && !scrollLoadingRef.current) {
      scrollLoadingRef.current = true;
      console.log('[Chat] Scroll to top detected, loading more...');
      loadMoreMessages().finally(() => {
        scrollLoadingRef.current = false;
      });
    }
  }; return (
    <>
      <div className="chat-global-actions">
        <button className="chat-action-btn" title="Đăng xuất" onClick={() => setLogoutOpen(true)}>🚪 Đăng xuất</button>
      </div>
      <div className="chat-layout">
        <aside className="chat-sidebar">
          <div className="chat-sidebar-header">
            <div className="chat-sidebar-user">
              <div className="chat-avatar chat-avatar-me" title="Thông tin của tôi" onClick={() => setMeOpen(true)}>
                {me?.fullName?.[0] || me?.username?.[0] || 'M'}
              </div>
              <div className="chat-sidebar-title">Đoạn chat</div>
            </div>
          </div>
          <div className="chat-search"><input placeholder="Tìm kiếm trên Messenger" /></div>
          <div className="chat-list">
            {conversations.length === 0 ? (
              <div className="chat-empty">Thêm Conversation ID để bắt đầu</div>
            ) : conversations.map(c => (
              <div key={c.conversationId} className={`chat-item ${activeConv?.conversationId === c.conversationId ? 'active' : ''}`} onClick={() => setActiveConv(c)}>
                <div className="chat-avatar">{(c.conversationName || 'C')[0]}</div>
                <div>
                  <div className="chat-title">{c.conversationName || `Conversation ${c.conversationId}`}</div>
                  <div className="chat-preview">{c.lastMessage?.content || 'Bắt đầu trò chuyện'}</div>
                </div>
                <div className="chat-time">{c.lastMessage?.sentAt ? new Date(c.lastMessage.sentAt).toLocaleTimeString() : ''}</div>
                {c.unreadCount ? <div className="chat-unread">{c.unreadCount}</div> : null}
              </div>
            ))}
          </div>
        </aside>

        <main className="chat-main">
          {activeConv ? (
            <>
              <header className="chat-header">
                <div className="chat-header-left">
                  <div className="chat-avatar">
                    {(activeConv.conversationName || 'C')[0]}
                  </div>
                  <div className="chat-header-title">{activeConv.conversationName || `Conversation ${activeConv.conversationId}`}</div>
                </div>
                <div className="chat-header-actions">
                  <div style={{ position: 'relative' }}>
                    <button
                      className="chat-icon-btn"
                      onClick={() => setMenuOpen(!menuOpen)}
                      title="Tùy chọn"
                      style={{
                        position: 'relative',
                        zIndex: 100
                      }}
                    >
                      ⋯
                    </button>
                    {menuOpen && (
                      <div style={{
                        position: 'absolute',
                        top: '100%',
                        right: 0,
                        background: 'rgba(30, 30, 40, 0.95)',
                        border: '1px solid rgba(255, 255, 255, 0.2)',
                        borderRadius: '8px',
                        minWidth: '200px',
                        boxShadow: '0 4px 12px rgba(0, 0, 0, 0.5)',
                        zIndex: 200,
                        marginTop: '8px',
                        backdropFilter: 'blur(10px)'
                      }}>
                        <button onClick={() => { setMenuOpen(false); }} style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: '10px',
                          padding: '12px 16px',
                          width: '100%',
                          border: 'none',
                          background: 'transparent',
                          color: '#fff',
                          cursor: 'pointer',
                          fontSize: '14px',
                          borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
                          transition: 'background 0.2s'
                        }} onMouseEnter={(e) => e.target.style.background = 'rgba(255, 255, 255, 0.1)'} onMouseLeave={(e) => e.target.style.background = 'transparent'}>
                          ℹ️ Thông tin hộp thoại
                        </button>
                        <button onClick={() => { setMenuOpen(false); }} style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: '10px',
                          padding: '12px 16px',
                          width: '100%',
                          border: 'none',
                          background: 'transparent',
                          color: '#fff',
                          cursor: 'pointer',
                          fontSize: '14px',
                          borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
                          transition: 'background 0.2s'
                        }} onMouseEnter={(e) => e.target.style.background = 'rgba(255, 255, 255, 0.1)'} onMouseLeave={(e) => e.target.style.background = 'transparent'}>
                          🖼️ Ảnh
                        </button>
                        <button onClick={() => { setMenuOpen(false); }} style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: '10px',
                          padding: '12px 16px',
                          width: '100%',
                          border: 'none',
                          background: 'transparent',
                          color: '#fff',
                          cursor: 'pointer',
                          fontSize: '14px',
                          borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
                          transition: 'background 0.2s'
                        }} onMouseEnter={(e) => e.target.style.background = 'rgba(255, 255, 255, 0.1)'} onMouseLeave={(e) => e.target.style.background = 'transparent'}>
                          📁 File
                        </button>
                        <button onClick={() => {
                          setMenuOpen(false);
                          setNicknameModalOpen(true);
                          setNewNickname('');
                        }} style={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: '10px',
                          padding: '12px 16px',
                          width: '100%',
                          border: 'none',
                          background: 'transparent',
                          color: '#fff',
                          cursor: 'pointer',
                          fontSize: '14px',
                          transition: 'background 0.2s'
                        }} onMouseEnter={(e) => e.target.style.background = 'rgba(255, 255, 255, 0.1)'} onMouseLeave={(e) => e.target.style.background = 'transparent'}>
                          ✏️ Chỉnh sửa biệt danh
                        </button>
                      </div>
                    )}
                  </div>
                </div>
              </header>
              <section className="chat-messages" ref={msgContainerRef} onScroll={onMessagesScroll}>
                {msgLoading && <div className="chat-typing">Đang tải...</div>}
                {messages.length === 0 ? (
                  <div className="chat-empty">Chưa có tin nhắn — hãy gửi thử.</div>
                ) : messages.map((m, idx) => {
                  const msg = m && m.payload && m.type !== 'TYPING' ? m.payload : m;
                  const isMe = me && (msg?.sender?.userId === me?.id || msg?.senderId === me?.id);
                  const senderName = msg?.sender?.fullName || msg?.senderName || 'Unknown';
                  const senderInitial = senderName.charAt(0).toUpperCase();
                  const sentAt = msg?.sentAt ? new Date(msg.sentAt) : null;
                  const messageTime = sentAt ? sentAt.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }) : '';

                  // Check if should show timestamp (10 min gap)
                  const prevMsg = idx > 0 ? (messages[idx - 1]?.payload || messages[idx - 1]) : null;
                  const prevTime = prevMsg?.sentAt ? new Date(prevMsg.sentAt) : null;
                  const timeDiff = sentAt && prevTime ? (sentAt - prevTime) / 1000 / 60 : 0; // minutes
                  const showTimestamp = !prevTime || timeDiff >= 10;

                  // Check if previous message is from same sender (for grouping)
                  const prevIsMe = prevMsg && me && (prevMsg?.sender?.userId === me?.id || prevMsg?.senderId === me?.id);
                  const isSameSender = prevMsg && (isMe === prevIsMe);
                  const isConsecutive = isSameSender && timeDiff < 1; // Less than 1 minute = consecutive

                  // Check if next message is from same sender
                  const nextMsg = idx < messages.length - 1 ? (messages[idx + 1]?.payload || messages[idx + 1]) : null;
                  const nextIsMe = nextMsg && me && (nextMsg?.sender?.userId === me?.id || nextMsg?.senderId === me?.id);
                  const nextIsSameSender = nextMsg && (isMe === nextIsMe);
                  const nextTime = nextMsg?.sentAt ? new Date(nextMsg.sentAt) : null;
                  const nextTimeDiff = sentAt && nextTime ? (nextTime - sentAt) / 1000 / 60 : 0;
                  const isLastInGroup = !nextIsSameSender || nextTimeDiff >= 1; // Show avatar only on last message in group

                  // Read status for sent messages
                  const isRead = msg?.isRead || false;
                  const isDelivered = msg?.isDelivered || false;
                  const isSending = msg?.status === 'SENDING' || m?.status === 'SENDING';
                  const isFailed = msg?.status === 'FAILED' || msg?.error;

                  // Find recipient info from current conversation
                  // For 1-1 chats: get the other user's info
                  // For groups: show all read receipts (but UI shows first member)
                  let recipient = null;
                  if (activeConv) {
                    // Try to get recipient from messages sender info
                    // Find a message from someone other than me
                    const otherUserMsg = messages.find(m2 => {
                      const m2Payload = m2?.payload || m2;
                      const senderId = m2Payload?.sender?.userId || m2Payload?.senderId;
                      return senderId && senderId !== me?.id;
                    });

                    if (otherUserMsg) {
                      const otherPayload = otherUserMsg?.payload || otherUserMsg;
                      recipient = otherPayload?.sender || {
                        userId: otherPayload?.senderId,
                        fullName: otherPayload?.senderName,
                        username: otherPayload?.senderName
                      };
                    }
                  }
                  const recipientInitial = recipient?.fullName?.[0] || recipient?.username?.[0] || 'U';

                  // Check if this is the last message from me (for status display)
                  const isLastMyMessage = isMe && (idx === messages.length - 1 ||
                    (messages[idx + 1] && me &&
                      (messages[idx + 1]?.payload?.sender?.userId !== me?.id &&
                        messages[idx + 1]?.senderId !== me?.id)));

                  const handleAvatarClick = () => {
                    if (!isMe) {
                      setSelectedUser({
                        fullName: senderName,
                        username: msg?.sender?.username || 'Unknown',
                        userId: msg?.sender?.userId || msg?.senderId
                      });
                      setUserInfoOpen(true);
                    }
                  };

                  return (
                    <React.Fragment key={`msg-${msg?.messageId || msg?.payload?.messageId}`}>
                      {showTimestamp && sentAt && (
                        <div className="chat-timestamp-divider">
                          {sentAt.toLocaleString('vi-VN', {
                            hour: '2-digit',
                            minute: '2-digit',
                            day: '2-digit',
                            month: '2-digit'
                          })}
                        </div>
                      )}
                      <div className={`chat-bubble ${isMe ? 'me' : 'other'} ${isConsecutive ? 'consecutive' : ''}`}>
                        {!isMe && isLastInGroup && (
                          <div
                            className="chat-bubble-avatar"
                            title={senderName}
                            onClick={handleAvatarClick}
                            style={{ cursor: 'pointer' }}
                          >
                            {senderInitial}
                          </div>
                        )}
                        {!isMe && !isLastInGroup && (
                          <div style={{ width: '32px', flexShrink: 0 }} />
                        )}
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', alignItems: isMe ? 'flex-end' : 'flex-start' }}>
                          {msg?.content && (
                            <div className="chat-bubble-content" title={messageTime}>
                              <span>{typeof msg === 'string' ? msg : (msg?.content || '')}</span>
                            </div>
                          )}
                          {msg?.media && msg.media.length > 0 && (
                            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', maxWidth: '300px' }}>
                              {msg.media.map((m, midx) => (
                                <div key={midx} style={{ position: 'relative', borderRadius: '8px', overflow: 'hidden', maxWidth: '100%' }}>
                                  {m.type === 'IMAGE' ? (
                                    <img src={m.url} alt={m.fileName} style={{ maxWidth: '280px', maxHeight: '300px', borderRadius: '8px', cursor: 'pointer', display: 'block' }}
                                      onClick={() => setImagePreview(m.url)}
                                      title="Click to preview" />
                                  ) : m.type === 'VIDEO' ? (
                                    <video controls style={{ maxWidth: '280px', maxHeight: '300px', borderRadius: '8px' }}>
                                      <source src={m.url} />
                                    </video>
                                  ) : m.type === 'AUDIO' ? (
                                    <AudioPlayer audioUrl={m.url} fileSize={m.fileSize} fileName={m.fileName} isMe={isMe} />
                                  ) : (
                                    <div style={{ padding: '8px 12px', background: '#333', borderRadius: '8px', display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}
                                      onClick={() => window.open(m.url, '_blank')} title="Click to download">
                                      <span style={{ fontSize: '20px' }}>📎</span>
                                      <div>
                                        <div style={{ fontSize: '12px', color: '#fff', fontWeight: 'bold', wordBreak: 'break-word' }}>{m.fileName}</div>
                                        <div style={{ fontSize: '11px', color: '#aaa' }}>
                                          {m.fileSize && typeof m.fileSize === 'number' && m.fileSize > 0
                                            ? (m.fileSize >= 1024 * 1024
                                              ? (m.fileSize / (1024 * 1024)).toFixed(1) + ' MB'
                                              : (m.fileSize / 1024).toFixed(0) + ' KB')
                                            : 'Unknown'
                                          }
                                        </div>
                                      </div>
                                    </div>
                                  )}
                                </div>
                              ))}
                            </div>
                          )}
                          {isMe && (
                            <>
                              {isSending ? (
                                <div style={{ fontSize: '11px', opacity: 0.7, marginTop: '2px' }}>
                                  ⏳ Đang gửi...
                                </div>
                              ) : isFailed ? (
                                <div style={{ fontSize: '11px', color: '#ff4444', marginTop: '2px' }}>
                                  ⚠ Gửi thất bại
                                </div>
                              ) : (idx === messages.length - 1) ? (
                                <div className="chat-read-receipt">
                                  {isRead && recipient ? (
                                    <div className="chat-seen-avatar" title={`${recipient.fullName || recipient.username} đã xem`}>
                                      {recipientInitial}
                                    </div>
                                  ) : isDelivered ? (
                                    <span style={{ fontSize: '11px', opacity: 0.7 }}>Đã nhận</span>
                                  ) : (
                                    <span style={{ fontSize: '11px', opacity: 0.7 }}>Đã gửi</span>
                                  )}
                                </div>
                              ) : null}
                            </>
                          )}
                        </div>
                      </div>
                    </React.Fragment>
                  );
                })}
                {typingUser && (
                  <div style={{ display: 'flex', gap: '8px', alignItems: 'center', marginBottom: '12px', marginLeft: '8px', animation: 'slideIn 0.3s ease-out' }}>
                    <div
                      style={{
                        width: '32px',
                        height: '32px',
                        borderRadius: '50%',
                        background: '#667eea',
                        color: 'white',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '14px',
                        fontWeight: 'bold',
                        flexShrink: 0
                      }}
                      title={typingUser.username}
                    >
                      {typingUser.username?.charAt(0).toUpperCase() || '?'}
                    </div>
                    <div className="chat-typing-indicator">
                      <span className="dot">•</span>
                      <span className="dot">•</span>
                      <span className="dot">•</span>
                    </div>
                  </div>
                )}
              </section>
              <footer className="chat-composer">
                {/* File Preview */}
                {files?.length > 0 && (
                  <div style={{ padding: '12px', borderBottom: '1px solid #333', display: 'flex', gap: '12px', flexWrap: 'wrap', alignItems: 'flex-start' }}>
                    {files.map((f, idx) => {
                      const isImage = f.type.startsWith('image');
                      const isAudio = f.type.startsWith('audio');
                      const objUrl = URL.createObjectURL(f);
                      return (
                        <div key={idx} style={{ position: 'relative', display: 'flex', flexDirection: 'column', alignItems: 'flex-start' }}>
                          <div style={{ borderRadius: '8px', overflow: 'hidden', border: '2px solid #667eea', background: '#1a1a2e', display: 'flex', alignItems: 'center', gap: '8px', padding: '8px 12px', fontSize: '12px' }}>
                            {isImage ? (
                              <>
                                <img src={objUrl} alt={f.name} style={{ width: '50px', height: '50px', borderRadius: '4px', objectFit: 'cover', cursor: 'pointer' }}
                                  onClick={() => setImagePreview(objUrl)}
                                  title="Click to preview" />
                                <div style={{ flex: 1, maxWidth: '150px' }}>
                                  <div style={{ wordBreak: 'break-word', color: '#fff', fontSize: '11px' }}>{f.name}</div>
                                  <div style={{ opacity: 0.7, fontSize: '10px', marginTop: '2px' }}>{(f.size / 1024).toFixed(2)} KB</div>
                                </div>
                              </>
                            ) : isAudio ? (
                              <>
                                <span style={{ fontSize: '24px' }}>🎙️</span>
                                <div style={{ flex: 1, maxWidth: '150px' }}>
                                  <div style={{ wordBreak: 'break-word', color: '#fff', fontSize: '11px' }}>{f.name}</div>
                                  <div style={{ opacity: 0.7, fontSize: '10px', marginTop: '2px' }}>{(f.size / 1024).toFixed(2)} KB</div>
                                </div>
                              </>
                            ) : (
                              <>
                                <span style={{ fontSize: '24px' }}>📎</span>
                                <div style={{ flex: 1, maxWidth: '150px' }}>
                                  <div style={{ wordBreak: 'break-word', color: '#fff', fontSize: '11px' }}>{f.name}</div>
                                  <div style={{ opacity: 0.7, fontSize: '10px', marginTop: '2px' }}>{(f.size / 1024).toFixed(2)} KB</div>
                                </div>
                              </>
                            )}
                          </div>
                          <button
                            onClick={() => setFiles(prev => prev.filter((_, i) => i !== idx))}
                            style={{ marginTop: '6px', background: '#ff4444', color: 'white', border: 'none', borderRadius: '4px', padding: '4px 8px', cursor: 'pointer', fontSize: '12px', fontWeight: 'bold', opacity: 0.9, transition: 'opacity 0.2s' }}
                            onMouseEnter={e => e.target.style.opacity = '1'}
                            onMouseLeave={e => e.target.style.opacity = '0.9'}
                            title="Remove file"
                          >
                            ✕ Remove
                          </button>
                        </div>
                      );
                    })}
                  </div>
                )}

                {/* Recording UI */}
                {isRecording ? (
                  <div style={{ padding: '12px', borderBottom: '1px solid #333', display: 'flex', alignItems: 'center', gap: '12px', background: 'rgba(220, 20, 60, 0.1)', borderRadius: '8px', margin: '0 12px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flex: 1 }}>
                      <span style={{ fontSize: '20px', animation: 'pulse 1s infinite' }}>🔴</span>
                      <span style={{ color: '#ff4444', fontWeight: 'bold', fontSize: '14px' }}>Recording {formatTime(recordingTime)}</span>
                    </div>
                    <button
                      onClick={stopRecording}
                      style={{ background: '#667eea', color: 'white', border: 'none', borderRadius: '20px', padding: '8px 16px', cursor: 'pointer', fontSize: '12px', fontWeight: 'bold' }}
                      title="Stop recording"
                    >
                      ✓ Done
                    </button>
                    <button
                      onClick={cancelRecording}
                      style={{ background: '#ff4444', color: 'white', border: 'none', borderRadius: '20px', padding: '8px 16px', cursor: 'pointer', fontSize: '12px', fontWeight: 'bold' }}
                      title="Cancel recording"
                    >
                      ✕ Cancel
                    </button>
                  </div>
                ) : (
                  <>
                    {/* Input Area */}
                    <div style={{ display: 'flex', gap: '8px', alignItems: 'center', padding: '12px' }}>
                      {/* Emoji/Sticker (placeholder) */}
                      <button
                        onClick={() => { }}
                        style={{ background: 'transparent', border: 'none', color: '#667eea', fontSize: '20px', cursor: 'pointer', padding: '8px' }}
                        title="Emoji & sticker (coming soon)"
                      >
                        😊
                      </button>

                      {/* Textarea */}
                      <textarea
                        className="chat-textarea"
                        placeholder="Nhập tin nhắn..."
                        value={content}
                        onChange={onContentChange}
                        onKeyDown={onTextareaKeyDown}
                        style={{ minHeight: '40px', maxHeight: '120px' }}
                      />

                      {/* File Upload */}
                      <label style={{ position: 'relative', cursor: 'pointer' }}>
                        <input
                          id="chat-files"
                          type="file"
                          multiple
                          onChange={onPickFiles}
                          accept="image/*,.pdf,.doc,.docx,.xls,.xlsx,.zip"
                          style={{ display: 'none' }}
                        />
                        <button
                          as="div"
                          onClick={e => document.getElementById('chat-files').click()}
                          style={{ background: 'transparent', border: 'none', color: '#667eea', fontSize: '20px', cursor: 'pointer', padding: '8px' }}
                          title="Attach file/photo"
                        >
                          📎
                        </button>
                      </label>

                      {/* Voice Recording */}
                      <button
                        onClick={startRecording}
                        style={{ background: 'transparent', border: 'none', color: '#667eea', fontSize: '20px', cursor: 'pointer', padding: '8px' }}
                        title="Record audio message"
                      >
                        🎙️
                      </button>

                      {/* Send Button */}
                      {(content.trim() || files.length > 0) && (
                        <button
                          onClick={sendMessage}
                          style={{
                            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                            color: 'white',
                            border: 'none',
                            borderRadius: '20px',
                            padding: '10px 20px',
                            cursor: 'pointer',
                            fontSize: '14px',
                            fontWeight: 'bold',
                            transition: 'opacity 0.2s'
                          }}
                          onMouseEnter={e => e.target.style.opacity = '0.8'}
                          onMouseLeave={e => e.target.style.opacity = '1'}
                          title="Send message"
                        >
                          Send
                        </button>
                      )}
                    </div>
                  </>
                )}
              </footer>
            </>
          ) : (
            <div className="chat-empty large">Chọn hoặc mở một cuộc trò chuyện</div>
          )}
        </main>
      </div>
      {/* Logout dialog */}
      <Dialog
        open={logoutOpen}
        title="Đăng xuất"
        onClose={() => setLogoutOpen(false)}
        actions={(
          <>
            <button className="dialog-btn" onClick={() => setLogoutOpen(false)}>Huỷ</button>
            <button className="dialog-btn primary" onClick={async () => {
              const done = await api.logout();
              try { localStorage.removeItem('autoLogin'); } catch (e) { }
              setLogoutOpen(false);
              if (done) {
                if (typeof onBack === 'function') onBack();
                window.location.href = '/';
              }
            }}>Đăng xuất</button>
          </>
        )}
      >
        <div>
          Bạn có chắc chắn muốn đăng xuất khỏi tài khoản hiện tại?
        </div>
      </Dialog>

      {/* Me info dialog */}
      <Dialog
        open={meOpen}
        title="Thông tin tài khoản"
        onClose={() => setMeOpen(false)}
        actions={<button className="dialog-btn primary" onClick={() => setMeOpen(false)}>Đóng</button>}
      >
        {me ? (
          <div style={{ display: 'grid', gridTemplateColumns: '120px 1fr', gap: 12, alignItems: 'center' }}>
            <div className="chat-avatar" style={{ width: 80, height: 80, fontSize: 28 }}>
              {me.fullName?.[0] || me.username?.[0] || 'M'}
            </div>
            <div>
              <div style={{ fontWeight: 700, fontSize: 16 }}>{me.fullName || me.username}</div>
              <div style={{ opacity: .8 }}>Username: {me.username}</div>
              {me.email ? <div style={{ opacity: .8 }}>Email: {me.email}</div> : null}
              {me.phone ? <div style={{ opacity: .8 }}>Phone: {me.phone}</div> : null}
            </div>
          </div>
        ) : (
          <div>Chưa tải được thông tin tài khoản.</div>
        )}
      </Dialog>

      {/* User info dialog */}
      <Dialog
        open={userInfoOpen}
        title="Thông tin người dùng"
        onClose={() => setUserInfoOpen(false)}
        actions={<button className="dialog-btn primary" onClick={() => setUserInfoOpen(false)}>Đóng</button>}
      >
        {selectedUser ? (
          <div style={{ display: 'grid', gridTemplateColumns: '80px 1fr', gap: 12, alignItems: 'center' }}>
            <div className="chat-avatar" style={{ width: 60, height: 60, fontSize: 24 }}>
              {selectedUser.fullName?.[0] || selectedUser.username?.[0] || 'U'}
            </div>
            <div>
              <div style={{ fontWeight: 700, fontSize: 16 }}>{selectedUser.fullName}</div>
              <div style={{ opacity: .8 }}>Username: {selectedUser.username}</div>
            </div>
          </div>
        ) : (
          <div>Không có thông tin.</div>
        )}
      </Dialog>

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

      {/* Nickname Edit Modal */}
      {nicknameModalOpen && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(0, 0, 0, 0.7)',
            zIndex: 1001,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '20px'
          }}
          onClick={() => setNicknameModalOpen(false)}
        >
          <div
            style={{
              background: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)',
              borderRadius: '16px',
              padding: '32px',
              minWidth: '300px',
              maxWidth: '500px',
              boxShadow: '0 20px 60px rgba(0, 0, 0, 0.6)',
              border: '1px solid rgba(255, 255, 255, 0.1)'
            }}
            onClick={e => e.stopPropagation()}
          >
            <h3 style={{ margin: '0 0 20px 0', color: '#fff', fontSize: '18px', fontWeight: 600 }}>
              Chỉnh sửa biệt danh
            </h3>

            <input
              type="text"
              placeholder="Nhập biệt danh mới..."
              value={newNickname}
              onChange={(e) => setNewNickname(e.target.value)}
              onKeyPress={(e) => {
                if (e.key === 'Enter') {
                  handleSaveNickname();
                }
              }}
              autoFocus
              style={{
                width: '100%',
                padding: '12px 16px',
                marginBottom: '20px',
                border: '1px solid rgba(255, 255, 255, 0.2)',
                borderRadius: '8px',
                background: 'rgba(255, 255, 255, 0.05)',
                color: '#fff',
                fontSize: '14px',
                outline: 'none',
                transition: 'all 0.2s',
                boxSizing: 'border-box'
              }}
              onFocus={(e) => e.target.style.borderColor = 'rgba(100, 200, 255, 0.5)'}
              onBlur={(e) => e.target.style.borderColor = 'rgba(255, 255, 255, 0.2)'}
            />

            <div style={{
              display: 'flex',
              gap: '12px',
              justifyContent: 'flex-end'
            }}>
              <button
                onClick={() => setNicknameModalOpen(false)}
                style={{
                  padding: '10px 20px',
                  border: 'none',
                  borderRadius: '8px',
                  background: 'rgba(255, 255, 255, 0.1)',
                  color: '#fff',
                  cursor: 'pointer',
                  fontSize: '14px',
                  fontWeight: 500,
                  transition: 'all 0.2s'
                }}
                onMouseEnter={(e) => e.target.style.background = 'rgba(255, 255, 255, 0.15)'}
                onMouseLeave={(e) => e.target.style.background = 'rgba(255, 255, 255, 0.1)'}
              >
                Hủy
              </button>
              <button
                onClick={handleSaveNickname}
                style={{
                  padding: '10px 20px',
                  border: 'none',
                  borderRadius: '8px',
                  background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                  color: '#fff',
                  cursor: 'pointer',
                  fontSize: '14px',
                  fontWeight: 500,
                  transition: 'all 0.2s'
                }}
                onMouseEnter={(e) => e.target.style.transform = 'translateY(-2px)'}
                onMouseLeave={(e) => e.target.style.transform = 'translateY(0)'}
              >
                Lưu
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

