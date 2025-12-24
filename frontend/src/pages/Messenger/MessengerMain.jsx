import React from 'react';
import ChatSidebar from '../../components/Chat/ChatSidebar';
import MessengerHeader from './MessengerHeader';
import MessengerMessages from './MessengerMessages';
import MessengerComposer from './MessengerComposer';
import SnowBackground from '../../components/Chat/SnowBackground';

export default function MessengerMain(props) {
  const {
    conversations,
    activeConv,
    setActiveConv,
    me,
    conversationId,
    setConversationId,
    handleAddConversationFromInput,
    messages,
    typingUsers,
    msgLoading,
    msgContainerRef,
    onMessagesScroll,
    setSelectedUser,
    setUserInfoOpen,
    content,
    onContentChange,
    onTextareaKeyDown,
    files,
    isRecording,
    recordingTime,
    startRecording,
    stopRecording,
    cancelRecording,
    onPickFiles,
    removeFile,
    sendMessage,
    setDrawerOpen,
    drawerOpen,
    readReceipts,
    conversationRecipient,
    onOpenProfile,
    onReplyClick,
    replyMessage,
    onCancelReply,
    // Lấy 2 hàm này từ props ra để dùng cho gọn
    onStartCall,
    onStartVideoCall,
    onBack,
    onLogout
  } = props;

  return (
    <div className="chat-layout">
      <ChatSidebar
        conversations={conversations}
        activeConv={activeConv}
        onSelectConversation={(c) => setActiveConv(c)}
        me={me}
        conversationId={conversationId}
        onOpenProfile={onOpenProfile}
        setConversationId={setConversationId}
        addConversation={handleAddConversationFromInput}
        onNavigateToFeed={onBack}
        onLogout={onLogout}
      />

      <main className="chat-main">
        <SnowBackground />
        {activeConv ? (
          <>
            {/* --- SỬA Ở ĐÂY --- */}
            <MessengerHeader
              activeConv={activeConv}
              me={me}
              onToggleDrawer={() => setDrawerOpen(!drawerOpen)}

              // Truyền đủ cả 2 hàm xuống
              onStartCall={onStartCall}
              onStartVideoCall={onStartVideoCall}
            />
            {/* ----------------- */}

            <MessengerMessages
              msgContainerRef={msgContainerRef}
              onMessagesScroll={onMessagesScroll}
              messages={messages}
              me={me}
              activeConv={activeConv}
              typingUsers={typingUsers}
              msgLoading={msgLoading}
              setSelectedUser={setSelectedUser}
              setUserInfoOpen={setUserInfoOpen}
              onImagePreview={() => { }}
              readReceipts={readReceipts}
              conversationRecipient={conversationRecipient}
              onReplyClick={onReplyClick}
            />

            <MessengerComposer
              content={content}
              onContentChange={onContentChange}
              onTextareaKeyDown={onTextareaKeyDown}
              files={files}
              isRecording={isRecording}
              recordingTime={recordingTime}
              startRecording={startRecording}
              stopRecording={stopRecording}
              cancelRecording={cancelRecording}
              onPickFiles={onPickFiles}
              removeFile={removeFile}
              sendMessage={sendMessage}
              replyMessage={replyMessage}
              onCancelReply={onCancelReply}
            />
          </>
        ) : (
          <div className="chat-empty large">Chọn hoặc mở một cuộc trò chuyện</div>
        )}
      </main>
    </div>
  );
}