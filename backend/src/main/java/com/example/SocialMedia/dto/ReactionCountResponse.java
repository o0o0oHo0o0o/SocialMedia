package com.example.SocialMedia.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReactionCountResponse {
    private String reactionType;
    private Integer reactionCount;
    private boolean hasUserReaction;
}
