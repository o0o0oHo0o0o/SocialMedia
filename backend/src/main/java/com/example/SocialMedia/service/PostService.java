package com.example.SocialMedia.service;

import com.example.SocialMedia.dto.PostRequest;
import com.example.SocialMedia.dto.PostResponse;
import com.example.SocialMedia.model.coredata_model.Post;
import java.util.List;

import com.example.SocialMedia.model.coredata_model.PostMedia;
import com.example.SocialMedia.model.coredata_model.Reaction;
import org.springframework.stereotype.Service;

@Service
public interface PostService {
    PostResponse getPostById(Integer id);
    List<PostMedia> findMediaByPost(Post post);
    PostResponse createPost(PostRequest postRequest);
    List<PostResponse> getPostByUserId(Integer id);
    PostResponse updatePost(PostRequest postRequest);
    void deletePost(int postId);
}
