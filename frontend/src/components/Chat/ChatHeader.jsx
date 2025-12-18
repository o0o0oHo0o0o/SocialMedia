import React from 'react';

const ChatHeader = ({ activeConv, meInitial, onToggleDrawer, onOpenProfile }) => {
  return (
    <header className="chat-header">
      <div className="chat-header-left">
        <div className="chat-avatar">{(activeConv?.conversationName || 'C')[0]}</div>
        <div className="chat-header-title">{activeConv?.conversationName || `Conversation ${activeConv?.conversationId}`}</div>
      </div>
      <div className="chat-header-actions">
        <button
          className="chat-icon-btn"
          onClick={onToggleDrawer}
          title="Chi tiết đoạn chat"
          style={{ position: 'relative', zIndex: 100 }}
        >
          ⋯
        </button>
      </div>
    </header>
  );
};

export default ChatHeader;
