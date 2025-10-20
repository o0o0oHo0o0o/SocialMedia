package com.example.SocialMedia.serviceImpl;

import com.example.SocialMedia.model.coredata_model.Post;
import com.example.SocialMedia.repository.PostRepository;
import com.example.SocialMedia.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    @Override
    public List<Post> findAll() {
        return postRepository.findAll();
    }
}
