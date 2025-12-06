import React from 'react';
import '../../styles/dialog.css';

export default function Dialog({ open, title, children, onClose, actions }) {
  if (!open) return null;
  return (
    <div className="dialog-backdrop" onClick={onClose}>
      <div className="dialog" onClick={e => e.stopPropagation()}>
        {title ? <div className="dialog-title">{title}</div> : null}
        <div className="dialog-content">{children}</div>
        <div className="dialog-actions">
          {actions}
        </div>
      </div>
    </div>
  );
}
