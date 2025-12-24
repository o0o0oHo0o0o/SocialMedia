import React from 'react';
import Dialog from '../../Common/Dialog';

/**
 * ProfileDialog - Dialog hiển thị thông tin tài khoản hiện tại
 */
const ProfileDialog = ({ open, onClose, me }) => {
  return (
    <Dialog
      open={open}
      title="Thông tin tài khoản"
      onClose={onClose}
      actions={<button className="dialog-btn primary" onClick={onClose}>Đóng</button>}
    >
      {me ? (
        <div style={{ display: 'grid', gridTemplateColumns: '120px 1fr', gap: 12, alignItems: 'center' }}>
          <div className="chat-avatar" style={{ width: 80, height: 80, fontSize: 28, overflow: 'hidden' }}>
            {me.avatarUrl ? (
              <img src={me.avatarUrl} alt="avatar" style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: '50%' }} referrerPolicy="no-referrer" onError={(e) => { e.currentTarget.onerror = null; e.currentTarget.src = ''; }} />
            ) : (
              (me.fullName?.[0] || me.username?.[0] || 'M')
            )}
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
  );
};

export default ProfileDialog;
