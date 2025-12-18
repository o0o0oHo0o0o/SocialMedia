import React, { useState, useEffect } from 'react';
import api from '../../services/api';
import MemberNicknameList from './MemberNicknameList';
import PhotoGallery from './PhotoGallery';
import FileList from './FileList';
import { useConversationPhotos, useConversationFiles, useConversationMembers } from '../../hooks/useConversationMedia';

/**
 * ChatDrawer - Panel bên phải hiển thị thông tin đoạn chat
 * Gồm: Nicknames, Photos, Files
 */
const ChatDrawer = ({ open, conversationId, onClose, isGroupChat = false, groupName = '', refreshKey = 0, onRenameGroup, onNicknameUpdated }) => {
  const [activeTab, setActiveTab] = useState('members'); // 'members', 'photos', 'files'
  const [prevConversationId, setPrevConversationId] = useState(null);

  const { members, loadingMembers, fetchMembers, resetMembers } = useConversationMembers(conversationId);
  const { photos, hasMorePhotos, loadingPhotos, fetchPhotos, resetPhotos } = useConversationPhotos(conversationId);
  const { files, hasMoreFiles, loadingFiles, fetchFiles, resetFiles } = useConversationFiles(conversationId);

  // Reset ALL data and refetch when conversationId changes
  useEffect(() => {
    if (conversationId && conversationId !== prevConversationId) {
      console.log('[ChatDrawer] Conversation changed:', prevConversationId, '->', conversationId);
      setPrevConversationId(conversationId);

      // Reset all data first
      resetMembers();
      resetPhotos();
      resetFiles();

      // Refetch all data for new conversation when drawer is open
      if (open) {
        fetchMembers();
        fetchPhotos(0);
        fetchFiles(0);
        // Reset to members tab when switching conversation
        setActiveTab('members');
      }
    }
  }, [conversationId, prevConversationId, open, fetchMembers, fetchPhotos, fetchFiles, resetMembers, resetPhotos, resetFiles]);

  // Refetch when refreshKey changes (triggered by socket events)
  useEffect(() => {
    if (open && conversationId && refreshKey > 0) {
      console.log('[ChatDrawer] Refreshing due to refreshKey:', refreshKey);
      fetchMembers();
    }
  }, [refreshKey, open, conversationId, fetchMembers]);

  // Debug logging to help trace why content may be empty
  useEffect(() => {
    console.log('[ChatDrawer] render debug:', { open, conversationId, activeTab, membersCount: members.length, photosCount: photos.length, filesCount: files.length });
  }, [open, conversationId, activeTab, members.length, photos.length, files.length]);

  useEffect(() => {
    if (!open || !conversationId) return;

    // Fetch data khi drawer mở theo tab
    if (activeTab === 'members' && members.length === 0) {
      fetchMembers();
    } else if (activeTab === 'photos' && photos.length === 0) {
      fetchPhotos(0);
    } else if (activeTab === 'files' && files.length === 0) {
      fetchFiles(0);
    }
  }, [open, activeTab, members.length, photos.length, files.length, fetchMembers, fetchPhotos, fetchFiles, conversationId]);

  const handleLoadMorePhotos = () => {
    fetchPhotos(Math.ceil(photos.length / 20));
  };

  const handleLoadMoreFiles = () => {
    fetchFiles(Math.ceil(files.length / 10));
  };

  if (!open) return null;

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        right: 0,
        bottom: 0,
        width: '320px',
        background: 'linear-gradient(to bottom, #1a1a2e, #0f3460)',
        borderLeft: '1px solid rgba(255,255,255,0.1)',
        zIndex: 300,
        display: 'flex',
        flexDirection: 'column',
        boxShadow: '-4px 0 16px rgba(0,0,0,0.3)',
        animation: 'slideInRight 0.3s ease-out'
      }}
    >
      {/* Header */}
      <div style={{
        padding: '12px',
        borderBottom: '1px solid rgba(255,255,255,0.1)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        background: 'rgba(0,0,0,0.3)',
        flexDirection: 'column',
        alignItems: 'flex-start'
      }}>
        <div style={{ display: 'flex', width: '100%', alignItems: 'center', justifyContent: 'space-between' }}>
          <h3 style={{ margin: 0, color: '#fff', fontSize: '16px', fontWeight: 600 }}>
            Chi tiết đoạn chat
          </h3>
          <button
            onClick={onClose}
            style={{
              background: 'transparent',
              border: 'none',
              color: '#fff',
              fontSize: '20px',
              cursor: 'pointer',
              padding: '0 4px',
              transition: 'opacity 0.2s'
            }}
            onMouseEnter={(e) => e.target.style.opacity = '0.7'}
            onMouseLeave={(e) => e.target.style.opacity = '1'}
          >
            ✕
          </button>
        </div>
        {groupName ? (
          <div style={{ marginTop: '8px', color: '#ddd', fontSize: '13px', width: '100%', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
            {groupName}
          </div>
        ) : null}
      </div>

      {/* Tabs */}
      <div style={{
        display: 'flex',
        gap: '0',
        padding: '8px 12px',
        borderBottom: '1px solid rgba(255,255,255,0.1)',
        background: 'rgba(0,0,0,0.2)'
      }}>
        {[
          { id: 'members', label: '👥', title: 'Thành viên' },
          { id: 'photos', label: '🖼️', title: 'Ảnh' },
          { id: 'files', label: '📁', title: 'File' }
        ].map(tab => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            title={tab.title}
            style={{
              flex: 1,
              padding: '8px',
              background: activeTab === tab.id ? '#667eea' : 'rgba(255,255,255,0.05)',
              color: '#fff',
              border: 'none',
              borderRadius: '6px',
              cursor: 'pointer',
              fontSize: '16px',
              transition: 'all 0.2s',
              marginRight: '4px'
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Content */}
      <div style={{
        flex: 1,
        overflowY: 'auto',
        padding: '0'
      }}>
        {activeTab === 'members' && (
          <MemberNicknameList
            isGroupChat={isGroupChat}
            groupName={groupName}
            conversationId={conversationId}
            members={members}
            onRefresh={fetchMembers}
            onRenameGroup={onRenameGroup}
            onNicknameUpdated={onNicknameUpdated}
          />
        )}

        {activeTab === 'members' && !loadingMembers && members.length === 0 && (
          <div style={{ padding: '12px', color: '#ccc', fontSize: '13px' }}>Không có thành viên để hiển thị</div>
        )}

        {activeTab === 'photos' && (
          <PhotoGallery
            photos={photos}
            loading={loadingPhotos}
            onLoadMore={handleLoadMorePhotos}
            hasMore={hasMorePhotos}
          />
        )}

        {activeTab === 'files' && (
          <FileList
            files={files}
            loading={loadingFiles}
            onLoadMore={handleLoadMoreFiles}
            hasMore={hasMoreFiles}
          />
        )}
      </div>

      <style>{`
        @keyframes slideInRight {
          from {
            transform: translateX(100%);
            opacity: 0;
          }
          to {
            transform: translateX(0);
            opacity: 1;
          }
        }
      `}</style>
    </div>
  );
};

export default ChatDrawer;

