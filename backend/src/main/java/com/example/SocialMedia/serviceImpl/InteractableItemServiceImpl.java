package com.example.SocialMedia.serviceImpl;

import com.example.SocialMedia.model.coredata_model.InteractableItems;
import com.example.SocialMedia.repository.InteractableItemsRepository;
import com.example.SocialMedia.service.InteractableItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class InteractableItemServiceImpl implements InteractableItemService {
    private final InteractableItemsRepository interactableItemsRepository;

    @Autowired
    public InteractableItemServiceImpl(InteractableItemsRepository interactableItemsRepository) {
        this.interactableItemsRepository = interactableItemsRepository;
    }

    @Override
    public InteractableItems createInteractableItems(String type, LocalDateTime createdAt) {
        InteractableItems item = new InteractableItems();
        item.setItemType("POST");
        item.setCreatedAt(createdAt);
        return interactableItemsRepository.save(item);
    }
}
