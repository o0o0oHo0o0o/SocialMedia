import React from 'react';
import ChatSidebar from '../../components/Chat/ChatSidebar';
import MessengerHeader from './MessengerHeader';
import MessengerMessages from './MessengerMessages';
import MessengerComposer from './MessengerComposer';

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
    conversationRecipient
  } = props;

  return (
    <div className="chat-layout">
      <ChatSidebar
        conversations={conversations}
        activeConv={activeConv}
        onSelectConversation={(c) => setActiveConv(c)}
        me={me}
        conversationId={conversationId}
        setConversationId={setConversationId}
        addConversation={handleAddConversationFromInput}
      />

      <main className="chat-main">
        {activeConv ? (
          <>
            <MessengerHeader
              activeConv={activeConv}
              me={me}
              onToggleDrawer={() => setDrawerOpen(!drawerOpen)}
            />

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
            />
          </>
        ) : (
          <div className="chat-empty large">Chọn hoặc mở một cuộc trò chuyện</div>
        )}
      </main>
    </div>
  );
}
