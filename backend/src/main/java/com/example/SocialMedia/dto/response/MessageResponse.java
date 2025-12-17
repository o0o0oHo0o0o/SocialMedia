package com.example.SocialMedia.dto.response;

import com.example.SocialMedia.constant.ReactionType;
import com.example.SocialMedia.dto.message.MessageStatusSummary;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class MessageResponse {
    private Long messageId;
    private Integer conversationId;
    private SenderDto sender; // Chứa id, name, avatar
    private String content;
    private List<MediaDto> media;
    private String sentAt;
    private Long replyToMessageId;
    private Boolean isRead;
    private Boolean isDelivered;
    private Map<ReactionType, Long> reactionCounts;

    private ReactionType myReactionType;
    private MessageStatusSummary statusSummary;
}