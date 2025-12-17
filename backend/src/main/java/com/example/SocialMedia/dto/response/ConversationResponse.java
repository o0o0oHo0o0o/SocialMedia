package com.example.SocialMedia.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversationResponse {
    private Integer conversationId;
    private String conversationName;
    private String avatarUrl;
    private boolean isGroup;

    // Last Message Info
    private String lastMessageContent;
    private String lastMessageTime;
    private String lastMessageType; // TEXT, IMAGE...
    private SenderDto lastSender;

    // Status
    private long unreadCount;
    private boolean isMuted;
}