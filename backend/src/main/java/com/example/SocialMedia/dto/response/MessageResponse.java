package com.example.SocialMedia.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MessageResponse {
    private Long messageId;
    private Integer conversationId;
    private SenderDto sender; // Chứa id, name, avatar
    private String content;
    private List<MediaDto> media; // Danh sách ảnh/video
    private String sentAt;
    private Long replyToMessageId;
    private Boolean isRead;
    private Boolean isDelivered;
}