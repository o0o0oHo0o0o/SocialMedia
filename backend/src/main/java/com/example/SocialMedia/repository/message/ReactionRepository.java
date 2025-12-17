package com.example.SocialMedia.repository.message;

import com.example.SocialMedia.model.coredata_model.InteractableItems;
import com.example.SocialMedia.model.coredata_model.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Integer> {
    Optional<Reaction> findByInteractableItemsAndUser_Id(InteractableItems item, Integer userId);
    List<Reaction> findByInteractableItems_InteractableItemIdIn(List<Integer> itemIds);
}
