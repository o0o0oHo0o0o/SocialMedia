package com.example.SocialMedia.service;

import com.example.SocialMedia.dto.ReactionCountResponse;
import com.example.SocialMedia.dto.ReactionRequest;
import com.example.SocialMedia.dto.ReactionResponse;
import com.example.SocialMedia.model.coredata_model.Reaction;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
public interface ReactionService {
    ReactionResponse getReaction(int userId, int interactableItemId);
    List<ReactionCountResponse> getReactionCount(int userId, int interactableItemId);
    void addReaction(ReactionRequest reactionRequest);
    void deleteReaction(int id);
}
