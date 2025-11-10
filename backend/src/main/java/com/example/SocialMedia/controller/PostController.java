package com.example.SocialMedia.controller;

import com.example.SocialMedia.dto.PostRequest;
import com.example.SocialMedia.dto.PostResponse;
import com.example.SocialMedia.service.PostService;
import com.example.SocialMedia.exception.FileTooLargeException;
import com.example.SocialMedia.exception.TooManyFileException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;// 10 MB limit
    private static final int MAX_FILE = 10;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    void checkUploadedFile(MultipartFile[] files){
        if (files.length > MAX_FILE) {
            throw new TooManyFileException("File exceeds maximum number of 10");
        }
        for (MultipartFile f : files) {
            if (f.getSize() > MAX_FILE_SIZE) {
                throw new FileTooLargeException("File exceeds maximum size limit of 10MB");
            }
        }
    }
    //Get a post by ID
    @GetMapping("/{id}")
    public PostResponse getPostById(@PathVariable int id) {
        return postService.getPostById(id);
    }
    // New POST method to create a post
    @PostMapping
    public PostResponse createPost(@RequestParam(value = "mediaFile", required = false) MultipartFile[] file, @RequestPart("postRequest") PostRequest postRequest) {
        checkUploadedFile(file);
        postRequest.setMedias(file);
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
    public PostResponse updatePost(@PathVariable Integer id,
                                   @RequestParam(value = "mediaFile", required = false) MultipartFile[] file,
                                   @RequestParam(value = "deleteFile", required = false) int[] deleteFile,
                                   @RequestPart(value = "postRequest", required = false) PostRequest postRequest) {
        if (file != null && file.length > 0) {
            checkUploadedFile(file);
            postRequest.setMedias(file);
        }
        if (deleteFile != null && deleteFile.length > 0) {
            postRequest.setDeleteMedia(deleteFile);
        }
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
