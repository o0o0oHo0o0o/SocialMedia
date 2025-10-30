package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.PostRequest;
import com.example.SocialMedia.dto.PostResponse;
import com.example.SocialMedia.service.PostService;
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
        return postService.getPostById(id);
    }
    // New POST method to create a post
    @PostMapping
    public PostResponse createPost(@RequestBody PostRequest postRequest) {
        // Create the post using the service
        postRequest.setCreatedAt(LocalDateTime.now());
        return postService.createPost(postRequest);
    }
    // Get posts from a user
    @GetMapping("/user/{id}")
    public List<PostResponse> getPostByUserId(@PathVariable int id) {
        return postService.getPostByUserId(id);
    }
    // Update a post
    @PostMapping("/{id}")
    public PostResponse updatePost(@PathVariable Integer id, @RequestBody PostRequest postRequest) {
        postRequest.setPostId(id);
        postRequest.setUpdatedAt(LocalDateTime.now());
        return postService.updatePost(postRequest);
    }
    // Delete a post
    @DeleteMapping("/{id}")
    public String deletePost(@PathVariable int id) {
        try {
            postService.deletePost(id);
            return "Post deleted successfully";
        } catch (Exception e) {
            return "Post not found";
        }
    }
}
