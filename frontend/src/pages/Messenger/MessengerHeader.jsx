import React from 'react';
import ChatHeader from '../../components/Chat/ChatHeader';

export default function MessengerHeader({ activeConv, me, onToggleDrawer, onOpenProfile }) {
  const meInitial = me?.fullName?.[0] || me?.username?.[0] || 'M';
  return (
    <ChatHeader
      activeConv={activeConv}
      meInitial={meInitial}
      onToggleDrawer={onToggleDrawer}
      onOpenProfile={onOpenProfile}
    />
  );
}
