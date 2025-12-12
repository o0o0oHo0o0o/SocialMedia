package com.example.SocialMedia.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReactionRequest {
    private int id;
    private Integer interactableItemId;
    private Integer userId;
    private String targetType; //SHARE, POST, COMMENT
    private String reactionType;
    private LocalDateTime reactedAt;
}
