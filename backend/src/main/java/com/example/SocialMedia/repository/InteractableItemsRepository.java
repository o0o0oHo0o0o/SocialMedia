package com.example.SocialMedia.repository;

import com.example.SocialMedia.model.coredata_model.InteractableItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InteractableItemsRepository extends JpaRepository<InteractableItems, Integer> {

}
