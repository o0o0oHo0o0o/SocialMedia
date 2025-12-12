package com.example.SocialMedia.repository;

import com.example.SocialMedia.model.coredata_model.InteractableItems;
import com.example.SocialMedia.model.coredata_model.Reaction;
import com.example.SocialMedia.model.coredata_model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface ReactionRepository extends JpaRepository<Reaction, Integer> {
    Reaction findReactionByInteractableItemsAndUser(InteractableItems interactableItems, User user);
    int countReactionByInteractableItems(InteractableItems interactableItems);

    @Query("SELECT r.reactionType AS reactionType, COUNT(r) AS reactionCount " +
            "FROM Reaction r WHERE r.interactableItems.interactableItemId = :interactableItemId " +
            "GROUP BY r.reactionType")
    List<Object[]> countReactionsByInteractableItemId(@Param("interactableItemId") Integer interactableItemId);

    User user(User user);
}
