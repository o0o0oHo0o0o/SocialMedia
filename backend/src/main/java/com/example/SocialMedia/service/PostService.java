package com.example.SocialMedia.service;

import com.example.SocialMedia.dto.PostRequest;
import com.example.SocialMedia.dto.PostResponse;
import com.example.SocialMedia.model.coredata_model.Post;
import java.util.List;

import com.example.SocialMedia.model.coredata_model.PostMedia;
import com.example.SocialMedia.model.coredata_model.Reaction;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface PostService {
    PostResponse getPostById(Integer id);
    PostResponse createPost(PostRequest postRequest);
    List<PostResponse> getPostByUserId(Integer id, Pageable pageable);
    PostResponse updatePost(PostRequest postRequest);
    PostResponse deletePost(int postId);
}
