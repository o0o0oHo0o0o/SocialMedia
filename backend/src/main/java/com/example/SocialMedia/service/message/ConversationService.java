package com.example.SocialMedia.service.message;

import com.example.SocialMedia.dto.message.ConversationMemberDTO;

public interface ConversationService {
    ConversationMemberDTO updateMemberNickname(Integer conversationId, Integer userId, String nickname);
}
