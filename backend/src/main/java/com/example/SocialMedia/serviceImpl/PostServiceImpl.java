package com.example.SocialMedia.serviceImpl;

import com.example.SocialMedia.dto.PostRequest;
import com.example.SocialMedia.model.coredata_model.Post;
import com.example.SocialMedia.repository.PostRepository;
import com.example.SocialMedia.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Override
    public Post getPostById(long id) {
        return postRepository.findByPostId(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    @Override
    public Post createPost(PostRequest postRequest) {
        // Find user by username (if username exists)
        User user = userRepository.findByUsername(postRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find interactable item (assuming you have logic to fetch it)
        InteractableItems interactableItem = interactableItemsRepository.findById(postRequest.getInteractableItemId())
                .orElseThrow(() -> new RuntimeException("Interactable item not found"));

        // Create new Post entity
        Post post = new Post();
        post.setContent(postRequest.getContent());
        post.setPostTopic(postRequest.getPostTopic());
        post.setLocation(postRequest.getLocation());
        post.setUser(user); // Set the user who is creating the post
        post.setInteractableItem(interactableItem);  // Set the interactable item
        post.setCreatedLocalDateTime(LocalDateTime.now());  // Set the creation time

        // Save the post using the repository
        Post savedPost = postRepository.save(post);

        // Return the saved post
        return savedPost;
    }
}
