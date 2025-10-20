package com.example.SocialMedia.service.serviceImpl;

import com.example.SocialMedia.model.coredata_model.Post;
import com.example.SocialMedia.repository.PostRepository;
import com.example.SocialMedia.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Override
    public Post getPostById(int id) {
        return postRepository.findByPostId(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }
}
