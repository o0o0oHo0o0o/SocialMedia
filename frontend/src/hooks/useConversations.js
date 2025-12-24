import { useCallback, useEffect, useState } from 'react';
import api from '../services/api';

// Hook to encapsulate conversation list fetching and basic operations
export function useConversations() {
  const [conversations, setConversations] = useState([]);
  const [activeConv, setActiveConv] = useState(null);

  const refreshConversations = useCallback(async () => {
    try {
      // SỬA: Truyền page = 1, size = 20 (hoặc số bất kỳ > 0)
      const list = await api.getConversations(1, 10);

      const convList = Array.isArray(list) ? list : (list?.items || []);
      setConversations(convList);
      return convList;
    } catch (e) {
      console.error('[useConversations] refresh failed', e);
      return [];
    }
  }, []);

  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        const convList = await refreshConversations();
        if (mounted && convList && convList.length && !activeConv) {
          setActiveConv(convList[0]);
        }
      } catch (e) { /* ignore */ }
    })();
    return () => { mounted = false; };
  }, [refreshConversations, activeConv]);

  const addConversation = useCallback((conv) => {
    if (!conv) return;
    setConversations(prev => {
      const exists = prev.some(c => c.conversationId === conv.conversationId);
      if (exists) return prev;
      return [conv, ...prev];
    });
  }, []);

  return {
    conversations,
    setConversations,
    activeConv,
    setActiveConv,
    addConversation,
    refreshConversations
  };
}

export default useConversations;
