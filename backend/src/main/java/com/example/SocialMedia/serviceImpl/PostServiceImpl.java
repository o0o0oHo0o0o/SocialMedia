package com.example.SocialMedia.serviceImpl;

import com.example.SocialMedia.model.coredata_model.Post;
import com.example.SocialMedia.repository.PostRepository;
import com.example.SocialMedia.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    @Override
    public List<Post> findAll() {
        return postRepository.findAll();
    }
    @Override
    public Post findById(int id) {
        return postRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Post not found: " + id)
        );
    }
}
