package com.example.SocialMedia.service.message;

public interface ConversationService {
    void updateMemberNickname(Integer conversationId, Integer userId, String nickname);
}
