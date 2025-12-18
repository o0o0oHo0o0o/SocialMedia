// import { useEffect, useRef, useState } from 'react';
// import api from '../services/api';
// import { connectSocket, disconnectSocket, subscribeConversation } from '../services/chatSocket';
// import { useEffect, useRef, useState } from 'react';
// import api from '../services/api';
// import { connectSocket, disconnectSocket, subscribeConversation } from '../services/chatSocket';

// // useChatMessages now DOES NOT manage `messages` state.
// // Parent (Messenger) must pass an `onMessage` callback to receive incoming messages.
// export function useChatMessages({ activeConv, me, wsBase, onMessage }) {
//   const [msgLoading, setMsgLoading] = useState(false);
//   const [hasMoreMessages, setHasMoreMessages] = useState(true);
//   const [connected, setConnected] = useState(false);
//   const [typingUsers, setTypingUsers] = useState([]);

//   const msgContainerRef = useRef(null);
//   const subRef = useRef(null);
//   const typingTimers = useRef({});
//   const wsUrl = useRef(null);

//   useEffect(() => {
//     if (!wsBase) return;
//     const isHttps = wsBase.startsWith('https://');
//     const scheme = isHttps ? 'wss://' : 'ws://';
//     const rest = wsBase.replace(/^https?:\/\//, '');
//     wsUrl.current = `${scheme}${rest}/ws`;
//   }, [wsBase]);

//   useEffect(() => {
//     let mounted = true;
//     (async () => {
//       try {
//         const tok = await api.getWebSocketToken();
//         if (tok?.token) {
//           try {
//             await connectSocket(wsUrl.current, {
//               onConnect: () => { if (mounted) setConnected(true); },
//               onError: (err) => { console.error('[useChatMessages] WS error', err); },
//               connectHeaders: { 'X-WS-TOKEN': tok.token }
//             });
//           } catch (e) {
//             console.error('[useChatMessages] Failed to connect socket', e);
//           }
//         }
//       } catch (e) {
//         console.error('[useChatMessages] Failed to get WS token', e);
//       }
//     })();
//     return () => { mounted = false; disconnectSocket(); };
//   }, []);

//   useEffect(() => {
//     if (!connected || !activeConv) return;
//     if (subRef.current) {
//       try { subRef.current.unsubscribe(); } catch (e) { /* noop */ }
//       subRef.current = null;
//     }

//     let retryCount = 0;
//     const maxRetries = 5;
//     const trySubscribe = () => {
//       try {
//         const result = subscribeConversation(activeConv.conversationId, (evt) => {
//           const type = evt?.type;
//           if (type === 'TYPING') {
//             const isTyping = Boolean(evt?.payload?.isTyping);
//             const username = evt?.payload?.username;
//             const evtUserId = evt?.payload?.userId;
//             const isFromMe = me && ((evtUserId && evtUserId === me.id) || (username && username === me.username));
//             if (isFromMe) return;
//             const userKey = evtUserId || username;
//             if (isTyping && username && userKey) {
//               setTypingUsers(prev => {
//                 const exists = prev.some(u => (u.userId || u.username) === userKey);
//                 if (exists) return prev;
//                 return [...prev, { username, userId: evtUserId }];
//               });
//               if (typingTimers.current[userKey]) clearTimeout(typingTimers.current[userKey]);
//               typingTimers.current[userKey] = setTimeout(() => {
//                 setTypingUsers(prev => prev.filter(u => (u.userId || u.username) !== userKey));
//                 delete typingTimers.current[userKey];
//               }, 5000);
//             } else if (userKey) {
//               setTypingUsers(prev => prev.filter(u => (u.userId || u.username) !== userKey));
//               if (typingTimers.current[userKey]) { clearTimeout(typingTimers.current[userKey]); delete typingTimers.current[userKey]; }
//             }
//             return;
//           }

//           if (type === 'NICKNAME_UPDATE') {
//             // bubble up as side-effect; parent can refresh conversations via other hooks
//             return;
//           }

//           if (type === 'NAME_UPDATE' || type === 'CONVERSATION_UPDATE') {
//             // bubble up
//             return;
//           }

//           try {
//             const msgId = evt?.payload?.messageId || evt?.messageId;
//             const senderId = evt?.payload?.sender?.userId || evt?.sender?.userId;
//             const messageType = evt?.payload?.messageType || evt?.messageType;
//             import { useEffect, useRef, useState } from 'react';
//             import api from '../services/api';
//             import { connectSocket, disconnectSocket, subscribeConversation } from '../services/chatSocket';

//             // useChatMessages now DOES NOT manage `messages` state.
//             // Parent (Messenger) must pass an `onMessage` callback to receive incoming messages.
//             export function useChatMessages({ activeConv, me, wsBase, onMessage }) {
//               const [msgLoading, setMsgLoading] = useState(false);
//               const [hasMoreMessages, setHasMoreMessages] = useState(true);
//               const [connected, setConnected] = useState(false);
//               const [typingUsers, setTypingUsers] = useState([]);

//               const msgContainerRef = useRef(null);
//               const subRef = useRef(null);
//               const typingTimers = useRef({});
//               const wsUrl = useRef(null);

