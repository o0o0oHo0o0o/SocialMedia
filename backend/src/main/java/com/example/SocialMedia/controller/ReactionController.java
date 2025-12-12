package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.ReactionCountResponse;
import com.example.SocialMedia.dto.ReactionRequest;
import com.example.SocialMedia.dto.ReactionResponse;
import com.example.SocialMedia.service.ReactionService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reactions")
public class ReactionController {
    private final ReactionService reactionService;

    public ReactionController(ReactionService reactionService) {this.reactionService = reactionService;}

    @GetMapping("/{userId}/{interactableItemId}")
    public List<ReactionCountResponse> findReactionByUserIdAndInteractableItemId(@PathVariable int userId, @PathVariable int interactableItemId) {
        return reactionService.getReactionCount(userId, interactableItemId);
    }
    @PostMapping()
    public void addReaction(@RequestBody ReactionRequest reactionRequest) {
        reactionRequest.setReactedAt(LocalDateTime.now());
        reactionService.addReaction(reactionRequest);
    }
    @DeleteMapping("/{id}")
    public void deleteReaction(@PathVariable int id) {
        reactionService.deleteReaction(id);
    }
}
