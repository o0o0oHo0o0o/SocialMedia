package com.example.SocialMedia.service;

import com.example.SocialMedia.dto.PostRequest;
import com.example.SocialMedia.model.coredata_model.Post;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface PostService {
    Post getPostById(Integer id);
    Post createPost(PostRequest postRequest);
    List<Post> getPostByUserId(Integer id);
    Post updatePost(PostRequest postRequest);
}
