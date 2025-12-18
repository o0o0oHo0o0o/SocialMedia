package com.example.SocialMedia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private int id;
    private String content;
    private ShortUserResponse user; // Info người comment
    private Integer interactableItemId;
    private Integer parentCommentId; // Null nếu là comment gốc
    private boolean replied; // Có người trả lời chưa?

    private Map<String, Long> reactionCounts;

    private LocalDateTime createdAt;
}