//               useEffect(() => {
//                 if (!wsBase) return;
//                 const isHttps = wsBase.startsWith('https://');
//                 const scheme = isHttps ? 'wss://' : 'ws://';
//                 const rest = wsBase.replace(/^https?:\/\//, '');
//                 wsUrl.current = `${scheme}${rest}/ws`;
//               }, [wsBase]);

//               useEffect(() => {
//                 let mounted = true;
//                 (async () => {
//                   try {
//                     const tok = await api.getWebSocketToken();
//                     if (tok?.token) {
//                       try {
//                         await connectSocket(wsUrl.current, {
//                           onConnect: () => { if (mounted) setConnected(true); },
//                           onError: (err) => { console.error('[useChatMessages] WS error', err); },
//                           connectHeaders: { 'X-WS-TOKEN': tok.token }
//                         });
//                       } catch (e) {
//                         console.error('[useChatMessages] Failed to connect socket', e);
//                       }
//                     }
//                   } catch (e) {
//                     console.error('[useChatMessages] Failed to get WS token', e);
//                   }
//                 })();
//                 return () => { mounted = false; disconnectSocket(); };
//               }, []);

//               useEffect(() => {
//                 if (!connected || !activeConv) return;
//                 if (subRef.current) {
//                   try { subRef.current.unsubscribe(); } catch (e) { /* noop */ }
//                   subRef.current = null;
//                 }

//                 let retryCount = 0;
//                 const maxRetries = 5;
//                 const trySubscribe = () => {
//                   try {
//                     const result = subscribeConversation(activeConv.conversationId, (evt) => {
//                       const type = evt?.type;
//                       if (type === 'TYPING') {
//                         const isTyping = Boolean(evt?.payload?.isTyping);
//                         const username = evt?.payload?.username;
//                         const evtUserId = evt?.payload?.userId;
//                         const isFromMe = me && ((evtUserId && evtUserId === me.id) || (username && username === me.username));
//                         if (isFromMe) return;
//                         const userKey = evtUserId || username;
//                         if (isTyping && username && userKey) {
//                           setTypingUsers(prev => {
//                             const exists = prev.some(u => (u.userId || u.username) === userKey);
//                             if (exists) return prev;
//                             return [...prev, { username, userId: evtUserId }];
//                           });
//                           if (typingTimers.current[userKey]) clearTimeout(typingTimers.current[userKey]);
//                           typingTimers.current[userKey] = setTimeout(() => {
//                             setTypingUsers(prev => prev.filter(u => (u.userId || u.username) !== userKey));
//                             delete typingTimers.current[userKey];
//                           }, 5000);
//                         } else if (userKey) {
//                           setTypingUsers(prev => prev.filter(u => (u.userId || u.username) !== userKey));
//                           if (typingTimers.current[userKey]) { clearTimeout(typingTimers.current[userKey]); delete typingTimers.current[userKey]; }
//                         }
//                         return;
//                       }

//                       if (type === 'NICKNAME_UPDATE') {
//                         // bubble up as side-effect; parent can refresh conversations via other hooks
//                         return;
//                       }

//                       if (type === 'NAME_UPDATE' || type === 'CONVERSATION_UPDATE') {
//                         // bubble up
//                         return;
//                       }

//                       try {
//                         const msgId = evt?.payload?.messageId || evt?.messageId;
//                         const senderId = evt?.payload?.sender?.userId || evt?.sender?.userId;
//                         const messageType = evt?.payload?.messageType || evt?.messageType;
//                         const isNotificationMsg = messageType === 'NOTIFICATION';
//                         if (!isNotificationMsg && me && senderId && Number(senderId) === Number(me.id)) return;

//                         // Normalize event to wrapper format
//                         let newMessage;
//                         if (evt?.payload) {
//                           newMessage = {
//                             ...evt,
//                             payload: {
//                               ...evt.payload,
//                               isRead: evt.payload?.isRead ?? false,
//                               isDelivered: evt.payload?.isDelivered ?? false,
//                               status: undefined
//                             }
//                           };
//                         } else {
//                           newMessage = { payload: { ...evt, isRead: evt.isRead ?? false, isDelivered: evt.isDelivered ?? false, status: undefined }, type: 'MESSAGE' };
//                         }

//                         // notify parent (Messenger) so it remains the single source of truth for messages
//                         if (typeof onMessage === 'function') {
//                           try { onMessage(newMessage); } catch (e) { console.error('[useChatMessages] onMessage callback failed', e); }
//                         }
//                       } catch (e) { console.error('[useChatMessages] Error processing evt', e); }
//                     });

//                     if (result) { subRef.current = result; }
//                     else if (retryCount < maxRetries) { retryCount++; setTimeout(trySubscribe, 200); }
//                   } catch (e) {
//                     console.error('[useChatMessages] Subscribe failed', e);
//                     if (retryCount < maxRetries) { retryCount++; setTimeout(trySubscribe, 200); }
//                   }
//                 };

//                 setTimeout(trySubscribe, 100);

//                 return () => {
//                   if (subRef.current) { try { subRef.current.unsubscribe(); } catch (e) { } subRef.current = null; }
//                 };
//               }, [connected, activeConv, me, onMessage]);

//               return {
//                 msgLoading,
//                 hasMoreMessages,
//                 msgContainerRef,
//                 typingUsers
//               };
//             }
