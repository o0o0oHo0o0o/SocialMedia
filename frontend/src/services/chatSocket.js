let clientInstance = null;
let isConnected = false;
let currentHeaders = {};

export async function connectSocket(wsUrl, { onConnect, onError, connectHeaders } = {}) {
  if (clientInstance && isConnected) return clientInstance;
  const { Client } = await import('@stomp/stompjs');
  currentHeaders = connectHeaders || {};
  const client = new Client({
    brokerURL: wsUrl,
    reconnectDelay: 3000,
    connectHeaders: currentHeaders,
    onConnect: () => {
      isConnected = true;
      if (onConnect) try { onConnect(); } catch (e) { void e; }
    },
    onStompError: (frame) => {
      if (onError) try { onError(frame); } catch (e) { void e; }
    },
    onWebSocketError: (ev) => {
      if (onError) try { onError(ev); } catch (e) { void e; }
    }
  });
  client.activate();
  clientInstance = client;
  return client;
}

export function disconnectSocket() {
  if (clientInstance) {
    try { clientInstance.deactivate(); } catch (e) { void e; }
  }
  clientInstance = null;
  isConnected = false;
}

export function getClient() {
  return clientInstance;
}

export function subscribeConversation(conversationId, cb) {
  const client = clientInstance;
  if (!client || !isConnected) return null;
  const dest = `/topic/chat.${conversationId}`;
  return client.subscribe(dest, (msg) => {
    try {
      const body = JSON.parse(msg.body);
      cb(body);
    } catch (e) { cb({ raw: msg.body }); }
  });
}

export function sendTyping(conversationId, isTyping, userId, username) {
  const client = clientInstance;
  if (!client || !isConnected) {
    console.warn('[Typing] Cannot send - client not connected');
    return;
  }
  const body = JSON.stringify({
    conversationId: Number(conversationId),
    isTyping: !!isTyping,
    userId: userId,
    username: username,
    timestamp: new Date().toISOString()
  });
  console.log('[Typing] Sending body:', body);
  try {
    client.publish({ destination: '/app/chat.typing', body });
    console.log('[Typing] Sent:', isTyping ? 'typing...' : 'stopped');
  } catch (e) {
    console.error('[Typing] Failed to send:', e);
  }
}
