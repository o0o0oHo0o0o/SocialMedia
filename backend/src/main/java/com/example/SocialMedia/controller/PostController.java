package com.example.SocialMedia.controller;

import com.example.SocialMedia.model.coredata_model.Post;
import com.example.SocialMedia.dto.PostRequest;
import com.example.SocialMedia.dto.PostResponse;
import com.example.SocialMedia.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }


    @GetMapping("/{id}")
    public Post getPostById(@PathVariable int id) {
        Post response = postService.getPostById(id);
        return response;
    }
}
