package com.example.SocialMedia.controller;

import com.example.SocialMedia.model.coredata_model.Post;
import com.example.SocialMedia.dto.PostRequest;
import com.example.SocialMedia.dto.PostResponse;
import com.example.SocialMedia.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    //Get a post by ID
    @GetMapping("/{id}")
    public PostResponse getPostById(@PathVariable int id) {
        // Fetch post from the service layer
        Post post = postService.getPostById(id);

        // Convert Post entity to PostResponse DTO
        PostResponse response = new PostResponse();
        response.setId(post.getPostId());
        response.setContent(post.getContent());
        response.setPostTopic(post.getPostTopic());
        response.setLocation(post.getLocation());
        response.setUsername(post.getUser().getUsername());  // Assuming 'User' has 'username'
        response.setCreatedAt(post.getCreatedLocalDateTime());
        response.setUpdatedAt(post.getUpdatedLocalDateTime());
        return response;
    }
    // New POST method to create a post
    @PostMapping
    public PostResponse createPost(@RequestBody PostRequest postRequest) {
        // Create the post using the service
        postRequest.setCreatedAt(LocalDateTime.now());
        Post createdPost = postService.createPost(postRequest);

        // Return a successful response with the PostResponse DTO
        return getPostById(createdPost.getPostId());
    }
    // Get posts from a user
    @GetMapping("/user/{id}")
    public List<PostResponse> getPostByUserId(@PathVariable int id) {
        List<PostResponse> postResponses = new ArrayList<>();
        List<Post> posts = postService.getPostByUserId(id);
        for (Post post : posts) {
            postResponses.add(getPostById(post.getPostId()));
        }
        return postResponses;
    }
    // Update a post
    @PostMapping("/{id}")
    public PostResponse updatePost(@PathVariable Integer id, @RequestBody PostRequest postRequest) {
        postRequest.setPostId(id);
        postRequest.setUpdatedAt(LocalDateTime.now());
        Post updatedPost = postService.updatePost(postRequest);
        return getPostById(updatedPost.getPostId());
    }
}
