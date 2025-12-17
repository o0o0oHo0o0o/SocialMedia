package com.example.SocialMedia.dto.request;


import com.example.SocialMedia.constant.ReactionType;
import lombok.Data;

@Data
public class ReactionRequest {
    private Long messageId;
    private ReactionType reactionType; // e.g., LIKE, LOVE, LAUGH, SAD,
}
