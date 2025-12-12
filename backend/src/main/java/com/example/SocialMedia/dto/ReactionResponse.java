package com.example.SocialMedia.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReactionResponse {
    private Integer id;
    private ShortUserResponse user;
    private String reactionType;
    private Integer interactableItemId;
    public ReactionResponse(Integer id, ShortUserResponse user, String reactionType, Integer interactableItemId) {
        this.id = id;
        this.user = user;
        this.reactionType = reactionType;
        this.interactableItemId = interactableItemId;
    }
}
