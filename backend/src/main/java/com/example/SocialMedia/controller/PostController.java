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

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    PostResponse response;
    @GetMapping("/{id}")
    public PostResponse getPostById(@PathVariable int id) {
        // Fetch post from the service layer
        Post post = postService.getPostById(id);

        // Convert Post entity to PostResponse DTO
        response = new PostResponse();
        response.setId(post.getPostId());
        response.setContent(post.getContent());
        response.setPostTopic(post.getPostTopic());
        response.setLocation(post.getLocation());
        response.setUsername(post.getUser().getUsername());  // Assuming 'User' has 'username'
        response.setCreatedAt(post.getCreatedLocalDateTime());
        return response;
    }
}
