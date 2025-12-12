package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.CommentRequest;
import com.example.SocialMedia.dto.CommentResponse;
import com.example.SocialMedia.service.CommentService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {this.commentService = commentService;}

    //Get a post by ID
    @GetMapping("/{id}")
    public List<CommentResponse> getPostById(@PathVariable int id, Pageable pageable) {
        return commentService.getCommentsByPostId(id, pageable);
    }

    @GetMapping("user/{id}")
    public List<CommentResponse> getUserComments(@PathVariable int id, Pageable pageable) {
        return commentService.getCommentsByUserId(id, pageable);
    }

    @GetMapping("parent/{id}")
    public List<CommentResponse> getParentComments(@PathVariable int id, Pageable pageable) {
        return commentService.getCommentsByParentCommentId(id, pageable);
    }

    @PostMapping
    public CommentResponse createPost(@RequestBody CommentRequest commentRequest) {
        // Create the post using the service
        commentRequest.setCreatedAt(LocalDateTime.now());
        return commentService.createComment(commentRequest);
    }

    // Delete a post
    @DeleteMapping("/{id}")
    public CommentResponse deletePost(@PathVariable int id) {
        return commentService.deleteComment(id);
    }
}
