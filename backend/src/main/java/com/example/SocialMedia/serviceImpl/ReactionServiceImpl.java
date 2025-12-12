package com.example.SocialMedia.serviceImpl;

import com.example.SocialMedia.dto.ReactionCountResponse;
import com.example.SocialMedia.dto.ReactionRequest;
import com.example.SocialMedia.dto.ReactionResponse;
import com.example.SocialMedia.dto.ShortUserResponse;
import com.example.SocialMedia.exception.ResourceNotFound.ResourceNotFoundException;
import com.example.SocialMedia.model.coredata_model.*;
import com.example.SocialMedia.repository.*;
import com.example.SocialMedia.service.ReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReactionServiceImpl implements ReactionService {
    private final ReactionRepository reactionRepository;
    private final UserRepository userRepository;
    private final InteractableItemsRepository interactableItemsRepository;
    @Autowired
    public ReactionServiceImpl(ReactionRepository reactionRepository,
                               UserRepository userRepository,
                               InteractableItemsRepository interactableItemsRepository) {
        this.reactionRepository = reactionRepository;
        this.userRepository = userRepository;
        this.interactableItemsRepository = interactableItemsRepository;
    }
    public ReactionResponse getReaction(int userId, int interactableItemId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found" +userId));
        InteractableItems interactableItems = interactableItemsRepository.findById(interactableItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Interactable Item not found" + interactableItemId));
        Reaction reaction = reactionRepository.findReactionByInteractableItemsAndUser(interactableItems, user);
        if (reaction == null) {
            return new ReactionResponse(null, null, null, null);
        }
        else{
            return new ReactionResponse(
                    reaction.getReactionId(),
                    new ShortUserResponse(user.getId(),
                    user.getUsername()),
                    reaction.getReactionType(),
                    interactableItemId);
        }
    }
    public List<ReactionCountResponse> getReactionCount(int userId, int interactableItemId) {
        ReactionResponse reaction = getReaction(userId, interactableItemId);
        List<Object[]> reactionCount = reactionRepository.countReactionsByInteractableItemId(interactableItemId);
        return reactionCount.stream().map((type)->{
            ReactionCountResponse reactionCountResponse = new ReactionCountResponse();
            reactionCountResponse.setReactionCount(Integer.valueOf(type[1].toString()));
            reactionCountResponse.setReactionType(type[0].toString());
            reactionCountResponse.setHasUserReaction(reaction.getReactionType()!=null&&reaction.getReactionType().equals(type[0]));
            return reactionCountResponse;
        }).toList();
    }
    public void addReaction(ReactionRequest reactionRequest) {

        User user = userRepository.findById(reactionRequest.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found" + reactionRequest.getUserId()));
        InteractableItems interactableItems = interactableItemsRepository.findById(reactionRequest.getInteractableItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Interactable Item not found" + reactionRequest.getInteractableItemId()));
        Reaction reaction =reactionRepository.findReactionByInteractableItemsAndUser(interactableItems, user);
        if (reaction != null) {
            if (reaction.getReactionType().equals(reactionRequest.getReactionType())) {
                deleteReaction(reaction.getReactionId());
            }
            else{
                reaction.setReactionType(reactionRequest.getReactionType());
                reactionRepository.save(reaction);
            }
        }
        else{
            reaction = new Reaction();
            reaction.setReactionType(reactionRequest.getReactionType());
            reaction.setUser(user);
            reaction.setInteractableItems(interactableItems);
            reaction.setReactedLocalDateTime(reactionRequest.getReactedAt());
            reactionRepository.save(reaction);
        }
    }
    public void deleteReaction(int id) {
        Reaction reaction = reactionRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Reaction not found" + id));
        reactionRepository.delete(reaction);
    }
}
