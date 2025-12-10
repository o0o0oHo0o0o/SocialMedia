package com.example.SocialMedia.service;

import com.example.SocialMedia.model.coredata_model.Post;

import java.util.List;
import java.util.Optional;

public interface PostService {
    List<Post> findAll();
    Post findById(int id);
}
