import React, { useState } from 'react';
import './ChatSidebar.css'; // Import file CSS mới

const ChatSidebar = ({
  conversations,
  activeConv,
  onSelectConversation,
  me,
  onOpenProfile,
  conversationId,
  setConversationId,
  addConversation,
  onNavigateToFeed,
  onLogout
}) => {
  const [searchTerm, setSearchTerm] = useState('');

  // Hàm filter (nếu muốn search client-side đơn giản)
  const filteredConversations = conversations.filter(c =>
    c.conversationName?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <aside className="chat-sidebar">
      {/* 1. HEADER: Title to + Icons */}
      <div className="chat-sidebar-header">
        <div className="chat-sidebar-title-large">Chat</div>
        <div className="header-actions">
          {/* Nút Profile (Avatar nhỏ của mình) */}
          <button
            className="icon-btn"
            title="Trang cá nhân"
            onClick={onOpenProfile}
          >
            {me?.avatarUrl ? (
              <img src={me.avatarUrl} alt="me" />
            ) : (
              <span>{(me?.fullName?.[0] || 'M')}</span>
            )}
          </button>

          {/* Nút Feed */}
          <button className="icon-btn" title="Về trang tin" onClick={onNavigateToFeed}>
            <svg viewBox="0 0 24 24"><path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z" /></svg>
          </button>

          {/* Nút Logout */}
          <button className="icon-btn" title="Đăng xuất" onClick={onLogout}>
            <svg viewBox="0 0 24 24"><path d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z" /></svg>
          </button>
        </div>
      </div>

      {/* 2. SEARCH BAR */}
      <div className="chat-search-container">
        <div className="search-input-wrapper">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="#b0b3b8"><path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z" /></svg>
          <input
            placeholder="Tìm kiếm trên Messenger"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      {/* 3. CHAT LIST */}
      <div className="chat-list">
        {filteredConversations.length === 0 ? (
          <div style={{ textAlign: 'center', marginTop: 40, color: '#b0b3b8', fontSize: 14 }}>
            Không tìm thấy cuộc trò chuyện nào.
          </div>
        ) : filteredConversations.map(c => {
          // Logic hiển thị time
          const timeStr = c.lastMessageTime ? (() => {
            try {
              const time = new Date(c.lastMessageTime);
              const now = new Date();
              const diff = (now - time) / 1000 / 60; // minutes
              if (diff < 1) return 'vừa xong';
              if (diff < 60) return `${Math.floor(diff)}p`;
              if (diff < 1440) return time.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
              return time.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' });
            } catch (e) { return ''; }
          })() : '';

          // Logic hiển thị preview text
          const lastSender = c.lastSender || {};
          const senderName = lastSender.fullName || lastSender.username || lastSender.name || lastSender.displayName;
          const senderId = lastSender.userId || lastSender.id || lastSender.senderId;
          const myId = me?.id || me?.userId;
          const isMe = myId && senderId && String(senderId) === String(myId);
          const prefix = isMe ? 'Bạn: ' : (senderName ? `${senderName}: ` : '');

          let preview = c.lastMessageContent || 'Đã gửi file đính kèm';
          if (!c.lastMessageContent && !c.lastMessageTime) preview = 'Bắt đầu trò chuyện ngay';

          const isActive = activeConv?.conversationId === c.conversationId;
          const isUnread = c.unreadCount > 0;

          return (
            <div
              key={c.conversationId}
              className={`chat-item ${isActive ? 'active' : ''} ${isUnread ? 'unread' : ''}`}
              onClick={() => onSelectConversation(c)}
            >
              {/* Avatar */}
              <div className="avatar-wrapper">
                {c.avatarUrl ? (
                  <img src={c.avatarUrl} alt="" className="chat-item-avatar" onError={(e) => e.target.style.display = 'none'} />
                ) : (
                  <div className="avatar-placeholder">{(c.conversationName || 'U')[0]}</div>
                )}
                {/* Giả lập online status cho vui (hoặc dùng logic thật) */}
                {/* <div className="status-dot"></div> */}
              </div>

              {/* Info */}
              <div className="chat-info">
                <div className="chat-name">{c.conversationName || `Chat ${c.conversationId}`}</div>
                <div className="chat-preview-row">
                  <div className="preview-text">
                    {isUnread && <span style={{ color: '#2e89ff', marginRight: 4 }}>●</span>}
                    <span className={isUnread ? "sender-name" : ""}>{prefix}</span>
                    {preview}
                  </div>
                  <div className="chat-meta">
                    {timeStr && <span>· {timeStr}</span>}
                  </div>
                </div>
              </div>

              {/* Unread Dot (Blue) */}
              {isUnread && <div className="unread-dot"></div>}
            </div>
          );
        })}
      </div>

      {/* Debug area removed per request */}
    </aside>
  );
};

export default ChatSidebar;