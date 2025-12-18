import React from 'react';

const ChatSidebar = ({
  conversations,
  activeConv,
  onSelectConversation,
  me,
  conversationId,
  setConversationId,
  addConversation
}) => {
  return (
    <aside className="chat-sidebar">
      <div className="chat-sidebar-header">
        <div className="chat-sidebar-user">
          <div className="chat-avatar chat-avatar-me" title="Thông tin của tôi">
            {me?.fullName?.[0] || me?.username?.[0] || 'M'}
          </div>
          <div className="chat-sidebar-title">Đoạn chat</div>
        </div>
      </div>
      <div className="chat-search"><input placeholder="Tìm kiếm trên Messenger" /></div>
      <div className="chat-list">
        {conversations.length === 0 ? (
          <div className="chat-empty">Thêm Conversation ID để bắt đầu</div>
        ) : conversations.map(c => {
          const previewText = c.lastMessageContent || 'Bắt đầu trò chuyện';
          const maxLength = 30;
          const truncatedPreview = previewText.length > maxLength
            ? previewText.substring(0, maxLength) + '...'
            : previewText;

          const timeStr = c.lastMessageTime ? (() => {
            try {
              const time = new Date(c.lastMessageTime);
              const now = new Date();
              const diff = (now - time) / 1000 / 60;
              if (diff < 1) return 'vừa xong';
              if (diff < 60) return `${Math.floor(diff)}m`;
              if (diff < 1440) return time.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
              return time.toLocaleDateString('vi-VN');
            } catch (e) { return ''; }
          })() : '';

          const senderPrefix = c.lastSender?.fullName || c.lastSender?.username ? `${c.lastSender.fullName || c.lastSender.username}: ` : '';

          return (
            <div key={c.conversationId} className={`chat-item ${activeConv?.conversationId === c.conversationId ? 'active' : ''}`} onClick={() => onSelectConversation(c)}>
              <div className="chat-avatar">{(c.conversationName || 'C')[0]}</div>
              <div>
                <div className="chat-title">{c.conversationName || `Conversation ${c.conversationId}`}</div>
                <div className="chat-preview" title={c.lastMessageContent || ''}>
                  {senderPrefix}{truncatedPreview}
                </div>
              </div>
              <div className="chat-time">{timeStr}</div>
              {c.unreadCount ? <div className="chat-unread">{c.unreadCount}</div> : null}
            </div>
          );
        })}
      </div>
      <div style={{ padding: '8px' }}>
        <input value={conversationId} onChange={(e) => setConversationId(e.target.value)} placeholder="Conversation ID" style={{ width: '100%', padding: '8px', borderRadius: '6px' }} />
        <button onClick={addConversation} style={{ marginTop: '8px', width: '100%', padding: '8px', borderRadius: '6px', background: '#667eea', color: 'white', border: 'none' }}>Open</button>
      </div>
    </aside>
  );
};

export default ChatSidebar;
