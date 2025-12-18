import React, { memo, useMemo, useCallback } from 'react';
import MessageBubble from './MessageBubble';

/**
 * MessageList - Memoized component for rendering the list of messages
 * Prevents re-render when unrelated state changes (like typing input)
 */
const MessageList = memo(function MessageList({
  messages,
  me,
  activeConv,
  typingUsers,
  msgLoading,
  onAvatarClick,
  onImagePreview,
  onMediaLoad,
  readReceipts,
  recipientOverride
}) {
  // Find recipient info for read receipts
  const recipientFromMessages = useMemo(() => {
    if (!activeConv || !messages.length || !me) return null;

    const otherUserMsg = messages.find(m => {
      const payload = m?.payload || m;
      const senderId = payload?.sender?.userId || payload?.senderId;
      return senderId && senderId !== me.id;
    });

    if (otherUserMsg) {
      const payload = otherUserMsg?.payload || otherUserMsg;
      return payload?.sender || {
        userId: payload?.senderId,
        fullName: payload?.senderName,
        username: payload?.senderName
      };
    }
    return null;
  }, [messages, me, activeConv]);

  const recipient = recipientFromMessages || recipientOverride;

  const recipientInitial = recipient?.fullName?.[0] || recipient?.username?.[0] || 'U';

  const handleAvatarClick = useCallback((user) => {
    if (onAvatarClick) {
      onAvatarClick(user);
    }
  }, [onAvatarClick]);

  if (messages.length === 0) {
    return <div className="chat-empty">Chưa có tin nhắn — hãy gửi thử.</div>;
  }

  return (
    <>
      {msgLoading && <div className="chat-typing">Đang tải...</div>}
      {messages.map((m, idx) => {
        const msg = m && m.payload && m.type !== 'TYPING' ? m.payload : m;
        const msgId = msg?.messageId || msg?.payload?.messageId;
        const isNotification = msg?.messageType === 'NOTIFICATION';
        const isMe = me && (msg?.sender?.userId === me?.id || msg?.senderId === me?.id);

        // Sender name with nickname priority
        const senderNickname = msg?.sender?.nickname || null;
        const senderFullName = msg?.sender?.fullName || msg?.senderName || 'Unknown';
        const senderName = senderNickname || senderFullName;
        const senderInitial = senderName.charAt(0).toUpperCase();
        const sentAt = msg?.sentAt ? new Date(msg.sentAt) : null;
        const messageTime = sentAt ? sentAt.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }) : '';

        // Check if should show timestamp (10 min gap)
        const prevMsg = idx > 0 ? (messages[idx - 1]?.payload || messages[idx - 1]) : null;
        const prevTime = prevMsg?.sentAt ? new Date(prevMsg.sentAt) : null;
        const timeDiff = sentAt && prevTime ? (sentAt - prevTime) / 1000 / 60 : 0;
        const showTimestamp = !prevTime || timeDiff >= 10;

        // Check if previous message is from same sender (for grouping)
        const prevIsMe = prevMsg && me && (prevMsg?.sender?.userId === me?.id || prevMsg?.senderId === me?.id);
        const isSameSender = prevMsg && (isMe === prevIsMe);
        const isConsecutive = isSameSender && timeDiff < 1;

        // Check if next message is from same sender
        const nextMsg = idx < messages.length - 1 ? (messages[idx + 1]?.payload || messages[idx + 1]) : null;
        const nextIsMe = nextMsg && me && (nextMsg?.sender?.userId === me?.id || nextMsg?.senderId === me?.id);
        const nextIsSameSender = nextMsg && (isMe === nextIsMe);
        const nextTime = nextMsg?.sentAt ? new Date(nextMsg.sentAt) : null;
        const nextTimeDiff = sentAt && nextTime ? (nextTime - sentAt) / 1000 / 60 : 0;
        const isLastInGroup = !nextIsSameSender || nextTimeDiff >= 1;

        // Read status
        const isRead = msg?.isRead || false;
        const isDelivered = msg?.isDelivered || false;
        const isSending = msg?.status === 'SENDING' || m?.status === 'SENDING';
        const isFailed = msg?.status === 'FAILED' || msg?.error;

        // Check if this is the last message
        const isLastMessage = idx === messages.length - 1;

        // Use tempId or messageId as key - prefer keeping the same key for optimistic updates
        // If message has _tempId, use that as key to prevent remount when server response arrives
        const keyId = m?._tempId || msgId;
        const readReceipt = msgId ? (readReceipts?.[msgId] ?? readReceipts?.[String(msgId)]) : null;

        return (
          <MessageBubble
            key={`msg-${keyId}`}
            msg={msg}
            msgId={msgId}
            isMe={isMe}
            isNotification={isNotification}
            senderName={senderName}
            senderInitial={senderInitial}
            messageTime={messageTime}
            showTimestamp={showTimestamp}
            sentAt={sentAt}
            isConsecutive={isConsecutive}
            isLastInGroup={isLastInGroup}
            isRead={isRead}
            isDelivered={isDelivered}
            isSending={isSending}
            isFailed={isFailed}
            recipient={recipient}
            recipientInitial={recipientInitial}
            isLastMessage={isLastMessage}
            isGroupChat={activeConv?.isGroupChat}
            onAvatarClick={() => handleAvatarClick({
              fullName: senderName,
              username: msg?.sender?.username || 'Unknown',
              userId: msg?.sender?.userId || msg?.senderId
            })}
            onImagePreview={onImagePreview}
            me={me}
            onMediaLoad={onMediaLoad}
            readReceipt={readReceipt}
          />
        );
      })}

      {messages.map((m, idx) => {
        const msg = m && m.payload && m.type !== 'TYPING' ? m.payload : m;
        const msgId = msg?.messageId || msg?.payload?.messageId;
        const isMe = me && (msg?.sender?.userId === me?.id || msg?.senderId === me?.id);
        const isLastMessage = idx === messages.length - 1;
        const isSending = msg?.status === 'SENDING' || m?.status === 'SENDING';
        const isFailed = msg?.status === 'FAILED' || msg?.error;
        const isRead = msg?.isRead || false;
        const isDelivered = msg?.isDelivered || false;
        const readReceipt = msgId ? (readReceipts?.[msgId] ?? readReceipts?.[String(msgId)]) : null;

        if (!(isMe && isLastMessage && !isSending && !isFailed)) return null;
        return (
          <div key={`status-${msgId || idx}`} className="chat-status-line status-below">
            {isRead ? (
              <div className="chat-read-receipt">
                <div
                  className="chat-seen-avatar"
                  title={`${(readReceipt?.reader?.fullName || readReceipt?.user?.fullName || recipient?.fullName || readReceipt?.reader?.username || readReceipt?.user?.username || recipient?.username || 'Đã xem')} đang xem`}
                >
                  {readReceipt?.reader?.avatarUrl || readReceipt?.user?.avatarUrl ? (
                    <img src={readReceipt?.reader?.avatarUrl || readReceipt?.user?.avatarUrl} alt="Seen" />
                  ) : (
                    (readReceipt?.reader?.fullName || readReceipt?.reader?.username || readReceipt?.user?.fullName || readReceipt?.user?.username || recipientInitial)
                  )}
                </div>
                <span>Đã xem</span>
              </div>
            ) : isDelivered ? (
              <span>Đã nhận</span>
            ) : (
              <span>Đã gửi</span>
            )}
          </div>
        );
      })}

      {/* Typing indicators for multiple users */}
      {typingUsers && typingUsers.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginBottom: '12px', marginLeft: '8px' }}>
          {typingUsers.map(typingUser => (
            <div
              key={typingUser.userId || typingUser.username}
              style={{
                display: 'flex',
                gap: '8px',
                alignItems: 'center',
                animation: 'slideIn 0.3s ease-out'
              }}
            >
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
          ))}
        </div>
      )}
    </>
  );
});

export default MessageList;
