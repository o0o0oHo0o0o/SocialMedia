package com.example.SocialMedia.serviceImpl;

import com.example.SocialMedia.dto.PostRequest;
import com.example.SocialMedia.model.coredata_model.InteractableItems;
import com.example.SocialMedia.model.coredata_model.Post;
import com.example.SocialMedia.model.coredata_model.User;
import com.example.SocialMedia.repository.InteractableItemsRepository;
import com.example.SocialMedia.repository.PostRepository;
import com.example.SocialMedia.repository.UserRepository;
import com.example.SocialMedia.service.PostService;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private InteractableItemsRepository interactableItemsRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public Post getPostById(Integer id) {
        return postRepository.findByPostId(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    @Override
    public Post createPost(PostRequest postRequest) {
        // Find user by username (if username exists)
        User user = userRepository.findById(postRequest.getUserID())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find interactable item (assuming you have logic to fetch it)
        InteractableItems item = new InteractableItems();
        item.setItemType("POST");
        item.setCreatedAt(postRequest.getCreatedAt());
        interactableItemsRepository.save(item);

        // Create new Post entity
        Post post = new Post();
        post.setContent(postRequest.getContent());
        post.setPostTopic(postRequest.getPostTopic());
        post.setLocation(postRequest.getLocation());
        post.setUser(user); // Set the user who is creating the post
        post.setInteractableItem(item);  // Set the interactable item
        post.setCreatedLocalDateTime(postRequest.getCreatedAt());  // Set the creation time

        // Save the post using the repository
        return postRepository.save(post);
    }

    @Override
    public List<Post> getPostByUserId(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return postRepository.findByUser(user);
    }

    @Override
    public Post updatePost(PostRequest postRequest) {

        // Find the post by ID
        Post post = postRepository.findByPostId(postRequest.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        System.out.println("Received id: " + postRequest.getPostId());
        // Update only the fields that are not null
        if (postRequest.getContent() != null) {
            post.setContent(postRequest.getContent());
        }
        if (postRequest.getPostTopic() != null) {
            post.setPostTopic(postRequest.getPostTopic());
        }
        if (postRequest.getLocation() != null) {
            post.setLocation(postRequest.getLocation());
        }

        post.setUpdatedLocalDateTime(postRequest.getUpdatedAt());  // Set the creation time

        // Save the post using the repository
        return postRepository.save(post);
    }
}
