package com.example.SocialMedia.controller;

import com.example.SocialMedia.model.coredata_model.Post;
import com.example.SocialMedia.service.PostService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/post")
public class PostController {
    public final PostService postService;
    public PostController(PostService postService) {
        this.postService = postService;
    }
    @GetMapping("/allPost")
    public List<Post> getAllPost() {
        postService.findAll();
        return postService.findAll();
    }
    @GetMapping("/{id}")
    public Post getPostById(@PathVariable int id) {
        return postService.findById(id);
    }
}
